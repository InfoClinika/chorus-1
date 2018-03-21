package com.infoclinika.mssharing.integration.test.components;

import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class ControlFactory {

    public Button button(By by) {
        return new Button(by);
    }

    public Label label(By by) {
        return new Label(by);
    }

    public DropdownList dropdownList(By by) {
        return new DropdownList(by);
    }

    public Checkbox checkbox(By by) {
        return new Checkbox(by);
    }

    public InputBox inputBox(By by) {
        return new InputBox(by);
    }

    public Image image(By by) {
        return new Image(by);
    }

    public Frame frame(By by) {
        return new Frame(by);
    }

    public Chart chart(By by){
        return new Chart(by);
    }

    public Pane pane(By by){
        return new Pane(by);
    }

    public AutoCompleteList autoCompleteList(By by){
        return new AutoCompleteList(by);
    }

    public WizardTable factorsTable(By by){
        return new WizardTable(by);
    }

    public RadioButton radioButton(By by){
        return new RadioButton(by);
    }
}
