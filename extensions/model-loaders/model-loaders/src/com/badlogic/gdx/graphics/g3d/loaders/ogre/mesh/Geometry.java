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

package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"vertexbuffer"})
@XmlRootElement(name = "geometry")
public class Geometry implements BaseGeometry {

	@XmlAttribute public int vertexcount;
	@XmlElement(required = true) protected List<Vertexbuffer> vertexbuffer;

	/** Gets the value of the vertexbuffer property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the
	 * vertexbuffer property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getVertexbuffer().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Vertexbuffer } */
	public List<Vertexbuffer> getVertexbuffer () {
		if (vertexbuffer == null) {
			vertexbuffer = new ArrayList<Vertexbuffer>();
		}
		return this.vertexbuffer;
	}

	@Override
	public int getVertexCount () {
		// TODO Auto-generated method stub
		return vertexcount;
	}

}
