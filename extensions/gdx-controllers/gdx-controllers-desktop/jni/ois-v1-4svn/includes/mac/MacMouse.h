#ifndef OIS_MacMouse_H
#define OIS_MacMouse_H

#include "OISMouse.h"
#include "mac/MacHelpers.h"
#include "mac/MacPrereqs.h"

#include <Carbon/Carbon.h>

namespace OIS
{
	class MacMouse : public Mouse
    {
	public:
		MacMouse( InputManager* creator, bool buffered );
		virtual ~MacMouse();
		
		/** @copydoc Object::setBuffered */
		virtual void setBuffered(bool buffered);

		/** @copydoc Object::capture */
		virtual void capture();

		/** @copydoc Object::queryInterface */
		virtual Interface* queryInterface(Interface::IType type) {return 0;}

		/** @copydoc Object::_initialize */
		virtual void _initialize();
        
	public:
        void _mouseCallback( EventRef theEvent );

	protected:
		static OSStatus WindowFocusChanged(EventHandlerCallRef nextHandler, EventRef event, void* macMouse);
        
        // "universal procedure pointers" - required reference for callbacks
		EventHandlerUPP mouseUPP;
		EventHandlerRef mouseEventRef;
		
		EventHandlerUPP mWindowFocusListener;
		EventHandlerRef mWindowFocusHandler;
		
		bool mNeedsToRegainFocus;
		bool mMouseWarped;
		
		MouseState mTempState;
	};
}


#endif // OIS_MacMouse_H
