package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.SimplePdmLinkProduct;

import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractDialog;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.ui.Option;
import priv.lee.cad.ui.OptionPanel;
import priv.lee.cad.ui.PromptTextField;
import priv.lee.cad.util.StringUtils;

public class SearchPDMLinkProductDialog extends AbstractDialog implements ActionListener {

	private static LayoutManager rightLayout = new FlowLayout(FlowLayout.RIGHT);
	private static LayoutManager leftLayout = new FlowLayout(FlowLayout.LEFT);

	private static final Logger logger = Logger.getLogger(SearchPDMLinkProductDialog.class);
	private static final long serialVersionUID = 1336292047030719519L;
	private int searchRow = 0;
	private ConfirmPanel confirmPanel;
	private SearchConditionsPanel searchConditionPanel;
	private SearchResultPanel searchResultPanel;

	public SearchPDMLinkProductDialog(Callback container) {
		super(SearchPDMLinkProductDialog.class, container);
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
		searchConditionPanel = new SearchConditionsPanel();
		add(searchConditionPanel);

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
		return searchResultPanel.table.getSelectedProduct();
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

	class SearchConditionsPanel extends AbstractPanel implements ActionListener {

		private static final String NAME = "name";
		private static final String SEARCH = "search";
		private static final long serialVersionUID = 7488199863056895133L;
		private final double HEIGHT_PROPORTION = 0.3d;
		private final double LABEL_PROPORTION = 0.05d;
		public PromptTextField name;
		public PromptTextField number;
		private final double TEXT_PROPORTION = 0.2d;
		private final String QUERY = "query";
		public JLabel msg;

		@Override
		public void actionPerformed(ActionEvent e) {
			String inputPDMName = StringUtils.trimWhitespace(name.getTextContent());
			if (StringUtils.isEmpty(inputPDMName)) {
				return;
			}
			DefaultTableModel model = (DefaultTableModel) searchResultPanel.table.getModel();
			int rowCount = model.getRowCount();
			for (int i = searchRow; i < rowCount; i++) {
				String value = (String) searchResultPanel.table.getValueAt(i, 2);
				if (StringUtils.isEmpty(value)) {
					continue;
				}
				if (StringUtils.containsIgnoreCase(value, inputPDMName)) {
					searchResultPanel.table.setRowSelectionInterval(i, i);
					Rectangle rect = searchResultPanel.table.getCellRect(i, 0, true);
					searchResultPanel.table.scrollRectToVisible(rect);
					searchRow = i + 1;
					if (searchRow >= rowCount) {
						searchRow = 0;
					}
					return;
				}
			}
		}

		@Override
		public double getHorizontalProportion() {
			return 0.95d;
		}

		@Override
		public double getVerticalProportion() {
			return 0.2d;
		}

		@Override
		public void initialize() {
			setLayout(leftLayout);

			PromptTextField.PromptTextFieldDimension dimension = PromptTextField.newDimension(getPreferredSize(),
					LABEL_PROPORTION, TEXT_PROPORTION, HEIGHT_PROPORTION);

			name = PromptTextField.newInstance((getResourceMap().getString(NAME)), null, dimension);
			name.setLabelAligment(SwingConstants.LEFT);
			add(name);

			Option search = new Option(SEARCH, null, this);
			add(new OptionPanel(Arrays.asList(search)));

			msg = new JLabel();
			msg.setText(getResourceMap().getString(QUERY));
			msg.setForeground(Color.red);
			add(msg);
		}
	}

	class SearchResultPanel extends AbstractPanel {

		private static final long serialVersionUID = -7416585921364617464L;
		protected SearchPDMLinkProductTable table;
		private double TABLE_HEIGTH_PROPORTION = 0.85d;
		private double TABLE_WIDTH_PROPORTION = 0.98d;
		private final String TITLE = "title";

		@Override
		public double getHorizontalProportion() {
			return 0.95d;
		}

		@Override
		public double getVerticalProportion() {
			return 0.55d;
		}

		@Override
		public void initialize() {
			// set panel border to be title and etched type
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					getResourceMap().getString(TITLE), TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, toolkit.getFont()));

			table = new SearchPDMLinkProductTable(searchConditionPanel);
			JScrollPane sp = new JScrollPane(table);
			sp.setPreferredSize(new Dimension((int) (getPreferredSize().width * TABLE_WIDTH_PROPORTION),
					(int) (getPreferredSize().height * TABLE_HEIGTH_PROPORTION)));
			table.setColumnWidth();
			add(sp);
		}

		public SimplePdmLinkProduct getPDMInfos() {
			return table.getSelectedProduct();
		}

	}
}