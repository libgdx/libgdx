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

class BaseContactAddedListener {
public:
	void enable();
	void disable();
	bool isEnabled();
};

class ContactAddedListenerByWrapper : public BaseContactAddedListener {
public:
	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) = 0;
};

class ContactAddedListenerByObject : public BaseContactAddedListener {
public:
	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObject* colObj0Wrap,int partId0,int index0,const btCollisionObject* colObj1Wrap,int partId1,int index1) = 0;
};

class ContactAddedListenerByValue : public BaseContactAddedListener {
public:
	virtual bool onContactAdded(btManifoldPoint& cp,int userValue0,int partId0,int index0,int userValue1,int partId1,int index1) = 0;
};

class BaseContactProcessedListener {
public:
	void enable();
	void disable();
	bool isEnabled();
};

class ContactProcessedListenerByObject : public BaseContactProcessedListener {
public:
	virtual bool onContactProcessed(btManifoldPoint& cp, const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
};

class ContactProcessedListenerByValue : public BaseContactProcessedListener {
public:
	virtual bool onContactProcessed(btManifoldPoint& cp,int userValue0,int userValue1) = 0;
};

class ContactDestroyedListener {
public:
	virtual bool onContactDestroyed(int manifoldPointUserValue) = 0;
	void enable();
	void disable();
	bool isEnabled();
};