
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
    "animation"
})
@XmlRootElement(name = "animations")
public class Animations {

    @XmlElement(required = true)
    protected List<Animation> animation;

    /**
     * Gets the value of the animation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Animation }
     * 
     * 
     */
    public List<Animation> getAnimation() {
        if (animation == null) {
            animation = new ArrayList<Animation>();
        }
        return this.animation;
    }

}
