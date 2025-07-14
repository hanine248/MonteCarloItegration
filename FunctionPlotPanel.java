package montecarlo;

import javax.swing.*;
import java.awt.*;

public class FunctionPlotPanel extends JPanel {
    private Function function;
    private double a = 0, b = 1;
    private boolean hasFunction = false;

    public void setFunction(Function function, double a, double b) {
        this.function = function;
        this.a = a;
        this.b = b;
        this.hasFunction = true;
        repaint();
    }

    public void clear() {
        this.function = null;
        this.hasFunction = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!hasFunction || function == null) return;

        Graphics2D g2 = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(230, 230, 230));
        for (int i = 0; i < width; i += 50) g2.drawLine(i, 0, i, height);
        for (int i = 0; i < height; i += 50) g2.drawLine(0, i, width, i);

        double center = (a + b) / 2.0;
        double extendedA = center - (b - a);
        double extendedB = center + (b - a);
        double range = extendedB - extendedA;
        double step = range / width;

        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double[] yValues = new double[width];

        for (int i = 0; i < width; i++) {
            double x = extendedA + i * step;
            double y;
            try {
                y = function.evaluate(x);
                if (Double.isNaN(y) || Double.isInfinite(y)) continue;
            } catch (Exception ex) {
                continue;
            }
            yValues[i] = y;
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        double yRange = maxY - minY;
        if (yRange < 1e-6) {
            minY = -1;
            maxY = 1;
        } else {
            minY -= 0.2 * yRange;
            maxY += 0.2 * yRange;
        }

        double scaleY = height / (maxY - minY);
        int zeroY = (int) (height - (-minY * scaleY));

        // Hachure the area under the curve
        g2.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < width - 1; i += 4) {
            double xVal = extendedA + i * step;
            if (xVal >= a && xVal <= b) {
                int y = height - (int) ((yValues[i] - minY) * scaleY);
                int baseY = height - (int) ((0 - minY) * scaleY);
                int startY = Math.min(y, baseY);
                int endY = Math.max(y, baseY);
                for (int j = startY; j < endY; j += 4) {
                    g2.drawLine(i, j, i + 4, j + 4);
                }
            }
        }

        // Draw function curve
        g2.setColor(Color.BLUE);
        g2.setStroke(new BasicStroke(2.0f));
        for (int i = 1; i < width; i++) {
            int x1 = i - 1;
            int x2 = i;
            int y1 = height - (int) ((yValues[i - 1] - minY) * scaleY);
            int y2 = height - (int) ((yValues[i] - minY) * scaleY);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Draw axes
        g2.setColor(Color.BLACK);
        g2.drawLine(0, zeroY, width, zeroY); // x-axis
        g2.drawLine((int) ((0 - extendedA) / step), 0, (int) ((0 - extendedA) / step), height); // y-axis

        // Axis numeration
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

        for (int i = 0; i <= 10; i++) {
            double x = extendedA + i * (extendedB - extendedA) / 10;
            int screenX = (int) ((x - extendedA) / (extendedB - extendedA) * width);
            g2.drawLine(screenX, zeroY - 3, screenX, zeroY + 3);
            g2.drawString(String.format("%.1f", x), screenX - 10, zeroY + 15);
        }

        for (int i = 0; i <= 10; i++) {
            double y = minY + i * (maxY - minY) / 10;
            int screenY = height - (int) ((y - minY) * scaleY);
            g2.drawLine((int) ((0 - extendedA) / step) - 3, screenY, (int) ((0 - extendedA) / step) + 3, screenY);
            g2.drawString(String.format("%.1f", y), 5, screenY + 4);
        }
    }
}

