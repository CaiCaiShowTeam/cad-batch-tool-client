/**
 * CadTablePanel.java 2018年10月19日
 */
package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.SimpleFolder;
import com.bplead.cad.bean.SimplePdmLinkProduct;
import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.CadStatus;
import com.bplead.cad.bean.io.Container;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.bean.io.Documents;
import com.bplead.cad.model.BottonColumnRenderer;
import com.bplead.cad.model.MutiTable;
import com.bplead.cad.model.MutiTableModel;
import com.bplead.cad.util.ClientUtils;

import priv.lee.cad.model.ResourceMap;
import priv.lee.cad.model.ResourceMapper;
import priv.lee.cad.model.impl.DefaultResourceMap;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.StringUtils;

/**
 * @author zjw 2018年10月19日下午1:44:35
 */
public class CadTablePanel extends AbstractPanel implements ResourceMapper {

    private static final long serialVersionUID = 1402990192978044447L;
    private static Logger logger = Logger.getLogger (CadTablePanel.class.getName ());
    private static final String TITLE = "title";
    private final int CHECKHEADERCOLUMN = 0;
    private final int DEFAULT_NUMBER_COLUMN = 2;
    private final int BOTTON_COLUMN_INDEX = 3;
    private final int DEFAULT_STATUS_COLUMN = 5;
    private final int DEFAULT_CONTAINER_COLUMN = 6;
    private final int DEFAULT_FOLDER_COLUMN = 7;
    private final double TABLE_HEIGHT_PROPORTION = 1.05d;
    private final double SCROLL_HEIGHT_PROPORTION = 0.9d;
    private final double SCROLL_WIDTH_PROPORTION = 0.97d;
    private Documents documents;
    private String [] columnNames;
    private MutiTable mutiTable;
    private final String COL_HEADER_SUFFIX = "].header";
    private final String COL_NAME_SUFFIX = "].value.name";
    private final String COL_TYPE_SUFFIX = "].value.type";
    private final String COL_WIDTH_SUFFIX = "].proportion.width";
    private final String COLUMN_TOTAL = "column.total";
    private final String PREFIX_COL_HEADER = "column[";
    private ResourceMap resourceMap;

    {
	resourceMap = new DefaultResourceMap (CadTablePanel.class);
    }

    CadTablePanel(Documents documents) {
	this.documents = documents;
    }

    @Override
    public double getHorizontalProportion() {
	return 0.99d;
    }

    @Override
    public double getVerticalProportion() {
	return 0.7d;
    }

    public Object [] [] buildTableData() throws Exception {
	if (documents == null) {
	    return null;
	}
	List<Document> documentL = documents.getDocuments ();
	Object [] [] data = new Object [documentL.size ()] [columnNames.length];
	if (documentL != null && !documentL.isEmpty ()) {
	    for (int i = 0; i < documentL.size (); i++) {
		Document document = documentL.get (i);
		data[i] = buildTableRowData (document,i);
	    }
	}
	return data;
    }

    public Object [] buildTableRowData(Document document, int rowIndex) throws Exception {
	if (logger.isDebugEnabled ()) {
	    logger.debug ("document is -> " + document);
	}
	CadDocument cadDocument = null;
	if (ClientUtils.enableObject (document)) {
	    cadDocument = (CadDocument) document.getObject ();
	} else {
	    cadDocument = null;
	}
	Object [] rowData = new Object [columnNames.length];
	for (int col = 0; col < columnNames.length; col++) {
	    String columnName = getCellContentName (col);
	    Class<?> clazz = getCellContentType (col);
	    int colWidth = getColumnWidth (col);
	    if (logger.isDebugEnabled ()) {
		logger.debug (col + " columnName is -> " + columnName + " columnType is -> " + clazz
			+ " colWidth is -> " + colWidth);
	    }
	    TableColumn tableColumn = mutiTable.getColumnModel ().getColumn (col);
	    tableColumn.setPreferredWidth (colWidth);
	    tableColumn.setMaxWidth (colWidth);
	    tableColumn.setMinWidth (colWidth);

	    if (Boolean.class.isAssignableFrom (clazz)) {
		rowData[col] = false;
	    } else if (Integer.class.isAssignableFrom (clazz)) {
		rowData[col] = rowIndex + 1;
	    } else if (JButton.class.isAssignableFrom (clazz)) {
		rowData[col] = new JButton ();
	    } else if (CadStatus.class.isAssignableFrom (clazz)) {
		rowData[col] = document.getCadStatus ().getDisplayName ();
	    } else if (String.class.isAssignableFrom (clazz)) {
		rowData[col] = getValueByField (( cadDocument == null ? document : cadDocument ),columnName);
	    } else {
		logger.debug ("not support class type is -> " + clazz);
	    }
	}
	return rowData;
    }

