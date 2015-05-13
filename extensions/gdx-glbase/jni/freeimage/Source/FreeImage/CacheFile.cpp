// ==========================================================
// Multi-Page functions
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - checkered (checkered@users.sourceforge.net)
//
// This file is part of FreeImage 3
//
// COVERED CODE IS PROVIDED UNDER THIS LICENSE ON AN "AS IS" BASIS, WITHOUT WARRANTY
// OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
// THAT THE COVERED CODE IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE
// OR NON-INFRINGING. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE COVERED
// CODE IS WITH YOU. SHOULD ANY COVERED CODE PROVE DEFECTIVE IN ANY RESPECT, YOU (NOT
// THE INITIAL DEVELOPER OR ANY OTHER CONTRIBUTOR) ASSUME THE COST OF ANY NECESSARY
// SERVICING, REPAIR OR CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL
// PART OF THIS LICENSE. NO USE OF ANY COVERED CODE IS AUTHORIZED HEREUNDER EXCEPT UNDER
// THIS DISCLAIMER.
//
// Use at your own risk!
// ==========================================================

#ifdef _MSC_VER 
#pragma warning (disable : 4786) // identifier was truncated to 'number' characters
#endif 

#include "CacheFile.h"

// ----------------------------------------------------------

CacheFile::CacheFile(const std::string filename, BOOL keep_in_memory) :
m_file(NULL),
m_filename(filename),
m_free_pages(),
m_page_cache_mem(),
m_page_cache_disk(),
m_page_map(),
m_page_count(0),
m_current_block(NULL),
m_keep_in_memory(keep_in_memory) {
}

CacheFile::~CacheFile() {
}

BOOL
CacheFile::open() {
	if ((!m_filename.empty()) && (!m_keep_in_memory)) {
		m_file = fopen(m_filename.c_str(), "w+b");
		return (m_file != NULL);
	}

	return (m_keep_in_memory == TRUE);
}

void
CacheFile::close() {
	// dispose the cache entries

	while (!m_page_cache_disk.empty()) {
		Block *block = *m_page_cache_disk.begin();
		m_page_cache_disk.pop_front();
		delete [] block->data;
		delete block;
	}
	while (!m_page_cache_mem.empty()) { 
		Block *block = *m_page_cache_mem.begin(); 
		m_page_cache_mem.pop_front(); 
		delete [] block->data; 
		delete block; 
	} 

	if (m_file) {
		// close the file

		fclose(m_file);

		// delete the file

		remove(m_filename.c_str());
	}
}

void
CacheFile::cleanupMemCache() {
	if (!m_keep_in_memory) {
		if (m_page_cache_mem.size() > CACHE_SIZE) {
			// flush the least used block to file

			Block *old_block = m_page_cache_mem.back();
			fseek(m_file, old_block->nr * BLOCK_SIZE, SEEK_SET);
			fwrite(old_block->data, BLOCK_SIZE, 1, m_file);

			// remove the data

			delete [] old_block->data;
			old_block->data = NULL;

			// move the block to another list

			m_page_cache_disk.splice(m_page_cache_disk.begin(), m_page_cache_mem, --m_page_cache_mem.end());
			m_page_map[old_block->nr] = m_page_cache_disk.begin();
		}
	}
}

int
CacheFile::allocateBlock() {
	Block *block = new Block;
	block->data = new BYTE[BLOCK_SIZE];
	block->next = 0;

	if (!m_free_pages.empty()) {
		block->nr = *m_free_pages.begin();
		m_free_pages.pop_front();
	} else {
		block->nr = m_page_count++;
	}

	m_page_cache_mem.push_front(block);
	m_page_map[block->nr] = m_page_cache_mem.begin();

	cleanupMemCache();

	return block->nr;
}

Block *
CacheFile::lockBlock(int nr) {
	if (m_current_block == NULL) {
		PageMapIt it = m_page_map.find(nr);

		if (it != m_page_map.end()) {
			m_current_block = *(it->second);

			// the block is swapped out to disc. load it back
			// and remove the block from the cache. it might get cached
			// again as soon as the memory buffer fills up

			if (m_current_block->data == NULL) {
				m_current_block->data = new BYTE[BLOCK_SIZE];

				fseek(m_file, m_current_block->nr * BLOCK_SIZE, SEEK_SET);
				fread(m_current_block->data, BLOCK_SIZE, 1, m_file);

				m_page_cache_mem.splice(m_page_cache_mem.begin(), m_page_cache_disk, it->second);
				m_page_map[nr] = m_page_cache_mem.begin();
			}

			// if the memory cache size is too large, swap an item to disc

			cleanupMemCache();

			// return the current block

			return m_current_block;
		}
	}

	return NULL;
}

BOOL
CacheFile::unlockBlock(int nr) {
	if (m_current_block) {
		m_current_block = NULL;

		return TRUE;
	}

	return FALSE;
}

BOOL
CacheFile::deleteBlock(int nr) {
	if (!m_current_block) {
		PageMapIt it = m_page_map.find(nr);

		// remove block from cache

		if (it != m_page_map.end())
			m_page_map.erase(nr);

		// add block to free page list

		m_free_pages.push_back(nr);

		return TRUE;
	}

	return FALSE;
}

BOOL
CacheFile::readFile(BYTE *data, int nr, int size) {
	if ((data) && (size > 0)) {
		int s = 0;
		int block_nr = nr;

		do {
			int copy_nr = block_nr;

			Block *block = lockBlock(copy_nr);

			block_nr = block->next;

			memcpy(data + s, block->data, (s + BLOCK_SIZE > size) ? size - s : BLOCK_SIZE);

			unlockBlock(copy_nr);

			s += BLOCK_SIZE;
		} while (block_nr != 0);

		return TRUE;
	}

	return FALSE;
}

int
CacheFile::writeFile(BYTE *data, int size) {
	if ((data) && (size > 0)) {
		int nr_blocks_required = 1 + (size / BLOCK_SIZE);
		int count = 0;
		int s = 0;
		int stored_alloc;
		int alloc;
		
		stored_alloc = alloc = allocateBlock();

		do {
			int copy_alloc = alloc;

			Block *block = lockBlock(copy_alloc);

			block->next = 0;

			memcpy(block->data, data + s, (s + BLOCK_SIZE > size) ? size - s : BLOCK_SIZE);

			if (count + 1 < nr_blocks_required)
				alloc = block->next = allocateBlock();

			unlockBlock(copy_alloc);

			s += BLOCK_SIZE;			
		} while (++count < nr_blocks_required);

		return stored_alloc;
	}

	return 0;
}

void
CacheFile::deleteFile(int nr) {
	do {
		Block *block = lockBlock(nr);

		if (block == NULL)
			break;

		int next = block->next;

		unlockBlock(nr);

		deleteBlock(nr);

		nr = next;
	} while (nr != 0);
}

