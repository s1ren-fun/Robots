package gui;

import game.GameModel;
import state.Stateful;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Objects;

/**
 * Окно с координатами робота
 */
public class CoordinatesWindow extends JInternalFrame implements PropertyChangeListener, Stateful {
    private final GameModel model;
    private final JLabel content;
    private String robotCoordinates;
    private String nameWindow = "coor_0";

    /**
     * Создаёт новое окно с координатми.
     * @param model
     */
    public CoordinatesWindow(GameModel model) {
        super("Координатное окно", true, true, true, true);
        this.model = model;
        content = new JLabel(robotCoordinates);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setLayout(new GridBagLayout());
        add(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(content);

        model.addTextChangeListener(this);

        setSize(400, 100);
        setLocation(50, 50);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Objects.equals(evt.getPropertyName(), GameModel.ROBOT_POSITION_UPDATED)) {
            robotCoordinates = "X:" + model.getRobotX() + ", Y:" + model.getRobotY();
            content.setText(robotCoordinates);
            repaint();
        }
    }

    @Override
    public String getWindowName() {
        return nameWindow;
    }

    public void setWindowName(String name) {
        nameWindow = name;
    }
}

