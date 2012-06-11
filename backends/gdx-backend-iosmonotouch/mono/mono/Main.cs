using System;
using System.Collections.Generic;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;
using com.badlogic.gdx;
using com.badlogic.gdx.math;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.graphics.glutils;
using java.io;
using java.nio;
using System.IO;

namespace mono
{
	public class TestListener: com.badlogic.gdx.ApplicationListener {
			ShaderProgram shader;
		Mesh mesh;
		Texture texture;
		Matrix4 matrix = new Matrix4();
	
		public void create () {
			String vertexShader = "attribute vec4 a_position;    \n" + 
										 "attribute vec4 a_color;\n" +
										 "attribute vec2 a_texCoord0;\n" + 
										 "uniform mat4 u_worldView;\n" + 
										 "varying vec4 v_color;" + 
										 "varying vec2 v_texCoords;" + 
										 "void main()                  \n" + 
										 "{                            \n" + 
										 "   v_color = vec4(1, 1, 1, 1); \n" + 
										 "   v_texCoords = a_texCoord0; \n" + 
										 "   gl_Position =  u_worldView * a_position;  \n"	+ 
										 "}                            \n";
			String fragmentShader = "#ifdef GL_ES\n" +
										 	"precision mediump float;\n" + 
										 	"#endif\n" + 
										 	"varying vec4 v_color;\n" + 
										 	"varying vec2 v_texCoords;\n" + 
										 	"uniform sampler2D u_texture;\n" + 
										 	"void main()                                  \n" + 
										 	"{                                            \n" + 
										 	"  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
										 	+ "}";
	
			shader = new ShaderProgram(vertexShader, fragmentShader);
			if (shader.isCompiled() == false) {
				Gdx.app.log("ShaderTest", shader.getLog());
				Gdx.app.exit();
			}
	 
			mesh = new Mesh(Mesh.VertexDataType.VertexArray, true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
			mesh.setVertices(new float[] {-0.5f, -0.5f, 0, 1, 1, 1, 1, 0, 1, 
													 0.5f, -0.5f, 0, 1, 1, 1, 1, 1, 1, 
													 0.5f, 0.5f, 0, 1, 1, 1, 1, 1, 0, 
													 -0.5f, 0.5f, 0, 1, 1, 1, 1, 0, 0});
			mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
			texture = new Texture(Gdx.files.@internal("data/badlogic.jpg"));
		}
	
		Vector3 axis = new Vector3(0, 0, 1);
		float angle = 0;
	
		public void render () {
			angle += Gdx.graphics.getDeltaTime() * 45;
			matrix.setToRotation(axis, angle);
	
			Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			Gdx.graphics.getGL20().glClearColor(0.2f, 0.2f, 0.2f, 1);
			Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
			Gdx.graphics.getGL20().glEnable(GL20.GL_TEXTURE_2D);
			Gdx.graphics.getGL20().glEnable(GL10.GL_BLEND);
			Gdx.graphics.getGL20().glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			texture.bind();
			shader.begin();
			shader.setUniformMatrix("u_worldView", matrix);
			shader.setUniformi("u_texture", 0);
			mesh.render(shader, GL10.GL_TRIANGLES);
			shader.end();
		}
		
		public void dispose () {
			mesh.dispose();
			texture.dispose();
			shader.dispose();
		}
		
		public void resume() {
			Gdx.app.log("Test", "resumed");
		}
		
		public void pause() {
			Gdx.app.log("Test", "paused");
		}
		
		public void resize(int width, int height) {
			Gdx.app.log("Test", "resized");
		}
	}
	
	
	public class Application
	{
		[Register ("AppDelegate")]
		public partial class AppDelegate : UIApplicationDelegate {
			public override bool FinishedLaunching (UIApplication app, NSDictionary options) {
				new com.badlogic.gdx.backends.ios.IOSApplication(app, new TestListener());
				return true;
			}
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}