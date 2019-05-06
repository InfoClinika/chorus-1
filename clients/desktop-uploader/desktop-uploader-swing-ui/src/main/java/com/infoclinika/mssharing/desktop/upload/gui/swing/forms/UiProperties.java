package com.infoclinika.mssharing.desktop.upload.gui.swing.forms;

import net.sf.image4j.codec.ico.ICODecoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *     date:   31.01.14
 */
public abstract class UiProperties {

    public static final int COLUMN_MARGIN = 3;
    public static final int ROW_MARGIN = 3;
    public static final int ROW_HEIGHT = 24;
    public static final int SPEED_COLUMN_WIDTH = 90;
    public static final int PROGRESS_COLUMN_WIDTH = 150;
    public static final int SIZE_COLUMN_WIDTH = 100;
    public static final int SIZE_INDEX_COLUMN_WIDTH = 30;
    public static final int EDITABLE_CELL_MARGIN = 1;
    public static final Color PROGRESS_BAR_COLOR = new Color(86, 170, 58);
    public static final Color BORDER_COLOR = new Color(230, 230, 230);
    public static final int BORDER_THICKNESS = 2;
    public static final List<BufferedImage> APP_ICONS;

    static {
        final URL resource = ClassLoader.getSystemClassLoader().getResource("images/icon.ico");

        checkNotNull(resource);

        try (InputStream inputStream = resource.openStream()) {
            APP_ICONS = ICODecoder.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read icon.ico");
        }
    }
}
