/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import javax.swing.JPanel;

import nl.xs4all.home.freekdb.b52reader.backgroundbrowsers.BackgroundBrowsers;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;

/**
 * Central connection point for general objects.
 */
public class ObjectHub {
    /**
     * The persistency handler that enables communication with the database.
     */
    private static PersistencyHandler persistencyHandler;

    /**
     * The hidden panel in the GUI where the background browsers can be added to.
     */
    private static JPanel backgroundBrowsersPanel;

    /**
     * The background browsers handler.
     */
    private static BackgroundBrowsers backgroundBrowsers;

    /**
     * Inject the persistency handler that enables communication with the database.
     *
     * @param persistencyHandler the persistency handler.
     */
    public static void injectPersistencyHandler(PersistencyHandler persistencyHandler) {
        ObjectHub.persistencyHandler = persistencyHandler;
    }

    /**
     * Get the persistency handler that enables communication with the database.
     *
     * @return the persistency handler.
     */
    public static PersistencyHandler getPersistencyHandler() {
        return persistencyHandler;
    }

    /**
     * Inject the hidden panel in the GUI where the background browsers can be added to.
     *
     * @param backgroundBrowsersPanel the panel for the background browsers.
     */
    public static void injectBackgroundBrowsersPanel(JPanel backgroundBrowsersPanel) {
        ObjectHub.backgroundBrowsersPanel = backgroundBrowsersPanel;
    }

    /**
     * Get the background browsers handler.
     *
     * @return the background browsers handler.
     */
    public static BackgroundBrowsers getBackgroundBrowsers() {
        if (backgroundBrowsers == null) {
            backgroundBrowsers = new BackgroundBrowsers(backgroundBrowsersPanel);
        }

        return backgroundBrowsers;
    }
}
