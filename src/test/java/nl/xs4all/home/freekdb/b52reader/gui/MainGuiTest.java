/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.main.MainCallbacks;
import nl.xs4all.home.freekdb.b52reader.model.Article;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static nl.xs4all.home.freekdb.b52reader.gui.MainGuiTest.FilterTestType.CHANGE_TEXT;
import static nl.xs4all.home.freekdb.b52reader.gui.MainGuiTest.FilterTestType.INSERT_TEXT;
import static nl.xs4all.home.freekdb.b52reader.gui.MainGuiTest.FilterTestType.NO_MATCHES;
import static nl.xs4all.home.freekdb.b52reader.gui.MainGuiTest.FilterTestType.REMOVE_TEXT;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link MainGui} class.
 */
public class MainGuiTest {
    private JFrame mockFrame;
    private Container mockContentPane;
    private MainCallbacks mockMainCallbacks;
    private Configuration mockConfiguration;

    private WindowListener windowListener;
    private boolean shutdownApplicationWasCalled;

    @Before
    public void setUp() {
        mockFrame = Mockito.mock(JFrame.class);
        mockContentPane = new Container();
        mockMainCallbacks = Mockito.mock(MainCallbacks.class);
        mockConfiguration = Mockito.mock(Configuration.class);

        Mockito.when(mockFrame.getContentPane()).thenReturn(mockContentPane);

        Mockito.doAnswer(invocationOnMock -> windowListener = invocationOnMock.getArgument(0))
                .when(mockFrame).addWindowListener(Mockito.any(WindowListener.class));

        Mockito.doAnswer(invocationOnMock -> shutdownApplicationWasCalled = true)
                .when(mockMainCallbacks).shutdownApplication(Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void testInitializeBackgroundBrowsersPanel() {
        MainGui mainGui = new MainGui(null);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);

        assertEquals(1, mockContentPane.getComponentCount());
        assertFalse(mockContentPane.getComponent(0).isVisible());
    }

    @Test
    public void testInitializeGuiSpanTable() throws InvocationTargetException, InterruptedException {
        MainGui mainGui = new MainGui(mockMainCallbacks);

        Mockito.when(mockConfiguration.useSpanTable()).thenReturn(true);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(new ArrayList<>());

        waitForGuiTasks();

        assertEquals(3, mockContentPane.getComponentCount());
    }

    @Test
    public void testInitializeGuiCustomRenderer() throws InvocationTargetException, InterruptedException {
        MainGui mainGui = new MainGui(mockMainCallbacks);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(new ArrayList<>());

        waitForGuiTasks();

        assertEquals(3, mockContentPane.getComponentCount());
    }

    @Test
    public void testFilterInsert() throws BadLocationException, InterruptedException, ReflectiveOperationException {
        testFilter(INSERT_TEXT);
    }

    @Test
    public void testFilterRemove() throws BadLocationException, InterruptedException, ReflectiveOperationException {
        testFilter(REMOVE_TEXT);
    }

    @Test
    public void testFilterChange() throws BadLocationException, InterruptedException, ReflectiveOperationException {
        testFilter(CHANGE_TEXT);
    }

    @Test
    public void testFilterNoMatches() throws BadLocationException, InterruptedException, ReflectiveOperationException {
        testFilter(NO_MATCHES);
    }

    private void testFilter(FilterTestType testType) throws BadLocationException, InterruptedException,
                                                            ReflectiveOperationException {
        MainGui mainGui = new MainGui(mockMainCallbacks);

        Mockito.when(mockConfiguration.useSpanTable()).thenReturn(testType == CHANGE_TEXT);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(getTestArticles());

        waitForGuiTasks();

        JTable table = (JTable) findComponent(mockContentPane, JTable.class);
        assertNotNull(table);

        assertEquals(mockConfiguration.useSpanTable() ? 6 : 3, table.getRowCount());

        JTextField filterTextField = (JTextField) findComponent(mockContentPane, JTextField.class);
        assertNotNull(filterTextField);
        AbstractDocument document = (AbstractDocument) filterTextField.getDocument();

        document.insertString(0, "title:title1", null);

        assertEquals(mockConfiguration.useSpanTable() ? 2 : 1, table.getRowCount());

        if (testType == REMOVE_TEXT) {
            document.remove(0, document.getLength());
        } else if (testType == CHANGE_TEXT) {
            document.replace(6, 6, "title2", null);

            // Since change is implemented as remove and insert, the fireChangedUpdate method is called with reflection.
            AbstractDocument.DefaultDocumentEvent mockEvent = Mockito.mock(AbstractDocument.DefaultDocumentEvent.class);
            Method method = AbstractDocument.class.getDeclaredMethod("fireChangedUpdate", DocumentEvent.class);
            method.setAccessible(true);
            method.invoke(document, mockEvent);
        } else if (testType == NO_MATCHES) {
            document.insertString(document.getLength(), "-some-nonsense", null);
        }

        checkArticlesInGui(testType, mainGui, table.getRowCount());
    }

    private List<Article> getTestArticles() {
        return Arrays.asList(
                new Article.Builder("u1", "s1", null, "Title1", null, "Text 1.")
                        .build(),
                new Article.Builder("u2", "s2", null, "Title2", null, "Text 2.")
                        .build(),
                new Article.Builder("u3", "s3", null, "Title3", null, "Text 3.")
                        .starred(true).read(true).archived(true)
                        .build()
        );
    }

    private void checkArticlesInGui(FilterTestType testType, MainGui mainGui, int tableRowCount)
            throws IllegalAccessException {
        int expectedRowCount = testType == NO_MATCHES
                ? 0
                : (testType == REMOVE_TEXT ? 2 : 1) * (mockConfiguration.useSpanTable() ? 2 : 1);

        assertEquals(expectedRowCount, tableRowCount);

        Object filteredArticlesField = FieldUtils.readField(mainGui, "filteredArticles", true);

        assertTrue(filteredArticlesField instanceof List);
        List filteredArticles = (List) filteredArticlesField;

        if (testType == INSERT_TEXT || testType == REMOVE_TEXT) {
            assertEquals("u1", ((Article) filteredArticles.get(0)).getUrl());
        }

        if (EnumSet.of(REMOVE_TEXT, CHANGE_TEXT).contains(testType)) {
            assertEquals("u2", ((Article) filteredArticles.get(testType == REMOVE_TEXT ? 1 : 0)).getUrl());
        }
    }

    @Test
    public void testShutdownApplication() throws InterruptedException, InvocationTargetException {
        MainGui mainGui = new MainGui(mockMainCallbacks);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(new ArrayList<>());

        waitForGuiTasks();

        windowListener.windowClosing(new WindowEvent(mockFrame, WindowEvent.WINDOW_CLOSING));

        assertTrue(shutdownApplicationWasCalled);
    }

    // Wait for other tasks on the event dispatch thread to be completed (like MainGui.finishGuiInitialization).
    private void waitForGuiTasks() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> mockFrame.getTitle());
    }

    private Component findComponent(Container parent, Class<?> searchClass) {
        Component result = null;

        int componentIndex = 0;
        while (componentIndex < parent.getComponentCount() && result == null) {
            Component component = parent.getComponent(componentIndex);

            if (searchClass.isInstance(component)) {
                result = component;
            } else if (component instanceof Container) {
                result = findComponent((Container) component, searchClass);
            }

            componentIndex++;
        }

        return result;
    }


    enum FilterTestType {
        INSERT_TEXT,
        REMOVE_TEXT,
        CHANGE_TEXT,
        NO_MATCHES
    }
}