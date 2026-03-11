package util;

import log.LogEntry;

import java.util.*;


/**
 * Список логов не превышающий заданной длины
 */
public class LogList {
    private final Deque<LogEntry> deque;
    private final int maxCapacity;

    /**
     * Конструктор создающий список с максимальной длиной
     *
     * @param maxCapacity Максимальная длина списка
     */
    public LogList(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        deque = new ArrayDeque<>();
    }

    /**
     * @return Количество логов в списке
     */
    public synchronized int size() {
        return deque.size();
    }

    /**
     * Добавляет элемент в список,
     * при попытке добавить лог с размером больше максимального
     * удалит первый добавленный лог
     *
     * @param logEntry Лог сообщение
     */
    public synchronized void add(LogEntry logEntry) {
        if (deque.size() >= maxCapacity) {
            deque.pollFirst();
        }
        deque.add(logEntry);
    }

    /**
     * @param fromIndex Начало диапазона(включительно)
     * @param toIndex   Конец диапазона(не включительно)
     * @return List содержащий логи c индексами в диапазоне
     */
    public List<LogEntry> subList(int fromIndex, int toIndex) {
        List<LogEntry> subList = new ArrayList<>();
        Iterator<LogEntry> iterator = iterator();
        for (int i = 0; i < toIndex; i++) {
            if (iterator.hasNext()) {
                LogEntry logEntry = iterator.next();
                if (i >= fromIndex) {
                    subList.add(logEntry);
                }
            }
        }
        return subList;
    }

    /**
     * @return Iterator списка логов
     */
    public synchronized Iterator<LogEntry> iterator() {
        return new ArrayList<>(deque).iterator();
    }
}
