package com.bplead.cad.ui;

import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import priv.lee.cad.util.StringUtils;

public class AddLocalFileChooser extends JFileChooser {

	private static final long serialVersionUID = -4355323193750393407L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AddLocalFileChooser(int mode, JTable table) {
		setFileSelectionMode(mode);
		setDialogType(JFileChooser.OPEN_DIALOG);
		resetChoosableFileFilters();
		setMultiSelectionEnabled(true);
		if (showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
			File[] selectedFiles = getSelectedFiles();
			if (selectedFiles == null || selectedFiles.length == 0) {
				return;
			}

			DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
			for (int i = 0; i < selectedFiles.length; i++) {
				File file = selectedFiles[i];
				if (file != null && file.isFile()) {
					String path = file.getPath();
					int rowCount = tableModel.getRowCount();
					boolean isExist = false;
					for (int j = 0; j < rowCount; j++) {
						String existPath = (String) table.getValueAt(j, 3);
						if (StringUtils.equalsIgnoreCase(existPath, path)) {
							isExist = true;
							break;
						}
					}
					if (!isExist) {
						Vector row = new Vector();
						row.add(false);
						row.add(String.valueOf(++rowCount));
						row.add(file.getName());
						row.add(path);
						tableModel.addRow(row);
					}

				}
			}

			int rowCount = tableModel.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModel.setValueAt(i + 1, i, 1);
			}
			
			DefaultTableCellRenderer render = new DefaultTableCellRenderer();
			render.setHorizontalAlignment(SwingConstants.CENTER);
			table.getColumnModel().getColumn(1).setCellRenderer(render);
		}
	}
}
