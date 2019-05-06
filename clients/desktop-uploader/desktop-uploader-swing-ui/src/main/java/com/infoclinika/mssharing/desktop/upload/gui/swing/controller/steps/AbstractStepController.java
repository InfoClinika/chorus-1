package com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps;

import javax.swing.*;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
 */
public abstract class AbstractStepController implements StepController {
    private JComponent view;

    @Override
    public void setView(JComponent component) {
        view = component;
    }

    @Override
    public JComponent getView() {
        return view;
    }
}
