package gui;

import java.util.EventListener;

/**
 * Слушатель события сохранения состояния приложения.
 */
public interface StateSaveListener extends EventListener {

    /**
     * Вызывается при наступлении события сохранения состояния.
     * В этом методе слушатель должен выполнить все необходимые действия по сохранению данных
     * @param event
     */
    void onStateSave(StateSaveEvent event);
}
