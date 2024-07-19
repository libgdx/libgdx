#!/bin/bash
set -e
BASE=$(cd $(dirname $0); pwd -P)

BUILD_DIR=$BASE/build/objectal

rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

curl https://codeload.github.com/libgdx/ObjectAL-for-iPhone/legacy.tar.gz/master -o $BUILD_DIR/objectal.tar.gz

tar xvfz $BUILD_DIR/objectal.tar.gz -C $BUILD_DIR --strip-components 1

XCODEPROJ=$BUILD_DIR/ObjectAL/ObjectAL.xcodeproj

xcodebuild -project $XCODEPROJ -arch arm64 -sdk iphoneos -target "ObjectAL-iOS-Framework" CONFIGURATION_BUILD_DIR=$BUILD_DIR/device
xcodebuild -project $XCODEPROJ -arch x86_64 -arch arm64 -sdk iphonesimulator -target "ObjectAL-iOS-Framework" CONFIGURATION_BUILD_DIR=$BUILD_DIR/simulator

xcodebuild -create-xcframework \
           -framework $BUILD_DIR/device/ObjectAL.framework \
           -debug-symbols $BUILD_DIR/device/ObjectAL.framework.dSYM \
           -framework $BUILD_DIR/simulator/ObjectAL.framework \
           -debug-symbols $BUILD_DIR/simulator/ObjectAL.framework.dSYM \
           -output $BUILD_DIR/ObjectAL.xcframework

mkdir -p $BASE/../../gdx/libs/ios32/
cp -r $BUILD_DIR/ObjectAL.xcframework $BASE/../../gdx/libs/ios32/
