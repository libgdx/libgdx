/*
Copyright (C) 2010 Copyright 2010 Google Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.google.gwt.corp.compatibility;


public class CompatibilityImpl implements Compatibility.Impl {

	public CompatibilityImpl() {
		ConsolePrintStream cps;
		cps = new ConsolePrintStream();
		System.setOut(cps);
		System.setErr(cps);
		
		System.out.println("Test for System.out.println()");
		new Throwable("Exception test").printStackTrace();
		System.out.println("Did the exception test appear above?");
		
	}
	
	public int floatToIntBits(float f) {
		return Numbers.floatToIntBits(f);
	}

	public float intBitsToFloat(int i) {
		return Numbers.intBitsToFloat(i);
	}

	public String createString(byte[] b, int ofs, int length) {
		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			sb.append((char) b[ofs + i]);
		}
		return sb.toString();
	}

	
	public String getOriginatingServerAddress() {
//		String moduleUrl = GWT.getModuleBaseURL();
//		return getDomain(moduleUrl);//  + ":" + getServerPort();
		return getServerAddress();
	}

	private static native String getServerAddress() /*-{
		if ($wnd.__serverAddress) {
		   return $wnd.__serverAddress;
		}
		return "127.0.0.1";
	}-*/;

	public void printStackTrace(Throwable e) {
		System.out.println("" + e);
        for (StackTraceElement ste : e.getStackTrace()) {
          System.out.println(" at " + ste);
        }
	}

	public String createString(byte[] b, String encoding) {
		return createString(b, 0, b.length);
	}

	public void sleep(int i) {
		// TODO Auto-generated method stub
		
	}
}
