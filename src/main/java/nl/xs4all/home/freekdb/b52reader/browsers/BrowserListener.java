/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.browsers;

import javax.swing.JComponent;

/**
 * Interface for listening to embedded browser events.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public interface BrowserListener {
    /**
     * Invoked when a page has loaded.
     *
     * @param browser the browser that has loaded the page.
     */
    void pageLoaded(JComponent browser);
}
