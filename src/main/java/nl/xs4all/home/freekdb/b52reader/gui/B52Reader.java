/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.testdata.TestData;

public class B52Reader {
    private static final String APPLICATION_NAME_AND_VERSION = "B52 reader 0.0.6";

    private List<Article> articles;
    private List<Article> filteredArticles;
    private Article selectedArticle;

    private JFrame frame;
    private JTextField filterTextField;
    private JTable table;
    private ArticlesTableModel tableModel;
    private JPanel selectedArticlePanel;

    public static void main(String[] arguments) {
        SwingUtilities.invokeLater(() -> new B52Reader().createAndShowGui());
    }

    private void createAndShowGui() {
        articles = TestData.getTestArticles();
        filteredArticles = articles;

        frame = new JFrame(APPLICATION_NAME_AND_VERSION);
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createFilterPanel(), BorderLayout.NORTH);

        JTable table = createTable(articles);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(10000, 200));
        northPanel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(northPanel, BorderLayout.NORTH);
        frame.setVisible(true);
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

        filteredArticles = articles.stream()
                .filter(filterTextField != null ? new ArticleFilter(filterTextField.getText()) : article -> true)
                .filter(article -> !article.isArchived())
                .collect(Collectors.toList());

        tableModel.setArticles(filteredArticles);

        frame.setTitle(APPLICATION_NAME_AND_VERSION + " - " + (filteredArticles.size() > 0 ? "1" : "0")
                       + "/" + filteredArticles.size());

        if (filteredArticles.size() > 0) {
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

        table = new JTable(tableModel);
        table.setDefaultRenderer(Article.class, new ArticleTableCellRenderer());
        table.setRowHeight(42);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().setSelectionInterval(0, 0);

        // todo: table.addKeyListener(new KeyboardShortcutHandler(this));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int selectedArticleIndex = table.getSelectedRow();
                Article selectedArticle = selectedArticleIndex != -1 ? filteredArticles.get(selectedArticleIndex) : null;

                if (selectedArticle != null) {
                    boolean updateArticleList = false;

                    // todo: Get rid of these magic numbers (36 and 60) below.
                    if (mouseEvent.getX() < 36) {
                        selectedArticle.setStarred(!selectedArticle.isStarred());
                        updateArticleList = true;
                    } else if (mouseEvent.getX() < 60) {
                        selectedArticle.setRead(!selectedArticle.isRead());
                        updateArticleList = true;
                    }

                    if (updateArticleList) {
                        // todo: Keep selection and scroll location if possible.
                        filterAndShowArticles();
                    }
                }
            }
        });

        table.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int selectedArticleIndex = table.getSelectedRow();

            if (selectedArticleIndex >= 0 && !listSelectionEvent.getValueIsAdjusting()) {
                Article selectedArticle = filteredArticles.get(selectedArticleIndex);
                selectArticle(selectedArticle, selectedArticleIndex);
            }
        });

        if (tableModel.getRowCount() > 0)
            selectArticle(filteredArticles.get(0), 0);

        return table;
    }

    private void selectArticle(Article article, int articleIndex) {
        frame.setTitle(APPLICATION_NAME_AND_VERSION + " - " +
                       + (articleIndex + 1) + "/" + filteredArticles.size());

        selectedArticle = article;

        // todo: Show the article in an embedded browser!

        // For now, we simply show a panel with the article's URL.
        if (selectedArticlePanel != null) {
            frame.getContentPane().remove(selectedArticlePanel);
        }

        selectedArticlePanel = new JPanel();
        selectedArticlePanel.add(new JLabel(article.getUrl()));
        frame.getContentPane().add(selectedArticlePanel, BorderLayout.CENTER);
        frame.getContentPane().validate();
    }
}
