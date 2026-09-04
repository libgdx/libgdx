
package com.badlogic.gdx;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

/** Tests the base failure classification used by {@link Preferences#flush(PreferencesSaveCallback)}. It is intentionally limited
 * to exception types available on all platforms including GWT; backends with richer native information classify locally. */
public class PreferencesSaveResultTest {

	@Test
	public void nullThrowableIsUnknown () {
		assertEquals(PreferencesSaveResult.UNKNOWN, PreferencesSaveResult.from(null));
	}

	@Test
	public void plainExceptionIsUnknown () {
		assertEquals(PreferencesSaveResult.UNKNOWN, PreferencesSaveResult.from(new RuntimeException("boom")));
	}

	@Test
	public void securityExceptionIsAccessDenied () {
		assertEquals(PreferencesSaveResult.ACCESS_DENIED, PreferencesSaveResult.from(new SecurityException("denied")));
	}

	@Test
	public void ioExceptionIsIoError () {
		assertEquals(PreferencesSaveResult.IO_ERROR, PreferencesSaveResult.from(new IOException("kaboom")));
	}

	/** {@link FileNotFoundException} is thrown for many reasons besides denied access (missing parent directory, ...), so it must
	 * degrade to IO_ERROR at the core level. Backends that can identify the OS error code upgrade it locally. */
	@Test
	public void fileNotFoundIsIoError () {
		assertEquals(PreferencesSaveResult.IO_ERROR,
			PreferencesSaveResult.from(new FileNotFoundException("/prefs/missing/parent/prefs.xml")));
	}

	/** Backends wrap I/O exceptions (e.g. FileHandle and GdxRuntimeException), so classification must walk the cause chain. */
	@Test
	public void causeChainIsWalked () {
		RuntimeException wrappedSecurity = new RuntimeException("Error writing preferences", new SecurityException("denied"));
		assertEquals(PreferencesSaveResult.ACCESS_DENIED, PreferencesSaveResult.from(wrappedSecurity));

		RuntimeException wrappedIo = new RuntimeException("Error writing preferences", new IOException("kaboom"));
		assertEquals(PreferencesSaveResult.IO_ERROR, PreferencesSaveResult.from(wrappedIo));

		RuntimeException wrappedPlain = new RuntimeException("Error writing preferences", new RuntimeException("boom"));
		assertEquals(PreferencesSaveResult.UNKNOWN, PreferencesSaveResult.from(wrappedPlain));
	}

	@Test
	public void selfCausingExceptionTerminates () {
		// Throwable.initCause(this) is rejected by the JDK, so the cycle is simulated through getCause().
		assertEquals(PreferencesSaveResult.UNKNOWN, PreferencesSaveResult.from(new CyclicThrowable()));
	}

	static class CyclicThrowable extends RuntimeException {
		@Override
		public Throwable getCause () {
			return this;
		}
	}
}
