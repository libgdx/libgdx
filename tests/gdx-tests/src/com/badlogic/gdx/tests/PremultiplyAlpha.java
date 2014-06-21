package com.badlogic.gdx.tests;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.bullet.linearmath.int4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

public class PremultiplyAlpha extends GdxTest{
	
	public static final String VERTEX_SHADER = 
		"attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
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
		+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
		+ "}\n";
	public static final String FRAG_SHADER = 
		"#ifdef GL_ES\n" //
		+ "#define LOWP lowp\n" //
		+ "precision mediump float;\n" //
		+ "#else\n" //
		+ "#define LOWP \n" //
		+ "#endif\n" //
		+ "varying LOWP vec4 v_color;\n" //
		+ "varying vec2 v_texCoords;\n" //
		+ "uniform sampler2D u_texture;\n" //
		+ "void main()\n"//
		+ "{\n" //
		+ "	vec4 color = texture2D(u_texture, v_texCoords)*v_color;\n" +
			"	gl_FragColor.rgb = color.rgb*color.a;\n" +
			"	gl_FragColor.a = color.a;\n"
		+ "}";
	
	@Override
	public void create () {
		Pixmap.setBlending(Blending.None);
		Pixmap.setFilter(Filter.NearestNeighbour);
		String 	in = "C:\\Users\\Inferno\\Documents\\backup OSX\\elflab\\assets\\text_in\\debug\\particle.png",
					out = "C:\\Users\\Inferno\\Documents\\backup OSX\\elflab\\assets\\text_in\\debug\\pre_particle.png";
		System.out.println("Converting "+in);
		imageIOPremultiplyAlpha(in, out);
		//cpuPremultiplyAlpha(in, out);
		//gpuPremultiplyAlpha(in, out);
		System.out.println("Conversion done!");
	}
	
	private void cpuPremultiplyAlpha(String in, String out){
		try {
			BufferedImage image = ImageIO.read(Gdx.files.absolute(in).file());
			BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
			outImage.createGraphics().drawImage(image, 0, 0, null);
			ImageIO.write(outImage, "png", Gdx.files.absolute(out).file());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void imageIOPremultiplyAlpha(String in, String out){
		try {
			BufferedImage image = ImageIO.read(Gdx.files.absolute(in).file());
			//BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
			BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			float[] color = new float[4];
			WritableRaster raster = image.getRaster();
			WritableRaster outRaster = outImage.getRaster();
			for(int x =0, w = image.getWidth(); x< w; ++x)
				for(int y =0, h = image.getHeight(); y< h; ++y){
					raster.getPixel(x, y, color);
					float alpha = color[3]/255f;
					for(int i=0;i < 3; ++i) 
						color[i] *= alpha;
					outRaster.setPixel(x, y, color);
				}
			//outImage.createGraphics().drawImage(image, 0, 0, null);
			ImageIO.write(outImage, "png", Gdx.files.absolute(out).file());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void gpuPremultiplyAlpha (String in, String out) {
		Texture texture = new Texture(Gdx.files.absolute(in));
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		FrameBuffer buffer = new FrameBuffer(Format.RGBA8888, texture.getWidth(), texture.getHeight(), false);
		buffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		ShaderProgram shader = new ShaderProgram(VERTEX_SHADER, FRAG_SHADER);
		Gdx.app.log("Log", shader.getLog());
		SpriteBatch batch = new SpriteBatch(10);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, texture.getWidth(), texture.getHeight());
		batch.disableBlending();
		batch.setShader(shader);
		
		//Premultiply
		buffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(texture, 0, 0);
		batch.end();
		Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, texture.getWidth(), texture.getHeight());
		buffer.end();

		//Save
		PixmapIO.writePNG(Gdx.files.absolute(out), pixmap);
		buffer.dispose();
		texture.dispose();
		pixmap.dispose();
		Gdx.app.exit();
	}
}
