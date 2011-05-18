
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
@XmlRootElement(name = "vertexboneassignment")
public class Vertexboneassignment {

    @XmlAttribute(required = true)    
    public int vertexindex;
    @XmlAttribute(required = true)    
    public int boneindex;
    @XmlAttribute    
    public float weight;  
}
