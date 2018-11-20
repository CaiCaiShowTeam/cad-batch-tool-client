package com.bplead.cad.util;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.SimpleFolder;
import com.bplead.cad.bean.SimplePdmLinkProduct;
import com.bplead.cad.bean.io.Attachment;
import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.CadStatus;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.bean.io.Documents;
import com.bplead.cad.bean.io.PartCategory;
import com.bplead.cad.model.CustomPrompt;

import priv.lee.cad.ui.RuntimeExceptionPanel;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.CollectionUtils;
import priv.lee.cad.util.StringUtils;

public class ValidateUtils {

    private static final Logger logger = Logger.getLogger (ValidateUtils.class);
    
    public static String validateComfirm (Documents documents) {
	logger.info ("validateComfirm begin...");
	List<Integer> checkRows = documents.getCheckRows ();
	List<Document> documentL = documents.getDocuments ();
	StringBuffer buf = new StringBuffer ();
	for (int i = 0; i < documentL.size (); i++) {
	    if (!checkRows.contains (i)) {
		continue;
	    }
	    Document document = documentL.get (i);
	    String checkConfirm = checkForConfirm(document);
	    if (StringUtils.isEmpty (checkConfirm)) {
	    } else {
		buf.append (checkConfirm);
	    }
	}
	logger.info ("validateComfirm end...");
	return buf.toString ();
    }

    public static void validateCheckin(Documents documents) {
	ClientAssert.notNull (documents,"documents is required");
	List<Integer> checkRows = documents.getCheckRows ();
	ClientAssert.isTrue (!CollectionUtils.isEmpty (checkRows),CustomPrompt.SELECTED_ITEM_NULL);
	logger.info ("Validate checkin begin...");
	List<Document> documentL = documents.getDocuments ();
	ClientAssert.notNull (documentL,"documentL is required");
	for (int i = 0; i < documentL.size (); i++) {
	    if (!checkRows.contains (i)) {
		continue;
	    }
	    Document document = documentL.get (i);
	    validateStatus (document);
	    List<Attachment> attachments = document.getObject ().getAttachments ();
	    ClientAssert.notEmpty (attachments,buildPromptSuffix (document) + RuntimeExceptionPanel.DELIM +  CustomPrompt.ATTACHMENTS_NULL);
	    //TODO 校验必填项
	    String checkPrompt = checkForPrompt (document);
	    ClientAssert.notNull (checkPrompt,checkPrompt);
	    
	    validateSuffix (attachments,buildPromptSuffix (document));

	    validateProduct (document.getContainer ().getProduct (),buildPromptSuffix (document));

	    validateFolder (document.getContainer ().getFolder (),buildPromptSuffix (document));
	}
	logger.info ("Validate checkin complete...");
    }

    public static void validateUndoCheckout(Documents documents) {
	ClientAssert.notNull (documents,"documents is required");
	List<Integer> checkRows = documents.getCheckRows ();
	ClientAssert.isTrue (!CollectionUtils.isEmpty (checkRows),CustomPrompt.SELECTED_ITEM_NULL);
	logger.info ("Validate UndoCheckout begin...");
	List<Document> documentL = documents.getDocuments ();
	ClientAssert.notNull (documentL,"documentL is required");
	for (int i = 0; i < documentL.size (); i++) {
	    if (!checkRows.contains (i)) {
		continue;
	    }
	    Document document = documentL.get (i);
	    CadStatus cadStatus = document.getCadStatus ();
	    if (cadStatus == CadStatus.NOT_EXIST) {
		ClientAssert.isTrue (false,buildPromptSuffix (document) + RuntimeExceptionPanel.DELIM +  CustomPrompt.NO_PERSISTENCE);
	    } else if (cadStatus == CadStatus.CHECK_IN) {
		ClientAssert.isTrue (false,buildPromptSuffix (document) + RuntimeExceptionPanel.DELIM +  CustomPrompt.NOT_CHECKOUT);
	    }
	}
	logger.info ("Validate UndoCheckout complete...");
    }

    public static void validateCheckout(Documents documents) {
	ClientAssert.notNull (documents,"Document is required");
	List<Integer> checkRows = documents.getCheckRows ();
	ClientAssert.isTrue (!CollectionUtils.isEmpty (checkRows),CustomPrompt.SELECTED_ITEM_NULL);
	logger.info ("Validate Checkout begin...");
	List<Document> documentL = documents.getDocuments ();
	ClientAssert.notNull (documentL,"documentL is required");
	for (int i = 0; i < documentL.size (); i++) {
	    if (!checkRows.contains (i)) {
		continue;
	    }
	    Document document = documentL.get (i);
	    CadStatus cadStatus = document.getCadStatus ();
	    if (cadStatus == CadStatus.NOT_EXIST) {
		ClientAssert.isTrue (false,buildPromptSuffix (document) + RuntimeExceptionPanel.DELIM +  CustomPrompt.NO_PERSISTENCE);
	    } else if (cadStatus == CadStatus.CHECK_OUT) {
		ClientAssert.isTrue (false,buildPromptSuffix (document) + RuntimeExceptionPanel.DELIM +  CustomPrompt.HAS_CHECKOUT);
	    }
	}
	logger.info ("Validate Checkout complete...");
    }

