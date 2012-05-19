
import aurelienribon.libgdx.LibraryDef;
import aurelienribon.libgdx.ui.Ctx;
import aurelienribon.libgdx.ui.MainPanel;
import aurelienribon.ui.components.ArStyle;
import aurelienribon.ui.css.swing.SwingStyle;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.io.FileUtils;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Main {
	public static void main(String[] args) {
		parseArgs(args);

		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException ex) {
				} catch (InstantiationException ex) {
				} catch (IllegalAccessException ex) {
				} catch (UnsupportedLookAndFeelException ex) {
				}

				SwingStyle.init();
				ArStyle.init();

				JFrame frame = new JFrame("LibGDX Project Setup");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setContentPane(new MainPanel());
				frame.setSize(1000, 550);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	private static void parseArgs(String[] args) {
		for (int i=0; i<args.length; i++) {
			if (args[i].equals("-testliburl") && i<args.length) {
				try {
					Ctx.testLibUrl = new URL(args[i+1]);
				} catch (MalformedURLException ex) {
					System.err.println("[warning] Test url is malformed");
				}
			} else if (args[i].equals("-testlibdef") && i<args.length) {
				File file = new File(args[i+1]);
				try {
					Ctx.testLibDef = new LibraryDef(FileUtils.readFileToString(file));
				} catch (IOException ex) {
					System.err.println("[warning]Error while trying to read the test library file");
				}
			}
		}
	}
}
