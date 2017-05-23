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

// todo: Add Javadocs.

// todo: Embedded browser (JWebBrowser) does not resize when application window is resized after initial view?

/**
 * Main class responsible for the GUI.
 */
public class MainGui {
    private static final Icon STARRED_ICON = Utilities.getIconResource("32x32-Full_Star_Yellow.png");
    private static final Icon UNSTARRED_ICON = Utilities.getIconResource("32x32-Empty_Star.png");

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger(MainGui.class);

    private MainCallbacks mainCallbacks;

    private List<Article> currentArticles;
    private List<Article> filteredArticles;
    private Article selectedArticle;
    private int backgroundArticleIndex;
    private int backgroundBrowserCount;

    private JFrame frame;
    private JTextField filterTextField;
    private JTable table;
    private TableModel tableModel;
    private ManyBrowsersPanel manyBrowsersPanel;

    public MainGui(MainCallbacks mainCallbacks) {
        this.mainCallbacks = mainCallbacks;
    }

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

        SwingUtilities.invokeLater(() -> finishGuiInitialization(currentArticles));
    }

    // Some of the following actions need to be performed on the EDT (like showing the first browser when creating
    // the table).
    private void finishGuiInitialization(List<Article> currentArticles) {
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
    }

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

    private void filterAndShowArticles() {
        Article previousSelectedArticle = selectedArticle;

        filteredArticles = currentArticles.stream()
                .filter(filterTextField != null ? new ArticleFilter(filterTextField.getText()) : article -> true)
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

    private <T> Vector<T> listToVector(List<T> list) {
        return new Vector<>(list);
    }

    private void setTableColumnWidths(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
            columnModel.getColumn(columnIndex).setPreferredWidth(columnIndex <= 2 ? 60 : 800);
        }
    }

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

    private void selectArticle(Article article, int articleIndex) {
        String articleCounterAndSize = (articleIndex + 1) + "/" + filteredArticles.size();
        frame.setTitle(Constants.APPLICATION_NAME_AND_VERSION + " - " + articleCounterAndSize);

        selectedArticle = article;

        manyBrowsersPanel.showBrowser(selectedArticle.getUrl(), true);
    }

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

    private void frameClosing() {
        manyBrowsersPanel.disposeAllBrowsers();

        mainCallbacks.shutdownApplication(frame.getExtendedState(), frame.getBounds());
    }
}
