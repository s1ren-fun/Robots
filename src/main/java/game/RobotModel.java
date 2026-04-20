package game;

/**
 * Представление робота
 */
public interface RobotModel {
    /**
     * Обновление позиции робота
     */
    void updateRobotPosition();

    /**
     * Возвращает {@code x} координату робота
     */
    int getX();

    /**
     * Возвращает {@code у} координату робота
     */
    int getY();

    /**
     * Возвращает {@code x} координату точки назначения
     */
    int getTargetX();

    /**
     * Возвращает {@code у} координату точки назначения
     */
    int getTargetY();

    /**
     * Устанавливает точку назначения
     */
    void setTargetPosition(int x, int y);

    /**
     * Возвращает направление, куда направлен робот в радианах
     */
    double getDirection();
}
