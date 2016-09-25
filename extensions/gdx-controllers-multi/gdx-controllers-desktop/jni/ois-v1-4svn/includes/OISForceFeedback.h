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
#ifndef OIS_ForceFeedBack_H
#define OIS_ForceFeedBack_H

#include "OISPrereqs.h"
#include "OISInterface.h"
#include "OISEffect.h"

namespace OIS
{
	/**
		Interface class for dealing with Force Feedback devices
	*/
	class _OISExport ForceFeedback : public Interface
	{
	public:
		ForceFeedback();
		virtual ~ForceFeedback() {}

		/**
		@remarks
			This is like setting the master volume of an audio device.
			Individual effects have gain levels; however, this affects all
			effects at once.
			Note: If the device does not support master gain setting, nothing is done
		@param level
			A value between 0.0 and 1.0 represent the percentage of gain. 1.0
			being the highest possible force level (means no scaling).
		*/
		virtual void setMasterGain( float level ) = 0;
		
		/**
		@remarks
			If using Force Feedback effects, this should be turned off
			before uploading any effects. Auto centering is the motor moving
			the joystick back to center. DirectInput only has an on/off setting,
			whereas linux has levels.. Though, we go with DI's on/off mode only
			Note: If the device does not support auto-centering, nothing is done
		@param auto_on
			true to turn auto centering on, false to turn off.
		*/
		virtual void setAutoCenterMode( bool auto_on ) = 0;

		/**
		@remarks
			Creates and Plays the effect immediately. If the device is full
			of effects, it will fail to be uploaded. You will know this by
			an invalid Effect Handle
		*/
		virtual void upload( const Effect* effect ) = 0;

		/**
		@remarks
			Modifies an effect that is currently playing
		*/
		virtual void modify( const Effect* effect ) = 0;

		/**
		@remarks
			Remove the effect from the device
		*/
		virtual void remove( const Effect* effect ) = 0;

		/**
		@remarks
			Get the number of supported Axes for FF usage
		*/
        virtual short getFFAxesNumber() = 0;

		/**
		@remarks
			Get the current load (%, in [0, 100] of the FF device memory 
		*/
		virtual unsigned short getFFMemoryLoad() = 0;

		typedef std::multimap<Effect::EForce, Effect::EType> SupportedEffectList;
		/**
		@remarks
			Get a list of all supported effects
		*/
		const SupportedEffectList& getSupportedEffects() const;

		/**
		@remarks
			Tell if a given force / effect type pair is supported
		*/
		bool supportsEffect(Effect::EForce force, Effect::EType type) const;

		void _addEffectTypes( Effect::EForce force, Effect::EType type );
		void _setGainSupport( bool on );
		void _setAutoCenterSupport( bool on );

	protected:
		SupportedEffectList mSupportedEffects;
		bool mSetGainSupport;
		bool mSetAutoCenterSupport;
	};
}
#endif //OIS_ForceFeedBack_H
