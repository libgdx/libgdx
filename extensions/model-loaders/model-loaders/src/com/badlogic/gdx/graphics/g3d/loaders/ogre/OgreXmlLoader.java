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

package com.badlogic.gdx.graphics.g3d.loaders.ogre;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.BaseGeometry;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Boneassignments;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.ColourDiffuse;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Face;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Geometry;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Mesh;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Submesh;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Submeshname;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Texcoord;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Vertex;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Vertexboneassignment;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh.Vertexbuffer;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Animation;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Bone;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Boneparent;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Keyframe;
import com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Track;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.model.skeleton.Skeleton;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonAnimation;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonJoint;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonKeyframe;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonSubMesh;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;

public class OgreXmlLoader {
	public SubMesh[] loadMeshes (FileHandle file) {
		InputStream in = null;
		try {
			in = file.read();
			return loadMesh(in);
		} catch (Throwable t) {
			throw new GdxRuntimeException("Couldn't load file '" + file.name() + "'", t);
		} finally {
			if (in != null) try {
				in.close();
			} catch (Exception e) {
			}
		}
	}

	public SubMesh[] loadMesh (InputStream in) {
		try {
			Mesh ogreMesh = loadOgreMesh(in);
			SubMesh[] meshes = generateSubMeshes(ogreMesh);
			return meshes;
		} catch (Throwable t) {
			throw new GdxRuntimeException("Couldn't load meshes", t);
		}
	}

	public SkeletonModel load (FileHandle mesh, FileHandle skeleton) {
		SubMesh[] meshes = loadMeshes(mesh);
		return new SkeletonModel(loadSkeleton(skeleton), meshes);
	}

	public StillModel load (FileHandle mesh) {
		SubMesh[] meshes = loadMeshes(mesh);
		return new StillModel(meshes);
	}
	
