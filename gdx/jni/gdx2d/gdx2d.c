/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
#include "gdx2d.h"
#define STB_TRUETYPE_IMPLEMENTATION
#define STBI_HEADER_FILE_ONLY
#define STBI_NO_FAILURE_STRINGS
#include "stb_image.c"
#include "stb_truetype.h"

static uint32_t gdx2d_blend = GDX2D_BLEND_NONE;
static uint32_t gdx2d_scale = GDX2D_SCALE_NEAREST;

typedef void(*set_pixel_func)(unsigned char* pixel_addr, uint32_t color);
typedef uint32_t(*get_pixel_func)(unsigned char* pixel_addr);

inline uint32_t to_format(uint32_t format, uint32_t color) {
	uint32_t r, g, b, a;

	switch(format) {
		case GDX2D_FORMAT_ALPHA: 
			return color & 0xff;
		case GDX2D_FORMAT_LUMINANCE_ALPHA: 
			return (color & 0xff00) >> 8 | (color & 0xff) << 8;
		case GDX2D_FORMAT_RGB888:
			return color & 0xffffff;
		case GDX2D_FORMAT_RGBA8888:
			return color;
		case GDX2D_FORMAT_RGB565: 
			r = (((color & 0xff000000) >> 27) << 11) & 0xf800;
			g = (((color & 0xff0000) >> 18) << 5) & 0x7e0;
			b = ((color & 0xff00) >> 11) & 0x1f;
			return r | g | b;
		case GDX2D_FORMAT_RGBA4444:
			r = (((color & 0xff000000) >> 28) << 12) & 0xf000;
			g = (((color & 0xff0000) >> 20) << 8) & 0xf00;
			b = ((color & 0xff00) >> 12) & 0xf0;
			a = ((color & 0xff) >> 4) & 0xf;
			return r | g | b | a;
		default: 
			return 0;
	}
}

inline uint32_t to_RGBA8888(uint32_t format, uint32_t color) {
	uint32_t r, g, b, a;

	switch(format) {
		case GDX2D_FORMAT_ALPHA: 
			return (color & 0xff) | 0xffffff00;
		case GDX2D_FORMAT_LUMINANCE_ALPHA: 
			return ((color & 0xff) << 24) | ((color & 0xff) << 16) | (color & 0xffff);
		case GDX2D_FORMAT_RGB888:
			return (color << 8) | 0x000000ff;
		case GDX2D_FORMAT_RGBA8888:
			return color;
		case GDX2D_FORMAT_RGB565: 
			r = (color & 0xf800) << 27;
			g = (color & 0x7e0) << 18;
			b = (color & 0x1f) << 11;
			return r | g | b | 0xff;
		case GDX2D_FORMAT_RGBA4444:
			r = (color & 0xf000) << 28;
			g = (color & 0xf00) << 20;
			b = (color & 0xf0) << 12;
			a = (color & 0xf) << 4;
			return r | g | b | a;
		default: 
			return 0;
	}
}

inline void set_pixel_alpha(unsigned char *pixel_addr, uint32_t color) {
	*pixel_addr = (unsigned char)(color & 0xff);
}

inline void set_pixel_luminance_alpha(unsigned char *pixel_addr, uint32_t color) {	
	*(unsigned short*)pixel_addr = (unsigned short)color;
}

inline void set_pixel_RGB888(unsigned char *pixel_addr, uint32_t color) {	
	//*(unsigned short*)pixel_addr = (unsigned short)(((color & 0xff0000) >> 16) | (color & 0xff00));
	pixel_addr[0] = (color & 0xff0000) >> 16;
	pixel_addr[1] = (color & 0xff00) >> 8;
	pixel_addr[2] = (color & 0xff);
}

