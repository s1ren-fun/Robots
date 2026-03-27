package state;


import gui.MainApplicationFrame;
import localization.LocalizationManager;
import log.Logger;

import javax.swing.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Менеджер состояний приложения.
 * Управляет глобальным словарем состояний, регистрацией компонентов.
 */
public class AppStateManager implements StateSaveListener {
    private final FileStateManager storage;
    private final Map<String, String> globalState;
    private final List<Stateful> components = new CopyOnWriteArrayList<>();
    private final String home = System.getProperty("user.home");
    private final String configPath = home + File.separator + "Dudin" + File.separator + "state.cfg";
    /**
     * Создаёт новый экземпляр менеджера состояний.
     */
    public AppStateManager() {
        this.storage = new FileStateManager(configPath);
        this.globalState = storage.getState();
    }

    /**
     * Регистрирует компонент для участия в сохранении состояния.
     */
    public void register(Stateful component) {
        components.add(component);
    }

    /**
     * Восстанавливает состояние всех зарегистрированных компонентов.
     */
    public void restoreAll() {
        for (Stateful component : components) {
            String prefix = component.getWindowName();
            PrefixedMapView  view = new PrefixedMapView(globalState,prefix);

            if (prefix.equals("main") && component instanceof JFrame) {
                JFrame frame = (JFrame) component;
                try {
                    int x = parseInt(view.get("x"), frame.getX());
                    int y = parseInt(view.get("y"), frame.getY());
                    int w = parseInt(view.get("width"), frame.getWidth());
                    int h = parseInt(view.get("height"), frame.getHeight());
                    int ext = parseInt(view.get("extendedState"), frame.getExtendedState());

                    frame.setBounds(x, y, w, h);
                    frame.setExtendedState(ext);

                    if (component instanceof MainApplicationFrame) {
                        MainApplicationFrame main = (MainApplicationFrame) component;
                        main.setLogWindowCounter(parseInt(view.get("logWindowCounter"), 0));
                        main.setGameWindowCounter(parseInt(view.get("gameWindowCounter"), 0));
                        String laf = view.get("lookAndFeel");
                        if (laf != null) {
                            main.setCurrentLookAndFeel(laf);
                            main.setLookAndFeel(laf);
                        }
                        String savedLanguage = view.get("language");
                        if (savedLanguage != null && !savedLanguage.isEmpty()) {
                            LocalizationManager.getInstance().changeLanguageTo(savedLanguage);
                        }
                    }

                } catch (NumberFormatException e) {
                    log.Logger.error("Ошибка восстановления главного окна: " + e.getMessage());
                }

            } else if (component instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) component;
                try {
                    int x = parseInt(view.get("x"), frame.getX());
                    int y = parseInt(view.get("y"), frame.getY());
                    int w = parseInt(view.get("width"), frame.getWidth());
                    int h = parseInt(view.get("height"), frame.getHeight());
                    boolean isIcon = parseBool(view.get("isIcon"), false);
                    boolean isMaximum = parseBool(view.get("isMaximum"), false);
                    boolean isClosed = parseBool(view.get("isClosed"), false);

                    frame.setBounds(x, y, w, h);
                    frame.setIcon(isIcon);

                    if (isMaximum) frame.setMaximum(true);
                    if (isClosed) frame.setClosed(true);

                } catch (PropertyVetoException e) {
                    log.Logger.error(LocalizationManager.getInstance().
                            getLocalizedMessage("ErrorLoadingState")+
                            prefix + ": " + e.getMessage());
                }
            }
        }
    }


    /**
     * Сохраняет состояние всех зарегистрированных компонентов и пишет в файл.
     */
    public void saveAll() {
        for (Stateful component : components) {
            String prefix = component.getWindowName();
            PrefixedMapView  view = new PrefixedMapView(globalState,prefix);

            if (prefix.equals("main") && component instanceof JFrame) {
                JFrame frame = (JFrame) component;
                view.put("x", String.valueOf(frame.getX()));
                view.put("y", String.valueOf(frame.getY()));
                view.put("width", String.valueOf(frame.getWidth()));
                view.put("height", String.valueOf(frame.getHeight()));
                view.put("extendedState", String.valueOf(frame.getExtendedState()));

                if (component instanceof MainApplicationFrame) {
                    MainApplicationFrame main = (MainApplicationFrame) component;
                    view.put("logWindowCounter", String.valueOf(main.getLogWindowCounter()));
                    view.put("gameWindowCounter", String.valueOf(main.getGameWindowCounter()));
                    view.put("language", LocalizationManager.getInstance().getLanguage());
                    view.put("lookAndFeel", main.getCurrentLookAndFeel());
                }

            } else if (component instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) component;
                view.put("x", String.valueOf(frame.getX()));
                view.put("y", String.valueOf(frame.getY()));
                view.put("width", String.valueOf(frame.getWidth()));
                view.put("height", String.valueOf(frame.getHeight()));
                view.put("isIcon", String.valueOf(frame.isIcon()));
                view.put("isMaximum", String.valueOf(frame.isMaximum()));
                view.put("isClosed", String.valueOf(frame.isClosed()));
            }
        }
        storage.save();
    }

    private int parseInt(String value, int defaultValue) {
        try { return value != null ? Integer.parseInt(value) : defaultValue; }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private boolean parseBool(String value, boolean defaultValue) {
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    @Override
    public void onStateSave(StateSaveEvent event) {
        saveAll();
    }
}