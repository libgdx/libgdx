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
package com.badlogic.gdx.awesomium;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;

public interface Awesomium extends Library {
	Awesomium INSTANCE = AwesomiumFactory.createInstance();
	
	public static final int AWE_AK_ADD = 107;
	public static final int AWE_CURSOR_NORTH_WEST_RESIZE = 9;
	public static final int AWE_AK_MEDIA_STOP = 178;
	public static final int AWE_AK_NUMLOCK = 144;
	public static final int AWE_AK_OEM_1 = 186;
	public static final int AWE_AK_PACKET = 231;
	public static final int AWE_AK_OEM_102 = 226;
	public static final int AWE_AK_OEM_2 = 191;
	public static final int AWE_AK_OEM_3 = 192;
	public static final int AWE_AK_MEDIA_LAUNCH_APP1 = 182;
	public static final int AWE_AK_OEM_4 = 219;
	public static final int AWE_AK_MEDIA_LAUNCH_APP2 = 183;
	public static final int AWE_AK_OEM_PLUS = 187;
	public static final int AWE_CURSOR_HAND = 2;
	public static final int AWE_AK_ZOOM = 251;
	public static final int AWE_AK_OEM_5 = 220;
	public static final int AWE_AK_APPS = 93;
	public static final int AWE_AK_OEM_6 = 221;
	public static final int AWE_AK_MENU = 18;
	public static final int AWE_AK_OEM_7 = 222;
	public static final int AWE_AK_OEM_8 = 223;
	public static final int AWE_UFM_NONE = 0;
	public static final int AWE_CURSOR_NORTH_WEST_SOUTH_EAST_RESIZE = 17;
	public static final int AWE_MOD_SHIFT_KEY = (1 << 0);
	public static final int AWE_AK_INSERT = 45;
	public static final int AWE_AK_MULTIPLY = 106;
	public static final int AWE_CURSOR_VERTICAL_TEXT = 30;
	public static final int AWE_AK_RCONTROL = 163;
	public static final int AWE_CURSOR_EAST_WEST_RESIZE = 15;
	public static final int AWE_AK_OEM_CLEAR = 254;
	public static final int AWE_AK_SPACE = 32;
	public static final int AWE_AK_KANA = 21;
	public static final int AWE_CURSOR_NONE = 37;
	public static final int AWE_CURSOR_POINTER = 0;
	public static final int AWE_AK_CONVERT = 28;
	public static final int AWE_AK_KANJI = 25;
	public static final int AWE_CURSOR_NORTH_EAST_PANNING = 23;
	public static final int AWE_CURSOR_HELP = 5;
	public static final int AWE_AK_CONTROL = 17;
	public static final int AWE_AK_HANJA = 25;
	public static final int AWE_CURSOR_MIDDLE_PANNING = 20;
	public static final int AWE_CURSOR_COLUMN_RESIZE = 18;
	public static final int AWE_CURSOR_COPY = 36;
	public static final int AWE_AK_MEDIA_PREV_TRACK = 177;
	public static final int AWE_CURSOR_CONTEXT_MENU = 32;
	public static final int AWE_AK_MEDIA_LAUNCH_MAIL = 180;
	public static final int AWE_AK_BROWSER_BACK = 166;
	public static final int AWE_CURSOR_ROW_RESIZE = 19;
	public static final int AWE_CURSOR_NORTH_RESIZE = 7;
	public static final int AWE_CURSOR_CUSTOM = 41;
	public static final int AWE_AK_SEPARATOR = 108;
	public static final int AWE_CURSOR_ZOOM_OUT = 40;
	public static final int AWE_AK_OEM_PERIOD = 190;
	public static final int AWE_AK_SNAPSHOT = 44;
	public static final int AWE_CURSOR_SOUTH_EAST_RESIZE = 11;
	public static final int AWE_AK_SHIFT = 16;
	public static final int AWE_AK_OEM_COMMA = 188;
	public static final int AWE_AK_PAUSE = 19;
	public static final int AWE_AK_UNKNOWN = 0;
	public static final int AWE_CURSOR_ZOOM_IN = 39;
	public static final int AWE_RIGHT_BUTTON = 2;
	public static final int AWE_AK_ESCAPE = 27;
	public static final int AWE_TYPE_KEY_UP = 2;
	public static final int AWE_AK_SELECT = 41;
	public static final int AWE_AK_OEM_MINUS = 189;
	public static final int AWE_CURSOR_SOUTH_WEST_RESIZE = 12;
	public static final int AWE_MOD_META_KEY = (1 << 3);
	public static final int AWE_AK_RETURN = 13;
	public static final int AWE_UFM_WHITELIST = 2;
	public static final int AWE_AK_RIGHT = 39;
	public static final int AWE_AK_DIVIDE = 111;
	public static final int AWE_CURSOR_MOVE = 29;
	public static final int AWE_AK_HOME = 36;
	public static final int AWE_AK_EXECUTE = 43;
	public static final int AWE_CURSOR_IBEAM = 3;
	public static final int AWE_CURSOR_SOUTH_PANNIN = 25;
	public static final int AWE_CURSOR_WAIT = 4;
	public static final int AWE_CURSOR_PROGRESS = 34;
	public static final int AWE_AK_MODECHANGE = 31;
	public static final int AWE_AK_MEDIA_NEXT_TRACK = 176;
	public static final int AWE_MOD_CONTROL_KEY = (1 << 1);
	public static final int AWE_CURSOR_WEST_RESIZE = 13;
	public static final int AWE_MIDDLE_BUTTON = 3;
	public static final int AWE_CURSOR_NO_DROP = 35;
	public static final int AWE_AK_PLAY = 250;
	public static final int AWE_AK_BROWSER_HOME = 172;
	public static final int AWE_AK_NONAME = 252;
	public static final int AWE_AK_UP = 38;
	public static final int AWE_AK_LWIN = 91;
	public static final int AWE_AK_CLEAR = 12;
	public static final int AWE_AK_LEFT = 37;
	public static final int AWE_AK_RWIN = 92;
	public static final int AWE_AK_CRSEL = 247;
	public static final int AWE_AK_DELETE = 46;
	public static final int AWE_AK_MEDIA_PLAY_PAUSE = 179;
	public static final int AWE_AK_TAB = 9;
	public static final int AWE_MOD_IS_AUTOREPEAT = (1 << 5);
	public static final int AWE_AK_VOLUME_DOWN = 174;
	public static final int AWE_AK_BROWSER_REFRESH = 168;
	public static final int AWE_CURSOR_CROSS = 1;
	public static final int AWE_AK_LCONTROL = 162;
	public static final int AWE_TYPE_CHAR = 3;
	public static final int AWE_AK_DECIMAL = 110;
	public static final int AWE_AK_CAPITAL = 20;
	public static final int AWE_CURSOR_EAST_PANNING = 21;
	public static final int AWE_AK_BROWSER_SEARCH = 170;
	public static final int AWE_CURSOR_SOUTH_WEST_PANNING = 27;
	public static final int AWE_CURSOR_ALIAS = 33;
	public static final int AWE_AK_PA1 = 253;
	public static final int AWE_AK_RSHIFT = 161;
	public static final int AWE_MOD_ALT_KEY = (1 << 2);
	public static final int AWE_MOD_IS_KEYPAD = (1 << 4);
	public static final int AWE_AK_HANGUL = 21;
	public static final int AWE_AK_BROWSER_FAVORITES = 171;
	public static final int AWE_AK_SUBTRACT = 109;
	public static final int AWE_AK_LMENU = 164;
	public static final int AWE_AK_LSHIFT = 160;
	public static final int AWE_CURSOR_SOUTH_RESIZE = 10;
	public static final int AWE_CURSOR_CELL = 31;
	public static final int AWE_AK_L = 76;
	public static final int AWE_AK_K = 75;
	public static final int AWE_AK_J = 74;
	public static final int AWE_AK_I = 73;
	public static final int AWE_AK_H = 72;
	public static final int AWE_AK_G = 71;
	public static final int AWE_AK_F = 70;
	public static final int AWE_AK_E = 69;
	public static final int AWE_AK_T = 84;
	public static final int AWE_AK_RMENU = 165;
	public static final int AWE_AK_S = 83;
	public static final int AWE_AK_R = 82;
	public static final int AWE_AK_Q = 81;
	public static final int AWE_AK_P = 80;
	public static final int AWE_AK_O = 79;
	public static final int AWE_AK_N = 78;
	public static final int AWE_CURSOR_NORTH_SOUTH_RESIZE = 14;
	public static final int AWE_AK_M = 77;
	public static final int AWE_CURSOR_NORTH_WEST_PANNING = 24;
	public static final int AWE_AK_VOLUME_MUTE = 173;
	public static final int AWE_AK_Y = 89;
	public static final int AWE_AK_Z = 90;
	public static final int AWE_CURSOR_EAST_RESIZE = 6;
	public static final int AWE_AK_W = 87;
	public static final int AWE_AK_X = 88;
	public static final int AWE_AK_U = 85;
	public static final int AWE_AK_V = 86;
	public static final int AWE_CURSOR_SOUTH_EAST_PANNING = 26;
	public static final int AWE_AK_END = 35;
	public static final int AWE_AK_2 = 50;
	public static final int AWE_AK_1 = 49;
	public static final int AWE_AK_JUNJA = 23;
	public static final int AWE_AK_BACK = 8;
	public static final int AWE_AK_4 = 52;
	public static final int AWE_AK_EREOF = 249;
	public static final int AWE_AK_3 = 51;
	public static final int AWE_AK_0 = 48;
	public static final int AWE_AK_9 = 57;
	public static final int AWE_AK_ATTN = 246;
	public static final int AWE_CURSOR_NORTH_EAST_SOUTH_WEST_RESIZE = 16;
	public static final int AWE_AK_PRIOR = 33;
	public static final int AWE_AK_5 = 53;
	public static final int AWE_AK_6 = 54;
	public static final int AWE_AK_DOWN = 40;
	public static final int AWE_AK_7 = 55;
	public static final int AWE_AK_8 = 56;
	public static final int AWE_TYPE_KEY_DOWN = 1;
	public static final int AWE_AK_A = 65;
	public static final int AWE_AK_B = 66;
	public static final int AWE_AK_C = 67;
	public static final int AWE_AK_SCROLL = 145;
	public static final int AWE_AK_D = 68;
	public static final int AWE_CURSOR_NOT_ALLOWED = 38;
	public static final int AWE_UFM_BLACKLIST = 1;
	public static final int AWE_AK_NEXT = 34;
	public static final int AWE_AK_F21 = 132;
	public static final int AWE_AK_BROWSER_FORWARD = 167;
	public static final int AWE_AK_F22 = 133;
	public static final int AWE_AK_F23 = 134;
	public static final int AWE_AK_F24 = 135;
	public static final int AWE_AK_BROWSER_STOP = 169;
	public static final int AWE_AK_NUMPAD2 = 98;
	public static final int AWE_AK_PRINT = 42;
	public static final int AWE_AK_NUMPAD3 = 99;
	public static final int AWE_AK_NUMPAD4 = 100;
	public static final int AWE_AK_NUMPAD5 = 101;
	public static final int AWE_AK_FINAL = 24;
	public static final int AWE_AK_NUMPAD6 = 102;
	public static final int AWE_AK_PROCESSKEY = 229;
	public static final int AWE_CURSOR_NORTH_PANNING = 22;
	public static final int AWE_LEFT_BUTTON = 1;
	public static final int AWE_AK_NUMPAD7 = 103;
	public static final int AWE_AK_NUMPAD8 = 104;
	public static final int AWE_AK_NUMPAD9 = 105;
	public static final int AWE_AK_NUMPAD1 = 97;
	public static final int AWE_AK_VOLUME_UP = 175;
	public static final int AWE_AK_NUMPAD0 = 96;
	public static final int AWE_AK_F20 = 131;
	public static final int AWE_AK_NONCONVERT = 29;
	public static final int AWE_AK_ACCEPT = 30;
	public static final int AWE_CURSOR_WEST_PANNING = 28;
	public static final int AWE_AK_MEDIA_LAUNCH_MEDIA_SELECT = 181;
	public static final int AWE_AK_F2 = 113;
	public static final int AWE_AK_F16 = 127;
	public static final int AWE_AK_F1 = 112;
	public static final int AWE_AK_F17 = 128;
	public static final int AWE_AK_EXSEL = 248;
	public static final int AWE_AK_F4 = 115;
	public static final int AWE_AK_F14 = 125;
	public static final int AWE_AK_F3 = 114;
	public static final int AWE_AK_F15 = 126;
	public static final int AWE_AK_F6 = 117;
	public static final int AWE_AK_F12 = 123;
	public static final int AWE_AK_F5 = 116;
	public static final int AWE_AK_F13 = 124;
	public static final int AWE_AK_F8 = 119;
	public static final int AWE_AK_F10 = 121;
	public static final int AWE_AK_F7 = 118;
	public static final int AWE_AK_F11 = 122;
	public static final int AWE_CURSOR_NORTH_EAST_RESIZE = 8;
	public static final int AWE_AK_F9 = 120;
	public static final int AWE_AK_SLEEP = 95;
	public static final int AWE_AK_F18 = 129;
	public static final int AWE_AK_HELP = 47;
	public static final int AWE_AK_F19 = 130;
	Pointer awe_WebCore_new();	
	Pointer awe_WebCore_newWithPlugins(WString pluginPath);
	void awe_WebCore_delete(Pointer webCore);
	void awe_WebCore_setBaseDirectory(Pointer webCore, String baseDirectory);
	void awe_WebCore_setBaseDirectoryW(Pointer webCore, WString baseDirectory);
	Pointer awe_WebCore_createWebView(Pointer webCore, int width, int height);
	void awe_WebCore_setCustomResponsePage(Pointer webCore, int statusCode, WString filePath);
	void awe_WebCore_update(Pointer webCore);
	WString awe_WebCore_getBaseDirectory(Pointer webCore);
	int awe_WebCore_arePluginsEnabled(Pointer webCore);
	void awe_WebCore_clearCache(Pointer webCore);
	void awe_WebCore_clearCookies(Pointer webCore);	
	int awe_WebCore_setCookie(Pointer webCore, String url, String cookieString, int isHTTPOnly, int forceSessionCookie);	
	Pointer awe_WebCore_getCookies(Pointer webCore, String url, int excludeHTTPOnly);	
	void awe_WebCore_deleteCookie(Pointer webCore, String url, String cookieName);
	void awe_WebView_destroy(Pointer webView);
	void awe_WebView_setListener(Pointer webView, WebViewListenerC webViewListener);
	// FIXME WebViewListenerC awe_WebView_getListener(Pointer webView);
	// FIXME PointerByReference awe_WebView_getResourceInterceptor(Pointer webView);	
	void awe_WebView_loadURL(Pointer webView, String url, WString frameName, String username, String password);	
	void awe_WebView_loadURLW(Pointer webView, WString url, WString frameName, String username, String password);	
	void awe_WebView_loadHTML(Pointer webView, String html, WString frameName);	
	void awe_WebView_loadHTMLW(Pointer webView, WString html, WString frameName);	
	void awe_WebView_loadFile(Pointer webView, String file, WString frameName);
	void awe_WebView_goToHistoryOffset(Pointer webView, int offset);
	void awe_WebView_stop(Pointer webView);
	void awe_WebView_reload(Pointer webView);	
	void awe_WebView_executeJavascript(Pointer webView, String javascript, WString frameName);	
	void awe_WebView_executeJavascriptW(Pointer webView, WString javascript, WString frameName);	
	Pointer awe_WebView_executeJavascriptWithResult(Pointer webView, String javascript, WString frameName);	
	Pointer awe_WebView_executeJavascriptWithResultTimeout(Pointer webView, String javascript, WString frameName, int timeoutMS);
	Pointer awe_WebView_executeJavascriptWithResultW(Pointer webView, WString javascript, WString frameName);
	Pointer awe_WebView_executeJavascriptWithResultTimeoutW(Pointer webView, WString javascript, WString frameName, int timeoutMS);
	void awe_WebView_callJavascriptFunction(Pointer webView, WString object, WString function, Pointer args, WString frameName);
	void awe_WebView_createObject(Pointer webView, WString objectName);
	void awe_WebView_destroyObject(Pointer webView, WString objectName);
	void awe_WebView_setObjectProperty(Pointer webView, WString objectName, WString propName, Pointer value);
	void awe_WebView_setObjectCallback(Pointer webView, WString objectName, WString callbackName);
	int awe_WebView_isLoadingPage(Pointer webView);
	int awe_WebView_isDirty(Pointer webView);
	// FIXME void awe_WebView_getDirtyBounds(Pointer webView, RectC rect);
	Pointer awe_WebView_render(Pointer webView);
	void awe_WebView_pauseRendering(Pointer webView);
	void awe_WebView_resumeRendering(Pointer webView);
	void awe_WebView_injectMouseMove(Pointer webView, int x, int y);
	void awe_WebView_injectMouseDown(Pointer webView, int mouseButton);
	void awe_WebView_injectMouseUp(Pointer webView, int mouseButton);
	void awe_WebView_injectMouseWheel(Pointer webView, int scrollAmount);
	// FIXME void awe_WebView_injectKeyboardEvent(Pointer webView, WebKeyboardEventC keyboardEvent);
	void awe_WebView_injectKeyboardEventArgs(Pointer webView, int type, int modifiers, int virtualKeyCode, int nativeKeyCode, ByteBuffer keyIdentifier, CharBuffer text, CharBuffer unmodifiedText, int isSystemKey);
	void awe_WebView_injectKeyboardEventCharacter(Pointer webView, int character);
	void awe_WebView_cut(Pointer webView);
	void awe_WebView_copy(Pointer webView);
	void awe_WebView_paste(Pointer webView);
	void awe_WebView_selectAll(Pointer webView);
	void awe_WebView_setZoom(Pointer webView, int zoomPercent);
	void awe_WebView_resetZoom(Pointer webView);
	int awe_WebView_resize(Pointer webView, int width, int height, int waitForRepaint, int repaintTimeoutMS);
	int awe_WebView_isResizing(Pointer webView);
	void awe_WebView_unfocus(Pointer webView);
	void awe_WebView_focus(Pointer webView);
	void awe_WebView_setTransparent(Pointer webView, int isTransparent);
	void awe_WebView_setURLFilteringMode(Pointer webView, int mode);
	void awe_WebView_addURLFilter(Pointer webView, WString filter);
	void awe_WebView_clearAllURLFilters(Pointer webView);
	void awe_getKeyIdentifierFromVirtualKeyCode(int keyCode, PointerByReference identifier);
	Pointer awe_RenderBuffer_new(int width, int height);	
	Pointer awe_RenderBuffer_newFromBuffer(ByteBuffer buffer, int width, int height, int rowSpan, int autoDeleteBuffer);
	void awe_RenderBuffer_delete(Pointer renderBuffer);
	void awe_RenderBuffer_copyTo(Pointer renderBuffer, ByteBuffer destBuffer, int destRowSpan, int destDepth, int convertToRGBA);
	void awe_RenderBuffer_saveToPNG(Pointer renderBuffer, WString filePath, int preserveTransparency);
	void awe_RenderBuffer_saveToJPEG(Pointer renderBuffer, WString filePath, int quality);
	void awe_RenderBuffer_reserve(Pointer renderBuffer, int width, int height);
	void awe_RenderBuffer_copyFrom(Pointer renderBuffer, ByteBuffer srcBuffer, int srcRowSpan);
	void awe_RenderBuffer_copyArea(Pointer renderBuffer, ByteBuffer srcBuffer, int srcRowSpan, int srcX, int srcY, int srcWidth, int srcHeight, int forceOpaque);
	void awe_RenderBuffer_copyArea2(Pointer renderBuffer, ByteBuffer srcBuffer, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight);
	void awe_RenderBuffer_scrollArea(Pointer renderBuffer, int dx, int dy, int clipX, int clipY, int clipWidth, int clipHeight);
	Pointer awe_RenderBuffer_buffer(Pointer renderBuffer);
	int awe_RenderBuffer_width(Pointer renderBuffer);
	int awe_RenderBuffer_height(Pointer renderBuffer);
	int awe_RenderBuffer_rowSpan(Pointer renderBuffer);
	int awe_RenderBuffer_ownsBuffer(Pointer renderBuffer);
	Pointer awe_JSValue_newNull();
	Pointer awe_JSValue_newBool(int value);
	Pointer awe_JSValue_newInt(int value);
	Pointer awe_JSValue_newDouble(double value);
	Pointer awe_JSValue_newString(String value);
	Pointer awe_JSValue_newWString(WString value);
	Pointer awe_JSValue_newObject(Pointer value);
	Pointer awe_JSValue_newArray(Pointer value);
	void awe_JSValue_delete(Pointer val);
	int awe_JSValue_isBoolean(Pointer val);
	int awe_JSValue_isInteger(Pointer val);
	int awe_JSValue_isDouble(Pointer val);
	int awe_JSValue_isNumber(Pointer val);
	int awe_JSValue_isString(Pointer val);
	int awe_JSValue_isArray(Pointer val);
	int awe_JSValue_isObject(Pointer val);
	int awe_JSValue_isNull(Pointer val);
	// FIXME CharByReference awe_JSValue_toString(Pointer val);
	int awe_JSValue_toInteger(Pointer val);
	double awe_JSValue_toDouble(Pointer val);
	int awe_JSValue_toBoolean(Pointer val);
	Pointer awe_JSValue_getArray(Pointer val);
	Pointer awe_JSValue_getObject(Pointer val);
	Pointer awe_Object_new();
	void awe_Object_delete(Pointer obj);
	void awe_Object_put(Pointer obj, CharBuffer key, Pointer val);
	Pointer awe_Object_get(Pointer obj, CharBuffer key);
	int awe_Object_contains(Pointer obj, CharBuffer key);
	Pointer awe_Array_new();
	void awe_Array_delete(Pointer arr);
	int awe_Array_size(Pointer arr);
	Pointer awe_Array_get(Pointer arr, int index);
	void awe_Array_add(Pointer arr, Pointer val);
	Pointer awe_JSArguments_new();
	void awe_JSArguments_delete(Pointer args);
	int awe_JSArguments_size(Pointer args);
	Pointer awe_JSArguments_get(Pointer args, int index);
	void awe_JSArguments_add(Pointer args, Pointer val);
	
