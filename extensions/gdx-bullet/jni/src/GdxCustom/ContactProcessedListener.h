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

#ifndef ContactProcessedListener_H
#define ContactProcessedListener_H

class BaseContactProcessedListener;
static bool ContactProcessedListener_CB(btManifoldPoint& cp,void *body0, void *body1);
BaseContactProcessedListener *currentContactProcessedListener = 0;

/** @author Xoppa */
#ifndef SWIG
class BaseContactProcessedListener {
public:
	BaseContactProcessedListener() {
		enable();
	}

	~BaseContactProcessedListener() {
		disable();
	}

	virtual bool internalCallback(btManifoldPoint& cp,void *body0, void *body1) = 0;

	void enable() {
		currentContactProcessedListener = this;
		gContactProcessedCallback = ContactProcessedListener_CB;
	}

	void disable() {
		if (currentContactProcessedListener == this) {
			gContactProcessedCallback = 0;
			currentContactProcessedListener = 0;
		}
	}

	bool isEnabled() {
		return currentContactProcessedListener == this;
	}
};
#endif //SWIG

class ContactProcessedListenerByObject : public BaseContactProcessedListener {
public:
	virtual void onContactProcessed(btManifoldPoint& cp, const btCollisionObject* colObj0, bool match0,
			const btCollisionObject* colObj1, bool match1) = 0;

#ifndef SWIG
	bool internalCallback(btManifoldPoint& cp,void *body0, void *body1) {
		bool match0 = gdxCheckFilter((btCollisionObject*)body0, (btCollisionObject*)body1);
		bool match1 = gdxCheckFilter((btCollisionObject*)body1, (btCollisionObject*)body0);
		if (match0 || match1)
			onContactProcessed(cp, (btCollisionObject*)body0, match0, (btCollisionObject*)body1, match1);
		return false;
	}
#endif // SWIG
};

class ContactProcessedListenerByValue : public BaseContactProcessedListener {
public:
	virtual void onContactProcessed(btManifoldPoint& cp,int userValue0,bool match0,int userValue1,bool match1) = 0;

#ifndef SWIG
	bool internalCallback(btManifoldPoint& cp,void *body0, void *body1) {
		bool match0 = gdxCheckFilter((btCollisionObject*)body0, (btCollisionObject*)body1);
		bool match1 = gdxCheckFilter((btCollisionObject*)body1, (btCollisionObject*)body0);
		if (match0 || match1) {
			int val0, val1;
			val0 = ((GdxCollisionObjectBridge*)(((btCollisionObject*)body0)->getUserPointer()))->userValue;
			val1 = ((GdxCollisionObjectBridge*)(((btCollisionObject*)body1)->getUserPointer()))->userValue;
			onContactProcessed(cp, val0, match0, val1, match1);
		}
		return false;
	}
#endif // SWIG
};

bool ContactProcessedListener_CB(btManifoldPoint& cp,void *body0, void *body1) {
	return currentContactProcessedListener->internalCallback(cp, body0, body1);
}

#endif // ContactProcessedListener_H
