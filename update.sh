#! /usr/bin/env sh
# Execute this in the root directory of the trunk to update all jars and natives from 
# the CI server.
wget http://libgdx.badlogicgames.com/nightlies/libgdx-nightly-latest.zip -O libgdx-nightlies.zip
unzip libgdx-nightlies.zip -d nightlies
unzip nightlies/gdx-natives.jar -d nightlies/libs

# copy natives to gdx/libs and extensions libs
cp nightlies/gdx-natives.jar gdx/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a gdx/libs

# gdx-audio natives
cp -rf nightlies/extensions/gdx-audio-natives.jar extensions/gdx-audio/libs
cp -rf nightlies/extensions/armeabi/libgdx-audio.so extensions/gdx-audio/libs/armeabi
cp -rf nightlies/extensions/armeabi-v7a/libgdx-audio.so extensions/gdx-audio/libs/armeabi-v7a


# gdx-image natives
cp -rf nightlies/extensions/gdx-image-natives.jar extensions/gdx-image/libs
cp -rf nightlies/extensions/armeabi/libgdx-image.so extensions/gdx-image/libs/armeabi
cp -rf nightlies/extensions/armeabi-v7a/libgdx-image.so extensions/gdx-image/libs/armeabi-v7a

# gdx-freetype natives
cp -rf nightlies/extensions/gdx-freetype-natives.jar extensions/gdx-freetype/libs
cp -rf nightlies/extensions/armeabi/libgdx-freetype.so extensions/gdx-freetype/libs/armeabi
cp -rf nightlies/extensions/armeabi-v7a/libgdx-freetype.so extensions/gdx-freetype/libs/armeabi-v7a

# copy android natives to tests
cp -rf nightlies/armeabi nightlies/armeabi-v7a tests/gdx-tests-android/libs
cp -rf nightlies/extensions/armeabi nightlies/extensions/armeabi-v7a tests/gdx-tests-android/libs

# copy jars and natives to demos
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/invaders/gdx-invaders-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/vector-pinball/gdx-vectorpinball-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/superjumper/superjumper-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/metagun/metagun-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/cuboc/cuboc-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/very-angry-robots/very-angry-robots-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a extensions/model-loaders/model-loaders-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a extensions/gdx-remote/libs

# remove temporary directory
rm -rf nightlies
rm libgdx-nightlies.zip
