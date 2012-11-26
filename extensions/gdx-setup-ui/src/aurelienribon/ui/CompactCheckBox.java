package aurelienribon.ui;

import aurelienribon.ui.components.ArProperties;
import aurelienribon.ui.css.DeclarationSet;
import aurelienribon.ui.css.DeclarationSetProcessor;
import aurelienribon.ui.css.Property;
import aurelienribon.ui.css.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class CompactCheckBox extends JPanel {
	private boolean isSelected = false;
	private final List<ActionListener> listeners = new CopyOnWriteArrayList<ActionListener>();
	private final JLabel label = new JLabel();
	private Paint stroke = Color.GRAY;
	private Paint fill = Color.WHITE;
	private int boxW = 14, boxH = 14;

	private static final Color disabledFg;

	static {
		Color c = (Color) UIManager.get("Label.disabledForeground");
		disabledFg = c != null ? c : Color.GRAY;
		Style.registerProcessor(CompactCheckBox.class, new CompactCheckBoxProcessor());
	}

	public CompactCheckBox() {
		this("My Compact Checkbox");
	}

	public CompactCheckBox(String text) {
		setOpaque(false);
		setLayout(new BorderLayout());
		add(label, BorderLayout.CENTER);

		label.setText(text);
		add(Box.createHorizontalStrut(boxW + 5), BorderLayout.WEST);

		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	public void addActionListener(ActionListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.remove(listener);
	}

	@Override
	protected void paintComponent(Graphics g) {
		label.setFont(getFont());
		label.setForeground(isEnabled() ? getForeground() : disabledFg);

		Graphics2D gg = (Graphics2D) g.create();

		int y = (getHeight() - boxH) / 2;

		gg.setPaint(fill);
		gg.fillRect(0, y, boxW-1, boxH-1);
		gg.setPaint(stroke);
		gg.drawRect(0, y, boxW-1, boxH-1);

		if (isSelected) {
			gg.setPaint(stroke);
			gg.fillRect(2, y+2, boxW-4, boxH-4);
		}

		gg.dispose();
	}

	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (isEnabled()) {
				isSelected = !isSelected;
				repaint();

				ActionEvent evt = new ActionEvent(CompactCheckBox.this, 0, null);
				for (ActionListener l : listeners) l.actionPerformed(evt);
			}
		}
	};

	public void setSelected(boolean isSelected) {this.isSelected = isSelected;repaint();}
	public boolean isSelected() {return isSelected;}

	public void setText(String text) {label.setText(text);}
	public void setStroke(Paint stroke) {this.stroke = stroke;}
	public void setFill(Paint fill) {this.fill = fill;}
	public String getText() {return label.getText();}
	public Paint getStroke() {return stroke;}
	public Paint getFill() {return fill;}

	private static class CompactCheckBoxProcessor implements DeclarationSetProcessor<CompactCheckBox>, ArProperties {
		@Override
		public void process(CompactCheckBox t, DeclarationSet ds) {
			Property p;
			p = stroke; if (ds.contains(p)) t.setStroke(ds.getValue(p, Paint.class));
			p = fill; if (ds.contains(p)) t.setFill(ds.getValue(p, Paint.class));
		}
	};
}
