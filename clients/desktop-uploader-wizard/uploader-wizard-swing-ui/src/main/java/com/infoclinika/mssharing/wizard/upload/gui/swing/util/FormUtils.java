package com.infoclinika.mssharing.wizard.upload.gui.swing.util;

import java.awt.*;

/**
 * @author timofey.kasyanov
 *         date: 08.04.14.
 */
public class FormUtils {

    public static void setToScreenCenter(Component component) {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final Dimension componentSize = component.getSize();
        component.setBounds(
                screenSize.width / 2 - componentSize.width / 2,
                screenSize.height / 2 - componentSize.height / 2,
                component.getWidth(),
                component.getHeight());
    }

}
