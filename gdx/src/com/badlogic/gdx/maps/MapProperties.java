package com.badlogic.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * @brief Set of string indexed values representing map elements' properties, allowing
 * to retrieve, modify and add properties to the set.
 */
public class MapProperties {

	private ObjectMap<String, Object> properties;
	
	/**
	 * Creates an empty properties set
	 */
	public MapProperties() {
		properties = new ObjectMap<String, Object>();
	}

	/**
	 * @param key property name 
	 * @return true if and only if the property exists
	 */
	public boolean has(String key) {
		return properties.containsKey(key);
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists, otherwise, null
	 */
	public Object get(String key) {
		return properties.get(key);
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and can be interpreted as a boolean, otherwise, null
	 */
	public Boolean getAsBoolean(String key) {
		Object value = properties.get(key);
		try {
			return (Boolean) value;
		} catch (ClassCastException e) {
			if (value instanceof CharSequence) {
				return Boolean.valueOf(value.toString());
			} else {
				return null;
			}
		}
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and can be interpreted as a byte, otherwise, null
	 */
	public Byte getAsByte(String key) {
		Object value = properties.get(key);
		if (value != null) {
			if (value instanceof Number) {
				return ((Number) value).byteValue();
			} else {
				if (value instanceof CharSequence) {
					try {
						return Byte.valueOf(value.toString());	
					} catch (NumberFormatException e2) {
						return null;
					}
				} else {
					return null;
				}
			}			
		} else {
			return null;			
		}
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and can be interpreted as a double, otherwise, null
	 */
	public Double getAsDouble(String key) {
		Object value = properties.get(key);
		if (value != null) {
			if (value instanceof Number) {
				return ((Number) value).doubleValue();
			} else {
				if (value instanceof CharSequence) {
					try {
						return Double.valueOf(value.toString());	
					} catch (NumberFormatException e2) {
						return null;
					}
				} else {
					return null;
				}
			}
		} else {
			return null;			
		}
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and can be interpreted as a float, otherwise, null
	 */
	public Float getAsFloat(String key) {
		Object value = properties.get(key);
		if (value != null) {
			if (value instanceof Number) {
				return ((Number) value).floatValue();
			} else {
				if (value instanceof CharSequence) {
					try {
						return Float.valueOf(value.toString());	
					} catch (NumberFormatException e2) {
						return null;
					}
				} else {
					return null;
				}
			}
		} else {
			return null;			
		}
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and can be interpreted as an integer, otherwise, null
	 */
	public Integer getAsInteger(String key) {
		Object value = properties.get(key);
		if (value != null) {
			if (value instanceof Number) {
				return ((Number) value).intValue();
			} else {
				if (value instanceof CharSequence) {
					try {
						return Integer.valueOf(value.toString());	
					} catch (NumberFormatException e2) {
						return null;
					}
				} else {
					return null;
				}
			}
		} else {
			return null;			
		}
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and can be interpreted as a long, otherwise, null
	 */
	public Long getAsLong(String key) {
		Object value = properties.get(key);
		if (value != null) {
			if (value instanceof Number) {
				return ((Number) value).longValue();
			} else {
				if (value instanceof CharSequence) {
					try {
						return Long.valueOf(value.toString());	
					} catch (NumberFormatException e2) {
						return null;
					}
				} else {
					return null;
				}
			}
		} else {
			return null;			
		}
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and can be interpreted as a short, otherwise, null
	 */
	public Short getAsShort(String key) {
		Object value = properties.get(key);
		if (value != null) {
			if (value instanceof Number) {
				return ((Number) value).shortValue();
			} else {
				if (value instanceof CharSequence) {
					try {
						return Short.valueOf(value.toString());	
					} catch (NumberFormatException e2) {
						return null;
					}
				} else {
					return null;
				}
			}
		} else {
			return null;			
		}
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property as a string if it exists, otherwise, null
	 */
	public String getAsString(String key) {
		Object value = properties.get(key);
		if (value != null) {
			return value.toString();
		} else {
			return null;			
		}
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and it's a Vector2, otherwise, null
	 */
	public Vector2 getAsVector2(String key) {
		Object value = properties.get(key);
		
		if (value != null && value instanceof Vector2) {
			return (Vector2)value;
		}
		
		return null;
	}
	
	/**
	 * @param key property name 
	 * @return the value for that property if it exists and it's a Color, otherwise, null
	 */
	public Color getAsColor(String key) {
		Object value = properties.get(key);
		
		if (value != null && value instanceof Color) {
			return (Color)value;
		}
		
		return null;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's a boolean, otherwise, defaultValue
	 */
	public Boolean getAsBoolean(String key, Boolean defaultValue) {
		Boolean value = getAsBoolean(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's a byte, otherwise, defaultValue
	 */
	public Byte getAsByte(String key, Byte defaultValue) {
		Byte value = getAsByte(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's a double, otherwise, defaultValue
	 */
	public Double getAsDouble(String key, Double defaultValue) {
		Double value = getAsDouble(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's a float, otherwise, defaultValue
	 */
	public Float getAsFloat(String key, Float defaultValue) {
		Float value = getAsFloat(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's an integer, otherwise, defaultValue
	 */
	public Integer getAsInteger(String key, Integer defaultValue) {
		Integer value = getAsInteger(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's a long, otherwise, defaultValue
	 */
	public Long getAsLong(String key, Long defaultValue) {
		Long value = getAsLong(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's a short, otherwise, defaultValue
	 */
	public Short getAsShort(String key, Short defaultValue) {
		Short value = getAsShort(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property as a string if it exists, otherwise, defaultValue
	 */
	public String getAsString(String key, String defaultValue) {
		String value = getAsString(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's a Vector2, otherwise, defaultValue
	 */
	public Vector2 getAsVector2(String key, Vector2 defaultValue) {
		Vector2 value = getAsVector2(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name 
	 * @param defaultValue value to be returned in case of failure
	 * @return the value for that property if it exists and it's a Color, otherwise, defaultValue
	 */
	public Color getAsColor(String key, Color defaultValue) {
		Color value = getAsColor(key);
		return value == null? defaultValue : value;
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Boolean value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Byte value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Double value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Float value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Integer value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Long value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Short value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, String value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Vector2 value) {
		properties.put(key, value);
	}
	
	/**
	 * @param key property name
	 * @param value value to be inserted or modified (if it already existed)
	 */
	public void put(String key, Color value) {
		properties.put(key, value);
	}
	
	/**
	 * @param properties set of properties to be added
	 */
	public void putAll(MapProperties properties) {
		this.properties.putAll(properties.properties);
	}
	
	/**
	 * @param key property name to be removed
	 */
	public void remove(String key) {
		properties.remove(key);
	}
	
	/**
	 * Removes all properties
	 */
	public void clear() {
		properties.clear();
	}
	
	/**
	 * @return iterator for the property names
	 */
	public Iterator<String> getKeys() {
		return properties.keys();
	}
	
	/**
	 * @return iterator to properties' values
	 */
	public Iterator<Object> getValues() {
		return properties.values();
	}

}
