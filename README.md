# lein-jib-build

Build docker containers with Leiningen, no docker installation needed. Uses Google's [Jib](https://github.com/GoogleContainerTools/jib) toolkit.

>**Note!** This is alpha quality code and has not been thoroughly tested. Use at your own discretion (and create a PR if you improve something)

## Requirements and caveats

Clojure 1.10.0 or later is needed. Leiningen 2.9.0 or later is needed. This will probably work with Java 8 but
I've only tested with Java 11.

**Your project must emit an uberjar** or something closely like it for this plugin to be useful in its current state.

## Usage

The plugin is available on clojars: https://clojars.org/vaik.io/lein-jib-build

Configure your project.clj as follows:

```clojure
  :plugins [[vaik.io/lein-jib-build "0.2.0"]]
  :jib-build/build-config {:base-image {:type :registry
                                        :image-name "gcr.io/distroless/java"}
                           :target-image {:type :docker
                                          :image-name "helloworld"}}

```

Build the image and deploy it to the specified registry:

    $ lein do uberjar, jib-build

Build the image and deploy it to the specified registry:

    $ docker run helloworld
    Hello, world!

There's also an [example project](https://github.com/vehvis/lein-jib-build/tree/master/lein-jib-build-test) 


## Configuration options

The `jib-build/build-config` map supports the following required options:
* `:base-image {...}` - base Docker image to build upon, see below for details
* `:target-image {...}` - what to do with the built image, also see below

The following are optional:
* `:entrypoint [...]` - a vector of strings to use as the container's ENTRYPOINT value, defaults to `["java" "-jar"]`
* `:arguments "..."` - a string to give to the entrypoint as arguments, defaults to the name of the project's uberjar

#### Referring to images

These options are usable with both `base-image` and `target-image`.

```clojure
;; Deploy to your local docker daemon (requires dockerd to be running)
:target-image {:type docker
               :image-name "helloworld"}
```
```clojure
;; Deploy as a tar archive
:target-image {:type tar
               :image-name "target/helloworld.tar"}
```
```clojure
;; Deploy to a Docker registry with optional username/password authentication 
;; Please mind your security!
:target-image {:type registry
               :image-name "docker.io/foobar/helloworld"
               :username "username"   ;; optional
               :password "password"   ;; optional
               }
```

#### Pull from or push to AWS ECR, with authentication

If you're using AWS ECR there's direct support for more sophisticated authentication.

Deploy to ECR with assume-role (this is the recommended option):

```clojure
:target-image {:type registry
               :image-name "123456789.dkr.ecr.mordor-east-1.amazonaws.com/helloworld"
               :authorizer {:fn leiningen.aws-ecr-auth/ecr-auth
                            :args {:type :assume-role
                                   :role-arn "arn:aws:iam::123456789:role/nazgul"}}}
```

Deploy to ECR using a profile:

```clojure
:target-image {:type registry
               :image-name "123456789.dkr.ecr.mordor-east-1.amazonaws.com/helloworld"
               :authorizer {:fn leiningen.aws-ecr-auth/ecr-auth
                            :args {:type :profile
                                   :profile-name "nazgul"}}}
```

Using environment variables:
* `AWS_ACCESS_KEY_ID`      (required)      
* `AWS_SECRET_ACCESS_KEY`  (required)
* `AWS_SESSION_TOKEN`      (optional)
```clojure
:target-image {:type registry
               :image-name "123456789.dkr.ecr.mordor-east-1.amazonaws.com/helloworld"
               :authorizer {:fn leiningen.aws-ecr-auth/ecr-auth
                            :args {:type :environment}}}
```

With an access key:
```clojure
:target-image {:type registry
               :image-name "123456789.dkr.ecr.mordor-east-1.amazonaws.com/helloworld"
               :authorizer {:fn leiningen.aws-ecr-auth/ecr-auth
                            :args {:type :access-key
                                   :access-key-id "AK1231232414"
                                   :secret-access-key "111111111111111"}}}
```

With JVM system properties:
* `aws.accessKeyId`  (required)
* `aws.secretKey`    (required)

```clojure
:target-image {:type registry
               :image-name "123456789.dkr.ecr.mordor-east-1.amazonaws.com/helloworld"
               :authorizer {:fn leiningen.aws-ecr-auth/ecr-auth
                            :args {:type :system-properties}}}
```

#### Using a custom registry authorizer

Create a function in a namespace accessible to leiningen with a single argument:

```clojure
(defn custom-authorizer [config] 
   {:username (:username config)
    :password (apply str (reverse (:encrypted-password config))})
```

The function gets passed the `args` map:

```clojure
:target-image {:type registry
               :image-name "123456789.dkr.ecr.mordor-east-1.amazonaws.com/helloworld"
               :authorizer {:fn my-namespace/my-custom-authorizer
                            :args {:username "Sauron" :encrypted-password "TERCESYREV"}}}
```

## Building (& other irritations)

So that things would not be too easy, Leiningen includes some libraries that are a bit long in the tooth. 
And when creating plugins, those libraries override everything you bring with you.

The main culprit in this case is Guava, a current version of which is required by the `jib-core` library used 
by this plugin. Leiningen however provides an old version, so we need to shadow the Guava library inside jib-core
for it to function. 

I have placed a forked version of `jib` as a Git submodule, which contains the required shadowing configuration.

The included build.sh script does everything that's needed:

```shell script
$ ./build.sh
--- Checking that we have the required submodule
--- Build the customised jib-core
BUILD SUCCESSFUL in 2s
4 actionable tasks: 2 executed, 2 up-to-date
'jib/jib-core/build/libs/jib-core-0.12.1-SNAPSHOT-GUAVASHADOW.jar' -> 'lib/jib-core-0.12.1-SNAPSHOT-GUAVASHADOW.jar'
--- Now build the plugin
Created /..../lein-jib-build/target/lein-jib-build-0.2.0.jar
Wrote /..../lein-jib-build/pom.xml
Installed jar and pom into local repo.
```

Another similar thing is that Leiningen includes an old version of `clojure.data.xml`, which is slightly incompatible
with the `cognitect/aws-api` library used for ECR authentication. There's an ugly `with-redefs` somewhere because 
of that.

## License

Copyright 2019 Ville Vehviläinen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.