inline void set_pixel_RGBA8888(unsigned char *pixel_addr, uint32_t color) {		
	if(!gdx2d_blend) {
		*(uint32_t*)pixel_addr = ((color & 0xff000000) >> 24) |
								((color & 0xff0000) >> 8) |
								((color & 0xff00) << 8) |
								((color & 0xff) << 24);
	} else {		
		uint32_t src_r = (color & 0xff000000) >> 24;
		uint32_t src_g = (color & 0xff0000) >> 16;
		uint32_t src_b = (color & 0xff00) >> 8;
		uint32_t src_a = (color & 0xff);
		
		uint32_t dst = *(uint32_t*)pixel_addr;		
		uint32_t dst_r = (dst & 0xff);
		uint32_t dst_g = (dst & 0xff00) >> 8;
		uint32_t dst_b = (dst & 0xff0000) >> 16;
		uint32_t dst_a = (dst & 0xff000000) >> 24;

		uint32_t src_one_minus_a = 255 - src_a;		
		dst_r = (src_a * src_r + src_one_minus_a * dst_r) / 255;
		dst_g = (src_a * src_g + src_one_minus_a * dst_g) / 255;
		dst_b = (src_a * src_b + src_one_minus_a * dst_b) / 255;
		if(dst_r > 255) dst_r = 255;
		if(dst_g > 255) dst_g = 255;
		if(dst_b > 255) dst_b = 255;
		*(uint32_t*)pixel_addr = (dst_r & 0xff) | ((dst_g & 0xff) << 8) | ((dst_b & 0xff) << 16) | ((src_a & 0xff) << 24);		
	}	
}

inline void set_pixel_RGB565(unsigned char *pixel_addr, uint32_t color) {
	*(uint16_t*)pixel_addr = (uint16_t)(color);
}

inline void set_pixel_RGBA4444(unsigned char *pixel_addr, uint32_t color) {
	if(!gdx2d_blend) {
		*(uint16_t*)pixel_addr = (uint16_t)(color);
	} else {
		uint16_t src_a = color & 0xf;
		uint16_t src_r = (color & 0xf000) >> 12;
		uint16_t src_g = (color & 0xf00) >> 8;
		uint16_t src_b = (color & 0xf0) >> 4;

		uint16_t dst = *(uint16_t*)pixel_addr;
		uint16_t dst_r = (dst & 0xf000) >> 12;
		uint16_t dst_g = (dst & 0xf00) >> 8;
		uint16_t dst_b = (dst & 0xf0) >> 4;

		uint16_t src_one_minus_a = 15 - src_a;
		dst_r = (src_a * src_r + src_one_minus_a * dst_r) / 15;
		dst_g = (src_a * src_g + src_one_minus_a * dst_g) / 15;
		dst_b = (src_a * src_b + src_one_minus_a * dst_b) / 15;
		if(dst_r > 15) dst_r = 15;
		if(dst_g > 15) dst_g = 15;
		if(dst_b > 15) dst_b = 15;
		*(uint16_t*)pixel_addr = (uint16_t)(((dst_r & 0xf) << 12) | ((dst_g & 0xf) << 8) | ((dst_b & 0xf) << 4) | (src_a & 0xf));		
	}
}

inline set_pixel_func set_pixel_func_ptr(uint32_t format) {
	switch(format) {
		case GDX2D_FORMAT_ALPHA:			return &set_pixel_alpha;
		case GDX2D_FORMAT_LUMINANCE_ALPHA:	return &set_pixel_luminance_alpha;
		case GDX2D_FORMAT_RGB888:			return &set_pixel_RGB888;
		case GDX2D_FORMAT_RGBA8888:			return &set_pixel_RGBA8888;
		case GDX2D_FORMAT_RGB565:			return &set_pixel_RGB565;
		case GDX2D_FORMAT_RGBA4444:			return &set_pixel_RGBA4444;
		default: return &set_pixel_alpha; // better idea for a default?
	}
}

inline uint32_t get_pixel_alpha(unsigned char *pixel_addr) {
	return *pixel_addr;
}

inline uint32_t get_pixel_luminance_alpha(unsigned char *pixel_addr) {
	return (((uint32_t)pixel_addr[0]) << 8) | pixel_addr[1];
}

inline uint32_t get_pixel_RGB888(unsigned char *pixel_addr) {
	return (((uint32_t)pixel_addr[0]) << 16) | (((uint32_t)pixel_addr[1]) << 8) | (pixel_addr[2]);
}

inline uint32_t get_pixel_RGBA8888(unsigned char *pixel_addr) {	
	return (((uint32_t)pixel_addr[0]) << 24) | (((uint32_t)pixel_addr[1]) << 16) | (((uint32_t)pixel_addr[2]) << 8) | pixel_addr[3];
}

inline uint32_t get_pixel_RGB565(unsigned char *pixel_addr) {
	return *(uint16_t*)pixel_addr;
}

