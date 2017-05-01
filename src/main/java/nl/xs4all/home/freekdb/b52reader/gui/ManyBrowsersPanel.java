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

import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

// todo: Make this class more generic by supporting different types of embedded browsers.
class ManyBrowsersPanel extends JPanel {
    private List<JPanel> browserPanels;
    private Map<String, JPanel> urlToBrowserPanels;
    private List<JWebBrowser> webBrowsers;

    ManyBrowsersPanel() {
        super(new BorderLayout());

        browserPanels = new ArrayList<>();
        urlToBrowserPanels = new HashMap<>();
        webBrowsers = new ArrayList<>();
    }

    boolean hasBrowserForUrl(String url) {
        return urlToBrowserPanels.containsKey(url);
    }

    void showBrowser(String url, boolean makeBrowserVisible) {
        if (urlToBrowserPanels.containsKey(url)) {
            if (makeBrowserVisible) {
                System.out.println("Show browser for " + url);
                hideAllBrowserPanels();
                makeBrowserPanelVisible(url);
            }
        } else {
            System.out.println("Create a new browser for " + url);

            Optional<JPanel> visibleBrowserPanel = browserPanels.stream().filter(Component::isVisible).findFirst();
            hideAllBrowserPanels();

            JWebBrowser webBrowser = createWebBrowser(url);
            webBrowsers.add(webBrowser);

            JPanel browserPanel = new JPanel(new BorderLayout());
            browserPanel.add(webBrowser, BorderLayout.CENTER);
            browserPanels.add(browserPanel);
            urlToBrowserPanels.put(url, browserPanel);

            System.out.println((makeBrowserVisible ? "Show" : "Add") + " the browser for " + url);

            add(browserPanel, BorderLayout.CENTER);
            validate();

            if (!makeBrowserVisible) {
                hideAllBrowserPanels();
                visibleBrowserPanel.ifPresent(panel -> panel.setVisible(true));
            }
        }
    }

    @SuppressWarnings("unused")
    void removeBrowser(String url) {
        if (urlToBrowserPanels.containsKey(url)) {

            // Dispose (disposeNativePeer) and remove (webBrowsers) the browser too!!!

            JPanel browserPanel = urlToBrowserPanels.remove(url);
            browserPanels.remove(browserPanel);
            if (browserPanel.isVisible() && !urlToBrowserPanels.isEmpty()) {
                // This should not happen. Show another browser before removing the visible one.
                String randomUrl = urlToBrowserPanels.keySet().iterator().next();
                makeBrowserPanelVisible(randomUrl);
            }
            remove(browserPanel);
            validate();
            repaint();
        } else {
            System.err.println("Browser not found.");
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
        System.out.println("Disposed " + Utilities.countAndWord(browserCount, "browser") + " in " +
                           (end - start) + " milliseconds.");
    }

    private JWebBrowser createWebBrowser(String url) {
        JWebBrowser webBrowser = new JWebBrowser();

        webBrowser.setMenuBarVisible(false);
        webBrowser.setButtonBarVisible(false);
        webBrowser.setLocationBarVisible(false);

        webBrowser.navigate(url);

        // https://sourceforge.net/p/djproject/discussion/671154/thread/1d25bf1a/

        String partUrl = url.substring(url.lastIndexOf('/') + 1);

        webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void loadingProgressChanged(WebBrowserEvent webBrowserEvent) {
                super.loadingProgressChanged(webBrowserEvent);

                System.out.println("[" + System.currentTimeMillis() + " - " + partUrl + "] Changed loading progress: " +
                                   webBrowser.getLoadingProgress());
            }

            @Override
            public void locationChanged(WebBrowserNavigationEvent webBrowserNavigationEvent) {
                super.locationChanged(webBrowserNavigationEvent);

                System.out.println("[" + System.currentTimeMillis() + " - " + partUrl + "] Location changed: " +
                                   webBrowserNavigationEvent.getNewResourceLocation());
            }
        });

        // The initial loading progress always seems to be 100.
        // System.out.println("[" + partUrl + "] Initial loading progress: " + webBrowser.getLoadingProgress());

        return webBrowser;
    }

    private void hideAllBrowserPanels() {
        browserPanels.forEach(browserPanel -> browserPanel.setVisible(false));
    }

    private void makeBrowserPanelVisible(String url) {
        if (urlToBrowserPanels.containsKey(url)) {
            urlToBrowserPanels.get(url).setVisible(true);
        } else {
            System.err.println("Browser with url " + url + " not found.");
        }
    }
}
