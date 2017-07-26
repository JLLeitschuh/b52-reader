/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: unknown.
 *
 * This class is based on the MultiSpanCellTableExample class and related classes from Nobuo Tamemasa.
 * Original Java package: jp.gr.java_conf.tame.swing.table
 *
 * In 2008 Andrew Thompson wrote (https://groups.google.com/forum/#!topic/comp.lang.java.gui/WicMvhk1DdM):
 *     "I spent a lot of time researching the possible licensing of the tame Swing codes,
 *      but Nobuo Tamemasa (the author) has proved to be untraceable, and never specified a
 *      license at the time of posting the codes to public forums.  :-("
 *
 * It is also referred to here: http://stackoverflow.com/a/21977825/1694043
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import lombok.Data;

/**
 * Span counts for a single cell (which can be part of a larger span rectangle).
 *
 * For each cell (a row and column position), a span number is stored for the row and column directions. This span
 * number is one for cells that are not combined. For a rectangle of cells that are combined, the top left cell contains
 * span numbers equal to the number of rows and columns in the rectangle, while the other cells have span numbers
 * smaller than or equal to zero.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
@Data
class SpanCounts {
    /**
     * Row span number: 1 for a single cell, span rectangle height (= row count) for the top left cell of a rectangle,
     * or smaller than or equal to zero for the other cells.
     */
    private int rowSpanNumber;

    /**
     * Column span number: 1 for a single cell, span rectangle width (= column count) for the top left cell of a
     * rectangle, or smaller than or equal to zero for the other cells.
     */
    private int columnSpanNumber;

    /**
     * Construct a span counts object with both numbers initialized to 1.
     */
    SpanCounts() {
        this.rowSpanNumber = 1;
        this.columnSpanNumber = 1;
    }

    /**
     * Construct a span counts object with specified numbers.
     *
     * @param rowSpanNumber initial row span number.
     * @param columnSpanNumber initial column span number.
     */
    SpanCounts(final int rowSpanNumber, final int columnSpanNumber) {
        this.rowSpanNumber = rowSpanNumber;
        this.columnSpanNumber = columnSpanNumber;
    }
}
