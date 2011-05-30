
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
    "texture"
})
@XmlRootElement(name = "textures")
public class Textures {

    @XmlElement(required = true)
    protected List<Texture> texture;

    /**
     * Gets the value of the texture property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the texture property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTexture().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Texture }
     * 
     * 
     */
    public List<Texture> getTexture() {
        if (texture == null) {
            texture = new ArrayList<Texture>();
        }
        return this.texture;
    }

}
