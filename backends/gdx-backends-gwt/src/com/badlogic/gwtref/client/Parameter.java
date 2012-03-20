package com.badlogic.gwtref.client;

public class Parameter {
	final String name;
	final Class type;
	final String jnsi;
	
	Parameter(String name, Class type, String jnsi) {
		this.name = name;
		this.type = type;
		this.jnsi = jnsi;
	}

	public String getName () {
		return name;
	}

	public Class getType () {
		return type;
	}
	
	public String getJnsi() {
		return jnsi;
	}

	@Override
	public String toString () {
		return "Parameter [name=" + name + ", type=" + type + ", jnsi=" + jnsi + "]";
	}
}
