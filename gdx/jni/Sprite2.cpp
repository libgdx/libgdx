#include "Sprite2.h"
#include <math.h>

#define DEG_TO_RAD 3.14159265358979323846f / 180.0f

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_Sprite2_computeVerticesJNI
  (JNIEnv * env, jclass, jfloatArray vertices, jint offset, 
   jfloat x, jfloat y, jfloat width, jfloat height, 
   jfloat originX, jfloat originY, 
   jfloat scaleX, jfloat scaleY, 
   jfloat rotation )
{		
	rotation *= DEG_TO_RAD;

	// bottom left and top right corner points relative to origin
	float worldOriginX = x + originX;
	float worldOriginY = y + originY;
	float fx = -originX;
	float fy = -originY;
	float fx2 = width - originX;
	float fy2 = height - originY;
	
	// scale
	if( scaleX != 1 || scaleY != 1 )
	{
		fx *= scaleX;
		fy *= scaleY;
		fx2 *= scaleX;
		fy2 *= scaleY;
	}
	
	// construct corner points, start from top left and go counter clockwise
	float p1x = fx;
	float p1y = fy;
	float p2x = fx;
	float p2y = fy2;
	float p3x = fx2;
	float p3y = fy2;
	float p4x = fx2;
	float p4y = fy;
	
	float x1;
	float y1;
	float x2;
	float y2;
	float x3;
	float y3;
	float x4;
	float y4;
	
	
	// rotate
	if( rotation != 0 )
	{
		float c = cos( rotation );
		float s = sin( rotation );						
		
		x1 = c * p1x - s * p1y;
		y1 = s * p1x + c * p1y;
		
		x2 = c * p2x - s * p2y;
		y2 = s * p2x + c * p2y;
		
		x3 = c * p3x - s * p3y;
		y3 = s * p3x + c * p3y;
		
		x4 = c * p4x - s * p4y;
		y4 = s * p4x + c * p4y;			
	}
	else
	{
		x1 = p1x;
		y1 = p1y;
		
		x2 = p2x;
		y2 = p2y;
		
		x3 = p3x;
		y3 = p3y;
		
		x4 = p4x;
		y4 = p4y;
	}			
	
	x1 += worldOriginX; y1 += worldOriginY;
	x2 += worldOriginX; y2 += worldOriginY;
	x3 += worldOriginX; y3 += worldOriginY;
	x4 += worldOriginX; y4 += worldOriginY;								
	
	float* pVertices = (float*)env->GetPrimitiveArrayCritical(vertices, 0);

	pVertices[0] = x1;
	pVertices[1] = y1;		
	
	pVertices[5] = x2;
	pVertices[6] = y2;		
	
	pVertices[10] = x3;
	pVertices[11] = y3;		
	
	pVertices[15] = x4;
	pVertices[16] = y4;

	env->ReleasePrimitiveArrayCritical(vertices, pVertices, 0);		
}
