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

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

/**
 * @version 1.0 11/22/98
 */
public class SpanCellTableModel extends DefaultTableModel {
    private TableSpans tableSpans;

    public SpanCellTableModel(int rowCount, int columnCount) {
        Vector names = new Vector(columnCount);
        names.setSize(columnCount);
        setColumnIdentifiers(names);

        dataVector = new Vector();
        setRowCount(rowCount);

        tableSpans = new DefaultTableSpans(rowCount, columnCount);
    }

    public TableSpans getTableSpans() {
        return tableSpans;
    }

    public void setDataVector(Vector newData, Vector columnNames) {
        if (newData == null) {
            throw new IllegalArgumentException("setDataVector() - Null parameter");
        }

        dataVector = new Vector(0);

        // Code modified to prevent stack overflow. See http://stackoverflow.com/a/21977825/1694043 for more information.
        // setColumnIdentifiers(columnNames);
        columnIdentifiers = columnNames;

        dataVector = newData;

        tableSpans = new DefaultTableSpans(dataVector.size(), columnIdentifiers.size());

        newRowsAdded(new TableModelEvent(this, 0, getRowCount() - 1,
                                         TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    public void addColumn(Object columnName, Vector columnData) {
        if (columnName == null) {
            throw new IllegalArgumentException("addColumn() - null parameter");
        }

        //noinspection unchecked
        columnIdentifiers.addElement(columnName);

        int index = 0;
        Enumeration enumeration = dataVector.elements();

        while (enumeration.hasMoreElements()) {
            Object value;
            if ((columnData != null) && (index < columnData.size()))
                value = columnData.elementAt(index);
            else
                value = null;

            //noinspection unchecked
            ((Vector) enumeration.nextElement()).addElement(value);

            index++;
        }

        tableSpans.addColumn();

        fireTableStructureChanged();
    }

    public void addRow(Vector rowData) {
        Vector newData = null;

        if (rowData == null) {
            newData = new Vector(getColumnCount());
        } else {
            rowData.setSize(getColumnCount());
        }

        //noinspection unchecked
        dataVector.addElement(newData);

        tableSpans.addRow();

        newRowsAdded(new TableModelEvent(this, getRowCount() - 1, getRowCount() - 1,
                                         TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    public void insertRow(int row, Vector rowData) {
        if (rowData == null) {
            rowData = new Vector(getColumnCount());
        } else {
            rowData.setSize(getColumnCount());
        }

        //noinspection unchecked
        dataVector.insertElementAt(rowData, row);

        tableSpans.insertRow(row);

        newRowsAdded(new TableModelEvent(this, row, row,
                                         TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }
}
