package game;

import java.awt.Color;

/**
 * Пример робота для динамической загрузки из .jar
 * Реализует требования ТЗ: быстрая скорость, малый угол поворота, свои визуальные параметры.
 */
public class RobotV2 implements RobotModel {
    private volatile double robotPositionX = 100;
    private volatile double robotPositionY = 100;
    private volatile double robotDirection = 0;

    private volatile int targetPositionX = 150;
    private volatile int targetPositionY = 100;

    private static final double MAX_VELOCITY = 0.5;
    private static final double MAX_ANGULAR_VELOCITY = 0.04;

    private double distance(double x1, double y1, double x2, double y2) {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private double angleTo(double fromX, double fromY, double toX, double toY) {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    @Override
    public void updateRobotPosition() {
        double distance = distance(targetPositionX, targetPositionY,
                robotPositionX, robotPositionY);
        if (distance < 0.5) {
            return;
        }
        double velocity = MAX_VELOCITY;
        double angleToTarget = angleTo(robotPositionX, robotPositionY,
                targetPositionX, targetPositionY);
        double angularVelocity = 0;
        if ((angleToTarget - robotDirection > 0 &&
                angleToTarget - robotDirection < Math.PI)
                |
                (angleToTarget - robotDirection > -2 * Math.PI &&
                        angleToTarget - robotDirection < -Math.PI)) {
            angularVelocity = MAX_ANGULAR_VELOCITY;
        } else if ((angleToTarget - robotDirection < 0 &&
                angleToTarget - robotDirection > -Math.PI)
                |
                (angleToTarget - robotDirection < 2 * Math.PI &&
                        angleToTarget - robotDirection > Math.PI)) {
            angularVelocity = -MAX_ANGULAR_VELOCITY;
        }

        if (oppositeIfBug(velocity, angularVelocity)) {
            angularVelocity *= -1;
        }

        moveRobot(velocity, angularVelocity, 10);
    }

    /**
     * Возвращает значение true, если целевая точка
     * находится в одном из кругов траектории,
     * в противном случае значение false
     */
    private boolean oppositeIfBug(double velocity, double angularVelocity) {
        double radiusTrajCircle = (velocity / angularVelocity);

        double diffXFromTargetTo1Center =
                robotPositionX - radiusTrajCircle *
                        Math.sin(robotDirection) - targetPositionX;
        double diffXFromTargetTo2Center =
                robotPositionX + radiusTrajCircle *
                        Math.sin(robotDirection) - targetPositionX;
        double diffYFromTargetTo1Center =
                robotPositionY + radiusTrajCircle *
                        Math.cos(robotDirection) - targetPositionY;
        double diffYFromTargetTo2Center =
                robotPositionY - radiusTrajCircle *
                        Math.cos(robotDirection) - targetPositionY;

        return diffXFromTargetTo1Center * diffXFromTargetTo1Center +
                diffYFromTargetTo1Center * diffYFromTargetTo1Center <
                radiusTrajCircle * radiusTrajCircle
                |
                diffXFromTargetTo2Center * diffXFromTargetTo2Center +
                        diffYFromTargetTo2Center * diffYFromTargetTo2Center <
                        radiusTrajCircle * radiusTrajCircle;
    }

    private static double applyLimits(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    private void moveRobot(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, MAX_VELOCITY);
        angularVelocity = applyLimits(angularVelocity, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);
        double newX = robotPositionX + velocity / angularVelocity *
                (Math.sin(robotDirection + angularVelocity * duration) -
                        Math.sin(robotDirection));
        if (!Double.isFinite(newX)) {
            newX = robotPositionX + velocity * duration * Math.cos(robotDirection);
        }
        double newY = robotPositionY - velocity / angularVelocity *
                (Math.cos(robotDirection + angularVelocity * duration) -
                        Math.cos(robotDirection));
        if (!Double.isFinite(newY)) {
            newY = robotPositionY + velocity * duration * Math.sin(robotDirection);
        }

        robotPositionX = newX;
        robotPositionY = newY;
        double newDirection = asNormalizedRadians(robotDirection + angularVelocity * duration);
        robotDirection = newDirection;
    }

    private int round(double value) {
        return (int) (value + 0.5);
    }

    private double asNormalizedRadians(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle - 2 * Math.PI >= 0.01) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    @Override
    public int getX() {
        return round(robotPositionX);
    }

    @Override
    public int getY() {
        return round(robotPositionY);
    }

    @Override
    public int getTargetX() {
        return targetPositionX;
    }

    @Override
    public int getTargetY() {
        return targetPositionY;
    }

    @Override
    public void setTargetPosition(int x, int y) {
        targetPositionX = x;
        targetPositionY = y;
    }

    @Override
    public double getDirection() {
        return robotDirection;
    }
}