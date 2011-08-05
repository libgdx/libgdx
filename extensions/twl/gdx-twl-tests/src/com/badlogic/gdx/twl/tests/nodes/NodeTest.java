package com.badlogic.gdx.twl.tests.nodes;

import java.nio.FloatBuffer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.twl.TWL;
import com.badlogic.gdx.utils.BufferUtils;

import de.matthiasmann.twl.ScrollPane;

public class NodeTest implements ApplicationListener, InputProcessor
{
	private TWL twl;
	private InputMultiplexer input = new InputMultiplexer();
	
	private NodeArea nodeArea;
	private Node nodeSource;
	private Node nodeSink;
	
	private boolean doubleClick = true;
	
	public NodeTest(boolean doubleClick, int radius)
	{
		super();
		this.doubleClick = doubleClick;
		Pad.RADIUS = radius;
	}
	
	@Override
	public void create()
	{
		nodeArea = new NodeArea(doubleClick);
        ScrollPane scrollPane = new ScrollPane(nodeArea);
        scrollPane.setExpandContentSize(true);
		
        SpriteBatch batch = new SpriteBatch();
		twl = new TWL(batch, "data/nodes.xml", FileType.Internal, scrollPane);
		input.addProcessor(twl);
		input.addProcessor(this);
		Gdx.input.setInputProcessor(twl);
		
		nodeSource = nodeArea.addNode("Source");
		Pad nodeSourceColor = nodeSource.addPad("Output 1", false);
        Pad nodeSourceAlpha = nodeSource.addPad("Alpha", false);
        
        nodeSink = nodeArea.addNode("Sink");
        Pad nodeSinkColor = nodeSink.addPad("Input 1", true);
        nodeArea.addConnection(nodeSourceColor, nodeSinkColor);
	}
	@Override
	public void resume()
	{
	}
	@Override
	public void render()
	{
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		twl.render();
	}
	@Override
	public void resize(int width, int height)
	{
        nodeSource.setPosition((int)(width * 0.2f), (int)(height * 0.3f));
        nodeSource.adjustSize();

        nodeSink.setPosition((int)(width * 0.75f), (int)(height * 0.6f));
        nodeSink.adjustSize();
	}
	@Override
	public void pause()
	{
	}
	@Override
	public void dispose()
	{
		twl.dispose();
	}
	public boolean keyDown (int keycode) {
		return false;
	}

	public boolean keyUp (int keycode) {
		return false;
	}

	public boolean keyTyped (char character) {
		return false;
	}

	public boolean touchDown (int x, int y, int pointer, int button) {
		System.out.println("This touch made it through and was not handled by TWL.");
		return false;
	}

	public boolean touchUp (int x, int y, int pointer, int button) {
		return false;
	}

	public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}
}