inline uint32_t get_pixel_RGBA4444(unsigned char *pixel_addr) {
	return *(uint16_t*)pixel_addr;
}

inline get_pixel_func get_pixel_func_ptr(uint32_t format) {
	switch(format) {
		case GDX2D_FORMAT_ALPHA:			return &get_pixel_alpha;
		case GDX2D_FORMAT_LUMINANCE_ALPHA:	return &get_pixel_luminance_alpha;
		case GDX2D_FORMAT_RGB888:			return &get_pixel_RGB888;
		case GDX2D_FORMAT_RGBA8888:			return &get_pixel_RGBA8888;
		case GDX2D_FORMAT_RGB565:			return &get_pixel_RGB565;
		case GDX2D_FORMAT_RGBA4444:			return &get_pixel_RGBA4444;
		default: return &get_pixel_alpha; // better idea for a default?
	}
}

gdx2d_pixmap* gdx2d_load(const unsigned char *buffer, uint32_t len, uint32_t req_format) {
	int32_t width, height, format;
	// TODO fix this! Add conversion to requested format
	if(req_format > GDX2D_FORMAT_RGBA8888) 
		req_format = GDX2D_FORMAT_RGBA8888;
	const unsigned char* pixels = stbi_load_from_memory(buffer, len, &width, &height, &format, req_format);
	if(pixels == NULL)
		return NULL;

	gdx2d_pixmap* pixmap = (gdx2d_pixmap*)malloc(sizeof(gdx2d_pixmap));
	pixmap->width = (uint32_t)width;
	pixmap->height = (uint32_t)height;
	pixmap->format = (uint32_t)format;
	pixmap->pixels = pixels;
	return pixmap;
}

inline uint32_t bytes_per_pixel(uint32_t format) {
	switch(format) {
		case GDX2D_FORMAT_ALPHA:
			return 1;
		case GDX2D_FORMAT_LUMINANCE_ALPHA:
		case GDX2D_FORMAT_RGB565:
		case GDX2D_FORMAT_RGBA4444:
			return 2;
		case GDX2D_FORMAT_RGB888:
			return 3;
		case GDX2D_FORMAT_RGBA8888:
			return 4;
		default:
			return 4;
	}
}

gdx2d_pixmap* gdx2d_new(uint32_t width, uint32_t height, uint32_t format) {
	gdx2d_pixmap* pixmap = (gdx2d_pixmap*)malloc(sizeof(gdx2d_pixmap));
	pixmap->width = width;
	pixmap->height = height;
	pixmap->format = format;
	pixmap->pixels = (unsigned char*)malloc(width * height * bytes_per_pixel(format));
	return pixmap;
}
void gdx2d_free(const gdx2d_pixmap* pixmap) {
	free((void*)pixmap->pixels);
	free((void*)pixmap);
}

void gdx2d_set_blend (uint32_t blend) {
	gdx2d_blend = blend;
}

void gdx2d_set_scale (uint32_t scale) {
	gdx2d_scale = scale;
}

inline void clear_alpha(const gdx2d_pixmap* pixmap, uint32_t col) {
	int pixels = pixmap->width * pixmap->height;
	memset((void*)pixmap->pixels, col, pixels);
}

inline void clear_luminance_alpha(const gdx2d_pixmap* pixmap, uint32_t col) {
	int pixels = pixmap->width * pixmap->height;
	unsigned short* ptr = (unsigned short*)pixmap->pixels;
	unsigned short l = (col & 0xff) << 8 | (col >> 8);	

	for(; pixels > 0; pixels--) {
		*ptr = l;				
		ptr++;
	}
}

inline void clear_RGB888(const gdx2d_pixmap* pixmap, uint32_t col) {
	int pixels = pixmap->width * pixmap->height;
	unsigned char* ptr = (unsigned char*)pixmap->pixels;
	unsigned char r = (col & 0xff0000) >> 16;
	unsigned char g = (col & 0xff00) >> 8;
	unsigned char b = (col & 0xff);

	for(; pixels > 0; pixels--) {
		*ptr = r;
		ptr++;
		*ptr = g;
		ptr++;
		*ptr = b;
		ptr++;
	}
}

