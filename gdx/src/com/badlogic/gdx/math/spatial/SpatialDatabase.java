package com.badlogic.gdx.math.spatial;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;

import java.util.Set;

public interface SpatialDatabase {

    void insert(Object userObject, Vector3 point);

    void remove(Object userObject);

    Set<Object> queryNear(Frustum frustum);

    Set<Object> queryNear(Vector3 point);

}
