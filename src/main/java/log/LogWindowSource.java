package log;

import util.WeakArrayList;

import java.util.Collections;
import java.util.List;

/**
 * Источник данных для окна лога с ограниченным размером очереди.
 * Хранит фиксированное количество последних записей и уведомляет
 * слушателей об изменениях.
 */
public class LogWindowSource {

   private final LogList messages;
   private final List<LogChangeListener> listeners;
   private volatile List<LogChangeListener> activeListeners;


    /**
     * Создаёт источник лога с заданной максимальной длиной очереди.
     */
    public LogWindowSource(int iQueueLength) {
        messages = new LogList(iQueueLength);
        listeners = new WeakArrayList<>();
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
        List<LogChangeListener> activeListeners = this.activeListeners;
        if(activeListeners == null){
            synchronized (listeners){
                if(this.activeListeners == null){
                    activeListeners = new WeakArrayList<>(listeners);
                    this.activeListeners = activeListeners;
                }
            }
        }
        assert activeListeners != null;
        for (LogChangeListener listener : activeListeners){
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
        return messages::iterator;
    }
}
