/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JPanel;

import nl.xs4all.home.freekdb.b52reader.browsers.BrowserFactory;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * GUI panel which can handle multiple browsers and show one of them.
 */
public class ManyBrowsersPanel extends JPanel {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The browser factory for creating embedded browsers.
     */
    private transient BrowserFactory browserFactory;

    /**
     * Browser panels that contain embedded browsers.
     */
    private List<JPanel> browserPanels;

    /**
     * URLs mapped to browser panels.
     */
    private Map<String, JPanel> urlToBrowserPanels;

    /**
     * Embedded web browsers.
     */
    private List<JWebBrowser> webBrowsers;

    /**
     * Construct a {@link ManyBrowsersPanel} object, which can handle multiple browsers and show one of them.
     *
     * @param browserFactory the browser factory for creating embedded browsers.
     */
    public ManyBrowsersPanel(BrowserFactory browserFactory) {
        super(new BorderLayout());

        this.browserFactory = browserFactory;
        this.browserPanels = new ArrayList<>();
        this.urlToBrowserPanels = new HashMap<>();
        this.webBrowsers = new ArrayList<>();
    }

    /**
     * Is there an embedded web browser for a specific URL?
     *
     * @param url the URL to check for.
     * @return whether there is an embedded web browser for the specified URL.
     */
    boolean hasBrowserForUrl(String url) {
        return urlToBrowserPanels.containsKey(url);
    }

    /**
     * Create and/or show a web browser for a specific URL. If there is a browser for the URL, it will be made visible
     * (if <code>makeBrowserVisible</code> is <code>true</code>). Otherwise, a new browser is created and also be made
     * visible (if <code>makeBrowserVisible</code> is <code>true</code>).
     *
     * @param url                the URL the browser should go to.
     * @param makeBrowserVisible whether to make the browser for this URL visible or not.
     */
    void showBrowser(String url, boolean makeBrowserVisible) {
        if (urlToBrowserPanels.containsKey(url)) {
            if (makeBrowserVisible) {
                logger.info("Show browser for {}", url);
                hideAllBrowserPanels();
                makeBrowserPanelVisible(url);
            }
        } else {
            Optional<JPanel> visibleBrowserPanel = browserPanels.stream().filter(Component::isVisible).findFirst();
            hideAllBrowserPanels();

            JWebBrowser webBrowser = createWebBrowser(url);
            webBrowsers.add(webBrowser);

            JPanel browserPanel = new JPanel(new BorderLayout());
            browserPanel.add(webBrowser, BorderLayout.CENTER);
            browserPanels.add(browserPanel);
            urlToBrowserPanels.put(url, browserPanel);

            logger.debug("{} browser for {}", makeBrowserVisible ? "Show" : "Add", url);

            add(browserPanel, BorderLayout.CENTER);
            validate();

            if (!makeBrowserVisible) {
                hideAllBrowserPanels();
                visibleBrowserPanel.ifPresent(panel -> panel.setVisible(true));
            }
        }
    }

    /**
     * Dispose of all browsers.
     */
    void disposeAllBrowsers() {
        long start = System.currentTimeMillis();

        webBrowsers.forEach(NSPanelComponent::disposeNativePeer);

        int browserCount = webBrowsers.size();

        webBrowsers.clear();
        urlToBrowserPanels.clear();
        browserPanels.clear();

        removeAll();

        long end = System.currentTimeMillis();
        logger.info("Disposed {} in {} milliseconds.",
                    Utilities.countAndWord(browserCount, "browser"), end - start);
    }

    /**
     * Create an embedded web browser and go to the specified URL.
     *
     * @param url the URL to go to.
     * @return the embedded web browser.
     */
    private JWebBrowser createWebBrowser(String url) {
        JWebBrowser webBrowser = (JWebBrowser) browserFactory.createBrowser(
                browser -> {
                    String partUrl = url.substring(url.lastIndexOf('/') + 1);
                    logger.trace("[{}] Page loaded.", partUrl);
                }
        );

        webBrowser.navigate(url);

        return webBrowser;
    }

    /**
     * Make all browser panels invisible.
     */
    private void hideAllBrowserPanels() {
        browserPanels.forEach(browserPanel -> browserPanel.setVisible(false));
    }

    /**
     * Make the browser panel for the specified URL visible.
     *
     * @param url the URL for which the browser panel should be shown.
     */
    private void makeBrowserPanelVisible(String url) {
        urlToBrowserPanels.get(url).setVisible(true);
    }
}
