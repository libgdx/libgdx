
package com.dozingcatsoftware.bouncy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.bouncy.elements.Box2DFactory;
import com.dozingcatsoftware.bouncy.elements.DropTargetGroupElement;
import com.dozingcatsoftware.bouncy.elements.FieldElement;
import com.dozingcatsoftware.bouncy.elements.FlipperElement;
import com.dozingcatsoftware.bouncy.elements.RolloverGroupElement;
import com.dozingcatsoftware.bouncy.elements.SensorElement;
import com.dozingcatsoftware.bouncy.fields.Field1Delegate;

public class Field implements ContactListener {

	FieldLayout layout;
	World world;

	List<Body> layoutBodies;
	List<Body> balls;
	List<Body> ballsAtTargets;

	// allow access to model objects from Box2d bodies
	Map<Body, FieldElement> bodyToFieldElement;
	Map<String, FieldElement> fieldElements;
	Map<String, List<FieldElement>> elementsByGroupID = new HashMap<String, List<FieldElement>>();
	FieldElement[] fieldElementsToTick;

	Random RAND = new Random();

	long gameTime;
	PriorityQueue<ScheduledAction> scheduledActions;

	Delegate delegate;

	GameState gameState = new GameState();
	GameMessage gameMessage;

	// interface to allow custom behavior for various game events
	public static interface Delegate {
		public void gameStarted (Field field);

		public void ballLost (Field field);

		public void gameEnded (Field field);

		public void tick (Field field, long msecs);

		public void processCollision (Field field, FieldElement element, Body hitBody, Body ball);

		public void flipperActivated (Field field);

		public void allDropTargetsInGroupHit (Field field, DropTargetGroupElement targetGroup);

		public void allRolloversInGroupActivated (Field field, RolloverGroupElement rolloverGroup);

		public void ballInSensorRange (Field field, SensorElement sensor);
	}

	// helper class to represent actions scheduled in the future
	static class ScheduledAction implements Comparable<ScheduledAction> {
		Long actionTime;
		Runnable action;

		@Override
		public int compareTo (ScheduledAction another) {
			// sort by action time so these objects can be added to a PriorityQueue in the right order
			return actionTime.compareTo(another.actionTime);
		}
	}

	/** Creates Box2D world, reads layout definitions for the given level (currently only one), and initializes the game to the
	 * starting state.
	 * @param level */
	public void resetForLevel (int level) {
		Vector2 gravity = new Vector2(0.0f, -1.0f);
		boolean doSleep = true;
		world = new World(gravity, doSleep);
		world.setContactListener(this);

		layout = FieldLayout.layoutForLevel(level, world);
		world.setGravity(new Vector2(0.0f, -layout.getGravity()));
		balls = new ArrayList<Body>();
		ballsAtTargets = new ArrayList<Body>();

		scheduledActions = new PriorityQueue<ScheduledAction>();
		gameTime = 0;

		// map bodies and IDs to FieldElements, and get elements on whom tick() has to be called
		bodyToFieldElement = new HashMap<Body, FieldElement>();
		fieldElements = new HashMap<String, FieldElement>();
		List<FieldElement> tickElements = new ArrayList<FieldElement>();

		for (FieldElement element : layout.getFieldElements()) {
			if (element.getElementID() != null) {
				fieldElements.put(element.getElementID(), element);
			}
			for (Body body : element.getBodies()) {
				bodyToFieldElement.put(body, element);
			}
			if (element.shouldCallTick()) {
				tickElements.add(element);
			}
		}
		fieldElementsToTick = tickElements.toArray(new FieldElement[0]);

		delegate = new Field1Delegate();
//		String delegateClass = layout.getDelegateClassName();
//		if (delegateClass != null) {
//			if (delegateClass.indexOf('.') == -1) {
//				delegateClass = "com.dozingcatsoftware.bouncy.fields." + delegateClass;
//			}
//			try {
//				delegate = (Delegate)Class.forName(delegateClass).newInstance();
//			} catch (Exception ex) {
//				throw new RuntimeException(ex);
//			}
//		} else {
//			// use no-op delegate if no class specified, so that field.getDelegate() is always non-null
//			delegate = new BaseFieldDelegate();
//		}
	}

	public void startGame () {
		gameState.setTotalBalls(layout.getNumberOfBalls());
		gameState.startNewGame();
		getDelegate().gameStarted(this);
	}

	/** Returns the FieldElement with the given value for its "id" attribute, or null if there is no such element. */
	public FieldElement getFieldElementByID (String elementID) {
		return fieldElements.get(elementID);
	}

