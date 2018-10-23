package com.bplead.cad.model;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.io.CadStatus;

public class MutiTable extends JTable implements MouseListener {

    private static final long serialVersionUID = 584842405181279389L;
    
    private static Logger logger = Logger.getLogger (MutiTable.class.getName ());

    private final int DEFAULT_CHECKHEADERCOLUMN = -1;

    private final int DEFAULT_PREFERREDWIDTH = 23;

    private final int DEFAULT_MAXWIDTH = 23;

    private final int DEFAULT_MINWIDTH = 23;

    private int checkHeaderColumn = DEFAULT_CHECKHEADERCOLUMN;

    private final HeaderCheckBoxRenderer checkHeader = new HeaderCheckBoxRenderer ();

    private TableCellRenderer oldCheckHeader = null;

    private boolean oldEnable = false;
    
    private int DEFAULT_BUTTON_COLUMN = 3;

    @Override
    public void mouseClicked(MouseEvent e) {
	if (this.rowAtPoint (e.getPoint ()) == DEFAULT_CHECKHEADERCOLUMN) {
	    return;
	}
	Object value = this.getModel ().getValueAt (this.rowAtPoint (e.getPoint ()),this.columnAtPoint (e.getPoint ()));
	if (logger.isDebugEnabled ()) {
	    logger.debug ("row index is -> " + this.rowAtPoint (e.getPoint ()) + " column index is -> " +  this.columnAtPoint (e.getPoint ()) + " value is -> " + value);
	}
	if (null != value && JButton.class.isAssignableFrom (value.getClass ())) {
	    String number = (String) this.getModel ().getValueAt (this.rowAtPoint (e.getPoint ()),DEFAULT_BUTTON_COLUMN - 1);
	    String status = (String) this.getModel ().getValueAt (this.rowAtPoint (e.getPoint ()),DEFAULT_BUTTON_COLUMN + 2);
	    CadStatus cadStatus = CadStatus.toEnumeration (CadStatus.getInnerValueByDisplayName (status));
	    if (logger.isDebugEnabled ()) {
		logger.debug ("e.getSource () is JButton and number is -> " + number + " cadStatus display is -> " + status + " cadStatus is -> " + cadStatus);
	    }
	    //TODO 
	    if (cadStatus == CadStatus.NOT_EXIST) {
		JOptionPane.showMessageDialog (null,"该行对象尚未在系统中创建,不能执行该BOM比较操作.","提示",JOptionPane.INFORMATION_MESSAGE);
	    } else {
		JOptionPane.showMessageDialog (null,"open compare window " + number,"success",JOptionPane.INFORMATION_MESSAGE);
	    }
	    
//	    new PdmLinkProductChooseDialog (number).activate ();
	}
	if (this.columnAtPoint (e.getPoint ()) != this.checkHeaderColumn) {
	    return;
	}

	if (e.getSource () == this.getTableHeader ()) {
	    boolean isSelected = !checkHeader.isSelected ();
	    checkHeader.setSelected (isSelected);
	    this.getTableHeader ().repaint ();
	    checkColumnCells (isSelected);
	} else {
	    int row = this.rowAtPoint (e.getPoint ());
	    boolean isSelected = !(Boolean) ( this.getModel ().getValueAt (row,this.checkHeaderColumn) );
	    this.getModel ().setValueAt (isSelected,row,this.checkHeaderColumn);
	    checkColumnHeader ();
	}
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    public int getCheckHeaderColumn() {
	return checkHeaderColumn;
    }

    public void setCheckHeaderColumn(int checkHeaderColumn) {
	TableColumn tableColumn;

	if (isCheckHeader ()) {
	    tableColumn = this.getColumnModel ().getColumn (this.checkHeaderColumn);

	    if (null != oldCheckHeader) {
		tableColumn.setHeaderRenderer (oldCheckHeader);
		this.setEnabled (oldEnable);
	    }

	    this.getTableHeader ().removeMouseListener (this);
	    this.removeMouseListener (this);
	}

	this.checkHeaderColumn = checkHeaderColumn;

	if (!isCheckHeader ()) {
	    this.checkHeaderColumn = DEFAULT_CHECKHEADERCOLUMN;
	    return;
	}

	tableColumn = this.getColumnModel ().getColumn (this.checkHeaderColumn);
	tableColumn.setPreferredWidth (DEFAULT_PREFERREDWIDTH);
	tableColumn.setMaxWidth (DEFAULT_MAXWIDTH);
	tableColumn.setMinWidth (DEFAULT_MINWIDTH);

	oldCheckHeader = tableColumn.getHeaderRenderer ();
	tableColumn.setHeaderRenderer (checkHeader);

	this.getTableHeader ().addMouseListener (this);

	this.addMouseListener (this);

	oldEnable = this.isEnabled ();

	if (oldEnable) {
	    this.setEnabled (false);
	}

	checkColumnHeader ();
    }

    public void checkColumnCells(boolean isCheck) {
	if (!isCheckHeader ()) {
	    return;
	}

	for (int ii = 0; ii < this.getRowCount (); ii++) {
	    this.getModel ().setValueAt (isCheck,ii,this.checkHeaderColumn);
	}
    }

    public void checkColumnHeader() {
	if (hasCheckedRow ()) {
	    if (this.checkHeader.isSelected ()) {
		return;
	    }

	    this.checkHeader.setSelected (true);
	    this.getTableHeader ().repaint ();
	} else {
	    if (!this.checkHeader.isSelected ()) {
		return;
	    }

	    this.checkHeader.setSelected (false);
	    this.getTableHeader ().repaint ();
	}
    }

    public boolean isCheckHeader() {
	return !( this.checkHeaderColumn < 0 || this.checkHeaderColumn >= this.getColumnCount () );
    }

    public boolean hasCheckedRow() {
	if (!isCheckHeader ()) {
	    return false;
	}

	for (int ii = 0; ii < this.getRowCount (); ii++) {
	    boolean isCheck = (Boolean) this.getModel ().getValueAt (ii,this.checkHeaderColumn);

	    if (isCheck) {
		return true;
	    }
	}

	return false;
    }

    public List<Integer> getAllCheckedRows() {
	List<Integer> rows = new ArrayList<Integer> ();

	if (!isCheckHeader ()) {
	    return rows;
	}

	for (int ii = 0; ii < this.getRowCount (); ii++) {
	    boolean isCheck = (Boolean) this.getModel ().getValueAt (ii,this.checkHeaderColumn);

	    if (isCheck) {
		rows.add (ii);
	    }
	}

	return rows;
    }

    public List<Object> getAllCheckedColumn(int col) {
	List<Object> rows = new ArrayList<Object> ();

	if (!isCheckHeader ()) {
	    return rows;
	}

	for (int ii = 0; ii < this.getRowCount (); ii++) {
	    boolean isCheck = (Boolean) this.getModel ().getValueAt (ii,this.checkHeaderColumn);

	    if (isCheck) {
		rows.add (this.getModel ().getValueAt (ii,col));
	    }
	}

	return rows;
    }

    public void refreshContents(Object [] [] datas) throws Exception {
	MutiTableModel mutiTableModel = (MutiTableModel) this.getModel ();
	mutiTableModel.refreshContents (datas);
	checkColumnHeader ();
	this.updateUI ();
    }

    public void addRow(Object [] data) throws Exception {
	MutiTableModel mutiTableModel = (MutiTableModel) this.getModel ();
	mutiTableModel.addRow (data);
	checkColumnHeader ();
	this.updateUI ();
    }

    public void removeRow(int row) {
	MutiTableModel mutiTableModel = (MutiTableModel) this.getModel ();
	mutiTableModel.removeRow (row);
	checkColumnHeader ();
	this.updateUI ();
    }

    public void removeRows(int row, int count) {
	MutiTableModel mutiTableModel = (MutiTableModel) this.getModel ();
	mutiTableModel.removeRows (row,count);
	checkColumnHeader ();
	this.updateUI ();
    }

    public void setDefaultButtonColumn (int column) {
	DEFAULT_BUTTON_COLUMN = column;
    }
    
}
