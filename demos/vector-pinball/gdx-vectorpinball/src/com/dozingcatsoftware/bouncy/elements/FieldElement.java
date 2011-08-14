
package com.dozingcatsoftware.bouncy.elements;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.bouncy.Field;
import com.dozingcatsoftware.bouncy.IFieldRenderer;

/** Abstract superclass of all elements in the pinball field, such as walls, bumpers, and flippers.
 * @author brian */

public abstract class FieldElement {

	Map parameters;
	World box2dWorld;
	String elementID;
	int[] color; // 3-element r,g,b values between 0 and 255

	int flashCounter = 0; // when >0, inverts colors (e.g. after being hit by the ball), decrements in tick()
	long score = 0;

	// default wall color shared by WallElement, WallArcElement, WallPathElement
	static int DEFAULT_WALL_RED = 64;
	static int DEFAULT_WALL_GREEN = 64;
	static int DEFAULT_WALL_BLUE = 160;

	/** Creates and returns a FieldElement object from the given map of parameters. The default class to instantiate is an argument
	 * to this method, and can be overridden by the "class" property of the parameter map. Calls the no-argument constructor of the
	 * default or custom class, and then calls initialize() passing the parameter map and World. */
	public static FieldElement createFromParameters (Map params, World world, Class defaultClass) {
		try {
			if (params.containsKey("class")) {
				// if package not specified, use this package
				String className = (String)params.get("class");
				if (className.indexOf('.') == -1) {
					className = "com.dozingcatsoftware.bouncy.elements." + className;
				}
				defaultClass = Class.forName(className);
			}

			FieldElement self = (FieldElement)defaultClass.newInstance();
			self.initialize(params, world);
			return self;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Extracts common values from the definition parameter map, and calls finishCreate to allow subclasses to further initialize
	 * themselves. Subclasses should override finishCreate, and should not override this method. */
	public void initialize (Map params, World world) {
		this.parameters = params;
		this.box2dWorld = world;
		this.elementID = (String)params.get("id");

		List<Integer> colorList = (List<Integer>)params.get("color");
		if (colorList != null) {
			this.color = new int[] {colorList.get(0), colorList.get(1), colorList.get(2)};
		}

		if (params.containsKey("score")) {
			this.score = ((Number)params.get("score")).longValue();
		}

		this.finishCreate(params, world);
	}

	/** Called after creation to determine if tick() needs to be called after every frame is simulated. Default returns false,
	 * subclasses must override to return true in order for tick() to be called. This is an optimization to avoid needless method
	 * calls in the game loop. */
	public boolean shouldCallTick () {
		return false;
	}

	/** Called on every update from Field.tick. Default implementation decrements flash counter if active, subclasses can override
	 * to perform additional processing, e.g. RolloverGroupElement checking for balls within radius of rollovers. Subclasses should
	 * call super.tick(field). */
	public void tick (Field field) {
		if (flashCounter > 0) flashCounter--;
	}

	/** Called when the player activates one or more flippers. The default implementation does nothing; subclasses can override. */
	public void flipperActivated (Field field) {

	}

	/** Causes the colors returned by red/blue/greenColorComponent methods to be inverted for the given number of frames. This can
	 * be used to flash an element when it is hit by a ball, see PegElement. */
	public void flashForFrames (int frames) {
		flashCounter = frames;
	}

	/** Must be overridden by subclasses, which should perform any setup required after creation. */
	public abstract void finishCreate (Map params, World world);

	/** Must be overridden by subclasses to return a collection of all Box2D bodies which make up this element. */
	public abstract Collection<Body> getBodies ();

	/** Must be overridden by subclasses to draw the element, using IFieldRenderer methods. */
	public abstract void draw (IFieldRenderer renderer);

	/** Called when a ball collides with a Body in this element. The default implementation does nothing (allowing objects to bounce
	 * off each other normally), subclasses can override (e.g. to apply extra force) */
	public void handleCollision (Body ball, Body bodyHit, Field field) {
	}

	/** Returns this element's ID as specified in the JSON definition, or null if the ID is not specified. */
	public String getElementID () {
		return elementID;
	}

	/** Returns the parameter map from which this element was created. */
	public Map getParameters () {
		return parameters;
	}

	/** Returns the "score" value for this element. The score is automatically added when the element is hit by a ball, and elements
	 * may apply scores under other conditions, e.g. RolloverGroupElement adds the score when a ball comes within range of a
	 * rollover. */
	public long getScore () {
		return score;
	}

	// look in optional "color" parameter, use default value if not present. Invert if flashCounter>0
	protected int colorComponent (int index, int defvalue) {
		int value = defvalue;
		if (this.color != null) value = this.color[index];
		return (flashCounter > 0) ? 255 - value : value;
	}

	/** Returns the red component of this element's base color, taken from the "color" parameter. If there is no color parameter,
	 * the default argument is returned. */
	protected int redColorComponent (int defvalue) {
		return colorComponent(0, defvalue);
	}

	/** Returns the green component of this element's base color, taken from the "color" parameter. If there is no color parameter,
	 * the default argument is returned. */
	protected int greenColorComponent (int defvalue) {
		return colorComponent(1, defvalue);
	}

	/** Returns the blue component of this element's base color, taken from the "color" parameter. If there is no color parameter,
	 * the default argument is returned. */
	protected int blueColorComponent (int defvalue) {
		return colorComponent(2, defvalue);
	}
}
