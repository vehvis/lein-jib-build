# lein-jib-build sample project

Sample project to demonstrate lein-jib-build

## Usage

Install lein and docker first.

    $ lein do uberjar, jib-build

You should now have a brand new container in your local docker repo:

    $ docker run lein-jib-build-test
    Hello, World!


## License

Apache 2.0. See ../LICENSE

