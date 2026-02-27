package gui;

import game.GameController;
import game.GameModel;
import game.GameVisualizer;
import state.Stateful;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.Map;

/**
 * Внутреннее окно (JInternalFrame) для отображения игрового поля.
 */
public class GameWindow extends JInternalFrame implements Stateful {
    private final GameVisualizer gameVisualizer;

    /**
     * Создаёт новое игровое окно с заголовком "Игровое поле".
     */
    public GameWindow(GameModel model) {
        super("Игровое поле", true, true, true, true);
        GameController controller = new GameController(model);
        gameVisualizer = new GameVisualizer(model,controller);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(gameVisualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
    }

    @Override
    public void saveState(Map<String, String> state) {
        state.put("x", String.valueOf(getX()));
        state.put("y", String.valueOf(getY()));
        state.put("width", String.valueOf(getWidth()));
        state.put("height", String.valueOf(getHeight()));
        state.put("isIcon", String.valueOf(isIcon()));
        state.put("isMaximum", String.valueOf(isMaximum()));
        state.put("isClosed", String.valueOf(isClosed()));
    }

    @Override
    public void loadState(Map<String, String> state) {
        try {
            int x = Integer.parseInt(state.getOrDefault("x", String.valueOf(getX())));
            int y = Integer.parseInt(state.getOrDefault("y", String.valueOf(getY())));
            int width = Integer.parseInt(state.getOrDefault("width", String.valueOf(getWidth())));
            int height = Integer.parseInt(state.getOrDefault("height", String.valueOf(getHeight())));
            boolean isIcon = Boolean.parseBoolean(state.getOrDefault("isIcon", "false"));
            boolean isMaximum = Boolean.parseBoolean(state.getOrDefault("isMaximum", "false"));
            boolean isClosed = Boolean.parseBoolean(state.getOrDefault("isClosed", "false"));

            setBounds(x, y, width, height);
            setIcon(isIcon);
            try {
                setMaximum(isMaximum);
            } catch (Exception e) {
                log.Logger.error("Ошибка восстановления состояния GameWindow: " + e.getMessage());            }
            try {
                setClosed(isClosed);
            } catch (Exception e) {
                log.Logger.error("Ошибка восстановления закрытости GameWindow: " + e.getMessage());
            }
            } catch (NumberFormatException e) {
                log.Logger.error("Ошибка восстановления состояния GameWindow: " + e.getMessage());
            } catch (PropertyVetoException e) {
                throw new RuntimeException(e);
        }
    }
}
