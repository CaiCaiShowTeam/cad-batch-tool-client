package com.bplead.cad.util;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.SimpleFolder;
import com.bplead.cad.bean.SimplePdmLinkProduct;
import com.bplead.cad.bean.io.Attachment;
import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.bean.io.Documents;
import com.bplead.cad.model.CustomPrompt;

import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.StringUtils;

public class ValidateUtils {

    private static final Logger logger = Logger.getLogger (ValidateUtils.class);

    public static void validateCheckin(Documents documents) {
	ClientAssert.notNull (documents,"Document is required");
	logger.info ("Validate checkin begin...");
	List<Document> documentL = documents.getDocuments ();
	ClientAssert.notNull (documentL,"documentL is required");
	for (Document document : documentL) {
	    
	    validateStatus (document);
	    
	    List<Attachment> attachments = document.getObject ().getAttachments ();
	    ClientAssert.notEmpty (attachments,buildPromptSuffix (document) + CustomPrompt.ATTACHMENTS_NULL);
	    
	    validateSuffix (attachments,buildPromptSuffix (document));

	    validateProduct (document.getContainer ().getProduct (),buildPromptSuffix (document));

	    validateFolder (document.getContainer ().getFolder (),buildPromptSuffix (document));
	}
	logger.info ("Validate checkin complete...");
    }
    
    public static void validateUndoCheckout (Documents documents) {
	ClientAssert.notNull (documents,"Document is required");
	logger.info ("Validate UndoCheckout begin...");
	List<Document> documentL = documents.getDocuments ();
	ClientAssert.notNull (documentL,"documentL is required");
	for (Document document : documentL) {
	    String oid = document.getOid ();
	    if (StringUtils.isEmpty (oid)) {
		ClientAssert.isTrue (false,buildPromptSuffix (document) + CustomPrompt.NO_PERSISTENCE);
	    } else {
		Boolean editEnable = document.getEditEnable ();
		if (editEnable != null && !editEnable) {
		    ClientAssert.isTrue (editEnable,buildPromptSuffix (document) + CustomPrompt.NOT_CHECKOUT);
		} 
	    }
	}
	logger.info ("Validate UndoCheckout complete...");
    }
    
    
    public static void validateCheckout (Documents documents) {
 	ClientAssert.notNull (documents,"Document is required");
 	logger.info ("Validate Checkout begin...");
 	List<Document> documentL = documents.getDocuments ();
 	ClientAssert.notNull (documentL,"documentL is required");
 	for (Document document : documentL) {
 	    String oid = document.getOid ();
 	    if (StringUtils.isEmpty (oid)) {
 		ClientAssert.isTrue (false,buildPromptSuffix (document) + CustomPrompt.NO_PERSISTENCE);
 	    } else {
 		Boolean editEnable = document.getEditEnable ();
 		if (editEnable != null && editEnable) {
 		    ClientAssert.isTrue (!editEnable,buildPromptSuffix (document) + CustomPrompt.HAS_CHECKOUT);
 		} 
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
	Boolean editEnable = document.getEditEnable ();
	//editEnable == null is not persistence
	if (editEnable == null || editEnable) {
	} else {
	    ClientAssert.isTrue (editEnable,buildPromptSuffix (document) + CustomPrompt.NOT_CHECKOUT);
	}
    }

    public static void validateFolder(SimpleFolder folder, String promptSuffix) {
	ClientAssert.notNull (folder,promptSuffix + CustomPrompt.FOLDER_NULL);
    }

    public static void validateProduct(SimplePdmLinkProduct product, String promptSuffix) {
	ClientAssert.notNull (product,promptSuffix + CustomPrompt.PRODUCT_NULL);
    }

    public static void validateSuffix(List<Attachment> attachments, String promptSuffix) {
	for (Attachment attachment : attachments) {
	    ClientAssert.isTrue (new File (attachment.getAbsolutePath ()).exists (),
		    promptSuffix + CustomPrompt.FILE_NOT_EXSIT);
	}
    }

}