package com.badlogic.gdx.maps;

import java.util.Iterator;

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
