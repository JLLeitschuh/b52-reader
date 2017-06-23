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
 * @version 1.0 11/22/98
 */
public interface TableSpans {
    int ROW    = 0;
    int COLUMN = 1;

    void setSize(Dimension size);
    void addColumn();
    void addRow();
    void insertRow(int rowIndex);

    boolean isVisible(int rowIndex, int columnIndex);
    int[] getSpan(int rowIndex, int columnIndex);
    void combine(int[] rowIndices, int[] columnIndices);
}
