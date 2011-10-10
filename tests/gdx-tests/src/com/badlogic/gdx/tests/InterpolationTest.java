
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class InterpolationTest extends GdxTest {
	static private final String[] interpolators = new String[] {"back", "backIn", "backOut", "bounce", "bounceIn", "bounceOut",
		"circle", "circleIn", "circleOut", "elastic", "elasticIn", "elasticOut", "exp", "expIn", "expOut", "fade", "linear",
		"pow2", "pow2In", "pow2Out", "pow3", "pow3In", "pow3Out", "pow4", "pow4In", "pow4Out", "pow5", "pow5In", "pow5Out", "sine",
		"sineIn", "sineOut"};

	private Stage stage;
	private Table root;
	private List list;
	private ShapeRenderer renderer;
	Vector2 position = new Vector2(300, 20);
	Vector2 targetPosition = new Vector2(position);
	Vector2 temp = new Vector2();
	float timer;

	public void create () {
		renderer = new ShapeRenderer();

		stage = new Stage(0, 0, true);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
			public boolean touchDown (int x, int y, int pointer, int button) {
				Vector2 current = getCurrentPosition();
				position.set(current);
				targetPosition.set(x - 10, Gdx.graphics.getHeight() - y - 10);
				timer = 0;
				return true;
			}
		}));

		root = new Table();
		stage.addActor(root);
		root.pad(10).top().left();

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));

		list = new List(interpolators, skin);
		root.add(new FlickScrollPane(list, stage)).expandY().fillY().prefWidth((int)list.getPrefWidth());
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, true);
		root.width = width;
		root.height = height;
		root.invalidate();
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		int steps = 100;
		int size = 200;
		int x = Gdx.graphics.getWidth() / 2 - size / 2;
		int y = Gdx.graphics.getHeight() / 2 - size / 2;

		renderer.setProjectionMatrix(stage.getCamera().combined);

		renderer.begin(ShapeType.Box);
		renderer.box(x, y, 0, size, size, 0);
		renderer.end();

		Interpolation interpolation = getInterpolation();
		float lastX = x, lastY = y;
		renderer.begin(ShapeType.Line);
		for (int i = 0; i <= steps; i++) {
			float alpha = i / (float)steps;
			float lineX = x + size * alpha;
			float lineY = y + size * interpolation.apply(alpha);
			renderer.line(lastX, lastY, lineX, lineY);
			lastX = lineX;
			lastY = lineY;
		}
		renderer.end();

		timer += Gdx.graphics.getDeltaTime();
		Vector2 current = getCurrentPosition();
		renderer.begin(ShapeType.FilledRectangle);
		renderer.filledRect(current.x, current.y, 20, 20);
		renderer.end();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	Vector2 getCurrentPosition () {
		temp.set(targetPosition);
		temp.sub(position);
		temp.mul(getInterpolation().apply(Math.min(1, timer / 1f)));
		temp.add(position);
		return temp;
	}

	private Interpolation getInterpolation () {
		try {
			return (Interpolation)Interpolation.class.getField(list.getSelection()).get(null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public boolean needsGL20 () {
		return false;
	}
}
