/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.main;

import java.net.URL;

import nl.xs4all.home.freekdb.b52reader.browsers.JWebBrowserFactory;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.EmbeddedBrowserType;
import nl.xs4all.home.freekdb.b52reader.gui.MainGui;
import nl.xs4all.home.freekdb.b52reader.gui.ManyBrowsersPanel;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandlerJdbc;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;

// todo: Use Lombok.
// todo: Use Guice.
// todo: Use JPA/Hibernate.
// todo: Use Checkstyle.
// todo: Use FindBugs.
// todo: Use PMD/CPD.

/**
 * The b52-reader main class which creates the application and launches it.
 * <p>
 * mvn exec:java -Dexec.mainClass="nl.xs4all.home.freekdb.b52reader.main.B52Reader"
 */
public class B52Reader {
    /**
     * The main method that starts the application and takes care of some library initialization.
     *
     * @param arguments the (currently unused) command-line parameters.
     */
    public static void main(String[] arguments) {
        if (Constants.EMBEDDED_BROWSER_TYPE == EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING) {
            Utilities.ignoreStandardErrorStream();

            NativeInterface.open();
        }

        MainGui mainGui = new MainGui(new ManyBrowsersPanel(new JWebBrowserFactory()));
        URL configurationUrl = B52Reader.class.getClassLoader().getResource(Constants.CONFIGURATION_FILE_NAME);
        PersistencyHandlerJdbc persistencyHandler = new PersistencyHandlerJdbc();

        MainApplication mainApplication = new MainApplication(mainGui, configurationUrl, persistencyHandler);
        
        mainApplication.createAndLaunchApplication();

        if (Constants.EMBEDDED_BROWSER_TYPE == EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING) {
            NativeInterface.runEventPump();
        }
    }
}
