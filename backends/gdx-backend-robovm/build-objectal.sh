#!/bin/bash
set -e
BASE=$(cd $(dirname $0); pwd -P)

BUILD_DIR=$BASE/target/objectal
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

curl https://codeload.github.com/libgdx/ObjectAL-for-iPhone/legacy.tar.gz/master -o $BUILD_DIR/objectal.tar.gz

tar xvfz $BUILD_DIR/objectal.tar.gz -C $BUILD_DIR --strip-components 1

XCODEPROJ=$BUILD_DIR/ObjectAL/ObjectAL.xcodeproj

xcodebuild -project $XCODEPROJ -arch armv7  -sdk iphoneos         CONFIGURATION_BUILD_DIR=$BUILD_DIR/armv7  OTHER_CFLAGS="-target armv7-apple-ios9.0.0"
xcodebuild -project $XCODEPROJ -arch arm64  -sdk iphoneos         CONFIGURATION_BUILD_DIR=$BUILD_DIR/arm64  OTHER_CFLAGS="-target arm64-apple-ios9.0.0"
xcodebuild -project $XCODEPROJ -arch x86_64 -sdk iphonesimulator  CONFIGURATION_BUILD_DIR=$BUILD_DIR/x86_64 OTHER_CFLAGS="-target x86_64-simulator-apple-ios9.0.0"
xcodebuild -project $XCODEPROJ -arch arm64  -sdk iphonesimulator  CONFIGURATION_BUILD_DIR=$BUILD_DIR/arm64-simulator  OTHER_CFLAGS="-target arm64-simulator-apple-ios9.0.0"

mkdir $BUILD_DIR/real/

lipo $BUILD_DIR/armv7/libObjectAL.a \
     $BUILD_DIR/arm64/libObjectAL.a \
     -create \
     -output $BUILD_DIR/real/libObjectAL.a

mkdir $BUILD_DIR/sim/

lipo $BUILD_DIR/x86_64/libObjectAL.a \
     $BUILD_DIR/arm64-simulator/libObjectAL.a \
     -create \
     -output $BUILD_DIR/sim/libObjectAL.a

xcodebuild -create-xcframework \
           -library $BUILD_DIR/sim/libObjectAL.a \
           -library $BUILD_DIR/real/libObjectAL.a \
           -output $BUILD_DIR/ObjectAL.xcframework

mkdir -p $BASE/../../gdx/libs/ios32/
cp -r $BUILD_DIR/ObjectAL.xcframework $BASE/../../gdx/libs/ios32/
