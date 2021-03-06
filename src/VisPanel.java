import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.JPanel;

public class VisPanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener {
	private float xValues[], yValues[], zValues[];
	private int xPoints[], yPoints[];
	private float maxX, minX, maxY, minY;
	// defines the region inside the plot axes
	private Rectangle plotRectangle;
	// defines a border around the plot
	private int borderSize = 40;
	// shape of the data marks
	private Shape dataShape = new Ellipse2D.Float(0.f, 0.f, 5.f, 5.f);

	// color of the data marks
	private Color dataColor = new Color(50, 50, 50, 150);

	private boolean antialiasEnabled = true;
	private Point mousePoint;

	// for drawing the plot offscreen for better efficiency
	private BufferedImage offscreenImage;

	// Get the variable names, by default is MPG
	private String xAxis = "MPG";
	private String yAxis = "MPG";

	private Double[] maxminXY;

	// Get the statistic data
	private Double[] meanDev;
	private Integer[] xbinCounter;
	private Integer[] ybinCounter;

	// Get the current dimensions of the plot region
	int left;
	int right;
	int bottom;
	int top;

	public VisPanel() {
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setStaticdata(Double[] meanDev, Integer[] xbinCounter, Integer[] ybinCounter) {
		this.meanDev = meanDev;
		this.xbinCounter = xbinCounter;
		this.ybinCounter = ybinCounter;
	}

	// sets the data values and forces the panel to layout the boundaries and
	// compute data points
	public void setData(float xValues[], float yValues[], float zValues[], String xAxis, String yAxis,
			Double[] maxminXY) {
		this.xValues = xValues;
		this.yValues = yValues;
		this.zValues = zValues;

		this.xAxis = xAxis;
		this.yAxis = yAxis;

		this.maxminXY = maxminXY;

		maxX = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		for (float value : xValues) {
			if (value > maxX) {
				maxX = value;
			}
			if (value < minX) {
				minX = value;
			}
		}

		maxY = Float.MIN_VALUE;
		minY = Float.MAX_VALUE;
		for (float value : yValues) {
			if (value > maxY) {
				maxY = value;
			}
			if (value < minY) {
				minY = value;
			}
		}

		offscreenImage = null;
		layoutPlot();
		calculatePoints();
		repaint();
	}

	private void layoutPlot() {
		if (xValues == null || yValues == null) {
			return;
		}

		// forces the scatterplot to be square using the smaller dimension
		// (width or height)
		int plotSize = getWidth();
		if (getHeight() < getWidth()) {
			plotSize = getHeight();
		}

		// centers the scatterplot in the middle of the panel
		int xOffset = (getWidth() - plotSize) / 2;
		int yOffset = (getHeight() - plotSize) / 2;

		// get the dimensions of the plot region for later use
		left = borderSize + xOffset;
		right = xOffset + (plotSize - (borderSize * 2));
		bottom = yOffset + (plotSize - (borderSize * 2));
		top = borderSize + yOffset;
		plotRectangle = new Rectangle(left, top, (right - left), (bottom - top));
	}

	// converts x value to screen pixel location
	private int toScreenX(float value, float minValue, float maxValue, int offset, int plotWidth) {
		float norm = (value - minValue) / (maxValue - minValue);
		int x = offset + (int) (Math.round(norm * plotWidth));
		return x;
	}

	// converts y value to screen pixel location
	private int toScreenY(float value, float minValue, float maxValue, int offset, int plotHeight) {
		float normVal = 1.f - ((value - minValue) / (maxValue - minValue));
		int y = offset + (int) (Math.round(normVal * plotHeight));
		return y;
	}

	// computes the x and y pixel locations for scatterplot data
	private void calculatePoints() {
		// nothing to compute
		if (xValues == null || yValues == null) {
			return;
		}

		xPoints = new int[xValues.length];
		for (int i = 0; i < xValues.length; i++) {
			xPoints[i] = toScreenX(xValues[i], minX, maxX, 0, plotRectangle.width);
		}

		yPoints = new int[yValues.length];
		for (int i = 0; i < yValues.length; i++) {
			yPoints[i] = toScreenY(yValues[i], minY, maxY, 0, plotRectangle.height);
		}
	}

	private void render(Graphics2D g2) {
		if (xPoints != null && yPoints != null) {
			g2.setColor(dataColor);
			g2.translate(plotRectangle.x, plotRectangle.y);
			// Label the max and min, if there are multiple, only label the 1st
			// one.
			boolean xMax = false;
			boolean yMax = false;
			boolean xMin = false;
			boolean yMin = false;

			for (int i = 0; i < xPoints.length; i++) {
				int x = xPoints[i] - (int) (dataShape.getBounds2D().getWidth() / 2.);
				int y = yPoints[i] - (int) (dataShape.getBounds2D().getHeight() / 2.);
				// Set the max and min labels
				// if(yPoints[i]==0){
				// if(!xMax){
				// g2.drawString(xAxis + " max", x, y);
				// xMax = true;
				// }
				// }
				// if(yPoints[i]==plotRectangle.height){
				// if(!xMin){
				// g2.drawString(xAxis + " min", x, y + 20);
				// xMin = true;
				// }
				// }
				// if(xPoints[i]==0){
				// if(!yMin){
				// g2.drawString(yAxis + " min", x -60, y);
				// yMin = true;
				// }
				// }
				// if(xPoints[i]==plotRectangle.width){
				// if(!yMax){
				// g2.drawString(yAxis + " max", x, y);
				// yMax = true;
				// }
				// }
				g2.translate(x, y);
				float ridius = zValues[i] * 5;
				Shape circleShape = new Ellipse2D.Float(0.f, 0.f, ridius, ridius);
				g2.draw(circleShape);
				g2.translate(-x, -y);
			}

			// Label the variable names
			// Position based on the real window size
			g2.drawString(xAxis, -50, (bottom + top) / 2 - 20);
			g2.drawString(yAxis, (-left + right) / 2, bottom - 15);

			// Label the max and min values
			g2.drawString(maxminXY[2].toString(), -40, top - 30);
			g2.drawString(maxminXY[3].toString(), -40, bottom - 35);
			g2.drawString(maxminXY[0].toString(), right - 345, bottom - 23);
			g2.drawString(maxminXY[1].toString(), left - 330, bottom - 23);

			DecimalFormat df = new DecimalFormat("#.00");

			// Label the mean
			g2.drawString(" <-- Mean: " + meanDev[1].toString(), right - 270,
					(int) (bottom - (bottom - top) * (meanDev[1] - maxminXY[3]) / (maxminXY[2] - maxminXY[3])) - 40);
			g2.drawString("⇩ Mean" + meanDev[0].toString(),
					(int) ((right - left) * (meanDev[0] - maxminXY[1]) / (maxminXY[0] - maxminXY[1])), top - 70);

			// Label the 2 * standard deviation
			Double a = meanDev[1] - 2 * meanDev[3];
			g2.drawString("↲ 2*D: " + df.format(a), right - 270,
					(int) (bottom - (bottom - top) * (a - maxminXY[3]) / (maxminXY[2] - maxminXY[3])) - 40);
			a = meanDev[1] + 2 * meanDev[3];
			g2.drawString("↰ 2*D: " + df.format(a), right - 270,
					(int) (bottom - (bottom - top) * (a - maxminXY[3]) / (maxminXY[2] - maxminXY[3])) - 40);

			Double b = meanDev[0] - 2 * meanDev[2];
			g2.drawString("↱ 2*D:" + df.format(b),
					(int) ((right - left) * (b - maxminXY[1]) / (maxminXY[0] - maxminXY[1])), top - 70);
			b = meanDev[0] + 2 * meanDev[2];
			g2.drawString("↰ 2*D:" + df.format(b),
					(int) ((right - left) * (b - maxminXY[1]) / (maxminXY[0] - maxminXY[1])), top - 70);

			// capture the mouse movement show the x and y value, set the
			// maximum decimal digits to be 2
			if (mousePoint != null) {
				double x_value = (mousePoint.getX() - left) / (right - left) * (maxminXY[0] - maxminXY[1])
						+ maxminXY[1];
				double y_value = (mousePoint.getY() - bottom) / (top - bottom) * (maxminXY[2] - maxminXY[3])
						+ maxminXY[3];
				g2.drawString(df.format(x_value) + " " + df.format(y_value), (int) (mousePoint.getX()) - 200,
						(int) mousePoint.getY());
			}

			g2.translate(-plotRectangle.x, -plotRectangle.y);

			g2.setStroke(new BasicStroke(2.f));
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(plotRectangle);
			
			int xMaxbin = Integer.MIN_VALUE;
			int yMaxbin = Integer.MIN_VALUE;
			for (int i=0; i<20; i++) {
				if (xbinCounter[i] > xMaxbin) {
					xMaxbin = xbinCounter[i];
				}
				if (ybinCounter[i] > yMaxbin) {
					yMaxbin = ybinCounter[i];
				}
			}
			for (int i = 0; i < 20; i++) {
				Rectangle2D horizonBar = new Rectangle2D.Float(left + (-left + right) / 20 * i + 5, top - 15,
						(-left + right) / 20, 15);
				Rectangle2D verticalBar = new Rectangle2D.Float(right, top + (-top + bottom) / 20 * i + 5, 15,
						(-top + bottom) / 20);
				float xDegree= (float) xbinCounter[i] / (float) xMaxbin;
				float yDegree= (float) ybinCounter[19 - i] / (float) yMaxbin;
				System.out.println(xDegree);
				System.out.println(yDegree);

				g2.setPaint (new Color((int) (255 * xDegree), 0 ,0));
				g2.draw(horizonBar);
				g2.fill(horizonBar);
				g2.setPaint(new Color(0, (int) (255 * yDegree), 0));
				g2.draw(verticalBar);
				g2.fill(verticalBar);

			}
		}
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (antialiasEnabled) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		if (xValues != null && yValues != null) {
			render(g2);

			// code below draws in offscreen image for buffering and greater
			// efficiency
			// if (offscreenImage == null) {
			// offscreenImage = new BufferedImage(getWidth(), getHeight(),
			// BufferedImage.TYPE_INT_ARGB);
			// Graphics2D offscreenImageGraphics =
			// (Graphics2D)offscreenImage.getGraphics();
			// if (antialiasEnabled) {
			// offscreenImageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_ON);
			// offscreenImageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// }
			//
			// render(offscreenImageGraphics);
			// }
			//
			// g2.drawImage(offscreenImage, 0, 0, this);
		}

		// draw the mouse location
		if (mousePoint != null && plotRectangle != null) {
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawLine(mousePoint.x, plotRectangle.y, mousePoint.x, plotRectangle.y + plotRectangle.height);
			g2.drawLine(plotRectangle.x, mousePoint.y, plotRectangle.x + plotRectangle.width, mousePoint.y);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (plotRectangle != null) {
			if (plotRectangle.contains(e.getPoint())) {
				mousePoint = e.getPoint();
				repaint();
			} else {
				mousePoint = null;
				repaint();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void componentResized(ComponentEvent e) {
		offscreenImage = null;
		layoutPlot();
		calculatePoints();
		repaint();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
}
