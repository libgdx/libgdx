
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "face"
})
@XmlRootElement(name = "lodfacelist")
public class Lodfacelist {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String submeshindex;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String numfaces;
    @XmlElement(required = true)
    protected List<Face> face;

    /**
     * Gets the value of the submeshindex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubmeshindex() {
        return submeshindex;
    }

    /**
     * Sets the value of the submeshindex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubmeshindex(String value) {
        this.submeshindex = value;
    }

    /**
     * Gets the value of the numfaces property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumfaces() {
        return numfaces;
    }

    /**
     * Sets the value of the numfaces property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumfaces(String value) {
        this.numfaces = value;
    }

    /**
     * Gets the value of the face property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the face property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Face }
     * 
     * 
     */
    public List<Face> getFace() {
        if (face == null) {
            face = new ArrayList<Face>();
        }
        return this.face;
    }

}
