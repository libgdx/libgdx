
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
    "textures",
    "faces",
    "geometry",
    "boneassignments"
})
@XmlRootElement(name = "submesh")
public class Submesh {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    public String material;
    @XmlAttribute(name = "usesharedvertices")    
    public boolean useSharedVertices;
    @XmlAttribute(name = "use32bitindexes")    
    public boolean use32Bitindexes;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String operationtype;
    protected Textures textures;
    @XmlElement(required = true)
    protected Faces faces;
    protected Geometry geometry;
    protected Boneassignments boneassignments;

    /**
     * Gets the value of the material property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaterial() {
        return material;
    }

    /**
     * Sets the value of the material property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaterial(String value) {
        this.material = value;
    }

    /**
     * Gets the value of the operationtype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperationtype() {
        if (operationtype == null) {
            return "triangle_list";
        } else {
            return operationtype;
        }
    }

    /**
     * Sets the value of the operationtype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperationtype(String value) {
        this.operationtype = value;
    }

    /**
     * Gets the value of the textures property.
     * 
     * @return
     *     possible object is
     *     {@link Textures }
     *     
     */
    public Textures getTextures() {
        return textures;
    }

    /**
     * Sets the value of the textures property.
     * 
     * @param value
     *     allowed object is
     *     {@link Textures }
     *     
     */
    public void setTextures(Textures value) {
        this.textures = value;
    }

    /**
     * Gets the value of the faces property.
     * 
     * @return
     *     possible object is
     *     {@link Faces }
     *     
     */
    public Faces getFaces() {
        return faces;
    }

    /**
     * Sets the value of the faces property.
     * 
     * @param value
     *     allowed object is
     *     {@link Faces }
     *     
     */
    public void setFaces(Faces value) {
        this.faces = value;
    }

    /**
     * Gets the value of the geometry property.
     * 
     * @return
     *     possible object is
     *     {@link Geometry }
     *     
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Sets the value of the geometry property.
     * 
     * @param value
     *     allowed object is
     *     {@link Geometry }
     *     
     */
    public void setGeometry(Geometry value) {
        this.geometry = value;
    }

    /**
     * Gets the value of the boneassignments property.
     * 
     * @return
     *     possible object is
     *     {@link Boneassignments }
     *     
     */
    public Boneassignments getBoneassignments() {
        return boneassignments;
    }

    /**
     * Sets the value of the boneassignments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boneassignments }
     *     
     */
    public void setBoneassignments(Boneassignments value) {
        this.boneassignments = value;
    }

}
