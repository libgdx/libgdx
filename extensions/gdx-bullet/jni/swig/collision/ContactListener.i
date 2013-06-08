%module ContactListener

%feature("director") ContactAddedListenerByWrapper;
%feature("director") ContactAddedListenerByObject;
%feature("director") ContactAddedListenerByValue;

%feature("director") ContactProcessedListenerByObject;
%feature("director") ContactProcessedListenerByValue;

%feature("director") ContactDestroyedListener;

%{
#include <gdx/ContactAddedListener.h>
#include <gdx/ContactProcessedListener.h>
#include <gdx/ContactDestroyedListener.h>
%}
%include "gdx/ContactAddedListener.h"
%include "gdx/ContactProcessedListener.h"
%include "gdx/ContactDestroyedListener.h"
