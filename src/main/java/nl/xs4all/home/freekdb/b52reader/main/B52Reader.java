/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.main;

import java.net.URL;

import nl.xs4all.home.freekdb.b52reader.browsers.EmbeddedBrowserType;
import nl.xs4all.home.freekdb.b52reader.browsers.JWebBrowserFactory;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandlerJdbc;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;
import nl.xs4all.home.freekdb.b52reader.gui.MainGui;
import nl.xs4all.home.freekdb.b52reader.gui.ManyBrowsersPanel;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;

// todo: Wish list: use Lombok, Guice, JPA/Hibernate, Checkstyle, FindBugs, and PMD/CPD.

/**
 * The b52-reader main class which creates the application and launches it.
 * <p>
 * mvn exec:java -Dexec.mainClass="nl.xs4all.home.freekdb.b52reader.main.B52Reader"
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class B52Reader {
    /**
     * Private constructor to hide the implicit public one, since this class is not meant to be instantiated.
     */
    private B52Reader() {
        // Should not be called.
    }

    /**
     * The main method that starts the application and takes care of some library initialization.
     *
     * @param arguments the (currently unused) command-line parameters.
     */
    public static void main(final String[] arguments) {
        if (Constants.EMBEDDED_BROWSER_TYPE == EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING) {
            Utilities.ignoreStandardErrorStream();

            NativeInterface.open();
        }

        final MainGui mainGui = new MainGui(new ManyBrowsersPanel(new JWebBrowserFactory()));
        final URL configurationUrl = B52Reader.class.getClassLoader().getResource(Constants.CONFIGURATION_FILE_NAME);
        final PersistencyHandlerJdbc persistencyHandler = new PersistencyHandlerJdbc();

        final MainApplication mainApplication = new MainApplication(mainGui, configurationUrl, persistencyHandler);

        mainApplication.createAndLaunchApplication();

        if (Constants.EMBEDDED_BROWSER_TYPE == EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING) {
            NativeInterface.runEventPump();
        }
    }
}
