/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.browsers;

import javax.swing.JComponent;

import org.junit.Test;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link JWebBrowserFactory} class.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class JWebBrowserFactoryTest {
    @Test
    public void testCreateBrowserWithoutListener() {
        checkBrowser(new JWebBrowserFactory().createBrowser(), 0);
    }

    @Test
    public void testCreateBrowserWithListener() {
        JComponent browser = new JWebBrowserFactory().createBrowser(webBrowser -> System.out.println("Page has loaded"));

        checkBrowser(browser, 1);
    }

    private void checkBrowser(JComponent browser, int expectedListenerCount) {
        assertEquals(JWebBrowser.class, browser.getClass());

        JWebBrowser webBrowser = (JWebBrowser) browser;

        assertEquals(expectedListenerCount, webBrowser.getWebBrowserListeners().length);

        assertFalse(webBrowser.isBackNavigationEnabled());
        assertFalse(webBrowser.isButtonBarVisible());
        assertFalse(webBrowser.isForwardNavigationEnabled());
        assertFalse(webBrowser.isJavascriptEnabled());
        assertFalse(webBrowser.isLocationBarVisible());
        assertFalse(webBrowser.isMenuBarVisible());
        assertTrue(webBrowser.isStatusBarVisible());
    }
}
