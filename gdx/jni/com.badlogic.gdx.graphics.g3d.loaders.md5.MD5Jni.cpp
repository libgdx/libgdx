#include <com.badlogic.gdx.graphics.g3d.loaders.md5.MD5Jni.h>
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g3d_loaders_md5_MD5Jni_calculateVertices(JNIEnv* env, jclass clazz, jfloatArray obj_joints, jfloatArray obj_weights, jfloatArray obj_verticesIn, jfloatArray obj_verticesOut, jint numVertices, jint vstride, jint wstride) {
	float* joints = (float*)env->GetPrimitiveArrayCritical(obj_joints, 0);
	float* weights = (float*)env->GetPrimitiveArrayCritical(obj_weights, 0);
	float* verticesIn = (float*)env->GetPrimitiveArrayCritical(obj_verticesIn, 0);
	float* verticesOut = (float*)env->GetPrimitiveArrayCritical(obj_verticesOut, 0);


//@line:23

		for (int vertexOffset = 2, k = 0; vertexOffset < numVertices; vertexOffset += vstride) {
			float finalX = 0;
			float finalY = 0;
			float finalZ = 0;

			int weightOffset = (int)verticesIn[vertexOffset];
			int weightCount = (int)verticesIn[vertexOffset + 1];
			weightOffset = (weightOffset << 2) + weightOffset;

			for (int j = 0; j < weightCount; j++) {
				int jointOffset = (int)weights[weightOffset++] << 3;
				float bias = weights[weightOffset++];
				float vx = weights[weightOffset++];
				float vy = weights[weightOffset++];
				float vz = weights[weightOffset++];

				float qx = joints[jointOffset + 4];
				float qy = joints[jointOffset + 5];
				float qz = joints[jointOffset + 6];
				float qw = joints[jointOffset + 7];

				float ix = -qx, iy = -qy, iz = -qz, iw = qw;

				float tw = -qx * vx - qy * vy - qz * vz;
				float tx = qw * vx + qy * vz - qz * vy;
				float ty = qw * vy + qz * vx - qx * vz;
				float tz = qw * vz + qx * vy - qy * vx;

				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;

				finalX += (joints[jointOffset + 1] + vx) * bias;
				finalY += (joints[jointOffset + 2] + vy) * bias;
				finalZ += (joints[jointOffset + 3] + vz) * bias;
			}

			verticesOut[k++] = finalX;
			verticesOut[k++] = finalY;
			verticesOut[k++] = finalZ;
			k += 2;
		}
	
	env->ReleasePrimitiveArrayCritical(obj_joints, joints, 0);
	env->ReleasePrimitiveArrayCritical(obj_weights, weights, 0);
	env->ReleasePrimitiveArrayCritical(obj_verticesIn, verticesIn, 0);
	env->ReleasePrimitiveArrayCritical(obj_verticesOut, verticesOut, 0);

}

