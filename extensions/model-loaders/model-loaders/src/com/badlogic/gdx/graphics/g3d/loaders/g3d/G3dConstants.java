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

package com.badlogic.gdx.graphics.g3d.loaders.g3d;

public class G3dConstants {
	// Version info for file format
	public static final byte MAJOR_VERSION = 0;
	public static final byte MINOR_VERSION = 1;

	// Unique IDs for chunk declarations
	public static final int G3D_ROOT = 0x4733441A;
	public static final int VERSION_INFO = 0x0001;

	// still model specific constants
	public static final int STILL_MODEL = 0x1000;
	public static final int STILL_SUBMESH = 0x1100;

	// keyframed model specific constants
	public static final int KEYFRAMED_MODEL = 0x2000;
	public static final int KEYFRAMED_SUBMESH = 0x2200;
	public static final int KEYFRAMED_ANIMATION = 0x2300;
	public static final int KEYFRAMED_FRAME = 0x2400;
	
	// skeleton model specific constants
	public static final int SKELETON_MODEL = 0x4000;
	public static final int SKELETON_SUBMESH = 0x4200;
	public static final int SKELETON_ANIMATIONS = 0x4300;
	public static final int SKELETON_ANIMATION = 0x4400;
	public static final int SKELETON = 0x4500;
	public static final int SKELETON_HIERARCHY = 0x4600;
	public static final int SKELETON_JOINT = 0x4700;
	public static final int SKELETON_KEYFRAME = 0x4800;

	// constants used for all types of models, mostly to describe a mesh
	public static final int VERTEX_LIST = 0x1110;
	public static final int INDEX_LIST = 0x1111;
	public static final int VERTEX_ATTRIBUTES = 0x1120;
	public static final int VERTEX_ATTRIBUTE = 0x1121;
	public static final int BONE_WEIGHTS = 0x1130;
	public static final int BONE_WEIGHT = 0x1131;
	public static final int BONE_ASSIGNMENTS = 0x1140;
	public static final int BONE_ASSIGNMENT = 0x1141;
}
