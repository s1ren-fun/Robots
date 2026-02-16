package log;

/**
 * Запись в логе, содержащая уровень важности и текстовое сообщение.
 */
public class LogEntry {
    private LogLevel logLevel;
    private String strMessage;

    /**
     * Создаёт новую запись лога с указанным уровнем и сообщением.
     */
    public LogEntry(LogLevel logLevel, String strMessage) {
        this.strMessage = strMessage;
        this.logLevel = logLevel;
    }

    /**
     * Возвращает текстовое сообщение записи.
     *
     * @return сообщение лога
     */
    public String getMessage() {
        return strMessage;
    }

    /**
     * Возвращает уровень важности записи.
     *
     * @return уровень логирования
     */
    public LogLevel getLevel() {
        return logLevel;
    }
}

