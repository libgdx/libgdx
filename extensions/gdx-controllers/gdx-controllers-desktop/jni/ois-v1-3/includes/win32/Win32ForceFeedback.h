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
#ifndef OIS_Win32ForceFeedBack_H
#define OIS_Win32ForceFeedBack_H

#include "OISPrereqs.h"
#include "OISForceFeedback.h"
#include "win32/Win32Prereqs.h"

namespace OIS
{
	class Win32ForceFeedback : public ForceFeedback
	{
		Win32ForceFeedback() {}
	public:
		Win32ForceFeedback(IDirectInputDevice8* pDIJoy, const DIDEVCAPS* pDIJoyCaps);
		~Win32ForceFeedback();

		/** @copydoc ForceFeedback::upload */
		void upload( const Effect* effect );

		/** @copydoc ForceFeedback::modify */
		void modify( const Effect* effect );

		/** @copydoc ForceFeedback::remove */
		void remove( const Effect* effect );

		/** @copydoc ForceFeedback::setMasterGain */
		void setMasterGain( float level );
		
		/** @copydoc ForceFeedback::setAutoCenterMode */
		void setAutoCenterMode( bool auto_on );

		/** @copydoc ForceFeedback::getFFAxesNumber */
		short getFFAxesNumber();

		/** @copydoc ForceFeedback::getFFMemoryLoad */
		unsigned short getFFMemoryLoad();

		/**
			@remarks
			Internal use.. Used during enumeration to build a list of a devices
			support effects.
		*/
		void _addEffectSupport( LPCDIEFFECTINFO pdei );

		/**
			@remarks
			Internal use.. Used during axis enumeration to get number of FF axes
			support effects.
		*/
		void _addFFAxis();

	protected:

		//Specific Effect Settings
		void _updateConstantEffect( const Effect* effect );
		void _updateRampEffect( const Effect* effect );
		void _updatePeriodicEffect( const Effect* effect );
		void _updateConditionalEffect( const Effect* effect );
		void _updateCustomEffect( const Effect* effect );

		//Sets the common properties to all effects
		void _setCommonProperties( DIEFFECT* diEffect, DWORD* rgdwAxes,
									LONG* rglDirection, DIENVELOPE* diEnvelope, DWORD struct_size, 
									LPVOID struct_type, const Effect* effect, const Envelope* envelope );
		//Actually do the upload
		void _upload( GUID, DIEFFECT*, const Effect* );

		// Map of currently uploaded effects (handle => effect)
		typedef std::map<int,LPDIRECTINPUTEFFECT> EffectList;
		EffectList mEffectList;

		//Simple unique handle creation - allows for upto 2+ billion effects
		//during the lifetime of application. Hopefully, that is enough.
		int mHandles;

		// Joystick device descriptor.
		IDirectInputDevice8* mJoyStick;
		
		// Joystick capabilities.
		const DIDEVCAPS* mpDIJoyCaps;

		// Number of axis supporting FF.
		short mFFAxes;
	};
}
#endif //OIS_Win32ForceFeedBack_H
