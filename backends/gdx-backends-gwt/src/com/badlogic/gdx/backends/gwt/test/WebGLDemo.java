package com.badlogic.gdx.backends.gwt.test;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.typedarrays.client.Float32Array;
import com.google.gwt.typedarrays.client.Int32Array;
import com.google.gwt.user.client.Timer;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderbuffer;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;
import com.google.gwt.webgl.client.WebGLUtil;

public class WebGLDemo implements EntryPoint {

  private interface EventHandler {
    void onEvent(NativeEvent e);
  }

  private static final int CANVAS_WIDTH = 640;
  private static final int CANVAS_HEIGHT = 480;

  private static native ImageElement createImage() /*-{
    return new Image();
  }-*/;

  private static native void hookOnLoad(ImageElement img, EventHandler h) /*-{
    img.addEventListener('load', function(e) {
      h.@com.badlogic.gdx.backends.gwt.test.WebGLDemo.EventHandler::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
    }, false);
  }-*/;

  private WebGLBuffer buffer;
  private WebGLBuffer indexBuffer;
  private WebGLProgram shaderProgram;
  private float[] projectionMatrix;
  private float[] modelViewMatrix;

  protected CanvasElement canvas;
  protected WebGLRenderingContext gl;
  private WebGLTexture birdTexture;
  private WebGLFramebuffer fbuf;

  @Override
  public void onModuleLoad() {
    canvas = Document.get().createElement("canvas").cast();
    Document.get().getBody().appendChild(canvas);
    canvas.setWidth(CANVAS_WIDTH);
    canvas.setHeight(CANVAS_HEIGHT);
    
    gl = WebGLRenderingContext.getContext(canvas);
    gl.viewport(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

    init();

    new Timer() {
      @Override
      public void run() {
        draw();
      }
    }.scheduleRepeating(60);
  }

  private void drawToFramebuffer() {
    gl.bindFramebuffer(FRAMEBUFFER, fbuf);
    gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT);
    gl.bindTexture(TEXTURE_2D, birdTexture);
    drawQuad();
    gl.bindTexture(TEXTURE_2D, null);
    gl.bindFramebuffer(FRAMEBUFFER, null);
  }

  private void draw() {
    gl.bindFramebuffer(FRAMEBUFFER, null);
    gl.clearColor((float)Math.random(), 0, 0, 1);
    gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT);
    gl.bindTexture(TEXTURE_2D, birdTexture);
    drawQuad();
    gl.bindTexture(TEXTURE_2D, null);
  }

  private void drawQuad() {
    // Use the sole shader program.
    gl.useProgram(shaderProgram);

    // Projection matrix.
    WebGLUniformLocation pUniform = gl.getUniformLocation(shaderProgram, "projectionMatrix");
    gl.uniformMatrix4fv(pUniform, false, projectionMatrix);

    // Model View matrix.
    WebGLUniformLocation mvUniform = gl.getUniformLocation(shaderProgram, "modelViewMatrix");
    gl.uniformMatrix4fv(mvUniform, false, modelViewMatrix);

    // Bird texture.
    gl.activeTexture(TEXTURE0);
    gl.uniform1i(gl.getUniformLocation(shaderProgram, "texture"), 0);

    // Vertices (position, texCoord)
    gl.bindBuffer(ARRAY_BUFFER, buffer);

    int vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "vertexPosition");
    gl.vertexAttribPointer(vertexPositionAttribute, 3, FLOAT, false, 20, 0);
    gl.enableVertexAttribArray(vertexPositionAttribute);

    int texCoordAttribute = gl.getAttribLocation(shaderProgram, "texCoord");
    gl.vertexAttribPointer(texCoordAttribute, 2, FLOAT, false, 20, 12);
    gl.enableVertexAttribArray(texCoordAttribute);

    // Elements.
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, indexBuffer);
    gl.drawArrays(TRIANGLE_STRIP, 0, 4);
  }

  private void init() {
    // Basic stuff.
    gl.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.enable(TEXTURE_2D);
    gl.enable(BLEND);
    gl.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA);

    // Projection matrix.
    //projectionMatrix = WebGLUtil.createPerspectiveMatrix(45, 1.0f, 0.1f, 100);

    projectionMatrix= new float[] {
        2f/CANVAS_WIDTH,0,0,0,
        0,2f/CANVAS_HEIGHT,0,0,
        0,0,1,0,
        0,0,0,1,
    };

    // Model View matrix.
    modelViewMatrix = new float[] {
        1,0,0,0,
        0,1,0,0,
        0,0,1,0,
        0,0,0,1,
    };

    // Bird texture.
    birdTexture = createTexture("/sky.png");

    // Compile the shader.
    shaderProgram = WebGLUtil.createShaderProgram(gl,
      Shaders.INSTANCE.vertexShader().getText(),
      Shaders.INSTANCE.fragmentShader().getText()
    );

    // Create quad buffers.
    initVertexBuffer();
  }

  private WebGLTexture createTexture() {
    WebGLTexture tex = gl.createTexture();
    gl.bindTexture(TEXTURE_2D, tex);
    gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
    gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR);
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE);
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE);
    return tex;
  }

  private WebGLFramebuffer createFramebuffer(int width, int height) {
    WebGLTexture tex = createTexture();

    fbuf = gl.createFramebuffer();
    gl.texImage2D(TEXTURE_2D, 0, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, null);

    WebGLRenderbuffer rbuf = gl.createRenderbuffer();
    gl.bindRenderbuffer(RENDERBUFFER, rbuf);
    gl.renderbufferStorage(RENDERBUFFER, RGBA4, width, height);

    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, tex, 0);
    gl.framebufferRenderbuffer(FRAMEBUFFER, DEPTH_ATTACHMENT, RENDERBUFFER, rbuf);

    gl.bindTexture(TEXTURE_2D, null);
    gl.bindRenderbuffer(RENDERBUFFER, null);
    gl.bindFramebuffer(FRAMEBUFFER, null);

    return fbuf;
  }

  private WebGLTexture createTexture(String url) {
    // Create the texture object.
    final WebGLTexture tex = gl.createTexture();

    // Load the image.
    final ImageElement img = createImage();
    img.setSrc(Shaders.INSTANCE.sky().getSafeUri().asString());
    hookOnLoad(img, new EventHandler() {
      @Override
      public void onEvent(NativeEvent e) {
        // Load image data into the texture object once it's loaded.
        gl.bindTexture(TEXTURE_2D, tex);
        gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, img);
        gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
        gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR);
        gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE);
        gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE);
        gl.bindTexture(TEXTURE_2D, null);
      }
    });

    return tex;
  }

  private void initVertexBuffer() {
    // Create the vertex buffer.
    buffer = gl.createBuffer();
    gl.bindBuffer(ARRAY_BUFFER, buffer);

    float z = 0f; // doesn't matter really; [-1, 1] all in front of the camera
    // attribute vec3 vertexPosition;
    float[] vertices = new float[] {
       -42/2f,-42/2f, z, 0, 1,
       42/2f,-42/2f,z, 1, 1,
       -42/2f,42/2f,z, 0, 0,
       42/2f,42/2f,z, 1, 0
   };
   gl.bufferData(ARRAY_BUFFER, Float32Array.create(vertices), STREAM_DRAW);

    // create the index buffer.
    int[] indices = new int[] { 0, 1, 2, 3 };
    indexBuffer = gl.createBuffer();
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, indexBuffer);
    gl.bufferData(ELEMENT_ARRAY_BUFFER, Int32Array.create(indices), STREAM_DRAW);
  }
}
