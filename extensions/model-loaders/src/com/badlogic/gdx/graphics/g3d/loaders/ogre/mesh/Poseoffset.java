
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "poseoffset")
public class Poseoffset {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String index;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String x;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String y;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String z;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String nx;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String ny;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String nz;

    /**
     * Gets the value of the index property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIndex(String value) {
        this.index = value;
    }

    /**
     * Gets the value of the x property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setX(String value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setY(String value) {
        this.y = value;
    }

    /**
     * Gets the value of the z property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZ() {
        return z;
    }

    /**
     * Sets the value of the z property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZ(String value) {
        this.z = value;
    }

    /**
     * Gets the value of the nx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNx() {
        if (nx == null) {
            return "";
        } else {
            return nx;
        }
    }

    /**
     * Sets the value of the nx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNx(String value) {
        this.nx = value;
    }

    /**
     * Gets the value of the ny property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNy() {
        if (ny == null) {
            return "";
        } else {
            return ny;
        }
    }

    /**
     * Sets the value of the ny property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNy(String value) {
        this.ny = value;
    }

    /**
     * Gets the value of the nz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNz() {
        if (nz == null) {
            return "";
        } else {
            return nz;
        }
    }

    /**
     * Sets the value of the nz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNz(String value) {
        this.nz = value;
    }

}
