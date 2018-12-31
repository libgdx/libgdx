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

package com.badlogic.gdx.backends.iosrobovm;

public enum IOSDevice {

	IPHONE_2G("iPhone1,1", 163),
	IPHONE_3G("iPhone1,2", 163),
	IPHONE_3GS("iPhone2,1", 163),
	IPHONE_4("iPhone3,1", 326),
	IPHONE_4V("iPhone3,2", 326),
	IPHONE_4_CDMA("iPhone3,3", 326),
	IPHONE_4S("iPhone4,1", 326),
	IPHONE_5("iPhone5,1", 326),
	IPHONE_5_CDMA_GSM("iPhone5,2", 326),
	IPHONE_5C("iPhone5,3", 326),
	IPHONE_5C_CDMA_GSM("iPhone5,4", 326),
	IPHONE_5S("iPhone6,1", 326),
	IPHONE_5S_CDMA_GSM("iPhone6,2", 326),
	IPHONE_6_PLUS("iPhone7,1", 401),
	IPHONE_6("iPhone7,2", 326),
	IPHONE_6S("iPhone8,1", 326),
	IPHONE_6S_PLUS("iPhone8,2", 401),
	IPHONE_7_CDMA_GSM("iPhone9,1", 326),
	IPHONE_7_PLUS_CDMA_GSM("iPhone9,2", 401),
	IPHONE_7("iPhone9,3", 326),
	IPHONE_7_PLUS("iPhone9,4", 401),
	IPHONE_SE("iPhone8,4", 326),
	IPHONE_8_CDMA_GSM("iPhone10,1", 326),
	IPHONE_8_PLUS_CDMA_GSM("iPhone10,2",401),
	IPHONE_X_CDMA_GSM("iPhone10,3", 458),
	IPHONE_8("iPhone10,4", 326),
    	IPHONE_8_PLUS("iPhone10,5", 401),
	IPHONE_X("iPhone10,6", 458),
	IPHONE_XR("iPhone11,8", 326),
	IPHONE_XS("iPhone11,2", 458),
	IPHONE_XS_MAX("iPhone11,4", 458),
	IPHONE_XS_MAX_2_NANO_SIM("iPhone11,6", 458),

	IPOD_TOUCH_1G("iPod1,1", 163),
	IPOD_TOUCH_2G("iPod2,1", 163),
	IPOD_TOUCH_3G("iPod3,1", 163),
	IPOD_TOUCH_4G("iPod4,1", 326),
	IPOD_TOUCH_5G("iPod5,1", 326),
	IPOD_TOUCH_6G("iPod7,1", 326),

	IPAD("iPad1,1", 132),
	IPAD_3G("iPad1,2", 132),
	IPAD_2_WIFI("iPad2,1", 132),
	IPAD_2("iPad2,2", 132),
	IPAD_2_CDMA("iPad2,3", 132),
	IPAD_2V("iPad2,4", 132),
	IPAD_MINI_WIFI("iPad2,5", 164),
	IPAD_MINI("iPad2,6", 164),
	IPAD_MINI_WIFI_CDMA("iPad2,7", 164),
	IPAD_3_WIFI("iPad3,1", 264),
	IPAD_3_WIFI_CDMA("iPad3,2", 264),
	IPAD_3("iPad3,3", 264),
	IPAD_4_WIFI("iPad3,4", 264),
	IPAD_4("iPad3,5", 264),
	IPAD_4_GSM_CDMA("iPad3,6", 264),
	IPAD_AIR_WIFI("iPad4,1", 264),
	IPAD_AIR_WIFI_GSM("iPad4,2", 264),
	IPAD_AIR_WIFI_CDMA("iPad4,3", 264),
	IPAD_MINI_RETINA_WIFI("iPad4,4", 326),
	IPAD_MINI_RETINA_WIFI_CDMA("iPad4,5", 326),
	IPAD_MINI_RETINA_WIFI_CELLULAR_CN("iPad4,6", 326),
	IPAD_MINI_3_WIFI("iPad4,7", 326),
	IPAD_MINI_3_WIFI_CELLULAR("iPad4,8", 326),
	IPAD_MINI_3_WIFI_CELLULAR_CN("iPad4,9", 326),
	IPAD_MINI_4_WIFI("iPad5,1", 326),
	IPAD_MINI_4_WIFI_CELLULAR("iPad5,2", 326),
	IPAD_MINI_AIR_2_WIFI("iPad5,3", 264),
	IPAD_MINI_AIR_2_WIFI_CELLULAR("iPad5,4", 264),
	IPAD_PRO_WIFI("iPad6,7", 264),
	IPAD_PRO("iPad6,8", 264),
	IPAD_PRO_97_WIFI("iPad6,3", 264),
	IPAD_PRO_97("iPad6,4", 264),
	IPAD_5_WIFI("iPad6,11", 264),
	IPAD_5_WIFI_CELLULAR("iPad6,12", 264),
	IPAD_PRO_2_WIFI("iPad7,1", 264),
	IPAD_PRO_2_WIFI_CELLULAR("iPad7,2", 264),
	IPAD_PRO_10_5_WIFI("iPad7,3", 264),
	IPAD_PRO_10_5_WIFI_CELLULAR("iPad7,4", 264),
	IPAD_6_WIFI("iPad7,5", 264),
	IPAD_6_WIFI_CELLULAR("iPad7,6", 264),

	SIMULATOR_32("i386", 264),
	SIMULATOR_64("x86_64", 264);

	final String machineString;
	final int ppi;

	IOSDevice(String machineString, int ppi) {
		this.machineString = machineString;
		this.ppi = ppi;
	}

	public static IOSDevice getDevice (String machineString) {
		for (IOSDevice device : values()) {
			if (device.machineString.equalsIgnoreCase(machineString)) return device;
		}
		return null;
	}


}
