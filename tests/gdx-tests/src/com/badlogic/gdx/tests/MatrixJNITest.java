package com.badlogic.gdx.tests;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MatrixJNITest extends GdxTest {
	@Override public boolean needsGL20 () {
		return false;
	}
	
	@Override public void create() {
		Matrix4 mat1 = new Matrix4();
		Matrix4 mat2 = new Matrix4();
		Matrix4 mat3 = new Matrix4();
		Vector3 vec = new Vector3(1, 2, 3);
		float[] fvec = {1, 2, 3};
		
		mat1.setToRotation(0, 1, 0, 45);
		mat2.setToRotation(1, 0, 0, 45);
		
		vec.mul(mat1);
		Matrix4.mulVecJNI(mat1.val, fvec);
		check(vec, fvec);
		
		vec.prj(mat1);
		Matrix4.projVec(mat1.val, fvec);
		check(vec, fvec);
		
		vec.rot(mat1);
		Matrix4.rotVec(mat1.val, fvec);
		check(vec, fvec);
		
		if(mat1.det() != Matrix4.detJNI(mat1.val)) throw new GdxRuntimeException("det doesn't work");
		
		mat2.set(mat1);
		mat1.inv();
		Matrix4.invJNI(mat2.val);
		check(mat1, mat2);
		
		mat3.set(mat1);
		mat1.mul(mat2);
		Matrix4.mulJNI(mat3.val, mat2.val);
		check(mat1, mat3);
	}
	
	private void check(Vector3 vec, float[] fvec) {
		if(vec.x != fvec[0] || vec.y != fvec[1] || vec.z != fvec[2]) throw new GdxRuntimeException("vectors are not equal");
	}
	
	private void check(Matrix4 mat1, Matrix4 mat2) {
		for(int i = 0; i < 16; i++) {
			if(mat1.val[i] != mat2.val[i]) throw new GdxRuntimeException("matrices not equal");
		}
	}
}
