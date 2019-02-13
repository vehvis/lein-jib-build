(defproject lein-jib-build-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]]

  :main  lein-jibcore-test.core

  :plugins [[lein-jib-build "0.1.1-SNAPSHOT"]]
  :jib-build/build-config {:base-image "gcr.io/distroless/java"
                           :target-image {:target-type :docker}}

  :profiles {:uberjar {:aot :all}})
