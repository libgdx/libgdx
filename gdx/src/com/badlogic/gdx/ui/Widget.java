package com.badlogic.gdx.ui;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

/**
 * A Widget is the base class of the UI module. A Widget can have a 
 * parent as well as children. A widget is defined relative to its 
 * parent. A widget can have an optional name, a position, a scale, an origin
 * relative to its position which is used when scaling is performed, a color and a hitarea.
 * The position of a widget is given for its top left corner. The hitarea
 * extends from that position to the right and downwards. 
 *  
 * 
 * @author mzechner
 *
 */
public abstract class Widget 
{
	/** the name of the widget, can be null **/
	protected String name = null;
	
	/** the widget's parent **/
	protected Widget parent;
	
	/** the widget's children **/
	protected final ArrayList<Widget> children = new ArrayList<Widget>();
	
	/** the widget's position relative to its parent **/
	protected final Vector2 position = new Vector2( );
	
	/** the widget's scaling and rotation origin relative to its position **/
	protected final Vector2 origin = new Vector2( );
	
	/** the scale of the widget **/
	protected final float scale = 1;
	
	/** the widget's hit area width and height **/
	protected float hitAreaWidth = 0;
	protected float hitAreaHeight = 0;
}
