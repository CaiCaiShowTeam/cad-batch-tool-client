package com.bplead.cad.ui;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class RemoveLocalFileChooser extends JFileChooser {

	private static final long serialVersionUID = -4355323193750393407L;

	public RemoveLocalFileChooser(JTable table) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		int rowCount = tableModel.getRowCount();
		for (int i = rowCount - 1; i >= 0; i--) {
			Boolean isSelected = (Boolean) table.getValueAt(i, 0);
			if (isSelected) {
				tableModel.removeRow(i);
				rowCount--;
			}
		}
		
		rowCount = tableModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tableModel.setValueAt(i + 1, i, 1);
		}
	}
}
