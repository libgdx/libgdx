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
package com.badlogic.gdx.graphics.g3d.loaders.collada;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Skin {
	public float[][] boneWeight;
	public int[][] boneIndex;
	
	public Skin(Element skinElement){
		Array<Element> colladaSources = skinElement.getChildrenByName("source");
		Map<String, Source> sources = new HashMap<String, Source>();
		for (int j = 0; j < colladaSources.size; j++) {
			Element colladaSource = colladaSources.get(j);
			//TODO: fix this so source can load more then just floats
			if(colladaSource.getChildrenByName("float_array").size != 0){
				sources.put(colladaSource.getAttribute("id"), new Source(colladaSource));
			}
		}
			
		Element vertexWeights = skinElement.getChildByName("vertex_weights");
		
		// read vertexCount
		String[] countTokens = vertexWeights.getChildByName("vcount").getText().split("\\s+");
		int[] vertexCount = new int[countTokens.length];
		for (int i = 0; i < countTokens.length; i++) {
			vertexCount[i] = Integer.parseInt(countTokens[i]);
		}
		
		String[] indexTokens = vertexWeights.getChildByName("v").getText().split("\\s+");
		int[] vertexWeightIndex = new int[indexTokens.length];
		for (int i = 0; i < indexTokens.length; i++) {
			vertexWeightIndex[i] = Integer.parseInt(indexTokens[i]);
		}
		
		ObjectMap<String , String> mapping = new ObjectMap<String, String>();
		
		Array<Element> inputs = vertexWeights.getChildrenByName("input");
		for(int i = 0;i<inputs.size;i++){
			mapping.put(inputs.get(i).getAttribute("semantic"),inputs.get(i).getAttribute("source").replaceFirst("#", ""));
		}
		
		boneWeight = new float[vertexCount.length][];
		boneIndex = new int[vertexCount.length][];
		
		int index = 0;
		
		for(int i=0;i<vertexCount.length;i++){
			int count = vertexCount[i];
			boneWeight[i] = new float[count];
			boneIndex[i] = new int[count];
			
			float[] weights = sources.get(mapping.get("WEIGHT")).data;
			
			for(int k=0;k<count;k++){
				boneIndex[i][k] = vertexWeightIndex[index++];
				boneWeight[i][k] = weights[vertexWeightIndex[index++]];
			}
		}
	}
}