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
#include "OISKeyboard.h"
#include "OISException.h"

using namespace OIS;

//----------------------------------------------------------------------//
void Keyboard::setTextTranslation( TextTranslationMode mode )
{
	mTextMode = mode;
}

//----------------------------------------------------------------------//
bool Keyboard::isModifierDown( Modifier mod ) const
{
#if defined(OIS_MSVC_COMPILER)
  #pragma warning (push)
  #pragma warning (disable : 4800)
#endif
	return (mModifiers & mod);
#if defined(OIS_MSVC_COMPILER)
  #pragma warning (pop)
#endif
}