	/** Called to advance the game's state by the specified number of milliseconds. iters is the number of times to call the Box2D
	 * World.step method; more iterations produce better accuracy. After updating physics, processes element collisions, calls
	 * tick() on every FieldElement, and performs scheduled actions. */
	void tick (long msecs, int iters) {
		float dt = (msecs / 1000.0f) / iters;

		for (int i = 0; i < iters; i++) {
			clearBallContacts();
			world.step(dt, 10, 10);
			processBallContacts();
		}

		gameTime += msecs;
		processElementTicks();
		processScheduledActions();
		processGameMessages();

		getDelegate().tick(this, msecs);
	}

	/** Calls the tick() method of every FieldElement in the layout. */
	void processElementTicks () {
		int size = fieldElementsToTick.length;
		for (int i = 0; i < size; i++) {
			fieldElementsToTick[i].tick(this);
		}
	}

	/** Runs actions that were scheduled with scheduleAction and whose execution time has arrived. */
	void processScheduledActions () {
		while (true) {
			ScheduledAction nextAction = scheduledActions.peek();
			if (nextAction != null && gameTime >= nextAction.actionTime) {
				scheduledActions.poll();
				nextAction.action.run();
			} else {
				break;
			}
		}
	}

	/** Schedules an action to be run after the given interval in milliseconds has elapsed. Interval is in game time, not real time. */
	public void scheduleAction (long interval, Runnable action) {
		ScheduledAction sa = new ScheduledAction();
		sa.actionTime = gameTime + interval;
		sa.action = action;
		scheduledActions.add(sa);
	}

	/** Launches a new ball. The position and velocity of the ball are controlled by the "launch" key in the field layout JSON. */
	public Body launchBall () {
		List<Number> position = layout.getLaunchPosition();
		List<Float> velocity = layout.getLaunchVelocity();
		float radius = layout.getBallRadius();

		Body ball = Box2DFactory.createCircle(world, position.get(0).floatValue(), position.get(1).floatValue(), radius, false);
		ball.setBullet(true);
		ball.setLinearVelocity(new Vector2(velocity.get(0), velocity.get(1)));
		this.balls.add(ball);
		return ball;
	}

	/** Removes a ball from play. If this results in no balls remaining on the field, calls doBallLost. */
	public void removeBall (Body ball) {
		world.destroyBody(ball);
		this.balls.remove(ball);
		if (this.balls.size() == 0) {
			this.doBallLost();
		}
	}

	/** Called when a ball has ended. Ends the game if that was the last ball, otherwise updates GameState to the next ball. Shows a
	 * game message to indicate the ball number or game over. */
	public void doBallLost () {
		boolean hasExtraBall = (this.gameState.getExtraBalls() > 0);
		this.gameState.doNextBall();
		// display message for next ball or game over
		String msg = null;
		if (hasExtraBall)
			msg = "Shoot Again";
		else if (this.gameState.isGameInProgress()) msg = "Ball " + this.gameState.getBallNumber();

		if (msg != null) {
			// game is still going, show message after delay
			final String msg2 = msg; // must be final for closure, yay Java
			this.scheduleAction(1500, new Runnable() {
				public void run () {
					showGameMessage(msg2, 1500);
				}
			});
		} else {
			endGame();
		}

		getDelegate().ballLost(this);
	}

	/** Returns true if there are active elements in motion. Returns false if there are no active elements, indicating that tick()
	 * can be called with larger time steps, less frequently, or not at all. */
	public boolean hasActiveElements () {
		// HACK: to allow flippers to drop properly at beginning of game, we need accurate simulation
		if (this.gameTime < 500) return true;
		// allow delegate to return true even if there are no balls?
		return this.getBalls().size() > 0;
	}

	ArrayList<Body> deadBalls = new ArrayList<Body>();

	/** Removes balls that are not in play, currently defined as those having a y position of less than 1. */
	public void removeDeadBalls () {
		int len = balls.size();
		deadBalls.clear();
		for (int i = 0; i < len; i++) {
			Body ball = this.balls.get(i);
			if (ball.getPosition().y < 1) {
				world.destroyBody(ball);
				deadBalls.add(ball);
			}
		}

		len = deadBalls.size();
		for (int i = 0; i < len; i++) {
			this.balls.remove(deadBalls.get(i));
		}
	}

	/** Called by FieldView to draw the balls currently in play. */
	public void drawBalls (IFieldRenderer renderer) {
		List<Integer> color = layout.getBallColor();
		int len = balls.size();
		for (int i = 0; i < len; i++) {
			Body ball = balls.get(i);
			CircleShape shape = (CircleShape)ball.getFixtureList().get(0).getShape();
			renderer.fillCircle(ball.getPosition().x, ball.getPosition().y, shape.getRadius(), color.get(0), color.get(1),
				color.get(2));
		}
	}

