/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.awt.Color;
import java.text.SimpleDateFormat;

/**
 * Some general constants.
 */
public class Constants {
    /**
     * The name and version of the application.
     */
    public static final String APPLICATION_NAME_AND_VERSION = "B52 reader 0.0.6";

    /**
     * The date/time format with day of week, day of month, month name, hours, and minutes. Example: "Mon 15-May 22:28".
     */
    public static final SimpleDateFormat DATE_TIME_FORMAT_LONGER = new SimpleDateFormat("EEE dd-MMM HH:mm");

    /**
     * Nice light blue color (used for selected rows in the table).
     */
    public static final Color NICE_LIGHT_BLUE = new Color(205, 230, 247);

    /**
     * The embedded browser type that is currently used.
     */
    public static final EmbeddedBrowserType EMBEDDED_BROWSER_TYPE = EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING;

    /**
     * The maximum number of browsers that are loaded in the background.
     */
    public static final int BACKGROUND_BROWSER_MAX_COUNT = 6;

    /**
     * The delay in milliseconds between background tasks: preloading browsers.
     */
    public static final int BACKGROUND_TIMER_DELAY = 1000;

    /**
     * Main URL of the NRC Handelsblad website.
     */
    public static final String NRC_MAIN_URL = "https://www.nrc.nl/";

    /**
     * Article source ID of the NRC Handelsblad website.
     */
    public static final String NRC_SOURCE_ID = "nrc";

    /**
     * Test fetching the html with the list of articles using a background browser.
     */
    static final boolean GET_ARTICLE_LIST_WITH_BROWSER = false;

    /**
     * Name of the configuration file.
     */
    public static final String CONFIGURATION_FILE_NAME = "b52-reader.configuration";

    /**
     * Header for the configuration file.
     */
    static final String CONFIGURATION_HEADER = "Configuration file for the b52-reader "
                                               + "(https://github.com/FreekDB/b52-reader).";
}