inline void clear_RGBA8888(const gdx2d_pixmap* pixmap, uint32_t col) {
	int pixels = pixmap->width * pixmap->height;
	uint32_t* ptr = (uint32_t*)pixmap->pixels;
	unsigned char r = (col & 0xff000000) >> 24;
	unsigned char g = (col & 0xff0000) >> 16;
	unsigned char b = (col & 0xff00) >> 8;
	unsigned char a = (col & 0xff);
	col = (a << 24) | (b << 16) | (g << 8) | r;

	for(; pixels > 0; pixels--) {
		*ptr = col;
		ptr++;		
	}
}

inline void clear_RGB565(const gdx2d_pixmap* pixmap, uint32_t col) {
	uint32_t pixels = pixmap->width * pixmap->height;
	uint32_t left = pixels % 2;
	pixels >>= 1;
	uint32_t* ptr = (uint32_t*)pixmap->pixels;
	uint32_t c = ((col & 0xffff) << 16) | (col & 0xffff);

	for(; pixels > 0; pixels--, ptr++) {
		*ptr = c;		
	}
	if(left) {
		*((uint16_t*)pixmap->pixels + pixmap->width * pixmap->height * 2) = (uint16_t)col;
	}
}

inline void clear_RGBA4444(const gdx2d_pixmap* pixmap, uint32_t col) {
	uint32_t pixels = pixmap->width * pixmap->height;
	uint32_t left = pixels % 2;
	pixels >>= 1;
	uint32_t* ptr = (uint32_t*)pixmap->pixels;
	uint32_t c = ((col & 0xffff) << 16) | (col & 0xffff);

	for(; pixels > 0; pixels--, ptr++) {
		*ptr = c;		
	}
	if(left) {
		*((uint16_t*)pixmap->pixels + pixmap->width * pixmap->height * 2) = (uint16_t)col;
	}
}

void gdx2d_clear(const gdx2d_pixmap* pixmap, uint32_t col) {	
	
	switch(pixmap->format) {
		case GDX2D_FORMAT_ALPHA:
			clear_alpha(pixmap, col);
			break;
		case GDX2D_FORMAT_LUMINANCE_ALPHA:
			clear_luminance_alpha(pixmap, col);
			break;
		case GDX2D_FORMAT_RGB888:
			clear_RGB888(pixmap, col);
			break;
		case GDX2D_FORMAT_RGBA8888:
			clear_RGBA8888(pixmap, col);
			break;
		case GDX2D_FORMAT_RGB565:
			clear_RGB565(pixmap, col);
			break;
		case GDX2D_FORMAT_RGBA4444:
			clear_RGBA4444(pixmap, col);
			break;
		default:
			break;
	}
}

inline int32_t in_pixmap(const gdx2d_pixmap* pixmap, int32_t x, int32_t y) {
	if(x < 0 || y < 0)
		return 0;
	if(x >= pixmap->width || y >= pixmap->height)
		return 0;
	return -1;
}

inline void set_pixel(unsigned char* pixels, uint32_t width, uint32_t height, uint32_t bpp, set_pixel_func pixel_func, int32_t x, int32_t y, uint32_t col) {
	if(x < 0 || y < 0) return;
	if(x >= width || y >= height) return;
	pixels = pixels + (x + height * y) * bpp;
	pixel_func(pixels, col);
}

void gdx2d_set_pixel(const gdx2d_pixmap* pixmap, int32_t x, int32_t y, uint32_t col) {
	set_pixel((unsigned char*)pixmap->pixels, pixmap->width, pixmap->height, bytes_per_pixel(pixmap->format), set_pixel_func_ptr(pixmap->format), x, y, col);
}

uint32_t gdx2d_get_pixel(const gdx2d_pixmap* pixmap, int32_t x, int32_t y) {
	if(!in_pixmap(pixmap, x, y)) 
		return 0;
	unsigned char* ptr = (unsigned char*)pixmap->pixels + (x + pixmap->height * y) * bytes_per_pixel(pixmap->format);
	return get_pixel_func_ptr(pixmap->format)(ptr);
}

