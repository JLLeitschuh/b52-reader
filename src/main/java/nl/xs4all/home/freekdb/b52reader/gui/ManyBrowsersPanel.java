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

import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

// todo: Make this class more generic by supporting different types of embedded browsers.
public class ManyBrowsersPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(ManyBrowsersPanel.class);

    private List<JPanel> browserPanels;
    private Map<String, JPanel> urlToBrowserPanels;
    private List<JWebBrowser> webBrowsers;
    private Map<String, String> urlToHtmlContent;
    
    public ManyBrowsersPanel() {
        super(new BorderLayout());

        browserPanels = new ArrayList<>();
        urlToBrowserPanels = new HashMap<>();
        webBrowsers = new ArrayList<>();
        urlToHtmlContent = new HashMap<>();
    }

    public boolean hasBrowserForUrl(String url) {
        return urlToBrowserPanels.containsKey(url);
    }

    public String getHtmlContent(String url) {
        return urlToHtmlContent.getOrDefault(url, "");
    }
    
    public void clearHtmlContent(String url) {
        urlToHtmlContent.remove(url);
    }
    
    public void showBrowser(String url, boolean makeBrowserVisible, boolean returnHtml) {
        if (urlToBrowserPanels.containsKey(url)) {
            if (makeBrowserVisible) {
                logger.info("Show browser for {}", url);
                hideAllBrowserPanels();
                makeBrowserPanelVisible(url);
            }
        } else {
            Optional<JPanel> visibleBrowserPanel = browserPanels.stream().filter(Component::isVisible).findFirst();
            hideAllBrowserPanels();

            JWebBrowser webBrowser = createWebBrowser(url, returnHtml);
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

    public void disposeAllBrowsers() {
        long start = System.currentTimeMillis();

        webBrowsers.forEach(NSPanelComponent::disposeNativePeer);

        int browserCount = webBrowsers.size();

        webBrowsers.clear();
        urlToBrowserPanels.clear();
        browserPanels.clear();

        long end = System.currentTimeMillis();
        logger.info("Disposed {} in {} milliseconds.", Utilities.countAndWord(browserCount, "browser"), end - start);
    }

    private JWebBrowser createWebBrowser(String url, boolean returnHtml) {
        JWebBrowser webBrowser = new JWebBrowser();

        webBrowser.setMenuBarVisible(false);
        webBrowser.setButtonBarVisible(false);
        webBrowser.setLocationBarVisible(false);

        webBrowser.navigate(url);

        addBrowserListener(url, returnHtml, webBrowser);

        return webBrowser;
    }

    private void addBrowserListener(String url, boolean returnHtml, JWebBrowser webBrowser) {
        String partUrl = url.substring(url.lastIndexOf('/') + 1);

        // Keep track of loading progress (https://sourceforge.net/p/djproject/discussion/671154/thread/1d25bf1a/).
        webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void loadingProgressChanged(WebBrowserEvent webBrowserEvent) {
                super.loadingProgressChanged(webBrowserEvent);

                logger.trace("[{}] Changed loading progress: {}", partUrl, webBrowser.getLoadingProgress());
            }

            @Override
            public void locationChanged(WebBrowserNavigationEvent webBrowserNavigationEvent) {
                super.locationChanged(webBrowserNavigationEvent);

                logger.trace("[{}] Location changed: {}", partUrl,
                             webBrowserNavigationEvent.getNewResourceLocation());

                if (returnHtml) {
                    urlToHtmlContent.put(url, webBrowser.getHTMLContent());
                }
            }
        });
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
