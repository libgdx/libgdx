
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
    "submeshname"
})
@XmlRootElement(name = "submeshnames")
public class Submeshnames {

    @XmlElement(required = true)
    protected List<Submeshname> submeshname;

    /**
     * Gets the value of the submeshname property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the submeshname property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubmeshname().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Submeshname }
     * 
     * 
     */
    public List<Submeshname> getSubmeshname() {
        if (submeshname == null) {
            submeshname = new ArrayList<Submeshname>();
        }
        return this.submeshname;
    }

}
