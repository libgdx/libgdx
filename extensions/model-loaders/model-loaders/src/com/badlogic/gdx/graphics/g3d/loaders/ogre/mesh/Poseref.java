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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "poseref")
public class Poseref {

	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String poseindex;
	@XmlAttribute
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String influence;

	/** Gets the value of the poseindex property.
	 * 
	 * @return possible object is {@link String } */
	public String getPoseindex () {
		return poseindex;
	}

	/** Sets the value of the poseindex property.
	 * 
	 * @param value allowed object is {@link String } */
	public void setPoseindex (String value) {
		this.poseindex = value;
	}

	/** Gets the value of the influence property.
	 * 
	 * @return possible object is {@link String } */
	public String getInfluence () {
		if (influence == null) {
			return "1.0";
		} else {
			return influence;
		}
	}

	/** Sets the value of the influence property.
	 * 
	 * @param value allowed object is {@link String } */
	public void setInfluence (String value) {
		this.influence = value;
	}

}