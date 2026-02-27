package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Визуализатор игрового поля с роботом, следующим за целью.
 * Робот автоматически перемещается к точке, по которой кликнул пользователь.
 */
public class GameVisualizer extends JPanel implements PropertyChangeListener {
    private final GameModel model;

    public GameVisualizer(GameModel model, GameController controller) {
        this.model = model;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.updateTargetPosition(e.getPoint());
            }
        });
        setDoubleBuffered(true);
        model.addTextChangeListener(this);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        drawRobot(g2d,
                model.getRobotX(),
                model.getRobotY(),
                model.getDirection());
        drawTarget(g2d, model.getTargetX(), model.getTargetY());
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int robotCenterX, int robotCenterY, double direction) {
        AffineTransform t = AffineTransform.getRotateInstance(direction,
                robotCenterX, robotCenterY);
        g.setTransform(t);
        g.setColor(Color.RED);
        fillOval(g, robotCenterX, robotCenterY, 40, 15);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX, robotCenterY, 40, 15);
        g.setColor(Color.BLUE);
        fillOval(g, robotCenterX + 10, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX + 10, robotCenterY, 5, 5);
    }

    private void drawTarget(Graphics2D g, int x, int y) {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.BLUE);
        fillOval(g, x, y, 10, 10);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 10, 10);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        repaint();
    }
}
