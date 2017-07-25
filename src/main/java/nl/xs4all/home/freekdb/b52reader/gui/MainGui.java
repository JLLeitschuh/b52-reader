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
import java.util.function.Predicate;
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

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.gui.multispan.SpanArticleTableCellRenderer;
import nl.xs4all.home.freekdb.b52reader.gui.multispan.SpanCellTable;
import nl.xs4all.home.freekdb.b52reader.gui.multispan.SpanCellTableModel;
import nl.xs4all.home.freekdb.b52reader.main.MainCallbacks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// todo: Embedded browser (JWebBrowser) does not resize when application window is resized after initial view?

/**
 * Main class responsible for the GUI.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
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
     * Article index for next article to load in the background. Currently we load articles from the full list with all
     * articles, but we could change that to loading articles from the filtered list.
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
     * Panel for the background browsers.
     */
    private JPanel backgroundBrowsersPanel;

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
     */
    public MainGui(final ManyBrowsersPanel manyBrowsersPanel) {
        this.manyBrowsersPanel = manyBrowsersPanel;
    }

    /**
     * Set the handler for the callback functions of the main program.
     *
     * @param mainCallbacks the handler for the callback functions of the main program.
     */
    public void setMainCallbacks(final MainCallbacks mainCallbacks) {
        this.mainCallbacks = mainCallbacks;
    }

    /**
     * Make sure the background browsers functionality is initialized before fetching articles, since for some article
     * sources a background browser is used to retrieve the list of articles.
     *
     * @param frame         the application frame that will contain the GUI.
     * @param configuration the application configuration.
     */
    public void initializeBackgroundBrowsersPanel(final JFrame frame, final Configuration configuration) {
        this.backgroundBrowsersPanel = new JPanel();
        this.backgroundBrowsersPanel.setVisible(false);

        this.frame = frame;
        this.frame.setTitle(configuration.getApplicationNameAndVersion());
        this.frame.getContentPane().add(this.backgroundBrowsersPanel, BorderLayout.SOUTH);
        this.frame.setVisible(true);

        this.configuration = configuration;
    }

    /**
     * Get the panel for the background browsers.
     *
     * @return the panel for the background browsers.
     */
    public JPanel getBackgroundBrowsersPanel() {
        return backgroundBrowsersPanel;
    }

    /**
     * Create a minimal version of the GUI to be able to start the background tasks timer.
     *
     * @param articles the list of current articles to show in the GUI.
     */
    public void initializeGui(final List<Article> articles) {
        this.currentArticles = articles;
        this.filteredArticles = articles;

        // Start a background timer to initialize and load some browsers in the background.
        backgroundBrowserCount = 0;
        backgroundArticleIndex = 1;

        final Timer backgroundTasksTimer
            = new Timer(configuration.getBackgroundTimerDelay(), actionEvent -> handleBackgroundTasks());

        backgroundTasksTimer.setInitialDelay(configuration.getBackgroundTimerInitialDelay());
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
        final JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createFilterPanel(), BorderLayout.NORTH);

        table = configuration.useSpanTable() ? createSpanTable(currentArticles) : createCustomRendererTable(currentArticles);

        final int tableWidth = 10000;
        final int tableHeight = 200;
        final JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(tableWidth, tableHeight));
        northPanel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(northPanel, BorderLayout.NORTH);
        frame.getContentPane().add(manyBrowsersPanel, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent windowEvent) {
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
        final JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Filter:"));

        final int filterWidthInColumns = 64;
        filterTextField = new JTextField("", filterWidthInColumns);

        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent documentEvent) {
                filterAndShowArticles();
            }

            @Override
            public void removeUpdate(final DocumentEvent documentEvent) {
                filterAndShowArticles();
            }

            @Override
            public void changedUpdate(final DocumentEvent documentEvent) {
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
        final Article previouslySelectedArticle = selectedArticle;

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

        updateFrameTitle(filteredArticles.isEmpty() ? -1 : 0);

        if (!filteredArticles.isEmpty()) {
            boolean selectFirstArticle = true;

            final int previousIndex = filteredArticles.indexOf(previouslySelectedArticle);
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
     * Update frame title to show application name, version, selected article (one based), and filtered article count.
     *
     * @param articleIndex (zero based) article index to show in frame title.
     */
    private void updateFrameTitle(final int articleIndex) {
        frame.setTitle(String.format("%s - %d/%d", configuration.getApplicationNameAndVersion(),
                                     articleIndex + 1, filteredArticles.size()));
    }

    /**
     * Create the GUI table with the custom article renderer and the corresponding table model.
     *
     * @param articles the (filtered) articles to show in the table.
     * @return the GUI table with the custom article renderer.
     */
    private JTable createCustomRendererTable(final List<Article> articles) {
        ArticleTableCellRenderer.setDefaultBackgroundColor(frame.getBackground());

        tableModel = new ArticlesTableModel(articles);

        final int rowHeight = 42;
        final JTable customRendererTable = new JTable(tableModel);
        customRendererTable.setDefaultRenderer(Article.class, new ArticleTableCellRenderer(configuration));
        customRendererTable.setRowHeight(rowHeight);
        customRendererTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customRendererTable.getSelectionModel().setSelectionInterval(0, 0);

        customRendererTable.setAutoCreateRowSorter(true);

        // todo: Add customRendererTable.addKeyListener(new KeyboardShortcutHandler(this));

        customRendererTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);

                handleTableClick(mouseEvent);
            }
        });

        customRendererTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            final int selectedArticleIndex = getSelectedArticleIndex();

            if (selectedArticleIndex >= 0 && !listSelectionEvent.getValueIsAdjusting()) {
                final Article article = filteredArticles.get(selectedArticleIndex);
                selectArticle(article, selectedArticleIndex);
            }
        });

        if (tableModel.getRowCount() > 0) {
            selectArticle(filteredArticles.get(0), 0);
        }

        return customRendererTable;
    }

    /**
     * Create the GUI span table with the corresponding table model.
     *
     * @param articles the (filtered) articles to show in the table.
     * @return the GUI span table.
     */
    private JTable createSpanTable(final List<Article> articles) {
        SpanArticleTableCellRenderer.setDefaultBackgroundColor(frame.getBackground());

        tableModel = createSpanTableModel(articles);

        final int rowHeight = 21;
        final JTable spanTable = new SpanCellTable(tableModel);
        spanTable.setDefaultRenderer(Object.class, new SpanArticleTableCellRenderer(configuration));
        spanTable.setRowHeight(rowHeight);
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
            public void mouseClicked(final MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);

                handleTableClick(mouseEvent);
            }
        });

        spanTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            final int selectedArticleIndex = getSelectedArticleIndex();

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
    private TableModel createSpanTableModel(final List<Article> articles) {
        final List<String> columnNames = Arrays.asList("fetched", "starred", "read", "title", "author", "date/time");

        final List<Class<?>> columnClasses = Arrays.asList(
            String.class, Icon.class, String.class, String.class, Author.class, String.class
        );

        final int[] columnIndices2 = {3, 4, 5};

        // todo: Base the ArticleSpanTableModel/SpanCellTableModel on AbstractTableModel (like the ArticlesTableModel)?
        final SpanCellTableModel spanTableModel = new SpanCellTableModel(articles, columnNames.size(), configuration);

        final Predicate<Article> isFetched = article -> manyBrowsersPanel.hasBrowserForUrl(article.getUrl());
        spanTableModel.setColumnsAndData(columnNames, columnClasses, articles, isFetched);

        for (int rowIndex = 1; rowIndex < 2 * articles.size(); rowIndex += 2) {
            spanTableModel.getTableSpans().combine(new int[]{rowIndex}, columnIndices2);
        }

        return spanTableModel;
    }

    /**
     * Set the column widths for the specified table.
     *
     * @param table the GUI table with the articles.
     */
    private void setTableColumnWidths(final JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();

        final int columnWidthFlag = 100;
        final int columnWidthRegular = 800;
        for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
            columnModel.getColumn(columnIndex).setPreferredWidth(columnIndex <= 2 ? columnWidthFlag : columnWidthRegular);
        }
    }

    /**
     * Handle a click event in the GUI table: show the selected article and/or toggle the starred & read fields.
     *
     * @param mouseEvent the mouse event (to determine the column index).
     */
    private void handleTableClick(final MouseEvent mouseEvent) {
        final int selectedArticleIndex = getSelectedArticleIndex();

        if (selectedArticleIndex != -1) {
            final Article clickedArticle = filteredArticles.get(selectedArticleIndex);
            final int columnIndex = getColumnIndexFromClick(mouseEvent);
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
    private int getColumnIndexFromClick(final MouseEvent mouseEvent) {
        final int columnIndex;

        if (configuration.useSpanTable()) {
            columnIndex = table.columnAtPoint(mouseEvent.getPoint());
        } else {
            columnIndex = ArticleTableCellRenderer.calculateColumnIndex(mouseEvent.getX());
        }

        return columnIndex;
    }

    /**
     * Select a specific article and show the embedded browser with that article.
     *
     * @param article      the article to select.
     * @param articleIndex the index of the article (to show in the window title).
     */
    private void selectArticle(final Article article, final int articleIndex) {
        updateFrameTitle(articleIndex);

        selectedArticle = article;

        manyBrowsersPanel.showBrowser(selectedArticle.getUrl(), true);
    }

    /**
     * Handle background tasks: create an embedded browser (if we do not have created the maximum number of browsers
     * yet).
     */
    private void handleBackgroundTasks() {
        logger.debug("Handle background tasks.");

        if (backgroundBrowserCount < configuration.getBackgroundBrowserMaxCount()
            && backgroundArticleIndex < currentArticles.size()) {

            final String url = currentArticles.get(backgroundArticleIndex).getUrl();

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
                if (fetchedShouldBeSet(rowIndex)) {
                    logger.debug("Set column 0 for row {} to fetched.", rowIndex);
                    tableModel.setValueAt(configuration.getFetchedValue(), rowIndex * 2, 0);
                }
            }
        }
    }

    /**
     * Check whether the fetched value should be set for this row: there is a browser for the URL and the fetched cell
     * is empty.
     *
     * @param rowIndex the row index for which to check.
     * @return whether the fetched value should be set
     */
    private boolean fetchedShouldBeSet(final int rowIndex) {
        return manyBrowsersPanel.hasBrowserForUrl(currentArticles.get(rowIndex).getUrl())
               && Objects.equals(tableModel.getValueAt(rowIndex * 2, 0), "");
    }

    /**
     * Handle the frame closing event: shutdown the application.
     */
    private void frameClosing() {
        manyBrowsersPanel.disposeAllBrowsers();

        mainCallbacks.shutdownApplication(frame.getExtendedState(), frame.getBounds());
    }
}
