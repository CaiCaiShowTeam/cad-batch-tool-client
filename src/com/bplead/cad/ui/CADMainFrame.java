package com.bplead.cad.ui;

import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractFrame;

public class CADMainFrame extends AbstractFrame implements Callback {

    private static final long serialVersionUID = 6115761238329106019L;

    /**
     * @param clazz
     */
    public CADMainFrame() {
	super (CADMainFrame.class);
	// TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see priv.lee.cad.model.Callback#call(java.lang.Object)
     */
    @Override
    public void call(Object object) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see priv.lee.cad.model.SelfAdaptionComponent#getHorizontalProportion()
     */
    @Override
    public double getHorizontalProportion() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see priv.lee.cad.model.SelfAdaptionComponent#getVerticalProportion()
     */
    @Override
    public double getVerticalProportion() {
	// TODO Auto-generated method stub
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see priv.lee.cad.model.TieContainer#initialize()
     */
    @Override
    public void initialize() {
	// TODO Auto-generated method stub

    }

}
