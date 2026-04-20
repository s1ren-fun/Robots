package game;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Модель описывающая движение робота к цели
 */
public class GameModel {
    public static final String ROBOT_POSITION_UPDATED = "ROBOT_POSITION_UPDATED";
    public static final String TARGET_POSITION_UPDATED = "TARGET_POSITION_UPDATED";


    private final PropertyChangeSupport propChangeDispatcher =
            new PropertyChangeSupport(this);

    private RobotModel robot;

    /**
     * Создает игровую модель с реализацией робота по умолчанию
     */
    public GameModel() {
        this.robot = new RobotDefalte();
    }

    /**
     * Двигает робота (изменяет его координаты)
     */
    public void updateRobotPosition() {
        robot.updateRobotPosition();
        propChangeDispatcher.firePropertyChange(ROBOT_POSITION_UPDATED, null, null);
    }

    public int getRobotX() {
        return robot.getX();
    }

    public int getRobotY() {
        return robot.getY();
    }

    public int getTargetX() {
        return robot.getTargetX();
    }

    public int getTargetY() {
        return robot.getTargetY();
    }

    public void setTargetPosition(int x, int y) {
        robot.setTargetPosition(x,y);
        propChangeDispatcher.firePropertyChange(TARGET_POSITION_UPDATED, null, null);
    }

    public double getDirection() {
        return robot.getDirection();
    }

    public void addTextChangeListener(PropertyChangeListener listener) {
        propChangeDispatcher.addPropertyChangeListener(ROBOT_POSITION_UPDATED, listener);
    }

    public void setRobotModel(RobotModel robotModel) {
        this.robot = robotModel;
    }
}
