package aurelienribon.gdxsetupui.ui.panels;

import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Quad;
import aurelienribon.ui.css.DeclarationSet;
import aurelienribon.ui.css.DeclarationSetProcessor;
import aurelienribon.ui.css.Property;
import aurelienribon.ui.css.Selector;
import aurelienribon.ui.css.Style;
import aurelienribon.ui.css.swing.SwingProperties;
import aurelienribon.utils.HttpUtils;
import aurelienribon.utils.HttpUtils.DownloadListener;
import aurelienribon.utils.HttpUtils.DownloadTask;
import aurelienribon.utils.Res;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class TaskPanel extends JPanel {
	private final List<Tile> tiles = new ArrayList<Tile>();
	private TweenManager tweenManager;

	static {
		Style.registerProcessor(TaskPanel.class, new StyleProcessor());
		Style.registerProcessor(DownloadTile.class, new DownloadTileStyleProcessor());
	}

	public TaskPanel() {
		setLayout(null);
		setPreferredSize(new Dimension(50, 30));
		Style.registerCssClasses(this, ".taskBar");

		HttpUtils.addListener(new HttpUtils.Listener() {
			@Override public void newDownload(DownloadTask task) {
				addDownloadTile(task);
			}
		});
	}

	public void setTweenManager(TweenManager tweenManager) {
		this.tweenManager = tweenManager;
	}

	// -------------------------------------------------------------------------
	// Style
	// -------------------------------------------------------------------------

	private Style style;
	private List<Selector.Atom> styleStack;

	private static class StyleProcessor implements DeclarationSetProcessor<TaskPanel> {
		@Override
		public void process(TaskPanel t, DeclarationSet ds) {
			t.style = ds.getStyle();
			t.styleStack = Style.getLastStack();
		}
	}

	private static class DownloadTileStyleProcessor implements DeclarationSetProcessor<DownloadTile>, SwingProperties {
		@Override
		public void process(DownloadTile t, DeclarationSet ds) {
			Property p;

			t.titleLabel.setForeground(t.getForeground());
			t.stateLabel.setForeground(t.getForeground());
			t.titleLabel.setFont(t.getFont());
			t.stateLabel.setFont(t.getFont());
		}

	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void addDownloadTile(DownloadTask task) {
		final DownloadTile tile = new DownloadTile(task);
		Style.registerCssClasses(tile, ".tile");
		if (style != null) Style.apply(tile, style, styleStack);

		tile.setLocation(getNextTileX() + getWidth(), 2);

		Tween.to(tile, SLAnimator.ComponentAccessor.X, 2)
			.target(getNextTileX())
			.ease(Quad.OUT)
			.start(tweenManager);

		task.addListener(new DownloadListener() {
			@Override public void onUpdate(int length, int totalLength) {tile.setCurrentSize(length, totalLength);}
			@Override public void onComplete() {tile.setToComplete(); tile.disappear(3.5f);}
			@Override public void onError(IOException ex) {tile.setToError("IOException: " + ex.getMessage());}
		});

		add(tile);
		tiles.add(tile);
	}

	private int getNextTileX() {
		return 2 + 202*tiles.size();
	}

	private int getTileX(Tile tile) {
		int idx = tiles.indexOf(tile);
		return 2 + 202*idx;
	}

	private final TweenCallback tileRemovedCallback = new TweenCallback() {
		@Override
		public void onEvent(int type, BaseTween<?> source) {
			tiles.remove((Tile) source.getUserData());

			for (Tile tile : tiles) {
				tweenManager.killTarget(tile, SLAnimator.ComponentAccessor.X);
				Tween.to(tile, SLAnimator.ComponentAccessor.X, 2)
					.target(getTileX(tile))
					.ease(Quad.OUT)
					.start(tweenManager);
			}
		}
	};

	// -------------------------------------------------------------------------

	private class Tile extends JPanel {
	}

	private class DownloadTile extends Tile {
		private final JLabel logoLabel = new JLabel();
		private final JLabel titleLabel = new JLabel();
		private final JLabel stateLabel = new JLabel();
		private final JLabel cancelLabel = new JLabel(Res.getImage("gfx/ic_cancel.png"));

		public DownloadTile(final DownloadTask task) {
			setBackground(Color.LIGHT_GRAY);
			setLayout(null);
			setSize(200, 26);

			if (task.getTag().startsWith("Master")) logoLabel.setIcon(Res.getImage("gfx/ic24_cog.png"));
			else if (task.getTag().startsWith("Def")) logoLabel.setIcon(Res.getImage("gfx/ic24_file.png"));
			else if (task.getTag().startsWith("Version")) logoLabel.setIcon(Res.getImage("gfx/ic24_cog.png"));
			else logoLabel.setIcon(Res.getImage("gfx/ic24_download.png"));

			logoLabel.setBounds(0, 1, 24, 24);
			titleLabel.setText(task.getTag());
			titleLabel.setVerticalAlignment(SwingConstants.TOP);
			titleLabel.setBounds(29, 0, getWidth()-30, getHeight());
			stateLabel.setText("0 / ?? (?%)");
			stateLabel.setVerticalAlignment(SwingConstants.BOTTOM);
			stateLabel.setBounds(29, 0, getWidth()-30, getHeight());
			cancelLabel.setBounds(199-16, 0, 16, 16);

			add(logoLabel);
			add(titleLabel);
			add(stateLabel);
			add(cancelLabel);

			cancelLabel.addMouseListener(new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {
					cancelLabel.setIcon(null);
					disappear(0);
					task.stop();
				}
			});
		}

		public void setCurrentSize(int currentSize, int totalSize) {
			int percent = totalSize > 0 ? (int) (100 * ((float)currentSize / totalSize)) : -1;

			if (percent >= 0) {
				stateLabel.setText(currentSize + " / " + totalSize + " (" + percent + "%)");
			} else {
				stateLabel.setText(currentSize + " / ?? (?%)");
			}
		}

		public void setToError(String msg) {
			firePropertyChange("error", false, true);
			setToolTipText(msg);
		}

		public void setToComplete() {
			firePropertyChange("complete", false, true);
			cancelLabel.setIcon(null);
		}

		public void disappear(float delay) {
			Tween.to(this, SLAnimator.ComponentAccessor.Y, 0.3f)
				.targetRelative(50)
				.ease(Quad.IN)
				.delay(delay)
				.setCallback(tileRemovedCallback)
				.setUserData(this)
				.start(tweenManager);
		}
	}
}
