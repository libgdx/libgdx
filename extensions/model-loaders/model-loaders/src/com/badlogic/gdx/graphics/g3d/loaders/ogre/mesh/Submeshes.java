
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
    "submesh"
})
@XmlRootElement(name = "submeshes")
public class Submeshes {

    @XmlElement(required = true)
    protected List<Submesh> submesh;

    /**
     * Gets the value of the submesh property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the submesh property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubmesh().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Submesh }
     * 
     * 
     */
    public List<Submesh> getSubmesh() {
        if (submesh == null) {
            submesh = new ArrayList<Submesh>();
        }
        return this.submesh;
    }

}
