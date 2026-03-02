package gui;

import game.GameController;
import game.GameModel;
import game.GameVisualizer;
import state.Stateful;

import javax.swing.*;
import java.awt.*;


/**
 * Внутреннее окно (JInternalFrame) для отображения игрового поля.
 */
public class GameWindow extends JInternalFrame implements Stateful {
    private final GameVisualizer gameVisualizer;
    private String nameWindow = "game_0";

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
    public String getWindowName() {
        return nameWindow;
    }


    public void setWindowName(String name) {
        nameWindow = name;
    }
}
