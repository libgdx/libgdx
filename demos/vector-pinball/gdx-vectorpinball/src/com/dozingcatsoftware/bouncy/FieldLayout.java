package com.dozingcatsoftware.bouncy;

import static com.dozingcatsoftware.bouncy.util.MathUtils.asFloat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.bouncy.elements.FieldElement;
import com.dozingcatsoftware.bouncy.elements.FlipperElement;
import com.dozingcatsoftware.bouncy.util.JSONUtils;


public class FieldLayout {
	
	static List _layoutArray;
	Random RAND = new Random();
	
	static void readLayoutArray() {
		try {
			InputStream fin = Gdx.files.internal("data/field_layout.json").read();
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));

			StringBuilder buffer = new StringBuilder();
			String line;
			while ((line=br.readLine())!=null) {
				buffer.append(line);
			}
			fin.close();
			_layoutArray = JSONUtils.listFromJSONString(buffer.toString());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static FieldLayout layoutForLevel(int level, World world) {
		try {
			if (_layoutArray==null) readLayoutArray();
			Map layoutMap = (Map)_layoutArray.get(level - 1);
			return new FieldLayout(layoutMap, world);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static int numberOfLevels() {
		return _layoutArray.size();
	}

	List<FieldElement> fieldElements = new ArrayList<FieldElement>();
	List<FlipperElement> flippers;
	float width;
	float height;
	List<Integer> ballColor;
	float targetTimeRatio;
	Map allParameters;
	
	static List<Integer> DEFAULT_BALL_COLOR = Arrays.asList(255, 0, 0);

	static List listForKey(Map map, String key) {
		if (map.containsKey(key)) return (List)map.get(key);
		return Collections.EMPTY_LIST;
	}
	
	List addFieldElements(Map layoutMap, String key, Class defaultClass, World world) {
		List elements = new ArrayList();
		for(Object obj : listForKey(layoutMap, key)) {
			// allow strings in JSON for comments
			if (!(obj instanceof Map)) continue;
			Map params = (Map)obj;
			elements.add(FieldElement.createFromParameters(params, world, defaultClass));
		}
		fieldElements.addAll(elements);
		return elements;
	}
	
	public FieldLayout(Map layoutMap, World world) {
		this.width = asFloat(layoutMap.get("width"), 20.0f);
		this.height = asFloat(layoutMap.get("height"), 30.0f);
		this.targetTimeRatio = asFloat(layoutMap.get("targetTimeRatio"));
		this.ballColor = (layoutMap.containsKey("ballcolor")) ? (List<Integer>)layoutMap.get("ballcolor") : DEFAULT_BALL_COLOR;
		this.allParameters = layoutMap;
		
		flippers = addFieldElements(layoutMap, "flippers", FlipperElement.class, world);
		
		addFieldElements(layoutMap, "elements", null, world);
	}

	public List<FieldElement> getFieldElements() {
		return fieldElements;
	}
	
	public List<FlipperElement> getFlipperElements() {
		return flippers;
	}
	
	public float getBallRadius() {
		return asFloat(allParameters.get("ballradius"), 0.5f);
	}
	
	public List<Integer> getBallColor() {
		return ballColor;
	}
	
	public int getNumberOfBalls() {
		return (allParameters.containsKey("numballs")) ? ((Number)allParameters.get("numballs")).intValue() : 3;
	}
	
	public List<Number> getLaunchPosition() {
		Map launchMap = (Map)allParameters.get("launch");
		return (List<Number>)launchMap.get("position");
	}
	
	// can apply random velocity increment if specified by "random_velocity" key
	public List<Float> getLaunchVelocity() {
		Map launchMap = (Map)allParameters.get("launch");
		List<Number> velocity = (List<Number>)launchMap.get("velocity");
		float vx = velocity.get(0).floatValue();
		float vy = velocity.get(1).floatValue();
		
		if (launchMap.containsKey("random_velocity")) {
			List<Number> delta = (List<Number>)launchMap.get("random_velocity");
			if (delta.get(0).floatValue()>0) vx += delta.get(0).floatValue() * RAND.nextFloat();
			if (delta.get(1).floatValue()>0) vy += delta.get(1).floatValue() * RAND.nextFloat();
		}
		return Arrays.asList(vx, vy);
	}
	
	public float getWidth() {
		return width;
	}
	public float getHeight() {
		return height;
	}
	
	/** Returns the desired ratio between real world time and simulation time. The application should adjust the frame rate and/or 
	 * time interval passed to Field.tick() to keep the ratio as close to this value as possible.
	 */
	public float getTargetTimeRatio() {
		return targetTimeRatio;
	}
	
	/** Returns the magnitude of the gravity vector. */
	public float getGravity() {
		return asFloat(allParameters.get("gravity"), 4.0f);
	}
	
	public String getDelegateClassName() {
		return (String)allParameters.get("delegate");
	}

}
