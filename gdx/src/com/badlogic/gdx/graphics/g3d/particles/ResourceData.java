package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/** This class handles the assets and configurations required by a given resource when de/serialized. 
 * It's handy when a given object or one of its members requires some assets to be loaded to work properly after
 * being deserialized. To save the assets, the object should implement the {@link Configurable} interface and obtain
 * a {@link SaveData} object to store every required asset or information which will be used during the loading phase.
 * The passed in {@link AssetManager} is generally used to find the asset file name for a given resource of a given type.
 * The class can also store global configurations, this is useful when dealing with objects which should be allocated once
 * (i.e singleton).
 * The deserialization process must happen in the same order of serialization, because the per object {@link SaveData} blocks are stored
 * as an {@link Array} within the {@link ResourceData}, while the global {@link SaveData} instances can be accessed in any order because
 * require a unique {@link String} and are stored in an {@link ObjectMap}.
 * @author Inferno */
public class ResourceData<T> implements Json.Serializable{
	
	/** This interface must be implemented by any class requiring additional assets to be loaded/saved */
	public static interface Configurable<T>{
		public void save(AssetManager manager, ResourceData<T> resources);
		public void load(AssetManager manager, ResourceData<T> resources);
	}
	
	/** Contains all the saved data.
	 * {@link #data} is a map which link an asset name to its instance.
	 * {@link #assets} is an array of indices addressing a given 
	 * {@link com.badlogic.gdx.graphics.g3d.particles.ResourceData.AssetData} in the {@link ResourceData} */
	public static class SaveData implements Json.Serializable{
		ObjectMap<String, Object> data;
		IntArray assets;
		private int loadIndex;
		protected ResourceData resources;
		
		public SaveData(){
			data = new ObjectMap<String, Object>();
			assets = new IntArray();
			loadIndex = 0;
		}
		
		public SaveData(ResourceData resources){
			data = new ObjectMap<String, Object>();
			assets = new IntArray();
			loadIndex = 0;
			this.resources = resources;
		}

		public <K> void saveAsset(String filename, Class<K> type){
			int i = resources.getAssetData(filename, type);
			if(i == -1){
				resources.sharedAssets.add(new AssetData(filename, type));
				i = resources.sharedAssets.size -1;
			}
			assets.add(i);
		}
		
		public void save(String key, Object value){
			data.put(key, value);
		}
		
		public AssetDescriptor loadAsset(){
			if(loadIndex == assets.size) return null;
			AssetData data = (AssetData)resources.sharedAssets.get(assets.get(loadIndex++));
			return new AssetDescriptor(data.filename, data.type);
		}

		public <K> K load(String key){
			return (K)data.get(key);
		}
		
		@Override
		public void write (Json json) {
			json.writeValue("data", data, ObjectMap.class);
			json.writeValue("indices", assets.toArray(), int[].class);
		}

		@Override
		public void read (Json json, JsonValue jsonData) {
			data = json.readValue("data", ObjectMap.class, jsonData);
			assets.addAll(json.readValue("indices", int[].class, jsonData));
		}
	}
	
	/** This class contains all the information related to a given asset */
	public static class AssetData<T> implements Json.Serializable{
		public String filename;
		public Class<T> type;
		public AssetData(){}
		public AssetData(String filename, Class<T> type){
			this.filename = filename;
			this.type = type;
		}
		@Override
		public void write (Json json) {
			json.writeValue("filename", filename);
			json.writeValue("type", type.getName());
		}
		@Override
		public void read (Json json, JsonValue jsonData) {
			filename = json.readValue("filename", String.class, jsonData);
			String className = json.readValue("type", String.class, jsonData);
			try {
				type = (Class<T>)ClassReflection.forName(className);
			} catch (ReflectionException e) {
				throw new GdxRuntimeException("Class not found: " + className, e);
			}
		}
	}
	
	/** Unique data, can be used to save/load generic data which is not always loaded back after saving.
	 * Must be used to store data which is uniquely addressable by a given string (i.e a system configuration).*/
	private ObjectMap<String, SaveData> uniqueData;
	
	/** Objects save data, must be loaded in the same saving order*/
	private Array<SaveData> data;
	
	/** Shared assets among all the configurable objects*/
	Array<AssetData> sharedAssets;
	private int currentLoadIndex;
	public T resource;

	public ResourceData(){
		uniqueData = new ObjectMap<String, SaveData>();
		data = new Array<SaveData>(true, 3, SaveData.class);
		sharedAssets = new Array<AssetData>();
		currentLoadIndex = 0;
	}
	
	public ResourceData(T resource){
		this();
		this.resource = resource;
	}
	
	<K> int getAssetData(String filename, Class<K> type){
		int i=0;
		for(AssetData data : sharedAssets){
			if(data.filename.equals(filename) && data.type.equals(type)){
				return i;
			}
			++i;
		}
		return -1;
	}
	
	public Array<AssetDescriptor> getAssetDescriptors () {
		Array<AssetDescriptor> descriptors = new Array<AssetDescriptor>();
		for(AssetData data : sharedAssets){
			descriptors.add(new AssetDescriptor<T>(data.filename, data.type));
		}
		return descriptors;
	}
	
	public Array<AssetData> getAssets(){
		return sharedAssets;
	}

	/** Creates and adds a new SaveData object to the save data list*/
	public SaveData createSaveData() {
		SaveData saveData = new SaveData(this);
		data.add(saveData);
		return saveData;
	}

	/** Creates and adds a new and unique SaveData object to the save data map*/
	public SaveData createSaveData(String key) {
		SaveData saveData = new SaveData(this);
		if(uniqueData.containsKey(key))
			throw new RuntimeException("Key already used, data must be unique, use a different key");
		uniqueData.put(key, saveData);
		return saveData;
	}

	/** @return the next save data in the list */
	public SaveData getSaveData() {
		return data.get(currentLoadIndex++);
	}
	
	/** @return the unique save data in the map */
	public SaveData getSaveData(String key) {
		return uniqueData.get(key);
	}

	@Override
	public void write (Json json) {
		json.writeValue("unique", uniqueData, ObjectMap.class);
		json.writeValue("data", data, Array.class, SaveData.class);
		json.writeValue("assets", sharedAssets.toArray(AssetData.class), AssetData[].class);
		json.writeValue("resource", resource, null);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		uniqueData = json.readValue("unique", ObjectMap.class, jsonData);
		for(Entry<String, SaveData> entry : uniqueData.entries()){
			entry.value.resources = this;
		}
		
		data = json.readValue("data", Array.class, SaveData.class, jsonData);
		for(SaveData saveData : data){
			saveData.resources = this;
		}
		
		sharedAssets.addAll(json.readValue("assets", AssetData[].class, jsonData));
		resource = json.readValue("resource", null, jsonData);
	}

	
}
