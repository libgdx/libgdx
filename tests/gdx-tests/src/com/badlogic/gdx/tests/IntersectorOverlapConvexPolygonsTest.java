package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class IntersectorOverlapConvexPolygonsTest extends GdxTest {

    private static final String TAG = IntersectorOverlapConvexPolygonsTest.class.getSimpleName();
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    private float triangleWidth = 10f;
    private float triangleHeight = 10f;
    //2 triangle polygons intersect at 0,10 - 10,10 will return the wrong direction
    private float[] vertsTriangle1 = {0f, 0f, triangleWidth, 0f, triangleWidth, triangleHeight};
    private float[] vertsTriangle2 = {0f, 0f, triangleWidth*2, 0f, triangleWidth*2, triangleHeight*2};

    private Polygon triangle1 = new Polygon();
    private Polygon triangle2 = new Polygon();

    private Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();

    private Vector3 mouseCoords = new Vector3();

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.position.set(0, 0, 0);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        //set inital position
        triangle1.setVertices(vertsTriangle1);
        triangle2.setVertices(vertsTriangle2);
        triangle1.setPosition(0, 0);
        triangle2.setPosition(10, 0);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = 80;
        camera.viewportHeight = 60;
    }

    private void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Intersector.overlapConvexPolygons(triangle1, triangle2, mtv);
            float x = triangle1.getX() + (mtv.normal.x * mtv.depth);
            float y = triangle1.getY() + (mtv.normal.y * mtv.depth);
            triangle1.setPosition(x, y);
            Gdx.app.debug(TAG, mtv.normal + " " + mtv.depth);
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            mouseCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseCoords);
            triangle1.setPosition(mouseCoords.x, mouseCoords.y);
            boolean overlaps = Intersector.overlapConvexPolygons(triangle1, triangle2, mtv);
            Gdx.app.debug(TAG, mtv.normal + " " + mtv.depth + " overlaps: " + overlaps + " " + mouseCoords);
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            triangle1.rotate(90);
            boolean overlaps = Intersector.overlapConvexPolygons(triangle1, triangle2, mtv);
            Gdx.app.debug(TAG, mtv.normal + " " + mtv.depth + " overlaps: " + overlaps);
        }
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());
        camera.update();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.polygon(triangle1.getTransformedVertices());
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.polygon(triangle2.getTransformedVertices());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(mtv.normal.x, mtv.normal.y, mtv.normal.x * 10, mtv.normal.y * 10);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle((mtv.normal.x), (mtv.normal.y), 0.5f);
        shapeRenderer.end();
    }
}
