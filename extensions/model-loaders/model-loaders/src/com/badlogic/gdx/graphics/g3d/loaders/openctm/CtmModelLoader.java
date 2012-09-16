/*******************************************************************************
 * Copyright 2012 Daniel Heinrich.
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
package com.badlogic.gdx.graphics.g3d.loaders.openctm;

import java.util.ArrayList;
import java.util.List;

import darwin.jopenctm.data.AttributeData;
import darwin.jopenctm.io.CtmFileReader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static darwin.jopenctm.data.Mesh.*;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class CtmModelLoader implements ModelLoader {

    @Override
    public Model load(FileHandle file, ModelLoaderHints hints) {
        CtmFileReader reader = new CtmFileReader(file.read());
        try {
            darwin.jopenctm.data.Mesh ctmMesh = reader.decode();

            Mesh mesh = convert(ctmMesh);

            StillSubMesh ssm = new StillSubMesh(reader.getFileComment(),
                                                mesh, GL10.GL_TRIANGLES);
            StillModel model = new StillModel(new StillSubMesh[]{ssm});
            return model;
        } catch (Throwable ex) {
            throw new GdxRuntimeException("An error occured while loading model: "
                                          + file.name(), ex);
        }

    }

    private Mesh convert(darwin.jopenctm.data.Mesh ctmMesh) {

        if (ctmMesh.getVertexCount() > Short.MAX_VALUE) {
            throw new GdxRuntimeException("The indices exceed the range of SHORT!");
        }

        List<VertexAttribute> vas = new ArrayList<VertexAttribute>();

        VertexAttribute position = new VertexAttribute(Usage.Position,
                                                       CTM_POSITION_ELEMENT_COUNT,
                                                       ShaderProgram.POSITION_ATTRIBUTE);

        vas.add(position);

        VertexAttribute normal = null;
        if (ctmMesh.hasNormals()) {
            normal = new VertexAttribute(Usage.Normal,
                                         CTM_NORMAL_ELEMENT_COUNT,
                                         ShaderProgram.NORMAL_ATTRIBUTE);
            vas.add(normal);
        }

        VertexAttribute[] uvs = new VertexAttribute[ctmMesh.getUVCount()];
        if (ctmMesh.getUVCount() > 0) {
            uvs[0] = new VertexAttribute(Usage.TextureCoordinates,
                                         CTM_UV_ELEMENT_COUNT,
                                         ShaderProgram.TEXCOORD_ATTRIBUTE);
            vas.add(uvs[0]);
            if (ctmMesh.getUVCount() > 1) {
                for (int i = 1; i < ctmMesh.getUVCount(); ++i) {
                    AttributeData ad = ctmMesh.texcoordinates[i];
                    uvs[i] = new VertexAttribute(Usage.TextureCoordinates,
                                                 CTM_UV_ELEMENT_COUNT,
                                                 ad.name);
                    vas.add(uvs[i]);
                }
            }
        }

        VertexAttribute[] others = new VertexAttribute[ctmMesh.getAttrCount()];
        for (int i = 0; i < ctmMesh.getAttrCount(); ++i) {
            others[i] = new VertexAttribute(Usage.Generic,
                                            CTM_ATTR_ELEMENT_COUNT,
                                            ctmMesh.attributs[i].name);
            vas.add(others[i]);
        }

        Mesh m = new Mesh(true, ctmMesh.getVertexCount(),
                          ctmMesh.getTriangleCount() * 3,
                          vas.toArray(new VertexAttribute[0]));

        m.setIndices(convertIndices(ctmMesh.indices));

        int vsize = m.getVertexSize() / 4;
        float[] data = new float[vsize * ctmMesh.getVertexCount()];
        for (int i = 0; i < ctmMesh.getVertexCount(); i++) {
            //position data
            System.arraycopy(ctmMesh.vertices, i * CTM_POSITION_ELEMENT_COUNT,
                             data, i * vsize, CTM_POSITION_ELEMENT_COUNT);
            //normal
            if (normal != null) {
                System.arraycopy(ctmMesh.normals, i * CTM_NORMAL_ELEMENT_COUNT,
                                 data, i * vsize + normal.offset / 4, CTM_NORMAL_ELEMENT_COUNT);
            }
            //uvs
            for(VertexAttribute va: uvs)
            {                
                AttributeData ad=ctmMesh.texcoordinates[i];
                System.arraycopy(ad.values, i * CTM_UV_ELEMENT_COUNT,
                                 data, i * vsize + va.offset / 4, CTM_UV_ELEMENT_COUNT);
            }
            //other
            for(VertexAttribute va: others)
            {                
                AttributeData ad=ctmMesh.attributs[i];
                System.arraycopy(ad.values, i * CTM_ATTR_ELEMENT_COUNT,
                                 data, i * vsize + va.offset / 4, CTM_ATTR_ELEMENT_COUNT);
            }
        }

        m.setVertices(data);
        return m;
    }

    private static short[] convertIndices(int[] ind) {
        short[] r = new short[ind.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = (short) ind[i];
        }
        return r;
    }
}
