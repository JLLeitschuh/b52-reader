/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.browsers;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import javax.swing.JComponent;

import lombok.RequiredArgsConstructor;

/**
 * Factory for creating embedded JxBrowser components.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class JxBrowserFactory implements BrowserFactory {
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
        final Browser browser = new Browser();
        final BrowserView browserView = new BrowserView(browser);

        if (browserListener != null) {
            browser.addLoadListener(new FinishLoadingListener(browserView, browserListener));
        }

        return browserView;
    }


    /**
     * TeamDev web browser listener that receives a "finish loading frame" event from a web browser and then sends a
     * page loaded event to a (b52-reader) browser listener.
     */
    @RequiredArgsConstructor
    static class FinishLoadingListener extends LoadAdapter {
        /**
         * Browser view with browser that loads content.
         */
        final BrowserView browserView;

        /**
         * Browser listener that receives a page loaded event.
         */
        private final BrowserListener browserListener;

        @Override
        public void onFinishLoadingFrame(final FinishLoadingEvent event) {
            super.onFinishLoadingFrame(event);

            browserListener.pageLoaded(browserView);
        }
    }
}
