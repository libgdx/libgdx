/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g3d.loaders.md5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.badlogic.gdx.graphics.g3d.Animation;
import com.badlogic.gdx.math.collision.BoundingBox;

/** Represents an MD5 (Doom 3) skeletal animation.
 * @author Mario Zechner <contact@badlogicgames.com>, Nathan Sweet <admin@esotericsoftware.com>, Dave Clayton
 *         <contact@redskyforge.com> */
public class MD5Animation extends Animation {
	public int frameRate;
	public float secondsPerFrame;

	public MD5Joints[] frames;
	public BoundingBox[] bounds;
	public String name;

	public void write (DataOutputStream out) throws IOException {
		out.writeUTF(name);
		out.writeInt(frameRate);
		out.writeFloat(secondsPerFrame);
		out.writeInt(frames.length);
		for (int i = 0; i < frames.length; i++) {
			frames[i].write(out);
		}
		out.writeInt(bounds.length);
		for (int i = 0; i < bounds.length; i++) {
			out.writeFloat(bounds[i].min.x);
			out.writeFloat(bounds[i].min.y);
			out.writeFloat(bounds[i].min.z);
			out.writeFloat(bounds[i].max.x);
			out.writeFloat(bounds[i].max.y);
			out.writeFloat(bounds[i].max.z);
		}
	}

	public void read (DataInputStream in) throws IOException {
		name = in.readUTF();
		frameRate = in.readInt();
		secondsPerFrame = in.readFloat();
		int numFrames = in.readInt();
		frames = new MD5Joints[numFrames];
		for (int i = 0; i < numFrames; i++) {
			frames[i] = new MD5Joints();
			frames[i].read(in);
		}
		int numBounds = in.readInt();
		bounds = new BoundingBox[numBounds];
		for (int i = 0; i < numBounds; i++) {
			bounds[i] = new BoundingBox();
			bounds[i].min.x = in.readFloat();
			bounds[i].min.y = in.readFloat();
			bounds[i].min.z = in.readFloat();
			bounds[i].max.x = in.readFloat();
			bounds[i].max.y = in.readFloat();
			bounds[i].max.z = in.readFloat();
		}
	}

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

	@Override
	public float getLength () {
		return frames.length * secondsPerFrame;
	}

	@Override
	public int getNumFrames () {
		return frames.length;
	}
}
