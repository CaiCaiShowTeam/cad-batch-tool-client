package com.bplead.cad.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import com.bplead.cad.bean.BOMInfo;
import com.bplead.cad.bean.SubBOMInfo;
import com.bplead.cad.bean.io.CADLink;
import com.bplead.cad.util.ClientUtils;
import priv.lee.cad.model.ResourceMap;
import priv.lee.cad.model.ResourceMapper;
import priv.lee.cad.model.impl.DefaultResourceMap;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.StringUtils;

public class BomDetailTable extends JTable implements ResourceMapper, MouseListener {

	private static final int FIXED_WIDTH = 5;
	private static final long serialVersionUID = -5844495101340439741L;
	private final String COL_HEADER_SUFFIX = "].header";
	private final String COL_TYPE_SUFFIX = "].value.type";
	private final String COL_WIDTH_SUFFIX = "].proportion.width";
	private final String COLUMN_TOTAL = "column.total";
	private final String PREFIX_COL_HEADER = "column[";
	private ResourceMap resourceMap;

	{
		resourceMap = new DefaultResourceMap(BomDetailTable.class);
	}

	public BomDetailTable(String partnumber, List<CADLink> details) {
		initTable(partnumber, details);
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

	private void initTable(String partNumber, List<CADLink> details) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		List<String> headers = getColumnHeaders();
		for (int column = 0; column < headers.size(); column++) {
			if (model.findColumn(headers.get(column)) == -1) {
				model.addColumn(headers.get(column));
			}
		}
		if (details == null || details.size() == 0) {
			return;
		}

		BOMInfo serverBomInfo = ClientUtils.getBomDetails(partNumber);
		Vector<SubBOMInfo> serverSubBomInfos = serverBomInfo.getSubParts();

		for (int i = 0; i < details.size(); i++) {
			CADLink subBomInfo = details.get(i);
			compare(subBomInfo, serverSubBomInfos, model);
		}
		addMouseListener(this);

		DefaultTableCellRenderer render = new DefaultTableCellRenderer();
		render.setHorizontalAlignment(SwingConstants.CENTER);
		getColumnModel().getColumn(0).setCellRenderer(render);

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void compare(CADLink subBomInfo, Vector<SubBOMInfo> svrBomInfo, DefaultTableModel model) {
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
				row.add(buildContent(sigletonWeight, svrSigletonWeight));
				row.add(buildContent(totalWeight, svrTotalWeight));
				row.add(buildContent(materialModel, svrMaterialModel));
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
			row.add("<html><sapn style=\"color:red\">[" + sigletonWeight + "]</span></html>");
			row.add("<html><sapn style=\"color:red\">[" + totalWeight + "]</span></html>");
			row.add("<html><sapn style=\"color:red\">[" + materialModel + "]</span></html>");
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
