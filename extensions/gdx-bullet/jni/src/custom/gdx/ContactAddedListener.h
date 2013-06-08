/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

#ifndef ContactAddedListener_H
#define ContactAddedListener_H

class BaseContactAddedListener;
static bool ContactAddedListener_CB(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
BaseContactAddedListener *currentContactAddedListener = 0;

/** @author Xoppa */
#ifndef SWIG
class BaseContactAddedListener {
public:
	BaseContactAddedListener() {
		enable();
	}

	~BaseContactAddedListener() {
		disable();
	}

	virtual bool internalCallback(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,
			const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) = 0;

	void enable() {
		currentContactAddedListener = this;
		gContactAddedCallback = ContactAddedListener_CB;
	}

	void disable() {
		if (currentContactAddedListener == this) {
			gContactAddedCallback = 0;
			currentContactAddedListener = 0;
		}
	}

	bool isEnabled() {
		return currentContactAddedListener == this;
	}
};
#endif // SWIG

class ContactAddedListenerByWrapper : public BaseContactAddedListener {
public:
	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,bool match0,
			const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1,bool match1) = 0;

#ifndef SWIG
	bool internalCallback(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
		bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
		bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
		return (match0 || match1) ? onContactAdded(cp, colObj0Wrap, partId0, index0, match0, colObj1Wrap, partId1, index1, match1) : false;
	}
#endif // SWIG
};

class ContactAddedListenerByObject : public BaseContactAddedListener {
public:
	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObject* colObj0,int partId0,int index0,bool match0,
			const btCollisionObject* colObj1,int partId1,int index1,bool match1) = 0;

#ifndef SWIG
	bool internalCallback(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
		bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
		bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
		return (match0 || match1) ? onContactAdded(cp, colObj0Wrap->m_collisionObject, partId0, index0, match0, colObj1Wrap->m_collisionObject, partId1, index1, match1) : false;
	}
#endif // SWIG
};

class ContactAddedListenerByValue : public BaseContactAddedListener {
public:
	virtual bool onContactAdded(btManifoldPoint& cp,int userValue0,int partId0,int index0,bool match0,
			int userValue1,int partId1,int index1,bool match1) = 0;

#ifndef SWIG
	bool internalCallback(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
		bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
		bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
		if (!match0 && !match1)
			return false;
		int val0, val1;
		val0 = ((GdxCollisionObjectBridge*)(colObj0Wrap->m_collisionObject->getUserPointer()))->userValue;
		val1 = ((GdxCollisionObjectBridge*)(colObj1Wrap->m_collisionObject->getUserPointer()))->userValue;
		return onContactAdded(cp, val0, partId0, index0, match0, val1, partId1, index1, match1);
	}
#endif // SWIG
};

bool ContactAddedListener_CB(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	return currentContactAddedListener->internalCallback(cp, colObj0Wrap, partId0, index0, colObj1Wrap, partId1, index1);
}

#endif // ContactAddedListener_H
