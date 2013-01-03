#include "OISConfig.h"
#ifdef OIS_WIN32_WIIMOTE_SUPPORT
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
#ifndef OIS_WiiMote_H
#define OIS_WiiMote_H
#include "OISJoyStick.h"
#include "OISWiiMoteRingBuffer.h"
#include "wiimote.h"

namespace OIS
{
	class WiiMoteFactoryCreator;
	class WiiMoteForceFeedback;

	//Number of ring buffer events. should be nice sized (the structure is not very big)
	//Will be rounded up to power of two automatically
	#define OIS_WII_EVENT_BUFFER 32

	/**	Specialty joystick - WiiMote controller */
	class _OISExport WiiMote : public JoyStick
	{
	public:
		WiiMote(InputManager* creator, int id, bool buffered, WiiMoteFactoryCreator* local_creator);
		~WiiMote();

		//Overrides of Object
		void setBuffered(bool buffered);

		void capture();

		Interface* queryInterface(Interface::IType type);

		void _initialize();

		void _threadUpdate();

	protected:
		void _doButtonCheck(bool new_state, int ois_button, unsigned int &pushed, unsigned int &released);
		bool _doPOVCheck(const cWiiMote::tButtonStatus &bState, unsigned int &newPosition);

		//! The creator who created us
		WiiMoteFactoryCreator *mWiiCreator;

		//! Actual WiiMote HID device
		cWiiMote mWiiMote;

		//! Used to signal thread that remote is ready
		volatile bool mtInitialized;

		//! Ringbuffer is used to store events from thread and be read from capture
		WiiMoteRingBuffer mRingBuffer;

		//Following variables are used entirely within threaded context
		int mtLastButtonStates;
		unsigned int mtLastPOVState;
		float mtLastX, mtLastY, mtLastZ;
		float mtLastNunChuckX, mtLastNunChuckY, mtLastNunChuckZ;
		int mLastNunChuckXAxis, mLastNunChuckYAxis;

		//Small workaround for slow calibration of wiimote data
		int _mWiiMoteMotionDelay;

		//Simple rumble force
		WiiMoteForceFeedback *mRumble;
	};
}
#endif //OIS_WiiMote_H
#endif
