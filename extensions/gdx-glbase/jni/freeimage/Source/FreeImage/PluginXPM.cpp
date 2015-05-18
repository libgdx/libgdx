// ==========================================================
// XPM Loader and Writer
//
// Design and implementation by
// - Ryan Rubley (ryan@lostreality.org)
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

// IMPLEMENTATION NOTES:
// ------------------------
// Initial design and implementation by
// - Karl-Heinz Bussian (khbussian@moss.de)
// - Hervé Drolon (drolon@infonie.fr)
// Completely rewritten from scratch by Ryan Rubley (ryan@lostreality.org)
// in order to address the following major fixes:
// * Supports any number of chars per pixel (not just 1 or 2)
// * Files with 2 chars per pixel but <= 256colors are loaded as 256 color (not 24bit)
// * Loads much faster, uses much less memory
// * supports #rgb #rrrgggbbb and #rrrrggggbbbb colors (not just #rrggbb)
// * supports symbolic color names
// ==========================================================

#include "FreeImage.h"
#include "Utilities.h"

// ==========================================================
// Plugin Interface
// ==========================================================
static int s_format_id;

// ==========================================================
// Internal Functions
// ==========================================================

// read in and skip all junk until we find a certain char
static BOOL
FindChar(FreeImageIO *io, fi_handle handle, BYTE look_for) {
	BYTE c;
	io->read_proc(&c, sizeof(BYTE), 1, handle);
	while(c != look_for) {
		if( io->read_proc(&c, sizeof(BYTE), 1, handle) != 1 )
			return FALSE;
	}
	return TRUE;
}

// find start of string, read data until ending quote found, allocate memory and return a string
static char *
ReadString(FreeImageIO *io, fi_handle handle) {
	if( !FindChar(io, handle,'"') )
		return NULL;
	BYTE c;
	std::string s;
	io->read_proc(&c, sizeof(BYTE), 1, handle);
	while(c != '"') {
		s += c;
		if( io->read_proc(&c, sizeof(BYTE), 1, handle) != 1 )
			return NULL;
	}
	char *cstr = (char *)malloc(s.length()+1);
	strcpy(cstr,s.c_str());
	return cstr;
}

static char *
Base92(unsigned int num) {
	static char b92[16]; //enough for more then 64 bits
	static char digit[] = " .XoO+@#$%&*=-;:>,<1234567890qwertyuipasdfghjklzxcvbnmMNBVCZASDFGHJKLPIUYTREWQ!~^/()_`'][{}|";
	b92[15] = '\0';
	int i = 14;
	do {
		b92[i--] = digit[num % 92];
		num /= 92;
	} while( num && i >= 0 );
	return b92+i+1;
}

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "XPM";
}

static const char * DLL_CALLCONV
Description() {
	return "X11 Pixmap Format";
}

static const char * DLL_CALLCONV
Extension() {
	return "xpm";
}

static const char * DLL_CALLCONV
RegExpr() {
	return "^[ \\t]*/\\* XPM \\*/[ \\t]$";
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/x-xpixmap";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	char buffer[256];

	// checks the first 256 characters for the magic string
	int count = io->read_proc(buffer, 1, 256, handle);
	if(count <= 9) return FALSE;
	for(int i = 0; i < (count - 9); i++) {
		if(strncmp(&buffer[i], "/* XPM */", 9) == 0)
			return TRUE;
	}
	return FALSE;
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return (
			(depth == 8) ||
			(depth == 24)
		);
}

static BOOL DLL_CALLCONV
SupportsExportType(FREE_IMAGE_TYPE type) {
	return (type == FIT_BITMAP) ? TRUE : FALSE;
}

static BOOL DLL_CALLCONV
SupportsNoPixels() {
	return TRUE;
}

