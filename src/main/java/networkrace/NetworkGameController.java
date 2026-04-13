package networkrace;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Обработчик клавиатуры для сетевой игры.
 * Отправляет команды на сервер, НЕ использует автоматику GameModel.
 */
public class NetworkGameController extends KeyAdapter {
    private final NetworkGameAdapter adapter;
    private volatile boolean enabled = true;

    /**
     * Создаёт обработчик ввода, связанный с указанным адаптером игры.
     */
    public NetworkGameController(NetworkGameAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!enabled) {
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_R && adapter.getWinnerId() != -1) {
            adapter.sendRestart();
            e.consume();
            return;
        }

        if (!adapter.isGameStarted() || adapter.getWinnerId() != -1) {
            return;
        }

        String command;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    command = "UP"; break;
            case KeyEvent.VK_DOWN:  command = "DOWN"; break;
            case KeyEvent.VK_LEFT:  command = "LEFT"; break;
            case KeyEvent.VK_RIGHT: command = "RIGHT"; break;
            default: return;
        }

        adapter.sendMove(command);
        e.consume();
    }

    /**
     * Включает или отключает обработку клавиатурного ввода.
     *
     * @param enabled {@code true} для разрешения ввода, {@code false} для блокировки
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Привязывает контроллер к графическому компоненту.
     * Регистрирует слушатель клавиатуры, добавляет обработчик клика для перехвата фокуса
     * и запрашивает фокус у компонента сразу после инициализации.
     *
     */
    public void attachTo(JComponent component) {
        component.setFocusable(true);
        component.setRequestFocusEnabled(true);
        component.addKeyListener(this);

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                component.requestFocusInWindow();
            }
        });

        SwingUtilities.invokeLater(component::requestFocusInWindow);
    }
}