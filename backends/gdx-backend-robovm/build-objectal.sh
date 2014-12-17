#!/bin/bash
curl https://codeload.github.com/kstenerud/ObjectAL-for-iPhone/legacy.tar.gz/master -o objectal.tar.gz
tar xvfz objectal.tar.gz
cd kstenerud-ObjectAL-for-iPhone-a2252f3/ObjectAL
xcodebuild -arch armv7 -sdk iphoneos
cp build/Release-iphoneos/libObjectAl.a build/libObjectAl.a.armv7
xcodebuild -arch arm64 -sdk iphoneos
cp build/Release-iphoneos/libObjectAl.a build/libObjectAl.a.arm64
xcodebuild -arch i386 -sdk iphonesimulator
cp build/Release-iphonesimulator/libObjectAl.a build/libObjectAl.a.i386
xcodebuild -arch x86_64 -sdk iphonesimulator
cp build/Release-iphonesimulator/libObjectAl.a build/libObjectAl.a.x86_64
lipo build/libObjectAL.a.armv7 build/libObjectAL.a.arm64 build/libObjectAL.a.i386 build/libObjectAL.a.x86_64 -create -output build/libObjectAL.a
cp build/libObjectAL.a ../../../../gdx/libs/ios32/
cd ../..
rm objectal.tar.gz
rm -r kstenerud-ObjectAL-for-iPhone-a2252f3/
