%module(directors="1") InverseDynamics

%include "arrays_java.i"

%import "../softbody/softbody.i"

%include "../common/gdxCommon.i"

%include "../../swig-src/linearmath/classes.i"
%include "../../swig-src/collision/classes.i"
%include "../../swig-src/dynamics/classes.i"
%include "../../swig-src/softbody/classes.i"

%typemap(javaimports) SWIGTYPE	%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
%}
%pragma(java) jniclassimports=%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool;
%}
%pragma(java) moduleimports=%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
%}

%{
#include <BulletInverseDynamics/IDConfig.hpp>
%}
%include "BulletInverseDynamics/IDConfig.hpp"

%{
#include <BulletInverseDynamics/IDErrorMessages.hpp>
%}
%include "BulletInverseDynamics/IDErrorMessages.hpp"

%{
#include <BulletInverseDynamics/IDMath.hpp>
%}
%include "BulletInverseDynamics/IDMath.hpp"

%rename(finalizeInternal) btInverseDynamicsBullet3::MultiBodyTree::finalize();
%{
#include <BulletInverseDynamics/MultiBodyTree.hpp>
%}
%include "BulletInverseDynamics/MultiBodyTree.hpp"

/*
%rename(operatorSubscriptConst) btVectorX< idScalar >::operator [](int) const;
%template(btVectorXIdScalar) btVectorX< idScalar >;
%template(btMatrixXIdScalar) btMatrixX< idScalar >;
%rename(operatorFunctionCallConst) btInverseDynamicsBullet3::vec3::operator ()(int) const;
%rename(operatorFunctionCallConst) btInverseDynamicsBullet3::mat33::operator ()(int,int) const;
%rename(operatorFunctionCallConst) btInverseDynamicsBullet3::vecx::operator ()(int) const;
%{
#include <BulletInverseDynamics/details/IDLinearMathInterface.hpp>
%}
%include "BulletInverseDynamics/details/IDLinearMathInterface.hpp"

%{
#include <BulletInverseDynamics/details/MultiBodyTreeImpl.hpp>
%}
%include "BulletInverseDynamics/details/MultiBodyTreeImpl.hpp"

%{
#include <BulletInverseDynamics/details/MultiBodyTreeInitCache.hpp>
%}
%include "BulletInverseDynamics/details/MultiBodyTreeInitCache.hpp"
*/
%{
using namespace btInverseDynamicsBullet3;
%}
