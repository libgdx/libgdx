
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "lodmanualOrLodgenerated"
})
@XmlRootElement(name = "levelofdetail")
public class Levelofdetail {

    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String strategy;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String numlevels;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String manual;
    @XmlElements({
        @XmlElement(name = "lodmanual", required = true, type = Lodmanual.class),
        @XmlElement(name = "lodgenerated", required = true, type = Lodgenerated.class)
    })
    protected List<Object> lodmanualOrLodgenerated;

    /**
     * Gets the value of the strategy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrategy() {
        if (strategy == null) {
            return "Distance";
        } else {
            return strategy;
        }
    }

    /**
     * Sets the value of the strategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrategy(String value) {
        this.strategy = value;
    }

    /**
     * Gets the value of the numlevels property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumlevels() {
        return numlevels;
    }

    /**
     * Sets the value of the numlevels property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumlevels(String value) {
        this.numlevels = value;
    }

    /**
     * Gets the value of the manual property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManual() {
        if (manual == null) {
            return "false";
        } else {
            return manual;
        }
    }

    /**
     * Sets the value of the manual property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManual(String value) {
        this.manual = value;
    }

    /**
     * Gets the value of the lodmanualOrLodgenerated property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lodmanualOrLodgenerated property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLodmanualOrLodgenerated().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Lodmanual }
     * {@link Lodgenerated }
     * 
     * 
     */
    public List<Object> getLodmanualOrLodgenerated() {
        if (lodmanualOrLodgenerated == null) {
            lodmanualOrLodgenerated = new ArrayList<Object>();
        }
        return this.lodmanualOrLodgenerated;
    }

}
