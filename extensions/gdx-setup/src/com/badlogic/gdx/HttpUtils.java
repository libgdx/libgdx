
package com.badlogic.gdx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/** Utilities to issue post/get requests and download files.
 * @author mzechner */
public class HttpUtils {
	/** Downloads the content of the given URL to the file
	 * @param url
	 * @param handle
	 * @return whether the download was successful */
	public static boolean downloadFile (String url, File handle) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setUseCaches(true);
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
			conn.connect();
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 4096);
			write(handle, bis);
			bis.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static void write (File file, InputStream input) {
		OutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(file));
			byte[] buffer = new byte[4096];
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				output.write(buffer, 0, length);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error stream writing to file: " + file, ex);
		} finally {
			try {
				if (input != null) input.close();
			} catch (Exception ignored) {
			}
			try {
				if (output != null) output.close();
			} catch (Exception ignored) {
			}
		}
	}
	
	public interface Progress {
		void setProgress(float progress);
		float getProgress();
	}
	
	public static int download (byte[] out, String url, Progress progress) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setUseCaches(true);
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
			if(progress != null) progress.setProgress(0.01f);
			conn.connect();
			int length = conn.getContentLength();
			InputStream bis = conn.getInputStream();
			int len = readBytes(out, bis, length, progress);
			return len;
		} catch (Exception e) {
			e.printStackTrace();
			if(progress != null) progress.setProgress(0);
			return 0;
		}
	}
	
	public static int download (byte[] out, String url) {
		return download(out, url, null);
	}
	
	private static int readBytes(byte[] out, InputStream in, int contentLength, Progress progress) {
		try {
			int readBytes = 0;
			while (true) {
				int length = in.read(out, readBytes, out.length - readBytes);
				if (length == -1) break;
				if (length == 0) throw new RuntimeException("Buffer to small for downloading content");
				readBytes += length;
				if(progress != null) progress.setProgress(readBytes / (float)contentLength);
				if(Thread.currentThread().isInterrupted()) throw new InterruptedException();
			}
			if(progress != null) progress.setProgress(1);
			return readBytes;
		} catch (Exception ex) {
			if(progress != null) progress.setProgress(0);
			return 0;
		} finally {
			try {
				if (in != null) in.close();
			} catch (Exception ignored) {
			}
		}
	}
	
	public static int getHttp (char[] out, String url, String... parameters) {
		return getHttp(out, url, null, parameters);
	}
	
	/** Sends a GET request to the given URL
	 * @param url
	 * @return the response or null */
	public static int getHttp (char[] out, String url, Progress progress, String... parameters) {
		HttpURLConnection conn = null;
		String data = encodeParameters(parameters);
		if(data == null) return 0;

		try {
			System.out.println("fetching: " + url + "?" + data);
			long start = System.nanoTime();
			URL commandURL = new URL(url + "?" + data);
			conn = (HttpURLConnection)commandURL.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setUseCaches(true);
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestMethod("GET");
			if(progress != null) progress.setProgress(0.1f);
			conn.connect();
			System.out.println("connecting took: " + (System.nanoTime() - start) / 1000000000.0f);
			InputStream bis = conn.getInputStream();
			InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
			int total = conn.getContentLength();
			int readChars = 0;
			while (true) {
				int length = isr.read(out, readChars, out.length - readChars);
				if (length == -1) break;
				if (length == 0) throw new RuntimeException("Buffer to small for downloading content");
				readChars += length;
				if(progress != null) progress.setProgress(readChars / (total / 2f));
			}
			if(progress != null) progress.setProgress(1);
			if (isr != null) isr.close();
			System.out.println("fetching took: " + (System.nanoTime() - start) / 1000000000.0f);
			return readChars;
		} catch (Exception e) {
			e.printStackTrace();
			if(progress != null) progress.setProgress(0);
			return 0;
		}
	}
	
	public static String getHttp (String url, String... parameters) {
		return getHttp(url, null, parameters);
	}
	
	/** Sends a GET request to the given URL
	 * @param url
	 * @return the response or null */
	public static String getHttp (String url, Progress progress, String... parameters) {
		HttpURLConnection conn = null;
		String data = encodeParameters(parameters);
		if(data == null) return null;

		try {
			System.out.println("fetching: " + url + (data.length() >0?"?" + data: ""));
			long start = System.nanoTime();
			URL commandURL = new URL(url + (data.length() >0?"?" + data: ""));
			conn = (HttpURLConnection)commandURL.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setUseCaches(true);
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestMethod("GET");
			if(progress != null) progress.setProgress(0.01f);
			conn.connect();
			int total = conn.getContentLength();
			System.out.println("connecting took: " + (System.nanoTime() - start) / 1000000000.0f);
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 1024);
			InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
			StringBuilder output = new StringBuilder(1024*100);
			char[] buffer = new char[1024];
			while (true) {
				int length = isr.read(buffer);
				if (length == -1) break;
				if (length == 0) throw new RuntimeException("Buffer to small for downloading content");
				output.append(buffer, 0, length);
				if(progress != null) progress.setProgress(output.length() / (total / 2f));
				if(Thread.currentThread().isInterrupted()) throw new InterruptedException(url);
			}
			if(progress != null) progress.setProgress(1);
			if (isr != null) isr.close();
			System.out.println("fetching took: " + (System.nanoTime() - start) / 1000000000.0f);
			return output.toString();
		} catch (Exception e) {
			e.printStackTrace();
			if(progress != null) progress.setProgress(0);
			return null;
		}
	}
	
	/** URL-encodes the given parameters and posts them to the given url. The
	 * content type is set to "application/x-www-form-urlencoded".
	 * @param url
	 * @param data
	 * @return the response or null */
	public static String postHttp (String url, String... parameters) {
		String encodedParams = encodeParameters(parameters);
		if(encodedParams == null) return null;
		try {
			return postHttp(url, "application/x-www-form-urlencoded", encodedParams.getBytes("UTF-8"));
		} catch(Exception e) {
			return null;
		}
	}

	/** Posts a UTF-8 encoded string to the given url, specifying the content type.
	 * @param url
	 * @param data
	 * @return the response or null */
	public static String postHttp (String url, String contentType, byte[] data) {
		HttpURLConnection conn = null;
		StringBuilder response;
		
		try {
			URL commandURL = new URL(url);
			conn = (HttpURLConnection)commandURL.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestProperty("Content-Type", contentType);
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
			conn.setRequestMethod("POST");
			conn.connect();

			if (data != null) {
				BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream(), 256);
				bos.write(data);
				bos.flush();
			}
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 256);
			InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
			response = new StringBuilder();
			int ch = 0;
			while ((ch = isr.read()) != -1) {
				response.append((char)ch);
			}
			return response.toString();
		} catch (Exception e) {
			if (conn != null) {
				InputStream in = conn.getErrorStream();
				if (in != null) {
					int c = 0;
					try {
						while ((c = in.read()) != -1) {
							System.out.print((char)c);
						}
					} catch (Exception ex) {
					}
				}
			}
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static HttpResponse postHttpWithCookies (String url, String[] cookies, String... parameters) {
		String encodedParams = encodeParameters(parameters);
		if(encodedParams == null) return null;
		try {
			return postHttpWithCookies(url, cookies, "application/x-www-form-urlencoded", encodedParams.getBytes("UTF-8"));
		} catch(Exception e) {
			return null;
		}
	}
	
	/** Posts a UTF-8 encoded string to the given url, specifying the content type.
	 * @param url
	 * @param data
	 * @return the response or null */
	public static HttpResponse postHttpWithCookies (String url, String[] cookies, String contentType, byte[] data) {
		HttpURLConnection conn = null;
		StringBuilder response;
		
		try {
			URL commandURL = new URL(url);
			conn = (HttpURLConnection)commandURL.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
			conn.setRequestProperty("Content-Type", contentType);
			for(String cookie: cookies) {
				conn.setRequestProperty("cookie", URLDecoder.decode(cookie, "UTF-8"));
			}
			conn.setRequestMethod("POST");
			conn.connect();

			if (data != null) {
				BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream(), 256);
				bos.write(data);
				bos.flush();
			}
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 256);
			InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
			response = new StringBuilder();
			int ch = 0;
			while ((ch = isr.read()) != -1) {
				response.append((char)ch);
			}
			
			HttpResponse resp = new HttpResponse();
			for(String key: conn.getHeaderFields().keySet()) {
				if(key == null) continue;
				if(key.equalsIgnoreCase("set-cookie")) {
					for(String cookie: conn.getHeaderFields().get(key)) {
						resp.cookies.add(cookie);
					}
					break;
				}
			}
			resp.response = response.toString();
			return resp;
		} catch (Exception e) {
			if (conn != null) {
				InputStream in = conn.getErrorStream();
				if (in != null) {
					int c = 0;
					try {
						while ((c = in.read()) != -1) {
							System.out.print((char)c);
						}
					} catch (Exception ex) {
					}
				}
			}
			e.printStackTrace();
			return null;
		}
	}
	
	public static HttpResponse getHttpWithCookies (String url, String[] cookies, String... parameters) {
		return getHttpWithCookies(url, cookies, null, parameters);
	}
	
	/** Sends a GET request to the given URL
	 * @param url
	 * @return the response or null */
	public static HttpResponse getHttpWithCookies (String url, String[] cookies, Progress progress, String... parameters) {
		HttpURLConnection conn = null;
		String data = encodeParameters(parameters);
		if(data == null) return null;
		
		try {
			long start = System.nanoTime();
			URL commandURL = new URL(url + (data.length() >0?"?" + data: ""));
			conn = (HttpURLConnection)commandURL.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setUseCaches(true);
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestMethod("GET");
			for(String cookie: cookies) {
				conn.setRequestProperty("cookie", URLDecoder.decode(cookie, "UTF-8"));
			}
			if(progress != null) progress.setProgress(0.01f);
			conn.connect();
			int total = conn.getContentLength();
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 1024);
			InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
			StringBuilder output = new StringBuilder(1024*100);
			char[] buffer = new char[1024];
			while (true) {
				int length = isr.read(buffer);
				if (length == -1) break;
				if (length == 0) throw new RuntimeException("Buffer to small for downloading content");
				output.append(buffer, 0, length);
				if(progress != null) progress.setProgress(output.length() / (total / 2f));
				if(Thread.currentThread().isInterrupted()) throw new InterruptedException(url);
			}
			if(progress != null) progress.setProgress(1);
			if (isr != null) isr.close();
			HttpResponse resp = new HttpResponse();
			for(String key: conn.getHeaderFields().keySet()) {
				if(key == null) continue;
				if(key.equalsIgnoreCase("set-cookie")) {
					for(String cookie: conn.getHeaderFields().get(key)) {
						resp.cookies.add(cookie);
					}
					break;
				}
			}
			resp.response = output.toString();
			resp.responseCode = conn.getResponseCode();
			return resp;
		} catch (Exception e) {
			if(progress != null) progress.setProgress(0);
			e.printStackTrace();
			return null;
		}
	}
	
	public static HttpResponse getHttpWithCookies (char[] out, String url, String[] cookies, Progress progress, String... parameters) {
		HttpURLConnection conn = null;
		String data = encodeParameters(parameters);
		if(data == null) return null;

		try {
			long start = System.nanoTime();
			URL commandURL = new URL(url + "?" + data);
			conn = (HttpURLConnection)commandURL.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setUseCaches(true);
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestMethod("GET");
			for(String cookie: cookies) {
				conn.setRequestProperty("cookie", URLDecoder.decode(cookie, "UTF-8"));
			}
			if(progress != null) progress.setProgress(0.1f);
			conn.connect();
			InputStream bis = conn.getInputStream();
			InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
			int total = conn.getContentLength();
			int readChars = 0;
			while (true) {
				int length = isr.read(out, readChars, out.length - readChars);
				if (length == -1) break;
				if (length == 0) throw new RuntimeException("Buffer to small for downloading content");
				readChars += length;
				if(progress != null) progress.setProgress(readChars / (total / 2f));
			}
			if(progress != null) progress.setProgress(1);
			if (isr != null) isr.close();
			HttpResponse resp = new HttpResponse();
			for(String key: conn.getHeaderFields().keySet()) {
				if(key == null) continue;
				if(key.equalsIgnoreCase("set-cookie")) {
					for(String cookie: conn.getHeaderFields().get(key)) {
						resp.cookies.add(cookie);
					}
					break;
				}
			}
			resp.responseCode = conn.getResponseCode();
			resp.readChars = readChars;
			return resp;
		} catch (Exception e) {
			if(progress != null) progress.setProgress(0);
			e.printStackTrace();
			return null;
		}
	}
	
	public static class HttpResponse {
		public ArrayList<String> cookies = new ArrayList<String>();
		public String response;
		public int responseCode;
		public int readChars;
		@Override
		public String toString () {
			return "HttpResponse [cookies=" + cookies + ", response=" + response + ", responseCode=" + responseCode + "]";
		}
	}


	private static String encodeParameters (String... parameters) {
		try {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < parameters.length; i += 2) {
				String key = URLEncoder.encode(parameters[i], "UTF-8");
				String value = URLEncoder.encode(parameters[i+1], "UTF-8");
				buffer.append(key);
				buffer.append("=");
				buffer.append(value);
				if(i < parameters.length - 2)  buffer.append("&");
			}
			return buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
