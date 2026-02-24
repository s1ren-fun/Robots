package gui;

import java.util.Map;

/**
 * интерфейс для объектов, котрые сохраняют и востанавливают свое состояние
 */
public interface Stateful {
    /**
     * сохраняет состояние объекта
     * @param state словарь для записи
     */
    void saveState(Map<String,String> state);

    /**
     * востонавливает состояние объекта
     * @param state
     */
    void loadState(Map<String,String> state);
}
