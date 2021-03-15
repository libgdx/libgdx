#!/bin/bash
set -e
BASE=$(cd $(dirname $0); pwd -P)

BUILD_DIR=$BASE/target/objectal
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

curl https://codeload.github.com/libgdx/ObjectAL-for-iPhone/legacy.tar.gz/master -o $BUILD_DIR/objectal.tar.gz

tar xvfz $BUILD_DIR/objectal.tar.gz -C $BUILD_DIR --strip-components 1

XCODEPROJ=$BUILD_DIR/ObjectAL/ObjectAL.xcodeproj

xcodebuild -project $XCODEPROJ -arch armv7  -sdk iphoneos         CONFIGURATION_BUILD_DIR=$BUILD_DIR/armv7  OTHER_CFLAGS="-fembed-bitcode -target armv7-apple-ios9.0.0"
xcodebuild -project $XCODEPROJ -arch arm64  -sdk iphoneos         CONFIGURATION_BUILD_DIR=$BUILD_DIR/arm64  OTHER_CFLAGS="-fembed-bitcode -target arm64-apple-ios9.0.0"
xcodebuild -project $XCODEPROJ -arch x86_64 -sdk iphonesimulator  CONFIGURATION_BUILD_DIR=$BUILD_DIR/x86_64 OTHER_CFLAGS="-target x86_64-apple-ios9.0.0"

lipo $BUILD_DIR/armv7/libObjectAL.a \
     $BUILD_DIR/arm64/libObjectAL.a \
     $BUILD_DIR/x86_64/libObjectAL.a \
     -create \
     -output $BUILD_DIR/libObjectAL.a

mkdir -p $BASE/../../gdx/libs/ios32/
cp $BUILD_DIR/libObjectAL.a $BASE/../../gdx/libs/ios32/
