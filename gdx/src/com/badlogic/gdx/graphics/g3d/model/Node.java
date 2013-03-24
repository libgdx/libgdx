package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * A node is part of a hierarchy of Nodes in a {@link Model}. A Node encodes
 * a transform relative to its parents. A Node can have child nodes. Optionally
 * a node can specify a {@link MeshPart} and a {@link NewMaterial} to be applied to the mesh part.
 * @author badlogic
 *
 */
public class Node {
	/** the id, may be null, FIXME is this unique? **/
	public String id;
	/** bone id, -1 if this is not a bone, used for skinning **/
	public int boneId = -1;
	/** parent node, may be null **/
	public Node parent;
	/** child nodes **/
	public final Array<Node> children = new Array<Node>(2);
	
	/** the translation, relative to the parent, not modified by animations **/
	public final Vector3 translation = new Vector3();
	/** the rotation, relative to the parent, not modified by animations **/
	public final Quaternion rotation = new Quaternion();
	/** the scale, relative to the parent, not modified by animations **/
	public final Vector3 scale = new Vector3();
	/** the local transform, based on translation/rotation/scale ({@link #calculateLocalTransform()}) or any applied animation **/
	public final Matrix4 localTransform = new Matrix4();
	/** the world transform, product of local transform and transform of the parent node, calculated via {@link #calculateWorldTransform()}**/
	public final Matrix4 worldTransform = new Matrix4();
	
	public Array<MeshPartMaterial> meshPartMaterials = new Array<MeshPartMaterial>(2);
	
	/**
	 * Calculates the local transform based on the translation, scale and rotation
	 * @return the local transform
	 */
	public Matrix4 calculateLocalTransform() {
		// FIXME implement local transform calculation based on translation,rotation,scale
		return localTransform;
	}

	/**
	 * Calculates the world transform; the product of local transform and the
	 * parent's world transform. 
	 * @return the world transform
	 */
	public Matrix4 calculateWorldTransform() {
		// FIXME implement world transform calculation based on local transform and parent transform
		return worldTransform;
	}
	
	/**
	 * Calculates the local and world transform of this node and optionally all
	 * its children.
	 * 
	 * @param recursive whether to calculate the local/world transforms for children.
	 */
	public void calculateTransforms(boolean recursive) {
		calculateLocalTransform();
		calculateWorldTransform();
		
		if(recursive) {
			for(Node child: children) {
				child.calculateTransforms(true);
			}
		}
	}
}
