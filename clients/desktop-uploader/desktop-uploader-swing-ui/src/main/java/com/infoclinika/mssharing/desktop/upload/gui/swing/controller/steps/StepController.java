package com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps;

import javax.swing.*;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
 */
public interface StepController {

    void activate();

    void setView(JComponent component);

    JComponent getView();

}
