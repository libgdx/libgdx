// ==========================================================
// Multi-Page functions
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - Laurent Rocher (rocherl@club-internet.fr)
// - Steve Johnson (steve@parisgroup.net)
// - Petr Pytelka (pyta@lightcomp.com)
// - Hervé Drolon (drolon@infonie.fr)
// - Vadim Alexandrov (vadimalexandrov@users.sourceforge.net
// - Martin Dyring-Andersen (mda@spamfighter.com)
// - Volodymyr Goncharov (volodymyr.goncharov@gmail.com)
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
#include "FreeImageIO.h"
#include "Plugin.h"
#include "Utilities.h"
#include "FreeImage.h"

// ----------------------------------------------------------

enum BlockType { BLOCK_CONTINUEUS, BLOCK_REFERENCE };

// ----------------------------------------------------------

struct BlockTypeS {
	BlockType m_type;

	BlockTypeS(BlockType type) : m_type(type) {
	}
	virtual ~BlockTypeS() {}
};

struct BlockContinueus : public BlockTypeS {
	int       m_start;
	int       m_end;

	BlockContinueus(int s, int e) : BlockTypeS(BLOCK_CONTINUEUS),
	m_start(s),
	m_end(e) {
	}	
};

struct BlockReference : public BlockTypeS {
	int       m_reference;
	int       m_size;

	BlockReference(int r, int size) : BlockTypeS(BLOCK_REFERENCE),
	m_reference(r),
	m_size(size) {
	}
};

// ----------------------------------------------------------

typedef std::list<BlockTypeS *> BlockList;
typedef std::list<BlockTypeS *>::iterator BlockListIterator;

// ----------------------------------------------------------

FI_STRUCT (MULTIBITMAPHEADER) {
	PluginNode *node;
	FREE_IMAGE_FORMAT fif;
	FreeImageIO *io;
	fi_handle handle;
	CacheFile *m_cachefile;
	std::map<FIBITMAP *, int> locked_pages;
	BOOL changed;
	int page_count;
	BlockList m_blocks;
	char *m_filename;
	BOOL read_only;
	FREE_IMAGE_FORMAT cache_fif;
	int load_flags;
};

// =====================================================================
// Helper functions
// =====================================================================

inline void
ReplaceExtension(std::string& dst_filename, const std::string& src_filename, const std::string& dst_extension) {
	size_t lastDot = src_filename.find_last_of('.');
	if (lastDot == std::string::npos) {
		dst_filename = src_filename;
		dst_filename += ".";
		dst_filename += dst_extension;
	}
	else {
		dst_filename = src_filename.substr(0, lastDot + 1);
		dst_filename += dst_extension;
	}
}

// =====================================================================
// Internal Multipage functions
// =====================================================================

inline MULTIBITMAPHEADER *
FreeImage_GetMultiBitmapHeader(FIMULTIBITMAP *bitmap) {
	return (MULTIBITMAPHEADER *)bitmap->data;
}

static BlockListIterator DLL_CALLCONV
FreeImage_FindBlock(FIMULTIBITMAP *bitmap, int position) {
	assert(NULL != bitmap);

	MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

	// step 1: find the block that matches the given position

	int prev_count = 0;
	int count = 0;
	BlockListIterator i;
	BlockTypeS *current_block = NULL;

	for (i = header->m_blocks.begin(); i != header->m_blocks.end(); ++i) {
		prev_count = count;

		switch((*i)->m_type) {
			case BLOCK_CONTINUEUS :
				count += ((BlockContinueus *)(*i))->m_end - ((BlockContinueus *)(*i))->m_start + 1;
				break;

			case BLOCK_REFERENCE :
				count++;
				break;
		}

		current_block = *i;

		if (count > position)
			break;
	}

	// step 2: make sure we found the node. from here it gets a little complicated:
	// * if the block is there, just return it
	// * if the block is a series of blocks, split it in max 3 new blocks
	//   and return the splitted block

	if ((current_block) && (count > position)) {
		switch(current_block->m_type) {
			case BLOCK_REFERENCE :
				return i;

			case BLOCK_CONTINUEUS :
			{
				BlockContinueus *block = (BlockContinueus *)current_block;

				if (block->m_start != block->m_end) {
					int item = block->m_start + (position - prev_count);

					// left part

					if (item != block->m_start) {
						BlockContinueus *block_a = new BlockContinueus(block->m_start, item - 1);
						header->m_blocks.insert(i, (BlockTypeS *)block_a);
					}

					// middle part

					BlockContinueus *block_b = new BlockContinueus(item, item);
					BlockListIterator block_target = header->m_blocks.insert(i, (BlockTypeS *)block_b);

					// right part

					if (item != block->m_end) {
						BlockContinueus *block_c = new BlockContinueus(item + 1, block->m_end);
						header->m_blocks.insert(i, (BlockTypeS *)block_c);
					}

					// remove the old block that was just splitted

					header->m_blocks.remove((BlockTypeS *)block);
					delete block;

					// return the splitted block
					
					return block_target;
				}

				return i;
			}
		}
	}
	// we should never go here ...
	assert(false);
	return header->m_blocks.end();
}

