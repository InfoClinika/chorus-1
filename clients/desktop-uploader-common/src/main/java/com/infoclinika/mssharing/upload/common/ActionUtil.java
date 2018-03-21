// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.upload.common;

import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class ActionUtil {
    private final static Logger LOGGER = Logger.getLogger(ActionUtil.class);
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    public static void modalExecute(Window parent, String actionName, final Runnable action) {
        final JDialog dialog = DialogUtil.createLongProcessDialog(parent, actionName);

        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    execute(action);
                } finally {
                    dialog.setCursor(Cursor.getDefaultCursor());
                    dialog.setVisible(false);
                }
            }
        });

        dialog.setVisible(true);
    }

    public static void execute(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));

            throw new RuntimeException(e);
        }
    }
}
