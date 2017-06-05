/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.browsers.BrowserFactory;
import nl.xs4all.home.freekdb.b52reader.browsers.BrowserListener;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for the {@link ManyBrowsersPanel} class.
 */
public class ManyBrowsersPanelTest {
    private List<JWebBrowser> mockBrowsers;
    private int browserCount;
    private Map<JWebBrowser, BrowserListener> browserToListener;
    private BrowserFactory mockBrowserFactory;

    @Before
    public void setUp() throws IllegalAccessException {
        mockBrowsers = Arrays.asList(createMockBrowser(), createMockBrowser());
        browserCount = 0;

        browserToListener = new HashMap<>();

        mockBrowserFactory = createMockBrowserFactory(mockBrowsers, browserToListener);
    }

    @Test
    public void testHasBrowserForUrl() {
        assertFalse(new ManyBrowsersPanel(mockBrowserFactory).hasBrowserForUrl("url"));
    }

    @Test
    public void testShowBrowserMakeVisible() throws IllegalAccessException {
        ManyBrowsersPanel manyBrowsersPanel = new ManyBrowsersPanel(mockBrowserFactory);

        assertEquals(0, manyBrowsersPanel.getComponentCount());

        // Create the first browser.
        manyBrowsersPanel.showBrowser("url1", true);

        // Show the existing (first) browser.
        manyBrowsersPanel.showBrowser("url1", true);

        // Create the second browser.
        manyBrowsersPanel.showBrowser("url2", false);

        assertManyBrowsersPanel(manyBrowsersPanel, mockBrowsers.get(0), true);
    }

    @Test
    public void testShowBrowserNotVisible() {
        ManyBrowsersPanel manyBrowsersPanel = new ManyBrowsersPanel(mockBrowserFactory);

        assertEquals(0, manyBrowsersPanel.getComponentCount());

        // Create the browsers.
        manyBrowsersPanel.showBrowser("url1", false);
        manyBrowsersPanel.showBrowser("url2", false);

        // Call showBrowser for an existing browser.
        manyBrowsersPanel.showBrowser("url2", false);

        assertManyBrowsersPanel(manyBrowsersPanel, mockBrowsers.get(0), false);
    }

    @Test
    public void testDisposeAllBrowsers() throws IllegalAccessException {
        ManyBrowsersPanel manyBrowsersPanel = new ManyBrowsersPanel(mockBrowserFactory);

        manyBrowsersPanel.showBrowser("url", true);

        assertEquals(1, manyBrowsersPanel.getComponentCount());

        manyBrowsersPanel.disposeAllBrowsers();

        assertEquals(0, manyBrowsersPanel.getComponentCount());
    }

    private JWebBrowser createMockBrowser() throws IllegalAccessException {
        JWebBrowser mockWebBrowser = Mockito.mock(JWebBrowser.class);

        // Initialize the private Container.component field to make prevent a null pointer exception later.
        FieldUtils.writeField(mockWebBrowser, "component", new ArrayList<>(), true);

        Mockito.when(mockWebBrowser.navigate(Mockito.anyString())).then(invocationOnMock -> {
            if (browserToListener.containsKey(mockWebBrowser)) {
                browserToListener.get(mockWebBrowser).pageLoaded(mockWebBrowser);
            }

            return null;
        });

        return mockWebBrowser;
    }

    private BrowserFactory createMockBrowserFactory(List<JWebBrowser> mockBrowsers,
                                                    Map<JWebBrowser, BrowserListener> browserToListener) {
        BrowserFactory mockBrowserFactory = Mockito.mock(BrowserFactory.class);

        Mockito.when(mockBrowserFactory.createBrowser(Mockito.any())).thenAnswer(invocationOnMock -> {
            JWebBrowser mockBrowser = mockBrowsers.get(Math.min(browserCount, mockBrowsers.size() - 1));
            browserCount++;

            BrowserListener browserListener = invocationOnMock.getArgument(0);
            if (browserListener != null) {
                browserToListener.put(mockBrowser, browserListener);
            }

            return mockBrowser;
        });

        return mockBrowserFactory;
    }

    private void assertManyBrowsersPanel(ManyBrowsersPanel manyBrowsersPanel, JWebBrowser expectedBrowser,
                                         boolean expectBrowserPanelVisible) {
        assertEquals(2, manyBrowsersPanel.getComponentCount());

        // The embedded browser is added to a browser panel, which is added to the manyBrowsersPanel.
        Container browserPanel = (Container) manyBrowsersPanel.getComponent(0);
        Component embeddedBrowser = browserPanel.getComponent(0);

        assertEquals(expectedBrowser, embeddedBrowser);
        assertEquals(expectBrowserPanelVisible, browserPanel.isVisible());
    }
}
