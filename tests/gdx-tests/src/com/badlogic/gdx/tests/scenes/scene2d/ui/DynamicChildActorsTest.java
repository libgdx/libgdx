/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.tests.scenes.scene2d.ui.DynamicChildActorsTest.FlashingLabel.FlashingLabelStyle;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Demonstrates the new g2d.ui interfaces.
 * @author Daniels118 */
public class DynamicChildActorsTest extends GdxTest {
	private SpriteBatch batch;
	private Stage stage;

	@Override
	public void create () {
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		batch = new SpriteBatch();
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		Table table = new Table(skin);
		table.setFillParent(true);
		stage.addActor(table);
		
		table.add("Default components").left().colspan(2);
		table.row();
		
		Label label1 = new Label("Label 1", skin);
		table.add(label1);
		TextButton textButton1 = new TextButton("TextButton 1", skin);
		table.add(textButton1);
		
		table.row();
		
		table.add("Custom components created from code").left().colspan(2);
		table.row();
		
		// Create a FlashingLabelStyle from the default LabelStyle defined in uiskin.json
		FlashingLabelStyle labelStyle = new FlashingLabelStyle(skin.get(LabelStyle.class));
		
		FlashingLabel label2 = new FlashingLabel("Label2", labelStyle);
		table.add(label2);
		TextButtonStyle textButtonStyle = skin.get("default", TextButtonStyle.class);
		textButtonStyle.labelStyle = labelStyle;
		TextButton textButton2 = new TextButton("TextButton 2", textButtonStyle);
		table.add(textButton2);
		
		table.row();
		
		table.add("Custom styles can also be defined in the json.").left().colspan(2);
	}

	@Override
	public void render () {
		float delta = Gdx.graphics.getDeltaTime();
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		stage.act(delta);
		stage.draw();
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		stage.dispose();
	}
	
	public static class FlashingLabel extends Label {
		private float time = 0;
		
		public FlashingLabel (CharSequence text, Skin skin, String styleName) {
			super(text, skin.get(styleName, FlashingLabelStyle.class));
		}
		
		public FlashingLabel (CharSequence text, FlashingLabelStyle style) {
			super(text, style);
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			FlashingLabelStyle style = (FlashingLabelStyle) getStyle();
			time += Gdx.graphics.getDeltaTime();
			if (time >= style.period) time -= style.period;
			if (time <= style.period * style.duty) super.draw(batch, parentAlpha);
		}
		
		static public class FlashingLabelStyle extends LabelStyle {
			public float period = 1f;
			public float duty = 0.5f;

			public FlashingLabelStyle () {
			}
			
			public FlashingLabelStyle (BitmapFont font, Color fontColor) {
				super(font, fontColor);
			}
			
			public FlashingLabelStyle (LabelStyle style) {
				super(style);
			}
			
			public FlashingLabelStyle (FlashingLabelStyle style) {
				super(style);
				period = style.period;
				duty = style.duty;
			}
			
			public FlashingLabel createLabel (CharSequence text) {
				return new FlashingLabel(text, this);
			}
			
			public FlashingLabelStyle copy() {
				return new FlashingLabelStyle(this);
			}
		}
	}
}
