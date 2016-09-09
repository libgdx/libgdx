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
#include "SDL/SDLInputManager.h"
#include "SDL/SDLKeyboard.h"
#include "SDL/SDLMouse.h"
#include "SDL/SDLJoyStick.h"
#include "OISException.h"
#include "OISObject.h"

using namespace OIS;

const std::string SDLInputManager::iName = "SDL Input Wrapper";

//--------------------------------------------------------------------------------//
SDLInputManager::SDLInputManager() : mGrabbed(false)
{
}

//--------------------------------------------------------------------------------//
SDLInputManager::~SDLInputManager()
{
}

//--------------------------------------------------------------------------------//
void SDLInputManager::_initialize( ParamList &paramList )
{
	Uint32 flags = SDL_WasInit(0);
	if( flags == 0 )
		OIS_EXCEPT( E_General, "SDLInputManager::SDLInputManager >> SDL Not Initialized already!");

	//Ok, now we have DirectInput, parse whatever extra settings were sent to us
	_parseConfigSettings( paramList );
	_enumerateDevices();
}

//--------------------------------------------------------------------------------//
void SDLInputManager::_parseConfigSettings( ParamList &paramList )
{
}

//--------------------------------------------------------------------------------//
void SDLInputManager::_enumerateDevices()
{
}

//--------------------------------------------------------------------------------//
int SDLInputManager::numJoySticks()
{
	return 0;
}

//--------------------------------------------------------------------------------//
int SDLInputManager::numMice()
{
	return 1;
}

//--------------------------------------------------------------------------------//
int SDLInputManager::numKeyboards()
{
	return 1;
}

//----------------------------------------------------------------------------//
Object* SDLInputManager::createInputObject( Type iType, bool bufferMode )
{
	Object* obj = 0;
	
	switch( iType )
	{
		case OISKeyboard: obj = new SDLKeyboard( bufferMode ); break;
		case OISMouse: obj = new SDLMouse( bufferMode ); break;
		case OISJoyStick: 
		default: OIS_EXCEPT( E_InputDeviceNotSupported, "Type not implemented");
	}

	try	{
		obj->_initialize();
	}
	catch(...) {
		delete obj;
		throw; //rethrow
	}

	return obj;
}

//----------------------------------------------------------------------------//
void SDLInputManager::destroyInputObject( Object* obj )
{
	if( obj == 0 ) return;

	delete obj;
}
