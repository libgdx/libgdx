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
#include <stdlib.h>
#define STB_IMAGE_IMPLEMENTATION
#define STBI_FAILURE_USERMSG
#define STBI_NO_STDIO
#include "stb_image.h"


static uint32_t* lu4 = 0;
static uint32_t* lu5 = 0;
static uint32_t* lu6 = 0;

typedef void(*set_pixel_func)(unsigned char* pixel_addr, uint32_t color);
typedef uint32_t(*get_pixel_func)(unsigned char* pixel_addr);

static inline void generate_look_ups() {
	uint32_t i = 0;
	lu4 = malloc(sizeof(uint32_t) * 16);
	lu5 = malloc(sizeof(uint32_t) * 32);
	lu6 = malloc(sizeof(uint32_t) * 64);

	for(i = 0; i < 16; i++) {
		lu4[i] = (uint32_t) i / 15.0f * 255;
		lu5[i] = (uint32_t) i / 31.0f * 255;
		lu6[i] = (uint32_t) i / 63.0f * 255;
	}

	for(i = 16; i < 32; i++) {
		lu5[i] = (uint32_t) i / 31.0f * 255;
		lu6[i] = (uint32_t) i / 63.0f * 255;
	}

	for(i = 32; i < 64; i++) {
		lu6[i] = (uint32_t) i / 63.0f * 255;
	}
}

static inline uint32_t to_format(uint32_t format, uint32_t color) {
	uint32_t r, g, b, a, l;

	switch(format) {
		case GDX2D_FORMAT_ALPHA:
			return color & 0xff;
		case GDX2D_FORMAT_LUMINANCE_ALPHA:
			r = (color & 0xff000000) >> 24;
			g = (color & 0xff0000) >> 16;
			b = (color & 0xff00) >> 8;
			a = (color & 0xff);
			l = ((uint32_t)(0.2126f * r + 0.7152 * g + 0.0722 * b) & 0xff) << 8;
			return (l & 0xffffff00) | a;
		case GDX2D_FORMAT_RGB888:
			return color >> 8;
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
			b = (((color & 0xff00) >> 12) << 4) & 0xf0;
			a = ((color & 0xff) >> 4) & 0xf;
			return r | g | b | a;
		default:
			return 0;
	}
}

#define min(a, b) (a > b?b:a)

static inline uint32_t weight_RGBA8888(uint32_t color, float weight) {
	uint32_t r, g, b, a;
	r = min((uint32_t)(((color & 0xff000000) >> 24) * weight), 255);
	g = min((uint32_t)(((color & 0xff0000) >> 16) * weight), 255);
	b = min((uint32_t)(((color & 0xff00) >> 8) * weight), 255);
	a = min((uint32_t)(((color & 0xff)) * weight), 255);

	return (r << 24) | (g << 16) | (b << 8) | a;
}

static inline uint32_t to_RGBA8888(uint32_t format, uint32_t color) {
	uint32_t r, g, b, a;

	if(!lu5) generate_look_ups();

	switch(format) {
		case GDX2D_FORMAT_ALPHA:
			return (color & 0xff) | 0xffffff00;
		case GDX2D_FORMAT_LUMINANCE_ALPHA:
			return ((color & 0xff00) << 16) | ((color & 0xff00) << 8) | (color & 0xffff);
		case GDX2D_FORMAT_RGB888:
			return (color << 8) | 0x000000ff;
		case GDX2D_FORMAT_RGBA8888:
			return color;
		case GDX2D_FORMAT_RGB565:
			r = lu5[(color & 0xf800) >> 11] << 24;
			g = lu6[(color & 0x7e0) >> 5] << 16;
			b = lu5[(color & 0x1f)] << 8;
			return r | g | b | 0xff;
		case GDX2D_FORMAT_RGBA4444:
			r = lu4[(color & 0xf000) >> 12] << 24;
			g = lu4[(color & 0xf00) >> 8] << 16;
			b = lu4[(color & 0xf0) >> 4] << 8;
			a = lu4[(color & 0xf)];
			return r | g | b | a;
		default:
			return 0;
	}
}

static inline void set_pixel_alpha(unsigned char *pixel_addr, uint32_t color) {
	*pixel_addr = (unsigned char)(color & 0xff);
}

