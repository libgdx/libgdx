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

public class MD5Joints {
	public String[] names;
	public int numJoints;
	/** (0) parent, (1) pos.x, (2) pos.y, (3) pos.z, (4) orient.x, (5) orient.y, (6) orient.z, (7) orient.w **/
	private static final int stride = 8;
	public float[] joints;

	public void read (DataInputStream in) throws IOException {
		int numNames = in.readInt();
		names = new String[numNames];
		for (int i = 0; i < numNames; i++) {
			names[i] = in.readUTF();
		}
		numJoints = in.readInt();
		joints = new float[numJoints * stride];
		for (int i = 0; i < numJoints * stride; i++) {
			joints[i] = in.readFloat();
		}
	}

	public void write (DataOutputStream out) throws IOException {
		out.writeInt(names.length);
		for (int i = 0; i < names.length; i++) {
			out.writeUTF(names[i]);
		}
		out.writeInt(numJoints);
		for (int i = 0; i < numJoints * stride; i++) {
			out.writeFloat(joints[i]);
		}
	}
}
