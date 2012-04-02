
import aurelienribon.libgdx.ui.MainPanel;
import aurelienribon.ui.components.AruiStyle;
import aurelienribon.ui.css.swing.SwingStyle;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Main {
	public static void main(String[] args) {
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
				AruiStyle.init();

				JFrame frame = new JFrame("LibGDX Project Setup");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setContentPane(new MainPanel());
				frame.setSize(1000, 550);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
}
