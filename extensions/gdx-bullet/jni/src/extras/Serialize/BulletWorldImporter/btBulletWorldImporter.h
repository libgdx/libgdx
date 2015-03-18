/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2003-2012 Erwin Coumans  http://bulletphysics.org

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/


#ifndef BULLET_WORLD_IMPORTER_H
#define BULLET_WORLD_IMPORTER_H


#include "btWorldImporter.h"


class btBulletFile;




namespace bParse
{
	class btBulletFile;
	
};



///The btBulletWorldImporter is a starting point to import .bullet files.
///note that not all data is converted yet. You are expected to override or modify this class.
///See Bullet/Demos/SerializeDemo for a derived class that extract btSoftBody objects too.
class btBulletWorldImporter : public btWorldImporter
{


public:
	
	btBulletWorldImporter(btDynamicsWorld* world=0);

	virtual ~btBulletWorldImporter();

	bool	loadFile(const char* fileName);

	///the memoryBuffer might be modified (for example if endian swaps are necessary)
	bool	loadFileFromMemory(char *memoryBuffer, int len);

	bool	loadFileFromMemory(bParse::btBulletFile* file);

	//call make sure bulletFile2 has been parsed, either using btBulletFile::parse or btBulletWorldImporter::loadFileFromMemory
	virtual	bool	convertAllObjects(bParse::btBulletFile* file);

	


};

#endif //BULLET_WORLD_IMPORTER_H

