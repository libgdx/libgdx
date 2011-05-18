
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "vertex"
})
@XmlRootElement(name = "vertexbuffer")
public class Vertexbuffer {

    @XmlAttribute    
    public boolean positions;
    @XmlAttribute    
    public boolean normals;
    @XmlAttribute(name = "colours_diffuse")    
    public boolean coloursDiffuse;
    @XmlAttribute(name = "colours_specular")    
    public boolean coloursSpecular;
    @XmlAttribute(name = "texture_coords")    
    public int textureCoords;
    @XmlAttribute(name = "texture_coord_dimensions_0")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textureCoordDimensions0;
    @XmlAttribute(name = "texture_coord_dimensions_1")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textureCoordDimensions1;
    @XmlAttribute(name = "texture_coord_dimensions_2")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textureCoordDimensions2;
    @XmlAttribute(name = "texture_coord_dimensions_3")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textureCoordDimensions3;
    @XmlAttribute(name = "texture_coord_dimensions_4")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textureCoordDimensions4;
    @XmlAttribute(name = "texture_coord_dimensions_5")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textureCoordDimensions5;
    @XmlAttribute(name = "texture_coord_dimensions_6")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textureCoordDimensions6;
    @XmlAttribute(name = "texture_coord_dimensions_7")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String textureCoordDimensions7;
    @XmlAttribute    
    public boolean tangents;
    @XmlAttribute(name = "tangent_dimensions")    
    public int tangentDimensions;
    @XmlAttribute    
    public boolean binormals;
    @XmlElement(required = true)
    protected List<Vertex> vertex;    

    /**
     * Gets the value of the textureCoordDimensions0 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureCoordDimensions0() {
        if (textureCoordDimensions0 == null) {
            return "2";
        } else {
            return textureCoordDimensions0;
        }
    }

    /**
     * Sets the value of the textureCoordDimensions0 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureCoordDimensions0(String value) {
        this.textureCoordDimensions0 = value;
    }

    /**
     * Gets the value of the textureCoordDimensions1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureCoordDimensions1() {
        if (textureCoordDimensions1 == null) {
            return "2";
        } else {
            return textureCoordDimensions1;
        }
    }

    /**
     * Sets the value of the textureCoordDimensions1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureCoordDimensions1(String value) {
        this.textureCoordDimensions1 = value;
    }

    /**
     * Gets the value of the textureCoordDimensions2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureCoordDimensions2() {
        if (textureCoordDimensions2 == null) {
            return "2";
        } else {
            return textureCoordDimensions2;
        }
    }

    /**
     * Sets the value of the textureCoordDimensions2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureCoordDimensions2(String value) {
        this.textureCoordDimensions2 = value;
    }

    /**
     * Gets the value of the textureCoordDimensions3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureCoordDimensions3() {
        if (textureCoordDimensions3 == null) {
            return "2";
        } else {
            return textureCoordDimensions3;
        }
    }

    /**
     * Sets the value of the textureCoordDimensions3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureCoordDimensions3(String value) {
        this.textureCoordDimensions3 = value;
    }

    /**
     * Gets the value of the textureCoordDimensions4 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureCoordDimensions4() {
        if (textureCoordDimensions4 == null) {
            return "2";
        } else {
            return textureCoordDimensions4;
        }
    }

    /**
     * Sets the value of the textureCoordDimensions4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureCoordDimensions4(String value) {
        this.textureCoordDimensions4 = value;
    }

    /**
     * Gets the value of the textureCoordDimensions5 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureCoordDimensions5() {
        if (textureCoordDimensions5 == null) {
            return "2";
        } else {
            return textureCoordDimensions5;
        }
    }

    /**
     * Sets the value of the textureCoordDimensions5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureCoordDimensions5(String value) {
        this.textureCoordDimensions5 = value;
    }

    /**
     * Gets the value of the textureCoordDimensions6 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureCoordDimensions6() {
        if (textureCoordDimensions6 == null) {
            return "2";
        } else {
            return textureCoordDimensions6;
        }
    }

    /**
     * Sets the value of the textureCoordDimensions6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureCoordDimensions6(String value) {
        this.textureCoordDimensions6 = value;
    }

    /**
     * Gets the value of the textureCoordDimensions7 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextureCoordDimensions7() {
        if (textureCoordDimensions7 == null) {
            return "2";
        } else {
            return textureCoordDimensions7;
        }
    }

    /**
     * Sets the value of the textureCoordDimensions7 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextureCoordDimensions7(String value) {
        this.textureCoordDimensions7 = value;
    }

    public List<Vertex> getVertex() {
        if (vertex == null) {
            vertex = new ArrayList<Vertex>();
        }
        return this.vertex;
    }

}