int DLL_CALLCONV
FreeImage_InternalGetPageCount(FIMULTIBITMAP *bitmap) {	
	if (bitmap) {
		if (((MULTIBITMAPHEADER *)bitmap->data)->handle) {
			MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

			header->io->seek_proc(header->handle, 0, SEEK_SET);

   			void *data = FreeImage_Open(header->node, header->io, header->handle, TRUE);

			int page_count = (header->node->m_plugin->pagecount_proc != NULL) ? header->node->m_plugin->pagecount_proc(header->io, header->handle, data) : 1;

			FreeImage_Close(header->node, header->io, header->handle, data);

			return page_count;
		}
	}

	return 0;
}

// =====================================================================
// Multipage functions
// =====================================================================

FIMULTIBITMAP * DLL_CALLCONV
FreeImage_OpenMultiBitmap(FREE_IMAGE_FORMAT fif, const char *filename, BOOL create_new, BOOL read_only, BOOL keep_cache_in_memory, int flags) {

	FILE *handle = NULL;
	try {
		// sanity check on the parameters

		if (create_new) {
			read_only = FALSE;
		}

		// retrieve the plugin list to find the node belonging to this plugin

		PluginList *list = FreeImage_GetPluginList();

		if (list) {
			PluginNode *node = list->FindNodeFromFIF(fif);

			if (node) {
				std::auto_ptr<FreeImageIO> io (new FreeImageIO);

				SetDefaultIO(io.get());

				if (!create_new) {
					handle = fopen(filename, "rb");
					if (handle == NULL) {
						return NULL;
					}
				}

				std::auto_ptr<FIMULTIBITMAP> bitmap (new FIMULTIBITMAP);
				std::auto_ptr<MULTIBITMAPHEADER> header (new MULTIBITMAPHEADER);
				header->m_filename = new char[strlen(filename) + 1];
				strcpy(header->m_filename, filename);
				header->node = node;
				header->fif = fif;
				header->io = io.get ();
				header->handle = handle;						
				header->changed = FALSE;						
				header->read_only = read_only;
				header->m_cachefile = NULL;
				header->cache_fif = fif;
				header->load_flags = flags;

				// store the MULTIBITMAPHEADER in the surrounding FIMULTIBITMAP structure

				bitmap->data = header.get();

				// cache the page count

				header->page_count = FreeImage_InternalGetPageCount(bitmap.get());

				// allocate a continueus block to describe the bitmap

				if (!create_new) {
					header->m_blocks.push_back((BlockTypeS *)new BlockContinueus(0, header->page_count - 1));
				}

				// set up the cache

				if (!read_only) {
					std::string cache_name;
					ReplaceExtension(cache_name, filename, "ficache");

					std::auto_ptr<CacheFile> cache_file (new CacheFile(cache_name, keep_cache_in_memory));

					if (cache_file->open()) {
						// we can use release() as std::bad_alloc won't be thrown from here on
						header->m_cachefile = cache_file.release();
					} else {
						// an error occured ...
						fclose(handle);
						return NULL;
					}
				}
				// return the multibitmap
				// std::bad_alloc won't be thrown from here on
				header.release(); // now owned by bitmap
				io.release();	  // now owned by bitmap
				return bitmap.release(); // now owned by caller
			}
		}
	} catch (std::bad_alloc &) {
		/** @todo report error */
	}
	if (handle)
		fclose(handle);
	return NULL;
}

