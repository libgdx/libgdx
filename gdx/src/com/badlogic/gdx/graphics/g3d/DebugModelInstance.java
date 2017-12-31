/*******************************************************************************
 * Copyright 2017 See AUTHORS file.
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

package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * <p>Convenience wrapper for {@link ModelInstance} which can be used for displaying a 
 * wireframe version of a model to aid in debugging.</p>
 *
 * <p>For example, to render a model in wireframe, use:</p>
 *
 * <pre>
 * {@code
 * Model model = ...;
 * ModelInstance model = DebugModelInstance(model).setPrimitiveType(GL20.GL_TRIANGLE_STRIP);
 * }
 * </pre>
 * @author kalexmills */
public class DebugModelInstance extends ModelInstance {

  private int glPrimitiveType;

  public DebugModelInstance(Model model) {
    super(model);
  }

  public DebugModelInstance(Model model, String nodeId, boolean mergeTransform) {
    super(model, nodeId, mergeTransform);
  }

  public DebugModelInstance(Model model, Matrix4 transform, String nodeId, boolean mergeTransform) {
    super(model, transform, nodeId, mergeTransform);
  }

  public DebugModelInstance(Model model, String nodeId, boolean parentTransform, boolean mergeTransform) {
    super(model, nodeId, parentTransform, mergeTransform);
  }

  public DebugModelInstance(Model model, Matrix4 transform, String nodeId, boolean parentTransform, boolean mergeTransform) {
    super(model, transform, nodeId, parentTransform, mergeTransform);
  }

  public DebugModelInstance(Model model, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
    super(model, nodeId, recursive, parentTransform, mergeTransform);
  }

  public DebugModelInstance(Model model, Matrix4 transform, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
    super(model, transform, nodeId, recursive, parentTransform, mergeTransform);
  }

  public DebugModelInstance(Model model, Matrix4 transform, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform, boolean shareKeyframes) {
    super(model, transform, nodeId, recursive, parentTransform, mergeTransform, shareKeyframes);
  }

  public DebugModelInstance(Model model, String... rootNodeIds) {
    super(model, rootNodeIds);
  }

  public DebugModelInstance(Model model, Matrix4 transform, String... rootNodeIds) {
    super(model, transform, rootNodeIds);
  }

  public DebugModelInstance(Model model, Array<String> rootNodeIds) {
    super(model, rootNodeIds);
  }

  public DebugModelInstance(Model model, Matrix4 transform, Array<String> rootNodeIds) {
    super(model, transform, rootNodeIds);
  }

  public DebugModelInstance(Model model, Matrix4 transform, Array<String> rootNodeIds, boolean shareKeyframes) {
    super(model, transform, rootNodeIds, shareKeyframes);
  }

  public DebugModelInstance(Model model, Vector3 position) {
    super(model, position);
  }

  public DebugModelInstance(Model model, float x, float y, float z) {
    super(model, x, y, z);
  }

  public DebugModelInstance(Model model, Matrix4 transform) {
    super(model, transform);
  }

  public DebugModelInstance(ModelInstance copyFrom) {
    super(copyFrom);
  }

  public DebugModelInstance(ModelInstance copyFrom, Matrix4 transform) {
    super(copyFrom, transform);
  }

  public DebugModelInstance(ModelInstance copyFrom, Matrix4 transform, boolean shareKeyframes) {
    super(copyFrom, transform, shareKeyframes);
  }

  /**
   * Sets the primitive type used to render this model instance.
   * @param primitiveType int primitiveType to be set on Renderable.
   */
  public void setPrimitiveType(int primitiveType) {
    this.glPrimitiveType = primitiveType;
  }

  /**
   * Sets the primitiveType to wireframe.
   * @inheritDoc
   */
  public Renderable getRenderable (final Renderable out, final Node node, final NodePart nodePart) {
    super.getRenderable(out, node, nodePart);
    out.meshPart.primitiveType = glPrimitiveType;
    return out;
  }

}

