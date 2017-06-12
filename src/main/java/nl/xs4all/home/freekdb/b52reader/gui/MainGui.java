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
import java.util.Objects;
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
import nl.xs4all.home.freekdb.b52reader.model.Author;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// todo: Embedded browser (JWebBrowser) does not resize when application window is resized after initial view?

/**
 * Main class responsible for the GUI.
 */
public class MainGui {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

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
     * <p>
     * todo: Should we load articles from the filtered articles list (instead of from all current articles list)?
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
     * Configuration object with data from the configuration file.
     */
    private Configuration configuration;

    /**
     * Construct the main GUI object: set the main callbacks handler.
     *
     * @param manyBrowsersPanel the panel with many embedded browsers, of which only one can be visible.
     * @param mainCallbacks the main callbacks handler.
     */
    public MainGui(ManyBrowsersPanel manyBrowsersPanel, MainCallbacks mainCallbacks) {
        this.manyBrowsersPanel = manyBrowsersPanel;
        this.mainCallbacks = mainCallbacks;
    }

    /**
     * Make sure the background browsers functionality is initialized before fetching articles, since for some article
     * sources a background browser is used to retrieve the list of articles.
     *
     * @param frame         the application frame that will contain the GUI.
     * @param configuration the application configuration.
     */
    public void initializeBackgroundBrowsersPanel(JFrame frame, Configuration configuration) {
        JPanel backgroundBrowsersPanel = new JPanel();
        backgroundBrowsersPanel.setVisible(false);
        ObjectHub.injectBackgroundBrowsersPanel(backgroundBrowsersPanel);

        this.frame = frame;
        this.frame.setTitle(Constants.APPLICATION_NAME_AND_VERSION);
        this.frame.getContentPane().add(backgroundBrowsersPanel, BorderLayout.SOUTH);
        this.frame.setVisible(true);

        this.configuration = configuration;
    }

    /**
     * Create a minimal version of the GUI to be able to start the background tasks timer.
     *
     * @param articles the list of current articles to show in the GUI.
     */
    public void initializeGui(List<Article> articles) {
        this.currentArticles = articles;
        this.filteredArticles = articles;

        // Start a background timer to initialize and load some browsers in the background.
        backgroundBrowserCount = 0;
        backgroundArticleIndex = 1;
        Timer backgroundTasksTimer = new Timer(Constants.BACKGROUND_TIMER_DELAY, actionEvent -> handleBackgroundTasks());
        backgroundTasksTimer.setInitialDelay(800);
        backgroundTasksTimer.start();

        frame.setBounds(configuration.getFrameBounds());
        frame.setExtendedState(configuration.getFrameExtendedState());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(this::finishGuiInitialization);
    }