FIMULTIBITMAP * DLL_CALLCONV
FreeImage_OpenMultiBitmapFromHandle(FREE_IMAGE_FORMAT fif, FreeImageIO *io, fi_handle handle, int flags) {
	try {
		BOOL read_only = FALSE;	// modifications (if any) will be stored into the memory cache

		if (io && handle) {
		
			// retrieve the plugin list to find the node belonging to this plugin
			PluginList *list = FreeImage_GetPluginList();
		
			if (list) {
				PluginNode *node = list->FindNodeFromFIF(fif);
			
				if (node) {
					std::auto_ptr<FIMULTIBITMAP> bitmap (new FIMULTIBITMAP);
					std::auto_ptr<MULTIBITMAPHEADER> header (new MULTIBITMAPHEADER);
					std::auto_ptr<FreeImageIO> tmp_io (new FreeImageIO (*io));
					header->io = tmp_io.get();
					header->m_filename = NULL;
					header->node = node;
					header->fif = fif;
					header->handle = handle;						
					header->changed = FALSE;						
					header->read_only = read_only;	
					header->m_cachefile = NULL;
					header->cache_fif = fif;
					header->load_flags = flags;
							
					// store the MULTIBITMAPHEADER in the surrounding FIMULTIBITMAP structure

					bitmap->data = header.get();

					// cache the page count

					header->page_count = FreeImage_InternalGetPageCount(bitmap.get());

					// allocate a continueus block to describe the bitmap

					header->m_blocks.push_back((BlockTypeS *)new BlockContinueus(0, header->page_count - 1));

					if (!read_only) {
						// set up the cache
						std::auto_ptr<CacheFile> cache_file (new CacheFile("", TRUE));
						
						if (cache_file->open()) {
							header->m_cachefile = cache_file.release();
						}
					}
					tmp_io.release();
					header.release();
					return bitmap.release();
				}
			}
		}
	} catch (std::bad_alloc &) {
		/** @todo report error */
	}
	return NULL;
}

BOOL DLL_CALLCONV
FreeImage_SaveMultiBitmapToHandle(FREE_IMAGE_FORMAT fif, FIMULTIBITMAP *bitmap, FreeImageIO *io, fi_handle handle, int flags) {
	if(!bitmap || !bitmap->data || !io || !handle) {
		return FALSE;
	}

	BOOL success = TRUE;

	// retrieve the plugin list to find the node belonging to this plugin
	PluginList *list = FreeImage_GetPluginList();
	
	if (list) {
		PluginNode *node = list->FindNodeFromFIF(fif);

		if(node) {
			MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);
			
			// dst data
			void *data = FreeImage_Open(node, io, handle, FALSE);
			// src data
			void *data_read = NULL;
			
			if(header->handle) {
				// open src
				header->io->seek_proc(header->handle, 0, SEEK_SET);
				data_read = FreeImage_Open(header->node, header->io, header->handle, TRUE);
			}
			
			// write all the pages to the file using handle and io
			
			int count = 0;
			
			for (BlockListIterator i = header->m_blocks.begin(); i != header->m_blocks.end(); i++) {
				if (success) {
					switch((*i)->m_type) {
						case BLOCK_CONTINUEUS:
						{
							BlockContinueus *block = (BlockContinueus *)(*i);
							
							for (int j = block->m_start; j <= block->m_end; j++) {

								// load the original source data
								FIBITMAP *dib = header->node->m_plugin->load_proc(header->io, header->handle, j, header->load_flags, data_read);
								
								// save the data
								success = node->m_plugin->save_proc(io, dib, handle, count, flags, data);
								count++;
								
								FreeImage_Unload(dib);
							}
							
							break;
						}
						
						case BLOCK_REFERENCE:
						{
							BlockReference *ref = (BlockReference *)(*i);
							
							// read the compressed data
							
							BYTE *compressed_data = (BYTE*)malloc(ref->m_size * sizeof(BYTE));
							
							header->m_cachefile->readFile((BYTE *)compressed_data, ref->m_reference, ref->m_size);
							
							// uncompress the data
							
							FIMEMORY *hmem = FreeImage_OpenMemory(compressed_data, ref->m_size);
							FIBITMAP *dib = FreeImage_LoadFromMemory(header->cache_fif, hmem, 0);
							FreeImage_CloseMemory(hmem);
							
							// get rid of the buffer
							free(compressed_data);
							
							// save the data
							
							success = node->m_plugin->save_proc(io, dib, handle, count, flags, data);
							count++;
							
							// unload the dib

							FreeImage_Unload(dib);

							break;
						}
					}
				} else {
					break;
				}
			}
			
			// close the files
			
			FreeImage_Close(header->node, header->io, header->handle, data_read);

			FreeImage_Close(node, io, handle, data); 
			
			return success;
		}
	}

	return FALSE;
}


