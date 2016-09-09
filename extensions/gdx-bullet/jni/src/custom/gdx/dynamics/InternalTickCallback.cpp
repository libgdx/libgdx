#include "InternalTickCallback.h"

InternalTickCallback::InternalTickCallback(btDynamicsWorld *dynamicsWorld, bool isPreTick) {
	attach(dynamicsWorld, isPreTick);
}

void InternalTickCallback::detach() {
	detach(mWorld, mIsPreTick);
}

void InternalTickCallback::attach(btDynamicsWorld *dynamicsWorld, bool isPreTick) {
	mIsPreTick = isPreTick;
	mWorld = dynamicsWorld;
	if (mWorld != NULL)
		mWorld->setInternalTickCallback(InternalTickCallback_CB, static_cast<void *>(this), isPreTick);
}

void InternalTickCallback::attach() {
	attach(mWorld, mIsPreTick);
}

void InternalTickCallback::detach(btDynamicsWorld *dynamicsWorld, bool isPreTick) {
	if (dynamicsWorld != NULL)
		dynamicsWorld->setInternalTickCallback(NULL, 0, isPreTick);
}