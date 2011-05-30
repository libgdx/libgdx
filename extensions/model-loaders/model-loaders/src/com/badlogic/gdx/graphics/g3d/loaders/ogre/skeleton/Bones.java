
package com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton;

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
    "bone"
})
@XmlRootElement(name = "bones")
public class Bones {

    @XmlElement(required = true)
    protected List<Bone> bone;

    /**
     * Gets the value of the bone property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bone property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBone().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bone }
     * 
     * 
     */
    public List<Bone> getBone() {
        if (bone == null) {
            bone = new ArrayList<Bone>();
        }
        return this.bone;
    }

}
