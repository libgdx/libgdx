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
#ifndef __GDX2D__
#define __GDX2D__

#ifdef __cplusplus
extern "C" {
#endif

/**
 * pixmap formats, components are laid out in memory
 * in the order they appear in the constant name. E.g.
 * GDX_FORMAT_RGB => pixmap[0] = r, pixmap[1] = g, pixmap[2] = b.
 * Components are 8-bit each.
 */
#define GDX2D_FORMAT_ALPHA 				1
#define GDX2D_FORMAT_ALPHA_LUMINANCE 	2
#define GDX2D_FORMAT_RGB 				3
#define GDX2D_FORMAT_RGBA 				4

/**
 * blending modes, to be extended
 */
#define GDX2D_BLEND_NONE 		0
#define GDX2D_BLEND_SRC_OVER 	1

/**
 * scaling modes, to be extended
 */
#define GDX2D_SCALE_NEAREST		0
#define GDX2D_SCALE_BILINEAR	1

/**
 * simple pixmap struct holding the pixel data,
 * the dimensions and the format of the pixmap.
 * the format is one of the GDX2D_FORMAT_XXX constants.
 */
struct {
	int width;
	int height;
	int format;
	const char* pixels;
} gdx2d_pixmap_struct;
typedef struct gdx2d_pixmap_struct gdx2d_pixmap;


gdx2d_pixmap* 	gdx2d_load_buffer	(const char *buffer, int len, int req_format);
gdx2d_pixmap* 	gdx2d_load_file 	(const char *filename, int req_format);
gdx2d_pixmap*	gdx2d_new			(int width, int height, int format);
void 			gdx2d_free			(const gdx2d_pixmap* pixmap);

void gdx2d_clear		(const gdx2d_pixmap* pixmap, int col);
void gdx2d_set_pixel	(const gdx2d_pixmap* pixmap, int x, int y, int col);
void gdx2d_draw_line	(const gdx2d_pixmap* pixmap, int x, int y, int x2, int y2, int col);
void gdx2d_draw_rect	(const gdx2d_pixmap* pixmap, int x, int y, int width, int height, int col);
void gdx2d_draw_circle	(const gdx2d_pixmap* pixmap, int x, int y, int radius, int col);
void gdx2d_fill_rect	(const gdx2d_pixmap* pixmap, int x, int y, int radius, int col);
void gdx2d_fill_circle	(const gdx2d_pixmap* pixmap, int x, int y, int radius, int col);
void gdx2d_draw_pixmap	(const gdx2d_pixmap* src_pixmap,
						 const gdx2d_pixmap* dst_pixmap,
						 int src_x, int src_y, int src_width, int src_height,
						 int dst_x, int dst_y, int dst_width, int dst_height,
						 int blend, int scale);

#ifdef __cplusplus
}
#endif

#endif
