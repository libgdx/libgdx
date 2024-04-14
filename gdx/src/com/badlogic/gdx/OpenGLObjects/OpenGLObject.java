package com.badlogic.gdx.OpenGLObjects;

public interface OpenGLObject {
    // Interface used to broadly categorize objects made with OpenGL.

    int getHandle(); // Primary handle of the object. Return -1 or something if it doesn't have one.

    int[] getHandles(); // In cases where multiple handles are used in the object, like ShaderProgram.

    int getType(); // Type of object, like GL_(object type)
}
