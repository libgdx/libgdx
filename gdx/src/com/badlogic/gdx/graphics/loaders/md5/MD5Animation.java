
package com.badlogic.gdx.graphics.loaders.md5;

import com.badlogic.gdx.math.collision.BoundingBox;

public class MD5Animation {
	public int frameRate;
	public float secondsPerFrame;

	public MD5Joints[] frames;
	public BoundingBox[] bounds;

	static MD5Quaternion jointAOrient = new MD5Quaternion();
	static MD5Quaternion jointBOrient = new MD5Quaternion();

	public static void interpolate (MD5Joints skeletonA, MD5Joints skeletonB, MD5Joints skeletonOut, float t) {
		for (int i = 0, idx = 0; i < skeletonA.numJoints; i++, idx += 8) {
			float jointAPosX = skeletonA.joints[idx + 1];
			float jointAPosY = skeletonA.joints[idx + 2];
			float jointAPosZ = skeletonA.joints[idx + 3];

			jointAOrient.x = skeletonA.joints[idx + 4];
			jointAOrient.y = skeletonA.joints[idx + 5];
			jointAOrient.z = skeletonA.joints[idx + 6];
			jointAOrient.w = skeletonA.joints[idx + 7];

			float jointBPosX = skeletonB.joints[idx + 1];
			float jointBPosY = skeletonB.joints[idx + 2];
			float jointBPosZ = skeletonB.joints[idx + 3];

			jointBOrient.x = skeletonB.joints[idx + 4];
			jointBOrient.y = skeletonB.joints[idx + 5];
			jointBOrient.z = skeletonB.joints[idx + 6];
			jointBOrient.w = skeletonB.joints[idx + 7];

			skeletonOut.joints[idx] = skeletonA.joints[idx];

			skeletonOut.joints[idx + 1] = jointAPosX + t * (jointBPosX - jointAPosX);
			skeletonOut.joints[idx + 2] = jointAPosY + t * (jointBPosY - jointAPosY);
			skeletonOut.joints[idx + 3] = jointAPosZ + t * (jointBPosZ - jointAPosZ);

			jointAOrient.slerp(jointBOrient, t);

			skeletonOut.joints[idx + 4] = jointAOrient.x;
			skeletonOut.joints[idx + 5] = jointAOrient.y;
			skeletonOut.joints[idx + 6] = jointAOrient.z;
			skeletonOut.joints[idx + 7] = jointAOrient.w;
		}
	}
}
