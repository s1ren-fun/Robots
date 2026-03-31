package state;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileStateManager {
    private final Logger LOGGER = java.util.logging.Logger.getLogger(FileStateManager.class.getName());
    private final String configPath;
    private final Map<String, String> state = new HashMap<>();

    public FileStateManager(String configPath) {
        this.configPath = configPath;
        loadFromFile();
    }

    public Map<String, String> getState(){
        return state;
    }

    /**
     * Сохраняет параметры в state.cfg в домашнем каталоге пользователя
     */
    public void save(){
        try {
            File file = new File(configPath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Properties props = new Properties();
            props.putAll(state);
            try (FileOutputStream out = new FileOutputStream(file)) {
                props.store(out, "Application State");
            }
            LOGGER.info("Cвойства успешно сохранены");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Ошибка сохранения состояния", e);
        }
    }

    /**
     * Считывает параметры из конфига и инициализирует свойства
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
                    state.put(key, props.getProperty(key));
                }

            }
            LOGGER.info("Состояние загружено");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки состояния", e);        }
    }
}
