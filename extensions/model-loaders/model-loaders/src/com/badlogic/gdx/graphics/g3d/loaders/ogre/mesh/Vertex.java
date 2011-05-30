
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
    "position",
    "normal",
    "tangent",
    "binormal",
    "colourDiffuse",
    "colourSpecular",
    "texcoord"
})
@XmlRootElement(name = "vertex")
public class Vertex {

    @XmlElement(required = true)
    protected Position position;
    protected Normal normal;
    protected Tangent tangent;
    protected Binormal binormal;
    @XmlElement(name = "colour_diffuse")
    protected ColourDiffuse colourDiffuse;
    @XmlElement(name = "colour_specular")
    protected ColourSpecular colourSpecular;
    protected List<Texcoord> texcoord;

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link Position }
     *     
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link Position }
     *     
     */
    public void setPosition(Position value) {
        this.position = value;
    }

    /**
     * Gets the value of the normal property.
     * 
     * @return
     *     possible object is
     *     {@link Normal }
     *     
     */
    public Normal getNormal() {
        return normal;
    }

    /**
     * Sets the value of the normal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Normal }
     *     
     */
    public void setNormal(Normal value) {
        this.normal = value;
    }

    /**
     * Gets the value of the tangent property.
     * 
     * @return
     *     possible object is
     *     {@link Tangent }
     *     
     */
    public Tangent getTangent() {
        return tangent;
    }

    /**
     * Sets the value of the tangent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tangent }
     *     
     */
    public void setTangent(Tangent value) {
        this.tangent = value;
    }

    /**
     * Gets the value of the binormal property.
     * 
     * @return
     *     possible object is
     *     {@link Binormal }
     *     
     */
    public Binormal getBinormal() {
        return binormal;
    }

    /**
     * Sets the value of the binormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Binormal }
     *     
     */
    public void setBinormal(Binormal value) {
        this.binormal = value;
    }

    /**
     * Gets the value of the colourDiffuse property.
     * 
     * @return
     *     possible object is
     *     {@link ColourDiffuse }
     *     
     */
    public ColourDiffuse getColourDiffuse() {
        return colourDiffuse;
    }

    /**
     * Sets the value of the colourDiffuse property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColourDiffuse }
     *     
     */
    public void setColourDiffuse(ColourDiffuse value) {
        this.colourDiffuse = value;
    }

    /**
     * Gets the value of the colourSpecular property.
     * 
     * @return
     *     possible object is
     *     {@link ColourSpecular }
     *     
     */
    public ColourSpecular getColourSpecular() {
        return colourSpecular;
    }

    /**
     * Sets the value of the colourSpecular property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColourSpecular }
     *     
     */
    public void setColourSpecular(ColourSpecular value) {
        this.colourSpecular = value;
    }

    /**
     * Gets the value of the texcoord property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the texcoord property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTexcoord().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Texcoord }
     * 
     * 
     */
    public List<Texcoord> getTexcoord() {
        if (texcoord == null) {
            texcoord = new ArrayList<Texcoord>();
        }
        return this.texcoord;
    }

}
