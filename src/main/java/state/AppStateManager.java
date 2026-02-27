package state;

import log.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Менеджер состояний приложения.
 * Управляет глобальным словарем состояний, сохранением в файл и регистрацией компонентов.
 */
public class AppStateManager implements StateSaveListener {
    private final Map<String, String> globalState = new HashMap<>();
    private final List<StatefulComponent> registeredComponents = new CopyOnWriteArrayList<>();
    private final String configPath;

    /**
     * Внутренний класс-обёртка для зарегистрированного компонента.
     * Хранит ссылку на компонент и его префикс в глобальном словаре.
     */
    private class StatefulComponent {
        Stateful component;
        String prefix;

        StatefulComponent(Stateful component, String prefix) {
            this.component = component;
            this.prefix = prefix;
        }
    }

    /**
     * Создаёт новый экземпляр менеджера состояний.
     */
    public AppStateManager() {
        String home = System.getProperty("user.home");
        this.configPath = home + File.separator + "Dudin" + File.separator + "state.cfg";
        loadFromFile();
    }

    /**
     *  Регистрирует компонент для участия в сохранении состояния.
     * @param component
     * @param prefix
     */
    public void register(Stateful component, String prefix) {
        registeredComponents.add(new StatefulComponent(component, prefix));
    }

    /**
     * Восстанавливает состояние всех зарегистрированных компонентов.
     */
    public void restoreAll() {
        for (StatefulComponent sc : registeredComponents) {
            PrefixedMapView view = new PrefixedMapView(globalState, sc.prefix);
            sc.component.loadState(view);
        }
    }

    /**
     * Сохраняет состояние всех зарегистрированных компонентов.
     */
    public void saveAll() {
        for (StatefulComponent sc : registeredComponents) {
            PrefixedMapView view = new PrefixedMapView(globalState, sc.prefix);
            sc.component.saveState(view);
        }
        saveToFile();
    }

    /**
     *  Сохраняет глобальный словарь состояний в файл конфигурации.
     */
    private void saveToFile() {
        try {
            File file = new File(configPath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Properties props = new Properties();
            props.putAll(globalState);
            try (FileOutputStream out = new FileOutputStream(file)) {
                props.store(out, "Application State");
            }
            Logger.debug("Состояние сохранено в " + configPath);
        } catch (IOException e) {
            Logger.error("Ошибка сохранения состояния: " + e.getMessage());
        }
    }

    /**
     * Загружает состояние из файла конфигурации при инициализации.
     */
    private void loadFromFile() {
        try {
            File file = new File(configPath);
            if (file.exists()) {
                Properties props = new Properties();
                try (FileInputStream in = new FileInputStream(file)) {
                    props.load(in);
                }
                for (String key : props.stringPropertyNames()) {
                    globalState.put(key, props.getProperty(key));
                }
                Logger.debug("Состояние загружено из " + configPath);
            }
        } catch (IOException e) {
            Logger.error("Ошибка загрузки состояния: " + e.getMessage());
        }
    }

    @Override
    public void onStateSave(StateSaveEvent event) {
        saveAll();
    }
}