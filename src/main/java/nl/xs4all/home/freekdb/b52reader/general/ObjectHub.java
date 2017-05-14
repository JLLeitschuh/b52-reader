/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import javax.swing.JPanel;

import nl.xs4all.home.freekdb.b52reader.backgroundbrowsers.BackgroundBrowsers;

/**
 * Central connection point for general objects.
 */
public class ObjectHub {
    private static JPanel backgroundBrowsersPanel;
    private static BackgroundBrowsers backgroundBrowsers;

    public static void setBackgroundBrowsersPanel(JPanel backgroundBrowsersPanel) {
        ObjectHub.backgroundBrowsersPanel = backgroundBrowsersPanel;
    }

    public static BackgroundBrowsers getBackgroundBrowsers() {
        if (backgroundBrowsers == null) {
            backgroundBrowsers = new BackgroundBrowsers(backgroundBrowsersPanel);
        }

        return backgroundBrowsers;
    }
}
