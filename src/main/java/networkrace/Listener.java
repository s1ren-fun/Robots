package networkrace;

/**
 * Интерфейс обратного вызова (callback) для обработки событий сетевой игры.
 */
public interface Listener {

    /**
     * Вызывается при старте новой игровой сессии.
     * Передаёт начальные параметры поля, позиции игроков, координаты цели и игровые настройки.
     */
    void onStart(int fieldWidth, int fieldHeight, int targetX, int targetY,
                 int p1x, int p1y, int p2x, int p2y, int targetRadius, int step);

    /**
     * Вызывается при каждом обновлении состояния игры.
     * Передаёт текущие координаты обоих игроков и цели для перерисовки поля.
     */
    void onState(int p1x, int p1y, int p2x, int p2y, int targetX, int targetY);

    /**
     * Вызывается при определении победителя в раунде.
     * @param winnerId
     */
    void onWin(int winnerId);

    /**
     * Вызывается при получении информационного сообщения от сервера.
     * @param text
     */
    void onInfo(String text);

    /**
     * Вызывается при разрыве сетевого соединения с сервером.
     * @param text
     */
    void onDisconnected(String text);

    /**
     * Вызывается при переходе сервера в режим ожидания нового игрока.
     * @param text
     */
    void onWaiting(String text);

    /**
     * Вызывается при получении команды от сервера на закрытие всех окон игры.
     */
    void onCloseRequest();
}
