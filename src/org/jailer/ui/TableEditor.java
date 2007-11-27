/*
 * TableEditor.java
 *
 * Created on 27. November 2007, 13:13
 */

package org.jailer.ui;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.jailer.util.CsvFile.Line;

/**
 * Editor for single tables.
 *
 * @author Wisser
 */
public class TableEditor extends javax.swing.JDialog {
    
	/**
	 * All tables (as csv-lines).
	 */
	private Collection<Line> tables;

	/**
	 * All associations (as csv-lines).
	 */
	private Collection<Line> associations;
	
    /** 
     * Creates new form TableEditor
     * 
     * @param tables all tables (as csv-lines)
     * @param associations all associations (as csv-line)
     */
    public TableEditor(java.awt.Dialog parent, Collection<Line> tables, List<Line> associations) {
        super(parent, true);
        this.tables = tables;
        this.associations = associations;
        initComponents();
        setLocation(parent.getLocation().x + parent.getSize().width/2 - getSize().width/2,
    			parent.getLocation().y + parent.getSize().height/2 - getSize().height/2);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Erzeugter Quelltext ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        pkField = new javax.swing.JTextField();
        upsertCheckbox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Table");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jLabel1.setText("Name ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setText("Primary key* ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(jLabel2, gridBagConstraints);

        nameField.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(nameField, gridBagConstraints);

        pkField.setText("jTextField2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(pkField, gridBagConstraints);

        upsertCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        upsertCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        upsertCheckbox.setText(" generate 'Upsert'-statements for exported rows");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(upsertCheckbox, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jButton1.setText("Ok");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(jButton1, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel3.setText("*comma-separated typed columns");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(jButton2, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel4.setText("example: A VARCHAR(10), B INTEGER");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 40;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        jLabel5.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        getContentPane().add(jLabel5, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    	String msg = null;
    	if (nameField.getText().trim().length() == 0) {
    		msg = "No table name";
    	} else {
    		for (Line l: tables) {
    			if (l != currentLine && l.cells.get(0).equalsIgnoreCase(nameField.getText().trim())) {
    				msg = "Table already exists";
    				break;
    			}
    		}
    	}
    	if (pkField.getText().trim().length() == 0) {
    		msg = "No primary key";
    	} else {
    		Pattern pkPattern = Pattern.compile("([A-Z_0-9]+ +[A-Z]+(\\([0-9]+\\))?)");
    		for (String col: pkField.getText().trim().toUpperCase().split(",")) {
	    		if (!pkPattern.matcher(col.trim()).matches()) {
	    			msg = "Syntax error in primary key";
	    		}
    		}
    	}
    	if (msg != null) {
    		JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    	} else {
    		isOk = true;
    		setVisible(false);
    	}
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private boolean isOk;
    private Line currentLine;
    
    /**
     * Edits a table (as csv-line).
     * 
     * @param line the table-line
     * @return <code>true</code> if line was modified
     */
	public boolean edit(Line line) {
		currentLine = line;
		String pk = "";
		for (int i = 2; i < line.length; ++i) {
			if (line.cells.get(i).length() == 0) {
				break;
			}
			if (pk.length() != 0) {
				pk += ", ";
			}
			pk += line.cells.get(i);
		}
		nameField.setText(line.cells.get(0));
		pkField.setText(pk);
		upsertCheckbox.setSelected("Y".equals(line.cells.get(1)));
		
		String origName = nameField.getText();
		String origPk = pkField.getText();
		boolean origUpsert = upsertCheckbox.isSelected();
		isOk = false;
		setVisible(true);
		if (isOk && !(origName.equals(nameField.getText()) && origPk.equals(pkField.getText()) && origUpsert == upsertCheckbox.isSelected())) {
			int l = line.length;
			line.cells.set(0, nameField.getText().trim().toUpperCase());
			line.cells.set(1, upsertCheckbox.isSelected()? "Y" : "N");
			int c = 2;
			for (String col: pkField.getText().split(",")) {
				line.cells.set(c++, col);
			}
			line.cells.set(c++, "");
			line.cells.set(c++, "Data Model Editor");
			line.length = c;
			
			//rename associations source/destination
			for (Line a: associations) {
				if (a.cells.get(0).equalsIgnoreCase(origName.trim())) {
					a.cells.set(0, nameField.getText().trim().toUpperCase());
				}
				if (a.cells.get(1).equalsIgnoreCase(origName.trim())) {
					a.cells.set(1, nameField.getText().trim().toUpperCase());
				}
			}
			return true;
		}
		return false;
	}
    
    // Variablendeklaration - nicht modifizieren//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField pkField;
    private javax.swing.JCheckBox upsertCheckbox;
    // Ende der Variablendeklaration//GEN-END:variables
    
}
