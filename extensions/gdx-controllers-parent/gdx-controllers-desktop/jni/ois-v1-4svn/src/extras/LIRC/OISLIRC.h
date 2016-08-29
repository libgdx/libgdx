#include "OISConfig.h"
#ifdef OIS_LIRC_SUPPORT
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
#ifndef OIS_LIRC_H
#define OIS_LIRC_H
#include "OISJoyStick.h"
#include "OISLIRCRingBuffer.h"

namespace OIS
{
	class LIRCFactoryCreator;

	struct RemoteInfo
	{
		RemoteInfo() : buttons(0) {}

		RemoteInfo( const RemoteInfo &other )
		{
			buttons = other.buttons;
			buttonMap = other.buttonMap;
		}

		int buttons;
		std::map<std::string, int> buttonMap;
	};

	//Number of ring buffer events. should be nice sized (the structure is not very big)
	//Will be rounded up to power of two automatically
	#define OIS_LIRC_EVENT_BUFFER 16

	/**	Specialty joystick - Linux Infrared Remote Support */
	class _OISExport LIRCControl : public JoyStick
	{
		friend class LIRCFactoryCreator;
	public:
		LIRCControl(InputManager* creator, int id, bool buffered, LIRCFactoryCreator* local_creator, RemoteInfo &info);
		~LIRCControl();

		//Overrides of Object
		/** copydoc Object::setBuffered */
		void setBuffered(bool buffered);

		/** copydoc Object::capture */
		void capture();

		/** copydoc Object::queryInterface */
		Interface* queryInterface(Interface::IType type);

		/** copydoc Object::_intialize */
		void _initialize();

	protected:
		//! Internal method used to add a button press to the queue (called from thread)
		void queueButtonPressed(const std::string &id);

		//! The creator who created us
		LIRCFactoryCreator *mLIRCCreator;

		//! Ringbuffer is used to store events from thread and be read from capture
		LIRCRingBuffer mRingBuffer;

		//! Information about remote
		RemoteInfo mInfo;
	};
}
#endif //OIS_LIRC_H
#endif
