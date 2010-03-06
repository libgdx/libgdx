package com.badlogic.gdx.math;

import java.io.Serializable;


public final class Quaternion implements Serializable
{   
	private static final long serialVersionUID = -7661875440774897168L;
	final float[] val = new float[4];
    final float[] tmp = new float[4];
    boolean dirty = true;
    
    public final void setX(float a_x)
    {
        val[0]=a_x;
        dirty=true;
    }

    public final float getX()
    {
        return val[0];
    }

    public final void setY(float a_y)
    {
        val[1]=a_y;
        dirty=true;
    }

    public final float getY()
    {
        return val[1];
    }

    public final void setZ(float a_z)
    {
        val[2]=a_z;
        dirty=true;
    }

    public final float getZ()
    {
        return val[2];
    }

    public final void setW(float a_w)
    {
        val[3]=a_w;
        dirty=true;
    }

    public final float getW()
    {
        return val[3];
    }

    public Quaternion()
    {
    }

    public Quaternion(float a_x, float a_y, float a_z, float a_w)
    {
        this.set(a_x,a_y,a_z,a_w);
    }

    public Quaternion(Quaternion a_qut)
    {
        this.set(a_qut);
    }

    public Quaternion(Vector a_axs, float a_ang)
    {
        this.set(a_axs,a_ang);
    }

    public Quaternion set(float a_x, float a_y, float a_z, float a_w)
    {
        val[0]=a_x;
        val[1]=a_y;
        val[2]=a_z;
        val[3]=a_w;
        dirty=true;
        return this;
    }

    public Quaternion set(Quaternion a_qut)
    {
        return this.set(a_qut.val[0],a_qut.val[1],a_qut.val[2],a_qut.val[3]);
    }

    public Quaternion set(Vector a_axs, float a_ang)
    {
        float l_ang= (float)(a_ang *(Math.PI/180));
        float l_sin = (float)(Math.sin(l_ang/2));
        float l_cos = (float)(Math.cos(l_ang/2));
        return this.set(a_axs.getX()*l_sin,
                        a_axs.getY()*l_sin,
                        a_axs.getZ()*l_sin,
                        l_cos).nor();
    }

    public Quaternion cpy()
    {
        return new Quaternion(this);
    }

    public float len()
    {
        return (float)Math.sqrt(val[0]*val[0]+val[1]*val[1]+val[2]*val[2]+val[3]*val[3]);
    }

    public Quaternion nor()
    {
        float l_len=this.len();
        return this.set(val[0]/l_len,
                        val[1]/l_len,
                        val[2]/l_len,
                        val[3]/l_len);
    }

    public String toString()
    {
        return "["+val[0]+"|"+val[1]+"|"+val[2]+"|"+val[3]+"]";
    }

}
