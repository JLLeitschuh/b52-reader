/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

/**
 * The different options that are available for embedded browsers.
 */
public enum EmbeddedBrowserType {
    /**
     * The {@link chrriis.dj.nativeswing.swtimpl.components.JWebBrowser} from the DJ Native Swing project.
     */
    EMBEDDED_BROWSER_DJ_NATIVE_SWING,

    /**
     * A simple placeholder for the real embedded browsers (a panel with a label).
     */
    EMBEDDED_BROWSER_PLACEHOLDER
}
