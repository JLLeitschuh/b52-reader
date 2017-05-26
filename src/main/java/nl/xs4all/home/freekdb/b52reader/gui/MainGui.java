/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.gui.multispan.SpanArticleTableCellRenderer;
import nl.xs4all.home.freekdb.b52reader.gui.multispan.SpanCellTable;
import nl.xs4all.home.freekdb.b52reader.gui.multispan.SpanCellTableModel;
import nl.xs4all.home.freekdb.b52reader.main.MainCallbacks;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// todo: Embedded browser (JWebBrowser) does not resize when application window is resized after initial view?

/**
 * Main class responsible for the GUI.
 */
public class MainGui {
    /**
     * Icon for starred articles.
     */
    private static final Icon STARRED_ICON = Utilities.getIconResource("32x32-Full_Star_Yellow.png");

    /**
     * Icon for unstarred articles.
     */
    private static final Icon UNSTARRED_ICON = Utilities.getIconResource("32x32-Empty_Star.png");

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger(MainGui.class);

    /**
     * Handler for the callback functions of the main program.
     */
    private MainCallbacks mainCallbacks;

    /**
     * Current articles read from the configured article sources.
     */
    private List<Article> currentArticles;

    /**
     * Filtered articles: all articles from <code>currentArticles</code> that match the filter (all if filter is empty).
     */
    private List<Article> filteredArticles;

    /**
     * Selected article (from the article table).
     */
    private Article selectedArticle;

    /**
     * Article index for next article to load in the background.
     *
     * todo: Should we load articles from the filtered articles list (instead of from all current articles list).
     */
    private int backgroundArticleIndex;

    /**
     * Number of browsers loaded in the background.
     */
    private int backgroundBrowserCount;

    /**
     * Frame: the application window.
     */
    private JFrame frame;

    /**
     * Text field for filtering articles.
     */
    private JTextField filterTextField;

    /**
     * Table showing all (filtered) articles.
     */
    private JTable table;

    /**
     * Table model for the article table.
     */
    private TableModel tableModel;

    /**
     * Panel with many embedded browsers, of which only one can be visible.
     */
    private ManyBrowsersPanel manyBrowsersPanel;

    /**
     * Construct the main GUI object: set the main callbacks handler.
     *
     * @param mainCallbacks the main callbacks handler.
     */
    public MainGui(MainCallbacks mainCallbacks) {
        this.mainCallbacks = mainCallbacks;
    }

