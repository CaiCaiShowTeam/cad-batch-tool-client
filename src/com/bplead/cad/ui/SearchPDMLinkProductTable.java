package com.bplead.cad.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.bplead.cad.bean.PDMInfo;
import com.bplead.cad.ui.SearchPDMLinkProductDialog.SearchConditionsPanel;
import com.bplead.cad.util.ClientUtils;

import priv.lee.cad.model.ResourceMap;
import priv.lee.cad.model.ResourceMapper;
import priv.lee.cad.model.impl.DefaultResourceMap;

class MyTableCellRenderer extends JRadioButton implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4028871936052636498L;

	public MyTableCellRenderer() {
		super();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JRadioButton newButton = (JRadioButton) value;
		newButton.setSelected(isSelected);
		return newButton;
	}
}

public class SearchPDMLinkProductTable extends JTable implements ResourceMapper, MouseListener {

	private class InitTable extends SwingWorker<String, String> {
		private final String QUERYRESULT = "queryResult";

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected String doInBackground() throws Exception {
			try {
				DefaultTableModel model = (DefaultTableModel) getModel();
				List<PDMInfo> infos = ClientUtils.getPDMInfos();
				if (infos == null || infos.isEmpty()) {
					return null;
				}
				for (int i = 0; i < infos.size(); i++) {
					PDMInfo info = infos.get(i);
					String name = info.getName();
					String modifyTime = info.getModifyTime();
					String modifier = info.getModifier();

					Vector row = new Vector();
					row.add(new JRadioButton());
					row.add(String.valueOf(i + 1));
					row.add(name);
					row.add(modifyTime);
					row.add(modifier);
					model.addRow(row);
				}
				searchConditionPanel.msg
						.setText(searchConditionPanel.getResourceMap().getString(QUERYRESULT) + infos.size() + "个");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	private static final int FIXED_WIDTH = 5;
	private static final long serialVersionUID = -5844495101340439741L;
	private final String COL_HEADER_SUFFIX = "].header";
	private final String COL_WIDTH_SUFFIX = "].proportion.width";
	private final String COLUMN_TOTAL = "column.total";
	private List<PDMInfo> infos;
	private final String PREFIX_COL_HEADER = "column[";
	private ResourceMap resourceMap;
	private SearchConditionsPanel searchConditionPanel;

	{
		resourceMap = new DefaultResourceMap(SearchPDMLinkProductTable.class);
	}

	public SearchPDMLinkProductTable(SearchConditionsPanel searchConditionPanel) {
		this.searchConditionPanel = searchConditionPanel;
		initHeader();
		InitTable initTable = new InitTable();
		initTable.execute();
	}

	public void clear() {
		DefaultTableModel tableModel = (DefaultTableModel) getModel();
		tableModel.setRowCount(0);
	}

	private List<String> getColumnHeaders() {
		int total = resourceMap.getInt(COLUMN_TOTAL);
		List<String> headers = new ArrayList<String>();
		for (int column = 0; column < total; column++) {
			String header = resourceMap.getString(PREFIX_COL_HEADER + column + COL_HEADER_SUFFIX);
			headers.add(header.equals(PREFIX_COL_HEADER + column + COL_HEADER_SUFFIX) ? "" : header);
		}
		return headers;
	}

	private int getColumnWidth(int column) {
		String proportion = resourceMap.getString(PREFIX_COL_HEADER + column + COL_WIDTH_SUFFIX);
		return new BigDecimal(getParent().getPreferredSize().width).multiply(new BigDecimal(proportion)).intValue();
	}

	@Override
	public ResourceMap getResourceMap() {
		return resourceMap;
	}

	public List<PDMInfo> getSelectedDocuments() {
		List<PDMInfo> selectedInfos = new ArrayList<PDMInfo>();
		int rows = getModel().getRowCount();
		for (int i = 0; i < rows; i++) {
			JRadioButton selected = (JRadioButton) getValueAt(i, 0);
			if (selected.isSelected()) {
				selectedInfos.add(infos.get(i));
			}
		}
		return selectedInfos;
	}

	public void initHeader() {
		DefaultTableModel model = (DefaultTableModel) getModel();
		List<String> headers = getColumnHeaders();
		for (int column = 0; column < headers.size(); column++) {
			if (model.findColumn(headers.get(column)) == -1) {
				model.addColumn(headers.get(column));
			}
		}
		getColumnModel().getColumn(0).setCellRenderer(new MyTableCellRenderer());
		addMouseListener(this);
		invalidate();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0)
			return true;
		else
			return false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
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

	public void setColumnWidth() {
		for (int column = 0; column < getColumnCount(); column++) {
			Class<?> cls = getColumnClass(column);
			if (column == 0 && Boolean.class.isAssignableFrom(cls)) {
				TableColumn firsetColumn = getColumnModel().getColumn(0);
				firsetColumn.setPreferredWidth(FIXED_WIDTH);
				firsetColumn.setMaxWidth(FIXED_WIDTH);
				firsetColumn.setMinWidth(FIXED_WIDTH);
				continue;
			}
			int width = getColumnWidth(column);
			getColumnModel().getColumn(column).setPreferredWidth(width);
		}
	}

	@Override
	public void setResourceMap(ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
	}
}
