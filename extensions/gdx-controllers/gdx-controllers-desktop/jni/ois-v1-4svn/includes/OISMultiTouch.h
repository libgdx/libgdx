/*
The zlib/libpng License

Copyright (c) 2005-2007 Phillip Castaneda (pjcast -- www.wreckedgames.com)

This software is provided 'as-is', without any express or implied warranty. In no event will
the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial
applications, and to alter it and redistribute it freely, subject to the following
restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that
		you wrote the original software. If you use this software in a product,
		an acknowledgment in the product documentation would be appreciated but is
		not required.

    2. Altered source versions must be plainly marked as such, and must not be
		misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
*/
#ifndef OIS_MultiTouch_H
#define OIS_MultiTouch_H
#include "OISObject.h"
#include "OISEvents.h"

#include <set>
#include <vector>

#define OIS_MAX_NUM_TOUCHES 4   // 4 finger touches are probably the highest we'll ever get

namespace OIS
{
	/**
		Represents the state of the multi-touch device
		All members are valid for both buffered and non buffered mode
	*/
    
	//! Touch Event type
	enum MultiTypeEventTypeID
	{
		MT_None = 0, MT_Pressed, MT_Released, MT_Moved, MT_Cancelled
	};

	class _OISExport MultiTouchState
	{
	public:
		MultiTouchState() : width(50), height(50), touchType(MT_None) {};

		/** Represents the height/width of your display area.. used if touch clipping
		or touch grabbed in case of X11 - defaults to 50.. Make sure to set this
		and change when your size changes.. */
		mutable int width, height;

		//! X Axis component
		Axis X;

		//! Y Axis Component
		Axis Y;

		//! Z Axis Component
		Axis Z;

        int touchType;

        inline bool touchIsType( MultiTypeEventTypeID touch ) const
		{
			return ((touchType & ( 1L << touch )) == 0) ? false : true;
		}
        
		//! Clear all the values
		void clear()
		{
			X.clear();
			Y.clear();
			Z.clear();
            touchType = MT_None;
		}
	};

	/** Specialised for multi-touch events */
	class _OISExport MultiTouchEvent : public EventArg
	{
	public:
		MultiTouchEvent( Object *obj, const MultiTouchState &ms ) : EventArg(obj), state(ms) {}
		virtual ~MultiTouchEvent() {}

		//! The state of the touch - including axes
		const MultiTouchState &state;
	};

	/**
		To receive buffered touch input, derive a class from this, and implement the
		methods here. Then set the call back to your MultiTouch instance with MultiTouch::setEventCallback
	*/
	class _OISExport MultiTouchListener
	{
	public:
		virtual ~MultiTouchListener() {}
		virtual bool touchMoved( const MultiTouchEvent &arg ) = 0;
		virtual bool touchPressed( const MultiTouchEvent &arg ) = 0;
		virtual bool touchReleased( const MultiTouchEvent &arg ) = 0;
		virtual bool touchCancelled( const MultiTouchEvent &arg ) = 0;
	};

	/**
		MultiTouch base class. To be implemented by specific system (ie. iPhone UITouch)
		This class is useful as you remain OS independent using this common interface.
	*/
	class _OISExport MultiTouch : public Object
	{
	public:
		virtual ~MultiTouch() {}

		/**
		@remarks
			Register/unregister a MultiTouch Listener - Only one allowed for simplicity. If broadcasting
			is necessary, just broadcast from the callback you registered.
		@param touchListener
			Send a pointer to a class derived from MultiTouchListener or 0 to clear the callback
		*/
		virtual void setEventCallback( MultiTouchListener *touchListener ) {mListener = touchListener;}

		/** @remarks Returns currently set callback.. or 0 */
		MultiTouchListener* getEventCallback() {return mListener;}

		/** @remarks Clear out the set of input states.  Should be called after input has been processed by the application */
        void clearStates(void) { mStates.clear(); }

		/** @remarks Returns the state of the touch - is valid for both buffered and non buffered mode */
		std::vector<MultiTouchState> getMultiTouchStates() const { return mStates; }
        
        /** @remarks Returns the first n touch states.  Useful if you know your app only needs to 
                process n touches.  The return value is a vector to allow random access */
        const std::vector<MultiTouchState> getFirstNTouchStates(int n) {
            std::vector<MultiTouchState> states;
            for( unsigned int i = 0; i < mStates.size(); i++ ) {
                if(!(mStates[i].touchIsType(MT_None))) {
                    states.push_back(mStates[i]);
                }
            }
            return states;
        }

        /** @remarks Returns the first n touch states.  Useful if you know your app only needs to 
         process n touches.  The return value is a vector to allow random access */
        const std::vector<MultiTouchState> getMultiTouchStatesOfType(MultiTypeEventTypeID type) {
            std::vector<MultiTouchState> states;
            for( unsigned int i = 0; i < mStates.size(); i++ ) {
                if(mStates[i].touchIsType(type)) {
                    states.push_back(mStates[i]);
                }
            }
            return states;
        }
        
	protected:
		MultiTouch(const std::string &vendor, bool buffered, int devID, InputManager* creator)
			: Object(vendor, OISMultiTouch, buffered, devID, creator), mListener(0) {}

		//! The state of the touch device, implemented in a vector to store the state from each finger touch
        std::vector<MultiTouchState> mStates;

		//! Used for buffered/actionmapping callback
		MultiTouchListener *mListener;
	};
}
#endif
