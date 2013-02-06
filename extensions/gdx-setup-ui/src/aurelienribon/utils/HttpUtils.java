package aurelienribon.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Utility class used to quickly download files on distant servers.
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class HttpUtils {
	private static final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	private static String referer = "http://aurelienribon-dev.com/unknown";

	/**
	 * Sets the referer used when accessing a remote file.
	 * Defaults to "http://aurelienribon-dev.com/unknown".
	 */
	public static void setReferer(String referer) {
		HttpUtils.referer = referer;
	}

	/**
	 * Gets the referer used when accessing a remote file.
	 * Defaults to "http://aurelienribon-dev.com/unknown".
	 */
	public static String getReferer() {
		return referer;
	}

	/**
	 * Asynchronously downloads the file located at the given url. Content is
	 * written to the given stream. If the url is malformed, the method returns
	 * null. Else, a {@link DownloadTask} is returned. Use it if you need to
	 * cancel the download at any time.
	 * <p/>
	 * The returned object also lets you add event listeners to warn you
	 * when the download is complete, if an error happens (such as a connection
	 * loss). The listeners also let you be notified of the download progress.
	 */
	public static DownloadTask downloadAsync(String url, OutputStream output) {
		return downloadAsync(url, output, null);
	}

	/**
	 * Asynchronously downloads the file located at the given url. Content is
	 * written to the given stream. If the url is malformed, the method returns
	 * null. Else, a {@link DownloadTask} is returned. Use it if you need to
	 * cancel the download at any time.
	 * <p/>
	 * The returned object also lets you add event listeners to warn you
	 * when the download is complete, if an error happens (such as a connection
	 * loss). The listeners also let you be notified of the download progress.
	 * <p/>
	 * You can also assign a custom tag to the download, to pass information
	 * to the listeners for instance.
	 */
	public static DownloadTask downloadAsync(String url, OutputStream output, String tag) {
		URL input;

		try {
			input = new URL(url);
		} catch (MalformedURLException ex) {
			return null;
		}

		final DownloadTask task = new DownloadTask(input, output, tag);
		for (Listener lst : listeners) lst.newDownload(task);

		task.start();
		return task;
	}

	/**
	 * Adds a new listener to catch the start of new downloads.
	 */
	public static void addListener(Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the given listener.
	 */
	public static void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	// -------------------------------------------------------------------------
	// Classes
	// -------------------------------------------------------------------------

	/**
	 * Listener for start of new downloads.
	 */
	public static interface Listener {
		public void newDownload(DownloadTask task);
	}

	/**
	 * Listener for a {@link DownloadTask}. Used to get notified about all the
	 * download events: completion, errors and progress.
	 */
	public static class DownloadListener {
		public void onComplete() {}
		public void onCancel() {}
		public void onError(IOException ex) {}
		public void onUpdate(int length, int totalLength) {}
	}

	/**
	 * A download task lets you cancel the current download in progress. You
	 * can also access its parameters, such as the input and output streams.
	 */
	public static class DownloadTask {
		private final URL input;
		private final OutputStream output;
		private final String tag;
		private final List<DownloadListener> listeners = new CopyOnWriteArrayList<DownloadListener>();
		private boolean run = true;

		public DownloadTask(URL input, OutputStream output, String tag) {
			this.input = input;
			this.output = output;
			this.tag = tag;
		}

		/**
		 * Adds a new listener to listen for the task events.
		 */
		public void addListener(DownloadListener listener) {
			listeners.add(listener);
		}

		/**
		 * Cancels the download. If a callback is associated to the download
		 * task, its onCancel() method will be raised instead of the
		 * onComplete() one.
		 */
		public void stop() {
			if (run == false) for (DownloadListener lst : listeners) lst.onCancel();
			run = false;
		}

		public URL getInput() {return input;}
		public OutputStream getOutput() {return output;}
		public String getTag() {return tag;}

		private void start() {
			new Thread(new Runnable() {@Override public void run() {
				OutputStream os = null;
				InputStream is = null;
				IOException ex = null;

				if (tag.equals("Test"))
					System.out.println("");

				try {
					HttpURLConnection connection = (HttpURLConnection) input.openConnection();
					if (referer != null) connection.addRequestProperty("REFERER", referer);
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
						for (DownloadListener l : listeners) l.onUpdate(total, length);
					}

				} catch (IOException ex1) {
					ex = ex1;

				} finally {
					if (os != null) try {os.flush(); os.close();} catch (IOException ex1) {}
					if (is != null) try {is.close();} catch (IOException ex1) {}

					if (ex != null) for (DownloadListener l : listeners) l.onError(ex);
					else if (run == true) for (DownloadListener l : listeners) l.onComplete();
					else for (DownloadListener l : listeners) l.onCancel();

					run = false;
				}

			}}).start();
		}
	}
}
