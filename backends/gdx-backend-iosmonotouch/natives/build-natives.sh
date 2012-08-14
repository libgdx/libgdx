rm lib*.a
/Developer/usr/bin/xcodebuild -project natives.xcodeproj -target natives -sdk iphonesimulator -configuration Release clean build
cp build/Release-iphonesimulator/libnatives.a libnatives-i386.a
/Developer/usr/bin/xcodebuild -project natives.xcodeproj -target natives -sdk iphoneos -arch armv6 -configuration Release clean build
cp build/Release-iphoneos/libnatives.a libnatives-arm6.a
/Developer/usr/bin/xcodebuild -project natives.xcodeproj -target natives -sdk iphoneos -arch armv7 -configuration Release clean build
cp build/Release-iphoneos/libnatives.a libnatives-arm7.a
lipo -create -output libnatives.a libnatives-i386.a libnatives-arm6.a libnatives-arm7.a
cp libnatives.a ../libs/libgdx.a
cp libnatives.a ../mono/mono/libgdx.a
cp ../libs/libikvm-natives.a ../mono/mono/
rm libnatives*
