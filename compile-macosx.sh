# compiles all the natives for mac os x...
cd gdx/jni
ant -f build-macosx32.xml
cd ../..
cd extensions
cd gdx-freetype/jni
ant -f build-macosx32.xml
cd ../..
cd gdx-audio/jni
ant -f build-macosx32.xml
cd ../..
cd gdx-image/jni
ant -f build-macosx32.xml
cd ../../../
