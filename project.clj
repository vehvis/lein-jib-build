(defproject vaik.io/lein-jib-build "0.2.0"
  :description "FIXME: write description"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :resource-paths ["lib"]

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.google.cloud.tools/jib-core "0.12.1-SNAPSHOT-GUAVASHADOW"]
                 [com.cognitect.aws/api "0.8.391"]
                 [com.cognitect.aws/endpoints "1.1.11.664"]
                 [com.cognitect.aws/ecr "762.2.557.0"]
                 [com.cognitect.aws/sts "747.2.533.0"]]

  :eval-in-leiningen true)
