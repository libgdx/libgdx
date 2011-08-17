
package com.badlogic.gdx.utils;

/** Indicates an error during serialization due to misconfiguration or during deserialization due to invalid input data.
 * @author Nathan Sweet <misc@n4te.com> */
public class SerializationException extends RuntimeException {
	private StringBuffer trace;

	public SerializationException () {
		super();
	}

	public SerializationException (String message, Throwable cause) {
		super(message, cause);
	}

	public SerializationException (String message) {
		super(message);
	}

	public SerializationException (Throwable cause) {
		super("", cause);
	}

	/** Returns true if any of the exceptions that caused this exception are of the specified type. */
	public boolean causedBy (Class type) {
		return causedBy(this, type);
	}

	private boolean causedBy (Throwable ex, Class type) {
		Throwable cause = ex.getCause();
		if (cause == null || cause == ex) return false;
		if (type.isAssignableFrom(cause.getClass())) return true;
		return causedBy(cause, type);
	}

	public String getMessage () {
		if (trace == null) return super.getMessage();
		StringBuffer buffer = new StringBuffer(512);
		buffer.append(super.getMessage());
		if (buffer.length() > 0) buffer.append('\n');
		buffer.append("Serialization trace:");
		buffer.append(trace);
		return buffer.toString();
	}

	/** Adds information to the exception message about where in the the object graph serialization failure occurred. Serializers
	 * can catch {@link SerializationException}, add trace information, and rethrow the exception. */
	public void addTrace (String info) {
		if (info == null) throw new IllegalArgumentException("info cannot be null.");
		if (trace == null) trace = new StringBuffer(512);
		trace.append('\n');
		trace.append(info);
	}
}
