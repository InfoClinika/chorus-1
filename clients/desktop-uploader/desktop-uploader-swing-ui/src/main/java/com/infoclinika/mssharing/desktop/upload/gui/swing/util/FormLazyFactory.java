package com.infoclinika.mssharing.desktop.upload.gui.swing.util;

import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.LoginForm;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.WizardMainForm;

/**
 * @author timofey.kasyanov
 *     date:   27.01.14
 */
public abstract class FormLazyFactory {

    public abstract LoginForm getLoginForm();

    public abstract WizardMainForm getWizardMainForm();

}
