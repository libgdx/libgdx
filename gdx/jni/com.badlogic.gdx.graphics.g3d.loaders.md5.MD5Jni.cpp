#include <com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Jni.h>
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g3d_loaders_md5_MD5Jni_calculateVertices(JNIEnv* env, jclass clazz, jfloatArray obj_skeleton, jfloatArray obj_weights, jfloatArray obj_verticesIn, jfloatArray obj_verticesOut, jint numVertices) {
	float* skeleton = (float*)env->GetPrimitiveArrayCritical(obj_skeleton, 0);
	float* weights = (float*)env->GetPrimitiveArrayCritical(obj_weights, 0);
	float* verticesIn = (float*)env->GetPrimitiveArrayCritical(obj_verticesIn, 0);
	float* verticesOut = (float*)env->GetPrimitiveArrayCritical(obj_verticesOut, 0);


//@line:21

		int len = numVertices * 4;
		for( int vertexOffset = 2, k = 0; vertexOffset < len; vertexOffset += 4 )
		{
			float finalX = 0, finalY = 0, finalZ = 0;
	
			int weightOffset = (int)verticesIn[vertexOffset];
			int weightCount = (int)verticesIn[vertexOffset +1];
			weightOffset = (weightOffset << 2) + weightOffset;
			float* vWeights = weights + weightOffset;
	
			for( int j = 0; j < weightCount; j++ )
			{
				int jointOffset = (int)(*vWeights++) << 3;
				float bias = *vWeights++;
				float *vSkeleton = skeleton + jointOffset + 1;
	
	
				float vx = *vWeights++;
				float vy = *vWeights++;
				float vz = *vWeights++;
	
				float jx = *vSkeleton++;
				float jy = *vSkeleton++;
				float jz = *vSkeleton++;
	
				float qx = *vSkeleton++;
				float qy = *vSkeleton++;
				float qz = *vSkeleton++;
				float qw = *vSkeleton++;
	
				float ix = -qx, iy = -qy, iz = -qz, iw = qw;
	
				float tw = -qx * vx - qy * vy - qz * vz;
				float tx =  qw * vx + qy * vz - qz * vy;
				float ty =  qw * vy + qz * vx - qx * vz;
				float tz =  qw * vz + qx * vy - qy * vx;
	
				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;
	
				finalX += (jx + vx) * bias;
				finalY += (jy + vy) * bias;
				finalZ += (jz + vz) * bias;
			}
	
			verticesOut[k++] = finalX;
			verticesOut[k++] = finalY;
			verticesOut[k++] = finalZ;
			k+=2;
		}
	
	env->ReleasePrimitiveArrayCritical(obj_skeleton, skeleton, 0);
	env->ReleasePrimitiveArrayCritical(obj_weights, weights, 0);
	env->ReleasePrimitiveArrayCritical(obj_verticesIn, verticesIn, 0);
	env->ReleasePrimitiveArrayCritical(obj_verticesOut, verticesOut, 0);

}

