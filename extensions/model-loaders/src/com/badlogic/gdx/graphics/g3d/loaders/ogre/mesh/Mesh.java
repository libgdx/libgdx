
package com.badlogic.gdx.graphics.g3d.loaders.ogre.mesh;

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
    "sharedgeometry",
    "submeshes",
    "skeletonlink",
    "boneassignments",
    "levelofdetail",
    "submeshnames",
    "poses",
    "animations",
    "extremes"
})
@XmlRootElement(name = "mesh")
public class Mesh {

    protected Sharedgeometry sharedgeometry;
    @XmlElement(required = true)
    protected Submeshes submeshes;
    protected Skeletonlink skeletonlink;
    protected Boneassignments boneassignments;
    protected Levelofdetail levelofdetail;
    protected Submeshnames submeshnames;
    protected Poses poses;
    protected Animations animations;
    protected Extremes extremes;

    /**
     * Gets the value of the sharedgeometry property.
     * 
     * @return
     *     possible object is
     *     {@link Sharedgeometry }
     *     
     */
    public Sharedgeometry getSharedgeometry() {
        return sharedgeometry;
    }

    /**
     * Sets the value of the sharedgeometry property.
     * 
     * @param value
     *     allowed object is
     *     {@link Sharedgeometry }
     *     
     */
    public void setSharedgeometry(Sharedgeometry value) {
        this.sharedgeometry = value;
    }

    /**
     * Gets the value of the submeshes property.
     * 
     * @return
     *     possible object is
     *     {@link Submeshes }
     *     
     */
    public Submeshes getSubmeshes() {
        return submeshes;
    }

    /**
     * Sets the value of the submeshes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Submeshes }
     *     
     */
    public void setSubmeshes(Submeshes value) {
        this.submeshes = value;
    }

    /**
     * Gets the value of the skeletonlink property.
     * 
     * @return
     *     possible object is
     *     {@link Skeletonlink }
     *     
     */
    public Skeletonlink getSkeletonlink() {
        return skeletonlink;
    }

    /**
     * Sets the value of the skeletonlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link Skeletonlink }
     *     
     */
    public void setSkeletonlink(Skeletonlink value) {
        this.skeletonlink = value;
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

    /**
     * Gets the value of the levelofdetail property.
     * 
     * @return
     *     possible object is
     *     {@link Levelofdetail }
     *     
     */
    public Levelofdetail getLevelofdetail() {
        return levelofdetail;
    }

    /**
     * Sets the value of the levelofdetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link Levelofdetail }
     *     
     */
    public void setLevelofdetail(Levelofdetail value) {
        this.levelofdetail = value;
    }

    /**
     * Gets the value of the submeshnames property.
     * 
     * @return
     *     possible object is
     *     {@link Submeshnames }
     *     
     */
    public Submeshnames getSubmeshnames() {
        return submeshnames;
    }

    /**
     * Sets the value of the submeshnames property.
     * 
     * @param value
     *     allowed object is
     *     {@link Submeshnames }
     *     
     */
    public void setSubmeshnames(Submeshnames value) {
        this.submeshnames = value;
    }

    /**
     * Gets the value of the poses property.
     * 
     * @return
     *     possible object is
     *     {@link Poses }
     *     
     */
    public Poses getPoses() {
        return poses;
    }

    /**
     * Sets the value of the poses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Poses }
     *     
     */
    public void setPoses(Poses value) {
        this.poses = value;
    }

    /**
     * Gets the value of the animations property.
     * 
     * @return
     *     possible object is
     *     {@link Animations }
     *     
     */
    public Animations getAnimations() {
        return animations;
    }

    /**
     * Sets the value of the animations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Animations }
     *     
     */
    public void setAnimations(Animations value) {
        this.animations = value;
    }

    /**
     * Gets the value of the extremes property.
     * 
     * @return
     *     possible object is
     *     {@link Extremes }
     *     
     */
    public Extremes getExtremes() {
        return extremes;
    }

    /**
     * Sets the value of the extremes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Extremes }
     *     
     */
    public void setExtremes(Extremes value) {
        this.extremes = value;
    }

}
