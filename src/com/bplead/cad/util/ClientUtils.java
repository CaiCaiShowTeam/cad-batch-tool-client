package com.bplead.cad.util;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.DataContent;
import com.bplead.cad.bean.SimpleDocument;
import com.bplead.cad.bean.SimpleFolder;
import com.bplead.cad.bean.SimplePdmLinkProduct;
import com.bplead.cad.bean.client.Temporary;
import com.bplead.cad.bean.constant.RemoteMethod;
import com.bplead.cad.bean.io.Attachment;
import com.bplead.cad.bean.io.AttachmentModel;
import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.CadDocuments;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.bean.io.Documents;
import com.bplead.cad.model.CustomPrompt;

import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.ClientInstanceUtils;
import priv.lee.cad.util.PropertiesUtils;

public class ClientUtils extends ClientInstanceUtils {

    public static StartArguments args = new StartArguments ();
    private static final int BUFFER_SIZE = 2 * 1024;
    private final static String CAD_PRIMARY_SUFFIX = "wt.caddoc.primary.file.suffix";
    public static String cadPrimarySuffix = PropertiesUtils.readProperty (CAD_PRIMARY_SUFFIX);
    private static final String CONFIG_SUFFIX = "wt.caddoc.config.file.suffix";
    public static String configSuffix = PropertiesUtils.readProperty (CONFIG_SUFFIX);
    private static final Logger logger = Logger.getLogger (ClientUtils.class);
    private static final String OID = "oid";
    public static Temporary temprary = new Temporary ();
    private static final String ZIP = ".zip";

    public static List<Attachment> buildAttachments(AttachmentModel model, String primarySuffix) {
	List<Attachment> attachments = model.getAttachments ();
	for (Attachment attachment : attachments) {
	    File file = new File (attachment.getAbsolutePath ());
	    attachment.setName (file.getName ());
	    attachment.setPrimary (file.getName ().toLowerCase ().endsWith (primarySuffix));
	}
	return attachments;
    }
    
    /**
     * TODO 
     * @author zjw
     * @param cadDocuments
     * @return 
     * 2018年10月17日下午3:29:38
     */
    public static Documents initialize(CadDocuments cadDocuments) {
//	ClientAssert.notNull (cadDocuments,"cadDocuments is required");
//	return invoke (RemoteMethod.INIT_DATA, new Class<?> [] { CadDocuments.class }, new Object [] { cadDocuments },
//		Documents.class);
	List<Document> docList = new ArrayList<Document>();
	for (CadDocument cadDocument : cadDocuments.getCadDocs ()) {
	    Document document = new Document ();
	    document.setObject (cadDocument);
	    docList.add (document);
	}
	Documents document = new Documents ();
	document.setDocuments (docList);
	return document;
    }

    public static boolean checkin(Documents documents) {
	ClientAssert.notNull (documents,"documents is required");
	return invoke (RemoteMethod.CHECKIN,new Class<?> [] { Documents.class },new Object [] { documents },
		Boolean.class);
    }
    
    public static List<?> undoCheckout(Documents documents) {
	ClientAssert.notNull (documents,"documents is required");
	return invoke (RemoteMethod.UNDO_CHECKOUT,new Class<?> [] { Documents.class },new Object [] { documents },
		List.class);
    }
    
    public static List<?> checkout(Documents documents) {
	ClientAssert.notNull (documents,"documents is required");
	return invoke (RemoteMethod.CHECKOUT,new Class<?> [] { Documents.class },new Object [] { documents },
		List.class);
    }

    public static DataContent checkoutAndDownload(List<SimpleDocument> documents) {
	ClientAssert.notEmpty (documents,"Documents is requried");
	return invoke (RemoteMethod.CHECKOUT_DOWNLOAD,new Class<?> [] { List.class },new Object [] { documents },
		DataContent.class);
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
	BufferedOutputStream bos = new BufferedOutputStream (new FileOutputStream (filePath));
	byte [] bytesIn = new byte [BUFFER_SIZE];
	int read = 0;
	while (( read = zipIn.read (bytesIn) ) != -1) {
	    bos.write (bytesIn,0,read);
	}
	bos.close ();
    }

    public static String getDocumentOid(String primarySuffix, List<Attachment> attachments) {
	try {
	    File configFile = null;
	    for (Attachment attachment : attachments) {
		if (attachment.isPrimary ()) {
		    configFile = new File (attachment.getAbsolutePath ().replace (primarySuffix,configSuffix));
		    break;
		}
	    }

	    if (configFile != null && configFile.exists ()) {
		Properties properties = new Properties ();
		properties.load (new FileInputStream (configFile));
		return properties.getProperty (OID);
	    }
	}
	catch(Exception e) {
	    e.printStackTrace ();
	}
	return null;
    }

    public static SimpleFolder getSimpleFolders(SimplePdmLinkProduct container) {
	ClientAssert.notNull (container,"container is requried");
	return invoke (RemoteMethod.GET_SIMPLE_FOLDERS,new Class<?> [] { SimplePdmLinkProduct.class },
		new Object [] { container },SimpleFolder.class);
    }

    @SuppressWarnings("unchecked")
    public static List<SimplePdmLinkProduct> getSimplePdmLinkProducts() {
	return invoke (RemoteMethod.GET_SIMPLE_CONTAINERS,null,null,List.class);
    }

    public static void open(File directory) {
	if (directory == null) {
	    return;
	}

	File [] files = directory.listFiles ();
	for (File file : files) {
	    if (file.isDirectory ()) {
		open (file);
	    } else {
		if (file.getName ().endsWith (cadPrimarySuffix)) {
		    try {
			Desktop.getDesktop ().open (file);
		    }
		    catch(IOException e) {
			logger.error ("Failed to open file:" + file.getAbsolutePath ());
		    }
		}
	    }
	}
    }

//    @SuppressWarnings("unchecked")
//    public static List<SimpleDocument> search(String number, String name) {
//	ClientAssert.isTrue (StringUtils.hasText (number) || StringUtils.hasText (name),"Number or name is requried");
//	return invoke (RemoteMethod.SEARCH,new Class<?> [] { String.class, String.class },
//		new Object [] { number, name },List.class);
//    }

    public static File unzip(File zipFile) {
	ClientAssert.notNull (zipFile,"Zip file is required");

	File directory = zipFile.getParentFile ();
	try {
	    ZipInputStream zipIn = new ZipInputStream (new FileInputStream (zipFile));
	    ZipEntry entry = zipIn.getNextEntry ();
	    while (entry != null) {
		String filePath = directory.getAbsolutePath () + File.separator + entry.getName ();
		if (!entry.isDirectory ()) {
		    File file = new File (filePath);
		    if (!file.getParentFile ().exists ()) {
			file.getParentFile ().mkdirs ();
		    }
		    extractFile (zipIn,filePath);
		}
		zipIn.closeEntry ();
		entry = zipIn.getNextEntry ();
	    }
	    zipIn.close ();
	}
	catch(IOException e) {
	    e.printStackTrace ();
	    ClientAssert.isTrue (true,CustomPrompt.FAILD_OPTION);
	}
	finally {
	    zipFile.delete ();
	}
	return new File (directory,zipFile.getName ().replace (ZIP,""));
    }

    public static class StartArguments {

	public static final String CAD = "cad";
	private String type;

	public StartArguments() {

	}

	public StartArguments(String type) {
	    this.type = type;
	}

	public String getType() {
	    return type;
	}

	public void setType(String type) {
	    this.type = type;
	}
    }
}
