/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import nl.xs4all.home.freekdb.b52reader.main.MainCallbacks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link MainGui} class.
 */
public class MainGuiTest {
    private JFrame mockFrame;
    private Container mockContentPane;
    private MainCallbacks mockMainCallbacks;

    private WindowListener windowListener;
    private boolean shutdownApplicationWasCalled;

    @Before
    public void setUp() {
        mockFrame = Mockito.mock(JFrame.class);
        mockContentPane = new Container();
        mockMainCallbacks = Mockito.mock(MainCallbacks.class);

        Mockito.when(mockFrame.getContentPane()).thenReturn(mockContentPane);

        Mockito.doAnswer(invocationOnMock -> windowListener = invocationOnMock.getArgument(0))
                .when(mockFrame).addWindowListener(Mockito.any(WindowListener.class));

        Mockito.doAnswer(invocationOnMock -> shutdownApplicationWasCalled = true)
                .when(mockMainCallbacks).shutdownApplication(Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void testInitializeBackgroundBrowsersPanel() {
        MainGui mainGui = new MainGui(null);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame);

        assertEquals(1, mockContentPane.getComponentCount());
        assertFalse(mockContentPane.getComponent(0).isVisible());
    }

    @Test
    public void testInitializeGui() throws InvocationTargetException, InterruptedException {
        MainGui mainGui = new MainGui(mockMainCallbacks);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame);
        mainGui.initializeGui(new ArrayList<>());

        waitForGuiTasks();

        assertEquals(3, mockContentPane.getComponentCount());
    }

    @Test
    public void testShutdownApplication() throws InterruptedException, InvocationTargetException {
        MainGui mainGui = new MainGui(mockMainCallbacks);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame);
        mainGui.initializeGui(new ArrayList<>());

        waitForGuiTasks();

        windowListener.windowClosing(new WindowEvent(mockFrame, WindowEvent.WINDOW_CLOSING));

        assertTrue(shutdownApplicationWasCalled);
    }

    // Wait for other tasks on the event dispatch thread to be completed (like MainGui.finishGuiInitialization).
    private void waitForGuiTasks() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> mockFrame.getTitle());
    }
}
