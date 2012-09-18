#include <com.badlogic.gdx.physics.box2d.Body.h>

//@line:28

#include <Box2D/Box2D.h>
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniCreateFixture__JJFFFZSSS(JNIEnv* env, jobject object, jlong addr, jlong shapeAddr, jfloat friction, jfloat restitution, jfloat density, jboolean isSensor, jshort filterCategoryBits, jshort filterMaskBits, jshort filterGroupIndex) {


//@line:84

	b2Body* body = (b2Body*)addr;
	b2Shape* shape = (b2Shape*)shapeAddr;
	b2FixtureDef fixtureDef;

	fixtureDef.shape = shape;
	fixtureDef.friction = friction;
	fixtureDef.restitution = restitution;
	fixtureDef.density = density;
	fixtureDef.isSensor = isSensor;
	fixtureDef.filter.maskBits = filterMaskBits;
	fixtureDef.filter.categoryBits = filterCategoryBits;
	fixtureDef.filter.groupIndex = filterGroupIndex;

	return (jlong)body->CreateFixture( &fixtureDef );
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniCreateFixture__JJF(JNIEnv* env, jobject object, jlong addr, jlong shapeAddr, jfloat density) {


//@line:116

		b2Body* body = (b2Body*)addr;
		b2Shape* shape = (b2Shape*)shapeAddr;
		return (jlong)body->CreateFixture( shape, density );
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniDestroyFixture(JNIEnv* env, jobject object, jlong addr, jlong fixtureAddr) {


//@line:134

		b2Body* body = (b2Body*)addr;
		b2Fixture* fixture = (b2Fixture*)fixtureAddr;
		body->DestroyFixture(fixture);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetTransform__JFFF(JNIEnv* env, jobject object, jlong addr, jfloat positionX, jfloat positionY, jfloat angle) {


//@line:169

		b2Body* body = (b2Body*)addr;
		body->SetTransform(b2Vec2(positionX, positionY), angle);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetTransform__JFFFZ(JNIEnv* env, jobject object, jlong addr, jfloat positionX, jfloat positionY, jfloat angle, jboolean updateContacts) {


//@line:174

		b2Body* body = (b2Body*)addr;
		body->SetTransform(b2Vec2(positionX, positionY), angle, updateContacts);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetTransform(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_vals) {
	float* vals = (float*)env->GetPrimitiveArrayCritical(obj_vals, 0);


//@line:186

		b2Body* body = (b2Body*)addr;
		b2Transform t = body->GetTransform();
		vals[0] = t.p.x;
		vals[1] = t.p.y;
		vals[2] = t.q.c;
		vals[3] = t.q.s;
	
	env->ReleasePrimitiveArrayCritical(obj_vals, vals, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetPosition(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_position) {
	float* position = (float*)env->GetPrimitiveArrayCritical(obj_position, 0);


//@line:206

		b2Body* body = (b2Body*)addr;
		b2Vec2 p = body->GetPosition();
		position[0] = p.x;
		position[1] = p.y;
	
	env->ReleasePrimitiveArrayCritical(obj_position, position, 0);

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetAngle(JNIEnv* env, jobject object, jlong addr) {


//@line:219

		b2Body* body = (b2Body*)addr;
		return body->GetAngle();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetWorldCenter(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_worldCenter) {
	float* worldCenter = (float*)env->GetPrimitiveArrayCritical(obj_worldCenter, 0);


//@line:234

		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetWorldCenter();
		worldCenter[0] = w.x;
		worldCenter[1] = w.y;
	
	env->ReleasePrimitiveArrayCritical(obj_worldCenter, worldCenter, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLocalCenter(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_localCenter) {
	float* localCenter = (float*)env->GetPrimitiveArrayCritical(obj_localCenter, 0);


//@line:251

		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLocalCenter();
		localCenter[0] = w.x;
		localCenter[1] = w.y;
	
	env->ReleasePrimitiveArrayCritical(obj_localCenter, localCenter, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetLinearVelocity(JNIEnv* env, jobject object, jlong addr, jfloat x, jfloat y) {


//@line:268

		b2Body* body = (b2Body*)addr;
		body->SetLinearVelocity(b2Vec2(x, y));
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLinearVelocity(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_linearVelocity) {
	float* linearVelocity = (float*)env->GetPrimitiveArrayCritical(obj_linearVelocity, 0);


//@line:283

		b2Body* body = (b2Body*)addr;
		b2Vec2 l = body->GetLinearVelocity();
		linearVelocity[0] = l.x;
		linearVelocity[1] = l.y;
	
	env->ReleasePrimitiveArrayCritical(obj_linearVelocity, linearVelocity, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetAngularVelocity(JNIEnv* env, jobject object, jlong addr, jfloat omega) {


//@line:295

		b2Body* body = (b2Body*)addr;
		body->SetAngularVelocity(omega);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetAngularVelocity(JNIEnv* env, jobject object, jlong addr) {


//@line:305

		b2Body* body = (b2Body*)addr;
		return body->GetAngularVelocity();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyForce(JNIEnv* env, jobject object, jlong addr, jfloat forceX, jfloat forceY, jfloat pointX, jfloat pointY) {


//@line:328

		b2Body* body = (b2Body*)addr;
		body->ApplyForce(b2Vec2(forceX, forceY), b2Vec2(pointX, pointY));
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyForceToCenter(JNIEnv* env, jobject object, jlong addr, jfloat forceX, jfloat forceY) {


//@line:346

		b2Body* body = (b2Body*)addr;
		body->ApplyForceToCenter(b2Vec2(forceX, forceY));
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyTorque(JNIEnv* env, jobject object, jlong addr, jfloat torque) {


//@line:358

		b2Body* body = (b2Body*)addr;
		body->ApplyTorque(torque);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyLinearImpulse(JNIEnv* env, jobject object, jlong addr, jfloat impulseX, jfloat impulseY, jfloat pointX, jfloat pointY) {


//@line:381

		b2Body* body = (b2Body*)addr;
		body->ApplyLinearImpulse( b2Vec2( impulseX, impulseY ), b2Vec2( pointX, pointY ) );
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyAngularImpulse(JNIEnv* env, jobject object, jlong addr, jfloat impulse) {


//@line:392

		b2Body* body = (b2Body*)addr;
		body->ApplyAngularImpulse(impulse);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetMass(JNIEnv* env, jobject object, jlong addr) {


//@line:403

		b2Body* body = (b2Body*)addr;
		return body->GetMass();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetInertia(JNIEnv* env, jobject object, jlong addr) {


//@line:414

		b2Body* body = (b2Body*)addr;
		return body->GetInertia();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetMassData(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_massData) {
	float* massData = (float*)env->GetPrimitiveArrayCritical(obj_massData, 0);


//@line:432

		b2Body* body = (b2Body*)addr;
		b2MassData m;
		body->GetMassData(&m);
		massData[0] = m.mass;
		massData[1] = m.center.x;
		massData[2] = m.center.y;
		massData[3] = m.I;
	
	env->ReleasePrimitiveArrayCritical(obj_massData, massData, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetMassData(JNIEnv* env, jobject object, jlong addr, jfloat mass, jfloat centerX, jfloat centerY, jfloat I) {


//@line:449

		b2Body* body = (b2Body*)addr;
		b2MassData m;
		m.mass = mass;
		m.center.x = centerX;
		m.center.y = centerY;
		m.I = I;
		body->SetMassData(&m);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniResetMassData(JNIEnv* env, jobject object, jlong addr) {


//@line:465

		b2Body* body = (b2Body*)addr;
		body->ResetMassData();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetWorldPoint(JNIEnv* env, jobject object, jlong addr, jfloat localPointX, jfloat localPointY, jfloatArray obj_worldPoint) {
	float* worldPoint = (float*)env->GetPrimitiveArrayCritical(obj_worldPoint, 0);


//@line:482

		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetWorldPoint( b2Vec2( localPointX, localPointY ) );
		worldPoint[0] = w.x;
		worldPoint[1] = w.y;
	
	env->ReleasePrimitiveArrayCritical(obj_worldPoint, worldPoint, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetWorldVector(JNIEnv* env, jobject object, jlong addr, jfloat localVectorX, jfloat localVectorY, jfloatArray obj_worldVector) {
	float* worldVector = (float*)env->GetPrimitiveArrayCritical(obj_worldVector, 0);


//@line:501

		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetWorldVector( b2Vec2( localVectorX, localVectorY ) );
		worldVector[0] = w.x;
		worldVector[1] = w.y;
	
	env->ReleasePrimitiveArrayCritical(obj_worldVector, worldVector, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLocalPoint(JNIEnv* env, jobject object, jlong addr, jfloat worldPointX, jfloat worldPointY, jfloatArray obj_localPoint) {
	float* localPoint = (float*)env->GetPrimitiveArrayCritical(obj_localPoint, 0);


//@line:520

		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLocalPoint( b2Vec2( worldPointX, worldPointY ) );
		localPoint[0] = w.x;
		localPoint[1] = w.y;
	
	env->ReleasePrimitiveArrayCritical(obj_localPoint, localPoint, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLocalVector(JNIEnv* env, jobject object, jlong addr, jfloat worldVectorX, jfloat worldVectorY, jfloatArray obj_worldVector) {
	float* worldVector = (float*)env->GetPrimitiveArrayCritical(obj_worldVector, 0);


//@line:539

		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLocalVector( b2Vec2( worldVectorX, worldVectorY ) );
		worldVector[0] = w.x;
		worldVector[1] = w.y;
	
	env->ReleasePrimitiveArrayCritical(obj_worldVector, worldVector, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLinearVelocityFromWorldPoint(JNIEnv* env, jobject object, jlong addr, jfloat worldPointX, jfloat worldPointY, jfloatArray obj_linVelWorld) {
	float* linVelWorld = (float*)env->GetPrimitiveArrayCritical(obj_linVelWorld, 0);


//@line:558

		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLinearVelocityFromWorldPoint( b2Vec2( worldPointX, worldPointY ) );
		linVelWorld[0] = w.x;
		linVelWorld[1] = w.y;
	
	env->ReleasePrimitiveArrayCritical(obj_linVelWorld, linVelWorld, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLinearVelocityFromLocalPoint(JNIEnv* env, jobject object, jlong addr, jfloat localPointX, jfloat localPointY, jfloatArray obj_linVelLoc) {
	float* linVelLoc = (float*)env->GetPrimitiveArrayCritical(obj_linVelLoc, 0);


//@line:577

		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLinearVelocityFromLocalPoint( b2Vec2( localPointX, localPointY ) );
		linVelLoc[0] = w.x;
		linVelLoc[1] = w.y;
	
	env->ReleasePrimitiveArrayCritical(obj_linVelLoc, linVelLoc, 0);

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLinearDamping(JNIEnv* env, jobject object, jlong addr) {


//@line:589

		b2Body* body = (b2Body*)addr;
		return body->GetLinearDamping();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetLinearDamping(JNIEnv* env, jobject object, jlong addr, jfloat linearDamping) {


//@line:599

		b2Body* body = (b2Body*)addr;
		body->SetLinearDamping(linearDamping);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetAngularDamping(JNIEnv* env, jobject object, jlong addr) {


//@line:609

		b2Body* body = (b2Body*)addr;
		return body->GetAngularDamping();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetAngularDamping(JNIEnv* env, jobject object, jlong addr, jfloat angularDamping) {


//@line:619

		b2Body* body = (b2Body*)addr;
		body->SetAngularDamping(angularDamping);
	

}


//@line:630

inline b2BodyType getBodyType( int type )
{
	switch( type )
	{
	case 0: return b2_staticBody;
	case 1: return b2_kinematicBody;
	case 2: return b2_dynamicBody;
	default:
		return b2_staticBody;
	}
}	 
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetType(JNIEnv* env, jobject object, jlong addr, jint type) {


//@line:644

		b2Body* body = (b2Body*)addr;
		body->SetType(getBodyType(type));
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetType(JNIEnv* env, jobject object, jlong addr) {


//@line:658

		b2Body* body = (b2Body*)addr;
		return body->GetType();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetBullet(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:668

		b2Body* body = (b2Body*)addr;
		body->SetBullet(flag);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsBullet(JNIEnv* env, jobject object, jlong addr) {


//@line:678

		b2Body* body = (b2Body*)addr;
		return body->IsBullet();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetSleepingAllowed(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:688

		b2Body* body = (b2Body*)addr;
		body->SetSleepingAllowed(flag);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsSleepingAllowed(JNIEnv* env, jobject object, jlong addr) {


//@line:698

		b2Body* body = (b2Body*)addr;
		return body->IsSleepingAllowed();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetAwake(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:709

		b2Body* body = (b2Body*)addr;
		body->SetAwake(flag);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsAwake(JNIEnv* env, jobject object, jlong addr) {


//@line:720

		b2Body* body = (b2Body*)addr;
		return body->IsAwake();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetActive(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:735

		b2Body* body = (b2Body*)addr;
		body->SetActive(flag);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsActive(JNIEnv* env, jobject object, jlong addr) {


//@line:745

		b2Body* body = (b2Body*)addr;
		return body->IsActive();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetFixedRotation(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:755

		b2Body* body = (b2Body*)addr;
		body->SetFixedRotation(flag);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsFixedRotation(JNIEnv* env, jobject object, jlong addr) {


//@line:765

		b2Body* body = (b2Body*)addr;
		return body->IsFixedRotation();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetGravityScale(JNIEnv* env, jobject object, jlong addr) {


//@line:793

		b2Body* body = (b2Body*)addr;
		return body->GetGravityScale();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetGravityScale(JNIEnv* env, jobject object, jlong addr, jfloat scale) {


//@line:803

		b2Body* body = (b2Body*)addr;
		body->SetGravityScale(scale);
	

}

