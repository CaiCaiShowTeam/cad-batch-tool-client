/**
 * CadTablePanel.java 2018年10月19日
 */
package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.bean.io.Documents;

import priv.lee.cad.model.ResourceMapper;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.util.StringUtils;

/**
 * @author zjw 2018年10月19日下午1:44:35
 */
public class CadTablePanel extends AbstractPanel implements ResourceMapper {

    private static final long serialVersionUID = 1402990192978044447L;

    private static final String TITLE = "title";
    
    private final int CHECKHEADERCOLUMN = 0;
    
    private final double TABLE_HEIGHT_PROPORTION = 1.05d;
    
    private final double SCROLL_HEIGHT_PROPORTION = 0.9d;
    
    private final double SCROLL_WIDTH_PROPORTION = 0.97d;

    private Documents documents;

    private String [] columnNames = { "", "序号", "图纸代号", "对比", "图纸名称", "状况", "产品容器", "文件夹" };

    private MutiTable mutiTable;

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

    public Object [] [] buildTableData() {
	if (documents == null) {
	    return null;
	}
	List<Document> documentL = documents.getDocuments ();
	Object [] [] data = new Object [documentL.size ()] [columnNames.length];
	if (documentL != null && !documentL.isEmpty ()) {
	    CadDocument cadDocument = null;
	    for (int i = 0; i < documentL.size (); i++) {
		Document document = documentL.get (i);
		if (StringUtils.isEmpty (document.getOid ())) {
		    cadDocument = (CadDocument) document.getObject ();
		} else {
		    cadDocument = null;
		}
		for (int col = 0; col < columnNames.length; col++) {
		    if (columnNames[col].equalsIgnoreCase ("")) {
			data[i][col] = false;
		    } else if (columnNames[col].equalsIgnoreCase ("序号")) {
			data[i][col] = i + 1;
		    } else if (columnNames[col].equalsIgnoreCase ("图纸代号")) {
			data[i][col] = cadDocument == null ? document.getNumber () : cadDocument.getNumber ();
		    } else if (columnNames[col].equalsIgnoreCase ("对比")) {
			data[i][col] = null;
		    } else if (columnNames[col].equalsIgnoreCase ("图纸名称")) {
			data[i][col] = cadDocument == null ? document.getName () : cadDocument.getName ();
		    } else if (columnNames[col].equalsIgnoreCase ("状况")) {
			data[i][col] = cadDocument == null ? ( document.getEditEnable () ? "检出" : "检入" ) : "";
		    } else if (columnNames[col].equalsIgnoreCase ("产品容器")) {
			data[i][col] = cadDocument == null ? document.getContainer ().getProduct ().getName () : null;
		    } else if (columnNames[col].equalsIgnoreCase ("文件夹")) {
			data[i][col] = cadDocument == null ? document.getContainer ().getFolder ().getName () : "";
		    }
		}
	    }
	}
	return data;
    }

    @Override
    public void initialize() {
	// init table model
	MutiTableModel tableModel = new MutiTableModel (columnNames);
	// init table data
	try {
	    tableModel.refreshContents (buildTableData ());
	}
	catch(Exception e) {
	    e.printStackTrace ();
	}
	// new jtable
	mutiTable = new MutiTable ();
	// set data model
	mutiTable.setModel (tableModel);
	// set table enabled
	mutiTable.setEnabled (true);
	// set checkHeader column
	mutiTable.setCheckHeaderColumn (CHECKHEADERCOLUMN);

	// set jtable dimension
	mutiTable.setPreferredSize (new Dimension (getPreferredSize ().width,
		( (Double) ( getPreferredSize ().height * TABLE_HEIGHT_PROPORTION) ).intValue ()));
	// set jtable border 
	mutiTable.setBorder (BorderFactory.createLineBorder (Color.CYAN));

	// new jscrollpane and set jtable into
	JScrollPane scrollPane = new JScrollPane (mutiTable);
	scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scrollPane.setWheelScrollingEnabled (true);
	// set jscrollpane dimension
	scrollPane.setPreferredSize (new Dimension (( (Double) ( getPreferredSize ().width * SCROLL_WIDTH_PROPORTION ) ).intValue (),
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
