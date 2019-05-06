// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.clients.common;

import javax.swing.*;
import java.awt.*;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class DialogUtil {

    public static void showMessage(Window parent, String message, String title, int optionPaneMessageCode) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            optionPaneMessageCode
        );
    }

}
