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

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

/**
 * Custom renderer for the GUI table that shows each article in a row.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class ArticleTableCellRenderer extends JPanel implements TableCellRenderer {
    /**
     * Name of the font.
     */
    private static final String FONT_NAME = "Calibri";

    /**
     * Bold font for title and author.
     */
    private static final Font BOLD_FONT = new Font(FONT_NAME, Font.BOLD, 14);

    /**
     * Regular font for likes, date/time, and text.
     */
    private static final Font REGULAR_FONT = new Font(FONT_NAME, Font.PLAIN, 14);

    /**
     * Map with background colors for specific source ids.
     */
    private static final Map<String, Color> BACKGROUND_COLOR_MAP = ImmutableMap.of(
            Constants.NRC_SOURCE_ID, new Color(144, 238, 144),
            Constants.TEST_SOURCE_ID, Color.ORANGE
    );

    /**
     * Width of the starred column.
     */
    private static final int STARRED_COLUMN_WIDTH = 36;

    /**
     * Width of the read column.
     */
    private static final int READ_COLUMN_WIDTH = 24;

    /**
     * Default background color for this renderer.
     */
    private static Color defaultBackgroundColor;

    /**
     * Starred/unstarred icon label.
     */
    private final JLabel starredLabel;

    /**
     * Read/unread label.
     */
    private final JLabel readLabel;

    /**
     * Title label.
     */
    private final JLabel titleLabel;

    /**
     * Number of likes label.
     */
    private final JLabel likesLabel;

    /**
     * Author name label.
     */
    private final JLabel authorLabel;

    /**
     * Date/time label.
     */
    private final JLabel dateTimeLabel;

    /**
     * Text label.
     */
    private final JLabel textLabel;

    /**
     * X-coordinate of the title and text component.
     */
    private int titleAndTextX;

    /**
     * Configuration object.
     */
    private final transient Configuration configuration;

    /**
     * Construct a GUI table cell renderer for articles.
     *
     * @param configuration the configuration object.
     */
    public ArticleTableCellRenderer(final Configuration configuration) {
        super(null);

        this.configuration = configuration;

        // Note: layout has currently been done without layout managers.

        starredLabel = new JLabel();
        readLabel = new JLabel();
        titleLabel = new JLabel();
        likesLabel = new JLabel();
        authorLabel = new JLabel();
        dateTimeLabel = new JLabel();
        textLabel = new JLabel();

        starredLabel.setFont(BOLD_FONT);
        readLabel.setFont(BOLD_FONT);
        titleLabel.setFont(BOLD_FONT);
        likesLabel.setFont(REGULAR_FONT);
        authorLabel.setFont(BOLD_FONT);
        dateTimeLabel.setFont(REGULAR_FONT);
        textLabel.setFont(REGULAR_FONT);

        positionAndAddTopComponents();
        positionAndAddBottomComponent();
    }

    /**
     * Position the top components and add them to the renderer.
     */
    private void positionAndAddTopComponents() {
        final int starredX = 4;
        final int readX = starredX + STARRED_COLUMN_WIDTH;

        titleAndTextX = readX + READ_COLUMN_WIDTH;

        final int titleWidth = 400;
        final int likesX = titleAndTextX + titleWidth + 154;
        final int likesWidth = 38;
        final int authorX = likesX + likesWidth + 2;
        final int authorWidth = 160;
        final int dateTimeX = authorX + authorWidth;
        final int dateTimeWidth = 120;

        final int topRowY = 3;
        final int flagsHeight = 40;
        final int topTextComponentsHeight = 18;

        starredLabel.setBounds(starredX, topRowY, STARRED_COLUMN_WIDTH, flagsHeight);
        add(starredLabel);

        readLabel.setBounds(readX, topRowY, READ_COLUMN_WIDTH, flagsHeight);
        add(readLabel);

        titleLabel.setBounds(titleAndTextX, topRowY, titleWidth, topTextComponentsHeight);
        add(titleLabel);

        likesLabel.setBounds(likesX, topRowY, likesWidth, topTextComponentsHeight);
        add(likesLabel);

        authorLabel.setBounds(authorX, topRowY, authorWidth, topTextComponentsHeight);
        add(authorLabel);

        dateTimeLabel.setBounds(dateTimeX, topRowY, dateTimeWidth, topTextComponentsHeight);
        add(dateTimeLabel);
    }

    /**
     * Position the bottom component and add it to the renderer.
     */
    private void positionAndAddBottomComponent() {
        final int bottomRowY = 20;
        final int textWidth = 1600;
        final int textHeight = 24;

        textLabel.setBounds(titleAndTextX, bottomRowY, textWidth, textHeight);
        add(textLabel);
    }

    /**
     * Set the default background color for this renderer.
     *
     * @param defaultBackgroundColor the default background color.
     */
    static void setDefaultBackgroundColor(final Color defaultBackgroundColor) {
        ArticleTableCellRenderer.defaultBackgroundColor = defaultBackgroundColor;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
        final Article article = (Article) value;

        this.setBackground(isSelected ? configuration.getNiceLightBlue() : getBackgroundColor(article.getSourceId()));

        final String iconFileName = "32x32-" + (article.isStarred() ? "Full_Star_Yellow" : "Empty_Star") + ".png";
        starredLabel.setIcon(Utilities.getIconResource(iconFileName));

        readLabel.setVisible(!article.isRead());
        readLabel.setText(article.isRead() ? "R" : "U");

        titleLabel.setText(article.getTitle());
        likesLabel.setText("+" + article.getLikes());
        authorLabel.setText(article.getAuthor() != null ? article.getAuthor().getName() : "");

        dateTimeLabel.setText(article.getDateTime() != null
                                      ? configuration.getDateTimeFormatLonger().format(article.getDateTime())
                                      : "");

        textLabel.setText(article.getText());

        return this;
    }

    /**
     * Get the background color that is configured for the specified source id.
     *
     * @param sourceId the source id to get the background color for.
     * @return the background color that is configured for the specified source id.
     */
    private Color getBackgroundColor(final String sourceId) {
        return BACKGROUND_COLOR_MAP.getOrDefault(sourceId, defaultBackgroundColor);
    }

    /**
     * Calculate column index for a mouse click in the GUI table.
     *
     * @param xCoordinate x coordinate of the mouse.
     * @return column index for a mouse click in the GUI table.
     */
    static int calculateColumnIndex(final int xCoordinate) {
        final int columnIndex;
        final int columnIndexForOtherColumns = 3;

        if (xCoordinate < STARRED_COLUMN_WIDTH) {
            columnIndex = 1;
        } else if (xCoordinate < STARRED_COLUMN_WIDTH + READ_COLUMN_WIDTH) {
            columnIndex = 2;
        } else {
            columnIndex = columnIndexForOtherColumns;
        }

        return columnIndex;
    }
}
