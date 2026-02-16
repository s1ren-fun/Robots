package log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Источник данных для окна лога с ограниченным размером очереди.
 * Хранит фиксированное количество последних записей и уведомляет
 * слушателей об изменениях.
 * <p>
 * Что починить:
 * 1. Этот класс порождает утечку ресурсов (связанные слушатели оказываются
 * удерживаемыми в памяти)
 * 2. Этот класс хранит активные сообщения лога, но в такой реализации он
 * их лишь накапливает. Надо же, чтобы количество сообщений в логе было ограничено
 * величиной m_iQueueLength (т.е. реально нужна очередь сообщений
 * ограниченного размера)
 */
public class LogWindowSource {
    private int queueLength;

    private ArrayList<LogEntry> messages;
    private final ArrayList<LogChangeListener> listeners;
    private volatile LogChangeListener[] activeListeners;

    /**
     * Создаёт источник лога с заданной максимальной длиной очереди.
     */
    public LogWindowSource(int iQueueLength) {
        queueLength = iQueueLength;
        messages = new ArrayList<>(iQueueLength);
        listeners = new ArrayList<>();
    }

    /**
     * Регистрирует нового слушателя изменений лога.
     */
    public void registerListener(LogChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
            activeListeners = null;
        }
    }

    /**
     * Отменяет регистрацию слушателя изменений лога.
     */
    public void unregisterListener(LogChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            activeListeners = null;
        }
    }

    /**
     * Добавляет новую запись в лог.
     */
    public void append(LogLevel logLevel, String strMessage) {
        LogEntry entry = new LogEntry(logLevel, strMessage);
        messages.add(entry);
        LogChangeListener[] activeListeners = this.activeListeners;
        if (activeListeners == null) {
            synchronized (listeners) {
                if (this.activeListeners == null) {
                    activeListeners = listeners.toArray(new LogChangeListener[0]);
                    this.activeListeners = activeListeners;
                }
            }
        }
        for (LogChangeListener listener : activeListeners) {
            listener.onLogChanged();
        }
    }

    /**
     * Возвращает текущее количество записей в логе.
     */
    public int size() {
        return messages.size();
    }

    /**
     * Возвращает текущее количество записей в логе.
     */
    public Iterable<LogEntry> range(int startFrom, int count) {
        if (startFrom < 0 || startFrom >= messages.size()) {
            return Collections.emptyList();
        }
        int indexTo = Math.min(startFrom + count, messages.size());
        return messages.subList(startFrom, indexTo);
    }

    /**
     * Возвращает все записи лога.
     */
    public Iterable<LogEntry> all() {
        return messages;
    }
}
