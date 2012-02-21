package com.badlogic.gdx.graphics.g2d;

import java.nio.ByteBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/**
 * Very thin wrapper around libjpeg. Returns Pixmaps given a file or raw jpeg data in a byte array.
 * The returned Pixmap will always have the {@link Format#RGB888} format.
 * @author mzechner
 *
 */
public class Jpeg {
	
	/**
	 * Reads a jpeg from the given file, throws a GdxRuntimeException in case something went wrong.
	 */
	public static Pixmap read(FileHandle file) {
		byte[] bytes = file.readBytes();
		long[] out = new long[4];
		ByteBuffer pixels = read(bytes, 0, bytes.length, out);
		if(pixels == null) throw new GdxRuntimeException("Couldn't load jpeg '" + file + "'");
		return new Pixmap(new Gdx2DPixmap(pixels, out));
	}
	
	/**
	 * Reads a jpeg from the byte array, throws a GdxRuntimeException in case something went wrong.
	 */
	public static Pixmap read(byte[] compressedData, int offset, int length) {
		long[] out = new long[4];
		ByteBuffer pixels = read(compressedData, offset, length, out);
		if(pixels == null) throw new GdxRuntimeException("Couldn't load jpeg");
		return new Pixmap(new Gdx2DPixmap(pixels, out));
	}
	
	/*JNI
	#include "gdx2d.h"
	#include "libjpeg/jpeglib.h"
	#include <setjmp.h>
	#include <stdlib.h>
	
	// custom error handler
	struct CustomErrorMgr
	{
	    jpeg_error_mgr pub;
	    jmp_buf setjmpBuf;
	};
	
	METHODDEF(void) silentExit(j_common_ptr cinfo) {
	  CustomErrorMgr* err = (CustomErrorMgr*) cinfo->err;
	  longjmp(err->setjmpBuf, 1);
	}
	 */
	
	private static native ByteBuffer read(byte[] compressedData, int offset, int length, long[] out); /*
		struct jpeg_decompress_struct cinfo;
		struct jpeg_error_mgr jerr;
		JSAMPROW row_pointer[1];
		unsigned char* raw_image = 0;
		
		CustomErrorMgr err;
		cinfo.err = jpeg_std_error(&err.pub);
		err.pub.error_exit = silentExit;
		if (setjmp(err.setjmpBuf)) {
			jpeg_destroy_decompress(&cinfo);
			if(raw_image != 0) {
				free(raw_image);
				free(row_pointer);
			}
			return 0;
		}
		
		jpeg_create_decompress( &cinfo );
		jpeg_mem_src( &cinfo, (unsigned char*)(compressedData + offset), length );
		jpeg_read_header( &cinfo, TRUE );
		jpeg_start_decompress( &cinfo );
		cinfo.out_color_components	= 3;
		cinfo.output_components = 3;
		cinfo.out_color_space = JCS_RGB;
	
		raw_image = (unsigned char*)malloc( cinfo.output_width*cinfo.output_height*cinfo.num_components );
		row_pointer[0] = (unsigned char *)malloc( cinfo.output_width*cinfo.num_components );
		unsigned int location = 0;
		while( cinfo.output_scanline < cinfo.image_height )
		{
			jpeg_read_scanlines( &cinfo, row_pointer, 1 );
			for(int i=0; i<cinfo.image_width*cinfo.num_components;i++) 
				raw_image[location++] = row_pointer[0][i];
		}
		jpeg_finish_decompress( &cinfo );
		jpeg_destroy_decompress( &cinfo );
		free( row_pointer[0] );
		
		gdx2d_pixmap* pixmap = (gdx2d_pixmap*)malloc(sizeof(gdx2d_pixmap));
		pixmap->width = cinfo.image_width;
		pixmap->height = cinfo.image_height;
		pixmap->format = GDX2D_FORMAT_RGB888;
		pixmap->pixels = raw_image;
		
		out[0] = (jlong)pixmap;
		out[1] = pixmap->width;
		out[2] = pixmap->height;
		out[3] = pixmap->format;
		return env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * pixmap->format);
	*/
	
	public static void main (String[] args) throws Exception {
		ImageBuild.main(new String[0]);
		GdxNativesLoader.load();
		new SharedLibraryLoader("libs/gdx-image-natives.jar").load("gdx-image");
		byte[] bytes = new FileHandle("test/43kJgl.jpg").readBytes();
		Pixmap pixmap = Jpeg.read(new FileHandle("test/43kJgl.jpg"));
		System.out.println(pixmap.getWidth() + ", " + pixmap.getHeight() + ", " + pixmap.getFormat());
		pixmap.dispose();
		pixmap = Jpeg.read(bytes, 0, bytes.length);
		System.out.println(pixmap.getWidth() + ", " + pixmap.getHeight() + ", " + pixmap.getFormat());
		pixmap.dispose();
	}
}
