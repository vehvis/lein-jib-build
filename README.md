# lein-jibcore

Build docker containers with Leiningen, no docker installation needed. Uses Google's Jib toolkit.

## Usage

Put `[vaik.io/lein-jib.build "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your `:user`
profile.


    $ lein do uberjar, jib-core

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