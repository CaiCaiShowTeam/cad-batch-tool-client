package com.bplead.cad.ui;

import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MutiTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -1264704523326656387L;

    private static final String ERROR_DATA_LENGTH = "data length not matching";

    protected int checkColumn = -1;

    protected List<String> columnNames;

    protected List<Object> contents;

    public MutiTableModel() {
	this.columnNames = new Vector<String> ();

	this.contents = new Vector<Object> ();
    }

    public MutiTableModel(String [] columnNames) {
	this ();

	if (null == columnNames) {
	    return;
	}

	for (String columnName : columnNames) {
	    this.columnNames.add (columnName);
	}
    }

    public MutiTableModel(Object [] [] datas, String [] columnNames) throws Exception {
	this (columnNames);
	refreshContents (datas);
    }

    public void refreshContents(Object [] [] datas) throws Exception {
	this.contents.clear ();

	if (null == datas) {
	    return;
	}

	for (Object [] data : datas) {
	    addRow (data);
	}
    }

    public void addRow(Object [] data) throws Exception {
	if (null == data) {
	    return;
	}

	if (this.columnNames.size () != data.length) {
	    throw new Exception (ERROR_DATA_LENGTH);
	}

	Vector<Object> content = new Vector<Object> (this.columnNames.size ());

	for (int ii = 0; ii < this.columnNames.size (); ii++) {
	    content.add (data[ii]);
	}

	contents.add (content);
    }

    public void removeRow(int row) {
	contents.remove (row);
    }

    public void removeRows(int row, int count) {
	for (int ii = 0; ii < count; ii++) {
	    if (contents.size () > row) {
		contents.remove (row);
	    }
	}
    }

    public boolean isCellEditable(int row, int col) {
	if (col == this.checkColumn) {
	    return true;
	}

	return super.isCellEditable (row,col);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setValueAt(Object value, int row, int col) {
	( (Vector) contents.get (row) ).set (col,value);
	this.fireTableCellUpdated (row,col);
    }

    public Class<?> getColumnClass(int column) {
	Object value = getValueAt (0,column);

	if (value != null) {
	    return value.getClass ();
	}

	return super.getClass ();
    }

    @Override
    public int getColumnCount() {
	return this.columnNames.size ();
    }

    @Override
    public int getRowCount() {
	return this.contents.size ();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getValueAt(int row, int col) {
	return ( (Vector) contents.get (row) ).get (col);
    }

    public String getColumnName(int col) {
	return columnNames.get (col);
    }

    public int getCheckColumn() {
	return checkColumn;
    }

    public void setCheckColumn(int checkColumn) {
	this.checkColumn = checkColumn;
    }
}
