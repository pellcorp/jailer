/*
 * Copyright 2007 - 2016 the original author or authors.
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
package net.sf.jailer.modelbuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.jailer.database.Session;
import net.sf.jailer.datamodel.Association;
import net.sf.jailer.datamodel.Cardinality;
import net.sf.jailer.datamodel.Column;
import net.sf.jailer.datamodel.DataModel;
import net.sf.jailer.datamodel.PrimaryKeyFactory;
import net.sf.jailer.datamodel.Table;
import net.sf.jailer.util.PrintUtil;

import org.apache.log4j.Logger;

/**
 * Finds associations and tables by reading the databases meta-data tables
 * via SQL-script.
 * 
 * @author Ralf Wisser
 */
public class DBMetaDataBasedModelElementFinder implements ModelElementFinder {

    /**
     * The logger.
     */
    private static final Logger _log = Logger.getLogger(DBMetaDataBasedModelElementFinder.class);

    /**
     * The script containing a SQL-Query for the retrieval of table-names.
     */
    private final String selectTablesScript;
    
    /**
     * The script containing a SQL-Query for the retrieval of table-columns.
     */
    private final String selectColumnsScript;
    
    /**
     * The script containing a SQL-Query for the retrieval of foreign-key constraints.
     */
    private final String selectForeignKeysScript;
    
    /**
     * Constructor.
     * 
     * @param session the statement executor for executing SQL-statements
     */
    public DBMetaDataBasedModelElementFinder(String selectTablesScript, String selectForeignKeysScript, String selectColumnsScript) {
        this.selectTablesScript = selectTablesScript;
        this.selectForeignKeysScript = selectForeignKeysScript;
        this.selectColumnsScript = selectColumnsScript;
    }
    
    /**
     * Finds associations by reading the databases meta-data.
     * 
     * @param session the statement executor for executing SQL-statements 
     * @param dataModel model containing already known elements
     * @param namingSuggestion to put naming suggestions for associations into
     * @return found associations
     */
    public Collection<Association> findAssociations(DataModel dataModel, Map<Association, String[]> namingSuggestion, Session session) throws Exception {
        Collection<Association> associations = new ArrayList<Association>();
        associations.addAll(findAssociations(dataModel, selectForeignKeysScript, session));
        return associations;
    }

    /**
     * Finds associations by reading the databases meta-data.
     * 
     * @param dataModel model containing already known elements
     * @param sqlFileName the name of the file containing the sql-select for
     *             retrieving association-information.
     *             The result-set must have the following columns:
     *             <ul>
     *                <li>name of source-table</li>
     *                <li>name of destination-table</li>
     *                <li>first-insert definition</li>
     *                <li>cardinality<li>
     *                <li>join-condition</li>
     *             </ul>
     *             Associations between unknown tables are ignored
     * @param session the statement executor for executing SQL-statements 
     *             
     * @return found associations
     */
    private Collection<Association> findAssociations(final DataModel dataModel, final String sqlFileName, Session session) throws Exception {
        final List<Association> associations = new ArrayList<Association>();
        String select = PrintUtil.applyTemplate(sqlFileName, new Object[] { session.getSchemaName() });
        session.executeQuery(select, new Session.AbstractResultSetReader() {
            public void readCurrentRow(ResultSet resultSet) throws SQLException {
                String tableA = resultSet.getString(1);
                String tableB = resultSet.getString(2);
                String firstInsert = resultSet.getString(3);
                Cardinality cardinality = Cardinality.parse(resultSet.getString(4));
                String joinCondition = resultSet.getString(5);
                
                Table a = dataModel.getTable(tableA);
                Table b = dataModel.getTable(tableB);
                if (a == null || b == null) {
                    _log.info("ignoring " + tableA + " -> " + tableB);
                    return;
                }
                boolean insertSourceBeforeDestination = "A".equalsIgnoreCase(firstInsert); 
                boolean insertDestinationBeforeSource = "B".equalsIgnoreCase(firstInsert); 
                Association association = new Association(a, b, insertSourceBeforeDestination, insertDestinationBeforeSource, joinCondition, dataModel, false, cardinality);
                association.setAuthor("DB:" + sqlFileName);
                associations.add(association);
            }
        });
        return associations;
    }
    
    /**
     * Finds all tables in DB schema.
     * 
     * @param session the statement executor for executing SQL-statements 
     */
    public Set<Table> findTables(Session session) throws Exception {
        final PrimaryKeyFactory primaryKeyFactory = new PrimaryKeyFactory();
        final Set<Table> tables = new TreeSet<Table>();
        String select = PrintUtil.applyTemplate(selectTablesScript, new Object[] { session.getSchemaName() });
        final Map<String, Map<Integer, Column>> pkColumns = new HashMap<String, Map<Integer, Column>>();
        session.executeQuery(select, new Session.AbstractResultSetReader() {
            public void readCurrentRow(ResultSet resultSet) throws SQLException {
                String tableName = resultSet.getString(1);
                Map<Integer, Column> pk = pkColumns.get(tableName);
                if (pk == null) {
                    pk = new HashMap<Integer, Column>();
                    pkColumns.put(tableName, pk);
                }
                
                String name = resultSet.getString(2);
                String type = resultSet.getString(3);
                int size = resultSet.getInt(4);
                int scale = resultSet.getInt(5);
                int keySeq = resultSet.getInt(6);
                pk.put(new Integer(keySeq), new Column(name, type, size, scale > 0 && size > 0? scale : -1));
            }
        });
        
        for (String tableName: pkColumns.keySet()) {
            List<Column> pk = new ArrayList<Column>();
            List<Integer> keySeqs = new ArrayList<Integer>(pkColumns.get(tableName).keySet());
            Collections.sort(keySeqs);
            for (Integer i: keySeqs) {
                pk.add(pkColumns.get(tableName).get(i));
            }
            Table table = new Table(tableName, primaryKeyFactory.createPrimaryKey(pk), false);
            table.setAuthor(selectTablesScript);
            tables.add(table);
        }
        return tables;
    }

    /**
     * Finds all columns in DB schema.
     * 
     * @param session the statement executor for executing SQL-statements 
     */
    public List<Column> findColumns(Table table, Session session) throws Exception {
        String select = PrintUtil.applyTemplate(selectColumnsScript, new Object[] { session.getSchemaName(), table.getName() });
        final List<Column> columns = new ArrayList<Column>();
        session.executeQuery(select, new Session.AbstractResultSetReader() {
            public void readCurrentRow(ResultSet resultSet) throws SQLException {
                String name = resultSet.getString(2);
                String type = resultSet.getString(3);
                int size = resultSet.getInt(4);
                int scale = resultSet.getInt(5);
                columns.add(new Column(name, type, size, scale > 0 && size > 0? scale : -1));
            }
        });
        return columns;
    }

}