    public static Object getValueByField(Object object, String fieldName)
	    throws IllegalArgumentException, IllegalAccessException {
	if (logger.isDebugEnabled ()) {
	    logger.debug ("getValueByField is -> object=[" + object.getClass ().getName () + "] fieldName=[" + fieldName
		    + "]");
	}
	if (null == fieldName || fieldName.equals ("")) return "";
	String [] fieldStrs = fieldName.split ("\\.");
	if (logger.isDebugEnabled ()) {
	    logger.debug (fieldStrs.length);
	}
	Object tempObj = object;
	for (String fieldStr : fieldStrs) {
	    tempObj = getFieldValue (tempObj,fieldStr,null);
	    if (logger.isDebugEnabled ()) {
		logger.debug ("field is -> " + fieldStr + " value is -> " + tempObj);
	    }
	}
	return tempObj;
    }

    public static Object getFieldValue(Object obj, String field, Class<?> superClass)
	    throws IllegalArgumentException, IllegalAccessException {
	if (obj == null) return null;
	Object result = null;
	Class<?> clazz = superClass == null ? obj.getClass () : superClass;
	Field f = null;
	try {
	    f = clazz.getDeclaredField (field);
	    f.setAccessible (true);
	    result = f.get (obj);
	}
	catch(NoSuchFieldException e) {
	    // e.printStackTrace ();
	    if (clazz.getSuperclass () != null) {
		result = getFieldValue (obj,field,clazz.getSuperclass ());
		if (result != null) {
		    return result;
		}
	    }
	}
	return result;
    }

    private String getCellContentName(int column) {
	return resourceMap.getString (PREFIX_COL_HEADER + column + COL_NAME_SUFFIX);
    }

    private Class<?> getCellContentType(int column) throws ClassNotFoundException {
	String type = resourceMap.getString (PREFIX_COL_HEADER + column + COL_TYPE_SUFFIX);
	ClientAssert.hasText (type,"Column " + column + " type is required");
	return Class.forName (type);
    }

    private List<String> getColumnHeaders() {
	int total = resourceMap.getInt (COLUMN_TOTAL);
	if (logger.isDebugEnabled ()) {
	    logger.debug ("total is -> " + total);
	}
	List<String> headers = new ArrayList<String> ();
	for (int column = 0; column < total; column++) {
	    String header = resourceMap.getString (PREFIX_COL_HEADER + column + COL_HEADER_SUFFIX);
	    if (logger.isDebugEnabled ()) {
		logger.debug ("header is -> " + header + " --> " + ( PREFIX_COL_HEADER + column + COL_HEADER_SUFFIX ));
		logger.debug (header.equals (PREFIX_COL_HEADER + column + COL_HEADER_SUFFIX));
	    }
	    headers.add (header.equals (PREFIX_COL_HEADER + column + COL_HEADER_SUFFIX) ? "" : header);
	}
	return headers;
    }

    private int getColumnWidth(int column) {
	String proportion = resourceMap.getString (PREFIX_COL_HEADER + column + COL_WIDTH_SUFFIX);
	if (logger.isDebugEnabled ()) {
	    logger.debug ("getColumnWidth " + ( PREFIX_COL_HEADER + column + COL_WIDTH_SUFFIX ) + " proportion is -> "
		    + proportion);
	}
	return new BigDecimal (getParent ().getPreferredSize ().width).multiply (new BigDecimal (proportion))
		.intValue ();
    }

