/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.provider.Settings.Secure;

import com.badlogic.gdx.DeviceInfo;

/** @author xoppa */
public class AndroidDeviceInfo implements DeviceInfo {
	public final Context context;
	
	protected String CpuArchitecture = null;
	protected int CpuCount = 0;
	protected float CpuBogoMIPS = 0f;
	
	// Perhaps works for the most part on Linux as well...
	private void getCpuInfo() {
		String data;
		try {
			StringBuilder sb = new StringBuilder();
			InputStream in = (new ProcessBuilder(new String[] {"/system/bin/cat", "/proc/cpuinfo"})).start().getInputStream();
			byte[] buf = new byte[1024];
			int s;
			while ((s = in.read(buf)) != -1)
				sb.append(new String(buf, 0, s));
			in.close();
			data = sb.toString();
		} catch (IOException ex) {
			CpuArchitecture = "unknown";
			return;
		}
		final String lines[] = data.split("\n");
		CpuCount = 0;
		CpuBogoMIPS = 0f;
		for (int i = 0; i < lines.length; i++) {
			final String info[] = lines[i].split(": ", 2);
			if (info.length < 2) continue;
			if (info[0].contains("Processor"))
				CpuArchitecture = info[1];
			else if (info[0].contains("processor"))
				CpuCount++;
			else if (info[0].contains("BogoMIPS")) {
				float tmp = 0;
				try {
					tmp = Float.valueOf(info[1]);
				} catch (Exception e) {}
				if (tmp > CpuBogoMIPS)
					CpuBogoMIPS = tmp;
			}
		}
		if (CpuArchitecture == null)
			CpuArchitecture = "unknown";
	}
	
	public AndroidDeviceInfo(final Context context) {
		this.context = context;
	}

	@Override
	public String getManufacturer () {
		return android.os.Build.MANUFACTURER;
	}

	@Override
	public String getBrand () {
		return android.os.Build.BRAND;
	}

	@Override
	public String getDevice () {
		return android.os.Build.DEVICE;
	}
	
	@Override
	public String getProduct () {
		return android.os.Build.PRODUCT;
	}

	@Override
	public String getModel () {
		return android.os.Build.MODEL;
	}

	@Override
	public String getSerial () {
		return context == null ? "" : Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		// For API 9 and above:
		// return android.os.Build.SERIAL;
	}

	@Override
	public String getVersion () {
		return android.os.Build.VERSION.RELEASE;
	}

	@Override
	public String getCpuArchitecture () {
		if (CpuArchitecture == null)
			getCpuInfo();
		return CpuArchitecture;
	}

	@Override
	public int getCpuCount () {
		if (CpuArchitecture == null)
			getCpuInfo();
		return CpuCount;
	}

	@Override
	public float getCpuSpeed () {
		if (CpuArchitecture == null)
			getCpuInfo();
		return CpuBogoMIPS;
	}
}