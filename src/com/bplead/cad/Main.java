package com.bplead.cad;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.bplead.cad.ui.CADMainFrame;
import com.bplead.cad.ui.ChooseDrawingDialog;
import com.bplead.cad.util.ClientUtils;

import priv.lee.cad.util.ClientAssert;

public class Main {

    public static void main(String [] args) {
	ClientAssert.notEmpty (args,"Miss starting arguments");

	ClientUtils.args.setType (args[0]);
	EventQueue.invokeLater (new Runnable () {
	    public void run() {
//	    	new LoginFrame().activate();
//		new CADMainFrame ().activate ();
//	    new SearchPDMLinkProductDialog(new CADMainFrame ()).activate();
//	    new ChooseDrawingDialog(new CADMainFrame ()).activate();
	    
//	    new BomDetailDialog(null,"TE001",new ArrayList<CADLink>()).activate();

//		new SearchForDownloadDialog(new CADMainFrame ()).activate();
//	    new FolderChooseDialog(new CADMainFrame (),new SimplePdmLinkProduct()).activate();

	    String ss = Class.class.getClass().getResource("/").getPath();
	    ss = ss + "dwglist.txt";
		System.out.println("ss--->"+ss);
	    String path = getCurrentDrawingPath(ss);
	    System.out.println("path--->"+path);
	    }
	});
    }

    public Object [] [] buildTestData() {
	Object [] [] data = new Object [] [] { { false, "1", "1x1", "1x2", "1x3", "1x4", "1x5", "1x6" },
		{ false, "2", "2x1", "2x2", "2x3", "2x4", "2x5", "2x6" },
		{ false, "3", "3x1", "3x2", "3x3", "3x4", "3x5", "3x6" },
		{ false, "4", "4x1", "4x2", "4x3", "4x4", "4x5", "4x6" } };
	return data;
    }
    
    public static String getCurrentDrawingPath(String path) {
    	File file = new File(path);
    	if(file.exists()) {
    		try {
    			FileInputStream fileInputStream = new FileInputStream(file);
    			String code = getCharset(fileInputStream);
    			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,code);
    			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    			StringBuffer sb = new StringBuffer();
    	    	String text = "";
    			
    			while((text = bufferedReader.readLine())!=null) {
    				sb.append(text);
    			}
    			bufferedReader.close();
    			
    			return sb.toString();
    		}catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    	return "";
    }
    
	private static String getCharset(FileInputStream fileInputStream) throws IOException {
    	int p = (fileInputStream.read()<<8)+ fileInputStream.read();
    	String code = "";
    	switch(p) {
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


