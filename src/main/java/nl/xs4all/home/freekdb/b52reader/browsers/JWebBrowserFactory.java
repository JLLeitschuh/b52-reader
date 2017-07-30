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
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
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
    public JComponent createBrowser(final BrowserListener browserListener) {
        final JWebBrowser webBrowser = new JWebBrowser();

        webBrowser.setButtonBarVisible(false);
        webBrowser.setLocationBarVisible(false);
        webBrowser.setMenuBarVisible(false);

        if (browserListener != null) {
            webBrowser.addWebBrowserListener(new PageLoadedListener(webBrowser, browserListener));
        }

        return webBrowser;
    }


    /**
     * Nativeswing web browser listener that receives a location changed event from a web browser and then sends a page
     * loaded event to a (b52-reader) browser listener.
     */
    static class PageLoadedListener extends WebBrowserAdapter {
        /**
         * Web browser that loads content.
         */
        private final JWebBrowser webBrowser;

        /**
         * Browser listener that receives a page loaded event.
         */
        private final BrowserListener browserListener;

        /**
         * Construct a page loaded listener.
         *
         * @param webBrowser      web browser that loads content.
         * @param browserListener browser listener that receives a page loaded event.
         */
        PageLoadedListener(final JWebBrowser webBrowser, final BrowserListener browserListener) {
            this.webBrowser = webBrowser;
            this.browserListener = browserListener;
        }

        @Override
        public void locationChanged(final WebBrowserNavigationEvent webBrowserNavigationEvent) {
            super.locationChanged(webBrowserNavigationEvent);

            browserListener.pageLoaded(webBrowser);
        }
    }
}
