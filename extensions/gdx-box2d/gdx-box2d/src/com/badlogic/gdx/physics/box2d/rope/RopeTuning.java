
package com.badlogic.gdx.physics.box2d.rope;

public class RopeTuning {
	public enum StretchingModel {
		Pbd(0), Xpbd(1);

		public final int value;

		StretchingModel (int value) {
			this.value = value;
		}
	}

	// Question: why not use .ordinal()?
	public enum BendingModel {
		SpringAngle(0), PbdAngle(1), XpbdAngle(2), PbdDistance(3), PbdHeight(4), PbdTriangle(5);

		public final int value;

		BendingModel (int value) {
			this.value = value;
		}
	}

	public StretchingModel stretchingModel;
	public BendingModel bendingModel;
	public float damping;
	public float stretchStiffness;
	public float stretchHertz;
	public float stretchDamping;
	public float bendStiffness;
	public float bendHertz;
	public float bendDamping;
	public boolean isometric;
	public boolean fixedEffectiveMass;
	public boolean warmStart;

	public RopeTuning () {
		stretchingModel = StretchingModel.Pbd;
		bendingModel = BendingModel.PbdAngle;
		damping = 0.0f;
		stretchStiffness = 1.0f;
		stretchHertz = 1.0f;
		stretchDamping = 0.0f;
		bendStiffness = 0.5f;
		bendHertz = 1.0f;
		bendDamping = 0.0f;
		isometric = false;
		fixedEffectiveMass = false;
		warmStart = false;
	}
}
