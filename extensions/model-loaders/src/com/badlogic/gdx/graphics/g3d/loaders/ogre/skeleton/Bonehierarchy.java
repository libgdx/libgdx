
package com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton;

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
    "boneparent"
})
@XmlRootElement(name = "bonehierarchy")
public class Bonehierarchy {

    protected List<Boneparent> boneparent;

    /**
     * Gets the value of the boneparent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the boneparent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBoneparent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boneparent }
     * 
     * 
     */
    public List<Boneparent> getBoneparent() {
        if (boneparent == null) {
            boneparent = new ArrayList<Boneparent>();
        }
        return this.boneparent;
    }

}
