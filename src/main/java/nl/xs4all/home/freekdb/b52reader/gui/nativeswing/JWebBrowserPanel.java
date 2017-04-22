/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui.nativeswing;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class JWebBrowserPanel {
    public static JPanel createJWebBrowserPanel(String url) {
        JPanel webBrowserPanel = new JPanel(new BorderLayout());

        JWebBrowser webBrowser = new JWebBrowser();

        webBrowser.setMenuBarVisible(false);
        webBrowser.setButtonBarVisible(false);
        webBrowser.setLocationBarVisible(false);

        webBrowser.navigate(url);

        webBrowserPanel.add(webBrowser, BorderLayout.CENTER);

        return webBrowserPanel;
    }
}
