package com.badlogic.gdx.graphics.g3d.utils.shapebuilders;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector2;

public class StarShapeBuilder {
    public static void build(MeshPartBuilder builder, Vector3 position, int points, float outer_radius, float inner_radius, float total_angle)
	{
		float tick = total_angle / points;

		Vector3 up = BaseShapeBuilder.tmpV0.set(0,1,0);

		Vector3 p0 = BaseShapeBuilder.tmpV1.set(0.00000001f, 0, 0);
		Vector3 p1 = BaseShapeBuilder.tmpV2.set(0, 0, -outer_radius);
		Vector3 p2 = BaseShapeBuilder.tmpV3.set(0,0,-inner_radius).rotate(up,tick/2f);
		Vector3 p3 = BaseShapeBuilder.tmpV4.set(0,0,-outer_radius).rotate(up, tick);

		Vector2 uv1 = new Vector2();
		Vector2 uv2 = new Vector2();
		Vector2 uv3 = new Vector2();
		
		for(int i = 0; i < points; i++)
		{
			uv1.set(p1.x, p1.z).nor().scl(.5f).add(.5f, .5f);
			uv2.set(p2.x, p2.z).nor().scl(inner_radius * .5f / outer_radius).add(.5f, .5f);
			uv3.set(p3.x, p3.z).nor().scl(.5f).add(.5f, .5f);
			
			BaseShapeBuilder.vertTmp0.setPos(p0.x + position.x, p0.y + position.y, p0.z + position.z).setUV(.5f, .5f);
			BaseShapeBuilder.vertTmp1.setPos(p1.x + position.x, p1.y + position.y, p1.z + position.z).setUV(uv1);
			BaseShapeBuilder.vertTmp2.setPos(p2.x + position.x, p2.y + position.y, p2.z + position.z).setUV(uv2);
			BaseShapeBuilder.vertTmp3.setPos(p3.x + position.x, p3.y + position.y, p3.z + position.z).setUV(uv3);

			builder.triangle(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp1,
				BaseShapeBuilder.vertTmp2);
			builder.triangle(
				BaseShapeBuilder.vertTmp0,
				BaseShapeBuilder.vertTmp2,
				BaseShapeBuilder.vertTmp3);

			if(i<points-1)
			{
				p1.rotate(up, tick);
				p2.rotate(up, tick);
				p3.rotate(up, tick);
			}
		}
	}
    
}
