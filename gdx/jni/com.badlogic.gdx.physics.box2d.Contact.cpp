#include <com.badlogic.gdx.physics.box2d.Contact.h>

//@line:25

#include <Box2D/Box2D.h>
	 static inline jint wrapped_Java_com_badlogic_gdx_physics_box2d_Contact_jniGetWorldManifold
(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_tmp, float* tmp) {

//@line:60

		b2Contact* contact = (b2Contact*)addr;
		b2WorldManifold manifold;
		contact->GetWorldManifold(&manifold);
		int numPoints = contact->GetManifold()->pointCount;
	
		tmp[0] = manifold.normal.x;
		tmp[1] = manifold.normal.y;
	
		for( int i = 0; i < numPoints; i++ )
		{
			tmp[2 + i*2] = manifold.points[i].x;
			tmp[2 + i*2+1] = manifold.points[i].y;
		}
	
		return numPoints;
	
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetWorldManifold(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_tmp) {
	float* tmp = (float*)env->GetPrimitiveArrayCritical(obj_tmp, 0);

	jint JNI_returnValue = wrapped_Java_com_badlogic_gdx_physics_box2d_Contact_jniGetWorldManifold(env, object, addr, obj_tmp, tmp);

	env->ReleasePrimitiveArrayCritical(obj_tmp, tmp, 0);

	return JNI_returnValue;
}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniIsTouching(JNIEnv* env, jobject object, jlong addr) {


//@line:82

		b2Contact* contact = (b2Contact*)addr;
		return contact->IsTouching();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniSetEnabled(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:93

		b2Contact* contact = (b2Contact*)addr;
		contact->SetEnabled(flag);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniIsEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:103

		b2Contact* contact = (b2Contact*)addr;
		return contact->IsEnabled();
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetFixtureA(JNIEnv* env, jobject object, jlong addr) {


//@line:113

		b2Contact* contact = (b2Contact*)addr;
		return (jlong)contact->GetFixtureA();
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetFixtureB(JNIEnv* env, jobject object, jlong addr) {


//@line:123

		b2Contact* contact = (b2Contact*)addr;
		return (jlong)contact->GetFixtureB();
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetChildIndexA(JNIEnv* env, jobject object, jlong addr) {


//@line:133

		b2Contact* contact = (b2Contact*)addr;
		return contact->GetChildIndexA();
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetChildIndexB(JNIEnv* env, jobject object, jlong addr) {


//@line:143

		b2Contact* contact = (b2Contact*)addr;
		return contact->GetChildIndexB();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniSetFriction(JNIEnv* env, jobject object, jlong addr, jfloat friction) {


//@line:154

		b2Contact* contact = (b2Contact*)addr;
		contact->SetFriction(friction);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetFriction(JNIEnv* env, jobject object, jlong addr) {


//@line:164

		b2Contact* contact = (b2Contact*)addr;
		return contact->GetFriction();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniResetFriction(JNIEnv* env, jobject object, jlong addr) {


//@line:174

	  	b2Contact* contact = (b2Contact*)addr;
		contact->ResetFriction();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniSetRestitution(JNIEnv* env, jobject object, jlong addr, jfloat restitution) {


//@line:185

	  	b2Contact* contact = (b2Contact*)addr;
		contact->SetRestitution(restitution);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetRestitution(JNIEnv* env, jobject object, jlong addr) {


//@line:195

	  	b2Contact* contact = (b2Contact*)addr;
		return contact->GetRestitution();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniResetRestitution(JNIEnv* env, jobject object, jlong addr) {


//@line:205

	  	b2Contact* contact = (b2Contact*)addr;
		contact->ResetRestitution();
	

}

