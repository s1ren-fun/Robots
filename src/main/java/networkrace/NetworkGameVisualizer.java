package networkrace;

import Localization.LocalizationManager;
import javax.swing.*;
import java.awt.*;


/**
 * Визуальный компонент для отрисовки игрового поля в сетевом режиме.
 * Расширяет {@link JPanel} и отвечает за рендеринг всех игровых объектов:
 * фоновой сетки, целевой точки, двух игроков (роботов), а также системных
 * сообщений о победе или ожидании подключения. Получает актуальное состояние
 * игры через {@link NetworkGameAdapter}.
 */
public class NetworkGameVisualizer extends JPanel {
    private final NetworkGameAdapter adapter;
    private static final int ROBOT_WIDTH = 40;
    private static final int ROBOT_HEIGHT = 15;
    private static final int TARGET_SIZE = 10;

    public NetworkGameVisualizer(NetworkGameAdapter adapter) {
        this.adapter = adapter;
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(RaceServer.FIELD_WIDTH, RaceServer.FIELD_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        setRequestFocusEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        drawGrid(g2d);
        drawTarget(g2d, adapter.getTargetX(), adapter.getTargetY());
        if (adapter.isGameStarted()) {
            drawPlayer(g2d, adapter.getMyX(), adapter.getMyY(),
                    adapter.getPlayerId() == 1 ? Color.RED : Color.GREEN,
                    true, adapter.getMyLastDirection());
            drawPlayer(g2d, adapter.getOpponentX(), adapter.getOpponentY(),
                    adapter.getPlayerId() == 1 ? Color.GREEN : Color.RED,
                    false, adapter.getOpponentLastDirection());
        }
        if (adapter.getWinnerId() != -1) {
            drawWinMessage(g2d, adapter.getWinnerId());
        }
        if (adapter.isWaiting()) {
            drawWaitingMessage(g2d);
        }
        g2d.dispose();
    }


    /**
     * Отрисовывает фоновую сетку игрового поля.
     *
     * @param g графический контекст
     */
    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(240, 240, 240));
        for (int x = 0; x < RaceServer.FIELD_WIDTH; x += 50) g.drawLine(x, 0, x, RaceServer.FIELD_HEIGHT);
        for (int y = 0; y < RaceServer.FIELD_HEIGHT; y += 50) g.drawLine(0, y, RaceServer.FIELD_WIDTH, y);
    }

    /**
     * Отрисовывает целевую точку с обводкой и полупрозрачным ореолом.
     *
     * @param g графический контекст
     * @param x X-координата центра цели
     * @param y Y-координата центра цели
     */
    private void drawTarget(Graphics2D g, int x, int y) {
        g.setColor(Color.BLUE);
        fillOval(g, x, y, TARGET_SIZE * 2, TARGET_SIZE * 2);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, TARGET_SIZE * 2, TARGET_SIZE * 2);
        g.setColor(new Color(0, 0, 255, 100));
        fillOval(g, x, y, TARGET_SIZE * 3, TARGET_SIZE * 3);
    }

    /**
     * Отрисовывает игрока (робота) с учётом позиции, цвета и направления.
     * Для управляемого игрока добавляется золотистая обводка.
     */
    private void drawPlayer(Graphics2D g, int x, int y, Color color, boolean isMe, int direction) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.rotate(angleForDirection(direction), x, y);
            g2.setColor(color);
            fillOval(g2, x, y, ROBOT_WIDTH, ROBOT_HEIGHT);
            g2.setColor(Color.BLACK);
            drawOval(g2, x, y, ROBOT_WIDTH, ROBOT_HEIGHT);
            g2.setColor(isMe ? Color.YELLOW : Color.WHITE);
            fillOval(g2, x + 10, y, 6, 6);
            g2.setColor(Color.BLACK);
            drawOval(g2, x + 10, y , 6, 6);
            if (isMe) {
                g2.setColor(new Color(255, 215, 0, 150));
                g2.setStroke(new BasicStroke(2));
                drawOval(g2, x, y, ROBOT_WIDTH + 10, ROBOT_HEIGHT + 10);
            }
        } finally { g2.dispose(); }
    }

    /**
     * Преобразует целочисленный код направления в угол поворота в радианах.
     *
     * @param direction код направления (0 — вправо, 1 — вниз, 2 — влево, 3 — вверх)
     * @return угол в радианах относительно положительной оси X
     */
    private double angleForDirection(int direction) {
        return switch (direction) {
            case 1 -> Math.PI / 2.0;
            case 2 -> Math.PI;
            case 3 -> -Math.PI / 2.0;
            default -> 0.0;
        };
    }

    /**
     * Отрисовывает полупрозрачную панель с сообщением о результате игры.
     *
     * @param g графический контекст
     * @param winnerId идентификатор победителя ({@code 1} или {@code 2})
     */
    private void drawWinMessage(Graphics2D g, int winnerId) {
        LocalizationManager lock = LocalizationManager.getInstance();
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, RaceServer.FIELD_HEIGHT / 2 - 40, RaceServer.FIELD_WIDTH, 80);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        String msg = winnerId == adapter.getPlayerId() ? lock.getLocalizedMessage("YouWin") :
                lock.getLocalizedMessage("EnemyWin");
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (RaceServer.FIELD_WIDTH - fm.stringWidth(msg)) / 2,
                RaceServer.FIELD_HEIGHT / 2 + 8);
    }

    /**
     * Отрисовывает затемняющий оверлей с текстом режима ожидания подключения.
     *
     * @param g графический контекст
     */
    private void drawWaitingMessage(Graphics2D g) {
        g.setColor(new Color(50, 50, 50, 220));
        g.fillRect(0, 0, RaceServer.FIELD_WIDTH, RaceServer.FIELD_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        String msg = LocalizationManager.getInstance().getLocalizedMessage("PlayerDis");

        FontMetrics fm = g.getFontMetrics();
        String[] lines = msg.split("\n");
        int lineHeight = fm.getHeight();
        int totalHeight = lines.length * lineHeight;
        int yStart = (RaceServer.FIELD_HEIGHT - totalHeight) / 2 + fm.getAscent();

        for (String line : lines) {
            int x = (RaceServer.FIELD_WIDTH - fm.stringWidth(line)) / 2;
            g.drawString(line, x, yStart);
            yStart += lineHeight;
        }
    }

    /**
     * Рисует заполненный овал, центрированный относительно заданных координат.
     *
     * @param g графический контекст
     * @param centerX X-координата центра
     * @param centerY Y-координата центра
     * @param diam1 горизонтальный диаметр
     * @param diam2 вертикальный диаметр
     */
    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    /**
     * Рисует контур овала, центрированный относительно заданных координат.
     *
     * @param g графический контекст
     * @param centerX X-координата центра
     * @param centerY Y-координата центра
     * @param diam1 горизонтальный диаметр
     * @param diam2 вертикальный диаметр
     */
    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    /**
     * Безопасно запрашивает перерисовку компонента.
     * Удобный алиас для {@code repaint()}.
     */
    public void repaintGame() { repaint(); }

}