static inline void set_pixel_luminance_alpha(unsigned char *pixel_addr, uint32_t color) {
	*(unsigned short*)pixel_addr = (unsigned short)color;
}

static inline void set_pixel_RGB888(unsigned char *pixel_addr, uint32_t color) {
	//*(unsigned short*)pixel_addr = (unsigned short)(((color & 0xff0000) >> 16) | (color & 0xff00));
	pixel_addr[0] = (color & 0xff0000) >> 16;
	pixel_addr[1] = (color & 0xff00) >> 8;
	pixel_addr[2] = (color & 0xff);
}

static inline void set_pixel_RGBA8888(unsigned char *pixel_addr, uint32_t color) {
	*(uint32_t*)pixel_addr = ((color & 0xff000000) >> 24) |
							((color & 0xff0000) >> 8) |
							((color & 0xff00) << 8) |
							((color & 0xff) << 24);
}

static inline void set_pixel_RGB565(unsigned char *pixel_addr, uint32_t color) {
	*(uint16_t*)pixel_addr = (uint16_t)(color);
}

static inline void set_pixel_RGBA4444(unsigned char *pixel_addr, uint32_t color) {
	*(uint16_t*)pixel_addr = (uint16_t)(color);
}

static inline set_pixel_func set_pixel_func_ptr(uint32_t format) {
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

static inline uint32_t blend(uint32_t src, uint32_t dst) {
	uint32_t src_a = src & 0xff;
	if (src_a == 0) return dst;
	uint32_t src_b = (src >> 8) & 0xff;
	uint32_t src_g = (src >> 16) & 0xff;
	uint32_t src_r = (src >> 24) & 0xff;

	uint32_t dst_a = dst & 0xff;
	uint32_t dst_b = (dst >> 8) & 0xff;
	uint32_t dst_g = (dst >> 16) & 0xff;
	uint32_t dst_r = (dst >> 24) & 0xff;

	dst_a -= (dst_a * src_a) / 255;
	uint32_t a = dst_a + src_a;
	dst_r = (dst_r * dst_a + src_r * src_a) / a;
	dst_g = (dst_g * dst_a + src_g * src_a) / a;
	dst_b = (dst_b * dst_a + src_b * src_a) / a;
	return (uint32_t)((dst_r << 24) | (dst_g << 16) | (dst_b << 8) | a);
}

static inline uint32_t get_pixel_alpha(unsigned char *pixel_addr) {
	return *pixel_addr;
}

static inline uint32_t get_pixel_luminance_alpha(unsigned char *pixel_addr) {
	return (((uint32_t)pixel_addr[0]) << 8) | pixel_addr[1];
}

static inline uint32_t get_pixel_RGB888(unsigned char *pixel_addr) {
	return (((uint32_t)pixel_addr[0]) << 16) | (((uint32_t)pixel_addr[1]) << 8) | (pixel_addr[2]);
}

static inline uint32_t get_pixel_RGBA8888(unsigned char *pixel_addr) {
	return (((uint32_t)pixel_addr[0]) << 24) | (((uint32_t)pixel_addr[1]) << 16) | (((uint32_t)pixel_addr[2]) << 8) | pixel_addr[3];
}

static inline uint32_t get_pixel_RGB565(unsigned char *pixel_addr) {
	return *(uint16_t*)pixel_addr;
}

static inline uint32_t get_pixel_RGBA4444(unsigned char *pixel_addr) {
	return *(uint16_t*)pixel_addr;
}

static inline get_pixel_func get_pixel_func_ptr(uint32_t format) {
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

gdx2d_pixmap* gdx2d_load(const unsigned char *buffer, uint32_t len) {
	int32_t width, height, format;

	const unsigned char* pixels = stbi_load_from_memory(buffer, len, &width, &height, &format, 0);
	if (pixels == NULL)
		return NULL;

	gdx2d_pixmap* pixmap = (gdx2d_pixmap*)malloc(sizeof(gdx2d_pixmap));
	if (!pixmap) return 0;
	pixmap->width = (uint32_t)width;
	pixmap->height = (uint32_t)height;
	pixmap->format = (uint32_t)format;
	pixmap->blend = GDX2D_BLEND_SRC_OVER;
	pixmap->scale = GDX2D_SCALE_BILINEAR;
	pixmap->pixels = pixels;
	return pixmap;
}

uint32_t gdx2d_bytes_per_pixel(uint32_t format) {
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
	if (!pixmap) return 0;
	pixmap->width = width;
	pixmap->height = height;
	pixmap->format = format;
	pixmap->blend = GDX2D_BLEND_SRC_OVER;
	pixmap->scale = GDX2D_SCALE_BILINEAR;
	pixmap->pixels = (unsigned char*)malloc(width * height * gdx2d_bytes_per_pixel(format));
	if (!pixmap->pixels) {
		free((void*)pixmap);
		return 0;
	}
	return pixmap;
}
void gdx2d_free(const gdx2d_pixmap* pixmap) {
	free((void*)pixmap->pixels);
	free((void*)pixmap);
}

void gdx2d_set_blend (gdx2d_pixmap* pixmap, uint32_t blend) {
    pixmap->blend = blend;
}

void gdx2d_set_scale (gdx2d_pixmap* pixmap, uint32_t scale) {
	pixmap->scale = scale;
}

const char *gdx2d_get_failure_reason(void) {
	return stbi_failure_reason();
}

static inline void clear_alpha(const gdx2d_pixmap* pixmap, uint32_t col) {
	int pixels = pixmap->width * pixmap->height;
	memset((void*)pixmap->pixels, col, pixels);
}

static inline void clear_luminance_alpha(const gdx2d_pixmap* pixmap, uint32_t col) {
	int pixels = pixmap->width * pixmap->height;
	unsigned short* ptr = (unsigned short*)pixmap->pixels;
	unsigned short l = (col & 0xff) << 8 | (col >> 8);

	for(; pixels > 0; pixels--) {
		*ptr = l;
		ptr++;
	}
}

static inline void clear_RGB888(const gdx2d_pixmap* pixmap, uint32_t col) {
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

static inline void clear_RGBA8888(const gdx2d_pixmap* pixmap, uint32_t col) {
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

static inline void clear_RGB565(const gdx2d_pixmap* pixmap, uint32_t col) {
	int pixels = pixmap->width * pixmap->height;
	unsigned short* ptr = (unsigned short*)pixmap->pixels;
	unsigned short l = col & 0xffff;

	for(; pixels > 0; pixels--) {
		*ptr = l;
		ptr++;
	}
}

static inline void clear_RGBA4444(const gdx2d_pixmap* pixmap, uint32_t col) {
	int pixels = pixmap->width * pixmap->height;
	unsigned short* ptr = (unsigned short*)pixmap->pixels;
	unsigned short l = col & 0xffff;

	for(; pixels > 0; pixels--) {
		*ptr = l;
		ptr++;
	}
}

void gdx2d_clear(const gdx2d_pixmap* pixmap, uint32_t col) {
	col = to_format(pixmap->format, col);

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

static inline int32_t in_pixmap(const gdx2d_pixmap* pixmap, int32_t x, int32_t y) {
	if(x < 0 || y < 0)
		return 0;
	if(x >= pixmap->width || y >= pixmap->height)
		return 0;
	return -1;
}

static inline void set_pixel(unsigned char* pixels, uint32_t width, uint32_t height, uint32_t bpp, set_pixel_func pixel_func, int32_t x, int32_t y, uint32_t col) {
	if(x < 0 || y < 0) return;
	if(x >= (int32_t)width || y >= (int32_t)height) return;
	pixels = pixels + (x + width * y) * bpp;
	pixel_func(pixels, col);
}

uint32_t gdx2d_get_pixel(const gdx2d_pixmap* pixmap, int32_t x, int32_t y) {
	if(!in_pixmap(pixmap, x, y))
		return 0;
	unsigned char* ptr = (unsigned char*)pixmap->pixels + (x + pixmap->width * y) * gdx2d_bytes_per_pixel(pixmap->format);
	return to_RGBA8888(pixmap->format, get_pixel_func_ptr(pixmap->format)(ptr));
}

void gdx2d_set_pixel(const gdx2d_pixmap* pixmap, int32_t x, int32_t y, uint32_t col) {
	if(pixmap->blend) {
		uint32_t dst = gdx2d_get_pixel(pixmap, x, y);
		col = blend(col, dst);
		col = to_format(pixmap->format, col);
		set_pixel((unsigned char*)pixmap->pixels, pixmap->width, pixmap->height, gdx2d_bytes_per_pixel(pixmap->format), set_pixel_func_ptr(pixmap->format), x, y, col);
	} else {
		col = to_format(pixmap->format, col);
		set_pixel((unsigned char*)pixmap->pixels, pixmap->width, pixmap->height, gdx2d_bytes_per_pixel(pixmap->format), set_pixel_func_ptr(pixmap->format), x, y, col);
	}
}

void gdx2d_draw_line(const gdx2d_pixmap* pixmap, int32_t x0, int32_t y0, int32_t x1, int32_t y1, uint32_t col) {
    int32_t dy = y1 - y0;
    int32_t dx = x1 - x0;
	int32_t fraction = 0;
    int32_t stepx, stepy;
	unsigned char* ptr = (unsigned char*)pixmap->pixels;
	uint32_t bpp = gdx2d_bytes_per_pixel(pixmap->format);
	set_pixel_func pset = set_pixel_func_ptr(pixmap->format);
	get_pixel_func pget = get_pixel_func_ptr(pixmap->format);
	uint32_t col_format = to_format(pixmap->format, col);
	void* addr = ptr + (x0 + y0 * pixmap->width) * bpp;

    if (dy < 0) { dy = -dy;  stepy = -1; } else { stepy = 1; }
    if (dx < 0) { dx = -dx;  stepx = -1; } else { stepx = 1; }
    dy <<= 1;
    dx <<= 1;

    if(in_pixmap(pixmap, x0, y0)) {
    	if(pixmap->blend) {
    		col_format = to_format(pixmap->format, blend(col, to_RGBA8888(pixmap->format, pget(addr))));
    	}
    	pset(addr, col_format);
    }
    if (dx > dy) {
        fraction = dy - (dx >> 1);
        while (x0 != x1) {
            if (fraction >= 0) {
                y0 += stepy;
                fraction -= dx;
            }
            x0 += stepx;
            fraction += dy;
			if(in_pixmap(pixmap, x0, y0)) {
				addr = ptr + (x0 + y0 * pixmap->width) * bpp;
				if(pixmap->blend) {
					col_format = to_format(pixmap->format, blend(col, to_RGBA8888(pixmap->format, pget(addr))));
				}
				pset(addr, col_format);
			}
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
			if(in_pixmap(pixmap, x0, y0)) {
				addr = ptr + (x0 + y0 * pixmap->width) * bpp;
				if(pixmap->blend) {
					col_format = to_format(pixmap->format, blend(col, to_RGBA8888(pixmap->format, pget(addr))));
				}
				pset(addr, col_format);
			}
		}
	}
}

static inline void hline(const gdx2d_pixmap* pixmap, int32_t x1, int32_t x2, int32_t y, uint32_t col) {
	int32_t tmp = 0;
	set_pixel_func pset = set_pixel_func_ptr(pixmap->format);
	get_pixel_func pget = get_pixel_func_ptr(pixmap->format);
	unsigned char* ptr = (unsigned char*)pixmap->pixels;
	uint32_t bpp = gdx2d_bytes_per_pixel(pixmap->format);
	uint32_t col_format = to_format(pixmap->format, col);

	if(y < 0 || y >= (int32_t)pixmap->height) return;

	if(x1 > x2) {
		tmp = x1;
		x1 = x2;
		x2 = tmp;
	}

	if(x1 >= (int32_t)pixmap->width) return;
	if(x2 < 0)  return;

	if(x1 < 0) x1 = 0;
	if(x2 >= (int32_t)pixmap->width) x2 = pixmap->width - 1;
	x2 += 1;

	ptr += (x1 + y * pixmap->width) * bpp;

	while(x1 != x2) {
		if(pixmap->blend) {
			col_format = to_format(pixmap->format, blend(col, to_RGBA8888(pixmap->format, pget(ptr))));
		}
		pset(ptr, col_format);
		x1++;
		ptr += bpp;
	}
}

static inline void vline(const gdx2d_pixmap* pixmap, int32_t y1, int32_t y2, int32_t x, uint32_t col) {
	int32_t tmp = 0;
	set_pixel_func pset = set_pixel_func_ptr(pixmap->format);
	get_pixel_func pget = get_pixel_func_ptr(pixmap->format);
	unsigned char* ptr = (unsigned char*)pixmap->pixels;
	uint32_t bpp = gdx2d_bytes_per_pixel(pixmap->format);
	uint32_t stride = bpp * pixmap->width;
	uint32_t col_format = to_format(pixmap->format, col);

	if(x < 0 || x >= pixmap->width) return;

	if(y1 > y2) {
		tmp = y1;
		y1 = y2;
		y2 = tmp;
	}

	if(y1 >= (int32_t)pixmap->height) return;
	if(y2 < 0) return;

	if(y1 < 0) y1 = 0;
	if(y2 >= (int32_t)pixmap->height) y2 = pixmap->height - 1;
	y2 += 1;

	ptr += (x + y1 * pixmap->width) * bpp;

	while(y1 != y2) {
		if(pixmap->blend) {
			col_format = to_format(pixmap->format, blend(col, to_RGBA8888(pixmap->format, pget(ptr))));
		}
		pset(ptr, col_format);
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

static inline void circle_points(unsigned char* pixels, uint32_t width, uint32_t height, uint32_t bpp, set_pixel_func pixel_func, int32_t cx, int32_t cy, int32_t x, int32_t y, uint32_t col) {
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
	uint32_t bpp = gdx2d_bytes_per_pixel(pixmap->format);
	set_pixel_func pixel_func = set_pixel_func_ptr(pixmap->format);
	col = to_format(pixmap->format, col);

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

	if(x >= (int32_t)pixmap->width) return;
	if(y >= (int32_t)pixmap->height) return;
	if(x2 < 0) return;
	if(y2 < 0) return;

	if(x < 0) x = 0;
	if(y < 0) y = 0;
	if(x2 >= (int32_t)pixmap->width) x2 = pixmap->width - 1;
	if(y2 >= (int32_t)pixmap->height) y2 = pixmap->height - 1;

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

#define max(a, b) (a < b?b:a)

#define EDGE_ASSIGN(edge,_x1,_y1,_x2,_y2) \
  { if (_y2 > _y1) { edge.y1 = _y1; edge.y2 = _y2; edge.x1 = _x1; edge.x2 = _x2; } \
    else { edge.y2 = _y1; edge.y1 = _y2; edge.x2 = _x1; edge.x1 = _x2; } }

void gdx2d_fill_triangle(const gdx2d_pixmap* pixmap, int32_t x1, int32_t y1, int32_t x2, int32_t y2, int32_t x3, int32_t y3, uint32_t col) {

	// this structure is used to sort edges according to y-component.
	struct edge {
		int32_t x1;
		int32_t y1;
		int32_t x2;
		int32_t y2;
	};
	struct edge edges[3], edge_tmp;
	float slope0, slope1, slope2;
	int32_t edge0_len, edge1_len, edge2_len, edge_len_tmp;
	int32_t y, bound_y1, bound_y2, calc_x1, calc_x2;

	// do nothing when points are colinear -- we draw the fill not the line.
	if ((x2 - x1) * (y3 - y1) == (x3 - x1) * (y2 - y1)) {
		return;
	}

	// asign input vertices into internally-sorted edge structures.
	EDGE_ASSIGN(edges[0], x1, y1, x2, y2);
	EDGE_ASSIGN(edges[1], x1, y1, x3, y3);
	EDGE_ASSIGN(edges[2], x2, y2, x3, y3);

	// order edges according to descending length.
	edge0_len = edges[0].y2 - edges[0].y1;
	edge1_len = edges[1].y2 - edges[1].y1;
	edge2_len = edges[2].y2 - edges[2].y1;

	if (edge1_len >= edge0_len && edge1_len >= edge2_len) {
		// swap edge0 and edge1 with respective lengths.
		edge_tmp = edges[0];
		edges[0] = edges[1];
		edges[1] = edge_tmp;
		edge_len_tmp = edge0_len;
		edge0_len = edge1_len;
		edge1_len = edge_len_tmp;
	} else if (edge2_len >= edge0_len && edge2_len >= edge1_len) {
		// swap edge0 and edge2 with respective lengths.
		edge_tmp = edges[0];
		edges[0] = edges[2];
		edges[2] = edge_tmp;
		edge_len_tmp = edge0_len;
		edge0_len = edge2_len;
		edge2_len = edge_len_tmp;
	}

	if (edge2_len > edge1_len) {
		// swap edge1 and edge2 - edge len no longer necessary.
		edge_tmp = edges[1];
		edges[1] = edges[2];
		edges[2] = edge_tmp;
	}

	// y-component of the two longest y-component edges is provably > 0.

	slope0 = ((float) (edges[0].x1 - edges[0].x2)) /
		((float) (edges[0].y2 - edges[0].y1));
	slope1 = ((float) (edges[1].x1 - edges[1].x2)) /
		((float) (edges[1].y2 - edges[1].y1));

	// avoid iterating on y values out of bounds.
	bound_y1 = max(edges[1].y1, 0);
	bound_y2 = min(edges[1].y2, pixmap->height-1);

	for ( y=bound_y1; y <= bound_y2; y++ ) {

		// calculate the x values for this y value.
		calc_x1 = (int32_t) ((float) edges[0].x2 +
			slope0 * (float) (edges[0].y2 - y) + 0.5);
		calc_x2 = (int32_t) ((float) edges[1].x2 +
			slope1 * (float) (edges[1].y2 - y) + 0.5);

		// do not duplicate hline() swap and boundary checking.
		hline(pixmap, calc_x1, calc_x2, y, col);
	}

	// if there are still values of y which remain, keep calculating.

	if (edges[2].y2 - edges[2].y1 > 0) {

		slope2 = ((float) (edges[2].x1 - edges[2].x2)) /
			((float) (edges[2].y2 - edges[2].y1));

		bound_y1 = max(edges[2].y1, 0);
		bound_y2 = min(edges[2].y2, pixmap->height-1);

		for ( y=bound_y1; y <= bound_y2; y++ ) {

			calc_x1 = (int32_t) ((float) edges[0].x2 +
				slope0 * (float) (edges[0].y2 - y) + 0.5);
			calc_x2 = (int32_t) ((float) edges[2].x2 +
				slope2 * (float) (edges[2].y2 - y) + 0.5);

			hline(pixmap, calc_x1, calc_x2, y, col);
		}
	}

	return;
}

static inline void blit_same_size(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap,
						 			 int32_t src_x, int32_t src_y,
									 int32_t dst_x, int32_t dst_y,
									 uint32_t width, uint32_t height) {
	set_pixel_func pset = set_pixel_func_ptr(dst_pixmap->format);
	get_pixel_func pget = get_pixel_func_ptr(src_pixmap->format);
	get_pixel_func dpget = get_pixel_func_ptr(dst_pixmap->format);
	uint32_t sbpp = gdx2d_bytes_per_pixel(src_pixmap->format);
	uint32_t dbpp = gdx2d_bytes_per_pixel(dst_pixmap->format);
	uint32_t spitch = sbpp * src_pixmap->width;
	uint32_t dpitch = dbpp * dst_pixmap->width;

	int sx = src_x;
	int sy = src_y;
	int dx = dst_x;
	int dy = dst_y;

	for(;sy < src_y + height; sy++, dy++) {
		if(sy < 0 || dy < 0) continue;
		if(sy >= src_pixmap->height || dy >= dst_pixmap->height) break;

		for(sx = src_x, dx = dst_x; sx < src_x + width; sx++, dx++) {
			if(sx < 0 || dx < 0) continue;
			if(sx >= src_pixmap->width || dx >= dst_pixmap->width) break;

			const void* src_ptr = src_pixmap->pixels + sx * sbpp + sy * spitch;
			const void* dst_ptr = dst_pixmap->pixels + dx * dbpp + dy * dpitch;
			uint32_t src_col = to_RGBA8888(src_pixmap->format, pget((void*)src_ptr));

			if(dst_pixmap->blend) {
				uint32_t dst_col = to_RGBA8888(dst_pixmap->format, dpget((void*)dst_ptr));
				src_col = to_format(dst_pixmap->format, blend(src_col, dst_col));
			} else {
				src_col = to_format(dst_pixmap->format, src_col);
			}

			pset((void*)dst_ptr, src_col);
		}
	}
}

static inline void blit_bilinear(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap,
		   int32_t src_x, int32_t src_y, uint32_t src_width, uint32_t src_height,
		   int32_t dst_x, int32_t dst_y, uint32_t dst_width, uint32_t dst_height) {
	set_pixel_func pset = set_pixel_func_ptr(dst_pixmap->format);
	get_pixel_func pget = get_pixel_func_ptr(src_pixmap->format);
	get_pixel_func dpget = get_pixel_func_ptr(dst_pixmap->format);
	uint32_t sbpp = gdx2d_bytes_per_pixel(src_pixmap->format);
	uint32_t dbpp = gdx2d_bytes_per_pixel(dst_pixmap->format);
	uint32_t spitch = sbpp * src_pixmap->width;
	uint32_t dpitch = dbpp * dst_pixmap->width;

	float x_ratio = ((float)src_width - 1)/ dst_width;
	float y_ratio = ((float)src_height - 1) / dst_height;
	float x_diff = 0;
	float y_diff = 0;

	int dx = dst_x;
	int dy = dst_y;
	int sx = src_x;
	int sy = src_y;
	int i = 0;
	int j = 0;

	for(;i < dst_height; i++) {
		sy = (int)(i * y_ratio) + src_y;
		dy = i + dst_y;
		y_diff = (y_ratio * i + src_y) - sy;
		if(sy < 0 || dy < 0) continue;
		if(sy >= src_pixmap->height || dy >= dst_pixmap->height) break;

		for(j = 0 ;j < dst_width; j++) {
			sx = (int)(j * x_ratio) + src_x;
			dx = j + dst_x;
			x_diff = (x_ratio * j + src_x) - sx;
			if(sx < 0 || dx < 0) continue;
			if(sx >= src_pixmap->width || dx >= dst_pixmap->width) break;

			const void* dst_ptr = dst_pixmap->pixels + dx * dbpp + dy * dpitch;
			const void* src_ptr = src_pixmap->pixels + sx * sbpp + sy * spitch;
			uint32_t c1 = 0, c2 = 0, c3 = 0, c4 = 0;
			c1 = to_RGBA8888(src_pixmap->format, pget((void*)src_ptr));
			if(sx + 1 < src_width) c2 = to_RGBA8888(src_pixmap->format, pget((void*)(src_ptr + sbpp))); else c2 = c1;
			if(sy + 1< src_height) c3 = to_RGBA8888(src_pixmap->format, pget((void*)(src_ptr + spitch))); else c3 = c1;
			if(sx + 1< src_width && sy + 1 < src_height) c4 = to_RGBA8888(src_pixmap->format, pget((void*)(src_ptr + spitch + sbpp))); else c4 = c1;

			float ta = (1 - x_diff) * (1 - y_diff);
			float tb = (x_diff) * (1 - y_diff);
			float tc = (1 - x_diff) * (y_diff);
			float td = (x_diff) * (y_diff);

			uint32_t r = (uint32_t)(((c1 & 0xff000000) >> 24) * ta +
									((c2 & 0xff000000) >> 24) * tb +
									((c3 & 0xff000000) >> 24) * tc +
									((c4 & 0xff000000) >> 24) * td) & 0xff;
			uint32_t g = (uint32_t)(((c1 & 0xff0000) >> 16) * ta +
									((c2 & 0xff0000) >> 16) * tb +
									((c3 & 0xff0000) >> 16) * tc +
									((c4 & 0xff0000) >> 16) * td) & 0xff;
			uint32_t b = (uint32_t)(((c1 & 0xff00) >> 8) * ta +
									((c2 & 0xff00) >> 8) * tb +
									((c3 & 0xff00) >> 8) * tc +
									((c4 & 0xff00) >> 8) * td) & 0xff;
			uint32_t a = (uint32_t)((c1 & 0xff) * ta +
									(c2 & 0xff) * tb +
									(c3 & 0xff) * tc +
									(c4 & 0xff) * td) & 0xff;

			uint32_t src_col = (r << 24) | (g << 16) | (b << 8) | a;

			if(dst_pixmap->blend) {
				uint32_t dst_col = to_RGBA8888(dst_pixmap->format, dpget((void*)dst_ptr));
				src_col = to_format(dst_pixmap->format, blend(src_col, dst_col));
			} else {
				src_col = to_format(dst_pixmap->format, src_col);
			}

			pset((void*)dst_ptr, src_col);
		}
	}
}

static inline void blit_linear(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap,
		   int32_t src_x, int32_t src_y, uint32_t src_width, uint32_t src_height,
		   int32_t dst_x, int32_t dst_y, uint32_t dst_width, uint32_t dst_height) {
	set_pixel_func pset = set_pixel_func_ptr(dst_pixmap->format);
	get_pixel_func pget = get_pixel_func_ptr(src_pixmap->format);
	get_pixel_func dpget = get_pixel_func_ptr(dst_pixmap->format);
	uint32_t sbpp = gdx2d_bytes_per_pixel(src_pixmap->format);
	uint32_t dbpp = gdx2d_bytes_per_pixel(dst_pixmap->format);
	uint32_t spitch = sbpp * src_pixmap->width;
	uint32_t dpitch = dbpp * dst_pixmap->width;

	uint32_t x_ratio = (src_width << 16) / dst_width + 1;
	uint32_t y_ratio = (src_height << 16) / dst_height + 1;

	int dx = dst_x;
	int dy = dst_y;
	int sx = src_x;
	int sy = src_y;
	int i = 0;
	int j = 0;

	for(;i < dst_height; i++) {
		sy = ((i * y_ratio) >> 16) + src_y;
		dy = i + dst_y;
		if(sy < 0 || dy < 0) continue;
		if(sy >= src_pixmap->height || dy >= dst_pixmap->height) break;

		for(j = 0 ;j < dst_width; j++) {
			sx = ((j * x_ratio) >> 16) + src_x;
			dx = j + dst_x;
			if(sx < 0 || dx < 0) continue;
			if(sx >= src_pixmap->width || dx >= dst_pixmap->width) break;

			const void* src_ptr = src_pixmap->pixels + sx * sbpp + sy * spitch;
			const void* dst_ptr = dst_pixmap->pixels + dx * dbpp + dy * dpitch;
			uint32_t src_col = to_RGBA8888(src_pixmap->format, pget((void*)src_ptr));

			if(dst_pixmap->blend) {
				uint32_t dst_col = to_RGBA8888(dst_pixmap->format, dpget((void*)dst_ptr));
				src_col = to_format(dst_pixmap->format, blend(src_col, dst_col));
			} else {
				src_col = to_format(dst_pixmap->format, src_col);
			}

			pset((void*)dst_ptr, src_col);
		}
	}
}

static inline void blit(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap,
					   int32_t src_x, int32_t src_y, uint32_t src_width, uint32_t src_height,
					   int32_t dst_x, int32_t dst_y, uint32_t dst_width, uint32_t dst_height) {
	if(dst_pixmap->scale == GDX2D_SCALE_NEAREST)
		blit_linear(src_pixmap, dst_pixmap, src_x, src_y, src_width, src_height, dst_x, dst_y, dst_width, dst_height);
	if(dst_pixmap->scale == GDX2D_SCALE_BILINEAR)
		blit_bilinear(src_pixmap, dst_pixmap, src_x, src_y, src_width, src_height, dst_x, dst_y, dst_width, dst_height);
}

void gdx2d_draw_pixmap(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap,
					   int32_t src_x, int32_t src_y, uint32_t src_width, uint32_t src_height,
					   int32_t dst_x, int32_t dst_y, uint32_t dst_width, uint32_t dst_height) {
	if(src_width == dst_width && src_height == dst_height) {
		blit_same_size(src_pixmap, dst_pixmap, src_x, src_y, dst_x, dst_y, src_width, src_height);
	} else {
		blit(src_pixmap, dst_pixmap, src_x, src_y, src_width, src_height, dst_x, dst_y, dst_width, dst_height);
	}
}
