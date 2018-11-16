package com.bplead.cad.util;

import java.io.File;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.ClientInstanceUtils;
import priv.lee.cad.util.PropertiesUtils;

public class ReadToXml {
	private static final String APPLICATION = "cad.application";
	private static final String ACTIVEDOC = "cad.activeDoc";
	private static final String COMMAND = "cad.commond";
	private static final String FUNCTION = "cad.function";
	private static final String XML = "cad.xml";

	public static String getCommandPath(String filePath) {
		ClientAssert.notNull(filePath, "filePath is null");
		String function = PropertiesUtils.readProperty(FUNCTION);
		ClientAssert.notNull(function, "function is null");
		String xml = PropertiesUtils.readProperty(XML);
		ClientAssert.notNull(xml, "xml is null");

		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append(PropertiesUtils.readProperty(FUNCTION));
		sb.append(" ");
		sb.append("\"");
		sb.append(ClientInstanceUtils.getTemporaryDirectory());
		sb.append(File.separator);
		sb.append(PropertiesUtils.readProperty(XML));
		sb.append("\"");
		sb.append(" ");
		sb.append(filePath);
		sb.append(")");
		sb.append("\n");
		System.out.println("sb.toString()--->"+sb.toString());

		return sb.toString();
	}

	public static void main(String[] args) {
		//test
		String filePath = "C:\\software\\Release20181103\\图纸\\中望\\CxxxM0000.dwg";
		String filePath1 = "C:\\Users\\bc\\Desktop\\图纸\\图纸\\中望\\M142B3100.dwg" ;
		filePath = "\"" +  filePath + "\" \"" + filePath1 +"\"";
		readToXml(filePath);
	}

	public static void readToXml(String filePath) {
		ComThread.InitSTA();
		// connect CAD Tools that has opened.if it doesn't exist opened CAD,will return null.
		String application = PropertiesUtils.readProperty(APPLICATION);
		ClientAssert.notNull(application, "application is null");
		ActiveXComponent zwApp = ActiveXComponent.connectToActiveInstance(application);
		// ZcadDocument
		String activeDoc = PropertiesUtils.readProperty(ACTIVEDOC);
		ClientAssert.notNull(activeDoc, "activeDoc is null");
		Dispatch doc = zwApp.getProperty(activeDoc).toDispatch();
		// call ZcadDocument.SendCommand
		String command = PropertiesUtils.readProperty(COMMAND);
		ClientAssert.notNull(command, "command is null");
		// get command path
		String commandPath = getCommandPath(filePath);
		ClientAssert.notNull(commandPath, "commandPath is null");

		Dispatch.call(doc, command, commandPath);
	}

}
