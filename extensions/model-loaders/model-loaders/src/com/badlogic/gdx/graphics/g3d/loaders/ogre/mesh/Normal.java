
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "normal")
public class Normal {

    @XmlAttribute(required = true)    
    public float x;
    @XmlAttribute(required = true)    
    public float y;
    @XmlAttribute(required = true)    
    public float z;
}
