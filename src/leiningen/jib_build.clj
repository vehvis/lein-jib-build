(ns leiningen.jib-build
  (:import [com.google.cloud.tools.jib.api Jib DockerDaemonImage Containerizer TarImage RegistryImage]
           [com.google.cloud.tools.jib.api AbsoluteUnixPath]
           [com.google.common.collect ImmutableList]
           [java.io File]
           [java.util List ArrayList]
           [java.nio.file Paths]
           [com.google.cloud.tools.jib.builder ProgressEventDispatcher])
  (:require [leiningen.core.main :as lein]
            [leiningen.jar :as jar]))


(defn- get-path [filename]
  (Paths/get (.toURI (File. ^String filename))))

(defn- containerizer [project {image-config :target-image}]
  (let [image-name (:name project)]
    (case (get image-config :target-type :docker)
      #_:tar #_(do
                 (lein/info "Building tar image:" image-name)
                 (Containerizer/to (-> (TarImage/named image-name)
                                       (.saveTo (Paths/get (.toURI (File. ^String image-name)))))))
      :registry (do
                  (lein/info "Deploying into registry:" image-name)
                  (Containerizer/to (RegistryImage/named ^String image-name)))
      :docker (do
                (lein/info "Deploying to local docker:" image-name)
                (Containerizer/to (DockerDaemonImage/named ^String image-name))))))

(defn- into-list [& args]
  (ArrayList. ^List args))


(defn jib-build
  "I don't do a lot."
  [project & args]
  (let [config (:jib-build/build-config project)
        standalone-jar (jar/get-jar-filename project :standalone)
        base-image (get config :base-image "gcr.io/distroless/java")]
    (lein/info "Constructing container upon" base-image "with" standalone-jar)
    ;(println "1" (.getProtectionDomain ImmutableList))
    ;(println "2" (.getCodeSource (.getProtectionDomain ImmutableList)))
    ;(println "3" (.getLocation (.getCodeSource (.getProtectionDomain ImmutableList))))
    ;(println "4" (.toExternalForm (.getLocation (.getCodeSource (.getProtectionDomain ImmutableList)))))
    (-> (Jib/from base-image)
        (.addLayer (into-list (get-path standalone-jar)) (AbsoluteUnixPath/get "/"))
        (.setEntrypoint (into-list "java" "-jar"))
        (.setProgramArguments (into-list (.toString (.getFileName (get-path standalone-jar)))))
        (.containerize (containerizer project config)))))

