package networkrace;

import Localization.LocalizationManager;
import log.Logger;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Клиентская часть сетевого соединения для игры «Гонки».
 * Протокол обмена (текстовый, разделитель — пробел):
 *   START w h tX tY p1X p1Y p2X p2Y r s  — старт игры с параметрами
 *   STATE p1X p1Y p2X p2Y tX tY          — обновление позиций
 *   WIN id                                — объявление победителя
 *   INFO text                             — информационное сообщение
 *   WAITING text                          — переход в режим ожидания
 *   CLOSE                                 — запрос на закрытие всех окон
 */
public class RaceClient {
    private final Listener listener;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread readerThread;
    private volatile boolean running = false;

    public RaceClient(Listener listener) {
        this.listener = listener;
    }

    /**
     * Устанавливает соединение с сервером и запускает поток чтения.
     * Метод выполняет:
     * Создание сокета и подключение к {@code host:port} с таймаутом 10 сек
     * Инициализацию потоков ввода/вывода в кодировке UTF-8
     * Запуск фонового потока {@code readerThread} для асинхронного чтения
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 10000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        running = true;
        readerThread = new Thread(this::readLoop, "race-client-reader");
        readerThread.start();
    }

    /**
     * Отправляет команду движения на сервер.
     */
    public void sendMove(String direction) {
        if (!running) return;
        synchronized (out) { out.println(direction); }
    }

    /**
     * Корректно закрывает соединение и останавливает фоновые процессы.
     * Последовательность действий:
     * Устанавливает {@code running = false} для остановки цикла чтения
     * Прерывает поток {@code readerThread}, если он активен
     * Закрывает сокет (что вызовет исключение в {@code readLoop})
     * Выжидает 50 мс для гарантированной обработки {@code SocketException}
     */
    public void close() {
        running = false;

        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }

        try {
            if (socket != null) {
                socket.close();
                Thread.sleep(50);
            }
        } catch (IOException | InterruptedException ignored) {}
    }

    /**
     * Основной цикл чтения сообщений от сервера.
     * Работает в фоновом потоке и выполняет:
     * Чтение строк до {@code null} или исключения
     * Парсинг каждой строки через {@link #parse(String)}
     * Обработку {@code InterruptedIOException} и {@code SocketException} как штатного закрытия
     * Уведомление слушателя об ошибке только если клиент ещё активен
     */
    private void readLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                parse(line.trim());
            }
        } catch (InterruptedIOException | SocketException e) {
            Logger.debug("[CLIENT] " + LocalizationManager.getInstance().getLocalizedMessage("ConnectClosed"));
        } catch (IOException e) {
            if (running) listener.onDisconnected(e.getMessage());
        } finally {
            running = false;
        }
    }

    /**
     * Парсит текстовую команду и делегирует обработку соответствующему методу {@link Listener}.
     * @param line строка команды от сервера (уже обрезанная от пробелов)
     */
    private void parse(String line) {
        if (line.isEmpty()) return;
        String[] p = line.split("\\s+");
        switch (p[0]) {
            case "INFO":
                listener.onInfo(line.length() > 5 ? line.substring(5) : "");
                break;
            case "START":
                if (p.length >= 11) {
                    listener.onStart(
                            Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                            Integer.parseInt(p[7]), Integer.parseInt(p[8]),
                            Integer.parseInt(p[3]), Integer.parseInt(p[4]),
                            Integer.parseInt(p[5]), Integer.parseInt(p[6]),
                            Integer.parseInt(p[9]), Integer.parseInt(p[10])
                    );
                }
                break;
            case "STATE":
                if (p.length >= 7) {
                    listener.onState(
                            Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                            Integer.parseInt(p[3]), Integer.parseInt(p[4]),
                            Integer.parseInt(p[5]), Integer.parseInt(p[6])
                    );
                }
                break;
            case "WIN":
                if (p.length >= 2) listener.onWin(Integer.parseInt(p[1]));
                break;
            case "WAITING":
                listener.onWaiting(line.length() > 8 ? line.substring(8) : "Waiting");
                break;
            case "CLOSE":
                listener.onCloseRequest();
                break;
        }
    }

}