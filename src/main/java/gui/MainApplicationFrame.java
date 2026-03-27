package gui;

import game.GameModel;
import localization.LocalizationManager;
import log.Logger;
import state.AppStateManager;
import state.StateSaveEvent;
import state.StateSaveListener;
import state.Stateful;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


/**
 * Главное окно приложения с рабочим столом (JDesktopPane).
 * Содержит меню управления внешним видом и тестовыми командами,
 * а также создаёт начальные внутренние окна: лог и игровое поле.
 */
public class MainApplicationFrame extends JFrame implements Stateful, PropertyChangeListener {
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
        stateManager.register(this);
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width - inset * 2,
                screenSize.height - inset * 2);

        setContentPane(desktopPane);

        stateManager.restoreAll();

        restoreWindows();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        setJMenuBar(createMenuBar());
        addStateSaveListener(stateManager);
        LocalizationManager.getInstance().addPropertyChangeListener(this);
    }

    /**
     * Добавляет слушателя события сохранения состояния.
     *
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
        for (int i = 0; i <= logWindowCounter; i++) {
            LogWindow logWindow = createLogWindow();
            addWindow(logWindow);
            logWindow.setWindowName("log_"+i);
            stateManager.register(logWindow);
        }

        for (int i = 0; i <= gameWindowCounter; i++) {
            GameModel model = new GameModel();
            GameWindow gameWindow = createGameWindow(model);
            addWindow(gameWindow);
            gameWindow.setWindowName("game_"+i);
            stateManager.register(gameWindow);
            CoordinatesWindow coordinatesWindow = createCoordinatesWindow(model);
            addWindow(coordinatesWindow);
            coordinatesWindow.setWindowName("coor_"+i);
            stateManager.register(coordinatesWindow);
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
        Logger.debug(logWindowCounter == 0 ?
                LocalizationManager.getInstance().getLocalizedMessage("LogWindowActivated") :
                LocalizationManager.getInstance().getLocalizedMessage("LogWindowCreate"));
        return logWindow;
    }

    /**
     * Создаёт и настраивает игровое окно.
     *
     * @return новое игровое окно
     */
    protected GameWindow createGameWindow(GameModel model) {
        GameWindow gameWindow = new GameWindow(model);
        gameWindow.setLocation(50 + (gameWindowCounter * 30), 50 + (gameWindowCounter * 30));
        gameWindow.setSize(400, 400);
        Logger.debug(LocalizationManager.getInstance().getLocalizedMessage("GameWindowCreate"));
        return gameWindow;
    }

    protected CoordinatesWindow createCoordinatesWindow(GameModel model) {
        CoordinatesWindow coordinatesWindow = new CoordinatesWindow(model);
        coordinatesWindow.setLocation(50 + (gameWindowCounter * 30), 50 + (gameWindowCounter * 30));
        Logger.debug(LocalizationManager.getInstance().getLocalizedMessage("CoorWindowCreate"));
        return coordinatesWindow;
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
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createLocalizationMenu());
        return menuBar;
    }

    /**
     * создание меню смены языка
     */
    private JMenu createLocalizationMenu() {
        JMenu langChangeMenu = new JMenu(LocalizationManager.getInstance().getLocalizedMessage("LangChange"));


        langChangeMenu.add(createLangItem("ru"));
        langChangeMenu.add(createLangItem("eu"));
        return langChangeMenu;
    }

    private JMenuItem createLangItem(String langAcronym) {
        String langName = LocalizationManager.getInstance().getMessage("LangName", langAcronym);
        JMenuItem langItem = new JMenuItem(
                langName);
        langItem.addActionListener((event) ->
                LocalizationManager.getInstance().changeLanguageTo(langAcronym));

        return langItem;
    }
    /**
     * Создаёт меню "Файл" с пунктом выхода из приложения.
     */
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu(LocalizationManager.getInstance().getLocalizedMessage("File"));
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.getAccessibleContext().setAccessibleDescription(LocalizationManager.getInstance().
                getLocalizedMessage("Operation"));

        JMenuItem gameItem = new JMenuItem(LocalizationManager.getInstance().getLocalizedMessage("NewGameWindow"),
                KeyEvent.VK_G);
        gameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        gameItem.addActionListener(e -> {
            GameModel model = new GameModel();
            GameWindow gameWindow = createGameWindow(model);
            addWindow(gameWindow);
            gameWindowCounter++;
            gameWindow.setWindowName("game_"+gameWindowCounter);
            stateManager.register(gameWindow);
            CoordinatesWindow coordinatesWindow = createCoordinatesWindow(model);
            addWindow(coordinatesWindow);
            coordinatesWindow.setWindowName("coor_"+gameWindowCounter);
            stateManager.register(coordinatesWindow);
        });
        fileMenu.add(gameItem);

        JMenuItem logItem = new JMenuItem(LocalizationManager.getInstance().getLocalizedMessage("NewLogWindow"),
                KeyEvent.VK_L);
        logItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        logItem.addActionListener(e -> {
            LogWindow logWindow = createLogWindow();
            addWindow(logWindow);
            logWindowCounter++;
            logWindow.setWindowName("log_"+gameWindowCounter);
            stateManager.register(logWindow);
        });
        fileMenu.add(logItem);

        JMenuItem exitItem = new JMenuItem(LocalizationManager.getInstance().getLocalizedMessage("ExitMenu"),
                KeyEvent.VK_X);
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
        JMenu lookAndFeelMenu = new JMenu(LocalizationManager.getInstance().getLocalizedMessage("DisplayMode"));
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                LocalizationManager.getInstance().getLocalizedMessage("DisplayModeDescription"));

        JMenuItem systemLookAndFeel = new JMenuItem(LocalizationManager.getInstance().
                getLocalizedMessage("CrossPlatformLookAndFeelItem"),
                KeyEvent.VK_S);
        systemLookAndFeel.addActionListener(e -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            currentLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem(LocalizationManager.getInstance().
                getLocalizedMessage("SystemLookAndFeelItem"),
                KeyEvent.VK_U);
        crossplatformLookAndFeel.addActionListener(e -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            currentLookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);

        JMenuItem nimbusLookAndFeel = new JMenuItem(LocalizationManager.getInstance().
                getLocalizedMessage("DefaultLookAndFeelItem"),
                KeyEvent.VK_N);
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
        LocalizationManager local = LocalizationManager.getInstance();
        Object[] options = {local.getLocalizedMessage("Yes"),local.getLocalizedMessage("No")};
        int result = JOptionPane.showOptionDialog(
                this,
                local.getLocalizedMessage("ExitConfirm"),
                local.getLocalizedMessage("ExitTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (result == JOptionPane.YES_OPTION) {
            fireStateSaveEvent();
            setVisible(false);
            dispose();
            System.exit(0);
        }
    }

    /**
     * Создаёт меню тестовых команд.
     */
    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu(LocalizationManager.getInstance().getLocalizedMessage("TestMenu"));
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(LocalizationManager.getInstance().
                getLocalizedMessage("TestMenuDescription"));

        testMenu.add(createLogMessageItem(LocalizationManager.getInstance().getLocalizedMessage("TestLogMessage1")));
        testMenu.add(createLogMessageItem(LocalizationManager.getInstance().getLocalizedMessage("TestLogMessage2")));
        return testMenu;
    }

    /**
     * создание сообщения в log
     */
    private JMenuItem createLogMessageItem(String text){
        String addedText = LocalizationManager.getInstance()
                .getLocalizedMessage("LogMessagePattern", (Object) text);
        JMenuItem addLogMessageItem = new JMenuItem(text, KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) ->
                Logger.debug(addedText));

        return addLogMessageItem;
    }
    /**
     * Устанавливает схему оформления (Look and Feel) для приложения.
     */
    public void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // just ignore
        }
    }

    public int getLogWindowCounter() { return logWindowCounter; }
    public void setLogWindowCounter(int value) { this.logWindowCounter = value; }

    public int getGameWindowCounter() { return gameWindowCounter; }
    public void setGameWindowCounter(int value) { this.gameWindowCounter = value; }

    public String getCurrentLookAndFeel() { return currentLookAndFeel; }
    public void setCurrentLookAndFeel(String value) { this.currentLookAndFeel = value; }

    @Override
    public String getWindowName() {
        return "main";
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setJMenuBar(createMenuBar());
    }
}
