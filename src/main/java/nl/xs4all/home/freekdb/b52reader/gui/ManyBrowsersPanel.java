/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


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

    void showBrowser(String url) {
        if (urlToBrowserPanels.containsKey(url)) {
            hideAllBrowsers();
            makeBrowserVisible(url);
        }
        else {
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
    }

    @SuppressWarnings("unused")
    void removeBrowser(String url) {
        if (urlToBrowserPanels.containsKey(url)) {
            JPanel browserPanel = urlToBrowserPanels.remove(url);
            browserPanels.remove(browserPanel);
            if (browserPanel.isVisible() && !urlToBrowserPanels.isEmpty()) {
                // This should not happen. Show another browser before removing the visible one.
                String randomUrl = urlToBrowserPanels.keySet().iterator().next();
                makeBrowserVisible(randomUrl);
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

    private void makeBrowserVisible(String url) {
        if (urlToBrowserPanels.containsKey(url)) {
            urlToBrowserPanels.get(url).setVisible(true);
        }
        else {
            System.err.println("Browser with url " + url + " not found.");
        }
    }
}
