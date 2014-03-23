package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public class ResourceData<T> implements Json.Serializable{
	
	public static interface Configurable{
		public void save(AssetManager manager, ResourceData resources);
		public void load(AssetManager manager, ResourceData resources);
	}
	
	public static class SaveData implements Json.Serializable{
		ObjectMap<String, Object> data;
		Array<Integer> assets;
		private int loadIndex;
		protected ResourceData resources;
		
		public SaveData(){
			data = new ObjectMap<String, Object>();
			assets = new Array<Integer>();
			loadIndex = 0;
		}
		
		public SaveData(ResourceData resources){
			data = new ObjectMap<String, Object>();
			assets = new Array<Integer>();
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
			json.writeValue("indices", assets.toArray(Integer.class), Integer[].class);
		}

		@Override
		public void read (Json json, JsonValue jsonData) {
			data = json.readValue("data", ObjectMap.class, jsonData);
			assets.addAll(json.readValue("indices", Integer[].class, jsonData));
		}
	}
	
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
			json.writeValue("type", type.getCanonicalName());
		}
		@Override
		public void read (Json json, JsonValue jsonData) {
			try {
			filename = json.readValue("filename", String.class, jsonData);
			String className = json.readValue("type", String.class, jsonData);
				type = (Class<T>)Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
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
	
	public void rewind(){
		currentLoadIndex = 0;
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
