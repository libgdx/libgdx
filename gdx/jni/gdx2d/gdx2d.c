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

static int gdx2d_blend = GDX2D_BLEND_NONE;
static int gdx2d_scale = GDX2D_SCALE_NEAREST;

gdx2d_pixmap* gdx2d_load(const char *buffer, int len, int req_format) {
	int width, height, format;
	const char* pixels = stbi_load_from_memory(buffer, len, &width, &height, &format, req_format);
	if(pixels == NULL)
		return NULL;

	gdx2d_pixmap* pixmap = (gdx2d_pixmap*)malloc(sizeof(gdx2d_pixmap));
	pixmap->width = width;
	pixmap->height = height;
	pixmap->format = format;
	pixmap->pixels = pixels;
	return pixmap;
}

gdx2d_pixmap* gdx2d_new(int width, int height, int format) {
	gdx2d_pixmap* pixmap = (gdx2d_pixmap*)malloc(sizeof(gdx2d_pixmap));
	pixmap->width = width;
	pixmap->height = height;
	pixmap->format = format;
	pixmap->pixels = (char*)malloc(width * height * format);
	return pixmap;
}
void gdx2d_free(const gdx2d_pixmap* pixmap) {
	free((void*)pixmap->pixels);
	free((void*)pixmap);
}

void gdx2d_set_blend (int blend) {
	gdx2d_blend = blend;
}

void gdx2d_set_scale (int scale) {
	gdx2d_scale = scale;
}

void gdx2d_clear(const gdx2d_pixmap* pixmap, int col) {

}

void gdx2d_set_pixel(const gdx2d_pixmap* pixmap, int x, int y, int col) {

}

void gdx2d_draw_line(const gdx2d_pixmap* pixmap, int x, int y, int x2, int y2, int col) {

}

void gdx2d_draw_rect(const gdx2d_pixmap* pixmap, int x, int y, int width, int height, int col) {

}

void gdx2d_draw_circle(const gdx2d_pixmap* pixmap, int x, int y, int radius, int col) {

}

void gdx2d_fill_rect(const gdx2d_pixmap* pixmap, int x, int y, int width, int height, int col) {

}

void gdx2d_fill_circle(const gdx2d_pixmap* pixmap, int x, int y, int radius, int col) {

}

void gdx2d_draw_pixmap(const gdx2d_pixmap* src_pixmap, const gdx2d_pixmap* dst_pixmap,
					   int src_x, int src_y, int src_width, int src_height,
					   int dst_x, int dst_y, int dst_width, int dst_height) {

}