	/** Called to engage or disengage all flippers. If called with an argument of true, and all flippers were not previously
	 * engaged, calls the flipperActivated methods of all field elements and the field's delegate. */
	public void setAllFlippersEngaged (boolean engaged) {
		boolean allFlippersPreviouslyActive = true;
		for (FlipperElement flipper : this.getFlipperElements()) {
			if (allFlippersPreviouslyActive && !flipper.isFlipperEngaged()) allFlippersPreviouslyActive = false;
			flipper.setFlipperEngaged(engaged);
		}

		if (engaged && !allFlippersPreviouslyActive) {
			for (FieldElement element : this.getFieldElements()) {
				element.flipperActivated(this);
			}
			getDelegate().flipperActivated(this);
		}
	}

	/** Ends a game in progress by removing all balls in play, calling setGameInProgress(false) on the GameState, and setting a
	 * "Game Over" message for display by the score view. */
	public void endGame () {
		int len = balls.size();
		for (int i = 0; i < len; i++) {
			world.destroyBody(balls.get(i));
		}
		balls.clear();
		this.getGameState().setGameInProgress(false);
		this.showGameMessage("Game Over", 2500);
		getDelegate().gameEnded(this);
	}

	/** Adjusts gravity in response to the device being tilted; not currently used. */
	public void receivedOrientationValues (float azimuth, float pitch, float roll) {
		double angle = roll - Math.PI / 2;
		float gravity = layout.getGravity();
		float gx = (float)(gravity * Math.cos(angle));
		float gy = -Math.abs((float)(gravity * Math.sin(angle)));
		world.setGravity(new Vector2(gx, gy));
	}

	// contact support
	Map<Body, List<Fixture>> ballContacts = new HashMap();

	void clearBallContacts () {
		ballContacts.clear();
	}

	/** Called after Box2D world step method, to notify FieldElements that the ball collided with. */
	void processBallContacts () {
		int len = balls.size();
		for (int i = 0; i < len; i++) {
			Body ball = balls.get(i);
			if (ball.getUserData() == null) continue;

			List<Fixture> fixtures = (List<Fixture>)ball.getUserData();
			int len2 = fixtures.size();
			for (int j = 0; j < len2; j++) {
				Fixture f = fixtures.get(j);
				FieldElement element = bodyToFieldElement.get(f.getBody());
				if (element != null) {
					element.handleCollision(ball, f.getBody(), this);
					if (delegate != null) {
						delegate.processCollision(this, element, f.getBody(), ball);
					}
					this.gameState.addScore(element.getScore());
				}
			}
			fixtures.clear();
		}
	}

	// ContactListener methods
	@Override
	public void beginContact (Contact contact) {
		// nothing here, contact is recorded in endContact()
	}

	@Override
	public void endContact (Contact contact) {
		// A ball can have multiple contacts (e.g. against two walls), so store list of contacted fixtures
		Body ball = null;
		Fixture fixture = null;
		if (balls.contains(contact.getFixtureA().getBody())) {
			ball = contact.getFixtureA().getBody();
			fixture = contact.getFixtureB();
		}
		if (balls.contains(contact.getFixtureB().getBody())) {
			ball = contact.getFixtureB().getBody();
			fixture = contact.getFixtureA();
		}
		if (ball != null) {
			List<Fixture> fixtures = (List<Fixture>)ball.getUserData();
			if (fixtures == null) {
				ball.setUserData(fixtures = new ArrayList<Fixture>());
			}
			fixtures.add(fixture);
		}
	}

	// end ContactListener methods

	/** Displays a message in the score view for the specified duration in milliseconds. Duration is in real world time, not
	 * simulated game time. */
	public void showGameMessage (String text, long duration) {
		gameMessage = new GameMessage();
		gameMessage.text = text;
		gameMessage.duration = duration;
		gameMessage.creationTime = System.currentTimeMillis();
	}

	// updates time remaining on current game message
	void processGameMessages () {
		if (gameMessage != null) {
			if (System.currentTimeMillis() - gameMessage.creationTime > gameMessage.duration) {
				gameMessage = null;
			}
		}
	}

	/** Adds the given value to the game score. The value is multiplied by the GameState's current multipler. */
	public void addScore (long s) {
		gameState.addScore(s);
	}

	// accessors
	public float getWidth () {
		return layout.getWidth();
	}

	public float getHeight () {
		return layout.getHeight();
	}

	public List<Body> getLayoutBodies () {
		return layoutBodies;
	}

	public List<Body> getBalls () {
		return balls;
	}

	public List<FlipperElement> getFlipperElements () {
		return layout.getFlipperElements();
	}

	public List<FieldElement> getFieldElements () {
		return layout.getFieldElements();
	}

	public GameMessage getGameMessage () {
		return gameMessage;
	}

	public GameState getGameState () {
		return gameState;
	}

	public long getGameTime () {
		return gameTime;
	}

	public float getTargetTimeRatio () {
		return layout.getTargetTimeRatio();
	}

	public World getBox2DWorld () {
		return world;
	}

	public Delegate getDelegate () {
		return delegate;
	}

	@Override
	public void preSolve (Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSolve (Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub

	}
}
