package log;

/**
 * Утилитный класс для логирования сообщений.
 */
public final class Logger {
    private static final LogWindowSource DEFAULT_LOG_SOURCE =
            new LogWindowSource(5);

    /**
     * Приватный конструктор для предотвращения создания экземпляров
     */
    private Logger() {
    }

    /**
     * Записывает отладочное сообщение в лог по умолчанию.
     */
    public static void debug(String strMessage) {
        DEFAULT_LOG_SOURCE.append(LogLevel.Debug, strMessage);
    }

    /**
     * Записывает сообщение об ошибке в лог по умолчанию.
     */
    public static void error(String strMessage) {
        DEFAULT_LOG_SOURCE.append(LogLevel.Error, strMessage);
    }

    /**
     * Возвращает источник лога по умолчанию для прямого доступа.
     */
    public static LogWindowSource getDefaultLogSource() {
        return DEFAULT_LOG_SOURCE;
    }
}
