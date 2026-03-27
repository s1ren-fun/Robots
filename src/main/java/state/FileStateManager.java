package state;

import localization.LocalizationManager;
import log.Logger;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileStateManager {
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
        } catch (IOException e) {
            Logger.error(LocalizationManager.getInstance().getLocalizedMessage("ErrorSavingState") +
                    e.getMessage());
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
        } catch (IOException e) {
            Logger.error(LocalizationManager.getInstance().getLocalizedMessage("ErrorLoadingState") + e.getMessage());
        }
    }
}
