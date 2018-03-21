package com.infoclinika.mssharing.integration.test.components;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Orlov
 */
public class Chart extends Element {

    public Chart(By by) {
        super(by);
    }

    public boolean isChartImageLoaded() {
        final boolean[] isLoaded = new boolean[1];
        FluentWait<By> fluentWait = new FluentWait<By>(getBy());
        fluentWait.pollingEvery(500, TimeUnit.MILLISECONDS);
        fluentWait.withTimeout(10000, TimeUnit.MILLISECONDS);
        fluentWait.withMessage("Chart image is not loaded after 10 seconds of waiting");
        fluentWait.until(new Predicate<By>() {
            public boolean apply(By by) {
                isLoaded[0] = !composeWebElement().getAttribute("src").contains("empty.gif");
                return isLoaded[0];
            }
        });
        return isLoaded[0];
    }
}
