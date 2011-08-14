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
package com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "boneparent")
public class Boneparent {

	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String bone;
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String parent;

	/** Gets the value of the bone property.
	 * 
	 * @return possible object is {@link String } */
	public String getBone () {
		return bone;
	}

	/** Sets the value of the bone property.
	 * 
	 * @param value allowed object is {@link String } */
	public void setBone (String value) {
		this.bone = value;
	}

	/** Gets the value of the parent property.
	 * 
	 * @return possible object is {@link String } */
	public String getParent () {
		return parent;
	}

	/** Sets the value of the parent property.
	 * 
	 * @param value allowed object is {@link String } */
	public void setParent (String value) {
		this.parent = value;
	}

}