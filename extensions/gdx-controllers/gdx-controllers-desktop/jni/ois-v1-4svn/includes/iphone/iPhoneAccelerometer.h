#ifndef OIS_iPhoneAccelerometer_H
#define OIS_iPhoneAccelerometer_H

#include "OISJoystick.h"
#include "iphone/iPhonePrereqs.h"

#import <UIKit/UIKit.h>
@class iPhoneAccelerometerDelegate;

class JoyStickState;

namespace OIS
{
	class iPhoneAccelerometer : public JoyStick
    {
	public:
		iPhoneAccelerometer(InputManager* creator, bool buffered);
		virtual ~iPhoneAccelerometer();
		
		/** @copydoc Object::setBuffered */
		virtual void setBuffered(bool buffered);

        void setUpdateInterval(float interval) { 
            mUpdateInterval = interval;
            [[UIAccelerometer sharedAccelerometer] setUpdateInterval:(1.0f / mUpdateInterval)];
        }
        
        Vector3 getAccelerometerVector3(void) { return mState.mVectors[0]; }
		/** @copydoc Object::capture */
		virtual void capture();

		/** @copydoc Object::queryInterface */
		virtual Interface* queryInterface(Interface::IType type) {return 0;}

		/** @copydoc Object::_initialize */
		virtual void _initialize();

        void didAccelerate(UIAcceleration *acceleration);

    protected:
        iPhoneAccelerometerDelegate *accelerometerDelegate;

        /** The update frequency of the accelerometer.  Represented in times per second. */
        float mUpdateInterval;
        Vector3 mTempState;
	};
}


#endif // OIS_iPhoneAccelerometer_H
