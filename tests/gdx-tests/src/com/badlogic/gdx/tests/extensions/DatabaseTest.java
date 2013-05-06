
package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.tests.utils.GdxTest;

public class DatabaseTest extends GdxTest {

	Database dbHandler;

	public static final String TABLE_COMMENTS = "comments";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_COMMENT = "comment";

	private static final String DATABASE_NAME = "comments.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table if not exists " + TABLE_COMMENTS + "(" + COLUMN_ID
		+ " integer primary key autoincrement, " + COLUMN_COMMENT + " text not null);";

	private Stage stage;
	private TextButton textButton;
	private Label statusLabel;
	private Skin skin;

	@Override
	public void create () {
		Gdx.app.log("DatabaseTest", "creation started");
		dbHandler = DatabaseFactory.getNewDatabase(DATABASE_NAME, DATABASE_VERSION, DATABASE_CREATE, null);

		dbHandler.setupDatabase();
		try {
			dbHandler.openOrCreateDatabase();
			dbHandler.execSQL(DATABASE_CREATE);
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		Gdx.app.log("DatabaseTest", "created successfully");

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		Gdx.input.setInputProcessor(stage);

		statusLabel = new Label("", skin);
		statusLabel.setWrap(true);
		statusLabel.setWidth(Gdx.graphics.getWidth() * 0.96f);
		statusLabel.setAlignment(Align.center);
		statusLabel.setPosition(Gdx.graphics.getWidth() * 0.5f - statusLabel.getWidth() * 0.5f, 30f);
		stage.addActor(statusLabel);

		textButton = new TextButton("Insert Data", skin);
		textButton.setPosition(Gdx.graphics.getWidth() * 0.5f - textButton.getWidth() * 0.5f, 60f);
		textButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				try {
					dbHandler.execSQL("INSERT INTO comments ('comment') VALUES ('This is a test comment')");
				} catch (SQLiteGdxException e) {
					e.printStackTrace();
				}

				DatabaseCursor cursor = null;

				try {
					cursor = dbHandler.rawQuery("SELECT * FROM comments");
				} catch (SQLiteGdxException e) {
					e.printStackTrace();
				}

				while (cursor.next()) {
					statusLabel.setText(String.valueOf(cursor.getInt(0)));
				}

				try {
					cursor = dbHandler.rawQuery(cursor, "SELECT * FROM comments");
				} catch (SQLiteGdxException e) {
					e.printStackTrace();
				}
			}
		});
		stage.addActor(textButton);

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void dispose () {
		try {
			dbHandler.closeDatabase();
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
		dbHandler = null;
		Gdx.app.log("DatabaseTest", "dispose");
	}
}
