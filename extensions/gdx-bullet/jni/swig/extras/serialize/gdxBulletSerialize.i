
%feature("director") btBulletWorldImporter;
%feature("nodirector") btBulletWorldImporter::createMultiSphereShape;

%{
#include <BulletWorldImporter/btWorldImporter.h>
#include <BulletWorldImporter/btBulletWorldImporter.h>
%}

%template(btStringArray) btAlignedObjectArray<char*>;

%ignore btWorldImporter::getNameForPointer(const void* ptr);
%extend btWorldImporter {
	const char*	getNameForPointer(unsigned long cPtr) {
		return $self->getNameForPointer((void*)cPtr);
	}
};

%ignore btBulletWorldImporter::loadFileFromMemory(char *memoryBuffer, int len);
%extend btBulletWorldImporter {
	bool loadFileFromMemory(unsigned char *memoryBuffer, int len) {
		return $self->loadFileFromMemory((char *)memoryBuffer, len);
	}
};

%typemap(javacode) btBulletWorldImporter %{
	public boolean loadFile(final com.badlogic.gdx.files.FileHandle fileHandle) {
		final int len = (int)fileHandle.length();
		if (len <= 0)
			throw new com.badlogic.gdx.utils.GdxRuntimeException("Incorrect file specified");
		java.nio.ByteBuffer buff = com.badlogic.gdx.utils.BufferUtils.newUnsafeByteBuffer(len);
		buff.put(fileHandle.readBytes());
		buff.position(0);
		boolean result = loadFileFromMemory(buff, len);
		com.badlogic.gdx.utils.BufferUtils.disposeUnsafeByteBuffer(buff);
		return result;
	}
%}

%include "BulletWorldImporter/btWorldImporter.h"
%include "BulletWorldImporter/btBulletWorldImporter.h"
