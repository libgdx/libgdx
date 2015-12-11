#!/bin/bash

BASE=$(cd $(dirname $0); pwd -P)

BUILD_DIR=$BASE/target/objectal
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

curl https://codeload.github.com/libgdx/ObjectAL-for-iPhone/legacy.tar.gz/master -o $BUILD_DIR/objectal.tar.gz

tar xvfz $BUILD_DIR/objectal.tar.gz -C $BUILD_DIR --strip-components 1

XCODEPROJ=$BUILD_DIR/ObjectAL/ObjectAL.xcodeproj

xcodebuild -project $XCODEPROJ -arch armv7  -sdk iphoneos         CONFIGURATION_BUILD_DIR=$BUILD_DIR/armv7  OTHER_CFLAGS="-fembed-bitcode -miphoneos-version-min=6.0"
xcodebuild -project $XCODEPROJ -arch arm64  -sdk iphoneos         CONFIGURATION_BUILD_DIR=$BUILD_DIR/arm64  OTHER_CFLAGS="-fembed-bitcode -miphoneos-version-min=6.0"
xcodebuild -project $XCODEPROJ -arch i386   -sdk iphonesimulator  CONFIGURATION_BUILD_DIR=$BUILD_DIR/i386   OTHER_CFLAGS="-miphoneos-version-min=6.0"
xcodebuild -project $XCODEPROJ -arch x86_64 -sdk iphonesimulator  CONFIGURATION_BUILD_DIR=$BUILD_DIR/x86_64 OTHER_CFLAGS="-miphoneos-version-min=6.0"
xcodebuild -project $XCODEPROJ -arch arm64  -sdk appletvos        CONFIGURATION_BUILD_DIR=$BUILD_DIR/tvos-arm64 OTHER_CFLAGS="-fembed-bitcode -mtvos-version-min=9.0"
xcodebuild -project $XCODEPROJ -arch x86_64 -sdk appletvsimulator CONFIGURATION_BUILD_DIR=$BUILD_DIR/tvos-x86_64 OTHER_CFLAGS="-mtvos-version-min=9.0"

lipo $BUILD_DIR/armv7/libObjectAL.a \
     $BUILD_DIR/arm64/libObjectAL.a \
     $BUILD_DIR/i386/libObjectAL.a \
     $BUILD_DIR/x86_64/libObjectAL.a \
     -create \
     -output $BUILD_DIR/libObjectAL.a

cp $BUILD_DIR/libObjectAL.a $BASE/../../gdx/libs/ios32/

lipo $BUILD_DIR/tvos-arm64/libObjectAL.a \
     $BUILD_DIR/tvos-x86_64/libObjectAL.a \
     -create \
     -output $BUILD_DIR/libObjectAL.a.tvos

cp $BUILD_DIR/libObjectAL.a.tvos $BASE/../../gdx/libs/ios32/
