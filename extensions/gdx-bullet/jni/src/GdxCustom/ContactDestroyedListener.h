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

#ifndef ContactDestroyedListener_H
#define ContactDestroyedListener_H

class ContactDestroyedListener;
static bool ContactDestroyedListener_CB(void *userPersistentData);
ContactDestroyedListener *currentContactDestroyedListener = 0;

/** @author Xoppa */
class ContactDestroyedListener {
public:
	ContactDestroyedListener() {
		enable();
	}

	~ContactDestroyedListener() {
		disable();
	}

	virtual void onContactDestroyed(int manifoldPointUserValue) = 0;

#ifndef SWIG
	bool internalCallback(void *userPersistentData) {
		int val;
		*(const void **)&val = userPersistentData;
		onContactDestroyed(val);
		return false;
	}
#endif // SWIG

	void enable() {
		currentContactDestroyedListener = this;
		gContactDestroyedCallback = ContactDestroyedListener_CB;
	}

	void disable() {
		if (currentContactDestroyedListener == this) {
			gContactDestroyedCallback = 0;
			currentContactDestroyedListener = 0;
		}
	}

	bool isEnabled() {
		return currentContactDestroyedListener == this;
	}
};

bool ContactDestroyedListener_CB(void *userPersistentData) {
	return currentContactDestroyedListener->internalCallback(userPersistentData);
}

#endif // ContactDestroyedListener_H
