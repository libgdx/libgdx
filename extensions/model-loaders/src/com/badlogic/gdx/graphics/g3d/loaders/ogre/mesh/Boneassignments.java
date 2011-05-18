
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "vertexboneassignment"
})
@XmlRootElement(name = "boneassignments")
public class Boneassignments {

    @XmlElement(required = true)
    protected List<Vertexboneassignment> vertexboneassignment;

    /**
     * Gets the value of the vertexboneassignment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vertexboneassignment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVertexboneassignment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Vertexboneassignment }
     * 
     * 
     */
    public List<Vertexboneassignment> getVertexboneassignment() {
        if (vertexboneassignment == null) {
            vertexboneassignment = new ArrayList<Vertexboneassignment>();
        }
        return this.vertexboneassignment;
    }

}
