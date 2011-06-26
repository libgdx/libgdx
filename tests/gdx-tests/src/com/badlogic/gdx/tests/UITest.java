package com.badlogic.gdx.tests;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageToggleButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.ToggleButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class UITest extends GdxTest {
	
	
	String[] listEntries = { "This is a list entry", "And another one", "The meaning of life", "Is hard to come by",
									 "This is a list entry", "And another one", "The meaning of life", "Is hard to come by",
									 "This is a list entry", "And another one", "The meaning of life", "Is hard to come by",
									 "This is a list entry", "And another one", "The meaning of life", "Is hard to come by",
									 "This is a list entry", "And another one", "The meaning of life", "Is hard to come by"
	};
	
	Skin skin;	
	Stage ui;
	SpriteBatch batch;
	Actor root;
	
	@Override
	public void create() {
		batch = new SpriteBatch();						
		skin = new Skin(Gdx.files.internal("data/uiskin.xml"), Gdx.files.internal("data/uiskin.png"));
		skin.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		TextureRegion image = new TextureRegion(new Texture(Gdx.files.internal("data/badlogicsmall.jpg")));
		TextureRegion image2 = new TextureRegion(new Texture(Gdx.files.internal("data/badlogic.jpg")));
		ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);					
		Gdx.input.setInputProcessor(ui);
		
		Window window = skin.newWindow("window", ui, "Dialog", 320, 240);		
		window.x = window.y = 0;				
		
		final Button button = skin.newButton("button-sl", "Single");		
		final ToggleButton buttonMulti = skin.newToggleButton("button-ml-tgl", "Multi\nLine\nToggle");
		final ImageButton imgButton = skin.newImageButton("button-img", image);
		final ImageToggleButton imgToggleButton = skin.newImageToggleButton("button-img-tgl", image);
		final CheckBox checkBox = skin.newCheckBox("checkbox", "Check me");
		final Slider slider = skin.newSlider("slider", 100, 0, 10, 1);
		final TextField textfield = skin.newTextField("textfield", 100);		
		final ComboBox combobox = skin.newComboBox("combo", new String[] {"Android", "Windows", "Linux", "OSX"}, ui);
		final Image imageActor = new Image("image", image2);
		final ScrollPane scrollPane = skin.newScrollPane("scroll", ui, imageActor, 100, 100);		
		final List list = skin.newList("list", listEntries);
		final ScrollPane scrollPane2 = skin.newScrollPane("scroll2", ui, list, 100, 100);
		final SplitPane splitPane = skin.newSplitPane("split", ui, scrollPane, scrollPane2, false, 0, 0, "default-horizontal");
		final Label label = skin.newLabel("label", "fps:");
		
		window.row().fill(true, true).expand(true, false).spacingBottom(10);
		window.add(button);
		window.add(buttonMulti);			
		window.add(imgButton);
		window.add(imgToggleButton);
		window.row().spacingBottom(10);
		window.add(checkBox);
		window.add(slider).fill(true, false).colspan(3);
		window.row().spacingBottom(10);		
		window.add(combobox);			
		window.add(textfield).expand(true, false).fill(true, false).colspan(3);
		window.row().spacingBottom(10);
		window.add(splitPane).fill(true, true).expand(true, true).colspan(4);
		window.row();
		window.add(label);
		
		textfield.setTextFieldListener(new TextFieldListener() {			
			@Override public void keyTyped (TextField textField, char key) {
				if(key == '\n') textField.getOnscreenKeyboard().show(false);
			}
		});
		
		ui.addActor(window);
	}

	Vector2 point = new Vector2();
	@Override
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		((Label)ui.findActor("label")).setText("fps: " + Gdx.graphics.getFramesPerSecond());
		
		ui.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		ui.draw();
		Table.drawDebug(ui);
	}
	
	@Override
	public void resize(int width, int height) {
		ui.setViewport(width, height, false);		
	}
	
	@Override public boolean needsGL20 () {
		return false;
	}
}