BOOL DLL_CALLCONV
FreeImage_CloseMultiBitmap(FIMULTIBITMAP *bitmap, int flags) {
	if (bitmap) {
		BOOL success = TRUE;
		
		if (bitmap->data) {
			MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);			
			
			// saves changes only of images loaded directly from a file
			if (header->changed && header->m_filename) {
				try {
					// open a temp file

					std::string spool_name;

					ReplaceExtension(spool_name, header->m_filename, "fispool");

					// open the spool file and the source file
        
					FILE *f = fopen(spool_name.c_str(), "w+b");
				
					// saves changes
					if (f == NULL) {
						FreeImage_OutputMessageProc(header->fif, "Failed to open %s, %s", spool_name.c_str(), strerror(errno));
						success = FALSE;
					} else {
						success = FreeImage_SaveMultiBitmapToHandle(header->fif, bitmap, header->io, (fi_handle)f, flags);

						// close the files

						if (fclose(f) != 0) {
							success = FALSE;
							FreeImage_OutputMessageProc(header->fif, "Failed to close %s, %s", spool_name.c_str(), strerror(errno));
						}
					}
					if (header->handle) {
						fclose((FILE *)header->handle);
					}
				
					// applies changes to the destination file

					if (success) {
						remove(header->m_filename);
						success = (rename(spool_name.c_str(), header->m_filename) == 0) ? TRUE:FALSE;
						if(!success) {
							FreeImage_OutputMessageProc(header->fif, "Failed to rename %s to %s", spool_name.c_str(), header->m_filename);
						}
					} else {
						remove(spool_name.c_str());
					}
				} catch (std::bad_alloc &) {
					success = FALSE;
				}

			} else {
				if (header->handle && header->m_filename) {
					fclose((FILE *)header->handle);
				}
			}

			// clear the blocks list

			for (BlockListIterator i = header->m_blocks.begin(); i != header->m_blocks.end(); ++i) {
				delete *i;
			}

			// flush and dispose the cache

			if (header->m_cachefile) {
				header->m_cachefile->close();
				delete header->m_cachefile;
			}

			// delete the last open bitmaps

			while (!header->locked_pages.empty()) {
				FreeImage_Unload(header->locked_pages.begin()->first);

				header->locked_pages.erase(header->locked_pages.begin()->first);
			}

			// get rid of the IO structure

			delete header->io;

			// delete the filename

			if(header->m_filename) {
				delete[] header->m_filename;
			}

			// delete the FIMULTIBITMAPHEADER

			delete header;
		}

		delete bitmap;

		return success;
	}

	return FALSE;
}

int DLL_CALLCONV
FreeImage_GetPageCount(FIMULTIBITMAP *bitmap) {
	if (bitmap) {
		MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

		if (header->page_count == -1) {
			header->page_count = 0;

			for (BlockListIterator i = header->m_blocks.begin(); i != header->m_blocks.end(); ++i) {
				switch((*i)->m_type) {
					case BLOCK_CONTINUEUS :
						header->page_count += ((BlockContinueus *)(*i))->m_end - ((BlockContinueus *)(*i))->m_start + 1;
						break;

					case BLOCK_REFERENCE :
						header->page_count++;
						break;
				}
			}
		}

		return header->page_count;
	}

	return 0;
}

static BlockReference* 
FreeImage_SavePageToBlock(MULTIBITMAPHEADER *header, FIBITMAP *data) {
	if (header->read_only || !header->locked_pages.empty())
		return NULL;

	DWORD compressed_size = 0;
	BYTE *compressed_data = NULL;

	// compress the bitmap data

	// open a memory handle
	FIMEMORY *hmem = FreeImage_OpenMemory();
	if(hmem==NULL) return NULL;
	// save the file to memory
	if(!FreeImage_SaveToMemory(header->cache_fif, data, hmem, 0)) {
		FreeImage_CloseMemory(hmem);
		return NULL;
	}
	// get the buffer from the memory stream
	if(!FreeImage_AcquireMemory(hmem, &compressed_data, &compressed_size)) {
		FreeImage_CloseMemory(hmem);
		return NULL;
	}

	// write the compressed data to the cache
	int ref = header->m_cachefile->writeFile(compressed_data, compressed_size);
	// get rid of the compressed data
	FreeImage_CloseMemory(hmem);

	return new(std::nothrow) BlockReference(ref, compressed_size);
}

