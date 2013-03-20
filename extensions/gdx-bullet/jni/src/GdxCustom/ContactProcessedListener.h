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
class BaseContactProcessedListener {
public:
	BaseContactProcessedListener() {
		enable();
	}

	~BaseContactProcessedListener() {
		disable();
	}

	virtual bool internalCallback(btManifoldPoint& cp,void *body0, void *body1) {
		return false;
	}

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

class ContactProcessedListenerByObject : public BaseContactProcessedListener {
public:
	bool internalCallback(btManifoldPoint& cp,void *body0, void *body1) {
		return onContactProcessed(cp, (btCollisionObject*)body0, (btCollisionObject*)body1);
	}

	virtual bool onContactProcessed(btManifoldPoint& cp, const btCollisionObject* colObj0, const btCollisionObject* colObj1) {
		return false;
	}
};

class ContactProcessedListenerByValue : public BaseContactProcessedListener {
public:
	bool internalCallback(btManifoldPoint& cp,void *body0, void *body1) {
		int val0, val1;
		val0 = ((GdxCollisionObjectBridge*)(((btCollisionObject*)body0)->getUserPointer()))->userValue;
		val1 = ((GdxCollisionObjectBridge*)(((btCollisionObject*)body1)->getUserPointer()))->userValue;
		return onContactProcessed(cp, val0, val1);
	}

	virtual bool onContactProcessed(btManifoldPoint& cp,int userValue0,int userValue1) {
		return false;
	}
};

bool ContactProcessedListener_CB(btManifoldPoint& cp,void *body0, void *body1) {
	return currentContactProcessedListener->internalCallback(cp, body0, body1);
}

#endif // ContactProcessedListener_H
