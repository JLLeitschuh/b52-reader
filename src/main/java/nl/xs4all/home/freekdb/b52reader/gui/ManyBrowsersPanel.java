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
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

// todo: Add Javadocs.

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

    private List<JPanel> browserPanels;
    private Map<String, JPanel> urlToBrowserPanels;
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

    boolean hasBrowserForUrl(String url) {
        return urlToBrowserPanels.containsKey(url);
    }

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

    void disposeAllBrowsers() {
        long start = System.currentTimeMillis();

        webBrowsers.forEach(NSPanelComponent::disposeNativePeer);

        int browserCount = webBrowsers.size();

        webBrowsers.clear();
        urlToBrowserPanels.clear();
        browserPanels.clear();

        long end = System.currentTimeMillis();
        logger.info("Disposed {} in {} milliseconds.",
                    Utilities.countAndWord(browserCount, "browser"), end - start);
    }

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

    private void hideAllBrowserPanels() {
        browserPanels.forEach(browserPanel -> browserPanel.setVisible(false));
    }

    private void makeBrowserPanelVisible(String url) {
        if (urlToBrowserPanels.containsKey(url)) {
            urlToBrowserPanels.get(url).setVisible(true);
        } else {
            logger.error("Browser with url {} not found.", url);
        }
    }
}
