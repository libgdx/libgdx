/*******************************************************************************
 Copyright 2014 See AUTHORS file.

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 specific language governing permissions and limitations under the License.
 *****************************************************************************/
package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.utils.Array;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

/**
 A dedicated, stand-alone, static-class Wavefront MTL (.mtl) parser/loader, intended for use with ObjLoader.
 <p />
 Currently supports all of most common MTL parameters. Note that you have to have a shader capable of processing the various Texture
 map/color attributes to be actually able to benefit from it.

 @author vaxquis
 */
public class MtlLoader {

    public static interface MtlProvider {
        ObjectMap<String, ModelMaterial> get( FileHandle fileHandle );
    }

    /**
     An MTL provider allowing to ignore explicit MTL naming in mtllib statements, providing all materials from a
     single file.
     */
    public static class SingleFileMtlProvider implements MtlProvider {

        private final ObjectMap<String, ModelMaterial> singleMtl;

        public SingleFileMtlProvider( String filename ) {
            singleMtl = MtlLoader.load( filename );
        }

        public SingleFileMtlProvider( FileHandle fileHandle ) {
            singleMtl = MtlLoader.load( fileHandle );
        }

        @Override
        public ObjectMap<String, ModelMaterial> get( FileHandle fileHandle ) {
            return singleMtl;
        }

    }

    /**
     Default MTL provider; simply loads an MTL file from a requested handle. Suppresses all exceptions, but emits
     Gdx.app.error() warnings on them if logWarnings is set.
     */
    public static MtlProvider DEFAULT_MTL_PROVIDER = new MtlProvider() {

        @Override
        public ObjectMap<String, ModelMaterial> get( FileHandle fileHandle ) {
            return MtlLoader.load( fileHandle );
        }
    };

    private static ObjectMap<String, Integer> usageMap;
    private static ObjectSet<String> unsupportedSet;
    private static ModelMaterial DEFAULT_MATERIAL;
    static final String DEFAULT_MATERIAL_NAME = "default";
    public static boolean logWarnings = true;

    private static void initStaticData() {
        if ( usageMap != null ) {
            return;
        }
        usageMap = new ObjectMap<String, Integer>( 10 );
        usageMap.put( "map_ka", ModelTexture.USAGE_AMBIENT );
        usageMap.put( "map_kd", ModelTexture.USAGE_DIFFUSE );
        usageMap.put( "map_ks", ModelTexture.USAGE_SPECULAR );
        usageMap.put( "map_ke", ModelTexture.USAGE_EMISSIVE ); // popular extension
        usageMap.put( "map_ns", ModelTexture.USAGE_SHININESS );
        usageMap.put( "map_d", ModelTexture.USAGE_TRANSPARENCY );
        usageMap.put( "map_bump", ModelTexture.USAGE_BUMP ); // popular extension
        usageMap.put( "bump", ModelTexture.USAGE_BUMP );
        usageMap.put( "refl", ModelTexture.USAGE_REFLECTION );
        usageMap.put( "disp", ModelTexture.USAGE_NORMAL );
        unsupportedSet = new ObjectSet<String>( 6 );
        unsupportedSet.add( "illum" ); // illumination model - unsupported
        unsupportedSet.add( "tf" ); // transmission filter - currently unsupported
        unsupportedSet.add( "ni" ); // optical density - currently unsupported
        unsupportedSet.add( "sharpness" ); // reflection sharpness - currently unsupported
        unsupportedSet.add( "map_aat" ); // per-texture antialiasing - currently unsupported
        unsupportedSet.add( "decal" ); // decal scalar texture - currently unsupported
    }

    private MtlLoader() {
        throw new UnsupportedOperationException();
    }

    private static Color parseColor( String[] tokens ) {
        float r = Float.parseFloat( tokens[1] );
        float g = Float.parseFloat( tokens[2] );
        float b = Float.parseFloat( tokens[3] );
        float a = ( tokens.length <= 4 )
                ? 1
                : Float.parseFloat( tokens[4] );
        return new Color( r, g, b, a );
    }

