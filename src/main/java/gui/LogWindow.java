package gui;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.Map;

/**
 * Внутреннее окно для отображения протокола работы приложения.
 * Реализует интерфейс {@link LogChangeListener} для автоматического
 * обновления содержимого при добавлении новых записей в лог.
 */
public class LogWindow extends JInternalFrame implements LogChangeListener,Stateful {
    private LogWindowSource logSource;
    private TextArea logContent;

    /**
     * Создаёт окно лога и регистрирует себя как слушателя изменений источника лога.
     */
    public LogWindow(LogWindowSource logSource) {
        super("Протокол работы", true, true, true, true);
        this.logSource = logSource;
        this.logSource.registerListener(this);
        this.logContent = new TextArea("");
        this.logContent.setSize(200, 500);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();
    }

    /**
     * Обновляет содержимое текстовой области на основе текущих записей в логе.
     */
    private void updateLogContent() {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : logSource.all()) {
            content.append(entry.getMessage()).append("\n");
        }
        logContent.setText(content.toString());
        logContent.invalidate();
    }

    @Override
    public void onLogChanged() {
        EventQueue.invokeLater(this::updateLogContent);
    }

    @Override
    public void saveState(Map<String, String> state) {
        state.put("x", String.valueOf(getX()));
        state.put("y", String.valueOf(getY()));
        state.put("width", String.valueOf(getWidth()));
        state.put("height", String.valueOf(getHeight()));
        state.put("isIcon", String.valueOf(isIcon()));
        state.put("isMaximum", String.valueOf(isMaximum()));
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

            setBounds(x, y, width, height);
            setIcon(isIcon);
            try {
                setMaximum(isMaximum);
            } catch (Exception e) {
                // ignore
            }
            } catch (NumberFormatException e) {
                log.Logger.error("Ошибка восстановления состояния LogWindow: " + e.getMessage());
            } catch (PropertyVetoException e) {
                throw new RuntimeException(e);
        }
    }
}