// ----------------------------------------------------------

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	char msg[256];
    FIBITMAP *dib = NULL;

    if (!handle) return NULL;

    try {
		char *str;
		
		BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;
		
		//find the starting brace
		if( !FindChar(io, handle,'{') )
			throw "Could not find starting brace";

		//read info string
		str = ReadString(io, handle);
		if(!str)
			throw "Error reading info string";

		int width, height, colors, cpp;
		if( sscanf(str, "%d %d %d %d", &width, &height, &colors, &cpp) != 4 ) {
			free(str);
			throw "Improperly formed info string";
		}
		free(str);

        if (colors > 256) {
			dib = FreeImage_AllocateHeader(header_only, width, height, 24, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
		} else {
			dib = FreeImage_AllocateHeader(header_only, width, height, 8);
		}

		//build a map of color chars to rgb values
		std::map<std::string,FILE_RGBA> rawpal; //will store index in Alpha if 8bpp
		for(int i = 0; i < colors; i++ ) {
			FILE_RGBA rgba;

			str = ReadString(io, handle);
			if(!str)
				throw "Error reading color strings";

			std::string chrs(str,cpp); //create a string for the color chars using the first cpp chars
			char *keys = str + cpp; //the color keys for these chars start after the first cpp chars

			//translate all the tabs to spaces
			char *tmp = keys;
			while( strchr(tmp,'\t') ) {
				tmp = strchr(tmp,'\t');
				*tmp++ = ' ';
			}

			//prefer the color visual
			if( strstr(keys," c ") ) {
				char *clr = strstr(keys," c ") + 3;
				while( *clr == ' ' ) clr++; //find the start of the hex rgb value
				if( *clr == '#' ) {
					int red = 0, green = 0, blue = 0, n;
					clr++;
					//end string at first space, if any found
					if( strchr(clr,' ') )
						*(strchr(clr,' ')) = '\0';
					//parse hex color, it can be #rgb #rrggbb #rrrgggbbb or #rrrrggggbbbb
					switch( strlen(clr) ) {
						case 3:	n = sscanf(clr,"%01x%01x%01x",&red,&green,&blue);
							red |= (red << 4);
							green |= (green << 4);
							blue |= (blue << 4);
							break;
						case 6:	n = sscanf(clr,"%02x%02x%02x",&red,&green,&blue);
							break;
						case 9:	n = sscanf(clr,"%03x%03x%03x",&red,&green,&blue);
							red >>= 4;
							green >>= 4;
							blue >>= 4;
							break;
						case 12: n = sscanf(clr,"%04x%04x%04x",&red,&green,&blue);
							red >>= 8;
							green >>= 8;
							blue >>= 8;
							break;
						default:
							n = 0;
							break;
					}
					if( n != 3 ) {
						free(str);
						throw "Improperly formed hex color value";
					}
					rgba.r = (BYTE)red;
					rgba.g = (BYTE)green;
					rgba.b = (BYTE)blue;
				} else if( !strncmp(clr,"None",4) || !strncmp(clr,"none",4) ) {
					rgba.r = rgba.g = rgba.b = 0xFF;
				} else {
					char *tmp = clr;

					//scan forward for each space, if its " x " or " xx " end the string there
					//this means its probably some other visual data beyond that point and not
					//part of the color name.  How many named color end with a 1 or 2 character
					//word? Probably none in our list at least.
					while( (tmp = strchr(tmp,' ')) != NULL ) {
						if( tmp[1] != ' ' ) {
							if( (tmp[2] == ' ') || (tmp[2] != ' ' && tmp[3] == ' ') ) {
								tmp[0] = '\0';
								break;
							}
						}
						tmp++;
					}

					//remove any trailing spaces
					tmp = clr+strlen(clr)-1;
					while( *tmp == ' ' ) {
						*tmp = '\0';
						tmp--;
					}

					if (!FreeImage_LookupX11Color(clr,  &rgba.r, &rgba.g, &rgba.b)) {
						sprintf(msg, "Unknown color name '%s'", str);
						free(str);
						throw msg;
					}
				}
			} else {
				free(str);
				throw "Only color visuals are supported";
			}

			//add color to map
			rgba.a = (BYTE)((colors > 256) ? 0 : i);
			rawpal[chrs] = rgba;

			//build palette if needed
			if( colors <= 256 ) {
				RGBQUAD *pal = FreeImage_GetPalette(dib);
				pal[i].rgbBlue = rgba.b;
				pal[i].rgbGreen = rgba.g;
				pal[i].rgbRed = rgba.r;
			}

			free(str);
		}
		//done parsing color map

		if(header_only) {
			// header only mode
			return dib;
		}

		//read in pixel data
		for(int y = 0; y < height; y++ ) {
			BYTE *line = FreeImage_GetScanLine(dib, height - y - 1);
			str = ReadString(io, handle);
			if(!str)
				throw "Error reading pixel strings";
			char *pixel_ptr = str;

			for(int x = 0; x < width; x++ ) {
				//locate the chars in the color map
				std::string chrs(pixel_ptr,cpp);
				FILE_RGBA rgba = rawpal[chrs];

				if( colors > 256 ) {
					line[FI_RGBA_BLUE] = rgba.b;
					line[FI_RGBA_GREEN] = rgba.g;
					line[FI_RGBA_RED] = rgba.r;
					line += 3;
				} else {
					*line = rgba.a;
					line++;
				}

				pixel_ptr += cpp;
			}

			free(str);
		}
		//done reading pixel data

		return dib;
	} catch(const char *text) {
       FreeImage_OutputMessageProc(s_format_id, text);

       if( dib != NULL )
           FreeImage_Unload(dib);

       return NULL;
    }
}

