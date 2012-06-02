#!/bin/sh
export IKVM_HOME=libs/
export MONO_PATH=/Developer/MonoTouch/usr/lib/mono/2.1

echo "Converting Java bytecode to CLR dll..."
mono $IKVM_HOME/ikvmc.exe -nostdlib -debug -target:library -out:libs/gdx-backend-ios.dll \
    -r:$MONO_PATH/mscorlib.dll \
    -r:$MONO_PATH/System.dll \
    -r:$MONO_PATH/System.Core.dll \
    -r:$MONO_PATH/System.Data.dll \
    -r:$MONO_PATH/OpenTK.dll \
    -r:$MONO_PATH/monotouch.dll \
    -r:$MONO_PATH/Mono.Data.Sqlite.dll \
    -r:libs/gdx.dll \
    -recurse:target/backend/*.class
    
