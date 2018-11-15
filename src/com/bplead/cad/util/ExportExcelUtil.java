package com.bplead.cad.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

public class ExportExcelUtil {
	/**
	 *
	 * @author jiyideguiji
	 */
	private static Logger logger = Logger.getLogger(ExportExcelUtil.class.getName());
	FileOutputStream fos;
	JFileChooser chooser = new JFileChooser();
	JTable table;

	public ExportExcelUtil(JTable table) {
		this.table = table;
		chooser.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File file) {
				boolean returnBoolean = file.getName().indexOf("xls") != -1;
				return returnBoolean;
			}

			public String getDescription() {
				return "Microsoft Excel文件(xls)";
			}
		});

		chooser.showSaveDialog(null);
		File file = chooser.getSelectedFile();
		try {
			this.fos = new FileOutputStream(file + ".xls");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public void export() {
		if (logger.isDebugEnabled()) {
			logger.debug("export - start");
		}
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet hs = wb.createSheet();
		TableModel tm = table.getModel();
		int row = tm.getRowCount();
		int column = tm.getColumnCount();

		HSSFCellStyle style = wb.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		HSSFFont font = wb.createFont();
		font.setFontHeightInPoints((short) 11);
		style.setFont(font);

		HSSFCellStyle style1 = wb.createCellStyle();
		style1.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style1.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style1.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style1.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style1.setFillForegroundColor(HSSFColor.ORANGE.index);
		style1.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		HSSFFont font1 = wb.createFont();
		font1.setFontHeightInPoints((short) 15);
		font1.setBoldweight((short) 700);
		style1.setFont(font1);

		HSSFFont redFont = wb.createFont();
		redFont.setColor(HSSFColor.RED.index);
		HSSFFont blueFont = wb.createFont();
		blueFont.setColor(HSSFColor.BLUE.index);

		for (int i = 0; i < row + 1; i++) {
			HSSFRow hr = hs.createRow(i);
			for (int j = 0; j < column; j++) {
				if (i == 0) {
					String value = tm.getColumnName(j);
					hs.setColumnWidth(j, value.getBytes().length * 2 * 256);
					HSSFRichTextString srts = new HSSFRichTextString(value);
					HSSFCell hc = hr.createCell(j);
					hc.setCellStyle(style1);
					hc.setCellValue(srts);
				} else {
					Object obj = tm.getValueAt(i - 1, j);
					String value = "";
					if (obj != null) {
						value = obj.toString();
					}
					if (value.indexOf("[") != -1 && value.indexOf("]") != -1) {
						String redValue = value.substring(value.indexOf("["), value.indexOf("]") + 1);
						String blueValue = value.substring(value.lastIndexOf("["), value.lastIndexOf("]") + 1);
						value = redValue + blueValue;

						HSSFRichTextString srts = new HSSFRichTextString(value);
						srts.applyFont(value.indexOf("["), value.indexOf("]") + 1, redFont);
						srts.applyFont(value.lastIndexOf("["), value.lastIndexOf("]") + 1, blueFont);

						HSSFCell hc = hr.createCell(j);
						hc.setCellStyle(style);
						hc.setCellValue(srts);
					} else {
						HSSFRichTextString srts = new HSSFRichTextString(value);
						
						HSSFCell hc = hr.createCell(j);
						hc.setCellStyle(style);
						hc.setCellValue(srts);
					}
				}
			}
		}

		try {
			wb.write(fos);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("export - end");
		}
	}
}
