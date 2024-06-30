/**
 * This class creates a box and allows you to ignore any side.
 * To ignore the RIGHT and TOP sides, use:
 * BoxSideShapeBuilder.build(builder, position, scale, IGNORE_RIGHT | IGNORE_TOP);
 *
 * @author if_desu
 */
 
package com.badlogic.gdx.graphics.g3d.utils.shapebuilders;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Vector3;

public class BoxSideShapeBuilder {
	public static int IGNORE_NOTHING = 0;
	public static int IGNORE_TOP = 2; // positive y
	public static int IGNORE_BOTTOM = 4; // negative y
	public static int IGNORE_FRONT = 8; // posivite z
	public static int IGNORE_BACK = 16; // negative z
	public static int IGNORE_LEFT = 32; // negative x
	public static int IGNORE_RIGHT = 64; // positive x

    public static void build(MeshPartBuilder builder, Vector3 position, Vector3 scale, int ignore)
	{
		if ( ( ignore & IGNORE_FRONT ) == 0)
		{
			// z++
			BaseShapeBuilder.vertTmp0.setPos(BaseShapeBuilder.tmpV0.set(-.5f, -.5f, .5f).scl(scale).add(position)).setUV(0,1);
			BaseShapeBuilder.vertTmp1.setPos(BaseShapeBuilder.tmpV1.set(.5f, -.5f, .5f).scl(scale).add(position)).setUV(1,1);
			BaseShapeBuilder.vertTmp2.setPos(BaseShapeBuilder.tmpV2.set(.5f, .5f, .5f).scl(scale).add(position)).setUV(1,0);
			BaseShapeBuilder.vertTmp3.setPos(BaseShapeBuilder.tmpV3.set(-.5f, .5f, .5f).scl(scale).add(position)).setUV(0,0);
			builder.rect
			(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp1,
				BaseShapeBuilder.vertTmp2,
				BaseShapeBuilder.vertTmp3
			);
		}

		if ( ( ignore & IGNORE_RIGHT ) == 0)
		{
			// x++
			BaseShapeBuilder.vertTmp0.setPos(BaseShapeBuilder.tmpV0.set(.5f, -.5f, .5f).scl(scale).add(position)).setUV(0,1);
			BaseShapeBuilder.vertTmp1.setPos(BaseShapeBuilder.tmpV1.set(.5f, -.5f, -.5f).scl(scale).add(position)).setUV(1,1);
			BaseShapeBuilder.vertTmp2.setPos(BaseShapeBuilder.tmpV2.set(.5f, .5f, -.5f).scl(scale).add(position)).setUV(1,0);
			BaseShapeBuilder.vertTmp3.setPos(BaseShapeBuilder.tmpV3.set(.5f, .5f, .5f).scl(scale).add(position)).setUV(0,0);
			builder.rect
			(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp1,
				BaseShapeBuilder.vertTmp2,
				BaseShapeBuilder.vertTmp3
			);
		}

		if ( ( ignore & IGNORE_BACK ) == 0)
		{
			// z--
			BaseShapeBuilder.vertTmp0.setPos(BaseShapeBuilder.tmpV0.set(.5f, -.5f, -.5f).scl(scale).add(position)).setUV(0,1);
			BaseShapeBuilder.vertTmp1.setPos(BaseShapeBuilder.tmpV1.set(-.5f, -.5f, -.5f).scl(scale).add(position)).setUV(1,1);
			BaseShapeBuilder.vertTmp2.setPos(BaseShapeBuilder.tmpV2.set(-.5f, .5f, -.5f).scl(scale).add(position)).setUV(1,0);
			BaseShapeBuilder.vertTmp3.setPos(BaseShapeBuilder.tmpV3.set(.5f, .5f, -.5f).scl(scale).add(position)).setUV(0,0);
			builder.rect
			(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp1,
				BaseShapeBuilder.vertTmp2,
				BaseShapeBuilder.vertTmp3
			);
		}

		if ( ( ignore & IGNORE_LEFT ) == 0)
		{
			// x--
			BaseShapeBuilder.vertTmp0.setPos(BaseShapeBuilder.tmpV0.set(-.5f, -.5f, -.5f).scl(scale).add(position)).setUV(0,1);
			BaseShapeBuilder.vertTmp1.setPos(BaseShapeBuilder.tmpV1.set(-.5f, -.5f, .5f).scl(scale).add(position)).setUV(1,1);
			BaseShapeBuilder.vertTmp2.setPos(BaseShapeBuilder.tmpV2.set(-.5f, .5f, .5f).scl(scale).add(position)).setUV(1,0);
			BaseShapeBuilder.vertTmp3.setPos(BaseShapeBuilder.tmpV3.set(-.5f, .5f, -.5f).scl(scale).add(position)).setUV(0,0);
			builder.rect
			(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp1,
				BaseShapeBuilder.vertTmp2,
				BaseShapeBuilder.vertTmp3
			);
		}

		if ( ( ignore & IGNORE_TOP ) == 0)
		{
			// y++
			BaseShapeBuilder.vertTmp0.setPos(BaseShapeBuilder.tmpV0.set(-.5f, .5f, .5f).scl(scale).add(position)).setUV(0,1);
			BaseShapeBuilder.vertTmp1.setPos(BaseShapeBuilder.tmpV1.set(.5f, .5f, .5f).scl(scale).add(position)).setUV(1,1);
			BaseShapeBuilder.vertTmp2.setPos(BaseShapeBuilder.tmpV2.set(.5f, .5f, -.5f).scl(scale).add(position)).setUV(1,0);
			BaseShapeBuilder.vertTmp3.setPos(BaseShapeBuilder.tmpV3.set(-.5f, .5f, -.5f).scl(scale).add(position)).setUV(0,0);
			builder.rect
			(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp1,
				BaseShapeBuilder.vertTmp2,
				BaseShapeBuilder.vertTmp3
			);
		}

		if ( ( ignore & IGNORE_BOTTOM ) == 0)
		{
			// y--
			BaseShapeBuilder.vertTmp0.setPos(BaseShapeBuilder.tmpV0.set(-.5f, -.5f, -.5f).scl(scale).add(position)).setUV(0,1);
			BaseShapeBuilder.vertTmp1.setPos(BaseShapeBuilder.tmpV1.set(.5f, -.5f, -.5f).scl(scale).add(position)).setUV(1,1);
			BaseShapeBuilder.vertTmp2.setPos(BaseShapeBuilder.tmpV2.set(.5f, -.5f, .5f).scl(scale).add(position)).setUV(1,0);
			BaseShapeBuilder.vertTmp3.setPos(BaseShapeBuilder.tmpV3.set(-.5f, -.5f, .5f).scl(scale).add(position)).setUV(0,0);
			builder.rect
			(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp1,
				BaseShapeBuilder.vertTmp2,
				BaseShapeBuilder.vertTmp3
			);
		}

    }
}
