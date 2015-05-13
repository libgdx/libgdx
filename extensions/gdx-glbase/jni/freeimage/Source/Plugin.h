// ==========================================================
// FreeImage Plugin Interface
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - Rui Lopes (ruiglopes@yahoo.com)
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

#ifndef PLUGIN_H
#define PLUGIN_H

#include "FreeImage.h"
#include "Utilities.h"

// ==========================================================

struct Plugin;

// =====================================================================
//  Plugin Node
// =====================================================================

FI_STRUCT (PluginNode) {
	/** FREE_IMAGE_FORMAT attached to this plugin */
	int m_id;
	/** Handle to a user plugin DLL (NULL for standard plugins) */
	void *m_instance;
	/** The actual plugin, holding the function pointers */
	Plugin *m_plugin;
	/** Enable/Disable switch */
	BOOL m_enabled;

	/** Unique format string for the plugin */
	const char *m_format;
	/** Description string for the plugin */
	const char *m_description;
	/** Comma separated list of file extensions indicating what files this plugin can open */
	const char *m_extension;
	/** optional regular expression to help	software identifying a bitmap type */
	const char *m_regexpr;
};

// =====================================================================
//  Internal Plugin List
// =====================================================================

class PluginList {
public :
	PluginList();
	~PluginList();

	FREE_IMAGE_FORMAT AddNode(FI_InitProc proc, void *instance = NULL, const char *format = 0, const char *description = 0, const char *extension = 0, const char *regexpr = 0);
	PluginNode *FindNodeFromFormat(const char *format);
	PluginNode *FindNodeFromMime(const char *mime);
	PluginNode *FindNodeFromFIF(int node_id);

	int Size() const;
	BOOL IsEmpty() const;

private :
	std::map<int, PluginNode *> m_plugin_map;
	int m_node_count;
};

// ==========================================================
//   Plugin Initialisation Callback
// ==========================================================

void DLL_CALLCONV FreeImage_OutputMessage(int fif, const char *message, ...);

// =====================================================================
// Reimplementation of stricmp (it is not supported on some systems)
// =====================================================================

int FreeImage_stricmp(const char *s1, const char *s2);

// ==========================================================
//   Internal functions
// ==========================================================

extern "C" {
	BOOL DLL_CALLCONV FreeImage_Validate(FREE_IMAGE_FORMAT fif, FreeImageIO *io, fi_handle handle);
    void * DLL_CALLCONV FreeImage_Open(PluginNode *node, FreeImageIO *io, fi_handle handle, BOOL open_for_reading);
    void DLL_CALLCONV FreeImage_Close(PluginNode *node, FreeImageIO *io, fi_handle handle, void *data); // plugin.cpp
    PluginList * DLL_CALLCONV FreeImage_GetPluginList(); // plugin.cpp
}

// ==========================================================
//   Internal plugins
// ==========================================================

void DLL_CALLCONV InitBMP(Plugin *plugin, int format_id);
void DLL_CALLCONV InitCUT(Plugin *plugin, int format_id);
void DLL_CALLCONV InitICO(Plugin *plugin, int format_id);
void DLL_CALLCONV InitIFF(Plugin *plugin, int format_id);
void DLL_CALLCONV InitJPEG(Plugin *plugin, int format_id);
void DLL_CALLCONV InitKOALA(Plugin *plugin, int format_id);
void DLL_CALLCONV InitLBM(Plugin *plugin, int format_id);
void DLL_CALLCONV InitMNG(Plugin *plugin, int format_id);
void DLL_CALLCONV InitPCD(Plugin *plugin, int format_id);
void DLL_CALLCONV InitPCX(Plugin *plugin, int format_id);
void DLL_CALLCONV InitPNG(Plugin *plugin, int format_id);
void DLL_CALLCONV InitPNM(Plugin *plugin, int format_id);
void DLL_CALLCONV InitPSD(Plugin *plugin, int format_id);
void DLL_CALLCONV InitRAS(Plugin *plugin, int format_id);
void DLL_CALLCONV InitTARGA(Plugin *plugin, int format_id);
//void DLL_CALLCONV InitTIFF(Plugin *plugin, int format_id);
void DLL_CALLCONV InitWBMP(Plugin *plugin, int format_id);
void DLL_CALLCONV InitXBM(Plugin *plugin, int format_id);
void DLL_CALLCONV InitXPM(Plugin *plugin, int format_id);
void DLL_CALLCONV InitDDS(Plugin *plugin, int format_id);
void DLL_CALLCONV InitGIF(Plugin *plugin, int format_id);
void DLL_CALLCONV InitHDR(Plugin *plugin, int format_id);
//void DLL_CALLCONV InitG3(Plugin *plugin, int format_id);
void DLL_CALLCONV InitSGI(Plugin *plugin, int format_id);
void DLL_CALLCONV InitEXR(Plugin *plugin, int format_id);
void DLL_CALLCONV InitJ2K(Plugin *plugin, int format_id);
void DLL_CALLCONV InitJP2(Plugin *plugin, int format_id);
void DLL_CALLCONV InitPFM(Plugin *plugin, int format_id);
void DLL_CALLCONV InitPICT(Plugin *plugin, int format_id);
//void DLL_CALLCONV InitRAW(Plugin *plugin, int format_id);
void DLL_CALLCONV InitJNG(Plugin *plugin, int format_id);
//void DLL_CALLCONV InitWEBP(Plugin *plugin, int format_id);
//void DLL_CALLCONV InitJXR(Plugin *plugin, int format_id);

#endif //!PLUGIN_H
