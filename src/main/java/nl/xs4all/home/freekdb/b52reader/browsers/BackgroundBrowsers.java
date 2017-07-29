/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.browsers;

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

/**
 * This class allows invisible browsers to be running in the background to get html content of a specific url.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class BackgroundBrowsers {
    /**
     * Map of URLs to {@link JWebBrowser} objects.
     */
    private static final Map<String, JWebBrowser> URL_TO_WEB_BROWSER = new HashMap<>();

    /**
     * Map of URLs to html content.
     */
    private static final Map<String, String> URL_TO_HTML_CONTENT = new HashMap<>();

    /**
     * The default maximum wait time (in milliseconds) for retrieving html content.
     */
    private static final int DEFAULT_MAXIMUM_WAIT_TIME_MS = 10000;

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The browser factory for creating embedded browsers.
     */
    private BrowserFactory browserFactory;

    /**
     * The (invisible) panel to which the browsers can be added (to allow them to work).
     */
    private JPanel backgroundBrowsersPanel;

    /**
     * List to keep track of all {@link JWebBrowser} objects, to enable cleanup of any forgotten browsers.
     */
    private List<JWebBrowser> webBrowsers;

    /**
     * Construct a {@link BackgroundBrowsers} object, which can handle multiple browsers working in the background.
     *
     * @param browserFactory          the browser factory for creating embedded browsers.
     * @param backgroundBrowsersPanel the (invisible) panel to which the browsers can be added (to allow them to work).
     */
    public BackgroundBrowsers(final BrowserFactory browserFactory, final JPanel backgroundBrowsersPanel) {
        this.browserFactory = browserFactory;
        this.backgroundBrowsersPanel = backgroundBrowsersPanel;
        this.webBrowsers = new ArrayList<>();
    }

    /**
     * Get the html content for the specified url. A default timeout of 10 seconds is used.
     *
     * @param url the url for which the html content should be retrieved.
     * @return the html content that was retrieved or null.
     */
    public String getHtmlContent(final String url) {
        return getHtmlContent(url, DEFAULT_MAXIMUM_WAIT_TIME_MS);
    }

    /**
     * Get the html content for the specified url. The specified maximum wait time in milliseconds is used.
     *
     * @param url           the url for which the html content should be retrieved.
     * @param maxWaitTimeMs the maximum amount of time to wait (in milliseconds).
     * @return the html content that was retrieved or null.
     */
    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public String getHtmlContent(final String url, final int maxWaitTimeMs) {
        try {
            logger.debug("Launching a background browser for url " + url);

            URL_TO_HTML_CONTENT.remove(url);

            SwingUtilities.invokeAndWait(() -> launchBackgroundBrowser(url));

            waitForHtmlContent(url, maxWaitTimeMs);

            if (URL_TO_HTML_CONTENT.containsKey(url)) {
                logger.debug("Html content size: {} characters.", URL_TO_HTML_CONTENT.get(url).length());
            }

            closeBackgroundBrowser(url);
        } catch (final InterruptedException | InvocationTargetException e) {
            logger.error("Exception while getting html content with a background browser.", e);
        }

        return URL_TO_HTML_CONTENT.get(url);
    }

    /**
     * Launch a background browser and add a listener that puts the html content in the <code>URL_TO_HTML_CONTENT</code>.
     *
     * @param url the url for which the html content should be retrieved.
     */
    private void launchBackgroundBrowser(final String url) {
        final JWebBrowser webBrowser = (JWebBrowser) browserFactory.createBrowser(
            browser -> updateHtmlContent(url, (JWebBrowser) browser)
        );

        URL_TO_WEB_BROWSER.put(url, webBrowser);
        webBrowsers.add(webBrowser);
        backgroundBrowsersPanel.add(webBrowser);

        webBrowser.navigate(url);
    }

    /**
     * Wait for html content to be received.
     *
     * @param url           the url for which the html content should be retrieved.
     * @param maxWaitTimeMs the maximum amount of time to wait (in milliseconds).
     * @throws InterruptedException      if waiting somehow fails or is interrupted.
     * @throws InvocationTargetException if getting the html content from the browser fails.
     */
    private void waitForHtmlContent(final String url, final int maxWaitTimeMs)
            throws InterruptedException, InvocationTargetException {
        logger.debug("Waiting for html content...");

        boolean done = false;
        int waitCount = 0;
        final int waitTimePerIterationMs = 100;
        final int maxWaitCount = maxWaitTimeMs / waitTimePerIterationMs;

        while (!done && waitCount < maxWaitCount) {
            Thread.sleep(waitTimePerIterationMs);

            if (URL_TO_HTML_CONTENT.containsKey(url)) {
                // Some systems provide some kind of intermediate "in progress" html content.
                if (URL_TO_HTML_CONTENT.get(url).contains("Working...")) {
                    logger.debug("Html page is being constructed...");

                    final int refreshEveryXthIteration = 10;

                    if (waitCount % refreshEveryXthIteration == 0) {
                        logger.trace("Refresh html content.");

                        SwingUtilities.invokeAndWait(() -> updateHtmlContent(url, URL_TO_WEB_BROWSER.get(url)));

                        final String htmlContent = URL_TO_HTML_CONTENT.get(url);
                        final int maxHtmlContentLengthToLog = 120;
                        final int endIndex = Math.min(maxHtmlContentLengthToLog, htmlContent.length());
                        logger.trace("Html content: " + htmlContent.substring(0, endIndex));
                    }
                } else {
                    done = true;
                }
            }

            waitCount++;
        }
    }

    /**
     * Update the html content map for the specified url.
     *
     * @param url     the url for which the html content should be updated.
     * @param browser the browser to use for getting the html content.
     */
    private void updateHtmlContent(final String url, final JWebBrowser browser) {
        URL_TO_HTML_CONTENT.put(url, browser.getHTMLContent());
    }

    /**
     * Close the background browser that was launched for the specified url.
     *
     * @param url the url for which the html content should be retrieved.
     */
    private void closeBackgroundBrowser(final String url) {
        logger.debug("Closing the background browser for url " + url);

        final JWebBrowser webBrowser = URL_TO_WEB_BROWSER.get(url);

        if (webBrowser != null) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    webBrowser.disposeNativePeer();

                    URL_TO_WEB_BROWSER.remove(url);
                    webBrowsers.remove(webBrowser);
                    backgroundBrowsersPanel.remove(webBrowser);
                });
            } catch (final InterruptedException | InvocationTargetException e) {
                logger.error("Exception while closing a background browser.", e);
            }
        } else {
            logger.error("Error closing background browser: no browser found for url {}", url);
        }
    }

    /**
     * Report whether there are background web browsers active.
     *
     * @return true if at least one background web browser is active.
     */
    boolean webBrowsersActive() {
        return !webBrowsers.isEmpty();
    }

    /**
     * Close all background browsers that for some reason have not been closed yet.
     */
    public void closeAllBackgroundBrowsers() {
        if (!webBrowsers.isEmpty()) {
            webBrowsers.forEach(NSPanelComponent::disposeNativePeer);

            logger.debug("Closed all {} background browsers.", webBrowsers.size());
        }

        URL_TO_WEB_BROWSER.clear();
        URL_TO_HTML_CONTENT.clear();
        webBrowsers.clear();

        if (backgroundBrowsersPanel != null) {
            backgroundBrowsersPanel.removeAll();
        }
    }
}
