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
}
