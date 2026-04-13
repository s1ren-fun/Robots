package gui;

import game.GameModel;
import Localization.LocalizationManager;
import networkrace.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;

/**
 * Основное окно для сетевой игры «Гонки».
 * <p>
 * Интегрирует клиент/сервер, адаптер, контроллер и визуализатор.
 * Поддерживает режимы хоста и клиента, отображает статус игры,
 * обрабатывает команды завершения и перезапуска, а также реагирует
 * на смену языка интерфейса.
 */
public class NetworkGameWindow extends JFrame implements PropertyChangeListener {
    private LocalizationManager local = LocalizationManager.getInstance();
    private final NetworkGameVisualizer visualizer;
    private final NetworkGameController controller;
    private final NetworkGameAdapter adapter;
    private RaceClient client;
    private RaceServer server;
    private final boolean isHost;
    private final JButton restartButton;
    private final JButton finishButton;
    private final JLabel statusLabel;
    private Timer uiUpdater;
    private volatile boolean closing = false;

    /**
     * Создаёт и инициализирует окно сетевой игры.
     */
    public NetworkGameWindow(boolean isHost, String serverHost) {
        super(LocalizationManager.getInstance().getLocalizedMessage("NetworkGameWindowTitle") +
                (isHost ? LocalizationManager.getInstance().getLocalizedMessage("Host") :
                        LocalizationManager.getInstance().getLocalizedMessage("Client")));
        this.isHost = isHost;

        statusLabel = new JLabel(local.getLocalizedMessage("WaitingConnect"), SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setBackground(new Color(240, 240, 240));
        statusLabel.setOpaque(true);

        GameModel model = new GameModel();
        adapter = new NetworkGameAdapter(model, isHost ? 1 : 2);
        client = new RaceClient(adapter.getNetworkListener());
        adapter.setClient(client);

        if (isHost) {
            server = new RaceServer(RaceServer.PORT);
            try {
                server.start();
                String ip = getHostIP();
                JOptionPane.showMessageDialog(this, local.getLocalizedMessage("IPHost") +
                        ip, local.getLocalizedMessage("IPTitle"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                showError(local.getLocalizedMessage("FailedServer") + e.getMessage());
            }
        }

        String host = isHost ? "localhost" : serverHost;
        Runnable connectTask = () -> {
            try {
                client.connect(host, RaceServer.PORT);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, local.getLocalizedMessage("NoConnect") +
                        host, local.getLocalizedMessage("Error"), JOptionPane.ERROR_MESSAGE);
                throw new IllegalStateException("Connection failed", e);
            }
        };
        if (isHost) SwingUtilities.invokeLater(connectTask);
        else connectTask.run();

        controller = new NetworkGameController(adapter);
        visualizer = new NetworkGameVisualizer(adapter);
        controller.attachTo(visualizer);

        restartButton = new JButton(local.getLocalizedMessage("Restart"));
        restartButton.setEnabled(false);
        restartButton.addActionListener(e -> adapter.sendRestart());

        finishButton = new JButton(local.getLocalizedMessage("FinishGame"));
        finishButton.setEnabled(false);
        finishButton.addActionListener(e -> {
            adapter.sendFinish();
            closeAllWindows();
        });

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.add(statusLabel, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(finishButton);
        btnPanel.add(restartButton);
        bottom.add(btnPanel, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(visualizer, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (closing) return;

                if (isHost) {
                    adapter.sendFinish();
                    closeAllWindows();
                }

                closeAllWindows();
            }
        });

        startUiUpdater();
        SwingUtilities.invokeLater(visualizer::requestFocusInWindow);
        LocalizationManager.getInstance().addPropertyChangeListener(this);
    }

    /**
     * Запускает таймер периодического обновления состояния UI.
     * Таймер срабатывает каждые 30 мс и вызывает {@link #updateUIState()}.
     */
    private void startUiUpdater() {
        uiUpdater = new Timer(30, e -> updateUIState());
        uiUpdater.start();
    }

    /**
     * Обновляет состояние элементов интерфейса в соответствии с текущим состоянием игры.
     */
    private void updateUIState() {
        if (closing) return;

        boolean finished = adapter.getWinnerId() != -1;
        boolean waiting = adapter.isWaiting();
        boolean started = adapter.isGameStarted();
        boolean closeRequested = adapter.isCloseRequested();

        if (closeRequested) {
            closeAllWindows();
            return;
        }

        controller.setEnabled(started && !finished && !waiting);

        restartButton.setEnabled(finished && !waiting);
        if (isHost) {
            finishButton.setEnabled(started && !finished && !waiting);
        } else {
            finishButton.setVisible(false);
        }

        if (waiting) {
            statusLabel.setText(local.getLocalizedMessage("PlayerDisStatus"));
        } else if (!started) {
            statusLabel.setText(local.getLocalizedMessage("WaitConnectSecPlayer"));
        } else if (finished) {
            statusLabel.setText(local.getLocalizedMessage("GameFinish"));
        } else {
            statusLabel.setText(local.getLocalizedMessage("GameStart") + " (" + adapter.getTargetX() + ", " +
                    adapter.getTargetY() + ")");
        }

        visualizer.repaintGame();
    }

    /**
     * Корректно закрывает текущее окно и все остальные окна NetworkGameWindow
     */
    private void closeAllWindows() {
        if (closing) return;
        closing = true;

        if (uiUpdater != null) uiUpdater.stop();

        SwingUtilities.invokeLater(() -> {
            dispose();

            for (Window w : Window.getWindows()) {
                if (w instanceof NetworkGameWindow && w != this && w.isVisible()) {
                    NetworkGameWindow other = (NetworkGameWindow) w;
                    other.closing = true;
                    w.dispose();
                }
            }
        });

        new Thread(this::cleanup, "cleanup-thread").start();
    }

    /**
     * Показывает модальное диалоговое окно с сообщением об ошибке.
     *
     * @param message текст сообщения об ошибке
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, local.getLocalizedMessage("Error"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Освобождает сетевые ресурсы и отключает ввод.
     * <p>
     * Метод безопасно закрывает клиент и сервер (если они инициализированы),
     * отключает контроллер ввода и останавливает таймер.
     * Все операции обернуты в {@code try-catch} для предотвращения прерывания
     * процесса закрытия при возникновении исключений.
     */
    private void cleanup() {
        try {
            if (client != null) client.close();
        } catch (Exception ignored) {
        }

        try {
            if (server != null) server.stop();
        } catch (Exception ignored) {
        }

        controller.setEnabled(false);

        if (uiUpdater != null && uiUpdater.isRunning()) {
            uiUpdater.stop();
        }
    }

    /**
     * Запускает окно сетевой игры в режиме хоста.
     */
    public static void startAsHost() {
        SwingUtilities.invokeLater(() -> new NetworkGameWindow(true, null).setVisible(true));
    }

    /**
     * Запускает окно сетевой игры в режиме клиента.
     */
    public static void startAsClient(String host) {
        SwingUtilities.invokeLater(() -> new NetworkGameWindow(false, host).setVisible(true));
    }


    /**
     * Возвращает локальный IP-адрес хоста.
     *
     * @return строковое представление IP-адреса, или сообщение об ошибке, если адрес не удалось определить
     */
    private String getHostIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return local.getLocalizedMessage("FailedIP");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setTitle(LocalizationManager.getInstance().getLocalizedMessage("NetworkGameWindowTitle"));
        updateUIState();
        restartButton.setText(local.getLocalizedMessage("Restart"));
        finishButton.setText(local.getLocalizedMessage("FinishGame"));
    }
}