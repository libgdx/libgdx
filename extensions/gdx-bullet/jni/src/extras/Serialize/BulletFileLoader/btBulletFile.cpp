/*
bParse
Copyright (c) 2006-2010 Erwin Coumans  http://gamekit.googlecode.com

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it freely,
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

#include "btBulletFile.h"
#include "bDefines.h"
#include "bDNA.h"

#if !defined( __CELLOS_LV2__) && !defined(__MWERKS__)
#include <memory.h>
#endif
#include <string.h>


// 32 && 64 bit versions
#ifdef BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
#ifdef _WIN64
extern char sBulletDNAstr64[];
extern int sBulletDNAlen64;
#else
extern char sBulletDNAstr[];
extern int sBulletDNAlen;
#endif //_WIN64
#else//BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES

extern char sBulletDNAstr64[];
extern int sBulletDNAlen64;
extern char sBulletDNAstr[];
extern int sBulletDNAlen;

#endif //BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES

using namespace bParse;

btBulletFile::btBulletFile()
:bFile("", "BULLET ")
{
	mMemoryDNA = new bDNA(); //this memory gets released in the bFile::~bFile destructor,@todo not consistent with the rule 'who allocates it, has to deallocate it"

	m_DnaCopy = 0;


#ifdef BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
#ifdef _WIN64
		m_DnaCopy = (char*)btAlignedAlloc(sBulletDNAlen64,16);
		memcpy(m_DnaCopy,sBulletDNAstr64,sBulletDNAlen64);
		mMemoryDNA->init(m_DnaCopy,sBulletDNAlen64);
#else//_WIN64
		m_DnaCopy = (char*)btAlignedAlloc(sBulletDNAlen,16);
		memcpy(m_DnaCopy,sBulletDNAstr,sBulletDNAlen);
		mMemoryDNA->init(m_DnaCopy,sBulletDNAlen);
#endif//_WIN64
#else//BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
	if (VOID_IS_8)
	{
		m_DnaCopy = (char*) btAlignedAlloc(sBulletDNAlen64,16);
		memcpy(m_DnaCopy,sBulletDNAstr64,sBulletDNAlen64);
		mMemoryDNA->init(m_DnaCopy,sBulletDNAlen64);
	}
	else
	{
		m_DnaCopy =(char*) btAlignedAlloc(sBulletDNAlen,16);
		memcpy(m_DnaCopy,sBulletDNAstr,sBulletDNAlen);
		mMemoryDNA->init(m_DnaCopy,sBulletDNAlen);
	}
#endif//BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
}



btBulletFile::btBulletFile(const char* fileName)
:bFile(fileName, "BULLET ")
{
	m_DnaCopy = 0;
}



btBulletFile::btBulletFile(char *memoryBuffer, int len)
:bFile(memoryBuffer,len, "BULLET ")
{
	m_DnaCopy = 0;
}


btBulletFile::~btBulletFile()
{
	if (m_DnaCopy)
		btAlignedFree(m_DnaCopy);

	
	while (m_dataBlocks.size())
	{
		char* dataBlock = m_dataBlocks[m_dataBlocks.size()-1];
		delete[] dataBlock;
		m_dataBlocks.pop_back();
	}

}



// ----------------------------------------------------- //
void btBulletFile::parseData()
{
//	printf ("Building datablocks");
//	printf ("Chunk size = %d",CHUNK_HEADER_LEN);
//	printf ("File chunk size = %d",ChunkUtils::getOffset(mFlags));

	const bool brokenDNA = (mFlags&FD_BROKEN_DNA)!=0;

	//const bool swap = (mFlags&FD_ENDIAN_SWAP)!=0;
	

	mDataStart = 12;

	char *dataPtr = mFileBuffer+mDataStart;

	bChunkInd dataChunk;
	dataChunk.code = 0;


	//dataPtr += ChunkUtils::getNextBlock(&dataChunk, dataPtr, mFlags);
	int seek = getNextBlock(&dataChunk, dataPtr, mFlags);
	
	
	if (mFlags &FD_ENDIAN_SWAP) 
		swapLen(dataPtr);

	//dataPtr += ChunkUtils::getOffset(mFlags);
	char *dataPtrHead = 0;

	while (dataChunk.code != DNA1)
	{
		if (!brokenDNA || (dataChunk.code != BT_QUANTIZED_BVH_CODE) )
		{

			// one behind
			if (dataChunk.code == SDNA) break;
			//if (dataChunk.code == DNA1) break;

			// same as (BHEAD+DATA dependency)
			dataPtrHead = dataPtr+ChunkUtils::getOffset(mFlags);
			if (dataChunk.dna_nr>=0)
			{
				char *id = readStruct(dataPtrHead, dataChunk);

				// lookup maps
				if (id)
				{
					m_chunkPtrPtrMap.insert(dataChunk.oldPtr, dataChunk);
					mLibPointers.insert(dataChunk.oldPtr, (bStructHandle*)id);

					m_chunks.push_back(dataChunk);
					// block it
					//bListBasePtr *listID = mMain->getListBasePtr(dataChunk.code);
					//if (listID)
					//	listID->push_back((bStructHandle*)id);
				}

				if (dataChunk.code == BT_SOFTBODY_CODE)
				{
					m_softBodies.push_back((bStructHandle*) id);
				}
				
				if (dataChunk.code == BT_RIGIDBODY_CODE)
				{
					m_rigidBodies.push_back((bStructHandle*) id);
				}

				if (dataChunk.code == BT_DYNAMICSWORLD_CODE)
				{
					m_dynamicsWorldInfo.push_back((bStructHandle*) id);
				}

				if (dataChunk.code == BT_CONSTRAINT_CODE)
				{
					m_constraints.push_back((bStructHandle*) id);
				}

				if (dataChunk.code == BT_QUANTIZED_BVH_CODE)
				{
					m_bvhs.push_back((bStructHandle*) id);
				}

				if (dataChunk.code == BT_TRIANLGE_INFO_MAP)
				{
					m_triangleInfoMaps.push_back((bStructHandle*) id);
				}

				if (dataChunk.code == BT_COLLISIONOBJECT_CODE)
				{
					m_collisionObjects.push_back((bStructHandle*) id);
				}

				if (dataChunk.code == BT_SHAPE_CODE)
				{
					m_collisionShapes.push_back((bStructHandle*) id);
				}

		//		if (dataChunk.code == GLOB)
		//		{
		//			m_glob = (bStructHandle*) id;
		//		}
			} else
			{
				printf("unknown chunk\n");

				mLibPointers.insert(dataChunk.oldPtr, (bStructHandle*)dataPtrHead);
			}
		} else
		{
			printf("skipping BT_QUANTIZED_BVH_CODE due to broken DNA\n");
		}

		
		dataPtr += seek;

		seek =  getNextBlock(&dataChunk, dataPtr, mFlags);
		if (mFlags &FD_ENDIAN_SWAP) 
			swapLen(dataPtr);

		if (seek < 0)
			break;
	}

}

void	btBulletFile::addDataBlock(char* dataBlock)
{
	m_dataBlocks.push_back(dataBlock);

}




void	btBulletFile::writeDNA(FILE* fp)
{

	bChunkInd dataChunk;
	dataChunk.code = DNA1;
	dataChunk.dna_nr = 0;
	dataChunk.nr = 1;
#ifdef BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES	
	if (VOID_IS_8)
	{
#ifdef _WIN64
		dataChunk.len = sBulletDNAlen64;
		dataChunk.oldPtr = sBulletDNAstr64;
		fwrite(&dataChunk,sizeof(bChunkInd),1,fp);
		fwrite(sBulletDNAstr64, sBulletDNAlen64,1,fp);
#else
		btAssert(0);
#endif
	}
	else
	{
#ifndef _WIN64
		dataChunk.len = sBulletDNAlen;
		dataChunk.oldPtr = sBulletDNAstr;
		fwrite(&dataChunk,sizeof(bChunkInd),1,fp);
		fwrite(sBulletDNAstr, sBulletDNAlen,1,fp);
#else//_WIN64
		btAssert(0);
#endif//_WIN64
	}
#else//BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
	if (VOID_IS_8)
	{
		dataChunk.len = sBulletDNAlen64;
		dataChunk.oldPtr = sBulletDNAstr64;
		fwrite(&dataChunk,sizeof(bChunkInd),1,fp);
		fwrite(sBulletDNAstr64, sBulletDNAlen64,1,fp);
	}
	else
	{
		dataChunk.len = sBulletDNAlen;
		dataChunk.oldPtr = sBulletDNAstr;
		fwrite(&dataChunk,sizeof(bChunkInd),1,fp);
		fwrite(sBulletDNAstr, sBulletDNAlen,1,fp);
	}
#endif//BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
}


void	btBulletFile::parse(int verboseMode)
{
#ifdef BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
	if (VOID_IS_8)
	{
#ifdef _WIN64
		
		if (m_DnaCopy)
			delete m_DnaCopy;
		m_DnaCopy = (char*)btAlignedAlloc(sBulletDNAlen64,16);
		memcpy(m_DnaCopy,sBulletDNAstr64,sBulletDNAlen64);
		parseInternal(verboseMode,(char*)sBulletDNAstr64,sBulletDNAlen64);
#else
		btAssert(0);
#endif
	}
	else
	{
#ifndef _WIN64

		if (m_DnaCopy)
			delete m_DnaCopy;
		m_DnaCopy = (char*)btAlignedAlloc(sBulletDNAlen,16);
		memcpy(m_DnaCopy,sBulletDNAstr,sBulletDNAlen);
		parseInternal(verboseMode,m_DnaCopy,sBulletDNAlen);
#else
		btAssert(0);
#endif
	}
#else//BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
	if (VOID_IS_8)
	{
		if (m_DnaCopy)
			delete m_DnaCopy;
		m_DnaCopy = (char*)btAlignedAlloc(sBulletDNAlen64,16);
		memcpy(m_DnaCopy,sBulletDNAstr64,sBulletDNAlen64);
		parseInternal(verboseMode,m_DnaCopy,sBulletDNAlen64);
	}
	else
	{
		if (m_DnaCopy)
			delete m_DnaCopy;
		m_DnaCopy = (char*)btAlignedAlloc(sBulletDNAlen,16);
		memcpy(m_DnaCopy,sBulletDNAstr,sBulletDNAlen);
		parseInternal(verboseMode,m_DnaCopy,sBulletDNAlen);
	}
#endif//BT_INTERNAL_UPDATE_SERIALIZATION_STRUCTURES
	
	//the parsing will convert to cpu endian
	mFlags &=~FD_ENDIAN_SWAP;

	int littleEndian= 1;
	littleEndian= ((char*)&littleEndian)[0];
	
	mFileBuffer[8] = littleEndian?'v':'V';
	
}

// experimental
int		btBulletFile::write(const char* fileName, bool fixupPointers)
{
	FILE *fp = fopen(fileName, "wb");
	if (fp)
	{
		char header[SIZEOFBLENDERHEADER] ;
		memcpy(header, m_headerString, 7);
		int endian= 1;
		endian= ((char*)&endian)[0];

		if (endian)
		{
			header[7] = '_';
		} else
		{
			header[7] = '-';
		}
		if (VOID_IS_8)
		{
			header[8]='V';
		} else
		{
			header[8]='v';
		}

		header[9] = '2';
		header[10] = '7';
		header[11] = '5';
		
		fwrite(header,SIZEOFBLENDERHEADER,1,fp);

		writeChunks(fp, fixupPointers);

		writeDNA(fp);

		fclose(fp);
		
	} else
	{
		printf("Error: cannot open file %s for writing\n",fileName);
		return 0;
	}
	return 1;
}



void	btBulletFile::addStruct(const	char* structType,void* data, int len, void* oldPtr, int code)
{
	
	bParse::bChunkInd dataChunk;
	dataChunk.code = code;
	dataChunk.nr = 1;
	dataChunk.len = len;
	dataChunk.dna_nr = mMemoryDNA->getReverseType(structType);
	dataChunk.oldPtr = oldPtr;

	///Perform structure size validation
	short* structInfo= mMemoryDNA->getStruct(dataChunk.dna_nr);
	int elemBytes;
	elemBytes= mMemoryDNA->getLength(structInfo[0]);
//	int elemBytes = mMemoryDNA->getElementSize(structInfo[0],structInfo[1]);
	assert(len==elemBytes);

	mLibPointers.insert(dataChunk.oldPtr, (bStructHandle*)data);
	m_chunks.push_back(dataChunk);
}