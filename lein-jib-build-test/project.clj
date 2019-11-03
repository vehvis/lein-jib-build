(defproject vaik.io/lein-jib-build-test "0.1.1-SNAPSHOT"
  :description "lein-jib-build sample project"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.1"]]

  :main  lein-jib-build-test.core


  :plugins [[vaik.io/lein-jib-build "0.2.0"]]
  :jib-build/build-config {:base-image {:type :registry
                                        :image-name "gcr.io/distroless/java"}
                           :target-image {:type :registry
                                          :image-name "566325148757.dkr.ecr.eu-central-1.amazonaws.com/helloworld"
                                          :authorizer {:fn leiningen.aws-ecr-auth/ecr-auth
                                                       :args {:type :assume-role
                                                              :role-arn "arn:aws:iam::566325148757:role/developer"}}}}

  :profiles {:uberjar {:aot :all
                       :uberjar-name "foo.jar"}})


