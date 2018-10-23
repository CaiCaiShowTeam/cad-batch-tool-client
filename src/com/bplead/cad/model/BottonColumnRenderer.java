/**
 * BottonColumnRenderer.java 2018年10月22日
 */
package com.bplead.cad.model;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * @author zjw 2018年10月22日下午6:30:43
 */
public class BottonColumnRenderer extends JButton implements TableCellRenderer {

    private static final long serialVersionUID = -5107281419591482999L;
    
    private JButton botton;

    public BottonColumnRenderer() {
	botton = new JButton ();
	botton.setIcon (new ImageIcon (BottonColumnRenderer.class.getClassLoader ().getResource ("com/bplead/cad/resource/detail.png")));
	botton.setToolTipText ("点击将打开BOM比较页面");
	botton.setContentAreaFilled (false);
	botton.setPreferredSize (new Dimension (20,20));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, 
	    					   Object value, 
	    					   boolean isSelected, 
	    					   boolean hasFocus,
	    					   int row,
	    					   int column) {
	if (hasFocus) {
	    botton.setForeground (table.getForeground ());
	    botton.setBackground (UIManager.getColor ("Botton.background"));
	} else if (isSelected) {
	    botton.setForeground (table.getSelectionForeground ());
	    botton.setBackground (table.getSelectionBackground ());
	} else {
	    botton.setForeground (table.getForeground ());
	    botton.setBackground (UIManager.getColor ("Botton.background"));
	}
	return botton;
    }

}
