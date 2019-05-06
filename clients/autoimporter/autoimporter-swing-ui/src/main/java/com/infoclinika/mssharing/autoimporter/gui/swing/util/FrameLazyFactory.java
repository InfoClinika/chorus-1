package com.infoclinika.mssharing.autoimporter.gui.swing.util;

import com.infoclinika.mssharing.autoimporter.gui.swing.forms.ConfigDialog;
import com.infoclinika.mssharing.autoimporter.gui.swing.forms.LoginForm;
import com.infoclinika.mssharing.autoimporter.gui.swing.forms.MainForm;

/**
 * author Ruslan Duboveckij
 */
public abstract class FrameLazyFactory {
    public abstract LoginForm getLoginForm();

    public abstract ConfigDialog getAddConfigDialog();

    public abstract MainForm getMainForm();
}
