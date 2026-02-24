package gui;

import java.util.EventObject;

/**
 * Событие сохранения состояния приложения.
 */
public class StateSaveEvent extends EventObject {
    public StateSaveEvent(Object source) {
        super(source);
    }
}