    /**
     * Finish the initialization of the GUI. Make sure to call this method from the EDT (event dispatch thread), since
     * some of the  actions need to be performed from the EDT (like showing the first browser when creating the table).
     */
    private void finishGuiInitialization() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createFilterPanel(), BorderLayout.NORTH);

        table = configuration.useSpanTable() ? createSpanTable(currentArticles) : createCustomRendererTable(currentArticles);

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
        Article previouslySelectedArticle = selectedArticle;

        filteredArticles = currentArticles.stream()
                .filter(new ArticleFilter(filterTextField.getText()))
                .filter(article -> !article.isArchived())
                .collect(Collectors.toList());

        if (configuration.useSpanTable()) {
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

            int previousIndex = filteredArticles.indexOf(previouslySelectedArticle);
            if (previousIndex != -1) {
                table.getSelectionModel().setSelectionInterval(previousIndex, previousIndex);
                selectFirstArticle = false;
            }

            if (selectFirstArticle) {
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
    private JTable createCustomRendererTable(List<Article> articles) {
        ArticleTableCellRenderer.setDefaultBackgroundColor(frame.getBackground());

        tableModel = new ArticlesTableModel(articles);

        JTable customRendererTable = new JTable(tableModel);
        customRendererTable.setDefaultRenderer(Article.class, new ArticleTableCellRenderer());
        customRendererTable.setRowHeight(42);
        customRendererTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customRendererTable.getSelectionModel().setSelectionInterval(0, 0);

        customRendererTable.setAutoCreateRowSorter(true);

        // todo: Add customRendererTable.addKeyListener(new KeyboardShortcutHandler(this));

        customRendererTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);

                handleTableClick(mouseEvent);
            }
        });

        customRendererTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int selectedArticleIndex = getSelectedArticleIndex();

            if (selectedArticleIndex >= 0 && !listSelectionEvent.getValueIsAdjusting()) {
                Article article = filteredArticles.get(selectedArticleIndex);
                selectArticle(article, selectedArticleIndex);
            }
        });

        if (tableModel.getRowCount() > 0) {
            selectArticle(filteredArticles.get(0), 0);
        }

        return customRendererTable;
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

        JTable spanTable = new SpanCellTable(tableModel);
        spanTable.setDefaultRenderer(Object.class, new SpanArticleTableCellRenderer());
        spanTable.setRowHeight(21);
        spanTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        spanTable.setAutoCreateRowSorter(true);

        if (tableModel.getRowCount() > 0) {
            selectArticle(filteredArticles.get(0), 0);
        }

        spanTable.getSelectionModel().setSelectionInterval(0, 0);
        setTableColumnWidths(spanTable);

        spanTable.setPreferredScrollableViewportSize(spanTable.getPreferredSize());

        // todo: Add spanTable.addKeyListener(new KeyboardShortcutHandler(this));

        spanTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);

                handleTableClick(mouseEvent);
            }
        });

        spanTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int selectedArticleIndex = getSelectedArticleIndex();

            if (selectedArticleIndex >= 0 && !listSelectionEvent.getValueIsAdjusting()) {
                selectArticle(filteredArticles.get(selectedArticleIndex), selectedArticleIndex);
            }
        });

        return spanTable;
    }

    /**
     * Create the span table model.
     *
     * @param articles the (filtered) articles to put in the table model.
     * @return the GUI span table model.
     */
    private TableModel createSpanTableModel(List<Article> articles) {
        List<String> columnNames = Arrays.asList("fetched", "starred", "read", "title", "author", "date/time");

        List<Class<?>> columnClasses = Arrays.asList(
                String.class, Icon.class, String.class, String.class, Author.class, String.class
        );

        //int[] columnIndices1 = {0, 1, 2
        int[] columnIndices2 = {3, 4, 5};

        // todo: Base the ArticleSpanTableModel/SpanCellTableModel on AbstractTableModel (like the ArticlesTableModel)?
        SpanCellTableModel spanTableModel = new SpanCellTableModel(articles, columnNames.size());

        spanTableModel.setColumnsAndData(columnNames, columnClasses, articles,
                                         article -> manyBrowsersPanel.hasBrowserForUrl(article.getUrl()));

        for (int rowIndex = 1; rowIndex < 2 * articles.size(); rowIndex += 2) {
            //spanTableModel.getTableSpans().combine(new int[]{rowIndex}, columnIndices1)
            spanTableModel.getTableSpans().combine(new int[]{rowIndex}, columnIndices2);
        }

        return spanTableModel;
    }

    /**
     * Set the column widths for the specified table.
     *
     * @param table the GUI table with the articles.
     */
    private void setTableColumnWidths(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();

        for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
            columnModel.getColumn(columnIndex).setPreferredWidth(columnIndex <= 2 ? 100 : 800);
        }
    }

    /**
     * Handle a click event in the GUI table: show the selected article and/or toggle the starred & read fields.
     *
     * @param mouseEvent the mouse event (to determine the column index).
     */
    private void handleTableClick(MouseEvent mouseEvent) {
        int selectedArticleIndex = getSelectedArticleIndex();

        if (selectedArticleIndex != -1) {
            Article clickedArticle = filteredArticles.get(selectedArticleIndex);
            int columnIndex = getColumnIndexFromClick(mouseEvent);
            boolean updateArticleList = false;

            if (columnIndex == 1) {
                clickedArticle.setStarred(!clickedArticle.isStarred());
                updateArticleList = true;
            } else if (columnIndex == 2) {
                clickedArticle.setRead(!clickedArticle.isRead());
                updateArticleList = true;
            }

            if (updateArticleList) {
                filterAndShowArticles();
            }
        }
    }

    /**
     * Get the selected article index, with an adjustment for span cell tables if necessary (divided by two).
     *
     * @return the selected article index.
     */
    private int getSelectedArticleIndex() {
        int selectedArticleIndex = -1;

        if (table.getSelectedRow() != -1) {
            selectedArticleIndex = table.getSelectedRow() / (tableModel instanceof SpanCellTableModel ? 2 : 1);
        }

        return selectedArticleIndex;
    }

    /**
     * Get the column index corresponding to a mouse click event.
     *
     * @param mouseEvent the related mouse event.
     * @return the column index corresponding to the mouse click event.
     */
    private int getColumnIndexFromClick(MouseEvent mouseEvent) {
        int columnIndex;

        if (configuration.useSpanTable()) {
            columnIndex = table.columnAtPoint(mouseEvent.getPoint());
        } else {
            // todo: Get rid of these magic numbers (36 and 60) below.
            if (mouseEvent.getX() < 36) {
                columnIndex = 1;
            } else if (mouseEvent.getX() < 60) {
                columnIndex = 2;
            } else {
                columnIndex = 3;
            }
        }

        return columnIndex;
    }

    /**
     * Select a specific article and show the embedded browser with that article.
     *
     * @param article      the article to select.
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
        logger.debug("Handle background tasks.");

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

        if (configuration.useSpanTable()) {
            logger.debug("Check fetched status for {} rows.", tableModel.getRowCount() / 2);

            for (int rowIndex = 0; rowIndex < tableModel.getRowCount() / 2; rowIndex++) {
                if (manyBrowsersPanel.hasBrowserForUrl(currentArticles.get(rowIndex).getUrl()) &&
                    Objects.equals(tableModel.getValueAt(rowIndex * 2, 0), "")) {
                    tableModel.setValueAt(Constants.FETCHED_VALUE, rowIndex * 2, 0);
                    logger.debug("Set column 0 for row {} to fetched.", rowIndex);
                }
            }
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
