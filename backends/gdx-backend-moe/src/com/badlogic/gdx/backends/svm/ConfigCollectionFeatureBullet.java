/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.svm;

import com.oracle.svm.core.jni.JNIRuntimeAccess;
import org.graalvm.nativeimage.hosted.Feature;

import java.util.ArrayList;
import java.util.Collections;

public class ConfigCollectionFeatureBullet implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.linearmath.LinearMath"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btBroadphasePair"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btBroadphaseAabbCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btBroadphaseRayCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btNodeOverlapCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btOverlapCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btOverlapFilterCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btTriangleCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btInternalTriangleIndexCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ICollide"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btConvexTriangleCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.RayResultCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.AllHitsRayResultCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ConvexResultCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ClosestConvexResultCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ContactResultCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btTriangleRaycastCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btTriangleConvexcastCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.CustomCollisionDispatcher"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ContactListener"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ContactCache"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.dynamics.InternalTickCallback"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.dynamics.CustomActionInterface"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.extras.btBulletWorldImporter"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw"));
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.linearmath.btMotionState"));
        JNIRuntimeAccess.register(access.findClassByName("java.math.BigInteger"));

        // TODO: 21.06.2021 Add the methods more selective
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.linearmath.LinearMathJNI").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.extras.ExtrasJNI").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.CollisionJNI").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.dynamics.DynamicsJNI").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btBroadphasePair").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btBroadphaseAabbCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btBroadphaseRayCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btNodeOverlapCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btOverlapFilterCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btTriangleCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btInternalTriangleIndexCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ICollide").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btConvexTriangleCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.RayResultCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.AllHitsRayResultCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ConvexResultCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ClosestConvexResultCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ContactResultCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btTriangleRaycastCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btTriangleConvexcastCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.CustomCollisionDispatcher").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ContactListener").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.ContactCache").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.dynamics.InternalTickCallback").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.dynamics.CustomActionInterface").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.extras.btBulletWorldImporter").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.linearmath.btMotionState").getDeclaredMethods());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.utils.Pool").getDeclaredMethods());

        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.math.Vector3").getDeclaredFields());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.math.Matrix4").getDeclaredFields());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.linearmath.LinearMath").getDeclaredFields());
        JNIRuntimeAccess.register(access.findClassByName("com.badlogic.gdx.physics.bullet.collision.btBroadphasePair").getDeclaredFields());

        try {
            JNIRuntimeAccess.register(access.findClassByName("java.math.BigInteger").getConstructor(byte[].class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public <T> T[] concatArrays(T[]... arrays) {
        ArrayList<T> list = new ArrayList<>();
        for (T[] array : arrays) {
            Collections.addAll(list, array);
        }
        return list.toArray(arrays[0]);
    }
}