    public void buildHeaderColumn() {
	List<String> headerList = getColumnHeaders ();
	if (logger.isDebugEnabled ()) {
	    logger.debug ("headerList is -> " + headerList);
	}
	columnNames = new String [headerList.size ()];
	for (int index = 0; index < headerList.size (); index++) {
	    columnNames[index] = headerList.get (index);
	}
    }

    public <T> T getFirstObjOfList(List<T> list) {
	if (list == null || list.isEmpty ()) {
	    return null;
	}
	for (T t : list) {
	    return t;
	}
	return null;
    }

    /**
     * @author zjw
     * @param text
     *            update content
     * @param type
     *            all/check/clear
     * @return 2018年10月24日下午3:17:25
     */
    public String refreshContainerData(String text, String type) {
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshContainerData is -> text=[" + text + "] type=[" + type + "]");
	}
	if (StringUtils.equals (type,"all")) {
	    return refreshAll (text,"container");
	} else if (StringUtils.equals (type,"check")) {
	    return refreshCheck (text,"container");
	} else if (StringUtils.equals (type,"clear")) {
	    return refreshClear ("container");
	}
	return null;
    }

    /**
     * @author zjw
     * @param text
     *            update content
     * @param type
     *            all/check/clear
     * @return 2018年10月24日下午3:17:25
     */
    public String refreshFolderData(String text, String type) {
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshFolderData is -> text=[" + text + "] type=[" + type + "]");
	}
	if (StringUtils.equals (type,"all")) {
	    return refreshAll (text,"folder");
	} else if (StringUtils.equals (type,"check")) {
	    return refreshCheck (text,"folder");
	} else if (StringUtils.equals (type,"clear")) {
	    return refreshClear ("folder");
	}
	return null;
    }

    /**
     * @author zjw
     * @param text
     *            update content
     * @param type
     *            container/folder
     * @return 2018年10月24日下午3:17:59
     */
    private String refreshAll(String text, String type) {
	StringBuffer buf = new StringBuffer ();
	int rowCount = mutiTable.getRowCount ();
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshAll mutiTable rowCount is -> " + rowCount);
	}
	if (StringUtils.equals (type,"container")) {
	    for (int i = 0; i < rowCount; i++) {
		String status = (String) mutiTable.getModel ().getValueAt (i,DEFAULT_STATUS_COLUMN);
		boolean enable = ClientUtils.enableObject (status);
		if (enable) {
		    mutiTable.getModel ().setValueAt (text,i,DEFAULT_CONTAINER_COLUMN);
		    buf.append ("表格中第" + ( i + 1 ) + "行数据的产品容器信息已更新为[" + text + "]");
		} else {
		    buf.append ("表格中第" + ( i + 1 ) + "行数据已在系统中存在,不能更新其产品容器信息.");
		}
	    }
	} else if (StringUtils.equals (type,"folder")) {
	    for (int i = 0; i < rowCount; i++) {
		String status = (String) mutiTable.getModel ().getValueAt (i,DEFAULT_STATUS_COLUMN);
		boolean enable = ClientUtils.enableObject (status);
		if (enable) {
		    mutiTable.getModel ().setValueAt (text,i,DEFAULT_FOLDER_COLUMN);
		    buf.append ("表格中第" + ( i + 1 ) + "行数据的文件夹信息已更新为[" + text + "]");
		} else {
		    buf.append ("表格中第" + ( i + 1 ) + "行数据已在系统中存在,不能更新其文件夹信息.");
		}
	    }
	}
	return buf.toString ();
    }

    /**
     * @author zjw
     * @param text
     *            update content
     * @param type
     *            container/folder
     * @return 2018年10月24日下午3:17:59
     */
    private String refreshCheck(String text, String type) {
	StringBuffer buf = new StringBuffer ();
	List<Integer> checkRowL = mutiTable.getAllCheckedRows ();
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshCheck mutiTable checkRowIndex is -> " + checkRowL);
	}
	if (StringUtils.equals (type,"container")) {
	    for (int i = 0; i < checkRowL.size (); i++) {
		Integer checkRowIndex = checkRowL.get (i);
		String status = (String) mutiTable.getModel ().getValueAt (checkRowIndex,DEFAULT_STATUS_COLUMN);
		boolean enable = ClientUtils.enableObject (status);
		if (enable) {
		    mutiTable.getModel ().setValueAt (text,checkRowIndex,DEFAULT_CONTAINER_COLUMN);
		    buf.append ("表格中第" + ( checkRowIndex + 1 ) + "行数据的产品容器信息已更新为[" + text + "]");
		} else {
		    buf.append ("表格中第" + ( checkRowIndex + 1 ) + "行数据已在系统中存在,不能更新其产品容器信息.");
		}
	    }
	} else if (StringUtils.equals (type,"folder")) {
	    for (int i = 0; i < checkRowL.size (); i++) {
		Integer checkRowIndex = checkRowL.get (i);
		String status = (String) mutiTable.getModel ().getValueAt (checkRowIndex,DEFAULT_STATUS_COLUMN);
		boolean enable = ClientUtils.enableObject (status);
		if (enable) {
		    mutiTable.getModel ().setValueAt (text,checkRowIndex,DEFAULT_FOLDER_COLUMN);
		    buf.append ("表格中第" + ( checkRowIndex + 1 ) + "行数据的文件夹信息已更新为[" + text + "]");
		} else {
		    buf.append ("表格中第" + ( checkRowIndex + 1 ) + "行数据已在系统中存在,不能更新其文件夹信息.");
		}
	    }
	}
	return buf.toString ();
    }

    /**
     * @author zjw
     * @param text
     *            update content
     * @param type
     *            container/folder
     * @return 2018年10月24日下午3:17:59
     */
    private String refreshClear(String type) {
	StringBuffer buf = new StringBuffer ();
	int rowCount = mutiTable.getRowCount ();
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshClear mutiTable rowCount is -> " + rowCount);
	}
	if (StringUtils.equals (type,"container")) {
	    for (int i = 0; i < rowCount; i++) {
		String status = (String) mutiTable.getModel ().getValueAt (i,DEFAULT_STATUS_COLUMN);
		boolean enable = ClientUtils.enableObject (status);
		if (enable) {
		    mutiTable.getModel ().setValueAt (null,i,DEFAULT_CONTAINER_COLUMN);
		    buf.append ("表格中第" + ( i + 1 ) + "行数据的产品容器信息已被清空.");
		} else {
		    buf.append ("表格中第" + ( i + 1 ) + "行数据已在系统中存在,不能更新其产品容器信息.");
		}
	    }
	} else if (StringUtils.equals (type,"folder")) {
	    for (int i = 0; i < rowCount; i++) {
		String status = (String) mutiTable.getModel ().getValueAt (i,DEFAULT_STATUS_COLUMN);
		boolean enable = ClientUtils.enableObject (status);
		if (enable) {
		    mutiTable.getModel ().setValueAt (null,i,DEFAULT_FOLDER_COLUMN);
		    buf.append ("表格中第" + ( i + 1 ) + "行数据的文件夹信息已被清空.");
		} else {
		    buf.append ("表格中第" + ( i + 1 ) + "行数据已在系统中存在,不能更新其文件夹信息.");
		}
	    }
	}
	return buf.toString ();
    }

    public LinkedHashMap<String, Integer> getCheckEnableColumnValue() {
	LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer> ();
	List<Integer> checkRowL = mutiTable.getAllCheckedRows ();
	if (logger.isDebugEnabled ()) {
	    logger.debug ("getCheckEnableColumnValue checkRowL is -> " + checkRowL);
	}
	for (int i = 0; i < checkRowL.size (); i++) {
	    Integer checkRowIndex = checkRowL.get (i);
	    String status = (String) mutiTable.getModel ().getValueAt (checkRowIndex,DEFAULT_STATUS_COLUMN);
	    boolean enable = ClientUtils.enableObject (status);
	    if (enable) {
		String number = (String) mutiTable.getModel ().getValueAt (checkRowIndex,DEFAULT_NUMBER_COLUMN);
		map.put (number,checkRowIndex);
	    }
	}
	return map;
    }

    public LinkedHashMap<String, Integer> getAllEnableColumnValue() {
	LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer> ();
	int rowCount = mutiTable.getRowCount ();
	if (logger.isDebugEnabled ()) {
	    logger.debug ("getAllEnableColumnValue mutiTable rowCount is -> " + rowCount);
	}
	for (int i = 0; i < rowCount; i++) {
	    String status = (String) mutiTable.getModel ().getValueAt (i,DEFAULT_STATUS_COLUMN);
	    boolean enable = ClientUtils.enableObject (status);
	    if (enable) {
		String number = (String) mutiTable.getModel ().getValueAt (i,DEFAULT_NUMBER_COLUMN);
		map.put (number,i);
	    }
	}
	return map;
    }

    public String refreshContainerByNumber(String number, String containerName) {
	StringBuffer buf = new StringBuffer ();
	Integer rowIndex = getRowIndexByValue (number,DEFAULT_NUMBER_COLUMN);
	if (rowIndex != null) {
	    mutiTable.getModel ().setValueAt (containerName,rowIndex,DEFAULT_CONTAINER_COLUMN);
	    buf.append ("第[" + ( rowIndex + 1 ) + "行的产品容器信息已成功更新为[" + containerName + "]");
	} else {
	    buf.append ("根据编号[" + number + "]和编号所在列从0开始[" + DEFAULT_NUMBER_COLUMN + "]找不到对应的数据行,操作无法执行.");
	}
	return buf.toString ();
    }

    public String refreshFolderByNumber(String number, String folderName) {
	StringBuffer buf = new StringBuffer ();
	Integer rowIndex = getRowIndexByValue (number,DEFAULT_NUMBER_COLUMN);
	if (rowIndex != null) {
	    mutiTable.getModel ().setValueAt (folderName,rowIndex,DEFAULT_FOLDER_COLUMN);
	    buf.append ("第[" + ( rowIndex + 1 ) + "行的文件夹信息已成功更新为[" + folderName + "]");
	} else {
	    buf.append ("根据编号[" + number + "]和编号所在列从0开始[" + DEFAULT_NUMBER_COLUMN + "]找不到对应的数据行,操作无法执行.");
	}
	return buf.toString ();
    }

    private Integer getRowIndexByValue(String value, Integer column) {
	int rowCount = mutiTable.getRowCount ();
	for (int i = 0; i < rowCount; i++) {
	    String tempValue = (String) mutiTable.getModel ().getValueAt (i,column);
	    if (StringUtils.equals (tempValue,value)) {
		return i;
	    }
	}
	return null;
    }