void gdx2d_draw_line(const gdx2d_pixmap* pixmap, int32_t x0, int32_t y0, int32_t x1, int32_t y1, uint32_t col) {	
    int32_t dy = y1 - y0;
    int32_t dx = x1 - x0;
	int32_t fraction = 0;
    int32_t stepx, stepy;
	unsigned char* ptr = (unsigned char*)pixmap->pixels;
	uint32_t bpp = bytes_per_pixel(pixmap->format);
	set_pixel_func pixel_func = set_pixel_func_ptr(pixmap->format);

    if (dy < 0) { dy = -dy;  stepy = -1; } else { stepy = 1; }
    if (dx < 0) { dx = -dx;  stepx = -1; } else { stepx = 1; }
    dy <<= 1;
    dx <<= 1;    

	pixel_func(ptr + (x0 + y0) * bpp, col);    
    if (dx > dy) {
        fraction = dy - (dx >> 1);
        while (x0 != x1) {
            if (fraction >= 0) {
                y0 += stepy;
                fraction -= dx;
            }
            x0 += stepx;
            fraction += dy;
			if(in_pixmap(pixmap, x0, y0))
				pixel_func(ptr + (x0 + y0 * pixmap->width) * bpp, col);
        }
    } else {
		fraction = dx - (dy >> 1);
		while (y0 != y1) {
			if (fraction >= 0) {
				x0 += stepx;
				fraction -= dy;
			}
			y0 += stepy;
			fraction += dx;
			if(in_pixmap(pixmap, x0, y0))
				pixel_func(ptr + (x0 + y0 * pixmap->width) * bpp, col);
		}
	}
}

inline void hline(const gdx2d_pixmap* pixmap, int32_t x1, int32_t x2, int32_t y, uint32_t col) {
	int32_t tmp = 0;
	set_pixel_func pixel_func =set_pixel_func_ptr(pixmap->format);
	unsigned char* ptr = (unsigned char*)pixmap->pixels;
	uint32_t bpp = bytes_per_pixel(pixmap->format);

	if(y < 0 || y >= pixmap->height) return;

	if(x1 > x2) {
		tmp = x1;
		x1 = x2;
		x2 = tmp;
	}

	if(x1 >= pixmap->width) return;
	if(x2 < 0) return;

	if(x1 < 0) x1 = 0;
	if(x2 >= pixmap->width) x2 = pixmap->width - 1;	
	x2 += 1;
	
	ptr += (x1 + y * pixmap->width) * bpp;

	while(x1 != x2) {
		pixel_func(ptr, col);
		x1++;
		ptr += bpp;
	}
}

inline void vline(const gdx2d_pixmap* pixmap, int32_t y1, int32_t y2, int32_t x, uint32_t col) {
	int32_t tmp = 0;
	set_pixel_func pixel_func =set_pixel_func_ptr(pixmap->format);
	unsigned char* ptr = (unsigned char*)pixmap->pixels;
	uint32_t bpp = bytes_per_pixel(pixmap->format);
	uint32_t stride = bpp * pixmap->width;

	if(x < 0 || x >= pixmap->width) return;

	if(y1 > y2) {
		tmp = y1;
		y1 = y2;
		y2 = tmp;
	}

	if(y1 >= pixmap->height) return;
	if(y2 < 0) return;

	if(y1 < 0) y1 = 0;
	if(y2 >= pixmap->height) y2 = pixmap->height - 1;	
	y2 += 1;

	ptr += (x + y1 * pixmap->width) * bpp;

	while(y1 != y2) {
		pixel_func(ptr, col);
		y1++;
		ptr += stride;
	}
}

void gdx2d_draw_rect(const gdx2d_pixmap* pixmap, int32_t x, int32_t y, uint32_t width, uint32_t height, uint32_t col) {
	hline(pixmap, x, x + width - 1, y, col);
	hline(pixmap, x, x + width - 1, y + height - 1, col);
	vline(pixmap, y, y + height - 1, x, col);
	vline(pixmap, y, y + height - 1, x + width - 1, col);
}

inline void circle_points(unsigned char* pixels, uint32_t width, uint32_t height, uint32_t bpp, set_pixel_func pixel_func, int32_t cx, int32_t cy, int32_t x, int32_t y, uint32_t col) {	        
    if (x == 0) {
        set_pixel(pixels, width, height, bpp, pixel_func, cx, cy + y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx, cy - y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx + y, cy, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx - y, cy, col);
    } else 
    if (x == y) {
        set_pixel(pixels, width, height, bpp, pixel_func, cx + x, cy + y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx - x, cy + y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx + x, cy - y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx - x, cy - y, col);
    } else 
    if (x < y) {
        set_pixel(pixels, width, height, bpp, pixel_func, cx + x, cy + y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx - x, cy + y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx + x, cy - y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx - x, cy - y, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx + y, cy + x, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx - y, cy + x, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx + y, cy - x, col);
        set_pixel(pixels, width, height, bpp, pixel_func, cx - y, cy - x, col);
    }
}

