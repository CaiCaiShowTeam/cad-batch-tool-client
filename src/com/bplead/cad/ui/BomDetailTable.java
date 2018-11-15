package com.bplead.cad.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.bplead.cad.bean.BOMInfo;
import com.bplead.cad.bean.SimpleDocument;
import com.bplead.cad.bean.SubBOMInfo;
import com.bplead.cad.util.ClientUtils;

import priv.lee.cad.model.ResourceMap;
import priv.lee.cad.model.ResourceMapper;
import priv.lee.cad.model.impl.DefaultResourceMap;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.StringUtils;
import priv.lee.cad.util.XmlUtils;

public class BomDetailTable extends JTable implements ResourceMapper, MouseListener {

	private static final int FIXED_WIDTH = 5;
	private static final long serialVersionUID = -5844495101340439741L;
	private final String COL_HEADER_SUFFIX = "].header";
	private final String COL_NAME_SUFFIX = "].value.name";
	private final String COL_TYPE_SUFFIX = "].value.type";
	private final String COL_WIDTH_SUFFIX = "].proportion.width";
	private final String COLUMN_TOTAL = "column.total";
	private List<SimpleDocument> documents;
	private final String PREFIX_COL_HEADER = "column[";
	private ResourceMap resourceMap;

	{
		resourceMap = new DefaultResourceMap(BomDetailTable.class);
	}

	public BomDetailTable(List<SimpleDocument> documents) {
		this.documents = documents;
		
		Vector<BOMInfo> bomInfos = new Vector<BOMInfo>();
		BOMInfo info = new BOMInfo();
		info.setNumber("TE001");
		info.setName("测试图号");
		Vector<SubBOMInfo> subParts = new  Vector<SubBOMInfo>();
		SubBOMInfo subInfo = new SubBOMInfo();
		subInfo.setNumber("W001");
		subInfo.setName("测试图号1");
		subInfo.setQuantity("0.1");
		subInfo.setMaterialModel("规格1");
		subInfo.setSigletonWeight("标识1");
		subInfo.setTotalWeight("尺寸1");
		subInfo.setDesciption("型号1");
		
		SubBOMInfo subInfo1 = new SubBOMInfo();
		subInfo1.setNumber("W002");
		subInfo1.setName("测试图号2");
		subInfo1.setQuantity("0.2");
		subInfo1.setMaterialModel("规格2");
		subInfo1.setSigletonWeight("标识2");
		subInfo1.setTotalWeight("尺寸2");
		subInfo1.setDesciption("型号2");
		
		subParts.add(subInfo);
		subParts.add(subInfo1);
		
		info.setSubParts(subParts);
		bomInfos.add(info);
		
		initTable("TE001",bomInfos);
	}

	public void clear() {
		DefaultTableModel tableModel = (DefaultTableModel) getModel();
		tableModel.setRowCount(0);
	}

