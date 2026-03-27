package localization;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * отвечает за локализацию
 */
public class LocalizationManager {
    public static final String LANGUAGE_UPDATED = "LANGUAGE_UPDATED";

    private static volatile LocalizationManager INSTANCE;
    private final ConcurrentMap<String, MessageFormat> messageFormatCache;
    private final PropertyChangeSupport propChangeDispatcher =
            new PropertyChangeSupport(this);
    private String language = "ru";

    private LocalizationManager() {
        messageFormatCache = new ConcurrentHashMap<>();
    }

    /**
     * @return экземпляр синглтон LocalizationManager
     */
    public static LocalizationManager getInstance() {
        if (INSTANCE == null) {
            synchronized (LocalizationManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocalizationManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Возвращает локализованное сообщение для указанного ключа и языка,
     * форматируя его с помощью предоставленных аргументов.
     */
    public String getLocalizedMessage(String key, String language, Object... arguments) {
        String pattern = ResourceBundle.getBundle("Localization/messages", Locale.of(language)).getString(key);
        MessageFormat messageFormat = messageFormatCache.computeIfAbsent(pattern, s -> new MessageFormat(pattern));
        return messageFormat.format(arguments);
    }


    /**
     * Возвращает сообщение для указанного ключа, используя текущий язык и
     * форматируя его с помощью предоставленных аргументов
     */
    public String getLocalizedMessage(String key, Object... arguments) {
        return getLocalizedMessage(key, language, arguments);
    }

    /**
     * Возвращает локализованное сообщение для указанного ключа и языка
     */
    public String getMessage(String key, String language) {
        return getLocalizedMessage(key, language);
    }

    /**
     * Возвращает локализованное сообщение для указанного ключа используя текущий язык
     */
    public String getLocalizedMessage(String key) {
        return getLocalizedMessage(key, language);
    }

    /**
     * Изменяет текущий язык и уведомляет зарегистрированных слушателей об этом изменении
     *
     * @param language Язык
     */
    public void changeLanguageTo(String language) {
        this.language = language;
        propChangeDispatcher.firePropertyChange(LANGUAGE_UPDATED, null, null);
    }

    /**
     * Добавляет PropertyChangeListener, который будет получать уведомления при обновлении языка
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeDispatcher.addPropertyChangeListener(LANGUAGE_UPDATED, listener);
    }

    /**
     * Возвращает текущий язык
     */
    public String getLanguage() {
        return language;
    }
}