void gdx2d_draw_circle(const gdx2d_pixmap* pixmap, int32_t x, int32_t y, uint32_t radius, uint32_t col) {	
    int32_t px = 0;
    int32_t py = radius;
    int32_t p = (5 - (int32_t)radius*4)/4;
	unsigned char* pixels = (unsigned char*)pixmap->pixels;
	uint32_t width = pixmap->width;
	uint32_t height = pixmap->height;
	uint32_t bpp = bytes_per_pixel(pixmap->format);
	set_pixel_func pixel_func = set_pixel_func_ptr(pixmap->format);

    circle_points(pixels, width, height, bpp, pixel_func, x, y, px, py, col);
    while (px < py) {
        px++;
        if (p < 0) {
            p += 2*px+1;
        } else {
            py--;
            p += 2*(px-py)+1;
        }
        circle_points(pixels, width, height, bpp, pixel_func, x, y, px, py, col);
    }
}

void gdx2d_fill_rect(const gdx2d_pixmap* pixmap, int32_t x, int32_t y, uint32_t width, uint32_t height, uint32_t col) {
	int32_t x2 = x + width - 1;
	int32_t y2 = y + height - 1;

	if(x >= pixmap->width) return;
	if(y >= pixmap->height) return;
	if(x2 < 0) return;
	if(y2 < 0) return;

	if(x < 0) x = 0;
	if(y < 0) y = 0;
	if(x2 >= pixmap->width) x2 = pixmap->width - 1;
	if(y2 >= pixmap->height) y2 = pixmap->height - 1;

	y2++;
	while(y!=y2) {
		hline(pixmap, x, x2, y, col);
		y++;
	}
}

void gdx2d_fill_circle(const gdx2d_pixmap* pixmap, int32_t x0, int32_t y0, uint32_t radius, uint32_t col) {
	int32_t f = 1 - (int32_t)radius;
	int32_t ddF_x = 1;
	int32_t ddF_y = -2 * (int32_t)radius;
	int32_t px = 0;
	int32_t py = (int32_t)radius;

	hline(pixmap, x0, x0, y0 + (int32_t)radius, col);
	hline(pixmap, x0, x0, y0 - (int32_t)radius, col);
	hline(pixmap, x0 - (int32_t)radius, x0 + (int32_t)radius, y0, col);	

	while(px < py)
	{	
		if(f >= 0) 
		{
			py--;
			ddF_y += 2;
			f += ddF_y;
		}
		px++;
		ddF_x += 2;
		f += ddF_x;    
		hline(pixmap, x0 - px, x0 + px, y0 + py, col);		
		hline(pixmap, x0 - px, x0 + px, y0 - py, col);
		hline(pixmap, x0 - py, x0 + py, y0 + px, col);		
		hline(pixmap, x0 - py, x0 + py, y0 - px, col);		
	}
}

void blit_same_format_and_size_blend(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap, 
						 			 int32_t src_x, int32_t src_y, 
									 int32_t dst_x, int32_t dst_y, 
									 uint32_t width, uint32_t height) {
}

void blit_same_format_and_size(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap, 
						 	   int32_t src_x, int32_t src_y, 
							   int32_t dst_x, int32_t dst_y, 
							   uint32_t width, uint32_t height) {
}

void blit_same_size(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap, 
					int32_t src_x, int32_t src_y, 
					int32_t dst_x, int32_t dst_y, 
					uint32_t width, uint32_t height) {
}

void blit_same_size_blend(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap, 
						  int32_t src_x, int32_t src_y, 
						  int32_t dst_x, int32_t dst_y, 
						  uint32_t width, uint32_t height) {
}

void gdx2d_draw_pixmap(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap,
					   int32_t src_x, int32_t src_y, uint32_t src_width, uint32_t src_height,
					   int32_t dst_x, int32_t dst_y, uint32_t dst_width, uint32_t dst_height) {

}
