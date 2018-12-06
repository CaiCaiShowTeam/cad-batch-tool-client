package com.bplead.cad.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import priv.lee.cad.model.ResourceMap;
import priv.lee.cad.model.ResourceMapper;
import priv.lee.cad.model.impl.DefaultResourceMap;
import priv.lee.cad.util.ClientAssert;

public class ChooseDrawingTable extends JTable implements ResourceMapper, MouseListener {

	private static final int FIXED_WIDTH = 5;
	private static final long serialVersionUID = -5844495101340439741L;
	private final String COL_HEADER_SUFFIX = "].header";
	private final String COL_TYPE_SUFFIX = "].value.type";
	private final String COL_WIDTH_SUFFIX = "].proportion.width";
	private final String COLUMN_TOTAL = "column.total";
	private final String DWGLIST = "dwglist.txt";
	private final String PREFIX_COL_HEADER = "column[";
	private ResourceMap resourceMap;

	{
		resourceMap = new DefaultResourceMap(ChooseDrawingTable.class);
	}

	public ChooseDrawingTable() {
		initTable();
	}

	public void clear() {
		DefaultTableModel tableModel = (DefaultTableModel) getModel();
		tableModel.setRowCount(0);
	}

	private Class<?> getCellContentType(int column) throws ClassNotFoundException {
		String type = resourceMap.getString(PREFIX_COL_HEADER + column + COL_TYPE_SUFFIX);
		ClientAssert.hasText(type, "Column " + column + " type is required");
		return Class.forName(type);
	}

	@Override
	public Class<?> getColumnClass(int column) {
		try {
			return getCellContentType(column);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return Object.class;
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

	private void initTable() {
		DefaultTableModel model = (DefaultTableModel) getModel();
		List<String> headers = getColumnHeaders();
		for (int column = 0; column < headers.size(); column++) {
			if (model.findColumn(headers.get(column)) == -1) {
				model.addColumn(headers.get(column));
			}
		}

		setRows(model);
		addMouseListener(this);

		DefaultTableCellRenderer render = new DefaultTableCellRenderer();
		render.setHorizontalAlignment(SwingConstants.CENTER);
		getColumnModel().getColumn(1).setCellRenderer(render);

		invalidate();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return Boolean.class.isAssignableFrom(getValueAt(row, column).getClass());
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setRows(DefaultTableModel model) {
		int rowCount = model.getRowCount();
		String path = Class.class.getClass().getResource("/").getPath();
		path = path + DWGLIST;
		List<String> currentPaths = getCurrentDrawingPath(path);
		if (currentPaths == null || currentPaths.size() == 0) {
			return;
		}
		for (int i = 0; i < currentPaths.size(); i++) {
			String currentPath = currentPaths.get(i);
			File file = new File(currentPath);
			if (!file.exists()) {
				continue;
			}
			Vector row = new Vector();
			row.add(false);
			row.add(String.valueOf(++rowCount));
			row.add(file.getName());
			row.add(currentPath);
			model.addRow(row);
		}
	}

	public List<String> getCurrentDrawingPath(String path) {
		File file = new File(path);
		if (file.exists()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				String code = getCharset(fileInputStream);
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, code);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				List<String> currentPaths = new ArrayList<String>();
				String text = "";
				while ((text = bufferedReader.readLine()) != null) {
					currentPaths.add(text);
				}
				return currentPaths;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String getCharset(FileInputStream fileInputStream) throws IOException {
		int p = (fileInputStream.read() << 8) + fileInputStream.read();
		String code = "";
		switch (p) {
		case 0xefbb:
			code = "UTF-8";
			break;
		case 0xfffe:
			code = "Unicode";
			break;
		case 0xfeff:
			code = "UTF-16BE";
			break;
		default:
			code = "GBK";

		}
		return code;

	}
}
