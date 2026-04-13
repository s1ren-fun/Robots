package networkrace;

import java.awt.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Серверная часть сетевой игры «Гонки».
 * <p>
 * Отвечает за:
 * Приём подключений от двух клиентов (хост и игрок)
 * Синхронизацию игрового состояния (позиции игроков, цель, победитель)
 * Обработку команд движения, перезапуска и завершения игры
 * Рассылку обновлений состояния обоим подключённым клиентам
 * Обработку отключений и переход в режим ожидания нового игрока
 */
public class RaceServer {
    public static final int PORT = 40000;
    public static final int FIELD_WIDTH = 800;
    public static final int FIELD_HEIGHT = 600;
    private static final int STEP = 15;
    private static final int TARGET_RADIUS = 14;

    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Object lock = new Object();
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    private PlayerConnection player1;
    private PlayerConnection player2;

    private final Point p1 = new Point(80, FIELD_HEIGHT / 2);
    private final Point p2 = new Point(FIELD_WIDTH - 80, FIELD_HEIGHT / 2);
    private Point target = randomTarget();
    private volatile int winnerId = -1;

    public RaceServer(int port) {
        this.port = port;
    }

    /**
     * Запускает сервер: создаёт сокет, устанавливает флаг активности
     * и передаёт цикл приёма подключений в пул потоков.
     */
    public void start() throws IOException {
        if (running) return;
        serverSocket = new ServerSocket(port);
        running = true;
        pool.submit(this::acceptLoop);
    }

    /**
     * Останавливает сервер: закрывает сокет, отключает всех игроков
     * и завершает работу пула потоков.
     */
    public void stop() {
        running = false;
        closeQuietly(serverSocket);
        closePlayer(player1);
        closePlayer(player2);
        pool.shutdownNow();
    }