	private SubMesh[] generateSubMeshes (Mesh ogreMesh) {
		List<Submesh> ogreSubmeshes = ogreMesh.getSubmeshes().getSubmesh();
		SubMesh[] submeshes = new SubMesh[ogreSubmeshes.size()];

		for (int i = 0; i < ogreSubmeshes.size(); i++) {
			Submesh ogreSubmesh = ogreSubmeshes.get(i);
			boolean usesTriangleList = false;

			if (ogreSubmesh.use32Bitindexes) throw new GdxRuntimeException("submesh '" + i + "' uses 32-bit indices");
			if (ogreSubmesh.getOperationtype().equals("triangle_list")) {
				usesTriangleList = true;
			}

			short[] indices = new short[ogreSubmesh.getFaces().count * (usesTriangleList ? 3 : 1)];
			for (int j = 0, idx = 0; j < ogreSubmesh.getFaces().count; j++) {
				Face face = ogreSubmesh.getFaces().getFace().get(j);
				indices[idx++] = face.v1;
				if (usesTriangleList || j == 0) {
					indices[idx++] = face.v2;
					indices[idx++] = face.v3;
				}
			}

			List<VertexAttribute> attributes = new ArrayList<VertexAttribute>();
			IntArray offsets = new IntArray();
			int offset = 0;
			
			BaseGeometry geom;
			
			if (ogreSubmesh.useSharedVertices) {
				geom = ogreMesh.getSharedgeometry();
			} else {
				geom = ogreSubmesh.getGeometry();
			}
			
			for (int j = 0; j < geom.getVertexbuffer().size(); j++) {
				Vertexbuffer buffer = geom.getVertexbuffer().get(j);
				offsets.add(offset);
				if (buffer.positions) {
					attributes.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
					offset += 3;
				}
				if (buffer.normals) {
					attributes.add(new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE));
					offset += 3;
				}
				if (buffer.tangents) {
					attributes.add(new VertexAttribute(Usage.Generic, buffer.tangentDimensions, ShaderProgram.TANGENT_ATTRIBUTE));
					offset += buffer.tangentDimensions;
				}
				if (buffer.binormals) {
					attributes.add(new VertexAttribute(Usage.Generic, 3, ShaderProgram.BINORMAL_ATTRIBUTE));
					offset += 3;
				}
				if (buffer.coloursDiffuse) {
					attributes.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
					offset += 4;
				}

				for (int k = 0; k < buffer.textureCoords; k++) {
					try {
						int numTexCoords = 0;
						switch (k) {
						case 0:
							numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions0());
							break;
						case 1:
							numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions1());
							break;
						case 2:
							numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions2());
							break;
						case 3:
							numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions3());
							break;
						case 4:
							numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions4());
							break;
						case 5:
							numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions5());
							break;
						case 6:
							numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions6());
							break;
						case 7:
							numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions7());
							break;
						}
						attributes
							.add(new VertexAttribute(Usage.TextureCoordinates, numTexCoords, ShaderProgram.TEXCOORD_ATTRIBUTE + k));
						offset += numTexCoords;
					} catch (NumberFormatException e) {
						throw new GdxRuntimeException("Can't process texture coords with dimensions != 1, 2, 3, 4 (e.g. float1)");
					}
				}
			}
			VertexAttributes attribs = new VertexAttributes(attributes.toArray(new VertexAttribute[0]));
			int vertexSize = offset;
			float[] vertices = new float[geom.getVertexCount() * offset];
			for (int j = 0; j < geom.getVertexbuffer().size(); j++) {
				Vertexbuffer buffer = geom.getVertexbuffer().get(j);
				offset = offsets.get(j);
				int idx = offset;

				for (int k = 0; k < buffer.getVertex().size(); k++) {
					Vertex v = buffer.getVertex().get(k);
					if (v.getPosition() != null) {
						vertices[idx++] = v.getPosition().x;
						vertices[idx++] = v.getPosition().y;
						vertices[idx++] = v.getPosition().z;
					}

					if (v.getNormal() != null) {
						vertices[idx++] = v.getNormal().x;
						vertices[idx++] = v.getNormal().y;
						vertices[idx++] = v.getNormal().z;
					}

					if (v.getTangent() != null) {
						vertices[idx++] = v.getTangent().x;
						vertices[idx++] = v.getTangent().y;
						vertices[idx++] = v.getTangent().z;
						if (buffer.tangentDimensions == 4) vertices[idx++] = v.getTangent().w;
					}

					if (v.getBinormal() != null) {
						vertices[idx++] = v.getBinormal().x;
						vertices[idx++] = v.getBinormal().y;
						vertices[idx++] = v.getBinormal().z;
					}

					if (v.getColourDiffuse() != null) {
						float color = getColor(v.getColourDiffuse());
						vertices[idx++] = color;
					}

					if (v.getTexcoord() != null) {
						for (int l = 0; l < v.getTexcoord().size(); l++) {
							Texcoord texCoord = v.getTexcoord().get(l);
							int numTexCoords = 0;
							switch (l) {
							case 0:
								numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions0());
								break;
							case 1:
								numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions1());
								break;
							case 2:
								numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions2());
								break;
							case 3:
								numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions3());
								break;
							case 4:
								numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions4());
								break;
							case 5:
								numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions5());
								break;
							case 6:
								numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions6());
								break;
							case 7:
								numTexCoords = Integer.valueOf(buffer.getTextureCoordDimensions7());
								break;
							}

							if (numTexCoords == 1) {
								vertices[idx++] = texCoord.u;
							}

							if (numTexCoords == 2) {
								vertices[idx++] = texCoord.u;
								vertices[idx++] = texCoord.v;
							}

							if (numTexCoords == 3) {
								vertices[idx++] = texCoord.u;
								vertices[idx++] = texCoord.v;
								vertices[idx++] = texCoord.w;
							}

							if (numTexCoords == 4) {
								vertices[idx++] = texCoord.u;
								vertices[idx++] = texCoord.v;
								vertices[idx++] = texCoord.w;
								vertices[idx++] = texCoord.x;
							}
						}
					}

					offset += vertexSize;
					idx = offset;
				}
			}

			com.badlogic.gdx.graphics.Mesh mesh = new com.badlogic.gdx.graphics.Mesh(false, vertices.length / vertexSize,
				indices.length, attribs);
			mesh.setIndices(indices);
			mesh.setVertices(vertices);
			
			String meshName = "";
			List<Submeshname> names = ogreMesh.getSubmeshnames().getSubmeshname();
			for(int n = 0; n < names.size(); ++n) {
				if(Integer.parseInt(names.get(n).getIndex()) == i)
					meshName = names.get(n).getName();
			}

			SubMesh subMesh;
			Boneassignments boneAssigments = (ogreSubmesh.getBoneassignments() != null)? ogreSubmesh.getBoneassignments() : ogreMesh.getBoneassignments();			
			
			if (boneAssigments != null) {
				subMesh = new SkeletonSubMesh(meshName, mesh, GL10.GL_TRIANGLES);
			} else {
				subMesh = new StillSubMesh(meshName, mesh, GL10.GL_TRIANGLES);
			}
			
			// FIXME ? subMesh.materialName = ogreSubmesh.material;


			if (boneAssigments != null) {
				SkeletonSubMesh subSkelMesh = (SkeletonSubMesh) subMesh;
				subSkelMesh.setVertices(vertices);
				subSkelMesh.setIndices(indices);
				subSkelMesh.skinnedVertices = new float[vertices.length];
				System.arraycopy(subSkelMesh.vertices, 0, subSkelMesh.skinnedVertices, 0, subSkelMesh.vertices.length);
				loadBones(boneAssigments, subSkelMesh);
			}

			if (ogreSubmesh.getOperationtype().equals("triangle_list")) subMesh.primitiveType = GL10.GL_TRIANGLES;
			if (ogreSubmesh.getOperationtype().equals("triangle_fan")) subMesh.primitiveType = GL10.GL_TRIANGLE_FAN;
			if (ogreSubmesh.getOperationtype().equals("triangle_strip")) subMesh.primitiveType = GL10.GL_TRIANGLE_STRIP;

			submeshes[i] = subMesh;
		}
		return submeshes;
	}

	private void loadBones (Boneassignments boneAssigments, SkeletonSubMesh subMesh) {
		Array<IntArray> boneAssignments = new Array<IntArray>();
		Array<FloatArray> boneWeights = new Array<FloatArray>();
		
		for (int j = 0; j < subMesh.getMesh().getNumVertices(); j++) {
			boneAssignments.add(new IntArray(4));
			boneWeights.add(new FloatArray(4));
		}
		
		List<Vertexboneassignment> vertexboneassignment = boneAssigments.getVertexboneassignment();
		for (int j = 0; j < vertexboneassignment.size(); j++) {
			Vertexboneassignment assignment = vertexboneassignment.get(j);
			int boneIndex = assignment.boneindex;
			int vertexIndex = assignment.vertexindex;
			float weight = assignment.weight;

			boneAssignments.get(vertexIndex).add(boneIndex);
			boneWeights.get(vertexIndex).add(weight);
		}

		subMesh.boneAssignments = new int[boneAssignments.size][];
		subMesh.boneWeights = new float[boneWeights.size][];
		for (int j = 0; j < boneAssignments.size; j++) {
			subMesh.boneAssignments[j] = new int[boneAssignments.get(j).size];
			subMesh.boneWeights[j] = new float[boneWeights.get(j).size];
			for (int k = 0; k < boneAssignments.get(j).size; k++) {
				subMesh.boneAssignments[j][k] = boneAssignments.get(j).get(k);
			}
			for (int k = 0; k < boneWeights.get(j).size; k++) {
				subMesh.boneWeights[j][k] = boneWeights.get(j).get(k);
			}
		}		
	}

	Color color = new Color();

	private float getColor (ColourDiffuse colourDiffuse) {
		String[] tokens = colourDiffuse.getValue().split(" ");
		if (tokens.length == 3)
			color.set(Float.valueOf(tokens[0]), Float.valueOf(tokens[1]), Float.valueOf(tokens[2]), 1);
		else
			color.set(Float.valueOf(tokens[0]), Float.valueOf(tokens[1]), Float.valueOf(tokens[2]), Float.valueOf(tokens[3]));
		return color.toFloatBits();
	}

	private Mesh loadOgreMesh (InputStream in) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Mesh.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		long start = System.nanoTime();
		Mesh mesh = (Mesh)unmarshaller.unmarshal(in);
		System.out.println("took: " + (System.nanoTime() - start) / 1000000000.0f);
		return mesh;
	}

	public Skeleton loadSkeleton (FileHandle file) {
		InputStream in = null;
		try {
			in = file.read();
			return loadSkeleton(in);
		} catch (Throwable t) {
			throw new GdxRuntimeException("Couldn't load file '" + file.name() + "'", t);
		} finally {
			if (in != null) try {
				in.close();
			} catch (Exception e) {
			}
		}
	}

	public Skeleton loadSkeleton (InputStream in) {
		try {
			com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Skeleton ogreSkel = loadOgreSkeleton(in);
			return generateSkeleton(ogreSkel);
		} catch (Throwable t) {
			throw new GdxRuntimeException("Couldn't load model", t);
		}
	}

	private Skeleton generateSkeleton (com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Skeleton ogreSkel) {
		List<Bone> bones = ogreSkel.getBones().getBone();
		List<SkeletonJoint> joints = new ArrayList<SkeletonJoint>();
		Map<String, SkeletonJoint> nameToJoint = new HashMap<String, SkeletonJoint>();
		for (int i = 0; i < bones.size(); i++) {
			Bone bone = bones.get(i);
			SkeletonJoint joint = new SkeletonJoint();

			joint.name = bone.name;
			joint.position.set(bone.position.x, bone.position.y, bone.position.z);
			joint.rotation.setFromAxis(bone.rotation.axis.x, bone.rotation.axis.y, bone.rotation.axis.z, MathUtils.radiansToDegrees
				* bone.rotation.angle);
			if (bone.scale != null) {
				if (bone.scale.factor == 0)
					joint.scale.set(bone.scale.x, bone.scale.y, bone.scale.z);
				else
					joint.scale.set(bone.scale.factor, bone.scale.factor, bone.scale.factor);
			}
			joints.add(joint);
			nameToJoint.put(joint.name, joint);
		}

		List<Boneparent> hierarchy = ogreSkel.getBonehierarchy().getBoneparent();
		for (int i = 0; i < hierarchy.size(); i++) {
			Boneparent link = hierarchy.get(i);
			SkeletonJoint joint = nameToJoint.get(link.getBone());
			SkeletonJoint parent = nameToJoint.get(link.getParent());
			parent.children.add(joint);
			joint.parent = parent;
		}

		Skeleton skel = new Skeleton();
		for (int i = 0; i < joints.size(); i++) {
			SkeletonJoint joint = joints.get(i);
			if (joint.parent == null) skel.hierarchy.add(joint);
		}

		skel.buildFromHierarchy();

		List<Animation> animations = ogreSkel.getAnimations().getAnimation();
		for (int i = 0; i < animations.size(); i++) {
			Animation animation = animations.get(i);
			SkeletonKeyframe[][] perJointkeyFrames = new SkeletonKeyframe[skel.bindPoseJoints.size][];

			List<Track> tracks = animation.getTracks().getTrack();
			if (tracks.size() != perJointkeyFrames.length)
				throw new IllegalArgumentException("Number of tracks does not equal number of joints");

			Matrix4 rotation = new Matrix4();
			Matrix4 transform = new Matrix4();

			for (int j = 0; j < tracks.size(); j++) {
				Track track = tracks.get(j);
				String jointName = track.getBone();
				int jointIndex = skel.namesToIndices.get(jointName);
				if (perJointkeyFrames[jointIndex] != null)
					throw new IllegalArgumentException("Track for bone " + jointName + " in animation " + animation.name
						+ " already defined!");
				SkeletonKeyframe[] jointKeyFrames = new SkeletonKeyframe[track.getKeyframes().getKeyframe().size()];
				perJointkeyFrames[jointIndex] = jointKeyFrames;

				for (int k = 0; k < track.getKeyframes().getKeyframe().size(); k++) {
					Keyframe keyFrame = track.getKeyframes().getKeyframe().get(k);
					SkeletonKeyframe jointKeyframe = new SkeletonKeyframe();
					jointKeyframe.timeStamp = keyFrame.time;
					jointKeyframe.position.set(keyFrame.translate.x, keyFrame.translate.y, keyFrame.translate.z);
					if (keyFrame.scale != null) {
						if (keyFrame.scale.factor == 0)
							jointKeyframe.scale.set(keyFrame.scale.x, keyFrame.scale.y, keyFrame.scale.z);
						else
							jointKeyframe.scale.set(keyFrame.scale.factor, keyFrame.scale.factor, keyFrame.scale.factor);
					}
					jointKeyframe.rotation
						.setFromAxis(keyFrame.rotate.axis.x, keyFrame.rotate.axis.y, keyFrame.rotate.axis.z,
							MathUtils.radiansToDegrees * keyFrame.rotate.angle).nor();
					jointKeyframe.parentIndex = skel.bindPoseJoints.get(jointIndex).parentIndex;
					jointKeyFrames[k] = jointKeyframe;

					rotation.set(jointKeyframe.rotation);
					rotation.trn(jointKeyframe.position);
					transform.set(skel.sceneMatrices.get(jointIndex));
					transform.mul(rotation);
					if (jointKeyframe.parentIndex != -1) {
						rotation.set(skel.offsetMatrices.get(jointKeyframe.parentIndex)).mul(transform);
						transform.set(rotation);
					}

					transform.getTranslation(jointKeyframe.position);
					transform.getRotation(jointKeyframe.rotation);
					jointKeyframe.rotation.x *= -1;
					jointKeyframe.rotation.y *= -1;
					jointKeyframe.rotation.z *= -1;
				}
			}

			for (int j = 0; j < perJointkeyFrames.length; j++) {
				if (perJointkeyFrames[j] == null) throw new IllegalArgumentException("No track for bone " + skel.jointNames.get(j));
			}

			skel.animations.put(animation.name, new SkeletonAnimation(animation.name, animation.length, perJointkeyFrames));
		}

		return skel;
	}

	private com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Skeleton loadOgreSkeleton (InputStream in) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Skeleton.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		long start = System.nanoTime();
		com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Skeleton skel = (com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton.Skeleton)unmarshaller
			.unmarshal(in);
		System.out.println("took: " + (System.nanoTime() - start) / 1000000000.0f);
		return skel;
	}
}
