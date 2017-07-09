/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import com.google.common.collect.ImmutableMap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

public class ArticleTableCellRenderer extends JPanel implements TableCellRenderer {
    private static final Font BOLD_FONT = new Font("Calibri", Font.BOLD, 14);
    private static final Font REGULAR_FONT = new Font("Calibri", Font.PLAIN, 14);

    private static final Map<String, Color> COLOR_MAP = ImmutableMap.of(
            "nrc", new Color(144, 238, 144),
            "test", Color.ORANGE
    );

    private static Color defaultBackgroundColor;

    private JLabel starredLabel;
    private JLabel readLabel;
    private JLabel titleLabel;
    private JLabel likesLabel;
    private JLabel authorLabel;
    private JLabel dateTimeLabel;
    private JLabel textLabel;

    // todo: Use layout managers?
    public ArticleTableCellRenderer() {
        super(null);

        int starredX = 4;
        int starredWidth = 36;
        int readX = starredX + starredWidth;
        int readWidth = 24;
        int titleX = readX + readWidth;
        int titleWidth = 400;
        int likesX = titleX + titleWidth + 154;
        int likesWidth = 38;
        int authorX = likesX + likesWidth + 2;
        int authorWidth = 160;
        int dateTimeX = authorX + authorWidth;
        int dateTimeWidth = 120;

        starredLabel = new JLabel();
        starredLabel.setBounds(starredX, 3, starredWidth, 40);
        starredLabel.setFont(BOLD_FONT);
        add(starredLabel);

        readLabel = new JLabel();
        readLabel.setBounds(readX, 3, readWidth, 40);
        readLabel.setFont(BOLD_FONT);
        add(readLabel);

        titleLabel = new JLabel();
        titleLabel.setBounds(titleX, 3, titleWidth, 18);
        titleLabel.setFont(BOLD_FONT);
        add(titleLabel);

        likesLabel = new JLabel();
        likesLabel.setBounds(likesX, 3, likesWidth, 18);
        likesLabel.setFont(REGULAR_FONT);
        add(likesLabel);

        authorLabel = new JLabel();
        authorLabel.setBounds(authorX, 3, authorWidth, 18);
        authorLabel.setFont(BOLD_FONT);
        add(authorLabel);

        dateTimeLabel = new JLabel();
        dateTimeLabel.setBounds(dateTimeX, 3, dateTimeWidth, 18);
        dateTimeLabel.setFont(REGULAR_FONT);
        add(dateTimeLabel);

        textLabel = new JLabel();
        textLabel.setBounds(titleX, 20, 1600, 24);
        textLabel.setFont(REGULAR_FONT);
        add(textLabel);
    }

    static void setDefaultBackgroundColor(Color defaultBackgroundColor) {
        ArticleTableCellRenderer.defaultBackgroundColor = defaultBackgroundColor;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Article article = (Article) value;

        this.setBackground(isSelected ? Constants.NICE_LIGHT_BLUE : getBackgroundColor(article.getSourceId()));

        String iconFileName = "32x32-" + (article.isStarred() ? "Full_Star_Yellow" : "Empty_Star") + ".png";
        starredLabel.setIcon(Utilities.getIconResource(iconFileName));

        readLabel.setVisible(!article.isRead());
        readLabel.setText(article.isRead() ? "R" : "U");
        titleLabel.setText(article.getTitle());
        likesLabel.setText("+" + article.getLikes());
        authorLabel.setText(article.getAuthor() != null ? article.getAuthor().getName() : "");

        dateTimeLabel.setText(article.getDateTime() != null
                                      ? Constants.DATE_TIME_FORMAT_LONGER.format(article.getDateTime())
                                      : "");

        textLabel.setText(article.getText());

        return this;
    }

    private Color getBackgroundColor(String sourceId) {
        return COLOR_MAP.getOrDefault(sourceId, defaultBackgroundColor);
    }
}
