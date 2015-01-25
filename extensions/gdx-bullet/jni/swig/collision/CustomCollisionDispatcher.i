%module CustomCollisionDispatcher

%{
#include <gdx/collision/CustomCollisionDispatcher.h>
%}

%feature("director") CustomCollisionDispatcher;
%feature("nodirector") CustomCollisionDispatcher::findAlgorithm;
%feature("nodirector") CustomCollisionDispatcher::getManifoldByIndexInternal;
%feature("nodirector") CustomCollisionDispatcher::getInternalManifoldPool;

%include "gdx/collision/CustomCollisionDispatcher.h"
