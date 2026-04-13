package gui;

import Localization.LocalizationManager;
import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;
import state.Stateful;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Внутреннее окно для отображения протокола работы приложения.
 * Реализует интерфейс {@link LogChangeListener} для автоматического
 * обновления содержимого при добавлении новых записей в лог.
 */
public class LogWindow extends JInternalFrame implements LogChangeListener, Stateful, PropertyChangeListener {
    private LogWindowSource logSource;
    private TextArea logContent;
    private String nameWindow = "log_0";

    /**
     * Создаёт окно лога и регистрирует себя как слушателя изменений источника лога.
     */
    public LogWindow(LogWindowSource logSource) {
        super(LocalizationManager.getInstance().getLocalizedMessage("LogWindowTitle"),
                true, true, true, true);
        this.logSource = logSource;
        this.logSource.registerListener(this);
        this.logContent = new TextArea("");
        this.logContent.setSize(200, 500);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();
        LocalizationManager.getInstance().addPropertyChangeListener(this);
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
    public String getWindowName() {
        return nameWindow;
    }

    public void setWindowName(String name) {
        nameWindow = name;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setTitle(LocalizationManager.getInstance().getLocalizedMessage("LogWindowTitle"));
    }
}
