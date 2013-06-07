package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.graphics.g3d.materials.Material.Attribute;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class FloatAttribute extends Material.Attribute {
    public static final String ShininessAlias = "shininess";
    public static final long Shininess = register(ShininessAlias);

    public static FloatAttribute createShininess(float value) {
        return new FloatAttribute(Shininess, value);
    }

    public static final String AlphaTestAlias = "alphaTest";
    public static final long AlphaTest = register(AlphaTestAlias);

    public static FloatAttribute createAlphaTest(float value) {
        return new FloatAttribute(AlphaTest, value);
    }

    public float value;

    public FloatAttribute(long type) {
        super(type);
    }

    public FloatAttribute(long type, float value) {
        super(type);
        this.value = value;
    }

    @Override
    public Attribute copy () {
        return new FloatAttribute(type, value);
    }

    @Override
    protected boolean equals (Attribute other) {
        // FIXME use epsilon?
        return ((FloatAttribute)other).value == value;
    }
}
