package gui;

import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Главное окно приложения с рабочим столом (JDesktopPane).
 * Содержит меню управления внешним видом и тестовыми командами,
 * а также создаёт начальные внутренние окна: лог и игровое поле.
 */
public class MainApplicationFrame extends JFrame implements Stateful{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final AppStateManager stateManager;
    private final List<StateSaveListener> stateSaveListener = new ArrayList<StateSaveListener>();
    private int logWindowCounter = 0;
    private int gameWindowCounter = 0;
    private String currentLookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

    /**
     * Создаёт главное окно приложения, размещённое по центру экрана
     * с отступом 50 пикселей от краёв. Инициализирует рабочий стол,
     * создаёт окна лога и игрового поля, настраивает меню и поведение при закрытии.
     */
    public MainApplicationFrame() {
        stateManager = new AppStateManager();
        stateManager.register(this, "main");

        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width - inset * 2,
                screenSize.height - inset * 2);

        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);
        stateManager.register(logWindow, "log_0");

        GameWindow gameWindow = createGameWindow();
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);
        stateManager.register(gameWindow, "game_0");

        stateManager.restoreAll();

        restoreWindows();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        setJMenuBar(generateMenuBar());
        addStateSaveListener(stateManager);
    }

    /**
     * Добавляет слушателя события сохранения состояния.
     * @param listener
     */
    public void addStateSaveListener(StateSaveListener listener) {
        stateSaveListener.add(listener);
    }

    /**
     * Генерирует и рассылает событие сохранения состояния всем слушателям.
     */
    private void fireStateSaveEvent() {
        StateSaveEvent event = new StateSaveEvent(this);
        for (StateSaveListener listener : stateSaveListener) {
            listener.onStateSave(event);
        }
    }

    /**
     * Восстанавливает все окна лога и игровые окна на основе сохранённых счётчиков.
     */
    private void restoreWindows() {
        for (int i = 1; i <= logWindowCounter; i++) {
            LogWindow logWindow = createLogWindow();
            addWindow(logWindow);
            stateManager.register(logWindow, "log_" + i);
        }

        for (int i = 1; i <= gameWindowCounter; i++) {
            GameWindow gameWindow = createGameWindow();
            addWindow(gameWindow);
            stateManager.register(gameWindow, "game_" + i);
        }

        stateManager.restoreAll();
    }

    /**
     * Создаёт и настраивает окно лога.
     */
    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(log.Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug(logWindowCounter==0?"Протокол работает":"Создано новое окно лого");
        return logWindow;
    }

    /**
     * Создаёт и настраивает игровое окно.
     *
     * @return новое игровое окно
     */
    protected GameWindow createGameWindow() {
        GameWindow gameWindow = new GameWindow();
        gameWindow.setLocation(50 + (gameWindowCounter * 30), 50 + (gameWindowCounter * 30));
        gameWindow.setSize(400, 400);
        Logger.debug("Создано новое игровое окно");
        return gameWindow;
    }

    /**
     * Добавляет внутреннее окно на рабочий стол и делает его видимым.
     */
    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    /**
     * Генерирует главное меню приложения через делегирование вспомогательным методам.
     */
    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        return menuBar;
    }

    /**
     * Создаёт меню "Файл" с пунктом выхода из приложения.
     */
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.getAccessibleContext().setAccessibleDescription("Операции с приложением");

        JMenuItem gameItem = new JMenuItem("Новое игровое окно", KeyEvent.VK_G);
        gameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        gameItem.addActionListener(e -> {
            GameWindow gameWindow = createGameWindow();
            addWindow(gameWindow);
            gameWindowCounter++;
            stateManager.register(gameWindow,"game_" + gameWindowCounter);
        });
        fileMenu.add(gameItem);

        JMenuItem logItem = new JMenuItem("Новое окно лога", KeyEvent.VK_L);
        logItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        logItem.addActionListener(e -> {
            LogWindow logWindow = createLogWindow();
            addWindow(logWindow);
            logWindowCounter++;
            stateManager.register(logWindow,"log_" + logWindowCounter);
        });
        fileMenu.add(logItem);

        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                    new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        fileMenu.add(exitItem);
        return fileMenu;
    }

    /**
     * Создаёт меню выбора схемы оформления.
     */
    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener(e -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            currentLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_U);
        crossplatformLookAndFeel.addActionListener(e -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            currentLookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);

        JMenuItem nimbusLookAndFeel = new JMenuItem("Обычная схема", KeyEvent.VK_N);
        nimbusLookAndFeel.addActionListener(e -> {
            setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            currentLookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
            invalidate();
        });
        lookAndFeelMenu.add(nimbusLookAndFeel);

        return lookAndFeelMenu;
    }

    /**
     * Отображает диалог подтверждения выхода с русскоязычными кнопками.
     * Завершает приложение при подтверждении.
     */
    private void confirmExit() {
        Object[] options = {"Да", "Нет"};
        int result = JOptionPane.showOptionDialog(
                this,
                "Вы действительно хотите выйти из приложения?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (result == JOptionPane.YES_OPTION) {
            Logger.debug("Пользователь подтвердил выход из приложения");
            fireStateSaveEvent();
            System.exit(0);
        }
    }

    /**
     * Создаёт меню тестовых команд.
     */
    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_L);
        addLogMessageItem.addActionListener(e -> Logger.debug("Новая строка"));
        testMenu.add(addLogMessageItem);

        return testMenu;
    }

    /**
     * Устанавливает схему оформления (Look and Feel) для приложения.
     */
    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // just ignore
        }
    }

    @Override
    public void saveState(Map<String, String> state) {
        state.put("x",String.valueOf(getX()));
        state.put("y",String.valueOf(getY()));
        state.put("width",String.valueOf(getWidth()));
        state.put("height",String.valueOf(getHeight()));
        state.put("extendedState",String.valueOf(getExtendedState()));
        state.put("logWindowCounter", String.valueOf(logWindowCounter));
        state.put("gameWindowCounter", String.valueOf(gameWindowCounter));
        state.put("lookAndFeel", currentLookAndFeel);
    }

    @Override
    public void loadState(Map<String, String> state) {
        try {
            int x = Integer.parseInt(state.getOrDefault("x", String.valueOf(getX())));
            int y = Integer.parseInt(state.getOrDefault("y", String.valueOf(getY())));
            int width = Integer.parseInt(state.getOrDefault("width", String.valueOf(getWidth())));
            int height = Integer.parseInt(state.getOrDefault("height", String.valueOf(getHeight())));
            int extendedState = Integer.parseInt(state.getOrDefault("extendedState", String.valueOf(getExtendedState())));
            logWindowCounter = Integer.parseInt(state.getOrDefault("logWindowCounter", "0"));
            gameWindowCounter = Integer.parseInt(state.getOrDefault("gameWindowCounter", "0"));
            String savedLookAndFeel = state.getOrDefault("lookAndFeel",
                    "javax.swing.plaf.nimbus.NimbusLookAndFeel");

            setBounds(x, y, width, height);
            setExtendedState(extendedState);
            setLookAndFeel(savedLookAndFeel);
            currentLookAndFeel = savedLookAndFeel;
        } catch (NumberFormatException e) {
            log.Logger.error("Ошибка восстановления состояния главного окна: " + e.getMessage());
        }
    }
}
