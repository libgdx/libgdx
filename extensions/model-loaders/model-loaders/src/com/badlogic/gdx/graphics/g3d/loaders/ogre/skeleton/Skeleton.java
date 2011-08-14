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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"bones", "bonehierarchy", "animations", "animationlinks"})
@XmlRootElement(name = "skeleton")
public class Skeleton {

	@XmlElement(required = true)
	protected Bones bones;
	@XmlElement(required = true)
	protected Bonehierarchy bonehierarchy;
	protected Animations animations;
	protected Animationlinks animationlinks;

	/** Gets the value of the bones property.
	 * 
	 * @return possible object is {@link Bones } */
	public Bones getBones () {
		return bones;
	}

	/** Sets the value of the bones property.
	 * 
	 * @param value allowed object is {@link Bones } */
	public void setBones (Bones value) {
		this.bones = value;
	}

	/** Gets the value of the bonehierarchy property.
	 * 
	 * @return possible object is {@link Bonehierarchy } */
	public Bonehierarchy getBonehierarchy () {
		return bonehierarchy;
	}

	/** Sets the value of the bonehierarchy property.
	 * 
	 * @param value allowed object is {@link Bonehierarchy } */
	public void setBonehierarchy (Bonehierarchy value) {
		this.bonehierarchy = value;
	}

	/** Gets the value of the animations property.
	 * 
	 * @return possible object is {@link Animations } */
	public Animations getAnimations () {
		return animations;
	}

	/** Sets the value of the animations property.
	 * 
	 * @param value allowed object is {@link Animations } */
	public void setAnimations (Animations value) {
		this.animations = value;
	}

	/** Gets the value of the animationlinks property.
	 * 
	 * @return possible object is {@link Animationlinks } */
	public Animationlinks getAnimationlinks () {
		return animationlinks;
	}

	/** Sets the value of the animationlinks property.
	 * 
	 * @param value allowed object is {@link Animationlinks } */
	public void setAnimationlinks (Animationlinks value) {
		this.animationlinks = value;
	}

}