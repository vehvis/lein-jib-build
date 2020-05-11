(ns leiningen.jib-build
  (:import [com.google.cloud.tools.jib.api Jib
                                           DockerDaemonImage
                                           Containerizer
                                           TarImage
                                           RegistryImage
                                           AbsoluteUnixPath
                                           ImageReference CredentialRetriever Credential]
           [java.io File]
           [java.util List ArrayList Optional]
           [java.nio.file Paths])
  (:require [leiningen.core.main :as lein]
            [leiningen.core.classpath :as lein-cp]
            [leiningen.uberjar :as uberjar]
            [leiningen.jar :as jar]
            [clojure.pprint :as pprint]
            [leiningen.core.project :as project]))

(def default-base-image {:type :registry
                         :image-name "gcr.io/distroless/java"})
(def default-entrypoint ["java" "-jar"])


(defn- into-list
  [& args]
  (ArrayList. ^List args))

(defn- get-path [filename]
  (Paths/get (.toURI (File. ^String filename))))

(defn- to-imgref [image-config]
  (ImageReference/parse (:image-name image-config)))

(defn add-registry-credentials [rimg registry-config]
  (cond
    (:username registry-config)
    (do (lein/debug "Using username/password authentication, user:" (:username registry-config))
        (.addCredential rimg (:username registry-config) (:password registry-config)))

    (:authorizer registry-config)
    (let [auth (:authorizer registry-config)]
      (lein/debug "Using custom registry authentication:" (:authorizer registry-config))
      (.addCredentialRetriever rimg (reify CredentialRetriever
                                      (retrieve [_]
                                        (require [(symbol (namespace (:fn auth)))])
                                        (let [creds (eval `(~(:fn auth) ~(:args auth)))]
                                          (Optional/of (Credential/from (:username creds) (:password creds))))))))

    :default rimg))



(defmulti configure-image (fn [image-config project] (:type image-config)))

(defmethod configure-image :tar [{:keys [image-name]} project]
  (let [image-name (or image-name (str "target/" (:name project) ".tar"))]
    (lein/debug "Tar image:" image-name)
    (.named (TarImage/at (-> (File. ^String image-name)
                             .toURI
                             Paths/get))
            ^String image-name)))

(defmethod configure-image :registry [{:keys [image-name] :as image-config} project]
  (let [image-name (or image-name (:name project))]
    (lein/debug "Registry image:" image-name)
    (-> (RegistryImage/named ^ImageReference (to-imgref image-config))
        (add-registry-credentials image-config))))

(defmethod configure-image :docker [{:keys [image-name] :as image-config} project]
  (let [image-name (or image-name (:name project))]
    (lein/debug "Local docker:" image-name)
    (DockerDaemonImage/named ^ImageReference (to-imgref image-config))))

(defmethod configure-image :default [image-config _]
  (throw (Exception. ^String (str "Unknown image type: " (:image-name image-config)))))

(defn jib-build
  "It places the jar in the container (or else it gets the hose again)."
  [project & args]
  #_(pprint/pprint (lein-cp/ext-classpath project))
  #_(pprint/pprint args)
  (let [project (project/merge-profiles project [:uberjar])
        config (:jib-build/build-config project)
        standalone-jar (jar/get-jar-filename project :standalone)
        base-image (get config :base-image default-base-image)
        entrypoint (get config :entrypoint default-entrypoint)
        arguments (get config :arguments (.toString (.getFileName (get-path standalone-jar))))
        app-layer [(into-list (get-path standalone-jar))
                   (AbsoluteUnixPath/get "/")]]
    (lein/info "Building container upon" (:image-name base-image) "with" standalone-jar)
    (-> (Jib/from (configure-image base-image project))
        (.addLayer (first app-layer) (second app-layer))
        (.setEntrypoint (apply into-list entrypoint))
        (.setProgramArguments (into-list arguments))
        (.containerize (Containerizer/to (configure-image (:target-image config) project))))))

