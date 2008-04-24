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

import java.awt.CardLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.sf.jailer.Jailer;
import net.sf.jailer.database.ExportReader;
import net.sf.jailer.datamodel.DataModel;

/**
 * Main frame of Extraction-Model-Editor.
 * 
 * @author Wisser
 */
public class ExtractionModelFrame extends javax.swing.JFrame {

	/**
	 * The embedded editor.
	 */
	private ExtractionModelEditor extractionModelEditor;
	
	/**
	 * Dialog for DB-connects.
	 */
	private final DbConnectionDialog dbConnectionDialog;
	
	/**
	 * File in which plaf-setting is stored.
	 */
	private static final String PLAFSETTING = ".plaf.ui";

    /**
     *  Creates new form ExtractionModelFrame.
     *  
     *  @param extractionModelFile file containing the model, <code>null</code> for new model
     */
    public ExtractionModelFrame(String extractionModelFile) {
        initComponents();
        editorPanel.add(extractionModelEditor = new ExtractionModelEditor(extractionModelFile, this), "editor");
        extractionModelEditor.extractionModelFile = extractionModelFile;
        pack();
        updateTitle(extractionModelEditor.needsSave);
        dbConnectionDialog = new DbConnectionDialog(this);
        try {
	        for (final LookAndFeelInfo lfInfo: UIManager.getInstalledLookAndFeels()) {
	        	JMenuItem mItem = new JMenuItem();
	        	mItem.setText(lfInfo.getName());
	        	view.add(mItem);
	        	mItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setPLAF(lfInfo.getClassName());
					}
	        	});
	        }
        } catch (Throwable t) {
        }
        updateMenuItems();
    }
    
    /**
     * Updates state of some menu items.
     */
    private void updateMenuItems() {
		connectDb.setSelected(dbConnectionDialog.isConnected);
	}

	/** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Erzeugter Quelltext ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        editorPanel = new javax.swing.JPanel();
        jMenuBar2 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newModel = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        load = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        save = new javax.swing.JMenuItem();
        saveAs = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        connectDb = new javax.swing.JCheckBoxMenuItem();
        disconnectDb = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        exit = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        ignoreAll = new javax.swing.JMenuItem();
        removeAllRestrictions = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        collapseAll = new javax.swing.JMenuItem();
        expandAll = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        refresh = new javax.swing.JMenuItem();
        zoomToFit = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        hideIgnored = new javax.swing.JCheckBoxMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        view = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        dataExport = new javax.swing.JMenuItem();
        dataImport = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        updateDataModel = new javax.swing.JMenuItem();
        openDataModelEditor = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        printDatamodel = new javax.swing.JMenuItem();
        renderHtml = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        helpContent = new javax.swing.JMenuItem();
        tutorial = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        jMenuItem1 = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(0);
        setTitle("Extraction Model Editor");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        editorPanel.setLayout(new java.awt.CardLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(editorPanel, gridBagConstraints);

        fileMenu.setText("File");
        fileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuActionPerformed(evt);
            }
        });

        newModel.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newModel.setText("New Model");
        newModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newModelActionPerformed(evt);
            }
        });

        fileMenu.add(newModel);

        fileMenu.add(jSeparator3);

        load.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        load.setText("Load");
        load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadActionPerformed(evt);
            }
        });

        fileMenu.add(load);

        fileMenu.add(jSeparator1);

        save.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        save.setText("Save");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        fileMenu.add(save);

        saveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        saveAs.setText("Save as...");
        saveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsActionPerformed(evt);
            }
        });

        fileMenu.add(saveAs);

        fileMenu.add(jSeparator2);

        connectDb.setText("Connect database");
        connectDb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectDbActionPerformed(evt);
            }
        });

        fileMenu.add(connectDb);

        disconnectDb.setText("Disconnect");
        disconnectDb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectDbActionPerformed(evt);
            }
        });

        fileMenu.add(disconnectDb);

        fileMenu.add(jSeparator10);

        exit.setText("Exit");
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitActionPerformed(evt);
            }
        });

        fileMenu.add(exit);

        jMenuBar2.add(fileMenu);

        jMenu5.setText("Restriction");
        ignoreAll.setActionCommand("Disable all associations");
        ignoreAll.setLabel("Disable all associations");
        ignoreAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreAllActionPerformed(evt);
            }
        });

        jMenu5.add(ignoreAll);

        removeAllRestrictions.setLabel("Remove all restrictions");
        removeAllRestrictions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllRestrictionsActionPerformed(evt);
            }
        });

        jMenu5.add(removeAllRestrictions);

        jMenuBar2.add(jMenu5);

        jMenu1.setText("View");
        collapseAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        collapseAll.setText("Collapse all");
        collapseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collapseAllActionPerformed(evt);
            }
        });

        jMenu1.add(collapseAll);

        expandAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        expandAll.setLabel("Expand all");
        expandAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandAllActionPerformed(evt);
            }
        });

        jMenu1.add(expandAll);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Fix all");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });

        jMenu1.add(jMenuItem3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("Unfix all");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });

        jMenu1.add(jMenuItem4);

        refresh.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        refresh.setText("Reset");
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });

        jMenu1.add(refresh);

        zoomToFit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        zoomToFit.setText("Zoom to fit");
        zoomToFit.setVerifyInputWhenFocusTarget(false);
        zoomToFit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomToFitActionPerformed(evt);
            }
        });

        jMenu1.add(zoomToFit);

        jMenu1.add(jSeparator9);

        hideIgnored.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        hideIgnored.setSelected(true);
        hideIgnored.setText("Hide disabled associations");
        hideIgnored.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideIgnoredActionPerformed(evt);
            }
        });

        jMenu1.add(hideIgnored);

        jMenu1.add(jSeparator11);

        view.setLabel("Look&Feel");
        jMenu1.add(view);

        jMenuBar2.add(jMenu1);

        jMenu3.setText("Tools");
        dataExport.setLabel("Export Data");
        dataExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataExportActionPerformed(evt);
            }
        });

        jMenu3.add(dataExport);

        dataImport.setText("Import Data");
        dataImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataImportActionPerformed(evt);
            }
        });

        jMenu3.add(dataImport);

        jMenu3.add(jSeparator6);

        updateDataModel.setText("Introspect DB");
        updateDataModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateDataModelActionPerformed(evt);
            }
        });

        jMenu3.add(updateDataModel);

        openDataModelEditor.setLabel("Data Model Editor");
        openDataModelEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDataModelEditorActionPerformed(evt);
            }
        });

        jMenu3.add(openDataModelEditor);

        jMenu3.add(jSeparator5);

        printDatamodel.setLabel("Print Data Model");
        printDatamodel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printDatamodelActionPerformed(evt);
            }
        });

        jMenu3.add(printDatamodel);

        renderHtml.setText("HTML Rendering");
        renderHtml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renderHtmlActionPerformed(evt);
            }
        });

        jMenu3.add(renderHtml);

        jMenuBar2.add(jMenu3);

        jMenu2.setText("Help");
        helpContent.setText("Content");
        helpContent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpContentActionPerformed(evt);
            }
        });

        jMenu2.add(helpContent);

        tutorial.setLabel("Tutorial");
        tutorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tutorialActionPerformed(evt);
            }
        });

        jMenu2.add(tutorial);

        jMenu2.add(jSeparator7);

        jMenuItem2.setText("Software Update");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });

        jMenu2.add(jMenuItem2);

        jMenu2.add(jSeparator8);

        jMenuItem1.setText("About Jailer");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });

        jMenu2.add(jMenuItem1);

        jMenuBar2.add(jMenu2);

        setJMenuBar(jMenuBar2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void zoomToFitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomToFitActionPerformed
    	extractionModelEditor.zoomToFit();
    }//GEN-LAST:event_zoomToFitActionPerformed

    private void tutorialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tutorialActionPerformed
    	try {
			BrowserLauncher.openURL(new File("doc" + File.separator + "htdocs" + File.separator + "JailerGuiTutorial.html").getCanonicalPath());
		} catch (IOException e) {
			UIUtil.showException(this, "Error", e);
		}
    }//GEN-LAST:event_tutorialActionPerformed

    private void helpContentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpContentActionPerformed
    	try {
			BrowserLauncher.openURL(new File("doc" + File.separator + "htdocs" + File.separator + "index.html").getCanonicalPath());
		} catch (IOException e) {
			UIUtil.showException(this, "Error", e);
		}
    }//GEN-LAST:event_helpContentActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
    	UIUtil.lookForUpdate(this);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void removeAllRestrictionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllRestrictionsActionPerformed
    	if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Remove all restrictions?", "Remove restrictions", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
    		extractionModelEditor.removeAllRestrictions();
    	}
    }//GEN-LAST:event_removeAllRestrictionsActionPerformed

    private void ignoreAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreAllActionPerformed
    	if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Disable each association (except dependencies)?", "Add restrictions", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
    		extractionModelEditor.ignoreAll();
    	}
    }//GEN-LAST:event_ignoreAllActionPerformed

    private void dataImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataImportActionPerformed
    	try {
    		String sqlFile = UIUtil.choseFile(null, ".", "Data Import", ".sql", this, false, true);
    		if (sqlFile != null) {
    			disconnect();
    			if (connectToDBIfNeeded("Data Import")) {
    				List<String> args = new ArrayList<String>();
    				args.add("import");
    				args.add(sqlFile);
    				dbConnectionDialog.addDbArgs(args);
    				disconnect();
    				UIUtil.runJailer(this, args, false, true, false, false, null, dbConnectionDialog.getPassword());
    			}
    		}
    	} catch (Exception e) {
        	UIUtil.showException(this, "Error", e);
        }
    }//GEN-LAST:event_dataImportActionPerformed

    private void printDatamodelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printDatamodelActionPerformed
    	try {
        	List<String> args = new ArrayList<String>();
        	args.add("print-datamodel");
        	File file = saveRestrictions();
        	args.add(file.getName());
        	UIUtil.runJailer(this, args, false, true, false, false, null, dbConnectionDialog.getPassword());
        } catch (Exception e) {
        	UIUtil.showException(this, "Error", e);
        }
	}//GEN-LAST:event_printDatamodelActionPerformed

	/**
	 * Looks up "show disabled associations" setting.
	 * 
	 * @return true if "show disabled associations" is set
	 */
	public boolean showDisabledAssociations() {
		return !hideIgnored();
	}

    /**
     * Sets Look&Feel.
     * 
     * @param plaf the l&f
     */
    private void setPLAF(String plaf) {
	    try {
	    	UIManager.setLookAndFeel(plaf);
	    	SwingUtilities.updateComponentTreeUI(this);
	    	try {
                File file = new File(PLAFSETTING);
                file.delete();
            } catch (Exception e) {
            }
            try {
	    		File plafSetting = new File(PLAFSETTING);
	    		PrintWriter out = new PrintWriter(plafSetting);
	    		out.println(plaf);
	    		out.close();
	    	} catch (Exception x) {
	    	}
	    }
    	catch (Exception e) {
	    	UIUtil.showException(this, "Error", e);
	    }
    }
    
    private void openDataModelEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDataModelEditorActionPerformed
    	try {
    		if (saveIfNeeded("edit data model", true)) {
       			DataModelEditor dataModelEditor = new DataModelEditor(this, false);
				dataModelEditor.setVisible(true);
       			if (dataModelEditor.saved) {
       				reload();
       			}
    		}
        } catch (Exception e) {
        	UIUtil.showException(this, "Error", e);
        }
    }//GEN-LAST:event_openDataModelEditorActionPerformed
 
    private void updateDataModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateDataModelActionPerformed
    	try {
    		if (saveIfNeeded("introspect DB", true)) {
	        	if (connectToDBIfNeeded("Introspect DB")) {
		        	List<String> args = new ArrayList<String>();
		        	args.add("build-model");
		        	dbConnectionDialog.addDbArgs(args);
	        		String schema = dbConnectionDialog.selectDBSchema(this);
	        		if (!"".equals(schema)) {
		        		if (schema != null) {
		        			args.add("-schema");
		        			args.add(schema);
		        		}
			        	if (UIUtil.runJailer(this, args, false, true, false, true, null, dbConnectionDialog.getPassword())) {
		        			DataModelEditor dataModelEditor = new DataModelEditor(this, true);
							dataModelEditor.setVisible(true);
		           			if (dataModelEditor.saved) {
		           				reload();
		           			}
		        		}
	        		}
	        	}
    		}
        } catch (Exception e) {
        	UIUtil.showException(this, "Error", e);
        }
    }//GEN-LAST:event_updateDataModelActionPerformed

	void dataExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataExportActionPerformed
    	try {
    		if (saveIfNeeded("Export data", false)) {
    			if (extractionModelEditor.extractionModelFile != null || extractionModelEditor.save(true, "Export data")) {
		        	if (connectToDBIfNeeded("Export data")) {
			        	List<String> args = new ArrayList<String>();
			        	args.add("export");
			        	args.add(extractionModelEditor.extractionModelFile);
			        	dbConnectionDialog.addDbArgs(args);
			        	ExportDialog exportDialog = new ExportDialog(this);
			        	if (exportDialog.isOk()) {
			        		exportDialog.fillCLIArgs(args);
			        		File excludeFromDeletion = new File(DataModel.EXCLUDE_FROM_DELETION_FILE);
			        		if (excludeFromDeletion.exists()) {
			        			args.add("-t");
			        			args.add(DataModel.EXCLUDE_FROM_DELETION_FILE);
			        		}
				        	List<String> ddlArgs = new ArrayList<String>();
				        	ddlArgs.add("create-ddl");
				        	dbConnectionDialog.addDbArgs(ddlArgs);
				        	ExportReader.numberOfExportedEntities = 0;
				        	ExportReader.numberOfExportedLOBs = 0;
				            if (UIUtil.runJailer(this, ddlArgs, true, true, false, true, 
			        				"Automatic creation of working-tables failed!\n" +
			        				"Please execute the Jailer-DDL manually (jailer_ddl.sql)\n\n" +
			        				"Continue Data Export?", dbConnectionDialog.getPassword())) {
			        			if (UIUtil.runJailer(this, args, true, true, exportDialog.explain.isSelected(), !exportDialog.explain.isSelected(), null, dbConnectionDialog.getPassword())) {
			        				String message = "Exported " + ExportReader.numberOfExportedEntities + " entities.";
			        				if (ExportReader.numberOfExportedLOBs > 0) {
			        					message += "\nExported " + ExportReader.numberOfExportedLOBs + " CLOBs/BLOBs.\n\n" +
			        					           "Note that the CLOBs/BLOBs can only\n" +
			        							   "be imported with the 'Import Data'-tool!";
			        				}
		        					JOptionPane.showMessageDialog(this, message);
			        			}
			        		}
			        	}
		        	}
	        	}
    		}
        } catch (Exception e) {
        	UIUtil.showException(this, "Error", e);
        }
    }//GEN-LAST:event_dataExportActionPerformed

    private void disconnectDbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectDbActionPerformed
    	disconnect();
    }//GEN-LAST:event_disconnectDbActionPerformed

	private void disconnect() {
		dbConnectionDialog.isConnected = false;
    	updateMenuItems();
	}

    private void connectDbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectDbActionPerformed
    	disconnect();
    	connectToDBIfNeeded(null);
    }//GEN-LAST:event_connectDbActionPerformed

    private void renderHtmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renderHtmlActionPerformed
        try {
        	if (connectToDBIfNeeded("Html rendering")) {
	        	List<String> args = new ArrayList<String>();
	        	args.add("render-datamodel");
	        	String schema = dbConnectionDialog.selectDBSchema(this);
        		if (!"".equals(schema)) {
	        		if (schema != null) {
	        			args.add("-schema");
	        			args.add(schema);
	        		}
	        		dbConnectionDialog.addDbArgs(args);
		        	File file = saveRestrictions();
		        	args.add(file.getName());
		        	UIUtil.runJailer(this, args, false, true, false, true, null, dbConnectionDialog.getPassword());
		        	BrowserLauncher.openURL("render/index.html");
        		}
        	}
        } catch (Exception e) {
        	UIUtil.showException(this, "Error", e);
        }
    }//GEN-LAST:event_renderHtmlActionPerformed

    /**
     * Saves restrictions of current extraction model.
     * 
     * @return restrictions file
     */
	private File saveRestrictions() throws Exception {
		File file;
		String extractionModelFile = extractionModelEditor.extractionModelFile;
		if (extractionModelFile == null) {
			file = new File("tmp_restrictions.csv");
		} else {
			extractionModelFile = new File(extractionModelFile).getName();
			if (extractionModelFile.toLowerCase().endsWith(".csv")) {
				file = new File(extractionModelFile.substring(0, extractionModelFile.length() - 4) + "-restrictions.csv");
			} else {
				file = new File(extractionModelFile + "-restrictions.csv");
			}
		}
		extractionModelEditor.saveRestrictions(file);
		return file;
	}

    /**
     * Opens connection dialog to establish DB-connection.
     * 
     * @return <code>false</code> if connection fails
     */
    private boolean connectToDBIfNeeded(String reason) {
    	try {
    		if (!dbConnectionDialog.isConnected) {
	    		return dbConnectionDialog.connect(reason);
    		}
    		return true;
    	} finally {
        	updateMenuItems();
    	}
	}

	private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
		About about = new About(this, true);
		about.setTitle("Jailer " + Jailer.VERSION);
		about.pack();
		about.setLocation(getLocation().x + (getSize().width - about.getSize().width) / 2, getLocation().y + (getSize().height - about.getSize().height) / 2);
		about.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadActionPerformed
    	if (saveIfNeeded("loading", true)) {
    		String modelFile = UIUtil.choseFile(null, "extractionmodel", "Load Extraction Model", ".csv", this, true, true);
    		if (modelFile != null) {
	    		load(modelFile);
    		}
    	}
    }//GEN-LAST:event_loadActionPerformed

    /**
     * Loads an extraction model.
     * 
     * @param modelFile name of model file
     */
	private void load(String modelFile) {
		extractionModelEditor.extractionModelFrame = null;
		editorPanel.remove(extractionModelEditor);
		editorPanel.add(extractionModelEditor = new ExtractionModelEditor(modelFile, this), "editor");
		((CardLayout) editorPanel.getLayout()).show(editorPanel, "editor");
		validate();
		updateTitle(extractionModelEditor.needsSave);
	}

    private void newModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newModelActionPerformed
    	if (saveIfNeeded("creating new model", true)) {
    		extractionModelEditor.extractionModelFrame = null;
    		editorPanel.remove(extractionModelEditor);
    		editorPanel.add(extractionModelEditor = new ExtractionModelEditor(null, this), "editor");
    		((CardLayout) editorPanel.getLayout()).show(editorPanel, "editor");
    		validate();
    		updateTitle(extractionModelEditor.needsSave);
    	}
    }//GEN-LAST:event_newModelActionPerformed

    private void reload() {
		extractionModelEditor.extractionModelFrame = null;
		editorPanel.remove(extractionModelEditor);
		editorPanel.add(extractionModelEditor = new ExtractionModelEditor(extractionModelEditor.extractionModelFile, this), "editor");
		((CardLayout) editorPanel.getLayout()).show(editorPanel, "editor");
		validate();
		updateTitle(extractionModelEditor.needsSave);
	}

	private void expandAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandAllActionPerformed
    	extractionModelEditor.expand();
    }//GEN-LAST:event_expandAllActionPerformed

    private void hideIgnoredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideIgnoredActionPerformed
        extractionModelEditor.refresh(true, false);
    }//GEN-LAST:event_hideIgnoredActionPerformed

    private void collapseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapseAllActionPerformed
        extractionModelEditor.refresh(false, true);
        extractionModelEditor.resetGraphEditor(true);
    }//GEN-LAST:event_collapseAllActionPerformed

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
    	extractionModelEditor.refresh(true, true);
    }//GEN-LAST:event_refreshActionPerformed

    private void saveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsActionPerformed
        extractionModelEditor.save(true, null);
    }//GEN-LAST:event_saveAsActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        extractionModelEditor.save(false, null);
    }//GEN-LAST:event_saveActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        onExit();
    }//GEN-LAST:event_formWindowClosing

    private void exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitActionPerformed
    	onExit();
    }//GEN-LAST:event_exitActionPerformed

    /**
     * Saves model if needed.
     * 
     * @return <code>false</code> if user cancels saving
     */
    private boolean saveIfNeeded(String cause, boolean ask) {
    	if (!extractionModelEditor.needsSave) {
    		return true;
    	}
    	if (ask) {
	    	int option = JOptionPane.showConfirmDialog(this, "Save changes before " + cause + "?", "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	    	if (option == JOptionPane.CANCEL_OPTION) {
	    		return false;
	    	}
	    	if (option == JOptionPane.NO_OPTION) {
	    		return true;
	    	}
    	}
    	return extractionModelEditor.save(false, cause);
	}

    /**
     * Exits GUI.
     */
    private void onExit() {
    	if (extractionModelEditor.needsSave) {
	    	if (0 == JOptionPane.showConfirmDialog(
	    			this,  
	    			"Exit without saving?",
	                "",
	                JOptionPane.YES_NO_OPTION,
	                JOptionPane.QUESTION_MESSAGE)) {
	    		System.exit(0);
	    	}
	    } else {
	    	System.exit(0);
	    }
    }

    private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuActionPerformed
    }//GEN-LAST:event_fileMenuActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        extractionModelEditor.graphView.setFix(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        extractionModelEditor.graphView.setFix(false);
    }//GEN-LAST:event_jMenuItem4ActionPerformed
    
    /**
     * Updates title.
     */
	public void updateTitle(boolean needsSave) {
		if (extractionModelEditor == null) {
			return;
		}
		String title = "Jailer " + Jailer.VERSION + " Extraction Model Editor";
        if (extractionModelEditor.extractionModelFile == null) {
        	title = "New Model - " + title;
        } else {
        	title = new File(extractionModelEditor.extractionModelFile).getName() + " - " + title;
        }
        if (needsSave) {
        	title = "*" + title;
        }
        setTitle(title);
	}

	boolean hideIgnored() {
		return hideIgnored.isSelected();
	}
	
	/**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
    	// turn of logging for prefuse library
    	try {
			Logger.getLogger("prefuse").setLevel(Level.OFF);
			
			// trigger log4j initialization
			Jailer.class.getName();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}
    	try {
    		// create initial data-model files
    		File file = new File("datamodel");
    		if (!file.exists()) {
    			file.mkdir();
    		}
    		file = new File(DataModel.TABLES_FILE);
    		if (!file.exists()) {
    			file.createNewFile();
    		}
    		file = new File(DataModel.ASSOCIATIONS_FILE);
    		if (!file.exists()) {
    			file.createNewFile();
    		}
    	} catch (Exception e) {
    	}
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ExtractionModelFrame extractionModelFrame = new ExtractionModelFrame(args.length > 0? args[0] : null);
    	    	try {
    	    		File plafSetting = new File(PLAFSETTING);
    	    		BufferedReader in = new BufferedReader(new FileReader(plafSetting));
    	    		String plaf = in.readLine();
    	    		in.close();
    	    		UIManager.setLookAndFeel(plaf);
    		    	SwingUtilities.updateComponentTreeUI(extractionModelFrame);
    	    	} catch (Exception x) {
    	    	}
    	    	try {
    	    		extractionModelFrame.setIconImage(new ImageIcon(extractionModelFrame.getClass().getResource("/jailer.gif")).getImage());
    	    	} catch (Throwable t) {
    	    	}
                extractionModelFrame.setLocation(40, 40);
                extractionModelFrame.setSize(960, 660);
                extractionModelFrame.setVisible(true);
                if (extractionModelFrame.extractionModelEditor.dataModel.getTables().isEmpty()) {
                	switch (JOptionPane.showOptionDialog(extractionModelFrame, "No Data Model found.", "Jailer " + Jailer.VERSION, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[] { "Introspect DB", "Data Model Editor", "Demo" }, null)) {
                		case 0: extractionModelFrame.updateDataModelActionPerformed(null); break;
	                   	case 1: extractionModelFrame.openDataModelEditorActionPerformed(null); break;
	                   	case 2: demo(extractionModelFrame); break;
                	}
                }
            }
        });
    }

    /**
     * Creates demo datamodel and loads demo extraction model.
     * 
     * @param extractionModelFrame the editor frame
     */
	private static void demo(ExtractionModelFrame extractionModelFrame) {
		File tables = new File(DataModel.TABLES_FILE);
		tables.delete();
		File associations = new File(DataModel.ASSOCIATIONS_FILE);
		associations.delete();
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(tables));
			out.println("# Name; Upsert; Primary key; ; Author");
			out.println("BONUS; N; ENAME VARCHAR(10); JOB VARCHAR(9); ; Demo; ; ");
			out.println("DEPARTMENT; N; DEPTNO INTEGER; ; Demo; ; ");
			out.println("EMPLOYEE; N; EMPNO INTEGER; ; Demo; ; ");
			out.println("SALARYGRADE; N; GRADE INTEGER; ; Demo; ; ");
			out.close();
			out = new PrintWriter(new FileOutputStream(associations));
			out.println("# Table A; Table B; First-insert; Cardinality; Join-condition; Name; Author");
			out.println("EMPLOYEE; BONUS; ; 1:1; A.NAME=B.ENAME and A.JOB=B.JOB; BONUS; Demo; ; ");
			out.println("SALARYGRADE; EMPLOYEE; ; 1:n; B.SALARY BETWEEN A.LOSAL and A.HISAL; SALARY; Demo; ; ");
			out.println("EMPLOYEE; DEPARTMENT; B; n:1; A.DEPTNO=B.DEPTNO; DEPARTMENT; Demo; ; ");
			out.println("EMPLOYEE; EMPLOYEE; B; n:1; A.BOSS=B.EMPNO; BOSS; Demo; ; ");
			out.close();
			extractionModelFrame.load("extractionmodel/demo.csv");
		} catch (Exception e) {
			UIUtil.showException(extractionModelFrame, "Error", e);
		}
	}
    
    // Variablendeklaration - nicht modifizieren//GEN-BEGIN:variables
    private javax.swing.JMenuItem collapseAll;
    private javax.swing.JCheckBoxMenuItem connectDb;
    private javax.swing.JMenuItem dataExport;
    private javax.swing.JMenuItem dataImport;
    private javax.swing.JMenuItem disconnectDb;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JMenuItem exit;
    private javax.swing.JMenuItem expandAll;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem helpContent;
    private javax.swing.JCheckBoxMenuItem hideIgnored;
    private javax.swing.JMenuItem ignoreAll;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JMenuItem load;
    private javax.swing.JMenuItem newModel;
    private javax.swing.JMenuItem openDataModelEditor;
    private javax.swing.JMenuItem printDatamodel;
    private javax.swing.JMenuItem refresh;
    private javax.swing.JMenuItem removeAllRestrictions;
    private javax.swing.JMenuItem renderHtml;
    private javax.swing.JMenuItem save;
    private javax.swing.JMenuItem saveAs;
    private javax.swing.JMenuItem tutorial;
    private javax.swing.JMenuItem updateDataModel;
    private javax.swing.JMenu view;
    private javax.swing.JMenuItem zoomToFit;
    // Ende der Variablendeklaration//GEN-END:variables
    
}
