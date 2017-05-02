package com.badlogic.gdx.graphics.g2d.newspritebatch;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.newspritebatch.NewBatch.BatchShape;
import com.badlogic.gdx.math.Matrix4;

public interface NewBatch {	
	public void begin();
	public void flush();
	public void end();
	
	public Matrix4 getProjectionMatrix();
	public Matrix4 getTransformMatrix();
	
	public BatchQuad quad();
	public BatchPolygon polygon(int numPoints);	
	
	public static abstract class BatchShape {
		float x, y, originX, originY, scaleX, scaleY, rotation;
		Texture texture;
		TextureRegion region;
		
		public BatchShape position(float x, float y) {
			this.x = x;
			this.y = y;
			return this;
		}
		
		public BatchShape origin(float x, float y) {
			this.originX = x;
			this.originY = y;
			return this;
		}
		
		public BatchShape scale(float x, float y) {
			this.scaleX = x;
			this.scaleY = y;
			return this;
		}
		
		public BatchShape rotation(float rot) {
			this.rotation = rot;
			return this;
		}
		
		public BatchShape texture(Texture tex) {
			this.region = null;
			this.texture = tex;
			return this;
		}
		
		public BatchShape region(TextureRegion region) {
			this.texture = null;
			this.region = region;
			return this;
		}
		
		public abstract void end();
	}
	
	public static class BatchQuad extends BatchShape {
		@Override
		public BatchQuad position (float x, float y) {
			return (BatchQuad)super.position(x, y);
		}

		@Override
		public BatchQuad origin (float x, float y) {
			return (BatchQuad)super.origin(x, y);
		}

		@Override
		public BatchQuad scale (float x, float y) {
			return (BatchQuad)super.scale(x, y);
		}

		@Override
		public BatchQuad rotation (float rot) {
			return (BatchQuad)super.rotation(rot);
		}

		@Override
		public BatchShape texture (Texture tex) {
			return (BatchQuad)super.texture(tex);
		}

		@Override
		public BatchShape region (TextureRegion region) {
			return (BatchQuad)super.region(region);
		}

		@Override
		public void end () {
		}
	}
	
	public static class BatchPolygon extends BatchShape {
		@Override
		public void end () {
			
		}		
	}
}
