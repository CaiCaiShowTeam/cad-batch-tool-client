package com.bplead.cad.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractDialog;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.ui.Option;
import priv.lee.cad.ui.OptionPanel;
import priv.lee.cad.ui.PromptTextField;

public class ChooseDrawingDialog extends AbstractDialog implements ActionListener {
	private final String BUTTON_ICON = "folder.search.icon";

	private static LayoutManager rightLayout = new FlowLayout(FlowLayout.RIGHT);
	private static LayoutManager leftLayout = new FlowLayout(FlowLayout.LEFT);

	private static final Logger logger = Logger.getLogger(ChooseDrawingDialog.class);
	private static final long serialVersionUID = 1336292047030719519L;
	private ConfirmPanel confirmPanel;
	private SearchResultPanel searchResultPanel;

	public ChooseDrawingDialog(Callback container) {
		super(ChooseDrawingDialog.class, container);
	}

	@Override
	public double getHorizontalProportion() {
		return 0.5;
	}

	@Override
	public double getVerticalProportion() {
		return 0.5;
	}

	@Override
	public void initialize() {
		logger.info("initialize " + getClass() + " layout...");
		setLayout(leftLayout);

		logger.info("initialize " + getClass() + " content...");
		searchResultPanel = new SearchResultPanel();
		add(searchResultPanel);

		logger.info("initialize " + getClass() + " content...");
		confirmPanel = new ConfirmPanel();
		add(confirmPanel);

		logger.info("initialize " + getClass() + "  completed...");
	}

	@Override
	public Object setCallbackObject() {
		return null;
	}

	class ConfirmPanel extends AbstractPanel implements ActionListener {
		private static final long serialVersionUID = -6481481565984135229L;
		protected PromptTextField setting;

		@Override
		public void actionPerformed(ActionEvent e) {
			new LocalFileChooser(JFileChooser.DIRECTORIES_ONLY, setting);
		}

		@Override
		public double getHorizontalProportion() {
			return 0.95d;
		}

		@Override
		public double getVerticalProportion() {
			return 0.15d;
		}

		@Override
		public void initialize() {
			setLayout(rightLayout);

			Container parent = getParent();
			while (!(parent instanceof Window)) {
				parent = parent.getParent();
			}
			Option confirm = new Option(Option.CONFIRM_BUTTON, null, (ActionListener) parent);

			add(new OptionPanel(Arrays.asList(confirm, Option.newCancelOption((Window) parent))));
		}
	}

	class SearchResultPanel extends AbstractPanel {

		private static final long serialVersionUID = -7416585921364617464L;
		protected ChooseDrawingTable table;
		private double TABLE_HEIGTH_PROPORTION = 0.75d;
		private double TABLE_WIDTH_PROPORTION = 0.97d;
		private final String TITLE = "title";

		@Override
		public double getHorizontalProportion() {
			return 0.95d;
		}

		@Override
		public double getVerticalProportion() {
			return 0.75d;
		}

		@Override
		public void initialize() {
			setLayout(leftLayout);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					getResourceMap().getString(TITLE), TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, toolkit.getFont()));

			Option addOption = new Option(null, BUTTON_ICON, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AddLocalFileChooser(JFileChooser.FILES_ONLY, table);
				}
			});
			addOption.setContentAreaFilled(false);
			addOption.setBorder(null);
			Option removeOption = new Option(null, BUTTON_ICON, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new RemoveLocalFileChooser(table);
				}
			});
			removeOption.setContentAreaFilled(false);
			removeOption.setBorder(null);

			OptionPanel optionPanel = new OptionPanel(Arrays.asList(addOption, removeOption));
			add(optionPanel);
			optionPanel.setPreferredSize(new Dimension(60, 20));

			table = new ChooseDrawingTable(null);
			JScrollPane sp = new JScrollPane(table);
			sp.setPreferredSize(new Dimension((int) (getPreferredSize().width * TABLE_WIDTH_PROPORTION),
					(int) (getPreferredSize().height * TABLE_HEIGTH_PROPORTION)));
			table.setColumnWidth();
			add(sp);
		}
	}
}