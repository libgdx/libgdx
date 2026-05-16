package com.badlogic.gdx.graphics.g3d.utils.shapebuilders;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;

public class DiskShapeBuilder
{
    public static void build(MeshPartBuilder builder, Vector3 position, float radius, int sides)
	{
		float tick = 360f / sides;
		float scale = radius * 2f;
		
		// used to rotate a triangle before put next inside builder
		Vector3 up = BaseShapeBuilder.tmpV0.set(0,1,0);
		
		Vector3 pCenter = BaseShapeBuilder.tmpV1.set(0.00000001f, 0, 0);
		Vector3 p1 = BaseShapeBuilder.tmpV2.set(.5f, 0, 0);
		Vector3 p2 = p1.cpy().rotate(up, tick);
		
		for(int i = 0; i < sides; i++)
		{
			// adjust ( point1, point2 ) by correct scale "radius / 2" and calculate uv
			BaseShapeBuilder.vertTmp0.setPos(pCenter.cpy().add(position)).setUV(.5f, .5f);
			BaseShapeBuilder.vertTmp1.setPos(p1.cpy().scl(scale).add(position)).setUV(p1.x+.5f, p1.z+.5f);
			BaseShapeBuilder.vertTmp2.setPos(p2.cpy().scl(scale).add(position)).setUV(p2.x+.5f, p2.z+.5f);

			builder.triangle(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp1,
				BaseShapeBuilder.vertTmp2);

			// rotate actual triangle to create next triangle
			if(i < sides - 1)
			{
				p1.rotate(up, tick);
				p2.rotate(up, tick);
			}
		}
	}
}
