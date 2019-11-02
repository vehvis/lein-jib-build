#!/bin/sh

JIB_CORE_VERSION=0.12.1-SNAPSHOT-GUAVASHADOW
# init submodule for jib
git submodule init jib || exit 1

# build jib and get the built jar
cd jib
./gradlew :jib-core:jar || exit 1
cd ..
cp -v jib/jib-core/build/libs/jib-core-$JIB_CORE_VERSION.jar lib/ || exit 1

lein do clean, install
