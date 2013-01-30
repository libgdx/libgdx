package com.badlogic.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;

public class MapProperties {

	private ObjectMap<String, Object> properties;
	
	public MapProperties() {
		properties = new ObjectMap<String, Object>();
	}

	public boolean has(String key) {
		return properties.containsKey(key);
	}
	
	public Object get(String key) {
		return properties.get(key);
	}
	
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
	
	public String getAsString(String key) {
		Object value = properties.get(key);
		if (value != null) {
			return value.toString();
		} else {
			return null;			
		}
	}
	
	public Vector2 getAsVector2(String key) {
		Object value = properties.get(key);
		
		if (value != null && value instanceof Vector2) {
			return (Vector2)value;
		}
		
		return null;
	}
	
	public Color getAsColor(String key) {
		Object value = properties.get(key);
		
		if (value != null && value instanceof Color) {
			return (Color)value;
		}
		
		return null;
	}
	
	public Boolean getAsBoolean(String key, Boolean defaultValue) {
		Boolean value = getAsBoolean(key);
		return value == null? defaultValue : value;
	}
	
	public Byte getAsByte(String key, Byte defaultValue) {
		Byte value = getAsByte(key);
		return value == null? defaultValue : value;
	}
	
	public Double getAsDouble(String key, Double defaultValue) {
		Double value = getAsDouble(key);
		return value == null? defaultValue : value;
	}
	
	public Float getAsFloat(String key, Float defaultValue) {
		Float value = getAsFloat(key);
		return value == null? defaultValue : value;
	}
	
	public Integer getAsInteger(String key, Integer defaultValue) {
		Integer value = getAsInteger(key);
		return value == null? defaultValue : value;
	}
	
	public Long getAsLong(String key, Long defaultValue) {
		Long value = getAsLong(key);
		return value == null? defaultValue : value;
	}
	
	public Short getAsShort(String key, Short defaultValue) {
		Short value = getAsShort(key);
		return value == null? defaultValue : value;
	}
	
	public String getAsString(String key, String defaultValue) {
		String value = getAsString(key);
		return value == null? defaultValue : value;
	}
	
	public Vector2 getAsVector2(String key, Vector2 defaultValue) {
		Vector2 value = getAsVector2(key);
		return value == null? defaultValue : value;
	}
	
	public Color getAsColor(String key, Color defaultValue) {
		Color value = getAsColor(key);
		return value == null? defaultValue : value;
	}
	
	public void put(String key, Boolean value) {
		properties.put(key, value);
	}
	
	public void put(String key, Byte value) {
		properties.put(key, value);
	}
	
	public void put(String key, Double value) {
		properties.put(key, value);
	}
	
	public void put(String key, Float value) {
		properties.put(key, value);
	}
	
	public void put(String key, Integer value) {
		properties.put(key, value);
	}
	
	public void put(String key, Long value) {
		properties.put(key, value);
	}
	
	public void put(String key, Short value) {
		properties.put(key, value);
	}
	
	public void put(String key, String value) {
		properties.put(key, value);
	}
	
	public void put(String key, Vector2 value) {
		properties.put(key, value);
	}
	
	public void put(String key, Color value) {
		properties.put(key, value);
	}
	
	public void putAll(MapProperties properties) {
		this.properties.putAll(properties.properties);
	}
	
	public void remove(String key) {
		properties.remove(key);
	}
	
	public void clear() {
		properties.clear();
	}
	
	public Iterator<String> getKeys() {
		return properties.keys();
	}
	
	public Iterator<Object> getValues() {
		return properties.values();
	}

}
