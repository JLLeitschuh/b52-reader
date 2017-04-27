package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

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

    void addAndShowBrowser(String url) {
        JPanel browserPanel = new JPanel(new BorderLayout());
        JWebBrowser webBrowser = createWebBrowser(url);
        webBrowsers.add(webBrowser);
        browserPanel.add(webBrowser, BorderLayout.CENTER);
        hideAllBrowsers();
        browserPanels.add(browserPanel);
        urlToBrowserPanels.put(url, browserPanel);
        add(browserPanel, BorderLayout.CENTER);
        validate();
    }

    void makeBrowserVisible(String url) {
        hideAllBrowsers();
        showBrowser(url);
    }

    @SuppressWarnings("unused")
    void removeBrowser(String url) {
        if (urlToBrowserPanels.containsKey(url)) {
            JPanel browserPanel = urlToBrowserPanels.remove(url);
            browserPanels.remove(browserPanel);
            if (browserPanel.isVisible() && !urlToBrowserPanels.isEmpty()) {
                // This should not happen. Show another browser before removing the visible one.
                String randomUrl = urlToBrowserPanels.keySet().iterator().next();
                showBrowser(randomUrl);
            }
            remove(browserPanel);
            validate();
            repaint();
        } else {
            System.err.println("No dummy browsers left?!?");
        }
    }

    void disposeAllBrowsers() {
        webBrowsers.forEach(NSPanelComponent::disposeNativePeer);

        int browserCount = webBrowsers.size();

        webBrowsers.clear();
        urlToBrowserPanels.clear();
        browserPanels.clear();

        System.out.println("Disposed " + Utilities.countAndWord(browserCount, "browser") + ".");
    }

    private JWebBrowser createWebBrowser(String url) {
        JWebBrowser webBrowser = new JWebBrowser();

        webBrowser.setMenuBarVisible(false);
        webBrowser.setButtonBarVisible(false);
        webBrowser.setLocationBarVisible(false);

        webBrowser.navigate(url);

        return webBrowser;
    }

    private void hideAllBrowsers() {
        browserPanels.forEach(browserPanel -> browserPanel.setVisible(false));
    }

    private void showBrowser(String url) {
        if (urlToBrowserPanels.containsKey(url)) {
            urlToBrowserPanels.get(url).setVisible(true);
        }
        else {
            System.err.println("Browser with url " + url + " not found.");
        }
    }

//    private void showRandomBrowser() {
//        browserPanels.get(randomGenerator.nextInt(browserPanels.size())).setVisible(true);
//    }
}
