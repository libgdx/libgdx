
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "keyframe"
})
@XmlRootElement(name = "keyframes")
public class Keyframes {

    protected List<Keyframe> keyframe;

    /**
     * Gets the value of the keyframe property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keyframe property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeyframe().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Keyframe }
     * 
     * 
     */
    public List<Keyframe> getKeyframe() {
        if (keyframe == null) {
            keyframe = new ArrayList<Keyframe>();
        }
        return this.keyframe;
    }

}
