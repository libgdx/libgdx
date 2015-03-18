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
#ifndef OIS_LinuxForceFeedBack_H
#define OIS_LinuxForceFeedBack_H

#include "linux/LinuxPrereqs.h"
#include "OISForceFeedback.h"
#include <linux/input.h>

namespace OIS
{
	class LinuxForceFeedback : public ForceFeedback
	{
	public:
		LinuxForceFeedback(int deviceID);
		~LinuxForceFeedback();

		/** @copydoc ForceFeedback::setMasterGain */
		void setMasterGain(float);
		
		/** @copydoc ForceFeedback::setAutoCenterMode */
		void setAutoCenterMode(bool);

		/** @copydoc ForceFeedback::upload */
		void upload( const Effect* effect );

		/** @copydoc ForceFeedback::modify */
		void modify( const Effect* effect );

		/** @copydoc ForceFeedback::remove */
		void remove( const Effect* effect );

		/** FF is not yet implemented fully on Linux.. just return -1 for now. todo, xxx */
		short int getFFAxesNumber() { return -1; }

		/** @copydoc ForceFeedback::getFFMemoryLoad */
		unsigned short getFFMemoryLoad();

	protected:

		//Sets the common properties to all effects
		void _setCommonProperties(struct ff_effect *event, struct ff_envelope *ffenvelope, 
								  const Effect* effect, const Envelope *envelope );

		//Specific Effect Settings
		void _updateConstantEffect( const Effect* effect );
		void _updateRampEffect( const Effect* effect );
		void _updatePeriodicEffect( const Effect* effect );
		void _updateConditionalEffect( const Effect* effect );
		//void _updateCustomEffect( const Effect* effect );

		void _upload( struct ff_effect* ffeffect, const Effect* effect);
		void _stop( int handle);
		void _start( int handle);
		void _unload( int handle);

		// Map of currently uploaded effects (handle => effect)
		typedef std::map<int, struct ff_effect *> EffectList;
		EffectList mEffectList;

		// Joystick device (file) descriptor.
		int mJoyStick;
	};
}
#endif //OIS_LinuxForceFeedBack_H
