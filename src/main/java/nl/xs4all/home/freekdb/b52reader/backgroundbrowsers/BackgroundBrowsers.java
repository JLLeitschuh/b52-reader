/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.backgroundbrowsers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

/**
 * This class allows invisible browsers to be running in the background to get html content of a specific url.
 */
public class BackgroundBrowsers {
    private static final Map<String, JWebBrowser> URL_TO_WEB_BROWSER = new HashMap<>();
    private static final Map<String, String> URL_TO_HTML_CONTENT = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(BackgroundBrowsers.class);

    private List<JWebBrowser> webBrowsers;
    private JPanel backgroundBrowsersPanel;

    public BackgroundBrowsers(JPanel backgroundBrowsersPanel) {
        this.webBrowsers = new ArrayList<>();
        this.backgroundBrowsersPanel = backgroundBrowsersPanel;
    }

    public String getHtmlContent(String url) {
        return getHtmlContent(url, 10000);
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public String getHtmlContent(final String url, final int maxWaitTimeMs) {
        Thread htmlContentThread = new Thread(() -> {
            try {
                logger.debug("Launching a background browser for url " + url);

                URL_TO_HTML_CONTENT.remove(url);

                SwingUtilities.invokeAndWait(() -> launchBackgroundBrowser(url));

                logger.debug("Waiting for html content...");

                boolean done = false;
                int waitCount = 0;
                int maxWaitCount = maxWaitTimeMs / 100;
                while (!done && waitCount < maxWaitCount) {
                    Thread.sleep(100);

                    done = URL_TO_HTML_CONTENT.containsKey(url);
                    waitCount++;
                }

                closeBackgroundBrowser(url);
            } catch (InterruptedException | InvocationTargetException e) {
                logger.error("Exception while getting html content with a background browser.", e);
            }
        });

        try {
            htmlContentThread.start();
            htmlContentThread.join(maxWaitTimeMs);
        } catch (InterruptedException e) {
            logger.error("Exception while waiting for html content from a background browser.", e);
        }

        return URL_TO_HTML_CONTENT.get(url);
    }

    private void launchBackgroundBrowser(String url) {
        JWebBrowser webBrowser = new JWebBrowser();

        webBrowser.setMenuBarVisible(false);
        webBrowser.setButtonBarVisible(false);
        webBrowser.setLocationBarVisible(false);

        webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void locationChanged(WebBrowserNavigationEvent webBrowserNavigationEvent) {
                super.locationChanged(webBrowserNavigationEvent);

                URL_TO_HTML_CONTENT.put(url, webBrowser.getHTMLContent());
            }
        });

        URL_TO_WEB_BROWSER.put(url, webBrowser);
        webBrowsers.add(webBrowser);
        backgroundBrowsersPanel.add(webBrowser);

        webBrowser.navigate(url);
    }

    private void closeBackgroundBrowser(String url) {
        logger.debug("Closing the background browser for url " + url);

        JWebBrowser webBrowser = URL_TO_WEB_BROWSER.get(url);

        if (webBrowser != null) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    webBrowser.disposeNativePeer();

                    URL_TO_WEB_BROWSER.remove(url);
                    webBrowsers.remove(webBrowser);
                    backgroundBrowsersPanel.remove(webBrowser);
                });
            } catch (InterruptedException | InvocationTargetException e) {
                logger.error("Exception while closing a background browser.", e);
            }
        } else {
            System.err.println("Error closing background browser: no browser found for url " + url);
        }
    }

    public void closeAllBackgroundBrowsers() {
        logger.debug("Closing all background browsers.");

        try {
            SwingUtilities.invokeAndWait(() -> {
                webBrowsers.forEach(NSPanelComponent::disposeNativePeer);

                URL_TO_WEB_BROWSER.clear();
                URL_TO_HTML_CONTENT.clear();
                webBrowsers.clear();
                backgroundBrowsersPanel.removeAll();
            });
        } catch (InterruptedException | InvocationTargetException e) {
            logger.error("Exception while closing all background browsers.", e);
        }
    }
}
