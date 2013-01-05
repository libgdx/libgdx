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
package com.badlogic.gdx.graphics.g3d.loaders.collada;

import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonJoint;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Joint {
	SkeletonJoint joint;
	
	public Joint(Element animationElement) {
		Array<Element> nodes = animationElement.getChildrenByName("node");
		
		Element jointNode = null;
		for(int i=0;i<nodes.size;i++){
			Element e = nodes.get(i);
			Array<Element> subNodes = e.getChildrenByName("node");
			if(subNodes.size > 0 && subNodes.get(0).getAttribute("type").equalsIgnoreCase("JOINT"))
			{
				jointNode = e;
				break;
			}
		}
		
		if(jointNode==null){
			throw new GdxRuntimeException("no Joint element in scene");
		}
		
		
		joint = getJoint(jointNode,true);
		
	}
	
	SkeletonJoint getJoint(Element jointNode, boolean isRootNode){
		SkeletonJoint joint = new SkeletonJoint();
		joint.name = jointNode.getAttribute("id");
		if(!isRootNode){
			Element matrixElement = jointNode.getChildByName("matrix");
			if(matrixElement != null)
			{
				Matrix4 m = getMatrix(matrixElement);
				m.getTranslation(joint.position);
				m.getRotation(joint.rotation);
				//TODO: get scale from matrix
			}
		}
		
		Array<Element> nodes = jointNode.getChildrenByName("node");
		for(int i=0;i<nodes.size;i++){
			SkeletonJoint child = getJoint(nodes.get(i),false);
			if(!isRootNode){
				child.parent = joint;
			}
			joint.children.add(child);
		}
		
		return joint;
	}
	
	public Matrix4 getMatrix(Element matrix){
		Matrix4 m = new Matrix4();
		
		// read elements into data[]
		String[] tokens = matrix.getText().split("\\s+");
		for (int i = 0; i < tokens.length; i++) {
			m.val[i] = Float.parseFloat(tokens[i]);
		}
		m.tra();
		return m;
	}
}