    /**
     * Основной цикл приёма подключений.
     * Работает в фоновом потоке и выполняет:
     * Ожидает подключения первого клиента → назначает роль {@code ROLE 1} (хост)
     * Ожидает подключения второго клиента → назначает роль {@code ROLE 2}, сбрасывает игру,
     * рассылает стартовое состояние и передаёт обработку в пул
     * При отключении любого игрока вызывает {@link #handleDisconnect(int)}
     */
    private void acceptLoop() {
        try {
            while (running) {
                if (player1 == null) {
                    Socket s = serverSocket.accept();
                    player1 = new PlayerConnection(1, s);
                    player1.send("ROLE 1");
                    broadcast("INFO " + "HostConnect");
                    pool.submit(player1);
                } else if (player2 == null) {
                    Socket s = serverSocket.accept();
                    player2 = new PlayerConnection(2, s);
                    player2.send("ROLE 2");
                    broadcast("INFO " + "EnemyConnect");
                    resetGameLocked();
                    sendStartState();
                    pool.submit(player2);
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (IOException e) {
            if (running) broadcast("INFO Server error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Рассылает текущее состояние игры (координаты игроков и цели) обоим клиентам.
     * Формат сообщения: {@code STATE p1x p1y p2x p2y targetX targetY}
     */
    private void broadcastState() {
        broadcast(String.format("STATE %d %d %d %d %d %d", p1.x, p1.y, p2.x, p2.y, target.x, target.y));
    }

    /**
     * Обрабатывает команду от игрока.
     * <p>
     * Поддерживаемые команды:
     * {@code FINISH} — рассылает {@code CLOSE} и останавливает сервер в фоновом потоке
     * {@code RESTART} — сбрасывает игру, если есть победитель
     * {@code UP/DOWN/LEFT/RIGHT} — перемещает игрока, проверяет победу, рассылает обновление
     *
     * @param playerId идентификатор отправителя ({@code 1} или {@code 2})
     * @param command  текстовая команда от клиента
     */
    private void processMove(int playerId, String command) {
        synchronized (lock) {
            String normalized = command.trim().toUpperCase();

            if ("FINISH".equals(normalized)) {
                broadcast("CLOSE");

                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    stop();
                }, "server-stop-thread").start();

                return;
            }

            if ("RESTART".equals(normalized)) {
                if (winnerId != -1) {
                    resetGameLocked();
                    sendStartState();
                }
                return;
            }

            if (winnerId != -1) return;

            Point p = (playerId == 1) ? p1 : p2;
            switch (normalized) {
                case "UP":
                    p.y -= STEP;
                    break;
                case "DOWN":
                    p.y += STEP;
                    break;
                case "LEFT":
                    p.x -= STEP;
                    break;
                case "RIGHT":
                    p.x += STEP;
                    break;
                default:
                    return;
            }
            clamp(p);

            if (reachedTarget(p, target)) {
                winnerId = playerId;
                broadcastState();
                broadcast("WIN " + playerId);
                broadcast("INFO " + playerId + " WonRest");
            } else {
                broadcastState();
            }
        }
    }

    /**
     * Сбрасывает игровое состояние в начальные значения.
     * <p>
     * Перемещает игроков на стартовые позиции, генерирует новую цель
     * и сбрасывает идентификатор победителя.
     */
    private void resetGameLocked() {
        p1.setLocation(80, FIELD_HEIGHT / 2);
        p2.setLocation(FIELD_WIDTH - 80, FIELD_HEIGHT / 2);
        target = randomTarget();
        winnerId = -1;
    }

    /**
     * Рассылает стартовое состояние игры обоим клиентам.
     */
    private void sendStartState() {
        broadcast(String.format(
                "START %d %d %d %d %d %d %d %d %d %d",
                FIELD_WIDTH, FIELD_HEIGHT,
                p1.x, p1.y, p2.x, p2.y,
                target.x, target.y,
                TARGET_RADIUS, STEP
        ));
        broadcastState();
    }


    /**
     * Проверяет, достиг ли игрок цели.
     *
     * @param p позиция игрока
     * @param t позиция цели
     * @return {@code true}, если расстояние между точками ≤ {@value #TARGET_RADIUS}
     */
    private boolean reachedTarget(Point p, Point t) {
        double dx = p.x - t.x, dy = p.y - t.y;
        return Math.sqrt(dx * dx + dy * dy) <= TARGET_RADIUS;
    }

    /**
     * Ограничивает позицию игрока пределами игрового поля.
     *
     * @param p позиция для нормализации (изменяется «на месте»)
     */
    private void clamp(Point p) {
        p.x = Math.max(0, Math.min(FIELD_WIDTH, p.x));
        p.y = Math.max(0, Math.min(FIELD_HEIGHT, p.y));
    }

    /**
     * Генерирует случайную позицию цели в допустимой области поля.
     *
     * @return новая точка {@code Point} с координатами в диапазоне [100; поле-100]
     */
    private Point randomTarget() {
        Random rnd = new Random();
        return new Point(100 + rnd.nextInt(FIELD_WIDTH - 200), 100 + rnd.nextInt(FIELD_HEIGHT - 200));
    }

    /**
     * Рассылает сообщение обоим подключённым клиентам.
     * <p>
     * Если клиент ещё не подключён или уже отключён — отправка для него пропускается.
     *
     * @param msg текст сообщения для отправки
     */
    private void broadcast(String msg) {
        if (player1 != null) player1.send(msg);
        if (player2 != null) player2.send(msg);
    }

    /**
     * Безопасно закрывает подключение игрока.
     *
     * @param p подключение для закрытия (может быть {@code null})
     */
    private void closePlayer(PlayerConnection p) {
        if (p != null) {
            p.close();
        }
    }

    /**
     * Закрывает ресурс {@link Closeable}, подавляя {@link IOException}.
     *
     * @param c закрываемый ресурс (может быть {@code null})
     */
    private void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Обрабатывает отключение игрока.
     * Логика:
     * Если отключился игрок 2 → сервер переходит в режим ожидания,
     * сбрасывает игру и очищает ссылку на {@code player2}
     * Если отключился игрок 1 (хост) → сервер останавливается полностью
     */
    private void handleDisconnect(int disconnectedId) {
        synchronized (lock) {
            if (!running) return;

            if (disconnectedId == 2) {
                broadcast("WAITING " + "WaitingPlayer2Dis");
                player2 = null;
                winnerId = -1;
                resetGameLocked();
            } else {
                broadcast("INFO " + "HostDis");
                stop();
            }
        }
    }


    /**
     * Внутренний класс, представляющий подключение одного игрока.
     * Инкапсулирует сокет, потоки ввода/вывода и логику обработки входящих команд.
     * Каждый экземпляр выполняется в отдельном потоке из пула сервера.
     */
    private class PlayerConnection implements Runnable {
        private final int id;
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;

        /**
         * Создаёт подключение для указанного игрока.
         */
        PlayerConnection(int id, Socket socket) throws IOException {
            this.id = id;
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }


        /**
         * Отправляет сообщение клиенту.
         *
         * @param msg текст сообщения
         */
        void send(String msg) {
            synchronized (out) {
                out.println(msg);
            }
        }

        @Override
        public void run() {
            boolean disconnected = false;
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    processMove(id, line.trim().toUpperCase());
                }
                disconnected = running;
            } catch (IOException ignored) {
                disconnected = running;
            } finally {
                if (disconnected) handleDisconnect(id);
                close();
            }
        }

        /**
         * Закрывает все ресурсы подключения: входной/выходной потоки и сокет.
         */
        void close() {
            closeQuietly(in);
            closeQuietly(out);
            closeQuietly(socket);
        }
    }
}