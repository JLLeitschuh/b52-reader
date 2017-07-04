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

/**
 * Unit tests for the {@link BackgroundBrowsers} class.
 */
public class BackgroundBrowsersTest {
    private BrowserFactory mockBrowserFactory;
    private JWebBrowser mockJWebBrowser;
    private BrowserListener browserListener;
    private String htmlContent;

    @Test
    public void testGetHtmlContent() throws IllegalAccessException {
        String url = "https://freekdb.home.xs4all.nl/";
        String inProgressHtmlContent = "<html><body>Working...</body></html>";
        String expectedHtmlContent = "<html><body>Hi there!</body></html>";
        htmlContent = "This is clearly different from the expected html content...";

        prepareMockObjects(inProgressHtmlContent, expectedHtmlContent);

        JPanel backgroundBrowsersPanel = new JPanel();

        BackgroundBrowsers backgroundBrowsers = new BackgroundBrowsers(mockBrowserFactory, backgroundBrowsersPanel);

        Thread getHtmlContentThread = new Thread(
                () -> htmlContent = backgroundBrowsers.getHtmlContent(url, 2000)
        );

        getHtmlContentThread.start();

        Awaitility.await().atMost(600, TimeUnit.MILLISECONDS)
                .until(() -> browserListener != null);

        simulatePageLoadedEvents();

        Awaitility.await().atMost(1600, TimeUnit.MILLISECONDS)
                .until(() -> expectedHtmlContent.equals(htmlContent));

        assertEquals(expectedHtmlContent, htmlContent);
    }

    private void prepareMockObjects(String inProgressHtmlContent, String expectedHtmlContent) throws IllegalAccessException {
        mockJWebBrowser = Mockito.mock(JWebBrowser.class);
        Mockito.when(mockJWebBrowser.getHTMLContent()).thenReturn(inProgressHtmlContent, expectedHtmlContent);

        // Initialize the private Container.component field to prevent a null pointer exception later.
        FieldUtils.writeField(mockJWebBrowser, "component", new ArrayList<>(), true);

        mockBrowserFactory = Mockito.mock(BrowserFactory.class);

        Mockito.when(mockBrowserFactory.createBrowser(Mockito.any(BrowserListener.class)))
                .thenAnswer(invocationOnMock -> {
                    browserListener = invocationOnMock.getArgument(0);

                    return mockJWebBrowser;
                });
    }

    private void simulatePageLoadedEvents() {
        Thread simulatePageLoadedThread = new Thread(() -> {
            // Simulate the "page loaded" event for the in progress html content ("Working...").
            browserListener.pageLoaded(mockJWebBrowser);

            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Simulate the "page loaded" event for the final html content.
            browserListener.pageLoaded(mockJWebBrowser);
        });

        simulatePageLoadedThread.start();
    }
}
