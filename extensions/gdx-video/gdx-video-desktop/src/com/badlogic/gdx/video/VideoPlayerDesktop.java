package com.badlogic.gdx.video;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.video.VideoDecoder.VideoDecoderBuffers;

/**
 * Desktop implementation of the VideoPlayer
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
public class VideoPlayerDesktop
implements VideoPlayer {

	//@formatter:off
	private static final String vertexShader =
			"attribute vec4 a_position;    \n" +
					"attribute vec2 a_texCoord0;\n" +
					"uniform mat4 u_worldView;\n" +
					"varying vec2 v_texCoords;" +
					"void main()                  \n" +
					"{                            \n" +
					"   v_texCoords = a_texCoord0; \n" +
					"   gl_Position =  u_worldView * a_position;  \n"      +
					"}                            \n" ;
	private static final String fragmentShader =
			"varying vec2 v_texCoords;\n" +
					"uniform sampler2D u_texture;\n" +
					"void main()                                  \n" +
					"{                                            \n" +
					"  gl_FragColor = texture2D(u_texture, v_texCoords);\n"+
					"}";

	//@formatter:on

	Viewport viewport;
	Camera cam;

	VideoDecoder decoder;
	Pixmap image;
	Texture texture;
	RawMusic audio;
	long startTime = 0;
	boolean showAlreadyDecodedFrame = false;

	FileInputStream inputStream;
	FileChannel fileChannel;

	boolean paused = false;
	long timeBeforePause = 0;

	ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);

	Mesh mesh;
	boolean customMesh = false;

	int currentVideoWidth, currentVideoHeight;
	VideoSizeListener sizeListener;
	CompletionListener completionListener;
	FileHandle currentFile;

	boolean playing = false;

	public VideoPlayerDesktop() {
		this(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
	}

	public VideoPlayerDesktop(Viewport viewport) {
		this.viewport = viewport;

		mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
		mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });

		cam = viewport.getCamera();
	}

	public VideoPlayerDesktop(Camera cam, Mesh mesh) {
		this.cam = cam;
		this.mesh = mesh;
		customMesh = true;
	}

	@Override
	public boolean play(FileHandle file) throws FileNotFoundException {
		if (file == null) {
			return false;
		}
		if (!file.exists()) {
			throw new FileNotFoundException("Could not find file: " + file.path());
		}

		currentFile = file;

		if (!FfMpeg.isLoaded()) {
			FfMpeg.loadLibraries();
		}

		if (decoder != null) {
			// Do all the cleanup
			stop();
		}

		inputStream = new FileInputStream(file.file());
		fileChannel = inputStream.getChannel();

		decoder = new VideoDecoder();
		VideoDecoderBuffers buffers = null;
		try {
			buffers = decoder.loadStream(this, "readFileContents");

			if (buffers != null) {
				ByteBuffer audioBuffer = buffers.getAudioBuffer();
				audio = new RawMusic(decoder, audioBuffer, buffers.getAudioChannels(), buffers.getAudioSampleRate());
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (sizeListener != null) {
			sizeListener.onVideoSize(currentVideoWidth, currentVideoHeight);
		}

		image = new Pixmap(buffers.getVideoWidth(), buffers.getVideoHeight(), Format.RGB888);

		float x = -buffers.getVideoWidth() / 2;
		float y = -buffers.getVideoHeight() / 2;
		float width = buffers.getVideoWidth();
		float height = buffers.getVideoHeight();

		//@formatter:off
		mesh.setVertices(new float[] {x, y, 0, 0, 1,
		                              x+width, y, 0, 1, 1,
		                              x+width, y + height, 0, 1, 0,
		                              x, y + height, 0, 0, 0});
		//@formatter:on

		viewport.setWorldSize(width, height);
		playing = true;
		return true;
	}

	@Override
	public void resize(int width, int height) {
		if (!customMesh) {
			viewport.update(width, height);
		}
	}

	/**
	 * Called by jni to fill in the file buffer.
	 *
	 * @param buffer
	 *            The buffer that needs to be filled
	 * @return The amount that has been filled into the buffer.
	 */
	@SuppressWarnings("unused")
	private int readFileContents(ByteBuffer buffer) {
		try {
			buffer.rewind();
			return fileChannel.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean render() {
		if (decoder != null && !paused) {
			if (startTime == 0) {
				// Since startTime is 0, this means that we should now display the first frame of the video, and set the
				// time.
				startTime = System.currentTimeMillis();
				if (audio != null) {
					audio.play();
				}
			}

			if (!showAlreadyDecodedFrame) {
				ByteBuffer videoData = decoder.nextVideoFrame();
				if (videoData != null) {

					ByteBuffer data = image.getPixels();
					data.rewind();
					data.put(videoData);
					data.rewind();
					if (texture != null) {
						texture.dispose();
					}
					texture = new Texture(image);
				} else {
					if (completionListener != null) {
						completionListener.onCompletionListener(currentFile);
					}
					playing = false;
					return false;
				}
			}

			showAlreadyDecodedFrame = false;
			long currentFrameTimestamp = (long) (decoder.getCurrentFrameTimestamp() * 1000);
			long currentVideoTime = (System.currentTimeMillis() - startTime);
			int difference = (int) (currentFrameTimestamp - currentVideoTime);
			if (difference > 20) {
				// Difference is more than a frame, draw this one twice
				showAlreadyDecodedFrame = true;
			}

			texture.bind();
			shader.begin();
			shader.setUniformMatrix("u_worldView", cam.combined);
			shader.setUniformi("u_texture", 0);
			mesh.render(shader, GL20.GL_TRIANGLES);
			shader.end();

		}
		return true;
	}

	/**
	 * Will return whether the buffer is filled. At the time of writing, the buffer used can store 10 frames of video.
	 * You can find the value in jni/VideoDecoder.h
	 *
	 * @return whether buffer is filled.
	 */
	@Override
	public boolean isBuffered() {
		if (decoder != null) {
			return decoder.isBuffered();
		}
		return false;
	}

	@Override
	public void stop() {
		playing = false;

		if (audio != null) {
			audio.dispose();
			audio = null;
		}
		if (texture != null) {
			texture.dispose();
			texture = null;
		}
		if (image != null) {
			image.dispose();
			image = null;
		}
		if (decoder != null) {
			decoder.dispose();
			decoder = null;
		}
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			inputStream = null;
		}

		startTime = 0;
		showAlreadyDecodedFrame = false;
	}

	@Override
	public void pause() {
		if (!paused) {
			paused = true;
			audio.pause();
			timeBeforePause = System.currentTimeMillis() - startTime;
		}
	}

	@Override
	public void resume() {
		if (paused) {
			paused = false;
			audio.play();
			startTime = System.currentTimeMillis() - timeBeforePause;
		}
	}

	@Override
	public void dispose() {
		stop();

		if (!customMesh && mesh != null) {
			mesh.dispose();
		}
	}

	@Override
	public void setOnVideoSizeListener(VideoSizeListener listener) {
		sizeListener = listener;
	}

	@Override
	public void setOnCompletionListener(CompletionListener listener) {
		completionListener = listener;
	}

	@Override
	public int getVideoWidth() {
		return currentVideoWidth;
	}

	@Override
	public int getVideoHeight() {
		return currentVideoHeight;
	}

	@Override
	public boolean isPlaying() {
		return playing;
	}
}
