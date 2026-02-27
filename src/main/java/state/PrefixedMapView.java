package state;

import java.util.*;

/**
 * Представление карты с префиксом для ключей.
 * Реализует шаблон "Фильтр" на основе {@link AbstractMap} и {@link AbstractSet}.
 */
public class PrefixedMapView extends AbstractMap<String,String> {
    private final Map<String, String> backingMap;
    private String prefix;

    /**
     * Создаёт представление карты с указанным префиксом.
     * @param backingMap
     * @param prefix
     */
    public PrefixedMapView(Map<String, String> backingMap, String prefix) {
        this.backingMap = backingMap;
        this.prefix = prefix.isEmpty() ? "" : prefix + ".";
    }

    /**
     * Добавляет префикс к ключу для хранения в глобальном словаре.
     * @param key
     * @return
     */
    private String keyWithPrefix(String key){
        return prefix +key;
    }

    /**
     *  Удаляет префикс из ключа при чтении из глобального словаря.
     * @param key
     * @return
     */
    private String keyWithoutPrefix(String key){
        if(key.startsWith(prefix)){
            return key.substring(prefix.length());
        }
        return key;
    }

    @Override
    public String put(String key, String value) {
        return backingMap.put(keyWithPrefix(key),value);
    }

    @Override
    public String get(Object key){
        return backingMap.get(keyWithPrefix((String) key));
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return new AbstractSet<Entry<String, String>>() {
            @Override
            public Iterator<Entry<String, String>> iterator() {
                return new Iterator<Entry<String, String>>() {
                    private final Iterator<Entry<String, String>> backingIterator = backingMap.entrySet().iterator();
                    private Entry<String, String> nextEntry = null;

                    @Override
                    public boolean hasNext() {
                        while (nextEntry == null && backingIterator.hasNext()) {
                            Entry<String, String> entry = backingIterator.next();
                            if (entry.getKey().startsWith(prefix)) {
                                nextEntry = new SimpleEntry<>(keyWithoutPrefix(entry.getKey()), entry.getValue());
                            }
                        }
                        return nextEntry != null;
                    }

                    @Override
                    public Entry<String, String> next() {
                        if (!hasNext()) return null;
                        Entry<String, String> result = nextEntry;
                        nextEntry = null;
                        return result;
                    }
                };
            }

            @Override
            public int size() {
                int count = 0;
                for (String key : backingMap.keySet()) {
                    if (key.startsWith(prefix)) count++;
                }
                return count;
            }
        };
    }
}