    private static ModelMaterial createMaterial( String name ) {
        ModelMaterial mat = new ModelMaterial();
        mat.diffuse = new Color( Color.WHITE );
        mat.specular = new Color( Color.WHITE );
        mat.id = name;
        return mat;
    }

    static ModelMaterial getDefaultMaterial() {
        if ( DEFAULT_MATERIAL == null ) {
            DEFAULT_MATERIAL = createMaterial( DEFAULT_MATERIAL_NAME );
        }
        return DEFAULT_MATERIAL;
    }

    /**
     Loads a Wavefront MTL material file from an internal file with a given name.

     @param filename
     @return
     */
    public static ObjectMap<String, ModelMaterial> load( String filename ) {
        return load( Gdx.files.internal( filename ) );
    }

    /**
     Loads a Wavefront MTL material file for a file handle.

     @param file
     @return
     */
    @SuppressWarnings( "AssignmentToForLoopParameter" )
    public static ObjectMap<String, ModelMaterial> load( FileHandle file ) {
        if ( !file.exists() ) {
            throw new IllegalArgumentException( "file '" + file + "' doesn't exist" );
        }

        initStaticData();

        ObjectMap<String, ModelMaterial> materials = new ObjectMap<String, ModelMaterial>();

        ModelMaterial mat = getDefaultMaterial(); // to allow parsing slightly malformed MTL files (no newmtl statement)
        BufferedReader reader = null;
        try {
            reader = new BufferedReader( new InputStreamReader( file.read() ), 4096 );
            for( String line = reader.readLine(); line != null; line = reader.readLine() ) {
                line = line.trim();
                String[] tokens = line.split( "\\s+" );

                if ( tokens[0].length() == 0 || tokens[0].charAt( 0 ) == '#' ) {
                    continue;
                }
                final String key = tokens[0].toLowerCase();
                if ( key.equals( "newmtl" ) ) {
                    mat = ( tokens.length <= 1 )
                            ? getDefaultMaterial()
                            : createMaterial( tokens[1].replace( '.', '_' ) );
                    materials.put( mat.id, mat );
                } else if ( key.equals( "ka" ) ) {
                    mat.ambient = parseColor( tokens );
                } else if ( key.equals( "kd" ) ) {
                    mat.diffuse = parseColor( tokens );
                } else if ( key.equals( "ks" ) ) {
                    mat.specular = parseColor( tokens );
                } else if ( key.equals( "ke" ) ) {
                    mat.emissive = parseColor( tokens );
                } else if ( key.equals( "tr" ) || key.equals( "d" ) ) {
                    mat.opacity = Float.parseFloat( tokens[1] );
                } else if ( key.equals( "ns" ) ) {
                    mat.shininess = Float.parseFloat( tokens[1] );
                } else if ( key.equals( "d" ) ) {
                    mat.opacity = Float.parseFloat( tokens[1] );
                } else if ( unsupportedSet.contains( key ) ) {
                    // Gdx.app.error( "MtlLoader", "unsupported MTL statement '" + tokens[0] + "'" );
                } else {
                    Integer usage = usageMap.get( key );
                    if ( usage != null ) {
                        ModelTexture tex = new ModelTexture();
                        tex.usage = usage;
                        tex.fileName = file.parent().child( tokens[1] ).path();
                        if ( mat.textures == null ) {
                            mat.textures = new Array<ModelTexture>( 1 );
                        }
                        mat.textures.add( tex );
                    } else if ( logWarnings ) {
                        Gdx.app.error( "MtlLoader.load()", "unknown MTL statement '" + tokens[0] + "'" );
                    }
                }
            }
        } catch (IOException ex) {
            if ( logWarnings ) {
                Gdx.app.error( "MtlLoader.load()", ex.toString() );
            }
            return null;
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Gdx.app.error( "MtlLoader.load()", ex.toString() );
                }
            }
        }

        return materials;
    }
}
