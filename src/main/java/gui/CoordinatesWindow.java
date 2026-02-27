package gui;

import game.GameModel;
import state.Stateful;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.Objects;

/**
 * Окно с координатами робота
 */
public class CoordinatesWindow extends JInternalFrame implements PropertyChangeListener, Stateful {
    private final GameModel model;
    private final JLabel content;
    private String robotCoordinates;

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
                log.Logger.error("Ошибка восстановления состояния LogWindow: " + e.getMessage());            }
            try {
                setClosed(isClosed);
            } catch (Exception e) {
                log.Logger.error("Ошибка восстановления закрытости LogWindow: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            log.Logger.error("Ошибка восстановления состояния LogWindow: " + e.getMessage());
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
    }
}

