package com.bplead.cad;

import java.awt.EventQueue;

import com.bplead.cad.ui.CADMainFrame;
import com.bplead.cad.ui.ChooseDrawingDialog;
import com.bplead.cad.util.ClientUtils;

import priv.lee.cad.util.ClientAssert;

public class Main {

	public static void main(String[] args) {
		ClientAssert.notEmpty(args, "Miss starting arguments");

		ClientUtils.args.setType(args[0]);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new ChooseDrawingDialog(new CADMainFrame()).activate();
			}
		});
	}

	public Object[][] buildTestData() {
		Object[][] data = new Object[][] { { false, "1", "1x1", "1x2", "1x3", "1x4", "1x5", "1x6" },
				{ false, "2", "2x1", "2x2", "2x3", "2x4", "2x5", "2x6" },
				{ false, "3", "3x1", "3x2", "3x3", "3x4", "3x5", "3x6" },
				{ false, "4", "4x1", "4x2", "4x3", "4x4", "4x5", "4x6" } };
		return data;
	}
}
