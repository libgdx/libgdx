package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.Disposable;

public class Line implements Disposable{
	public Vector2 lineStart;
	public Vector2 lineEnd;
	
	private Vector2 mTMP = new Vector2();
	
	public static final class LineMode{
		public static final byte SQUARE_LINE = 0;
		public static final byte PARALLEL_LINE = 1;
	}
	
	public Line(){
		lineStart =new Vector2();
		lineEnd   =new Vector2();
	}
	
	public Line(Line line){
		set(line);
	}

	public Line(Vector2 startPoint,float angleFactor){
		
	}
	
	public Line(Vector2 startPoint,Line line,byte linemode){
		if(linemode == LineMode.SQUARE_LINE){
			Vector2 d = line.getNormalVector();
			Vector2 endPoint = new Vector2(startPoint.x + d.x ,startPoint.y + d.y);
			set(startPoint,endPoint);
		}else if(linemode == LineMode.PARALLEL_LINE){
			Vector2 d = line.getDirectionalVector();
			Vector2 endPoint = new Vector2(startPoint.x + d.x ,startPoint.y + d.y);
			set(startPoint,endPoint);
		}
	}
	
	public Line(float startX,float startY,Vector2 normalVector){
		lineStart =new Vector2(startX, startY);
		lineEnd = new Vector2(startX + normalVector.y, startY - normalVector.x);
	}
	
	public Line(Vector2 lineStart,Vector2 lineEnd){
		this.lineStart = lineStart;
		this.lineEnd =  lineEnd;
	}
	
	public Line(float startX,float startY,float endX,float endY){
		this.lineStart= new Vector2(startX, startY);
		this.lineEnd = new Vector2(endX, endY);
	}

	/*************************************************************
	 * 
	 *************************************************************/
	public Line set(float x,float y,float x1,float y1){
		lineStart.set(x, y);
		lineEnd.set(x1, y1);
		return this;
	}
	
	public Line setEnd(float x,float y){
		lineEnd.set(x,y);
		return this;
	}
	
	public Line setStart(float x,float y){
		lineStart.set(x, y);
		return this;
	}
	
	private void set (Vector2 startPoint, Vector2 endPoint) {
		lineStart = startPoint;
		lineEnd = endPoint;
	}

	public Line set(Line line){
		this.lineStart = new Vector2(line.lineStart.x, line.lineStart.y);
		this.lineEnd = new Vector2(line.lineEnd.x, line.lineEnd.y);
		return this;
	}
	
	public Line cpy(){
		return new Line(this);	
	}

	/*************************************************************
	 * 
	 *************************************************************/

	public Vector2 getNormalVector(){
		mTMP.set(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y);
		if(mTMP.y < 0)
			mTMP.set(-mTMP.y, mTMP.x);
		else
			mTMP.set(mTMP.y, -mTMP.x);
		return mTMP;
	}
	
	public Vector2 getDirectionalVector(){
		return mTMP.set(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y);
	}

	/**
	 * Get the angle between this line and 0x line
	 * @return result is in range [0,360]
	 */
	public float getAngleFactor(){
		if(lineEnd.x == lineStart.x){
			if(lineEnd.y > lineStart.y)
				return 90;
			if (lineEnd.y < lineStart.y)
				return 270;
		}
		
		if(lineEnd.y == lineStart.y){
			if(lineStart.x < lineEnd.x)
				return 360;
			if(lineStart.x > lineEnd.x)
				return 180;
		}
		
		if(lineEnd.x > lineStart.x)
			return (float) (180.0f/MathUtils.PI* Math.atan((lineEnd.y - lineStart.y)/(lineEnd.x - lineStart.x)));
		if(lineEnd.x < lineStart.x)
			return (float) (180.0f/MathUtils.PI* Math.atan((lineEnd.y - lineStart.y)/(lineEnd.x - lineStart.x))) - 180;
		
		return 0;
	}
	
	public float getConstantOfLine(){
		Vector2 n = getNormalVector();
		float c = -(n.x * lineStart.x + n.y * lineStart.y);
		return c;
	}
	
	public Vector2 getMidPoint(){
		return mTMP.set( (lineStart.x+lineEnd.x)/2, 
						  (lineStart.y+lineEnd.y)/2);
	}
	
	/*************************************************************
	 * 
	 *************************************************************/

	public float module(){
		float tmp = (float) Math.sqrt((lineStart.x - lineEnd.x)*(lineStart.x - lineEnd.x) 
							   		  +(lineStart.y - lineEnd.y)*(lineStart.y - lineEnd.y));
		return tmp;
	}
	
	public float module2(){
		float tmp = (float) ((lineStart.x - lineEnd.x)*(lineStart.x - lineEnd.x) 
							+(lineStart.y - lineEnd.y)*(lineStart.y - lineEnd.y));
		return tmp;
	}

	/*************************************************************
	 * 
	 *************************************************************/

	public int getXinLine(int y){
		float delta = ( (float)(lineStart.x-lineEnd.x) / (float)(lineStart.y-lineEnd.y) );
		return (int)(delta* (y-lineStart.y)+ lineStart.x);
	}
	
	public  float getYinLine(float x){
		float delta = ( (float)(lineStart.y - lineEnd.y) / (float)(lineStart.x-lineEnd.x) );
		return (int)(delta * (x - lineStart.x)+lineStart.y);
	}
	
	/**
	 * You input point(x0,y0) and i will return: a*x0 + b*y0 + c
	 * @param point
	 * @return
	 */
	public  float getLine(Vector2 point){
		// Normal vector in 2D (vector pha'p tuyen)
		Vector2 n = getNormalVector();
		float c = -(n.x * lineStart.x + n.y * lineStart.y);
		
		return (n.x*point.x + n.y* point.y + c);
	}
	
	/*************************************************************
	 * 
	 *************************************************************/
	public float getSideOnLine(Vector2 point){
		float side = getLine(point);
		return side;
	}
	
	public boolean isOnLeftSide(Vector2 point){
		if(getSideOnLine(point) < 0)
			return true;
		return false;
	}

	public boolean isOnRightSide(Vector2 point){
		if(getSideOnLine(point) > 0)
			return true;
		return false;
	}

	
	public  float calDistancePointToLine(Vector2 point){
		float delta = Math.abs(getLine(point));
		
		float module = getNormalVector().len();
		
		return (delta/module);
	}
	
	public Vector2 calCrossPoint(Line line){
		mTMP = getNormalVector();
		float a1 = mTMP.x;
		float b1 = mTMP.y;
		float c1 = getConstantOfLine();
		
		mTMP = line.getNormalVector();
		float a2 = mTMP.x;
		float b2 = mTMP.y;
		float c2 = line.getConstantOfLine();
		
		if(a1*b2 == b1*a2)
			return null;
		else
			return new Vector2( (b1*c2-c1*b2)/(b2*a1-b1*a2), 
								 (a2*c1-c2*a1)/(b2*a1-b1*a2));
	}

	@Override
	public void dispose () {
		lineEnd  = null;
		lineStart = null;
		mTMP = null;
	}
}