void DLL_CALLCONV
FreeImage_AppendPage(FIMULTIBITMAP *bitmap, FIBITMAP *data) {
	if (!bitmap || !data) 
		return;

	MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

	BlockReference *block = FreeImage_SavePageToBlock(header, data);
	if(block==NULL) return;

	// add the block
	header->m_blocks.push_back((BlockTypeS *)block);
	header->changed = TRUE;
	header->page_count = -1;
}

void DLL_CALLCONV
FreeImage_InsertPage(FIMULTIBITMAP *bitmap, int page, FIBITMAP *data) {
	if (!bitmap || !data) 
		return;

	if (page >= FreeImage_GetPageCount(bitmap)) 
		return;
			
	MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

	BlockReference *block = FreeImage_SavePageToBlock(header, data);
	if(block==NULL) return;

	// add a block
	if (page > 0) {
		BlockListIterator block_source = FreeImage_FindBlock(bitmap, page);		

		header->m_blocks.insert(block_source, (BlockTypeS *)block);
	} else {
		header->m_blocks.push_front((BlockTypeS *)block);
	}

	header->changed = TRUE;
	header->page_count = -1;
}

void DLL_CALLCONV
FreeImage_DeletePage(FIMULTIBITMAP *bitmap, int page) {
	if (bitmap) {
		MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

		if ((!header->read_only) && (header->locked_pages.empty())) {
			if (FreeImage_GetPageCount(bitmap) > 1) {
				BlockListIterator i = FreeImage_FindBlock(bitmap, page);

				if (i != header->m_blocks.end()) {
					switch((*i)->m_type) {
						case BLOCK_CONTINUEUS :
							delete *i;
							header->m_blocks.erase(i);
							break;

						case BLOCK_REFERENCE :
							header->m_cachefile->deleteFile(((BlockReference *)(*i))->m_reference);
							delete *i;
							header->m_blocks.erase(i);
							break;
					}

					header->changed = TRUE;
					header->page_count = -1;
				}
			}
		}
	}
}


FIBITMAP * DLL_CALLCONV
FreeImage_LockPage(FIMULTIBITMAP *bitmap, int page) {
	if (bitmap) {
		MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

		// only lock if the page wasn't locked before...

		for (std::map<FIBITMAP *, int>::iterator i = header->locked_pages.begin(); i != header->locked_pages.end(); ++i) {
			if (i->second == page) {
				return NULL;
			}
		}

		// open the bitmap

		header->io->seek_proc(header->handle, 0, SEEK_SET);

   		void *data = FreeImage_Open(header->node, header->io, header->handle, TRUE);

		// load the bitmap data

		if (data != NULL) {
			FIBITMAP *dib = (header->node->m_plugin->load_proc != NULL) ? header->node->m_plugin->load_proc(header->io, header->handle, page, header->load_flags, data) : NULL;

			// close the file

			FreeImage_Close(header->node, header->io, header->handle, data);

			// if there was still another bitmap open, get rid of it

			if (dib) {
				header->locked_pages[dib] = page;

				return dib;
			}	

			return NULL;
		}
	}

	return NULL;
}

void DLL_CALLCONV
FreeImage_UnlockPage(FIMULTIBITMAP *bitmap, FIBITMAP *page, BOOL changed) {
	if ((bitmap) && (page)) {
		MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

		// find out if the page we try to unlock is actually locked...

		if (header->locked_pages.find(page) != header->locked_pages.end()) {
			// store the bitmap compressed in the cache for later writing

			if (changed && !header->read_only) {
				header->changed = TRUE;

				// cut loose the block from the rest

				BlockListIterator i = FreeImage_FindBlock(bitmap, header->locked_pages[page]);

				// compress the data

				DWORD compressed_size = 0;
				BYTE *compressed_data = NULL;

				// open a memory handle
				FIMEMORY *hmem = FreeImage_OpenMemory();
				// save the page to memory
				FreeImage_SaveToMemory(header->cache_fif, page, hmem, 0);
				// get the buffer from the memory stream
				FreeImage_AcquireMemory(hmem, &compressed_data, &compressed_size);

				// write the data to the cache

				switch ((*i)->m_type) {
					case BLOCK_CONTINUEUS :
					{
						int iPage = header->m_cachefile->writeFile(compressed_data, compressed_size);

						delete (*i);

						*i = (BlockTypeS *)new BlockReference(iPage, compressed_size);

						break;
					}

					case BLOCK_REFERENCE :
					{
						BlockReference *reference = (BlockReference *)(*i);

						header->m_cachefile->deleteFile(reference->m_reference);

						delete (*i);

						int iPage = header->m_cachefile->writeFile(compressed_data, compressed_size);

						*i = (BlockTypeS *)new BlockReference(iPage, compressed_size);

						break;
					}
				}

				// get rid of the compressed data

				FreeImage_CloseMemory(hmem);
			}

			// reset the locked page so that another page can be locked

			FreeImage_Unload(page);

			header->locked_pages.erase(page);
		}
	}
}