	@SuppressWarnings("unchecked")
	private <T> T getCellContent(SimpleDocument product, String name, Class<T> clatt) {
		if (product == null || StringUtils.isEmpty(name) || clatt == null) {
			return null;
		}

		try {
			Field field = XmlUtils.findField(product.getClass(), name);
			field.setAccessible(true);
			return (T) field.get(product);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getCellContentName(int column) {
		return resourceMap.getString(PREFIX_COL_HEADER + column + COL_NAME_SUFFIX);
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

	public List<SimpleDocument> getProducts() {
		return documents;
	}

	@Override
	public ResourceMap getResourceMap() {
		return resourceMap;
	}

	public SimpleDocument getRowData(int row) {
		if (documents == null) {
			return null;
		}
		ClientAssert.isTrue(row < documents.size(), "Row out of bounds:" + row);
		return documents.get(row);
	}

	public List<SimpleDocument> getSelectedDocuments() {
		List<SimpleDocument> selectedDocuments = new ArrayList<SimpleDocument>();
		int rows = getModel().getRowCount();
		for (int i = 0; i < rows; i++) {
			Boolean selected = (Boolean) getValueAt(i, 0);
			if (selected) {
				selectedDocuments.add(documents.get(i));
			}
		}
		return selectedDocuments;
	}

	private void initTable() {
		DefaultTableModel model = (DefaultTableModel) getModel();
		List<String> headers = getColumnHeaders();
		for (int column = 0; column < headers.size(); column++) {
			if (model.findColumn(headers.get(column)) == -1) {
				model.addColumn(headers.get(column));
			}
		}

		addMouseListener(this);

		invalidate();
	}

	@SuppressWarnings("unused")
	private void initTable(String partNumber, Vector<BOMInfo> bomInfos) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		List<String> headers = getColumnHeaders();
		for (int column = 0; column < headers.size(); column++) {
			if (model.findColumn(headers.get(column)) == -1) {
				model.addColumn(headers.get(column));
			}
		}
		if (bomInfos == null || bomInfos.isEmpty()) {
			return;
		}
//		BOMInfo serverBomInfo = ClientUtils.getBomDetails(partNumber);
		
//		Vector<SubBOMInfo> serverSubBomInfos = serverBomInfo.getSubParts();
		
		Vector<SubBOMInfo> serverSubBomInfos = new Vector<SubBOMInfo>();
		SubBOMInfo subInfo = new SubBOMInfo();
		subInfo.setNumber("W001");
		subInfo.setName("测试图号3");
		subInfo.setQuantity("0.3");
		subInfo.setMaterialModel("规格3");
		subInfo.setSigletonWeight("标识3");
		subInfo.setTotalWeight("尺寸3");
		subInfo.setDesciption("型号3");
		
		SubBOMInfo subInfo1 = new SubBOMInfo();
		subInfo1.setNumber("W002");
		subInfo1.setName("测试图号4");
		subInfo1.setQuantity("0.4");
		subInfo1.setMaterialModel("规格4");
		subInfo1.setSigletonWeight("标识4");
		subInfo1.setTotalWeight("尺寸4");
		subInfo1.setDesciption("型号4");
		
		serverSubBomInfos.add(subInfo);
		serverSubBomInfos.add(subInfo1);
		
		for (int i = 0; i < bomInfos.size(); i++) {
			BOMInfo bomInfo = bomInfos.get(i);
			String number = bomInfo.getNumber();
			if (StringUtils.equalsIgnoreCase(number, partNumber)) {
				Vector<SubBOMInfo> subBomInfos = bomInfo.getSubParts();
				if (subBomInfos != null && !subBomInfos.isEmpty()) {
					for (int j = 0; j < subBomInfos.size(); j++) {
						SubBOMInfo subBomInfo = subBomInfos.get(j);
						compare(subBomInfo, serverSubBomInfos, model);
					}
				}
			}
		}

		addMouseListener(this);

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

	public void refresh(List<SimpleDocument> documents) {
		this.documents = documents;
		initTable();
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

	private void setRows(DefaultTableModel model, int column) {
		if (documents == null || documents.isEmpty()) {
			clear();
			return;
		}

		for (int row = 0; row < documents.size(); row++) {
			if (model.getRowCount() <= row) {
				model.addRow(new Object[] {});
			}

			try {
				Object content = getCellContent(documents.get(row), getCellContentName(column),
						getCellContentType(column));
				model.setValueAt(content == null ? "" : content, row, column);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void compare(SubBOMInfo subBomInfo, Vector<SubBOMInfo> svrBomInfo, DefaultTableModel model) {
		if (subBomInfo == null) {
			return;
		}
		String number = StringUtils.isEmpty(subBomInfo.getNumber()) ? "-" : subBomInfo.getNumber();
		String name = StringUtils.isEmpty(subBomInfo.getName()) ? "-" : subBomInfo.getName();
		String quantity = StringUtils.isEmpty(subBomInfo.getQuantity()) ? "-" : subBomInfo.getQuantity();
		String materialModel = StringUtils.isEmpty(subBomInfo.getMaterialModel()) ? "-" : subBomInfo.getMaterialModel();
		String sigletonWeight = StringUtils.isEmpty(subBomInfo.getSigletonWeight()) ? "-"
				: subBomInfo.getSigletonWeight();
		String totalWeight = StringUtils.isEmpty(subBomInfo.getTotalWeight()) ? "-" : subBomInfo.getTotalWeight();
		String desciption = StringUtils.isEmpty(subBomInfo.getDesciption()) ? "-" : subBomInfo.getDesciption();

		boolean flag = false;
		int rowCount = model.getRowCount();
		Vector row = new Vector();
		if (svrBomInfo != null && !svrBomInfo.isEmpty()) {
			for (int i = 0; i < svrBomInfo.size(); i++) {
				SubBOMInfo svrSubBomInfo = svrBomInfo.get(i);
				String svrNumber = svrSubBomInfo.getNumber();
				if (!StringUtils.equalsIgnoreCase(svrNumber, number)) {
					continue;
				}
				String svrName = StringUtils.isEmpty(svrSubBomInfo.getName()) ? "-" : svrSubBomInfo.getName();
				String svrQuantity = StringUtils.isEmpty(svrSubBomInfo.getQuantity()) ? "-"
						: svrSubBomInfo.getQuantity();
				String svrMaterialModel = StringUtils.isEmpty(svrSubBomInfo.getMaterialModel()) ? "-"
						: svrSubBomInfo.getMaterialModel();
				String svrSigletonWeight = StringUtils.isEmpty(svrSubBomInfo.getSigletonWeight()) ? "-"
						: svrSubBomInfo.getSigletonWeight();
				String svrTotalWeight = StringUtils.isEmpty(svrSubBomInfo.getTotalWeight()) ? "-"
						: svrSubBomInfo.getTotalWeight();
				String svrDesciption = StringUtils.isEmpty(svrSubBomInfo.getDesciption()) ? "-"
						: svrSubBomInfo.getDesciption();
				row.add(rowCount + 1);
				row.add(buildContent(number, svrNumber));
				row.add(buildContent(name, svrName));
				row.add(buildContent(quantity, svrQuantity));
				row.add("");
				row.add("");
				row.add(buildContent(materialModel, svrMaterialModel));
				row.add(buildContent(sigletonWeight, svrSigletonWeight));
				row.add(buildContent(totalWeight, svrTotalWeight));
				row.add(buildContent(desciption, svrDesciption));
				flag = true;
				break;
			}
		}
		if (!flag) {
			row.add(rowCount + 1);
			row.add("<html><sapn style=\"color:red\">[" + number + "]</span></html>");
			row.add("<html><sapn style=\"color:red\">[" + name + "]</span></html>");
			row.add("<html><sapn style=\"color:red\">[" + quantity + "]</span></html>");
			row.add("");
			row.add("");
			row.add("<html><sapn style=\"color:red\">[" + materialModel + "]</span></html>");
			row.add("<html><sapn style=\"color:red\">[" + sigletonWeight + "]</span></html>");
			row.add("<html><sapn style=\"color:red\">[" + totalWeight + "]</span></html>");
			row.add("<html><sapn style=\"color:red\">[" + desciption + "]</span></html>");
		}
		model.addRow(row);
	}

	public String buildContent(String value, String svrValue) {
		String content = "";
		content = "<html><span style=\"color:red\">[" + value + "]</span><span style=\"color:blue\">["
				+ StringUtils.trimAllWhitespace(svrValue) + "]</span></html>";
		return content;
	}

}
