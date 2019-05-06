package com.infoclinika.mssharing.autoimporter.gui.swing.util;

import javax.swing.*;
import java.awt.*;

/**
 * author Ruslan Duboveckij
 */
public class FancyButton extends JButton {
    public FancyButton(Icon icon, Icon pressed, Icon rollover, String toolTip) {
        super(icon);
        setMargin(new Insets(0, 0, 0, 0));
        setPreferredSize(new Dimension(40, 24));
        setFocusPainted(false);
        setRolloverEnabled(true);
        setRolloverIcon(rollover);
        setPressedIcon(pressed);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setToolTipText(toolTip);
    }


}
