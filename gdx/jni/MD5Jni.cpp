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

		for( int j = 0; j < weightCount; j++ )
		{
			int jointOffset = (int)pWeights[weightOffset++] << 3;
			float bias = pWeights[weightOffset++];

			float vx = pWeights[weightOffset++];
			float vy = pWeights[weightOffset++];
			float vz = pWeights[weightOffset++];

			float qx = pSkeleton[jointOffset+4];
			float qy = pSkeleton[jointOffset+5];
			float qz = pSkeleton[jointOffset+6];
			float qw = pSkeleton[jointOffset+7];

			float ix = -qx, iy = -qy, iz = -qz, iw = qw;

			float tw = -qx * vx - qy * vy - qz * vz;
			float tx =  qw * vx + qy * vz - qz * vy;
			float ty =  qw * vy + qz * vx - qx * vz;
			float tz =  qw * vz + qx * vy - qy * vx;

			vx = tx * iw + tw * ix + ty * iz - tz * iy;
			vy = ty * iw + tw * iy + tz * ix - tx * iz;
			vz = tz * iw + tw * iz + tx * iy - ty * ix;

			finalX += (pSkeleton[jointOffset+1] + vx) * bias;
			finalY += (pSkeleton[jointOffset+2] + vy) * bias;
			finalZ += (pSkeleton[jointOffset+3] + vz) * bias;
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
