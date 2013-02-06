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
#ifndef OIS_WiiMoteForceFeedBack_H
#define OIS_WiiMoteForceFeedBack_H

#include "OISPrereqs.h"
#include "OISForceFeedback.h"
#include "wiimote.h"

namespace OIS
{
	class WiiMoteForceFeedback : public ForceFeedback
	{
	public:
		WiiMoteForceFeedback(cWiiMote &wiiMote);
		~WiiMoteForceFeedback();

		/** @copydoc ForceFeedback::upload */
		void upload( const Effect* effect );

		/** @copydoc ForceFeedback::modify */
		void modify( const Effect* effect );

		/** @copydoc ForceFeedback::remove */
		void remove( const Effect* effect );

		/** @copydoc ForceFeedback::setMasterGain */
		void setMasterGain( float level ) {}
		
		/** @copydoc ForceFeedback::setAutoCenterMode */
		void setAutoCenterMode( bool auto_on ) {}

		/** @copydoc ForceFeedback::getFFAxesNumber */
		short getFFAxesNumber() { return 1; }

	protected:
		//! The WiiMote associated with this effect interface
		cWiiMote &mWiiMote;

		//! The handle of the one and only allowed effect
		int mHandle;
	};
}
#endif //OIS_WiiMoteForceFeedBack_H
#endif
