/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.browsers;

import javax.swing.JComponent;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

/**
 * Factory for creating embedded JWebBrowser components.
 */
public class JWebBrowserFactory implements BrowserFactory {
    /**
     * Create a new embedded browser.
     *
     * @return the new embedded browser.
     */
    @Override
    public JComponent createBrowser() {
        return createBrowser(null);
    }

    /**
     * Create a new embedded browser.
     *
     * @param browserListener the listener that is notified when the page has loaded.
     * @return the new embedded browser.
     */
    @Override
    public JComponent createBrowser(BrowserListener browserListener) {
        JWebBrowser webBrowser = new JWebBrowser();

        webBrowser.setMenuBarVisible(false);
        webBrowser.setButtonBarVisible(false);
        webBrowser.setLocationBarVisible(false);

        if (browserListener != null) {
            webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
                @Override
                public void locationChanged(WebBrowserNavigationEvent webBrowserNavigationEvent) {
                    super.locationChanged(webBrowserNavigationEvent);

                    browserListener.pageLoaded(webBrowser);
                }
            });
        }

        return webBrowser;
    }
}
