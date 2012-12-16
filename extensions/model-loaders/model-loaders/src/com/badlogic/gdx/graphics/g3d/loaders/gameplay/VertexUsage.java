package com.badlogic.gdx.graphics.g3d.loaders.gameplay;

import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Defines the usage of a vertex attribute,
 * see <a href="https://github.com/blackberry/GamePlay/blob/master/gameplay-encoder/gameplay-bundle.txt#L73">gameplay-bundle.txt</a>
 * @author mzechner
 *
 */
public enum VertexUsage {
	Position(1),
	Normal(2),
	Color(3),
	Tangent(4),
	Binormal(5),
	BlendWeights(6),
	BlendIndics(7),
	TexCoord0(8),
	TexCoord1(9),
	TexCoord2(10),
	TexCoord3(11),
	TexCoord4(12),
	TexCoord5(13),
	TexCoord6(14),
	TexCoord7(15);
	
	private final int id;
	
	private VertexUsage(int id) {
		this.id = id;
	}
	
	public static VertexUsage forId(int id) {
		for(VertexUsage type: VertexUsage.values()) {
			if(type.id == id) return type;
		}
		throw new GdxRuntimeException("Unknown VertexUsage id " + id);
	}
}
