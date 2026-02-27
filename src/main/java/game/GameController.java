package game;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Провоцирует изменение модели
 */
public class GameController {
    private final GameModel model;

    public GameController(GameModel model) {
        this.model = model;
        Timer m_timer = new Timer("event genarator", true);
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                model.updateRobotPosition();
            }
        },0,10);
    }

    /**
     * Устанавливает новую конечную точку
     *
     * @param point Точка(x, y)
     */
    public void updateTargetPosition(Point point) {
        model.setTargetPosition(point.x, point.y);
    }
}
