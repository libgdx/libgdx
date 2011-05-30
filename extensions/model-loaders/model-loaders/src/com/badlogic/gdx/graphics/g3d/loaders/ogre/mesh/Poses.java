
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
    "pose"
})
@XmlRootElement(name = "poses")
public class Poses {

    @XmlElement(required = true)
    protected List<Pose> pose;

    /**
     * Gets the value of the pose property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pose property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPose().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Pose }
     * 
     * 
     */
    public List<Pose> getPose() {
        if (pose == null) {
            pose = new ArrayList<Pose>();
        }
        return this.pose;
    }

}
