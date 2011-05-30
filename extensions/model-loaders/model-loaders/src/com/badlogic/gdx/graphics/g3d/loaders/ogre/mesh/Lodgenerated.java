
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

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
    "lodfacelist"
})
@XmlRootElement(name = "lodgenerated")
public class Lodgenerated {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String value;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String meshname;
    @XmlElement(required = true)
    protected Lodfacelist lodfacelist;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the meshname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMeshname() {
        return meshname;
    }

    /**
     * Sets the value of the meshname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMeshname(String value) {
        this.meshname = value;
    }

    /**
     * Gets the value of the lodfacelist property.
     * 
     * @return
     *     possible object is
     *     {@link Lodfacelist }
     *     
     */
    public Lodfacelist getLodfacelist() {
        return lodfacelist;
    }

    /**
     * Sets the value of the lodfacelist property.
     * 
     * @param value
     *     allowed object is
     *     {@link Lodfacelist }
     *     
     */
    public void setLodfacelist(Lodfacelist value) {
        this.lodfacelist = value;
    }

}
