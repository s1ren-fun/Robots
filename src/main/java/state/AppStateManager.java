package state;


import gui.MainApplicationFrame;

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

    /**
     * Создаёт новый экземпляр менеджера состояний.
     */
    public AppStateManager() {
        String home = System.getProperty("user.home");
        String configPath = home + File.separator + "Dudin" + File.separator + "state.cfg";

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
                    int x = parseInt(view.get(prefix + ".x"), frame.getX());
                    int y = parseInt(view.get(prefix + ".y"), frame.getY());
                    int w = parseInt(view.get(prefix + ".width"), frame.getWidth());
                    int h = parseInt(view.get(prefix + ".height"), frame.getHeight());
                    int ext = parseInt(view.get(prefix + ".extendedState"), frame.getExtendedState());

                    frame.setBounds(x, y, w, h);
                    frame.setExtendedState(ext);

                    if (component instanceof MainApplicationFrame) {
                        MainApplicationFrame main = (MainApplicationFrame) component;
                        main.setLogWindowCounter(parseInt(view.get(prefix + ".logWindowCounter"), 0));
                        main.setGameWindowCounter(parseInt(view.get(prefix + ".gameWindowCounter"), 0));
                        String laf = view.get(prefix + ".lookAndFeel");
                        if (laf != null) {
                            main.setCurrentLookAndFeel(laf);
                            main.setLookAndFeel(laf);
                        }
                    }

                } catch (NumberFormatException e) {
                    log.Logger.error("Ошибка восстановления главного окна: " + e.getMessage());
                }

            } else if (component instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) component;
                try {
                    int x = parseInt(view.get(prefix + ".x"), frame.getX());
                    int y = parseInt(view.get(prefix + ".y"), frame.getY());
                    int w = parseInt(view.get(prefix + ".width"), frame.getWidth());
                    int h = parseInt(view.get(prefix + ".height"), frame.getHeight());
                    boolean isIcon = parseBool(view.get(prefix + ".isIcon"), false);
                    boolean isMaximum = parseBool(view.get(prefix + ".isMaximum"), false);
                    boolean isClosed = parseBool(view.get(prefix + ".isClosed"), false);

                    frame.setBounds(x, y, w, h);
                    frame.setIcon(isIcon);

                    if (isMaximum) frame.setMaximum(true);
                    if (isClosed) frame.setClosed(true);

                } catch (PropertyVetoException e) {
                    log.Logger.error("Ошибка восстановления окна " + prefix + ": " + e.getMessage());
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
                view.put(prefix + ".x", String.valueOf(frame.getX()));
                view.put(prefix + ".y", String.valueOf(frame.getY()));
                view.put(prefix + ".width", String.valueOf(frame.getWidth()));
                view.put(prefix + ".height", String.valueOf(frame.getHeight()));
                view.put(prefix + ".extendedState", String.valueOf(frame.getExtendedState()));

                if (component instanceof MainApplicationFrame) {
                    MainApplicationFrame main = (MainApplicationFrame) component;
                    view.put(prefix + ".logWindowCounter", String.valueOf(main.getLogWindowCounter()));
                    view.put(prefix + ".gameWindowCounter", String.valueOf(main.getGameWindowCounter()));
                    view.put(prefix + ".lookAndFeel", main.getCurrentLookAndFeel());
                }

            } else if (component instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) component;
                view.put(prefix + ".x", String.valueOf(frame.getX()));
                view.put(prefix + ".y", String.valueOf(frame.getY()));
                view.put(prefix + ".width", String.valueOf(frame.getWidth()));
                view.put(prefix + ".height", String.valueOf(frame.getHeight()));
                view.put(prefix + ".isIcon", String.valueOf(frame.isIcon()));
                view.put(prefix + ".isMaximum", String.valueOf(frame.isMaximum()));
                view.put(prefix + ".isClosed", String.valueOf(frame.isClosed()));
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