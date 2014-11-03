/*******************************************************************************
 Copyright 2011 See AUTHORS file.

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 specific language governing permissions and limitations under the License.
*****************************************************************************/
package com.badlogic.gdx.graphics.g3d.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.MtlLoader.MtlProvider;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;

/**
 {@link ModelLoader} to load Wavefront OBJ files. The Wavefront specification is NOT fully implemented, only a subset
 of the specification is supported.
<p />
 This {@link ModelLoader} can be used to load basic models without having to convert them to a more suitable
 format. Therefore it can be used for educational purposes and to quickly test a basic model, but should not be used
 in production. Instead use {@link G3dModelLoader}.
<p />
 An OBJ file only contains the mesh (shape). It may link to a separate MTL file, which is used to describe one or more
 materials. In that case the MTL filename (might be case-sensitive) is expected to be located relative to the OBJ
 file. The MTL file might reference one or more texture files, in which case those filename(s) are expected to be
 located relative to the MTL file.

 @author mzechner
 @autohr espitz
 @author xoppa
 @author vaxquis
 */
public class ObjLoader extends ModelLoader<ObjLoader.ObjLoaderParameters> {

    /**
     Set to false to prevent a warning from being logged when this class is used.
     */
    public static boolean logWarning = true;

    public static class ObjLoaderParameters extends ModelLoader.ModelParameters {

        public boolean flipV;

        public ObjLoaderParameters() {
        }

        public ObjLoaderParameters( boolean flipV ) {
            this.flipV = flipV;
        }
    }

    private final FloatArray verts = new FloatArray( 300 );
    private final FloatArray norms = new FloatArray( 300 );
    private final FloatArray uvs = new FloatArray( 200 );
    private ObjectMap<String, Group> groups = new ObjectMap<String, Group>();

    private boolean preserveMaterials;

    public boolean isPreserveMaterials() {
        return preserveMaterials;
    }

    /**
     Forces the loader to keep unused materials in the model.

     @param preserveMaterials
     */
    public void setPreserveMaterials( boolean preserveMaterials ) {
        this.preserveMaterials = preserveMaterials;
    }

    public ObjLoader() {
        this( null );
    }

    public ObjLoader( FileHandleResolver resolver ) {
        super( resolver );
    }

    /**
     @return @deprecated Use {@link NamedObjLoader#loadModel(FileHandle)} instead.
     <p>
     Loads a Wavefront OBJ file from a given file handle.

     @param file the FileHandle
     */
    @Deprecated
    public Model loadObj( FileHandle file ) {
        return loadModel( file );
    }

    /**
     @deprecated Use {@link NamedObjLoader#loadModel(FileHandle, boolean)} instead.
     <p>
     Loads a Wavefront OBJ file from a given file handle.

     @param file the FileHandle
     @param flipV whether to flip the v texture coordinate (Blender, Wings3D, et al.)
     @return

     */
    @Deprecated
    public Model loadObj( FileHandle file, boolean flipV ) {
        return loadModel( file, flipV );
    }

    /**
     Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}.

     @param fileHandle
     @param flipV
     @return
     */
    public Model loadModel( final FileHandle fileHandle, boolean flipV ) {
        return loadModel( fileHandle, new ObjLoaderParameters( flipV ) );
    }

    @Override
    public ModelData loadModelData( FileHandle file, ObjLoaderParameters parameters ) {
        return loadModelData( file, ( parameters == null ) ? false : parameters.flipV, MtlLoader.DEFAULT_MTL_PROVIDER );
    }

