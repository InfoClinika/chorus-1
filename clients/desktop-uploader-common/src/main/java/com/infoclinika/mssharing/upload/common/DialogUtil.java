// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.upload.common;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class DialogUtil {

    public static JDialog createLongProcessDialog(Window parent, String message) {
        final JDialog dialog = new JDialog(parent);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setUndecorated(true);

        final JPanel panel = (JPanel) dialog.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new EmptyBorder(10, 30, 10, 50)));
        final JLabel messageLabel = new JLabel(message);
        messageLabel.setIconTextGap(30);
        messageLabel.setIcon(loadIcon("/images/progress.gif"));

        panel.add(messageLabel, BorderLayout.CENTER);

        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);

        return dialog;
    }

    public static void showMessage(Window parent, String message, String title, int jOptionPaneMessageCode) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                title,
                jOptionPaneMessageCode
        );
    }

    public static ImageIcon loadIcon(String pathToIcon) {
        return new ImageIcon(DialogUtil.class.getResource(pathToIcon));
    }
}
