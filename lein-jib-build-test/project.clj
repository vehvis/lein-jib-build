(defproject vaik.io/lein-jib-build-test "0.1.1-SNAPSHOT"
  :description "lein-jib-build sample project"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]]

  :main  lein-jib-build-test.core

  :plugins [[vaik.io/lein-jib-build "0.2.0.12.0.2-standalone"]]
  :jib-build/build-config {:base-image "gcr.io/distroless/java"
                           :target-image {:target-type :docker}}

  :profiles {:uberjar {:aot :all}})
