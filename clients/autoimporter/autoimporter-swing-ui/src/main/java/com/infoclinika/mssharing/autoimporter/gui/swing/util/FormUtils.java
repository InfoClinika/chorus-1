package com.infoclinika.mssharing.autoimporter.gui.swing.util;

import net.sf.image4j.codec.ico.ICODecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.*;
import static com.infoclinika.mssharing.autoimporter.messages.MessagesSource.getMessage;

public final class FormUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormUtils.class);

    public static final ImageIcon IS_STOPPED_ICON = getImageIcon("image/icon-stop-status.png");
    public static final ImageIcon IS_STARTED_ICON = getImageIcon("image/icon-play-status.png");
    public static final ImageIcon ADD = getImageIcon("image/icon-add.png");
    public static final ImageIcon ADD_HOVER = getImageIcon("image/icon-add-hover.png");
    public static final ImageIcon ADD_PRESSED = getImageIcon("image/icon-add-pressed.png");
    public static final ImageIcon DELETE = getImageIcon("image/icon-delete.png");
    public static final ImageIcon DELETE_HOVER = getImageIcon("image/icon-delete-hover.png");
    public static final ImageIcon DELETE_PRESSED = getImageIcon("image/icon-delete-pressed.png");
    public static final ImageIcon START = getImageIcon("image/icon-play.png");
    public static final ImageIcon START_HOVER = getImageIcon("image/icon-play-hover.png");
    public static final ImageIcon START_PRESSED = getImageIcon("image/icon-play-pressed.png");
    public static final ImageIcon STOP = getImageIcon("image/icon-stop.png");
    public static final ImageIcon STOP_HOVER = getImageIcon("image/icon-stop-hover.png");
    public static final ImageIcon STOP_PRESSED = getImageIcon("image/icon-stop-pressed.png");
    public static final ImageIcon CLIPBOARD = getImageIcon("image/icon-copy-to-clipboard.png");
    public static final ImageIcon CLIPBOARD_HOVER = getImageIcon("image/icon-copy-to-clipboard.png");
    public static final ImageIcon CLIPBOARD_PRESSED = getImageIcon("image/icon-copy-to-clipboard.png");

    private static ImageIcon getImageIcon(String pathToResource) {
        return new ImageIcon(ClassLoader.getSystemClassLoader().getResource(pathToResource));
    }

    public static final Color PROGRESS_BAR_COLOR = new Color(86, 170, 58);

    public static void setToScreenCenter(Component component) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Dimension componentSize = component.getSize();
        component.setBounds(
            screenSize.width / 2 - componentSize.width / 2,
            screenSize.height / 2 - componentSize.height / 2,
            component.getWidth(),
            component.getHeight()
        );
    }

    public static boolean checkFileExists(Component parentComponent, String folder) {

        if (!new File(folder).exists()) {

            int confirm = JOptionPane.showConfirmDialog(
                parentComponent,
                getMessage(MODALS_CREATE_FOLDER_TEXT) + " (" + folder + ")",
                getMessage(MODALS_CONFIRM_TITLE),
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                if (!new File(folder).mkdirs()) {

                    JOptionPane.showMessageDialog(
                        parentComponent,
                        getMessage(MODALS_CANNOT_CREATE_FOLDER_TEXT) + " (" + folder + ")",
                        getMessage(MODALS_ERROR_TITLE),
                        JOptionPane.ERROR_MESSAGE
                    );

                } else {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static void logError(Throwable ex) {
        ex.printStackTrace();
        LOGGER.error("", ex);
    }

    public static void printError(Throwable ex) {

        JOptionPane.showMessageDialog(
            null,
            getMessage(APP_ERROR_COMMON),
            getMessage(MODALS_ERROR_TITLE),
            JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
        LOGGER.error("", ex);

    }

    public static void showError(String message) {

        JOptionPane.showMessageDialog(
            null,
            message,
            getMessage(MODALS_ERROR_TITLE),
            JOptionPane.ERROR_MESSAGE
        );

    }

    public static final java.util.List<BufferedImage> APP_ICONS;

    static {

        final URL resource = ClassLoader.getSystemClassLoader().getResource("image/icon.ico");

        checkNotNull(resource);

        try (InputStream inputStream = resource.openStream()) {
            APP_ICONS = ICODecoder.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read icon.ico");

        }

    }

}
