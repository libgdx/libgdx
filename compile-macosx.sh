# compiles all the natives for mac os x and ios
cd extensions/gdx-controllers/gdx-controllers-desktop/jni
ant -f build-macosx32.xml clean
ant -f build-macosx32.xml
cd ../../../..
cd gdx/jni
ant -f build-macosx32.xml clean
ant -f build-macosx32.xml
cd ../..
cd extensions
cd gdx-freetype/jni
ant -f build-macosx32.xml clean
ant -f build-macosx32.xml
cd ../..
cd gdx-audio/jni
ant -f build-macosx32.xml clean
ant -f build-macosx32.xml
cd ../..
cd gdx-image/jni
ant -f build-macosx32.xml clean
ant -f build-macosx32.xml
cd ../..
cd gdx-bullet/jni
ant -f build-macosx32.xml clean
ant -f build-macosx32.xml
cd ../../..

# special iOS sauce, you need to have IKVM_HOME set in your environment
# MonoTouch must be installed as well as XCode. You also need Ant 1.8.x
# in your PATH 
cd backends/gdx-backend-iosmonotouch
ant
cd ../..
