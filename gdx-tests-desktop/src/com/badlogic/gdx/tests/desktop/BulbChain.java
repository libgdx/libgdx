package com.badlogic.gdx.tests.desktop;

import java.util.ArrayList;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class BulbChain implements RenderListener, InputListener, Input {

    private static final String TAG = "LightBulb";

    /** the camera **/
    private OrthographicCamera camera;

    /** the immediate mode renderer to output our debug drawings **/
    private ImmediateModeRenderer renderer;

    private Texture backgroundTexture;
    private Texture bulbTexture;

    /** our box2D world **/
    private World world;

    /** our boxes **/
    private ArrayList<Body> links = new ArrayList<Body>();
    private Body bulbBody;

    private SpriteBatch batch;
    private Font font;

    private Color bulbColor = Color.WHITE;
    private Color chainColor = new Color(0.5f, 0.5f, 0.5f, 0);

    @Override
    public void dispose(Application app) {
        font.dispose();
        backgroundTexture.dispose();
    }

    @Override
    public void render(Application app) {
        // first we update the world. For simplicity
        // we use the delta time provided by the Graphics
        // instance. Normally you'll want to fix the time
        // step.
        world.step(app.getGraphics().getDeltaTime(), 3, 3);

        world.setGravity(new Vector2(-app.getInput().getAccelerometerX(), -10));
        GL10 gl = app.getGraphics().getGL10();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glViewport(0, 0, app.getGraphics().getWidth(), app.getGraphics()
                .getHeight());

        
       // HERE IT IS

        batch.begin();
        batch.draw(backgroundTexture, 0, app.getGraphics().getHeight(),
                backgroundTexture.getWidth(), backgroundTexture.getHeight(), 48,
                0, 48, 80, Color.WHITE, false, false);
        batch.end();

        camera.setMatrices();
        for (Body link : links) {
            chainColor.set(0.3f + Math.abs(link.getLinearVelocity().x / 30),
                    0.2f - Math.abs(link.getLinearVelocity().x / 30),
                    0.2f - Math.abs(link.getLinearVelocity().x / 30), 0.0f);
            renderBox(gl, link, 0.2f, 0.6f);
        }

        this.drawSolidCircle(gl, bulbBody.getPosition(), 5.0f, new Vector2(
                bulbBody.getPosition().x, bulbBody.getPosition().y - 10),
                bulbColor);
    }

    @Override
    public void surfaceChanged(Application app, int arg1, int arg2) {

    }

    @Override
    public void surfaceCreated(Application app) {

        camera = new OrthographicCamera(app.getGraphics());
        camera.setViewport(app.getGraphics().getWidth()/10, app.getGraphics()
                .getHeight()/10);
        camera.getPosition().set(0, 0, 0);

        // next we setup the immediate mode renderer
        renderer = new ImmediateModeRenderer(app.getGraphics().getGL10());

        batch = new SpriteBatch(app.getGraphics());

        font = app.getGraphics().newFont("Arial", 12, FontStyle.Plain, true);

        Pixmap pixmap = app.getGraphics().newPixmap(
                app.getFiles().getFileHandle("data/carbon.png",
                        FileType.Internal));
        backgroundTexture = app.getGraphics().newTexture(pixmap,
                TextureFilter.Linear, TextureFilter.Linear,
                TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true);
      
  // next we create out physics world.
        createPhysicsWorld();

        // finally we register ourselfs as an InputListener so we
        // can manipulate our world
        app.getInput().addInputListener(this);
    }

    private void createPhysicsWorld() {
        // we instantiate a new World with a proper gravity vector
        // and tell it to sleep when possible.
        world = new World(new Vector2(0, -500), true);

        createBulbOnChain(world);
    }

    private void createBulbOnChain(World world) {
        Body ground;

        {
            BodyDef bd = new BodyDef();
            ground = world.createBody(bd);
        }

        {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.2f, 0.6f);

            FixtureDef fd = new FixtureDef();
            fd.shape = shape;
            fd.density = 1.0f;
            fd.friction = 1.0f;
            fd.restitution = 0.0f;

            RevoluteJointDef jd = new RevoluteJointDef();
            jd.collideConnected = true;

            float y = 40.0f;
            Body prevBody = ground;

            for (int i = 0; i < 30; i++) {
                BodyDef bd = new BodyDef();
                bd.type = BodyType.DynamicBody;
                bd.position.set(0.1f, y - i * 1.5f);
                Body body = world.createBody(bd);
                body.createFixture(fd);

                Vector2 anchor = new Vector2(0.1f, y - i * 1.5f);
                jd.initialize(prevBody, body, anchor);
                world.createJoint(jd);
                prevBody = body;

                links.add(body);
            }

            shape.dispose();

            CircleShape bulbShape = new CircleShape();
            shape.setRadius(10.0f);
            fd.shape = bulbShape;
            fd.density = 100.0f;
            fd.restitution = 0.0f;

            BodyDef bd = new BodyDef();
            bd.type = BodyType.DynamicBody;
            bd.position.set(0.0f, y - 30 * 1.5f - 5.0f);
            bulbBody = world.createBody(bd);
            bulbBody.createFixture(fd);

            Vector2 anchor = new Vector2(0.0f, y - 30 * 1.5f - 5.0f);
            jd.initialize(prevBody, bulbBody, anchor);
            world.createJoint(jd);

            bulbShape.dispose();

        }

    }

    private void renderBox(GL10 gl, Body body, float halfWidth, float halfHeight) {
        // push the current matrix and
        // get the bodies center and angle in world coordinates
        gl.glPushMatrix();

        Vector2 pos = body.getWorldCenter();
        float angle = body.getAngle();

        // set the translation and rotation matrix
        gl.glTranslatef(pos.x, pos.y, 0);
        gl.glRotatef((float) Math.toDegrees(angle), 0, 0, 1);

        // render the box
        renderer.begin(GL10.GL_LINE_STRIP);
        renderer.color(chainColor.r, chainColor.g, chainColor.b, 0);
        renderer.vertex(-halfWidth, -halfHeight, 0);
        renderer.color(chainColor.r, chainColor.g, chainColor.b, 0);
        renderer.vertex(-halfWidth, halfHeight, 0);
        renderer.color(chainColor.r, chainColor.g, chainColor.b, 0);
        renderer.vertex(halfWidth, halfHeight, 0);
        renderer.color(chainColor.r, chainColor.g, chainColor.b, 0);
        renderer.vertex(halfWidth, -halfHeight, 0);
        renderer.color(chainColor.r, chainColor.g, chainColor.b, 0);
        renderer.vertex(-halfWidth, -halfHeight, 0);
        renderer.end();

        renderer.begin(GL10.GL_LINE_STRIP);
        renderer.vertex(0, -halfHeight, 0);
        renderer.color(0.5f, 0.5f, 0.5f, 0);
        renderer.vertex(0, -halfHeight - 0.5f, 0);
        renderer.end();

        // pop the matrix
        gl.glPopMatrix();
    }

    private final Vector2 v = new Vector2();

    private void drawSolidCircle(GL10 gl, Vector2 center, float radius,
            Vector2 axis, Color color) {

        renderer.begin(GL10.GL_LINE_LOOP);
        float angle = 0;
        float angleInc = 2 * (float) Math.PI / 40;
        for (int i = 0; i < 40; i++, angle += angleInc) {
            v.set((float) Math.cos(angle) * radius + center.x,
                    (float) Math.sin(angle) * radius + center.y);
            renderer.color(color.r, color.g, color.b, color.a);
            renderer.vertex(v.x, v.y, 0);
        }
        renderer.end();
        /*
         * renderer.begin(GL10.GL_LINES); renderer.color(color.r, color.g,
         * color.b, color.a); renderer.vertex(center.x, center.y, 0);
         * renderer.color(color.r, color.g, color.b, color.a);
         * renderer.vertex(center.x + axis.x * radius, center.y + axis.y *
         * radius, 0); renderer.end();
         */
    }

    @Override
    public void addInputListener(InputListener arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getAccelerometerX() {
        return 0;
    }

    @Override
    public float getAccelerometerY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getAccelerometerZ() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void getTextInput(TextInputListener arg0, String arg1, String arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isAccelerometerAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isKeyPressed(int arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTouched() {
        return false;
    }

    @Override
    public void removeInputListener(InputListener arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean keyDown(int arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyTyped(char arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyUp(int arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDown(int arg0, int arg1, int arg2) {
        if (bulbColor == Color.WHITE)
            bulbColor = new Color(1, 1, 0, 0);
        else
            bulbColor = Color.WHITE;
        return false;
    }

    @Override
    public boolean touchDragged(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchUp(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return false;
    }
    
    public static void main( String[] argv )
    {
    	JoglApplication app = new JoglApplication( "Bulb test", 480, 800, false );
    	app.getGraphics().setRenderListener( new BulbChain() );
    	
    }

}