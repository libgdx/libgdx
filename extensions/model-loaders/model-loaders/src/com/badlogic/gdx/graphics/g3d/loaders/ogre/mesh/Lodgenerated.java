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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"lodfacelist"})
@XmlRootElement(name = "lodgenerated")
public class Lodgenerated {

	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String value;
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String meshname;
	@XmlElement(required = true)
	protected Lodfacelist lodfacelist;

	/** Gets the value of the value property.
	 * 
	 * @return possible object is {@link String } */
	public String getValue () {
		return value;
	}

	/** Sets the value of the value property.
	 * 
	 * @param value allowed object is {@link String } */
	public void setValue (String value) {
		this.value = value;
	}

	/** Gets the value of the meshname property.
	 * 
	 * @return possible object is {@link String } */
	public String getMeshname () {
		return meshname;
	}

	/** Sets the value of the meshname property.
	 * 
	 * @param value allowed object is {@link String } */
	public void setMeshname (String value) {
		this.meshname = value;
	}

	/** Gets the value of the lodfacelist property.
	 * 
	 * @return possible object is {@link Lodfacelist } */
	public Lodfacelist getLodfacelist () {
		return lodfacelist;
	}

	/** Sets the value of the lodfacelist property.
	 * 
	 * @param value allowed object is {@link Lodfacelist } */
	public void setLodfacelist (Lodfacelist value) {
		this.lodfacelist = value;
	}

}