#include <com.badlogic.gdx.physics.box2d.Shape.h>

//@line:26

#include <Box2D/Box2D.h>
	 JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniGetRadius(JNIEnv* env, jobject object, jlong addr) {


//@line:48

		b2Shape* shape = (b2Shape*)addr;
		return shape->m_radius;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniSetRadius(JNIEnv* env, jobject object, jlong addr, jfloat radius) {


//@line:58

		b2Shape* shape = (b2Shape*)addr;
		shape->m_radius = radius;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniDispose(JNIEnv* env, jobject object, jlong addr) {


//@line:68

		b2Shape* shape = (b2Shape*)addr;
		delete shape;
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniGetType(JNIEnv* env, jclass clazz, jlong addr) {


//@line:73

		b2Shape* shape = (b2Shape*)addr;
		switch(shape->m_type) {
		case b2Shape::e_circle: return 0;
		case b2Shape::e_edge: return 1;
		case b2Shape::e_polygon: return 2;
		case b2Shape::e_chain: return 3;
		default: return -1;
		}
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniGetChildCount(JNIEnv* env, jobject object, jlong addr) {


//@line:89

		b2Shape* shape = (b2Shape*)addr;
		return shape->GetChildCount();
	

}

