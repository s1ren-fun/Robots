package log;

/**
 * Перечисление уровней важности записей лога.
 * Уровни упорядочены от наименее важного (Trace) к наиболее критичному (Fatal).
 */
public enum LogLevel {
    Trace(0),
    Debug(1),
    Info(2),
    Warning(3),
    Error(4),
    Fatal(5);

    private final int level;

    /**
     * Создаёт уровень логирования с заданным числовым кодом.
     */
    LogLevel(int iLevel) {
        level = iLevel;
    }

    /**
     * Возвращает числовой код уровня.
     */
    public int level() {
        return level;
    }
}