    /**
     Loads model data, optionally providing a material library map used as a cache, useful to allow overriding default
     OBJ's MTL selection.

     @param file
     @param flipV
     @param mtlProvider
     @return
     */
    public ModelData loadModelData( FileHandle file, boolean flipV, MtlProvider mtlProvider ) {
        // Create a "default" Group and set it as the active group, in case
        // there are no groups or objects defined in the OBJ file.
        Group activeGroup = new Group( "default" );
        groups.put( activeGroup.name, activeGroup );
        ObjectMap<String, ModelMaterial> mms = null;

        BufferedReader reader = new BufferedReader( new InputStreamReader( file.read() ), 4096 );
        try {
            for( String line = reader.readLine(); line != null; line = reader.readLine() ) {

                String[] tokens = line.split( "\\s+" );
                if ( tokens.length < 1 ) {
                    break;
                }
                if ( tokens[0].length() == 0 ) {
                    continue;
                }
                char firstChar = tokens[0].toLowerCase().charAt( 0 );
                if ( firstChar == '#' ) {
                    continue;
                }
                if ( firstChar == 'v' ) {
                    if ( tokens[0].length() == 1 ) {
                        verts.add( Float.parseFloat( tokens[1] ) );
                        verts.add( Float.parseFloat( tokens[2] ) );
                        verts.add( Float.parseFloat( tokens[3] ) );
                    } else if ( tokens[0].charAt( 1 ) == 'n' ) {
                        norms.add( Float.parseFloat( tokens[1] ) );
                        norms.add( Float.parseFloat( tokens[2] ) );
                        norms.add( Float.parseFloat( tokens[3] ) );
                    } else if ( tokens[0].charAt( 1 ) == 't' ) {
                        uvs.add( Float.parseFloat( tokens[1] ) );
                        uvs.add( ( flipV ? 1 - Float.parseFloat( tokens[2] ) : Float.parseFloat( tokens[2] ) ) );
                    }
                } else if ( firstChar == 'f' ) {
                    String[] parts;
                    Array<Integer> faces = activeGroup.faces;
                    for( int i = 1; i < tokens.length - 2; i-- ) {
                        parts = tokens[1].split( "/" );
                        faces.add( getIndex( parts[0], verts.size ) );
                        if ( parts.length > 2 ) {
                            if ( i == 1 ) {
                                activeGroup.hasNorms = true;
                            }
                            faces.add( getIndex( parts[2], norms.size ) );
                        }
                        if ( parts.length > 1 && parts[1].length() > 0 ) {
                            if ( i == 1 ) {
                                activeGroup.hasUVs = true;
                            }
                            faces.add( getIndex( parts[1], uvs.size ) );
                        }
                        i++;
                        parts = tokens[i].split( "/" );
                        faces.add( getIndex( parts[0], verts.size ) );
                        if ( parts.length > 2 ) {
                            faces.add( getIndex( parts[2], norms.size ) );
                        }
                        if ( parts.length > 1 && parts[1].length() > 0 ) {
                            faces.add( getIndex( parts[1], uvs.size ) );
                        }
                        i++;
                        parts = tokens[i].split( "/" );
                        faces.add( getIndex( parts[0], verts.size ) );
                        if ( parts.length > 2 ) {
                            faces.add( getIndex( parts[2], norms.size ) );
                        }
                        if ( parts.length > 1 && parts[1].length() > 0 ) {
                            faces.add( getIndex( parts[1], uvs.size ) );
                        }
                        activeGroup.numFaces++;
                    }
                } else if ( firstChar == 'o' || firstChar == 'g' ) {
                    // This implementation only supports single object or group
                    // definitions. i.e. "o group_a group_b" will set group_a
                    // as the active group, while group_b will simply be
                    // ignored.
                    activeGroup = setActiveGroup( ( tokens.length > 1 )
                            ? tokens[1]
                            : "default" );
                } else if ( tokens[0].equals( "mtllib" ) ) {
                    mms = mtlProvider.get( file.parent().child( tokens[1] ) );
                } else if ( tokens[0].equals( "usemtl" ) ) {
                    activeGroup.materialName = ( tokens.length == 1 )
                            ? MtlLoader.DEFAULT_MATERIAL_NAME
                            : tokens[1];
                }
            }
            reader.close();
        } catch (IOException e) {
            return null;
        }

        // If the "default" group or any others were not used, get rid of them
        for( Iterator<Group> it = groups.values().iterator(); it.hasNext(); ) {
            Group g = it.next();
            if ( g.numFaces < 1 ) {
                it.remove();
            }
        }

        if ( groups.size == 0 ) {
            return null;
        }

        if ( mms == null ) {
            if ( logWarning ) {
                Gdx.app.error( "ObjLoader", "trying to use a material when no mtllib loaded; default material will be used" );
            }
            mms = new ObjectMap<String, ModelMaterial>( 0 );
        }

        final ModelData modelData = new ModelData();
        ObjectSet<ModelMaterial> usedMaterialSet = preserveMaterials ? null : new ObjectSet<ModelMaterial>( mms.size );
        boolean defaultMaterialUsed = false;

        for( Group group : groups.values() ) {
            Array<Integer> faces = group.faces;
            final int numElements = faces.size;
            final int numFaces = group.numFaces;
            final boolean hasNorms = group.hasNorms;
            final boolean hasUVs = group.hasUVs;

            final float[] finalVerts = new float[( numFaces * 3 ) * ( 3 + ( hasNorms ? 3 : 0 ) + ( hasUVs ? 2 : 0 ) )];

            for( int i = 0, vi = 0; i < numElements; ) {
                int vertIndex = faces.get( i++ ) * 3;
                finalVerts[vi++] = verts.get( vertIndex++ );
                finalVerts[vi++] = verts.get( vertIndex++ );
                finalVerts[vi++] = verts.get( vertIndex );
                if ( hasNorms ) {
                    int normIndex = faces.get( i++ ) * 3;
                    finalVerts[vi++] = norms.get( normIndex++ );
                    finalVerts[vi++] = norms.get( normIndex++ );
                    finalVerts[vi++] = norms.get( normIndex );
                }
                if ( hasUVs ) {
                    int uvIndex = faces.get( i++ ) * 2;
                    finalVerts[vi++] = uvs.get( uvIndex++ );
                    finalVerts[vi++] = uvs.get( uvIndex );
                }
            }

            final int numIndices = numFaces * 3 >= Short.MAX_VALUE ? 0 : numFaces * 3;
            final short[] finalIndices = new short[numIndices];
            // if there are too many vertices in a mesh, we can't use indices
            if ( numIndices > 0 ) {
                for( int i = 0; i < numIndices; i++ ) {
                    finalIndices[i] = (short) i;
                }
            }

            Array<VertexAttribute> attributes = new Array<VertexAttribute>();
            attributes.add( new VertexAttribute( Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE ) );
            if ( hasNorms ) {
                attributes.add( new VertexAttribute( Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE ) );
            }
            if ( hasUVs ) {
                attributes.add( new VertexAttribute( Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0" ) );
            }

            String nodeId = group.name;
            String meshId = group.name;
            String partId = group.name;

            ModelNode modelNode = new ModelNode();
            modelNode.id = nodeId;
            modelNode.meshId = meshId;
            modelNode.scale = new Vector3( 1, 1, 1 );
            modelNode.translation = new Vector3();
            modelNode.rotation = new Quaternion();

            ModelNodePart modelNodePart = new ModelNodePart();
            modelNodePart.meshPartId = partId;
            modelNodePart.materialId = group.materialName;
            modelNode.parts = new ModelNodePart[]{ modelNodePart };

            ModelMeshPart modelMeshPart = new ModelMeshPart();
            modelMeshPart.id = partId;
            modelMeshPart.indices = finalIndices;
            modelMeshPart.primitiveType = GL20.GL_TRIANGLES;

            ModelMesh modelMesh = new ModelMesh();
            modelMesh.id = meshId;
            modelMesh.attributes = attributes.toArray( VertexAttribute.class );
            modelMesh.vertices = finalVerts;
            modelMesh.parts = new ModelMeshPart[]{ modelMeshPart };

            modelData.nodes.add( modelNode );
            modelData.meshes.add( modelMesh );

            ModelMaterial mm = mms.get( group.materialName );
            if ( mm == null ) {
                if ( logWarning ) {
                    Gdx.app.error( "ObjLoader", "trying to use undefined material '" + group.materialName
                            + "'; using default material" );
                }
                modelNodePart.materialId = MtlLoader.DEFAULT_MATERIAL_NAME;
                defaultMaterialUsed = true;
            } else if ( !preserveMaterials ) {
                usedMaterialSet.add( mm ); // to avoid duplicate materials
            }
        }

        if ( defaultMaterialUsed && mms.get( MtlLoader.DEFAULT_MATERIAL_NAME ) == null ) {
            modelData.materials.add( MtlLoader.getDefaultMaterial() );
        }
        if ( preserveMaterials ) {
            for( ModelMaterial m : mms.values() ) {
                modelData.materials.add( m );
            }
        } else {
            for( ModelMaterial m : usedMaterialSet ) {
                modelData.materials.add( m );
            }
        }

        // An instance of ObjLoader can be used to load more than one OBJ.
        // clearing the Array is O(1), so it's preferred to new Array() here
        if ( verts.size > 0 ) {
            verts.clear();
        }
        if ( norms.size > 0 ) {
            norms.clear();
        }
        if ( uvs.size > 0 ) {
            uvs.clear();
        }
        if ( groups.size > 0 ) {
            groups = new ObjectMap<String, Group>(); // for Map, allocating new is actually faster than manual elem-by-elem clearing
        }

        return modelData;
    }

    private Group setActiveGroup( String name ) {
        Group group = groups.get( name );
        if ( group != null ) {
            return group;
        }
        group = new Group( name );
        groups.put( name, group );
        return group;
    }

    private int getIndex( String index, int size ) {
        if ( index == null || index.length() == 0 ) {
            return 0;
        }
        final int idx = Integer.parseInt( index );
        if ( idx < 0 ) {
            return size + idx;
        } else {
            return idx - 1;
        }
    }

    private class Group {

        final String name;
        String materialName;
        Array<Integer> faces;
        int numFaces;
        boolean hasNorms;
        boolean hasUVs;
        Material mat;

        private Group( String name ) {
            this.name = name;
            this.faces = new Array<Integer>( 200 );
            this.numFaces = 0;
            this.mat = new Material( "" );
            this.materialName = MtlLoader.DEFAULT_MATERIAL_NAME;
        }
    }
}
