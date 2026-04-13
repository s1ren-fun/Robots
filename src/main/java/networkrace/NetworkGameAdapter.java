package networkrace;

import game.GameModel;
import log.Logger;

/**
 * Адаптер между сетевым клиентом ({@link RaceClient}) и локальной игровой логикой.
 * Отвечает за:
 * Преобразование сетевых событий в вызовы локальных методов
 * Синхронизацию состояний (координаты, цель, статус игры) между сетевым потоком и EDT
 * Определение победителя и управление режимами игры/ожидания
 * Отправку команд управления (движение, рестарт, завершение) на сервер
 */
public class NetworkGameAdapter {
    private final GameModel model;
    private RaceClient client;
    private final int playerId;
    private volatile int myX, myY;
    private volatile int opponentX, opponentY;
    private volatile int targetX, targetY;
    private volatile int targetRadius = 14;
    private volatile boolean gameStarted = false;
    private volatile boolean isWaiting = false;
    private volatile int winnerId = -1;
    private volatile int myLastDirection = 0;
    private volatile int opponentLastDirection = 0;
    private volatile boolean closeRequested = false;

    private final Listener networkListener;

    public NetworkGameAdapter(GameModel model, int playerId) {
        this.model = model;
        this.playerId = playerId;
        this.networkListener = new NetworkGameListener(this);
    }

    /**
     * Привязывает экземпляр сетевого клиента к адаптеру.
     *
     */
    public void setClient(RaceClient client) {
        this.client = client;
    }

    /**
     * Возвращает слушателя сетевых событий для передачи в {@link RaceClient}.
     */
    public Listener getNetworkListener() {
        return networkListener;
    }

    /**
     * Проверяет, находится ли сервер в режиме ожидания нового игрока.
     */
    public boolean isWaiting() {
        return isWaiting;
    }

    /**
     *  * Обрабатывает команду запуска или сброса игры от сервера.
     */
    void handleStart(int targetX, int targetY, int p1x, int p1y, int p2x, int p2y, int targetRadius) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetRadius = targetRadius;

        if (playerId == 1) {
            myX = p1x;
            myY = p1y;
            opponentX = p2x;
            opponentY = p2y;
        } else {
            myX = p2x;
            myY = p2y;
            opponentX = p1x;
            opponentY = p1y;
        }

        myLastDirection = 0;
        opponentLastDirection = 0;
        model.setTargetPosition(targetX, targetY);
        gameStarted = true;
        isWaiting = false;
        winnerId = -1;
    }

    /**
     * Обновляет состояние игры при получении новых координат от сервера.
     */
    void handleState(int p1x, int p1y, int p2x, int p2y, int targetX, int targetY) {
        int oldMyX = myX, oldMyY = myY;
        int oldOppX = opponentX, oldOppY = opponentY;

        if (playerId == 1) {
            myX = p1x;
            myY = p1y;
            opponentX = p2x;
            opponentY = p2y;
        } else {
            myX = p2x;
            myY = p2y;
            opponentX = p1x;
            opponentY = p1y;
        }

        this.targetX = targetX;
        this.targetY = targetY;

        if (targetX != model.getTargetX() || targetY != model.getTargetY()) {
            model.setTargetPosition(targetX, targetY);
        }

        myLastDirection = resolveDirection(oldMyX, oldMyY, myX, myY, myLastDirection);
        opponentLastDirection = resolveDirection(oldOppX, oldOppY, opponentX, opponentY, opponentLastDirection);
        checkWinCondition();
    }

    /**
     * Обрабатывает сообщение о победе одного из игроков.
     * @param winnerId
     */
    void handleWin(int winnerId) {
        this.winnerId = winnerId;
        gameStarted = false;
    }

    /**
     * Обрабатывает разрыв сетевого соединения.
     */
    void handleDisconnected(String text) {
        Logger.debug("[DISCONNECTED] " + text);
        gameStarted = false;
        if (winnerId == -1) {
            determineWinnerByPosition();
        }
    }

    /**
     * Переводит адаптер в режим ожидания при отключении второго игрока.
     */
    void handleWaiting(String text) {
        Logger.debug("[WAITING] " + text);
        gameStarted = false;
        isWaiting = true;
        winnerId = -1;
    }

    /**
     * Устанавливает флаг запроса на закрытие всех окон игры.
     */
    void handleCloseRequest() {
        closeRequested = true;
    }

    /**
     * Определяет направление движения на основе изменения координат.
     */
    private int resolveDirection(int oldX, int oldY, int newX, int newY, int previousDirection) {
        if (newX > oldX) return 0;
        if (newX < oldX) return 2;
        if (newY > oldY) return 1;
        if (newY < oldY) return 3;
        return previousDirection;
    }

    /**
     * Проверяет, достиг ли кто-либо из игроков цели в текущем кадре.
     */
    private void checkWinCondition() {
        if (winnerId != -1) return;

        double myDist = distance(myX, myY, targetX, targetY);
        double oppDist = distance(opponentX, opponentY, targetX, targetY);

        if (myDist <= targetRadius) {
            winnerId = playerId;
        } else if (oppDist <= targetRadius) {
            winnerId = (playerId == 1) ? 2 : 1;
        }
    }

    /**
     * Определяет победителя по ближайшему расстоянию к цели при экстренном отключении.
     */
    private void determineWinnerByPosition() {
        double myDist = distance(myX, myY, targetX, targetY);
        double oppDist = distance(opponentX, opponentY, targetX, targetY);

        if (myDist <= targetRadius) {
            winnerId = playerId;
        } else if (oppDist <= targetRadius) {
            winnerId = (playerId == 1) ? 2 : 1;
        } else {
            winnerId = (myDist < oppDist) ? playerId : ((playerId == 1) ? 2 : 1);
        }
    }

    /**
     * Вычисляет евклидово расстояние между двумя точками.
     */
    private double distance(int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Отправляет команду движения на сервер.
     * Игнорируется, если игра не активна, завершена или клиент не подключён.
     */
    public void sendMove(String direction) {
        if (client == null || !gameStarted || winnerId != -1) return;
        client.sendMove(direction);
    }

    /**
     * Отправляет запрос на перезапуск игры после завершения раунда.
     */
    public void sendRestart() {
        if (client != null && winnerId != -1) {
            client.sendMove("RESTART");
        }
    }

    /**
     * Отправляет команду принудительного завершения игры (доступно только хосту).
     */
    public void sendFinish() {
        if (client != null && gameStarted && winnerId == -1) {
            client.sendMove("FINISH");
        }
    }

    /**
     * геттеры
     * */
    public int getMyX() { return myX; }
    public int getMyY() { return myY; }
    public int getOpponentX() { return opponentX; }
    public int getOpponentY() { return opponentY; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public boolean isGameStarted() { return gameStarted; }
    public int getWinnerId() { return winnerId; }
    public int getPlayerId() { return playerId; }
    public boolean isCloseRequested() { return closeRequested; }
    public int getMyLastDirection() { return myLastDirection; }
    public int getOpponentLastDirection() { return opponentLastDirection; }
}