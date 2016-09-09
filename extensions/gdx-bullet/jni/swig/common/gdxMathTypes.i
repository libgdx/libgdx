/*
 * Use some libgdx types instead of Bullet types.
 */

// Vector3:
%{
#include <gdx/linearmath/mathtypes.h>
#include <LinearMath/btVector3.h>
#include <LinearMath/btQuaternion.h>
#include <LinearMath/btMatrix3x3.h>
#include <LinearMath/btTransform.h>
%}

CREATE_POOLED_METHODS(Vector3, "com/badlogic/gdx/physics/bullet/linearmath/LinearMath");
CREATE_POOLED_TYPEMAP(btVector3, Vector3, "Lcom/badlogic/gdx/math/Vector3;", Vector3_to_btVector3, btVector3_to_Vector3);
ENABLE_POOLED_TYPEMAP(btVector3, Vector3, "Lcom/badlogic/gdx/math/Vector3;");

CREATE_POOLED_METHODS(Quaternion, "com/badlogic/gdx/physics/bullet/linearmath/LinearMath");
CREATE_POOLED_TYPEMAP(btQuaternion, Quaternion, "Lcom/badlogic/gdx/math/Quaternion;", Quaternion_to_btQuaternion, btQuaternion_to_Quaternion);
ENABLE_POOLED_TYPEMAP(btQuaternion, Quaternion, "Lcom/badlogic/gdx/math/Quaternion;");

CREATE_POOLED_METHODS(Matrix3, "com/badlogic/gdx/physics/bullet/linearmath/LinearMath");
CREATE_POOLED_TYPEMAP(btMatrix3x3, Matrix3, "Lcom/badlogic/gdx/math/Matrix3;", Matrix3_to_btMatrix3, btMatrix3_to_Matrix3);
ENABLE_POOLED_TYPEMAP(btMatrix3x3, Matrix3, "Lcom/badlogic/gdx/math/Matrix3;");

CREATE_POOLED_METHODS(Matrix4, "com/badlogic/gdx/physics/bullet/linearmath/LinearMath");
CREATE_POOLED_TYPEMAP(btTransform, Matrix4, "Lcom/badlogic/gdx/math/Matrix4;", Matrix4_to_btTransform, btTransform_to_Matrix4);
ENABLE_POOLED_TYPEMAP(btTransform, Matrix4, "Lcom/badlogic/gdx/math/Matrix4;");

