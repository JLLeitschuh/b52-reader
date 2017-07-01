/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import com.google.common.collect.ImmutableSet;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
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
    private ManyBrowsersPanel mockManyBrowsersPanel;
    private MainCallbacks mockMainCallbacks;
    private Configuration mockConfiguration;

    private WindowListener windowListener;
    private boolean shutdownApplicationWasCalled;

    @Before
    public void setUp() throws IllegalAccessException {
        Set<String> urlsWithBrowsers = ImmutableSet.of("u2", "u4", "u6");

        mockFrame = Mockito.mock(JFrame.class);
        mockContentPane = new Container();
        mockManyBrowsersPanel = Mockito.mock(ManyBrowsersPanel.class);
        mockMainCallbacks = Mockito.mock(MainCallbacks.class);
        mockConfiguration = Mockito.mock(Configuration.class);

        Mockito.when(mockFrame.getContentPane()).thenReturn(mockContentPane);

        Mockito.doAnswer(invocationOnMock -> windowListener = invocationOnMock.getArgument(0))
                .when(mockFrame).addWindowListener(Mockito.any(WindowListener.class));

        Mockito.when(mockManyBrowsersPanel.hasBrowserForUrl(Mockito.anyString()))
                .thenAnswer(invocationOnMock -> urlsWithBrowsers.contains(invocationOnMock.<String>getArgument(0)));

        // Initialize the private Container.component field to prevent a null pointer exception later.
        FieldUtils.writeField(mockManyBrowsersPanel, "component", new ArrayList<>(), true);

        Mockito.doAnswer(invocationOnMock -> shutdownApplicationWasCalled = true)
                .when(mockMainCallbacks).shutdownApplication(Mockito.anyInt(), Mockito.any());

        Mockito.when(mockConfiguration.getBackgroundBrowserMaxCount()).thenReturn(2);
        Mockito.when(mockConfiguration.getBackgroundTimerInitialDelay()).thenReturn(2000);
        Mockito.when(mockConfiguration.getBackgroundTimerDelay()).thenReturn(600);
    }

    @Test
    public void testInitializeBackgroundBrowsersPanel() {
        MainGui mainGui = new MainGui(mockManyBrowsersPanel);
        mainGui.setMainCallbacks(mockMainCallbacks);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);

        assertEquals(1, mockContentPane.getComponentCount());
        assertFalse(mockContentPane.getComponent(0).isVisible());

        assertNotNull(mainGui.getBackgroundBrowsersPanel());
        assertEquals(0, mainGui.getBackgroundBrowsersPanel().getComponentCount());
    }

    @Test
    public void testInitializeGuiSpanTable() throws InvocationTargetException, InterruptedException {
        MainGui mainGui = new MainGui(mockManyBrowsersPanel);
        mainGui.setMainCallbacks(mockMainCallbacks);

        Mockito.when(mockConfiguration.useSpanTable()).thenReturn(true);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(new ArrayList<>());

        waitForGuiTasks();

        assertEquals(3, mockContentPane.getComponentCount());
    }

    @Test
    public void testInitializeGuiCustomRendererTable() throws InvocationTargetException, InterruptedException {
        MainGui mainGui = new MainGui(mockManyBrowsersPanel);
        mainGui.setMainCallbacks(mockMainCallbacks);

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

    @Test
    public void testClickWithoutSelectedRow() throws InterruptedException, InvocationTargetException {
        testClickInTable(false, false, true, false, 120);
    }

    @Test
    public void testClickInSpanTable() throws InterruptedException, InvocationTargetException {
        // Note: the x coordinates for the mouse events are related to MainGui.setTableColumnWidths.

        testClickInTable(false, false, true, true, 120);
        testClickInTable(true, false, true, true, 120);

        testClickInTable(false, false, true, true, 220);
        testClickInTable(false, true, true, true, 220);
    }

    @Test
    public void testClickInCustomRendererTable() throws InterruptedException, InvocationTargetException {
        // Note: the x coordinates for the mouse events are related to ArticleTableCellRenderer and
        // MainGui.getColumnIndexFromClick.

        testClickInTable(false, false, false, true, 20);
        testClickInTable(true, false, false, true, 20);

        testClickInTable(false, false, false, true, 40);
        testClickInTable(false, true, false, true, 40);

        testClickInTable(false, true, false, true, 80);
    }

    @Test
    public void testFetchedArticlesBrowserMaxCount0() throws InvocationTargetException, InterruptedException {
        testFetchedArticles(true);
    }

    @Test
    public void testFetchedArticlesBrowserMaxCount2() throws InvocationTargetException, InterruptedException {
        testFetchedArticles(false);
    }

    private void testFetchedArticles(boolean zeroBackgroundBrowserMaxCount)
            throws InterruptedException, InvocationTargetException {
        String fetchedValue = "fetched";

        if (zeroBackgroundBrowserMaxCount) {
            Mockito.when(mockConfiguration.getBackgroundBrowserMaxCount()).thenReturn(0);
        }

        MainGui mainGui = new MainGui(mockManyBrowsersPanel);
        mainGui.setMainCallbacks(mockMainCallbacks);

        Mockito.when(mockConfiguration.useSpanTable()).thenReturn(true);
        Mockito.when(mockConfiguration.getFetchedValue()).thenReturn(fetchedValue);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(getSixTestArticles());

        waitForGuiTasks();

        JTable table = (JTable) findComponent(mockContentPane, JTable.class);
        assertNotNull(table);

        assertEquals(fetchedValue, table.getModel().getValueAt(2, 0));
    }

    @Test
    public void testShutdownApplication() throws InterruptedException, InvocationTargetException {
        MainGui mainGui = new MainGui(mockManyBrowsersPanel);
        mainGui.setMainCallbacks(mockMainCallbacks);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(new ArrayList<>());

        waitForGuiTasks();

        windowListener.windowClosing(new WindowEvent(mockFrame, WindowEvent.WINDOW_CLOSING));

        assertTrue(shutdownApplicationWasCalled);
    }

    private void testFilter(FilterTestType testType) throws BadLocationException, InterruptedException,
                                                            ReflectiveOperationException {
        MainGui mainGui = new MainGui(mockManyBrowsersPanel);
        mainGui.setMainCallbacks(mockMainCallbacks);

        Mockito.when(mockConfiguration.useSpanTable()).thenReturn(testType == CHANGE_TEXT);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(getSixTestArticles());

        waitForGuiTasks();

        JTable table = (JTable) findComponent(mockContentPane, JTable.class);
        assertNotNull(table);

        assertEquals(mockConfiguration.useSpanTable() ? 12 : 6, table.getRowCount());

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

    private List<Article> getSixTestArticles() {
        return Arrays.asList(
                new Article.Builder("u1", "s1", null, "Title1", null, "Text 1.")
                        .starred(true).read(true)
                        .build(),
                new Article.Builder("u2", "s2", null, "Title2", null, "Text 2.")
                        .build(),
                new Article.Builder("u3", "s3", null, "Title3", null, "Text 3.")
                        .starred(true).read(true).archived(true)
                        .build(),
                new Article.Builder("u4", "s4", null, "Title4", null, "Text 4.")
                        .build(),
                new Article.Builder("u5", "s5", null, "Title5", null, "Text 5.")
                        .build(),
                new Article.Builder("u6", "s6", null, "Title6", null, "Text 6.")
                        .build()
        );
    }

    private void checkArticlesInGui(FilterTestType testType, MainGui mainGui, int tableRowCount)
            throws IllegalAccessException {
        int expectedRowCount = mockConfiguration.useSpanTable() ? 2 : 1;

        if (testType == NO_MATCHES) {
            expectedRowCount = 0;
        } else if (testType == REMOVE_TEXT) {
            expectedRowCount = 5;
        }

        assertEquals(expectedRowCount, tableRowCount);

        Object filteredArticlesField = FieldUtils.readField(mainGui, "filteredArticles", true);

        assertTrue(filteredArticlesField instanceof List);
        List filteredArticles = (List) filteredArticlesField;

        if (testType == INSERT_TEXT) {
            assertEquals("u1", ((Article) filteredArticles.get(0)).getUrl());
        } else if (testType == CHANGE_TEXT) {
            assertEquals("u2", ((Article) filteredArticles.get(0)).getUrl());
        } else if (testType == REMOVE_TEXT) {
            IntStream.range(0, filteredArticles.size())
                    .forEach(index -> {
                        // Test article 3 (with index 2) is archived and should be filtered out: u1, u2, u4, u5, and u6.
                        String expectedUrl = "u" + (index + (index < 2 ? 1 : 2));
                        assertEquals(expectedUrl, ((Article) filteredArticles.get(index)).getUrl());
                    });
        }
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

    private void testClickInTable(boolean firstStarred, boolean firstRead, boolean spanTable, boolean selectRow,
                                  int xMouseEvent)
            throws InterruptedException, InvocationTargetException {
        List<Article> testArticles = getSixTestArticles();
        testArticles.get(0).setStarred(firstStarred);
        testArticles.get(0).setRead(firstRead);

        MainGui mainGui = new MainGui(mockManyBrowsersPanel);
        mainGui.setMainCallbacks(mockMainCallbacks);

        Mockito.when(mockConfiguration.useSpanTable()).thenReturn(spanTable);

        mainGui.initializeBackgroundBrowsersPanel(mockFrame, mockConfiguration);
        mainGui.initializeGui(testArticles);

        waitForGuiTasks();

        JTable table = (JTable) findComponent(mockContentPane, JTable.class);
        assertNotNull(table);

        if (selectRow) {
            table.getSelectionModel().setValueIsAdjusting(true);
            table.getSelectionModel().setSelectionInterval(1, 1);
            table.getSelectionModel().setValueIsAdjusting(true);
            table.getSelectionModel().setSelectionInterval(0, 0);
        } else {
            table.getSelectionModel().setValueIsAdjusting(true);
            table.clearSelection();
        }

        MouseEvent mouseEvent = new MouseEvent(table, 123456, new Date().getTime(), 0, xMouseEvent, 10,
                                               1, false);

        for (MouseListener mouseListener : table.getMouseListeners()) {
            mouseListener.mouseClicked(mouseEvent);
        }
    }


    enum FilterTestType {
        INSERT_TEXT,
        REMOVE_TEXT,
        CHANGE_TEXT,
        NO_MATCHES
    }
}
