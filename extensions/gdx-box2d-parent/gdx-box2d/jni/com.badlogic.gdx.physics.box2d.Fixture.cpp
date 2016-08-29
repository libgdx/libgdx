#include <com.badlogic.gdx.physics.box2d.Fixture.h>

//@line:25

#include <Box2D/Box2D.h>
	 JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetType(JNIEnv* env, jobject object, jlong addr) {


//@line:72

		b2Fixture* fixture = (b2Fixture*)addr;
		b2Shape::Type type = fixture->GetType();
		switch( type )
		{
		case b2Shape::e_circle: return 0;
		case b2Shape::e_edge: return 1;
		case b2Shape::e_polygon: return 2;
		case b2Shape::e_chain: return 3;
		default:
			return -1;
		}
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetShape(JNIEnv* env, jobject object, jlong addr) {


//@line:114

		b2Fixture* fixture = (b2Fixture*)addr;
		return (jlong)fixture->GetShape();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetSensor(JNIEnv* env, jobject object, jlong addr, jboolean sensor) {


//@line:124

		b2Fixture* fixture = (b2Fixture*)addr;
		fixture->SetSensor(sensor);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniIsSensor(JNIEnv* env, jobject object, jlong addr) {


//@line:135

		b2Fixture* fixture = (b2Fixture*)addr;
		return fixture->IsSensor();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetFilterData(JNIEnv* env, jobject object, jlong addr, jshort categoryBits, jshort maskBits, jshort groupIndex) {


//@line:146

		b2Fixture* fixture = (b2Fixture*)addr;
		b2Filter filter;
		filter.categoryBits = categoryBits;
		filter.maskBits = maskBits;
		filter.groupIndex = groupIndex;
		fixture->SetFilterData(filter);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetFilterData(JNIEnv* env, jobject object, jlong addr, jshortArray obj_filter) {
	short* filter = (short*)env->GetPrimitiveArrayCritical(obj_filter, 0);


//@line:167

		b2Fixture* fixture = (b2Fixture*)addr;
		unsigned short* filterOut = (unsigned short*)filter;
		b2Filter f = fixture->GetFilterData();
		filterOut[0] = f.maskBits;
		filterOut[1] = f.categoryBits;
		filterOut[2] = f.groupIndex;
	
	env->ReleasePrimitiveArrayCritical(obj_filter, filter, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniRefilter(JNIEnv* env, jobject object, jlong addr) {


//@line:181

		b2Fixture* fixture = (b2Fixture*)addr;
		fixture->Refilter();
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniTestPoint(JNIEnv* env, jobject object, jlong addr, jfloat x, jfloat y) {


//@line:204

		b2Fixture* fixture = (b2Fixture*)addr;
		return fixture->TestPoint( b2Vec2( x, y ) );
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetDensity(JNIEnv* env, jobject object, jlong addr, jfloat density) {


//@line:239

		b2Fixture* fixture = (b2Fixture*)addr;
		fixture->SetDensity(density);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetDensity(JNIEnv* env, jobject object, jlong addr) {


//@line:249

		b2Fixture* fixture = (b2Fixture*)addr;
		return fixture->GetDensity();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetFriction(JNIEnv* env, jobject object, jlong addr) {


//@line:259

		b2Fixture* fixture = (b2Fixture*)addr;
		return fixture->GetFriction();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetFriction(JNIEnv* env, jobject object, jlong addr, jfloat friction) {


//@line:269

		b2Fixture* fixture = (b2Fixture*)addr;
		fixture->SetFriction(friction);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetRestitution(JNIEnv* env, jobject object, jlong addr) {


//@line:279

		b2Fixture* fixture = (b2Fixture*)addr;
		return fixture->GetRestitution();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetRestitution(JNIEnv* env, jobject object, jlong addr, jfloat restitution) {


//@line:289

		b2Fixture* fixture = (b2Fixture*)addr;
		fixture->SetRestitution(restitution);
	

}