BOOL DLL_CALLCONV
FreeImage_MovePage(FIMULTIBITMAP *bitmap, int target, int source) {
	if (bitmap) {
		MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

		if ((!header->read_only) && (header->locked_pages.empty())) {
			if ((target != source) && ((target >= 0) && (target < FreeImage_GetPageCount(bitmap))) && ((source >= 0) && (source < FreeImage_GetPageCount(bitmap)))) {
				BlockListIterator block_source = FreeImage_FindBlock(bitmap, target);
				BlockListIterator block_target = FreeImage_FindBlock(bitmap, source);

				header->m_blocks.insert(block_target, *block_source);			
				header->m_blocks.erase(block_source);

				header->changed = TRUE;

				return TRUE;
			}
		}
	}

	return FALSE;
}

BOOL DLL_CALLCONV
FreeImage_GetLockedPageNumbers(FIMULTIBITMAP *bitmap, int *pages, int *count) {
	if ((bitmap) && (count)) {
		MULTIBITMAPHEADER *header = FreeImage_GetMultiBitmapHeader(bitmap);

		if ((pages == NULL) || (*count == 0)) {
			*count = (int)header->locked_pages.size();
		} else {
			int c = 0;

			for (std::map<FIBITMAP *, int>::iterator i = header->locked_pages.begin(); i != header->locked_pages.end(); ++i) {
				pages[c] = i->second;

				c++;

				if (c == *count)
					break;
			}
		}

		return TRUE;
	}

	return FALSE;
}

// =====================================================================
// Memory IO Multipage functions
// =====================================================================

FIMULTIBITMAP * DLL_CALLCONV
FreeImage_LoadMultiBitmapFromMemory(FREE_IMAGE_FORMAT fif, FIMEMORY *stream, int flags) {
	BOOL read_only = FALSE;	// modifications (if any) will be stored into the memory cache

	// retrieve the plugin list to find the node belonging to this plugin

	PluginList *list = FreeImage_GetPluginList();

	if (list) {
		PluginNode *node = list->FindNodeFromFIF(fif);

		if (node) {
			FreeImageIO *io = new(std::nothrow) FreeImageIO;

			if (io) {
				SetMemoryIO(io);

				FIMULTIBITMAP *bitmap = new(std::nothrow) FIMULTIBITMAP;

				if (bitmap) {
					MULTIBITMAPHEADER *header = new(std::nothrow) MULTIBITMAPHEADER;

					if (header) {
						header->m_filename = NULL;
						header->node = node;
						header->fif = fif;
						header->io = io;
						header->handle = (fi_handle)stream;						
						header->changed = FALSE;						
						header->read_only = read_only;
						header->m_cachefile = NULL;
						header->cache_fif = fif;
						header->load_flags = flags;

						// store the MULTIBITMAPHEADER in the surrounding FIMULTIBITMAP structure

						bitmap->data = header;

						// cache the page count

						header->page_count = FreeImage_InternalGetPageCount(bitmap);

						// allocate a continueus block to describe the bitmap

						header->m_blocks.push_back((BlockTypeS *)new BlockContinueus(0, header->page_count - 1));
						
						if (!read_only) {
							// set up the cache
							CacheFile *cache_file = new(std::nothrow) CacheFile("", TRUE);
							
							if (cache_file && cache_file->open()) {
								header->m_cachefile = cache_file;
							}
						}

						return bitmap;
					}
					
					delete bitmap;
				}
				
				delete io;
			}
		}
	}

	return NULL;
}

BOOL DLL_CALLCONV
FreeImage_SaveMultiBitmapToMemory(FREE_IMAGE_FORMAT fif, FIMULTIBITMAP *bitmap, FIMEMORY *stream, int flags) {
	if (stream && stream->data) {
		FreeImageIO io;
		SetMemoryIO(&io);

		return FreeImage_SaveMultiBitmapToHandle(fif, bitmap, &io, (fi_handle)stream, flags);
	}

	return FALSE;
}
