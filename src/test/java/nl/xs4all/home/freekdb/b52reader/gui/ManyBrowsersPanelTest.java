/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.util.ArrayList;

import javax.swing.JLabel;

import nl.xs4all.home.freekdb.b52reader.browsers.BrowserFactory;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.mockito.Mockito;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for the {@link ManyBrowsersPanel} class.
 */
public class ManyBrowsersPanelTest {
    @Test
    public void testHasBrowserForUrl() {
        BrowserFactory mockBrowserFactory = Mockito.mock(BrowserFactory.class);
        Mockito.when(mockBrowserFactory.createBrowser(Mockito.any())).thenReturn(new JLabel());

        ManyBrowsersPanel manyBrowsersPanel = new ManyBrowsersPanel(mockBrowserFactory);

        assertFalse(manyBrowsersPanel.hasBrowserForUrl("url"));
    }

    @Test
    public void testShowBrowser() throws IllegalAccessException {
        JWebBrowser mockWebBrowser = Mockito.mock(JWebBrowser.class);
        // Set the private Container.component field to make sure initialization is successful.
        FieldUtils.writeField(mockWebBrowser, "component", new ArrayList<>(), true);
        BrowserFactory mockBrowserFactory = Mockito.mock(BrowserFactory.class);
        Mockito.when(mockBrowserFactory.createBrowser(Mockito.any())).thenReturn(mockWebBrowser);

        ManyBrowsersPanel manyBrowsersPanel = new ManyBrowsersPanel(mockBrowserFactory);

        assertEquals(0, manyBrowsersPanel.getComponentCount());

        manyBrowsersPanel.showBrowser("url", false);

        assertEquals(1, manyBrowsersPanel.getComponentCount());
    }
}
