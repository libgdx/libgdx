
package com.badlogic.gdx.graphics.g3d.loaders.ogre.skeleton;

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
    "keyframes"
})
@XmlRootElement(name = "track")
public class Track {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String bone;
    @XmlElement(required = true)
    protected Keyframes keyframes;

    /**
     * Gets the value of the bone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBone() {
        return bone;
    }

    /**
     * Sets the value of the bone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBone(String value) {
        this.bone = value;
    }

    /**
     * Gets the value of the keyframes property.
     * 
     * @return
     *     possible object is
     *     {@link Keyframes }
     *     
     */
    public Keyframes getKeyframes() {
        return keyframes;
    }

    /**
     * Sets the value of the keyframes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Keyframes }
     *     
     */
    public void setKeyframes(Keyframes value) {
        this.keyframes = value;
    }

}
