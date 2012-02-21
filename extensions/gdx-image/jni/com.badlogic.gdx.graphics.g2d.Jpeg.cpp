#include <com.badlogic.gdx.graphics.g2d.Jpeg.h>

//@line:27

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
	 static inline jobject wrapped_Java_com_badlogic_gdx_graphics_g2d_Jpeg_read
(JNIEnv* env, jclass clazz, jbyteArray obj_compressedData, jint offset, jint length, jlongArray obj_out, char* compressedData, long long* out) {

//@line:46

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
	
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_Jpeg_read(JNIEnv* env, jclass clazz, jbyteArray obj_compressedData, jint offset, jint length, jlongArray obj_out) {
	char* compressedData = (char*)env->GetPrimitiveArrayCritical(obj_compressedData, 0);
	long long* out = (long long*)env->GetPrimitiveArrayCritical(obj_out, 0);

	jobject JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_g2d_Jpeg_read(env, clazz, obj_compressedData, offset, length, obj_out, compressedData, out);

	env->ReleasePrimitiveArrayCritical(obj_compressedData, compressedData, 0);
	env->ReleasePrimitiveArrayCritical(obj_out, out, 0);

	return JNI_returnValue;
}

