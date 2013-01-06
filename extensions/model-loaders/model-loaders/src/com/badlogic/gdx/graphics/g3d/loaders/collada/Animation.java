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

import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonKeyframe;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Animation {
	ObjectMap<String, float[]> inputMap = new ObjectMap<String, float[]>();
	ObjectMap<String, float[]> outputMap = new ObjectMap<String, float[]>();
	
	//TODO: replace this with the bone name/index read in skin data
	Array<String> channels = new Array<String>(); 
	
	SkeletonKeyframe[][] keyFrames;
	
	public Animation(Element libAnimElement){
		Array<Element> animElements = libAnimElement.getChildrenByName("animation");
		for(int i=0;i<animElements.size;i++){
			Element animElement = animElements.get(i);
			Array<Element> colladaSources = animElement.getChildrenByName("source");
			Map<String, Source> sources = new HashMap<String, Source>();
			for (int j = 0; j < colladaSources.size; j++) {
				Element colladaSource = colladaSources.get(j);
				//TODO: fix this so source can load more then just floats
				if(colladaSource.getChildrenByName("float_array").size != 0){
					sources.put(colladaSource.getAttribute("id"), new Source(colladaSource));
				}
			}
			
			ObjectMap<String , String> mapping = new ObjectMap<String, String>();
			
			Element samplerElement = animElement.getChildByName("sampler");
			if(samplerElement ==null)
				throw new GdxRuntimeException("no sampler in animation element in scene");
			
			Array<Element> inputs = samplerElement.getChildrenByName("input");
			for(int k = 0;k<inputs.size;k++){
				mapping.put(inputs.get(k).getAttribute("semantic"),inputs.get(k).getAttribute("source").replaceFirst("#", ""));
			}
			
			Element channelElement = animElement.getChildByName("channel");
			if(channelElement ==null)
				throw new GdxRuntimeException("no sampler in animation element in scene");
			
			String channelName = channelElement.getAttribute("target");
			
			channels.add(channelName);
			inputMap.put(channelName,sources.get(mapping.get("INPUT")).data);
			outputMap.put(channelName,sources.get(mapping.get("OUTPUT")).data);
		}
		
		keyFrames = new SkeletonKeyframe[channels.size][];
		for(int i=0;i<channels.size;i++){
			String channel = channels.get(i);
			float[] input = inputMap.get(channel);
			float[] output = outputMap.get(channel);
			keyFrames[i] = new SkeletonKeyframe[input.length];
			for(int j=0;j<input.length;j++){
				SkeletonKeyframe frame = new SkeletonKeyframe();
				keyFrames[i][j] = frame;
				Matrix4 m = getMatrix(output, j*16);
				m.getTranslation(frame.position);
				m.getRotation(frame.rotation);
				//TODO: get Scale from matrix
				frame.timeStamp = input[j];
				//TODO:set parent index joint data
			}
		}
	}
	
	private Matrix4 getMatrix(float[] val, int offset){
		Matrix4 m = new Matrix4();
		for(int i=0;i<16;i++){
			m.val[i] = val[i+offset];
		}
		m.tra();
		return m;
	}
}