%module ContactCache

%{
#include <gdx/ContactCache.h>
%}

%feature("director") ContactCache;

%javamethodmodifiers ContactCache::ContactCache "private";

%typemap(javacode) ContactCache %{
	public ContactCache() {
		this(false);
		enable();
	}
%}

%include "gdx/ContactCache.h"