static BOOL DLL_CALLCONV
Save(FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int page, int flags, void *data) {
	if ((dib != NULL) && (handle != NULL)) {
		char header[] = "/* XPM */\nstatic char *freeimage[] = {\n/* width height num_colors chars_per_pixel */\n\"",
		start_colors[] = "\",\n/* colors */\n\"",
		start_pixels[] = "\",\n/* pixels */\n\"",
		new_line[] = "\",\n\"",
		footer[] = "\"\n};\n",
		buf[256]; //256 is more then enough to sprintf 4 ints into, or the base-92 chars and #rrggbb line

		if( io->write_proc(header, (unsigned int)strlen(header), 1, handle) != 1 )
			return FALSE;

		int width = FreeImage_GetWidth(dib), height = FreeImage_GetHeight(dib), bpp = FreeImage_GetBPP(dib);
		RGBQUAD *pal = FreeImage_GetPalette(dib);
		int x,y;

		//map base92 chrs to the rgb value to create the palette
		std::map<DWORD,FILE_RGB> chrs2color;
		//map 8bpp index or 24bpp rgb value to the base92 chrs to create pixel data
		typedef union {
			DWORD index;
			FILE_RGBA rgba;
		} DWORDRGBA;
		std::map<DWORD,std::string> color2chrs;

		//loop thru entire dib, if new color, inc num_colors and add to both maps
		int num_colors = 0;
		for(y = 0; y < height; y++ ) {
			BYTE *line = FreeImage_GetScanLine(dib, height - y - 1);
			for(x = 0; x < width; x++ ) {
				FILE_RGB rgb;
				DWORDRGBA u;
				if( bpp > 8 ) {
					u.rgba.b = rgb.b = line[FI_RGBA_BLUE];
					u.rgba.g = rgb.g = line[FI_RGBA_GREEN];
					u.rgba.r = rgb.r = line[FI_RGBA_RED];
					u.rgba.a = 0;
					line += 3;
				} else {
					u.index = *line;
					rgb.b = pal[u.index].rgbBlue;
					rgb.g = pal[u.index].rgbGreen;
					rgb.r = pal[u.index].rgbRed;
					line++;
				}
				if( color2chrs.find(u.index) == color2chrs.end() ) { //new color
					std::string chrs(Base92(num_colors));
					color2chrs[u.index] = chrs;
					chrs2color[num_colors] = rgb;
					num_colors++;
				}
			}
		}

		int cpp = (int)(log((double)num_colors)/log(92.0)) + 1;

		sprintf(buf, "%d %d %d %d", FreeImage_GetWidth(dib), FreeImage_GetHeight(dib), num_colors, cpp );
		if( io->write_proc(buf, (unsigned int)strlen(buf), 1, handle) != 1 )
			return FALSE;

		if( io->write_proc(start_colors, (unsigned int)strlen(start_colors), 1, handle) != 1 )
			return FALSE;

		//write colors, using map of chrs->rgb
		for(x = 0; x < num_colors; x++ ) {
			sprintf(buf, "%*s c #%02x%02x%02x", cpp, Base92(x), chrs2color[x].r, chrs2color[x].g, chrs2color[x].b );
			if( io->write_proc(buf, (unsigned int)strlen(buf), 1, handle) != 1 )
				return FALSE;
			if( x == num_colors - 1 ) {
				if( io->write_proc(start_pixels, (unsigned int)strlen(start_pixels), 1, handle) != 1 )
					return FALSE;
			} else {
				if( io->write_proc(new_line, (unsigned int)strlen(new_line), 1, handle) != 1 )
					return FALSE;
			}
		}


		//write pixels, using map of rgb(if 24bpp) or index(if 8bpp)->chrs
		for(y = 0; y < height; y++ ) {
			BYTE *line = FreeImage_GetScanLine(dib, height - y - 1);
			for(x = 0; x < width; x++ ) {
				DWORDRGBA u;
				if( bpp > 8 ) {
					u.rgba.b = line[FI_RGBA_BLUE];
					u.rgba.g = line[FI_RGBA_GREEN];
					u.rgba.r = line[FI_RGBA_RED];
					u.rgba.a = 0;
					line += 3;
				} else {
					u.index = *line;
					line++;
				}
				sprintf(buf, "%*s", cpp, (char *)color2chrs[u.index].c_str());
				if( io->write_proc(buf, cpp, 1, handle) != 1 )
					return FALSE;
			}
			if( y == height - 1 ) {
				if( io->write_proc(footer, (unsigned int)strlen(footer), 1, handle) != 1 )
					return FALSE;
			} else {
				if( io->write_proc(new_line, (unsigned int)strlen(new_line), 1, handle) != 1 )
					return FALSE;
			}
		}

		return TRUE;
	} else {
		return FALSE;
	}
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitXPM(Plugin *plugin, int format_id)
{
    s_format_id = format_id;

	plugin->format_proc = Format;
	plugin->description_proc = Description;
	plugin->extension_proc = Extension;
	plugin->regexpr_proc = RegExpr;
	plugin->open_proc = NULL;
	plugin->close_proc = NULL;
	plugin->pagecount_proc = NULL;
	plugin->pagecapability_proc = NULL;
	plugin->load_proc = Load;
	plugin->save_proc = Save;
	plugin->validate_proc = Validate;
	plugin->mime_proc = MimeType;
	plugin->supports_export_bpp_proc = SupportsExportDepth;
	plugin->supports_export_type_proc = SupportsExportType;
	plugin->supports_icc_profiles_proc = NULL;
	plugin->supports_no_pixels_proc = SupportsNoPixels;
}