    /**
     * Create a minimal version of the GUI to be able to start the background tasks timer.
     *
     * @param currentArticles the list of current articles to show in the GUI.
     */
    public void initializeGui(List<Article> currentArticles) {
        this.currentArticles = currentArticles;
        this.filteredArticles = currentArticles;

        JPanel backgroundBrowsersPanel = new JPanel();
        backgroundBrowsersPanel.setVisible(false);
        ObjectHub.injectBackgroundBrowsersPanel(backgroundBrowsersPanel);

        frame = new JFrame(Constants.APPLICATION_NAME_AND_VERSION);
        frame.getContentPane().add(backgroundBrowsersPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Start a background timer to initialize and load some browsers in the background.
        backgroundBrowserCount = 0;
        backgroundArticleIndex = 1;
        Timer backgroundTasksTimer = new Timer(Constants.BACKGROUND_TIMER_DELAY, actionEvent -> handleBackgroundTasks());
        backgroundTasksTimer.start();

        frame.setBounds(Configuration.getFrameBounds());
        frame.setExtendedState(Configuration.getFrameExtendedState());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(this::finishGuiInitialization);
    }

    /**
     * Finish the initialization of the GUI. Make sure to call this method from the EDT (event dispatch thread), since
     * some of the  actions need to be performed from the EDT (like showing the first browser when creating the table).
     */
    private void finishGuiInitialization() {
        if (SwingUtilities.isEventDispatchThread()) {
            JPanel northPanel = new JPanel(new BorderLayout());
            northPanel.add(createFilterPanel(), BorderLayout.NORTH);

            manyBrowsersPanel = new ManyBrowsersPanel();

            table = Configuration.useSpanTable() ? createSpanTable(currentArticles) : createTable(currentArticles);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(10000, 200));
            northPanel.add(scrollPane, BorderLayout.CENTER);

            frame.getContentPane().add(northPanel, BorderLayout.NORTH);
            frame.getContentPane().add(manyBrowsersPanel, BorderLayout.CENTER);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    super.windowClosing(windowEvent);

                    frameClosing();
                }
            });
        } else {
            logger.error("The MainGui.finishGuiInitialization method should be called from the EDT (event dispatch " +
                         "thread).");
        }
    }

    /**
     * Create the panel with the filter field (for filtering articles).
     *
     * @return the panel with the filter field.
     */
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Filter:"));

        filterTextField = new JTextField("", 64);

        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                filterAndShowArticles();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                filterAndShowArticles();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                filterAndShowArticles();
            }
        });

        filterPanel.add(filterTextField);

        return filterPanel;
    }

    /**
     * Filter the articles and update the GUI.
     */
    private void filterAndShowArticles() {
        Article previousSelectedArticle = selectedArticle;

        filteredArticles = currentArticles.stream()
                .filter(new ArticleFilter(filterTextField.getText()))
                .filter(article -> !article.isArchived())
                .collect(Collectors.toList());

        if (Configuration.useSpanTable()) {
            tableModel = createSpanTableModel(filteredArticles);
            table.setModel(tableModel);
            setTableColumnWidths(table);
        } else {
            ((ArticlesTableModel) tableModel).setArticles(filteredArticles);
        }

        frame.setTitle(Constants.APPLICATION_NAME_AND_VERSION + " - " + (!filteredArticles.isEmpty() ? "1" : "0")
                       + "/" + filteredArticles.size());

        if (!filteredArticles.isEmpty()) {
            boolean selectFirstArticle = true;
            if (previousSelectedArticle != null) {
                int previousIndex = filteredArticles.indexOf(previousSelectedArticle);
                if (previousIndex != -1) {
                    //selectArticle(previousSelectedArticle, previousIndex);
                    table.getSelectionModel().setSelectionInterval(previousIndex, previousIndex);
                    selectFirstArticle = false;
                }
            }
            if (selectFirstArticle && table != null) {
                table.getSelectionModel().setSelectionInterval(0, 0);
            }
        }
    }

    /**
     * Create the GUI table with the custom article renderer and the corresponding data model.
     *
     * @param articles the (filtered) articles to show in the table.
     * @return the GUI table with the custom article renderer.
     */
    private JTable createTable(List<Article> articles) {
        ArticleTableCellRenderer.setDefaultBackgroundColor(frame.getBackground());

        tableModel = new ArticlesTableModel(articles);

        JTable table = new JTable(tableModel);
        table.setDefaultRenderer(Article.class, new ArticleTableCellRenderer());
        table.setRowHeight(42);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().setSelectionInterval(0, 0);

        table.setAutoCreateRowSorter(true);

        // todo: table.addKeyListener(new KeyboardShortcutHandler(this));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);

                handleTableClick(mouseEvent);
            }
        });

        table.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int selectedArticleIndex = getSelectedTableRow();

            if (selectedArticleIndex >= 0 && !listSelectionEvent.getValueIsAdjusting()) {
                Article selectedArticle = filteredArticles.get(selectedArticleIndex);
                selectArticle(selectedArticle, selectedArticleIndex);
            }
        });

        if (tableModel.getRowCount() > 0)
            selectArticle(filteredArticles.get(0), 0);

        return table;
    }

    /**
     * Create the GUI span table with the corresponding data model.
     *
     * @param articles the (filtered) articles to show in the table.
     * @return the GUI span table.
     */
    private JTable createSpanTable(List<Article> articles) {
        SpanArticleTableCellRenderer.setDefaultBackgroundColor(frame.getBackground());

        tableModel = createSpanTableModel(articles);

        JTable table = new SpanCellTable(tableModel);
        table.setDefaultRenderer(Object.class, new SpanArticleTableCellRenderer());
        table.setRowHeight(21);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        if (tableModel.getRowCount() > 0) {
            selectArticle(filteredArticles.get(0), 0);
        }

        table.getSelectionModel().setSelectionInterval(0, 0);
        setTableColumnWidths(table);

        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        // todo: Add table.addKeyListener(new KeyboardShortcutHandler(this));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                handleTableClick(mouseEvent);
            }
        });

        table.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int selectedArticleIndex = getSelectedTableRow();

            if (selectedArticleIndex >= 0 && !listSelectionEvent.getValueIsAdjusting()) {
                selectArticle(filteredArticles.get(selectedArticleIndex), selectedArticleIndex);
            }
        });

        return table;
    }

    /**
     * Create the span table model.
     *
     * @param articles the (filtered) articles to put in the table model.
     * @return the GUI span table model.
     */
    private TableModel createSpanTableModel(List<Article> articles) {
        List<String> columnIdentifiers = Arrays.asList("fetched", "starred", "read", "title", "author", "date/time");
        //int[] columnIndices1 = {0, 1, 2};
        int[] columnIndices2 = {3, 4, 5};

        // todo: Base the ArticleSpanTableModel/SpanCellTableModel on AbstractTableModel (like the ArticlesTableModel)?
        SpanCellTableModel spanTableModel = new SpanCellTableModel(articles, columnIdentifiers.size());

        Vector<Vector<Object>> data = new Vector<>();
        articles.forEach(article -> {
            data.add(listToVector(Arrays.asList(
                    "",
                    article.isStarred() ? STARRED_ICON : UNSTARRED_ICON,
                    article.isRead() ? "" : "U",
                    article.getTitle(),
                    article.getAuthor(),
                    Constants.DATE_TIME_FORMAT_LONGER.format(article.getDateTime())
            )));

            data.add(listToVector(Arrays.asList("", "", "", article.getText())));
        });

        spanTableModel.setDataVector(data, listToVector(columnIdentifiers));

        for (int rowIndex = 1; rowIndex < data.size(); rowIndex += 2) {
            //spanTableModel.getTableSpans().combine(new int[]{rowIndex}, columnIndices1);
            spanTableModel.getTableSpans().combine(new int[]{rowIndex}, columnIndices2);
        }

        return spanTableModel;
    }

    /**
     * Convert a list to a vector.
     *
     * @param list the list to convert.
     * @param <T> the type of list items.
     * @return the vector with the same items as are in the list.
     */
    private <T> Vector<T> listToVector(List<T> list) {
        return new Vector<>(list);
    }

    /**
     * Set the column widths for the specified table.
     *
     * @param table the GUI table with the articles.
     */
    private void setTableColumnWidths(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();

        for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
            columnModel.getColumn(columnIndex).setPreferredWidth(columnIndex <= 2 ? 60 : 800);
        }
    }

    /**
     * Handle a click event in the GUI table: show the selected article and/or toggle the starred & read fields.
     *
     * @param mouseEvent the mouse event (to determine the column index).
     */
    private void handleTableClick(MouseEvent mouseEvent) {
        int selectedArticleIndex = getSelectedTableRow();
        Article clickedArticle = selectedArticleIndex != -1 ? filteredArticles.get(selectedArticleIndex) : null;

        if (clickedArticle != null) {
            int columnIndex = table.columnAtPoint(mouseEvent.getPoint());
            boolean updateArticleList = false;

            if (columnIndex == 1) {
                clickedArticle.setStarred(!clickedArticle.isStarred());
                updateArticleList = true;
            } else if (columnIndex == 2) {
                clickedArticle.setRead(!clickedArticle.isRead());
                updateArticleList = true;
            }

            if (updateArticleList) {
                // todo: Keep selection and scroll location if possible.
                filterAndShowArticles();
            }
        }
    }

    /**
     * Get the selected table row, with an adjustment for span cell tables if necessary (divided by two).
     *
     * @return the selected table row.
     */
    private int getSelectedTableRow() {
        return table.getSelectedRow() / (tableModel instanceof SpanCellTableModel ? 2 : 1);
    }

    /**
     * Select a specific article and show the embedded browser with that article.
     *
     * @param article the article to select.
     * @param articleIndex the index of the article (to show in the window title).
     */
    private void selectArticle(Article article, int articleIndex) {
        String articleCounterAndSize = (articleIndex + 1) + "/" + filteredArticles.size();
        frame.setTitle(Constants.APPLICATION_NAME_AND_VERSION + " - " + articleCounterAndSize);

        selectedArticle = article;

        manyBrowsersPanel.showBrowser(selectedArticle.getUrl(), true);
    }

    /**
     * Handle background tasks: create an embedded browser (if we do not have created the maximum number of browsers
     * yet).
     */
    private void handleBackgroundTasks() {
        if (backgroundBrowserCount < Constants.BACKGROUND_BROWSER_MAX_COUNT &&
            backgroundArticleIndex < currentArticles.size()) {

            String url = currentArticles.get(backgroundArticleIndex).getUrl();

            if (!manyBrowsersPanel.hasBrowserForUrl(url)) {
                logger.debug("Background: prepare browser " + (backgroundBrowserCount + 1) + ".");
                manyBrowsersPanel.showBrowser(url, false);
                backgroundBrowserCount++;
            }

            backgroundArticleIndex++;
        }
    }

    /**
     * Handle the frame closing event: shutdown the application.
     */
    private void frameClosing() {
        manyBrowsersPanel.disposeAllBrowsers();

        mainCallbacks.shutdownApplication(frame.getExtendedState(), frame.getBounds());
    }
}
