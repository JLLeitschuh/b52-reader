/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.browsers;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.mockito.Mockito;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the {@link BackgroundBrowsers} class.
 */
public class BackgroundBrowsersTest {
    private String htmlContent;

    @Test
    public void testWithNonfunctionalBrowser() throws IllegalAccessException {
        JWebBrowser mockJWebBrowser = Mockito.mock(JWebBrowser.class);

        // Initialize the private Container.component field to prevent a null pointer exception later.
        FieldUtils.writeField(mockJWebBrowser, "component", new ArrayList<>(), true);

        BrowserFactory mockBrowserFactory = Mockito.mock(BrowserFactory.class);
        Mockito.when(mockBrowserFactory.createBrowser(Mockito.any(BrowserListener.class))).thenReturn(mockJWebBrowser);

        JPanel backgroundBrowsersPanel = new JPanel();
        String url = "https://freekdb.home.xs4all.nl/";
        htmlContent = "This is clearly not null...";

        BackgroundBrowsers backgroundBrowsers = new BackgroundBrowsers(mockBrowserFactory, backgroundBrowsersPanel);
        Thread workerThread = new Thread(() -> htmlContent = backgroundBrowsers.getHtmlContent(url, 2000));
        workerThread.start();

        Awaitility.await().atMost(600, TimeUnit.MILLISECONDS)
                .until(() -> backgroundBrowsersPanel.getComponentCount() == 1);

        assertEquals(mockJWebBrowser, backgroundBrowsersPanel.getComponent(0));

        Awaitility.await().atMost(2800, TimeUnit.MILLISECONDS)
                .until(() -> htmlContent == null);

        assertNull(htmlContent);
    }
}
