package com.badlogic.gdx.maps;

/**
 * @brief Generic map
 * 
 * A Map instance contains the following data
 * 
 * <ul>
 * <li> MapLayers<ul>
 * 	<li>MapLayer<ul>
 * 		<li>MapObjects<ul>
 * 			<li>MapObject<ul>
 * 				<li>Can be: TextureMapObject, CircleMapObject, RectangleMapObject, PolygonMapObject or PolylineMapObject</li>
 * 				<li>MapProperties</li>
 * 			</ul></li>
 * 		</ul></li>
 * 		<li>MapProperties</li>	
 * 	</ul></li>
 * </ul></li>
 * <li> MapProperties
 * </ul>
 */
public class Map {
	
	private MapLayers layers = new MapLayers();
	private MapProperties properties = new MapProperties();
	
	/**
	 * @return map's layers
	 */
	public MapLayers getLayers() {
		return layers;
	}

	/**
	 * @return map's properties set
	 */
	public MapProperties getProperties() {
		return properties;
	}
	
	/**
	 * Creates empty map
	 */
	public Map() {
		
	}
	
}
