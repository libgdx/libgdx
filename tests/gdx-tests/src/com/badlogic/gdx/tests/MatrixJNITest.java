package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
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
		float[] fvecs = { 1, 2, 3, 0, 0, 1, 2, 3, 0, 0, 1, 2, 3, 0, 0 };
		
		mat1.setToRotation(0, 1, 0, 45);
		mat2.setToRotation(1, 0, 0, 45);
		
		vec.mul(mat1);
		Matrix4.mulVecJNI(mat1.val, fvec);
		Matrix4.mulVecJNI(mat1.val, fvecs, 0, 3, 5);
		check(vec, fvec);
		check(vec, fvecs, 3, 5);
		
		vec.prj(mat1);
		Matrix4.projVecJNI(mat1.val, fvec);
		Matrix4.projVecJNI(mat1.val, fvecs, 0, 3, 5);
		check(vec, fvec);
		check(vec, fvecs, 3, 5);
		
		vec.rot(mat1);
		Matrix4.rotVecJNI(mat1.val, fvec);
		Matrix4.rotVecJNI(mat1.val, fvecs, 0, 3, 5);
		check(vec, fvec);
		check(vec, fvecs, 3, 5);
		
		if(mat1.det() != Matrix4.detJNI(mat1.val)) throw new GdxRuntimeException("det doesn't work");
		
		mat2.set(mat1);
		mat1.inv();
		Matrix4.invJNI(mat2.val);
		check(mat1, mat2);
		
		mat3.set(mat1);
		mat1.mul(mat2);
		Matrix4.mulJNI(mat3.val, mat2.val);
		check(mat1, mat3);
		
		bench();
	}
	
	private void bench() {
		Matrix4 mata = new Matrix4();
		Matrix4 matb = new Matrix4();
		
		long start = System.nanoTime();
		for(int i = 0; i < 1000000; i++) {
			mata.mul(matb);
		}
		Gdx.app.log("MatrixJNITest", "java mul took: " + (System.nanoTime() - start) / 1000000000.0f);
		
		start = System.nanoTime();
		for(int i = 0; i < 1000000; i++) {
			Matrix4.mulJNI(mata.val, matb.val);
		}
		Gdx.app.log("MatrixJNITest", "jni mul took: " + (System.nanoTime() - start) / 1000000000.0f);
	}
	
	private void check(Vector3 vec, float[] fvec) {
		if(vec.x != fvec[0] || vec.y != fvec[1] || vec.z != fvec[2]) throw new GdxRuntimeException("vectors are not equal");
	}
	
	private void check(Vector3 vec, float[] fvec, int numVecs, int stride) {
		int offset = 0;
		for(int i = 0; i < numVecs; i++) {
			if(vec.x != fvec[0] || vec.y != fvec[1] || vec.z != fvec[2]) throw new GdxRuntimeException("vectors are not equal");
		}
	}
	
	private void check(Matrix4 mat1, Matrix4 mat2) {
		for(int i = 0; i < 16; i++) {
			if(mat1.val[i] != mat2.val[i]) throw new GdxRuntimeException("matrices not equal");
		}
	}
}
