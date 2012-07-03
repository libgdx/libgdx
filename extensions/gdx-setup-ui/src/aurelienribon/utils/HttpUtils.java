package aurelienribon.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class HttpUtils {
	public static DownloadTask downloadAsync(URL input, OutputStream output, Callback callback) {
		final DownloadTask task = new DownloadTask(input, output, callback);

		new Thread(new Runnable() {
			@Override public void run() {
				task.download();
			}
		}).start();

		return task;
	}

	public static class Callback {
		public void completed() {}
		public void canceled() {}
		public void error(IOException ex) {}
		public void updated(int length, int totalLength) {}
	}

	public static class DownloadTask {
		private final URL input;
		private final OutputStream output;
		private final Callback callback;
		private boolean run = true;

		public DownloadTask(URL input, OutputStream output, Callback callback) {
			this.input = input;
			this.output = output;
			this.callback = callback;
		}

		public void stop() {
			run = false;
		}

		public URL getInput() {
			return input;
		}

		public OutputStream getOutput() {
			return output;
		}

		public Callback getCallback() {
			return callback;
		}

		private void download() {
			OutputStream os = null;
			InputStream is = null;
			IOException ex = null;

			try {
				HttpURLConnection connection = (HttpURLConnection) input.openConnection();
				connection.setDoInput(true);
				connection.setDoOutput(false);
				connection.setUseCaches(true);
				connection.setConnectTimeout(3000);
				connection.connect();

				is = new BufferedInputStream(connection.getInputStream(), 4096);
				os = output;

				byte[] data = new byte[4096];
				int length = connection.getContentLength();
				int total = 0;

				int count;
				while (run && (count = is.read(data)) != -1) {
					total += count;
					os.write(data, 0, count);
					if (callback != null) callback.updated(total, length);
				}

			} catch (IOException ex1) {
				ex = ex1;

			} finally {
				if (os != null) try {os.flush(); os.close();} catch (IOException ex1) {}
				if (is != null) try {is.close();} catch (IOException ex1) {}

				if (callback != null) {
					if (ex != null) callback.error(ex);
					else if (run == true) callback.completed();
					else callback.canceled();
				}
			}
		}
	}
}
