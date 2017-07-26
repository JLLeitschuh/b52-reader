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

import java.awt.Dimension;

/**
 * Interface for storing and modifying table span data.
 *
 * @author <a href="mailto:unknown@unknown.org">Nobuo Tamemasa</a>
 * @version 1.0 11/22/98
 */
public interface TableSpans {
    /**
     * Set new size of table span data.
     *
     * @param size new size.
     */
    void setSize(Dimension size);

    /**
     * Add a column to the table span data.
     */
    void addColumn();

    /**
     * Add a row to the table span data.
     */
    void addRow();

    /**
     * Insert a row in the table span data.
     *
     * @param rowIndex row index where the new row should be inserted.
     */
    void insertRow(int rowIndex);

    /**
     * Determine whether a cell at a specified row and column index is visible (or part of a span rectangle).
     *
     * @param rowIndex    row index.
     * @param columnIndex column index.
     * @return whether a cell at a certain row and column index is visible.
     */
    boolean isVisible(int rowIndex, int columnIndex);

    /**
     * Get span data for a cel at a specified row and column index.
     *
     * @param rowIndex    row index.
     * @param columnIndex column index.
     * @return span data for a cell at a specified row and column index.
     */
    SpanCounts getSpan(int rowIndex, int columnIndex);

    /**
     * Combine cells at specified row and column indices into a span rectangle. Note: all cells should not yet be part
     * of a span rectangle and all indices are assumed to be adjacent numbers.
     *
     * @param rowIndices    one or more row indices.
     * @param columnIndices one or more column indices.
     */
    void combine(int[] rowIndices, int[] columnIndices);
}
