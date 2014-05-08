#!/bin/bash
curl https://codeload.github.com/kstenerud/ObjectAL-for-iPhone/legacy.tar.gz/master -o objectal.tar.gz
tar xvfz objectal.tar.gz
cd kstenerud-ObjectAL-for-iPhone-71e903e/ObjectAL
xcodebuild -arch armv7 -sdk iphoneos
xcodebuild -arch i386 -sdk iphonesimulator
lipo build/Release-iphoneos/libObjectAL.a build/Release-iphonesimulator/libObjectAL.a -create -output build/libObjectAL.a
cp build/libObjectAL.a ../../../../gdx/libs/ios32/
cd ../..
rm objectal.tar.gz
rm -r kstenerud-ObjectAL-for-iPhone-71e903e/
