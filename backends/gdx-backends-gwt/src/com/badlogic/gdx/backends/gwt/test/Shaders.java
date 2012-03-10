package com.badlogic.gdx.backends.gwt.test;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;

public interface Shaders extends ClientBundle {

  public static Shaders INSTANCE = GWT.create(Shaders.class);

  @Source(value = {"fragment-shader.txt"})
  TextResource fragmentShader();

  @Source(value = {"vertex-shader.txt"})
  TextResource vertexShader();

  @Source("sky.png")
  ImageResource sky();
}
