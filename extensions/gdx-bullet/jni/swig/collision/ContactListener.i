%module ContactListener

%{
#include <gdx/collision/ContactListener.h>
bool custom_ContactListener_setEvents(ContactListener *listener);
%}

%feature("director") ContactListener;

%javamethodmodifiers ContactListener::ContactListener "private";
%javamethodmodifiers ContactListener::setEvents "private";

%extend ContactListener {
	bool setEvents() {
		return custom_ContactListener_setEvents($self);
	}
};

%typemap(javacode) ContactListener %{
	public ContactListener() {
		this(false);
		if (!setEvents())
			throw new com.badlogic.gdx.utils.GdxRuntimeException("Only one method per callback event can be overridden.");
		enable();
	}
%}

%include "gdx/collision/ContactListener.h"

%init %{
bool custom_ContactListener_setEvents(ContactListener *listener) {
	SwigDirector_ContactListener *dir = (SwigDirector_ContactListener *)listener;
	
	int cnt = 0;
	for (int i = 0; i < 12; i++)
		if (dir->swig_overrides(i))
			cnt++;
	if (cnt > 1)
		return false;
	
	cnt = 0;
	for (int i = 12; i < 20; i++)
		if (dir->swig_overrides(i))
			cnt++;
	if (cnt > 1)
		return false;
	
	cnt = 0;
	for (int i = 21; i < 27; i++)
		if (dir->swig_overrides(i))
			cnt++;
	if (cnt > 1)
		return false;
	
	cnt = 0;
	for (int i = 27; i < 33; i++)
		if (dir->swig_overrides(i))
			cnt++;
	if (cnt > 1)
		return false;
	
	int events = 0;

	if (dir->swig_overrides(0))
		events |= ON_ADDED_UNFILTERED_WRAPPER_INCLUDEPOINT;
	else if (dir->swig_overrides(1))
		events |= ON_ADDED_UNFILTERED_OBJECT_INCLUDEPOINT;
	else if (dir->swig_overrides(2))
		events |= ON_ADDED_UNFILTERED_VALUE_INCLUDEPOINT;
	else if (dir->swig_overrides(3))
		events |= ON_ADDED_FILTERED_WRAPPER_INCLUDEPOINT;
	else if (dir->swig_overrides(4))
		events |= ON_ADDED_FILTERED_OBJECT_INCLUDEPOINT;
	else if (dir->swig_overrides(5))
		events |= ON_ADDED_FILTERED_VALUE_INCLUDEPOINT;
	else if (dir->swig_overrides(6))
		events |= ON_ADDED_UNFILTERED_WRAPPER;
	else if (dir->swig_overrides(7))
		events |= ON_ADDED_UNFILTERED_OBJECT;
	else if (dir->swig_overrides(8))
		events |= ON_ADDED_UNFILTERED_VALUE;
	else if (dir->swig_overrides(9))
		events |= ON_ADDED_FILTERED_WRAPPER;
	else if (dir->swig_overrides(10))
		events |= ON_ADDED_FILTERED_OBJECT;
	else if (dir->swig_overrides(11))
		events |= ON_ADDED_FILTERED_VALUE;
	
	if (dir->swig_overrides(12))
		events |= ON_PROCESSED_UNFILTERED_OBJECT_INCLUDEPOINT;
	else if (dir->swig_overrides(13))
		events |= ON_PROCESSED_UNFILTERED_VALUE_INCLUDEPOINT;
	else if (dir->swig_overrides(14))
		events |= ON_PROCESSED_FILTERED_OBJECT_INCLUDEPOINT;
	else if (dir->swig_overrides(15))
		events |= ON_PROCESSED_FILTERED_VALUE_INCLUDEPOINT;
	else if (dir->swig_overrides(16))
		events |= ON_PROCESSED_UNFILTERED_OBJECT;
	else if (dir->swig_overrides(17))
		events |= ON_PROCESSED_UNFILTERED_VALUE;
	else if (dir->swig_overrides(18))
		events |= ON_PROCESSED_FILTERED_OBJECT;
	else if (dir->swig_overrides(19))
		events |= ON_PROCESSED_FILTERED_VALUE;
	
	if (dir->swig_overrides(20))
		events |= ON_DESTROYED;
	
	if (dir->swig_overrides(21))
		events |= ON_STARTED_UNFILTERED_MANIFOLD;
	else if (dir->swig_overrides(22))
		events |= ON_STARTED_UNFILTERED_OBJECT;
	else if (dir->swig_overrides(23))
		events |= ON_STARTED_UNFILTERED_VALUE;
	else if (dir->swig_overrides(24))
		events |= ON_STARTED_FILTERED_MANIFOLD;
	else if (dir->swig_overrides(25))
		events |= ON_STARTED_FILTERED_OBJECT;
	else if (dir->swig_overrides(26))
		events |= ON_STARTED_FILTERED_VALUE;
	
	if (dir->swig_overrides(27))
		events |= ON_ENDED_UNFILTERED_MANIFOLD;
	else if (dir->swig_overrides(28))
		events |= ON_ENDED_UNFILTERED_OBJECT;
	else if (dir->swig_overrides(29))
		events |= ON_ENDED_UNFILTERED_VALUE;
	else if (dir->swig_overrides(30))
		events |= ON_ENDED_FILTERED_MANIFOLD;
	else if (dir->swig_overrides(31))
		events |= ON_ENDED_FILTERED_OBJECT;
	else if (dir->swig_overrides(32))
		events |= ON_ENDED_FILTERED_VALUE;
	
	listener->setEvents(events);
	
	return true;
}
%}
