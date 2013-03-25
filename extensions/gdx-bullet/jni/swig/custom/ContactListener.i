%module ContactListener

%feature("director") ContactAddedListenerByWrapper;
%feature("director") ContactAddedListenerByObject;
%feature("director") ContactAddedListenerByValue;

%feature("director") ContactProcessedListenerByObject;
%feature("director") ContactProcessedListenerByValue;

%feature("director") ContactDestroyedListener;

%{
#include <GdxCustom/ContactAddedListener.h>
#include <GdxCustom/ContactProcessedListener.h>
#include <GdxCustom/ContactDestroyedListener.h>
%}
%include "GdxCustom/ContactAddedListener.h"
%include "GdxCustom/ContactProcessedListener.h"
%include "GdxCustom/ContactDestroyedListener.h"
