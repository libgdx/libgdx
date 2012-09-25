package aurelienribon.utils;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Collection of utility methods for various stuff with swing applications.
 * @author Aurelien Ribon | http://www.aurelienribon.com
 */
public class SwingUtils {
	/**
	 * Adds a listener to the window parent of the given component. Can be
	 * called before the component is added to its hierachy.
	 */
	public static void addWindowListener(final Component source, final WindowListener listener) {
		if (source instanceof Window) {
			((Window)source).addWindowListener(listener);
		} else {
			source.addHierarchyListener(new HierarchyListener() {
				@Override public void hierarchyChanged(HierarchyEvent e) {
					if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED) {
						SwingUtilities.getWindowAncestor(source).addWindowListener(listener);
					}
				}
			});
		}
	}

	/**
	 * Gets the parent JFrame of the component.
	 */
	public static JFrame getJFrame(Component cmp) {
		return (JFrame) SwingUtilities.getWindowAncestor(cmp);
	}

	/**
	 * Opens the given website in the default browser, or shows a message saying
	 * that no default browser could be accessed.
	 */
	public static void browse(String url, Component msgParent) {
		boolean error = false;

		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (URISyntaxException ex) {
				throw new RuntimeException(ex);
			} catch (IOException ex) {
				error = true;
			}
		} else {
			error = true;
		}

		if (error) {
			String msg = "Impossible to open the default browser from the application.\nSorry.";
			JOptionPane.showMessageDialog(msgParent, msg);
		}
	}

	/**
	 * Adds a hand cursor to the component, as well as a click listener that
	 * triggers a browse action to the given url.
	 */
	public static void addBrowseBehavior(final Component cmp, final String url){
		if (url == null) return;
		cmp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cmp.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				JFrame frame = getJFrame(cmp);
				browse(url, frame);
			}
		});
	}

	/**
	 * Packs the window size when it is opened. Useful for dialogs using the
	 * Universal CSS Engine.
	 */
	public static void packLater(final Window wnd, final Component parent) {
		wnd.pack();
		wnd.setLocationRelativeTo(parent);
		wnd.addWindowListener(new WindowAdapter() {
			@Override public void windowOpened(WindowEvent e) {
				wnd.pack();
				wnd.setLocationRelativeTo(parent);
			}
		});
	}

	/**
	 * Statically imports the given font. It can then be used directly in
	 * calls to <code>new Font()</code>.
	 */
	public static void importFont(InputStream stream) {
		try {
			Font font1 = Font.createFont(Font.TRUETYPE_FONT, stream);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font1);
		} catch (FontFormatException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
