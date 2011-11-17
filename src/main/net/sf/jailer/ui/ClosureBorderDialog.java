/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.jailer.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.jailer.datamodel.Association;
import net.sf.jailer.datamodel.DataModel;
import net.sf.jailer.datamodel.Table;
import net.sf.jailer.ui.AssociationListUI.AssociationModel;
import net.sf.jailer.ui.AssociationListUI.DefaultAssociationModel;

/**
 * Closure Border Dialog.
 * 
 * @author Ralf Wisser
 */
public abstract class ClosureBorderDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = -7151994890007647782L;
    
    private AssociationListUI associationListUI;
    
	/** Creates new form ClosureBorderDialog */
    public ClosureBorderDialog(java.awt.Frame parent) {
        super(parent, false);
        initComponents();
        
        associationListUI = new AssociationListUI("Remove Restrictions", "Remove Restrictions from selected Associations", false) {
			private static final long serialVersionUID = 1129925600909956307L;
			@Override
			protected void applyAction(Collection<AssociationModel> selection) {
				Collection<Association> associations = new ArrayList<Association>();
				for (AssociationModel associationModel: selection) {
					associations.add(((DefaultAssociationModel) associationModel).association);
				}
				removeRestrictions(associations);
			}
        };
        
        rootNameLabel.setFont(
        		new Font(rootNameLabel.getFont().getName(),
        				rootNameLabel.getFont().getStyle() | Font.BOLD,
        				rootNameLabel.getFont().getSize()));
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(associationListUI, gridBagConstraints);

        setLocation(30, 80);
        setSize(500, 500);
        setAlwaysOnTop(true);
        refresh();
    }

    protected abstract void removeRestrictions(Collection<Association> associations);

	/** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rootNameLabel = new javax.swing.JLabel();

        setTitle("Closure Border");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Closure-Border of ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel1, gridBagConstraints);

        rootNameLabel.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(rootNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 0, 2);
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel rootNameLabel;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Refreshes view after model changes.
     */
	public void refresh() {
		Table root = getRoot();
		DataModel datamodel = getDataModel();
 		if (root != null && datamodel != null) {
			rootNameLabel.setText(datamodel.getDisplayName(root));
			Set<Association> border = new HashSet<Association>();
			Set<Table> closure = root.closure(new HashSet<Table>(), true);
			for (Table table: closure) {
				for (Association association: table.associations) {
					if (association.isIgnored() && !closure.contains(association.destination)) {
						border.add(association);
					}
				}
			}
			Collection<AssociationModel> model = new ArrayList<AssociationListUI.AssociationModel>();
			for (Association association: border) {
				model.add(new DefaultAssociationModel(association));
			}
			associationListUI.setModel(model);
		}
	}

	protected abstract Table getRoot();
	protected abstract DataModel getDataModel();

}
