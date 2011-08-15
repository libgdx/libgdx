#!/usr/bin/sh 
# Execute this in the root directory of the trunk to update all jars and natives from 
# the CI server.
wget http://libgdx.l33tlabs.org/libgdx-nightly-`date +%Y%m%d`.zip -O libgdx-nightlies.zip
unzip libgdx-nightlies.zip -d nightlies
unzip nightlies/gdx-natives.jar -d nightlies

# copy natives to gdx/libs
cp nightlies/gdx-natives.jar gdx/libs/
cp nightlies/gdx.dll gdx/libs/windows
cp nightlies/gdx-64.dll gdx/libs/windows64
cp nightlies/libgdx.so gdx/libs/linux
cp nightlies/libgdx-64.so gdx/libs/linux64
cp nightlies/libgdx.dylib gdx/libs/mac
cp -rf nightlies/armeabi gdx/libs/armeabi
cp -rf nightlies/armeabi-v7a gdx/libs/armeabi-v7a

# copy android natives to tests
cp -rf nightlies/armeabi tests/gdx-tests-android/libs
cp -rf nightlies/armeabi-v7a tests/gdx-tests-android/libs
cp -rf nightlies/armeabi nightlies/armeabi-v7a extensions/twl/gdx-twl-tests-android/libs

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