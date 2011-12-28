#!/usr/bin/sh 
# Execute this in the root directory of the trunk to update all jars and natives from 
# the CI server.
wget http://libgdx.l33tlabs.org/libgdx-nightly-latest.zip -O libgdx-nightlies.zip
unzip libgdx-nightlies.zip -d nightlies
unzip nightlies/gdx-natives.jar -d nightlies/libs

# copy natives to gdx/libs
cp nightlies/libs/ gdx/libs
cp -rf nightlies/libs/armeabi nightlies/libs/armeabi-v7a gdx/libs

# copy android natives to tests
cp -rf nightlies/armeabi nightlies/armeabi-v7a tests/gdx-tests-android/libs

# copy jars and natives to demos
cp -rf nightlies/gdx.jar nightlies/gdx-natives.jar nightlies/gdx-backend-jogl.jar nightlies/gdx-backend-jogl-natives.jar nightlies/gdx-sources.jar demos/helloworld/gdx-helloworld/libs
cp -rf nightlies/gdx.jar nightlies/gdx-backend-android.jar nightlies/armeabi nightlies/armeabi-v7a demos/helloworld/gdx-helloworld-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/invaders/gdx-invaders-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/vector-pinball/gdx-vectorpinball-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/superjumper/superjumper-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/returntomarchfeld/rtm-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/metagun/metagun-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/cuboc/cuboc-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a demos/very-angry-robots/very-angry-robots-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a extensions/model-loaders/model-loaders-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a extensions/gdx-remote/libs

# remove temporary directory
rm -rf nightlies
rm libgdx-nightlies.zip