//    public LinkedHashMap<Integer, HashMap<Integer, Object>> getAllCheckRowValues() {
//	LinkedHashMap<Integer, HashMap<Integer, Object>> map = new LinkedHashMap<Integer, HashMap<Integer, Object>> ();
//	List<Integer> checkRowL = mutiTable.getAllCheckedRows ();
//	int colCount = mutiTable.getModel ().getColumnCount ();
//	if (logger.isDebugEnabled ()) {
//	    logger.debug ("getAllCheckRowValues checkRowL is -> " + checkRowL);
//	}
//	for (int i = 0; i < checkRowL.size (); i++) {
//	    Integer checkRowIndex = checkRowL.get (i);
//	    HashMap<Integer, Object> columnValueMap = new HashMap<Integer, Object> ();
//	    for (int j = 0; j < colCount; j++) {
//		Object value = mutiTable.getModel ().getValueAt (checkRowIndex,j);
//		columnValueMap.put (j,value);
//	    }
//	    map.put (checkRowIndex,columnValueMap);
//	}
//	return map;
//    }

    public Object getCellValue(int rowIndex, int columnIndex) {
	return mutiTable.getModel ().getValueAt (rowIndex,columnIndex);
    }

    public void refreshCheckRowStatus(Documents tempDocuments) {
	this.documents = tempDocuments;
	List<Integer> checkRowL = mutiTable.getAllCheckedRows ();
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshCheckRowStatus mutiTable checkRowIndex is -> " + checkRowL);
	}
	HashMap<String, Document> tempMap = ClientUtils.exchangeDocuments (tempDocuments);
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshCheckRowStatus tempMap is -> " + tempMap);
	}
	for (int i = 0; i < checkRowL.size (); i++) {
	    Integer checkRowIndex = checkRowL.get (i);
	    String number = (String) mutiTable.getModel ().getValueAt (checkRowIndex,DEFAULT_NUMBER_COLUMN);
	    Document document = tempMap.get (number);
	    if (logger.isDebugEnabled ()) {
		logger.debug ("refreshCheckRowStatus number is -> " + number + " document is -> " + document);
	    }
	    mutiTable.getModel ().setValueAt (document.getCadStatus ().getDisplayName (),checkRowIndex,
		    DEFAULT_STATUS_COLUMN);
	}
    }
    
    public Documents mergeCommitParam () {
	if (logger.isDebugEnabled ()) {
	    logger.debug ("mergeCommitParam before is -> " + documents);
	}
	//LinkedHashMap<rowIndex, HashMap<columnIndex,Object>>
	List<Document> documentL = documents.getDocuments ();
	//sort order
	List<Integer> checkRowL = mutiTable.getAllCheckedRows ();
	if (logger.isDebugEnabled ()) {
	    logger.debug ("mergeCommitParam mutiTable checkRowIndex is -> " + checkRowL);
	}
	for (int i = 0; i < checkRowL.size (); i++) {
	    Integer checkRowIndex = checkRowL.get (i);
	    String status = (String) mutiTable.getModel ().getValueAt (checkRowIndex,DEFAULT_STATUS_COLUMN);
	    boolean enable = ClientUtils.enableObject (status);
	    if (enable) {
		Document document = documentL.get (checkRowIndex);
		String containerName = (String)mutiTable.getModel ().getValueAt (checkRowIndex,DEFAULT_CONTAINER_COLUMN);
		String folderName = (String)mutiTable.getModel ().getValueAt (checkRowIndex,DEFAULT_FOLDER_COLUMN);
		document.setContainer (new Container (new SimplePdmLinkProduct (null,containerName),new SimpleFolder (null,folderName)));
	    } 
	}
	if (logger.isDebugEnabled ()) {
	    logger.debug ("mergeCommitParam after is -> " + documents);
	}
	return documents;
    }

    @Override
    public void initialize() {
	// build header column
	buildHeaderColumn ();

	// init table model
	MutiTableModel tableModel = new MutiTableModel (columnNames);

	// new jtable
	mutiTable = new MutiTable ();

	// set data model
	mutiTable.setModel (tableModel);
	// set table enabled
	mutiTable.setEnabled (true);
	// set checkHeader column
	mutiTable.setCheckHeaderColumn (CHECKHEADERCOLUMN);
	// set button column
	TableColumn column = mutiTable.getColumnModel ().getColumn (BOTTON_COLUMN_INDEX);
	column.setCellRenderer (new BottonColumnRenderer ());

	// init table data
	try {
	    tableModel.refreshContents (buildTableData ());
	}
	catch(Exception e) {
	    e.printStackTrace ();
	}

	// set jtable dimension
	mutiTable.setPreferredSize (new Dimension (getPreferredSize ().width,
		( (Double) ( getPreferredSize ().height * TABLE_HEIGHT_PROPORTION ) ).intValue ()));
	// set jtable border
	mutiTable.setBorder (BorderFactory.createLineBorder (Color.CYAN));

	// new jscrollpane and set jtable into
	JScrollPane scrollPane = new JScrollPane (mutiTable);
	scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scrollPane.setWheelScrollingEnabled (true);
	// set jscrollpane dimension
	scrollPane.setPreferredSize (
		new Dimension (( (Double) ( getPreferredSize ().width * SCROLL_WIDTH_PROPORTION ) ).intValue (),
			( (Double) ( getPreferredSize ().height * SCROLL_HEIGHT_PROPORTION ) ).intValue ()));
	// set jscrollpane border
	scrollPane.setBorder (BorderFactory.createLineBorder (Color.ORANGE));

	// add jscrollpane to panel
	add (scrollPane);

	// set panel border
	setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
		getResourceMap ().getString (TITLE),TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,
		toolkit.getFont ()));
    }

}
