
package com.badlogic.gdx.tests.gles3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;

@GdxTestConfig(requireGL30 = true)
public class PixelBufferObjectTest extends GdxTest {

	private SpriteBatch batch;
	private Texture texture;
	protected Lock lock;
	protected Pixmap pixmap;
	protected int pixmapSizeBytes;
	protected boolean pixmapReady;
	protected boolean textureReady;
	protected boolean pboTransferComplete;
	protected Buffer mappedBuffer;
	protected int pboHandle;

	@Override
	public void create () {
		batch = new SpriteBatch();
		lock = new ReentrantLock();
		lock.lock();

		new Thread(new Runnable() {
			@Override
			public void run () {
				// load the pixmap in order to get header information
				pixmap = new Pixmap(Gdx.files.internal("data/badlogic.jpg"));
				pixmapSizeBytes = pixmap.getWidth() * pixmap.getHeight() * 3;
				pixmapReady = true;

				// wait for PBO initialization (need to be done in GLThread)
				lock.lock();

				// Transfer data from pixmap to PBO
				ByteBuffer data = pixmap.getPixels();
				data.rewind();
				BufferUtils.copy(data, mappedBuffer, pixmapSizeBytes);
				pboTransferComplete = true;
			}
		}).start();
	}

	@Override
	public void render () {
		// first step: once we get pixmap size, we can create both texture and PBO
		if (pixmapReady && texture == null) {
			texture = new Texture(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());

			pboHandle = Gdx.gl.glGenBuffer();
			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, pboHandle);
			Gdx.gl.glBufferData(GL30.GL_PIXEL_UNPACK_BUFFER, pixmapSizeBytes, null, GL30.GL_STREAM_DRAW);

			mappedBuffer = Gdx.gl30.glMapBufferRange(GL30.GL_PIXEL_UNPACK_BUFFER, 0, pixmapSizeBytes,
				GL30.GL_MAP_WRITE_BIT | GL30.GL_MAP_UNSYNCHRONIZED_BIT);
			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, 0);

			lock.unlock();
		}
		// second step: once async transfer is complete, we can transfer to the texture and cleanup
		if (!textureReady && pboTransferComplete) {

			// transfer data to texture (GL Thread)
			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, pboHandle);
			Gdx.gl30.glUnmapBuffer(GL30.GL_PIXEL_UNPACK_BUFFER);

			Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
			Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), null);
			Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);

			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, 0);

			// cleanup
			mappedBuffer = null;
			Gdx.gl.glDeleteBuffer(pboHandle);
			pboHandle = 0;
			pixmap.dispose();
			pixmap = null;
			textureReady = true;
		}
		// last step: texture is ready to be used.
		if (textureReady) {
			batch.begin();
			batch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();
		}
	}
}
