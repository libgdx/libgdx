package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 
public class LightingGame implements ApplicationListener {
        private OrthographicCamera camera;
        private SpriteBatch batch;
       
        private Texture light;
        private Texture background;
       
        private FrameBuffer lightMap;
        private TextureRegion fboRegion;
 
        private ShaderProgram shadowShader;
        private ShaderProgram defaultShader;
       
        @Override
        public void create() {
               
                camera = new OrthographicCamera(1024/2, 300);
                camera.position.set(1024/4, 300/2, 0);
                camera.update();
               
                batch = new SpriteBatch();
               
                background = new Texture(Gdx.files.internal("data/tiles.png"));
                light = new Texture(Gdx.files.internal("data/light.png"));
               
                lightMap = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
               
                fboRegion = new TextureRegion(lightMap.getColorBufferTexture(), 0, 0, lightMap.getWidth(), lightMap.getHeight());
                fboRegion.flip(false, true);
               
                setupShaders();
        }
       
        public void setupShaders(){
                defaultShader = SpriteBatch.createDefaultShader();
               
                String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                                + "uniform mat4 u_projTrans;\n" //
                                + "varying vec4 v_color;\n" //
                                + "varying vec2 v_texCoords;\n" //
                                + "\n" //
                                + "void main()\n" //
                                + "{\n" //
                                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
                                + "}\n";
                String fragmentShader = "#ifdef GL_ES\n" //
                                + "#define LOWP lowp\n" //
                                + "precision mediump float;\n" //
                                + "#else\n" //
                                + "#define LOWP \n" //
                                + "#endif\n" //
                                + "varying LOWP vec4 v_color;\n" //
                                + "varying vec2 v_texCoords;\n" //
                                + "uniform sampler2D u_texture;\n"
                                + "uniform sampler2D u_texture1;\n"
                                + "uniform vec2 lightMapResolution;\n"
                                + "void main()\n"//
                                + "{\n" //
                                + "  vec4 texColor0 = texture2D(u_texture, v_texCoords); \n"
                                + "  vec4 texColor1 = texture2D(u_texture1, gl_FragCoord.xy / lightMapResolution); \n"
                                + "  vec4 shadow = texColor0 * vec4(0.5, 0.5, 0.5, 1.0);\n"
                                + "  vec4 light = texColor0 * vec4(texColor1.rgb, 1.0);\n"
                                + "  gl_FragColor = mix(shadow, light, texColor1.a);"
                                + "}";
 
                shadowShader = new ShaderProgram(vertexShader, fragmentShader);
                if (!shadowShader.isCompiled()) {
                        System.err.println(shadowShader.getLog());
                        System.exit(0);
                }
                if (shadowShader.getLog().length()!=0)
                        System.out.println(shadowShader.getLog());
        }
 
        @Override
        public void dispose() {
                batch.dispose();
        }
 
        @Override
        public void render() {         
                // Draw lightMap
               
               
                lightMap.begin();
               
                Gdx.gl.glClearColor(0, 0, 0, 0.0f);
                Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
                batch.setShader(defaultShader);
                batch.begin();
                
                batch.setColor(Color.RED);
                batch.draw(light, Gdx.input.getX()-light.getWidth()/2f, Gdx.graphics.getHeight()-Gdx.input.getY()-light.getHeight()/2f);
                batch.end();
               
                batch.flush();
               
                lightMap.end();
                batch.setColor(Color.WHITE);
                
                // Draw normal stuff
                Gdx.gl.glClearColor(0, 0, 0, 0);
                Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
               
                batch.setProjectionMatrix(camera.combined);
               
                batch.setShader(shadowShader);
                batch.begin();
                shadowShader.setUniformf("lightMapResolution", lightMap.getWidth(), lightMap.getHeight());
                shadowShader.setUniformi("u_texture1", 1);
               
                lightMap.getColorBufferTexture().bind(1);
                background.bind(0);
                
                batch.draw(background, 0, 0);
       
                batch.end();
        }
 
        @Override
        public void resize(int width, int height) {
                camera.setToOrtho(false, width, height);
               
        }
 
        @Override
        public void pause() {
        }
 
        @Override
        public void resume() {
        }
}