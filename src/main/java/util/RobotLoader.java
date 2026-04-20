package util;

import Localization.LocalizationManager;
import game.RobotDefalte;
import game.RobotModel;
import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

/**
 * Загружает робота из jar архива
 */
public class RobotLoader {
    /**
     * Загружает робота извне, если не получилось возвращает {@code defaultRobot}
     */
    public static RobotModel getNewRobotOrDefault(RobotDefalte defaultRobot, Component parent) {
        try {
            Optional<File> optionalJarFile = chooseFileToLoad(parent);

            if (optionalJarFile.isEmpty()) {
                return defaultRobot;
            }

            File jarFile = optionalJarFile.get();
            URL jarUrl = jarFile.toURI().toURL();

            try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl})) {
                Class<?> clazz = classLoader.loadClass("game.RobotV2");

                if (RobotModel.class.isAssignableFrom(clazz)) {
                    Logger.debug(LocalizationManager.getInstance().getLocalizedMessage("ClassLoad"));
                    return (RobotModel) clazz.getDeclaredConstructor().newInstance();
                } else {
                    Logger.debug("Загруженный класс не реализует " +
                            RobotModel.class.getName());
                }
            }
        } catch (ReflectiveOperationException e) {
            Logger.error(LocalizationManager.getInstance().getLocalizedMessage("ReflectError"));
        } catch (IOException e) {
            Logger.debug("Неудачная или прерванная операция ввода-вывода.");
        }

        return defaultRobot;
    }

    /**
     * Создает окно с выбором пути до файла и возвращает этот путь
     */
    private static Optional<File> chooseFileToLoad(Component parent) {
        JFileChooser fileChooser = new JFileChooser();

        int returnValue = fileChooser.showOpenDialog(parent);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return Optional.ofNullable(selectedFile);
        } else {
            return Optional.empty();
        }
    }
}