    public static String buildPromptSuffix(Document document) {
	if (document == null) {
	    return "";
	}
	StringBuffer sb = new StringBuffer ();
	String number = document.getNumber ();
	if (StringUtils.isEmpty (number)) {
	    CadDocument cadDocument = (CadDocument) document.getObject ();
	    sb.append (cadDocument.getNumber ()).append ("(").append (cadDocument.getName ()).append (")");
	} else {
	    sb.append (number).append ("(").append (document.getName ()).append (")");
	}
	return sb.toString ();
    }

    public static void validateStatus(Document document) {
	CadStatus cadStatus = document.getCadStatus ();
	// editEnable == null is not persistence
	if (cadStatus == CadStatus.CHECK_OUT || cadStatus == CadStatus.NOT_EXIST) {
	} else {
	    ClientAssert.isTrue (false,buildPromptSuffix (document) + RuntimeExceptionPanel.DELIM +  CustomPrompt.NOT_CHECKOUT);
	}
    }

    public static void validateFolder(SimpleFolder folder, String promptSuffix) {
	ClientAssert.notNull (folder,promptSuffix + RuntimeExceptionPanel.DELIM +  CustomPrompt.FOLDER_NULL);
    }

    public static void validateProduct(SimplePdmLinkProduct product, String promptSuffix) {
	ClientAssert.notNull (product,promptSuffix + RuntimeExceptionPanel.DELIM +  CustomPrompt.PRODUCT_NULL);
    }

    public static void validateSuffix(List<Attachment> attachments, String promptSuffix) {
	for (Attachment attachment : attachments) {
	    ClientAssert.isTrue (new File (attachment.getAbsolutePath ()).exists (),
		    promptSuffix + RuntimeExceptionPanel.DELIM + CustomPrompt.FILE_NOT_EXSIT);
	}
    }
    
    public static PartCategory getPartCategory (Document document) throws Exception {
	CadDocument cadDocument = (CadDocument) document.getObject ();
	return getPartCategory (cadDocument);
    }
    
    public static PartCategory getPartCategory (CadDocument cadDocument) throws Exception {
	String material = cadDocument.getMaterial ();//标题栏中的外购件图号
	String type = cadDocument.getSource ();//零部件类型
	//当零部件类型为"外购件"且标题栏中外购件图号不为空时为外购件
	if (StringUtils.equals (type,"外购件") && !StringUtils.isEmpty (material)) {
	    return PartCategory.BUY;
	} //零部件类型不存在或等于自制件且标题栏没有外购件图号为自制件
	else if ((StringUtils.isEmpty (type) || StringUtils.contains (type,"自制件")) && StringUtils.isEmpty (material)) {
	    return PartCategory.MAKE;  
	} else {
	    throw new Exception ("图纸[" + cadDocument.getNumber () + "]即不是外购件也不是自制件.");
	}
    }
    
    public static String checkForPrompt (Document document) {
	StringBuffer buf = new StringBuffer ();
	CadDocument cadDocument = (CadDocument) document.getObject ();
	String cindex = cadDocument.getNumber ();
	PartCategory category = null;
	try {
	    category = getPartCategory (cadDocument);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
	// 如果是自制件
	if (category == PartCategory.MAKE) {
	    if (cindex.startsWith ("2.")) {
		buf.append ("图纸代号为["+cindex+"]是以2.开始的自制件禁止检入系统");
	    }
	} else if (category == PartCategory.BUY) {
	    if (!(cindex.startsWith ("2.") || cindex.startsWith ("1."))) {
		buf.append ("图纸代号为["+cindex+"]不是以2.或者1.开始的我购件禁止检入系统");
	    }
	}
	return buf.toString ();
    }
    
    public static String checkForConfirm (Document document) {
	StringBuffer buf = new StringBuffer ();
	CadDocument cadDocument = (CadDocument) document.getObject ();
	String cindex = cadDocument.getNumber ();
	PartCategory category = null;
	try {
	    category = getPartCategory (cadDocument);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
	// 如果是自制件
	if (category == PartCategory.MAKE) {
	    if (cindex.startsWith ("1.")) {
		buf.append ("图纸代号["+cindex+"]开头为'1.',可能是外购件,请修改图纸代号后检入或者直接检入.");
	    }
	}
	return buf.toString ();
    }
    
    
    
}
