#!/bin/sh

JIB_CORE_VERSION=0.12.1-SNAPSHOT-GUAVASHADOW

echo "--- Checking that we have the required submodule"
git submodule init jib || exit 1
git submodule update jib || exit 1

echo "--- Build the customised jib-core"
cd jib
./gradlew :jib-core:jar || exit 1
cd ..
mkdir -p lib || exit 1
cp -v jib/jib-core/build/libs/jib-core-$JIB_CORE_VERSION.jar lib/ || exit 1

echo "--- Now build the plugin"
lein do clean, install
