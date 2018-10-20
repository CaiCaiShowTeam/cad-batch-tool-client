package com.bplead.cad.ui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class HeaderCheckBoxRenderer extends JCheckBox implements TableCellRenderer {
  
    private static final long serialVersionUID = -3224639986882887200L;

    public HeaderCheckBoxRenderer() {
        this.setBorderPainted(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        return this;
    }

}