	public class AwesomiumFactory {
		static Awesomium createInstance() {
			try {
				unpackNatives();
			} catch(FileNotFoundException e) {
				System.out.println( "Couldn't write natives:" + e.getMessage());
			}
			
			return (Awesomium)Native.loadLibrary("awesomiumc", Awesomium.class);
		}
		
		private static void unpackNatives() throws FileNotFoundException {
			unpackFile("Awesomium.dll", ".");
			unpackFile("awesomiumc.dll", ".");
			unpackFile("AwesomiumProcess.exe", ".");
			unpackFile("icudt42.dll", ".");
			if(new File("locales").exists() || new File("locales").mkdirs()) {
				unpackFile("locales/en-US.dll", ".");
			} else {
				System.out.println("couldn't create 'locales' folder");
			}
		}
		
		private static void unpackFile(String source, String dst) throws FileNotFoundException {
			InputStream in = null;
			OutputStream out = null;
			
			try {
				in = Awesomium.class.getResourceAsStream("/com/badlogic/gdx/awesomium/natives/" + source);
				out = new BufferedOutputStream(new FileOutputStream(dst + "/" + source));
				
				byte[] bytes = new byte[1024 * 4];
				while (true) {
					int read_bytes = in.read(bytes);
					if (read_bytes == -1) break;
					out.write(bytes, 0, read_bytes);
				}						
			} catch(IOException e) {
				System.out.println( "Couldn't unpack native: " + source + ", " + e.getMessage());
			} finally {
				if(out != null) try { out.close(); } catch (IOException e) {}
				if(in != null) try { in.close(); } catch (IOException e) {}			
			}
		}		
	}	
}
