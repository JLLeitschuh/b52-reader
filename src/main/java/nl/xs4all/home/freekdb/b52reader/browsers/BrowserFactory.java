/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.browsers;

import javax.swing.JComponent;

/**
 * Interface for factories that create embedded browser components.
 */
public interface BrowserFactory {
    /**
     * Create a new embedded browser.
     *
     * @return the new embedded browser.
     */
    JComponent createBrowser();

    /**
     * Create a new embedded browser.
     *
     * @param browserListener the listener that is notified when the page has loaded.
     * @return the new embedded browser.
     */
    JComponent createBrowser(BrowserListener browserListener);
}
