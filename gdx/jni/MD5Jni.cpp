#include <MD5Jni.h>

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_loaders_md5_MD5Jni_calculateVertices
  (JNIEnv *env, jclass, jfloatArray skeleton, jfloatArray weights, jfloatArray verticesIn, jfloatArray verticesOut, jint numVertices )
{
	float* pSkeleton = (float*)env->GetPrimitiveArrayCritical(skeleton, 0);
	float* pWeights = (float*)env->GetPrimitiveArrayCritical(weights, 0);
	float* pVerticesIn = (float*)env->GetPrimitiveArrayCritical(verticesIn, 0);
	float* pVerticesOut = (float*)env->GetPrimitiveArrayCritical(verticesOut, 0);

	int len = numVertices * 4;
	for( int vertexOffset = 2, k = 0; vertexOffset < len; vertexOffset += 4 )
	{
		float finalX = 0, finalY = 0, finalZ = 0;

		int weightOffset = (int)pVerticesIn[vertexOffset];
		int weightCount = (int)pVerticesIn[vertexOffset +1];
		weightOffset = (weightOffset << 2) + weightOffset;
		float* vWeights = pWeights + weightOffset;

		for( int j = 0; j < weightCount; j++ )
		{
			int jointOffset = (int)(*vWeights++) << 3;
			float bias = *vWeights++;
			float *vSkeleton = pSkeleton + jointOffset + 1;


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

		pVerticesOut[k++] = finalX;
		pVerticesOut[k++] = finalY;
		pVerticesOut[k++] = finalZ;
		k+=2;
	}

	env->ReleasePrimitiveArrayCritical(skeleton, pSkeleton, 0);
	env->ReleasePrimitiveArrayCritical(weights, pWeights, 0);
	env->ReleasePrimitiveArrayCritical(verticesIn, pVerticesIn, 0);
	env->ReleasePrimitiveArrayCritical(verticesOut, pVerticesOut, 0);
}
