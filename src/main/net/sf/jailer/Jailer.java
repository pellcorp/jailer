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
package net.sf.jailer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.sf.jailer.database.DMLTransformer;
import net.sf.jailer.database.DeletionTransformer;
import net.sf.jailer.database.Session;
import net.sf.jailer.database.Session.ResultSetReader;
import net.sf.jailer.database.StatisticRenovator;
import net.sf.jailer.database.TemporaryTableScope;
import net.sf.jailer.datamodel.AggregationSchema;
import net.sf.jailer.datamodel.Association;
import net.sf.jailer.datamodel.Cardinality;
import net.sf.jailer.datamodel.Column;
import net.sf.jailer.datamodel.DataModel;
import net.sf.jailer.datamodel.ParameterHandler;
import net.sf.jailer.datamodel.Table;
import net.sf.jailer.dbunit.FlatXMLTransformer;
import net.sf.jailer.domainmodel.DomainModel;
import net.sf.jailer.enhancer.ScriptEnhancer;
import net.sf.jailer.entitygraph.EntityGraph;
import net.sf.jailer.entitygraph.local.LocalEntityGraph;
import net.sf.jailer.entitygraph.remote.RemoteEntityGraph;
import net.sf.jailer.extractionmodel.ExtractionModel;
import net.sf.jailer.extractionmodel.ExtractionModel.AdditionalSubject;
import net.sf.jailer.liquibase.LiquibaseXMLTransformer;
import net.sf.jailer.modelbuilder.ModelBuilder;
import net.sf.jailer.progress.ProgressListener;
import net.sf.jailer.progress.ProgressListenerRegistry;
import net.sf.jailer.render.DataModelRenderer;
import net.sf.jailer.restrictionmodel.RestrictionModel;
import net.sf.jailer.util.CancellationException;
import net.sf.jailer.util.CancellationHandler;
import net.sf.jailer.util.ClasspathUtil;
import net.sf.jailer.util.CycleFinder;
import net.sf.jailer.util.JobManager;
import net.sf.jailer.util.PrintUtil;
import net.sf.jailer.util.SqlScriptExecutor;
import net.sf.jailer.util.SqlUtil;
import net.sf.jailer.xml.XmlExportTransformer;
import net.sf.jailer.xml.XmlUtil;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tool for database subsetting, schema browsing, and rendering. It exports
 * consistent, referentially intact row-sets from relational databases. It
 * removes obsolete data without violating integrity. It is DBMS agnostic (by
 * using JDBC), platform independent, and generates DbUnit datasets,
 * hierarchically structured XML, and topologically sorted SQL-DML.
 * 
 * <a href="http://jailer.sourceforge.net/">http://jailer.sourceforge.net</a>
 * 
 * @author Ralf Wisser
 */
public class Jailer {

	/**
	 * The Jailer version.
	 */
	public static final String VERSION = "5.4.1";
	
	/**
	 * The Jailer application name.
	 */
	public static final String APPLICATION_NAME = "Jailer";
	
	/**
	 * The relational data model.
	 */
	private DataModel datamodel;

	/**
	 * The entity-graph to be used for finding the transitive closure.
	 */
	private EntityGraph entityGraph;

	/**
	 * The job-manager to be used for concurrent execution of jobs.
	 */
	private final JobManager jobManager;

	/**
	 * The logger.
	 */
	private static final Logger _log;

	/**
	 * Comment header of the export-script.
	 */
	private StringBuffer commentHeader = new StringBuffer();
	
	/**
	 * Constructor.
	 * 
	 * @param threads
	 *            number of threads
	 */
	public Jailer(int threads) throws Exception {
		jobManager = new JobManager(threads);
	}

	/**
	 * Sets the entity-graph to be used for finding the transitive closure.
	 * 
	 * @param entityGraph
	 *            the entity-graph to be used for finding the transitive closure
	 */
	public void setEntityGraph(EntityGraph entityGraph) {
		this.entityGraph = entityGraph;
	}

	/**
	 * Sets the restricted data-model to be used for extraction.
	 * 
	 * @param dataModel
	 *            the restricted data-model to be used for extraction
	 */
	public void setDataModel(DataModel dataModel) {
		this.datamodel = dataModel;
	}

	/**
	 * Appends a line to the comment-header of the export script.
	 * 
	 * @param comment
	 *            the comment line (without '--'-prefix)
	 */
	private void appendCommentHeader(String comment) {
		commentHeader.append("-- " + (comment.replace('\n', ' ').replace('\r', ' ')) + "\n");
	}

	/**
	 * Exports rows from table.
	 * 
	 * @param table
	 *            the table
	 * @param condition
	 *            the condition (in SQL) the exported rows must fulfill

	 * @param progressOfYesterday
	 *            set of tables to account for resolvation
	 * 
	 * @return set of tables from which entities are added
	 */
	public Set<Table> export(Table table, String condition, Collection<Table> progressOfYesterday, boolean skipRoot) throws Exception {
		_log.info("exporting " + datamodel.getDisplayName(table) + " Where " + condition.replace('\n', ' ').replace('\r', ' '));
		int today = entityGraph.getAge();
		entityGraph.setAge(today + 1);
		Map<Table, Collection<Association>> progress = new HashMap<Table, Collection<Association>>();
		if (!skipRoot) {
			ProgressListenerRegistry.getProgressListener().collectionJobEnqueued(today, table);
			ProgressListenerRegistry.getProgressListener().collectionJobStarted(today, table);
			long rc = entityGraph.addEntities(table, condition, today);
			ProgressListenerRegistry.getProgressListener().collected(today, table, rc);
			if (rc > 0) {
				progress.put(table, new ArrayList<Association>());
			}
		}
		if (progressOfYesterday != null) {
			for (Table t: progressOfYesterday) {
				progress.put(t, new ArrayList<Association>());
			}
		}
		Set<Table> totalProgress = new HashSet<Table>();

		while (!progress.isEmpty()) {
			totalProgress.addAll(progress.keySet());
			_log.info("day " + today + ", progress: " + asString(progress.keySet()));
			++today;
			entityGraph.setAge(today + 1);
			progress = resolveAssociations(today, progress);
		}

		_log.info("exported " + datamodel.getDisplayName(table) + " Where " + condition.replace('\n', ' ').replace('\r', ' '));
		_log.info("total progress: " + asString(totalProgress));
		_log.info("export statistic:");
		boolean firstLine = true;
		for (String line : entityGraph.getStatistics(datamodel, new HashSet<Table>())) {
			String l = (firstLine ? "Exported Rows:     " : "    ") + line;
			_log.info(l);
			appendCommentHeader(l);
			if (firstLine) {
				appendCommentHeader("");
			}
			firstLine = false;
		}
//		if (CommandLineParser.getInstance().getScriptFormat() != ScriptFormat.XML) {
			appendCommentHeader("");
			boolean isFiltered = false;
			for (Table t : new TreeSet<Table>(totalProgress)) {
				for (Column c : t.getColumns()) {
					if (c.getFilterExpression() != null) {
						if (!isFiltered) {
							isFiltered = true;
							appendCommentHeader("Used Filters:");
						}
						appendCommentHeader("    " + t.getUnqualifiedName() + "." + c.name + " := " + c.getFilterExpression());
					}
				}
			}
//		}

		return totalProgress;
	}

	/**
	 * Exports all entities from initial-data tables.
	 * 
	 * @param extractionModel the extraction model
	 */
	private Set<Table> exportSubjects(ExtractionModel extractionModel) throws Exception {
		List<AdditionalSubject> allSubjects = new ArrayList<ExtractionModel.AdditionalSubject>();
		for (AdditionalSubject as: extractionModel.additionalSubjects) {
			allSubjects.add(new AdditionalSubject(as.subject, ParameterHandler.assignParameterValues(as.condition, CommandLineParser.getInstance().getParameters())));
		}
		allSubjects.add(new AdditionalSubject(extractionModel.subject, extractionModel.condition.equals("1=1")? "" : extractionModel.condition));
		Map<Table, String> conditionPerTable = new HashMap<Table, String>();
		for (AdditionalSubject as: allSubjects) {
			String cond = conditionPerTable.get(as.subject);
			if (cond == null || cond.trim().length() > 0) {
				if (as.condition.trim().length() > 0) {
					String newCond = "(" + as.condition + ")";
					if (cond == null) {
						cond = newCond;
					} else {
						cond += " or " + newCond;
					}
				} else {
					cond = "";
				}
				conditionPerTable.put(as.subject, cond);
			}
		}
		Set<Table> progress = new HashSet<Table>();
		for (Map.Entry<Table, String> e: conditionPerTable.entrySet()) {
			Table table = e.getKey();
			_log.info("exporting all " + datamodel.getDisplayName(table));
			int today = entityGraph.getAge();
			ProgressListenerRegistry.getProgressListener().collectionJobEnqueued(today, table);
			ProgressListenerRegistry.getProgressListener().collectionJobStarted(today, table);
			long rc = entityGraph.addEntities(table, e.getValue().trim().length() > 0? e.getValue() : "1=1", today);
			if (rc > 0) {
				progress.add(table);
			}
			ProgressListenerRegistry.getProgressListener().collected(today, table, rc);
		}
		return progress;
	}

	/**
	 * Resolves all associations defined in data-model.
	 * 
	 * @param today
	 *            birthday of newly created entities
	 * @param progressOfYesterday
	 *            set of tables to account for resolvation
	 * 
	 * @return map from tables from which entities are added to all associations
	 *         which lead to the entities
	 */
	private Map<Table, Collection<Association>> resolveAssociations(final int today, Map<Table, Collection<Association>> progressOfYesterday) throws Exception {
		final Map<Table, Collection<Association>> progress = new HashMap<Table, Collection<Association>>();

		// resolve associations with same dest-type sequentially
		Map<Table, List<JobManager.Job>> jobsPerDestination = new HashMap<Table, List<JobManager.Job>>();

		for (final Table table : progressOfYesterday.keySet()) {
			for (final Association association : table.associations) {
				Collection<Association> as = progressOfYesterday.get(table);
				if (as != null && as.size() == 1 && as.iterator().next() == association.reversalAssociation) {
					if (association.getCardinality() == Cardinality.MANY_TO_ONE || association.getCardinality() == Cardinality.ONE_TO_ONE) {
						_log.info("skip reversal association " + datamodel.getDisplayName(table) + " -> " + datamodel.getDisplayName(association.destination));
						continue;
					}
				}

				String jc = association.getJoinCondition();
		        if (jc != null) {
		        	ProgressListenerRegistry.getProgressListener().collectionJobEnqueued(today, association);
		        }
				JobManager.Job job = new JobManager.Job() {
					public void run() throws Exception {
						runstats(false);
						if (association.getJoinCondition() != null) {
							_log.info("resolving " + datamodel.getDisplayName(table) + " -> " + association.toString(0, true) + "...");
						}
						ProgressListenerRegistry.getProgressListener().collectionJobStarted(today, association);
						long rc = entityGraph.resolveAssociation(table, association, today);
						ProgressListenerRegistry.getProgressListener().collected(today, association, rc);
						if (rc >= 0) {
							_log.info(rc + " entities found resolving " + datamodel.getDisplayName(table) + " -> " + association.toString(0, true));
						}
						synchronized (progress) {
							if (rc > 0) {
								Collection<Association> as = progress.get(association.destination);
								if (as == null) {
									as = new ArrayList<Association>();
									progress.put(association.destination, as);
								}
								as.add(association);
							}
						}
					}
				};
				List<JobManager.Job> jobList = jobsPerDestination.get(association.destination);
				if (jobList == null) {
					jobList = new ArrayList<JobManager.Job>();
					jobsPerDestination.put(association.destination, jobList);
				}
				jobList.add(job);
			}
		}
		List<JobManager.Job> jobs = new ArrayList<JobManager.Job>();
		for (final Map.Entry<Table, List<JobManager.Job>> entry : jobsPerDestination.entrySet()) {
			jobs.add(new JobManager.Job() {
				public void run() throws Exception {
					for (JobManager.Job job : entry.getValue()) {
						job.run();
					}
				}
			});
		}
		jobManager.executeJobs(jobs);

		if (EntityGraph.maxTotalRowcount > 0 && EntityGraph.maxTotalRowcount < entityGraph.getTotalRowcount()) {
			throw new RuntimeException("found more than " + EntityGraph.maxTotalRowcount + " entities.");
		}

		return progress;
	}

	/**
	 * Adds all dependencies.
	 * 
	 * @param progress
	 *            set of tables to take into account
	 */
	public void addDependencies(Set<Table> progress, boolean treatAggregationAsDependency) throws Exception {
		List<JobManager.Job> jobs = new ArrayList<JobManager.Job>();
		for (final Table table : progress) {
			for (final Association association : table.associations) {
				if (progress.contains(association.destination)) {
					final int aggregationId = treatAggregationAsDependency ? association.getId() : 0;
					final int dependencyId = association.getId();
					if (treatAggregationAsDependency) {
						if (association.getAggregationSchema() != AggregationSchema.NONE) {
							final String jc = association.getUnrestrictedJoinCondition();
							jobs.add(new JobManager.Job() {
								public void run() throws Exception {
									_log.info("find aggregation for " + datamodel.getDisplayName(table) + " -> "
											+ datamodel.getDisplayName(association.destination) + " on " + jc);
									String fromAlias, toAlias;
									fromAlias = association.reversed ? "B" : "A";
									toAlias = association.reversed ? "A" : "B";
									entityGraph.addDependencies(table, fromAlias, association.destination, toAlias, jc, aggregationId, dependencyId,
											association.reversed);
								}
							});
						}
					} else {
						final String jc = association.getUnrestrictedJoinCondition();
						if (jc != null && association.isInsertDestinationBeforeSource()) {
							jobs.add(new JobManager.Job() {
								public void run() throws Exception {
									_log.info("find dependencies " + datamodel.getDisplayName(table) + " -> "
											+ datamodel.getDisplayName(association.destination) + " on " + jc);
									String fromAlias, toAlias;
									fromAlias = association.reversed ? "B" : "A";
									toAlias = association.reversed ? "A" : "B";
									entityGraph.addDependencies(table, fromAlias, association.destination, toAlias, jc, aggregationId, dependencyId,
											association.reversed);
								}
							});
						}
						if (jc != null && association.isInsertSourceBeforeDestination()) {
							jobs.add(new JobManager.Job() {
								public void run() throws Exception {
									_log.info("find dependencies " + datamodel.getDisplayName(association.destination) + " -> "
											+ datamodel.getDisplayName(table) + " on " + jc);
									String fromAlias, toAlias;
									fromAlias = association.reversed ? "B" : "A";
									toAlias = association.reversed ? "A" : "B";
									entityGraph.addDependencies(association.destination, toAlias, table, fromAlias, jc, aggregationId, dependencyId,
											association.reversed);
								}
							});
						}
					}
				}
			}
		}
		jobManager.executeJobs(jobs);
	}

	/**
	 * Writes entities into extract-SQL-script.
	 * 
	 * @param sqlScriptFile
	 *            the name of the sql-script to write the data to
	 * @param transformerHandler
	 *            SAX transformer handler for generating XML. <code>null</code>
	 *            if script format is not XML.
	 * @param table
	 *            write entities from this table only
	 * @param result
	 *            a writer for the extract-script
	 * @param orderByPK
	 *            if <code>true</code>, result will be ordered by primary keys
	 */
	private void writeEntities(OutputStreamWriter result, TransformerHandler transformerHandler, ScriptType scriptType, Table table, boolean orderByPK, String filepath)
			throws Exception {
		ResultSetReader reader = createResultSetReader(result, transformerHandler, scriptType, table, filepath);
		entityGraph.readEntities(table, reader, orderByPK);
		entityGraph.deleteEntities(table);
	}

	/**
	 * Creates result set reader for processing the rows to be exported.
	 * 
	 * @param outputWriter
	 *            writer into export file
	 * @param transformerHandler
	 *            SAX transformer handler for generating XML. <code>null</code>
	 *            if script format is not XML.
	 * @param scriptType
	 *            the script type
	 * @param table
	 *            the table to read rows from
	 * 
	 * @return result set reader for processing the rows to be exported
	 */
	private ResultSetReader createResultSetReader(OutputStreamWriter outputWriter, TransformerHandler transformerHandler, ScriptType scriptType, Table table, String filepath)
			throws SQLException {
		Session targetSession = entityGraph.getTargetSession();
		if (scriptType == ScriptType.INSERT) {
			if (ScriptFormat.DBUNIT_FLAT_XML.equals(CommandLineParser.getInstance().getScriptFormat())) {
				return new FlatXMLTransformer(table, transformerHandler, targetSession.getMetaData(), targetSession.dbms);
			}else if(ScriptFormat.LIQUIBASE_XML.equals(CommandLineParser.getInstance().getScriptFormat())){
				return new LiquibaseXMLTransformer(table,transformerHandler,targetSession.getMetaData(), entityGraph, filepath,
						CommandLineParser.getInstance().xmlDatePattern,
						CommandLineParser.getInstance().xmlTimePattern,
						CommandLineParser.getInstance().xmlTimeStampPattern);
			} else {
				return new DMLTransformer(table, outputWriter, CommandLineParser.getInstance().upsertOnly, CommandLineParser.getInstance().numberOfEntities,
						targetSession.getMetaData(), targetSession);
			}
		} else {
			return new DeletionTransformer(table, outputWriter, CommandLineParser.getInstance().numberOfEntities, targetSession.getMetaData(),
					targetSession);
		}
	}

	/**
	 * Writes entities into extract-SQL-script.
	 * 
	 * @param sqlScriptFile
	 *            the name of the sql-script to write the data to
	 * @param progress
	 *            set of tables to account for extraction
	 */
	public void writeEntities(final String sqlScriptFile, final ScriptType scriptType, final Set<Table> progress, Session session) throws Exception {
		_log.info("writing file '" + sqlScriptFile + "'...");

		OutputStream outputStream = new FileOutputStream(sqlScriptFile);
		if (sqlScriptFile.toLowerCase().endsWith(".zip")) {
			outputStream = new ZipOutputStream(outputStream);
			String zipFileName = new File(sqlScriptFile).getName();
			((ZipOutputStream)outputStream).putNextEntry(new ZipEntry(zipFileName.substring(0, zipFileName.length() - 4)));
		} else {
			if (sqlScriptFile.toLowerCase().endsWith(".gz")) {
				outputStream = new GZIPOutputStream(outputStream);
			}
		}
		TransformerHandler transformerHandler = null;
		OutputStreamWriter result = null;
		Charset charset = Charset.defaultCharset();
		if (CommandLineParser.getInstance().uTF8) {
			charset = Charset.forName("UTF8");
		}
		if (scriptType == ScriptType.INSERT && ScriptFormat.DBUNIT_FLAT_XML.equals(CommandLineParser.getInstance().getScriptFormat())) {
			StreamResult streamResult = new StreamResult(new OutputStreamWriter(outputStream, charset));
			transformerHandler = XmlUtil.createTransformerHandler(commentHeader.toString(), "dataset", streamResult, charset);
		} else if(scriptType == ScriptType.INSERT && ScriptFormat.LIQUIBASE_XML.equals(CommandLineParser.getInstance().getScriptFormat())){
			StreamResult streamResult = new StreamResult(
					new OutputStreamWriter(outputStream,
							charset));
			
		
			transformerHandler = XmlUtil.createTransformerHandler(commentHeader.toString(), "", streamResult, charset);	//root tag removed to add namespaces 

			AttributesImpl attrdatabaseChangeLog = new AttributesImpl();
			attrdatabaseChangeLog.addAttribute("", "", "xmlns:xsi", "", "http://www.w3.org/2001/XMLSchema-instance");
			attrdatabaseChangeLog.addAttribute("", "", "xmlns:ext", "", "http://www.liquibase.org/xml/ns/dbchangelog-ext");
			attrdatabaseChangeLog.addAttribute("", "", "xsi:schemaLocation", "", "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd");
			transformerHandler.startElement("http://www.liquibase.org/xml/ns/dbchangelog", "", "databaseChangeLog",attrdatabaseChangeLog);
			
			AttributesImpl attrchangeset = new AttributesImpl();
			attrchangeset.addAttribute("", "", "id", "","JailerExport" );
			attrchangeset.addAttribute("", "", "author", "",System.getProperty("user.name") );
			
			transformerHandler.startElement("", "", "changeSet", attrchangeset);
		} else {
			if (CommandLineParser.getInstance().uTF8) {
				result = new OutputStreamWriter(outputStream, charset);
			} else {
				result = new OutputStreamWriter(outputStream);
			}
			result.append(commentHeader);
			// result.append(System.getProperty("line.separator"));
			for (ScriptEnhancer enhancer : Configuration.getScriptEnhancer()) {
				enhancer.addComments(result, scriptType, session, entityGraph, progress);
			}
			// result.append(System.getProperty("line.separator"));
			// result.append(System.getProperty("line.separator"));
			for (ScriptEnhancer enhancer : Configuration.getScriptEnhancer()) {
				enhancer.addProlog(result, scriptType, session, entityGraph, progress);
			}
		}

		// first write entities of independent tables
		final Set<Table> dependentTables = writeEntitiesOfIndependentTables(result, transformerHandler, scriptType, progress, sqlScriptFile);

		// then write entities of tables having cyclic-dependencies
		_log.info("cyclic dependencies for: " + asString(dependentTables));
		if (!CommandLineParser.getInstance().noSorting) {
			addDependencies(dependentTables, false);
			runstats(true);
			removeSingleRowCycles(progress, session);
		} else {
			_log.warn("skipping topological sorting");
		}

		final TransformerHandler fTransformerHandler = transformerHandler;
		final OutputStreamWriter fResult = result;
		long rest;

		if (scriptType == ScriptType.INSERT && (ScriptFormat.DBUNIT_FLAT_XML.equals(CommandLineParser.getInstance().getScriptFormat())||ScriptFormat.LIQUIBASE_XML.equals(CommandLineParser.getInstance().getScriptFormat()))) {
			Set<Table> remaining = new HashSet<Table>(dependentTables);

			// topologically sort remaining tables while ignoring reflexive
			// dependencies
			// and dependencies for which no edge exists in entity graph
			Set<Association> relevantAssociations = new HashSet<Association>(datamodel.namedAssociations.values());
			Set<Integer> existingEdges = entityGraph.getDistinctDependencyIDs();
			for (Iterator<Association> i = relevantAssociations.iterator(); i.hasNext();) {
				Association association = i.next();
				if (association.source.equals(association.destination)) {
					i.remove();
				} else if (!existingEdges.contains(association.getId())) {
					if (association.isInsertDestinationBeforeSource()) {
						_log.info("irrelevant dependency: " + datamodel.getDisplayName(association.source) + " -> "
								+ datamodel.getDisplayName(association.destination));
					}
					i.remove();
				}
			}
			Set<Table> independentTables = datamodel.getIndependentTables(remaining, relevantAssociations);
			rest = entityGraph.getSize();
			while (!independentTables.isEmpty()) {
				_log.info("independent tables: " + asString(independentTables));
				for (final Table independentTable : independentTables) {
					rest = entityGraph.getSize();
					for (;;) {
						entityGraph.markIndependentEntities(independentTable);
						// don't use jobManager, export rows sequentially, don't
						// mix rows of different tables in a dataset!
						ResultSetReader reader = createResultSetReader(fResult, fTransformerHandler, scriptType, independentTable, sqlScriptFile);
						entityGraph.readMarkedEntities(independentTable, reader, true);
						entityGraph.deleteIndependentEntities(independentTable);
						long newRest = entityGraph.getSize();
						if (rest == newRest) {
							break;
						}
						rest = newRest;
					}
				}
				remaining.removeAll(independentTables);
				independentTables = datamodel.getIndependentTables(remaining, relevantAssociations);
			}
		} else {
			rest = entityGraph.getSize();
			for (;;) {
				for (final Table table : dependentTables) {
					entityGraph.markIndependentEntities(table);
				}
				List<JobManager.Job> jobs = new ArrayList<JobManager.Job>();
				for (final Table table : dependentTables) {
					jobs.add(new JobManager.Job() {
						public void run() throws Exception {
							ResultSetReader reader = createResultSetReader(fResult, fTransformerHandler, scriptType, table, sqlScriptFile);
							entityGraph.readMarkedEntities(table, reader, false);
						}
					});
				}
				jobManager.executeJobs(jobs);
				for (final Table table : dependentTables) {
					entityGraph.deleteIndependentEntities(table);
				}
				long newRest = entityGraph.getSize();
				if (rest == newRest) {
					break;
				}
				rest = newRest;
			}
		}

		if (result != null) {
			// write epilogs
			for (ScriptEnhancer enhancer : Configuration.getScriptEnhancer()) {
				enhancer.addEpilog(result, scriptType, session, entityGraph, progress);
			}
			result.close();
		}

		if (transformerHandler != null) {
			String content = "\n";
			transformerHandler.characters(content.toCharArray(), 0, content.length());
			if (ScriptFormat.LIQUIBASE_XML.equals(CommandLineParser.getInstance().getScriptFormat())) {

				transformerHandler.endElement("","", "changeSet");
				transformerHandler.endElement("","", "databaseChangeLog");
				
			} else if (ScriptFormat.DBUNIT_FLAT_XML.equals(CommandLineParser.getInstance().getScriptFormat())) {
				transformerHandler.endElement("", "", "dataset");			
			}
			transformerHandler.endDocument();

		}

		if (rest > 0) {
			try {
				new File(sqlScriptFile).renameTo(new File(sqlScriptFile + ".failed"));
			} catch (Exception e) {
				_log.warn(e.getMessage());
			}
			Set<Table> cycle = CycleFinder.getCycle(dependentTables);
			String msgTitel = rest + " entities not exported due to cyclic dependencies.\n";
			String msg = msgTitel + (cycle.size() == 1? "Table" : "Tables") + " with cyclic dependencies: " + asString(cycle);
			_log.error(msg);
			try {
				// try to get a more sophisticated error message
				_log.info("starting cycle analysis...");
				ProgressListenerRegistry.getProgressListener().newStage("cycle error, analysing...", true, false);
				String sMsg = msgTitel + "Paths:\n";
				int i = 0;
				for (CycleFinder.Path path: CycleFinder.findCycle(datamodel, cycle)) {
					List<Table> pList = new ArrayList<Table>();
					path.fillPath(pList);
					sMsg += "[ ";
					boolean ft = true;
					for (Table t: pList) {
						if (!ft) {
							sMsg += " -> ";
						}
						ft = false;
						sMsg += datamodel.getDisplayName(t);
					}
					sMsg += " ]\n";
					if (++i > 30) {
						sMsg += "...\n";
						break;
					}
				}
				msg = sMsg + "\nConsider to disable the option \"sort topologically\" in the Data Export dialog";
			} catch (CancellationException e) {
				CancellationHandler.reset(null);
			} catch (Throwable t) {
				_log.warn("cycle analysis failed: " + t.getMessage());
			}
			throw new RuntimeException(msg);
		}
		_log.info("file '" + sqlScriptFile + "' written.");
	}
	
	/**
	 * Removes all single-row cycles from dependency table.
	 * 
	 * @param progress
	 *            set of all tables from which rows are collected
	 * @param session
	 *            for executing SQL statements
	 */
	private void removeSingleRowCycles(Set<Table> progress, Session session) throws Exception {
		for (Table table : progress) {
			boolean hasReflexiveAssociation = false;
			for (Association a : table.associations) {
				if (a.destination == table) {
					hasReflexiveAssociation = true;
					break;
				}
			}
			if (hasReflexiveAssociation) {
				entityGraph.removeReflexiveDependencies(table);
			}
		}
	}

	/**
	 * Writes entities into XML-document.
	 * 
	 * @param xmlFile
	 *            the name of the xml-file to write the data to
	 * @param progress
	 *            set of tables to account for extraction
	 */
	public void writeEntitiesAsXml(String xmlFile, final Set<Table> progress, final Set<Table> subjects, Session session) throws Exception {
		_log.info("writing file '" + xmlFile + "'...");

		OutputStream outputStream = new FileOutputStream(xmlFile);
		if (xmlFile.toLowerCase().endsWith(".zip")) {
			outputStream = new ZipOutputStream(outputStream);
			String zipFileName = new File(xmlFile).getName();
			((ZipOutputStream)outputStream).putNextEntry(new ZipEntry(zipFileName.substring(0, zipFileName.length() - 4)));
		} else {
			if (xmlFile.toLowerCase().endsWith(".gz")) {
				outputStream = new GZIPOutputStream(outputStream);
			}
		}

		// then write entities of tables having cyclic-dependencies
		_log.info("create hierarchy for: " + asString(progress));
		addDependencies(progress, true);
		runstats(true);
		removeSingleRowCycles(progress, session);

		List<Table> lexSortedTables = new ArrayList<Table>(progress);
		Collections.sort(lexSortedTables, new Comparator<Table>() {
			public int compare(Table t1, Table t2) {
				boolean s1 = subjects.contains(t1);
				boolean s2 = subjects.contains(t2);
				if (s1 && !s2) {
					return -1;
				}
				if (!s1 && s2) {
					return 1;
				}
				return datamodel.getDisplayName(t1).compareTo(datamodel.getDisplayName(t2));
			}
		});

		List<Table> sortedTables = new ArrayList<Table>();
		boolean done = false;
		while (!done) {
			done = true;
			for (int step = 1; step <= 2; ++step) {
				for (Table table: lexSortedTables) {
					boolean depends = false;
					for (Association association: table.associations) {
						if (association.destination != table) {
							if (association.isInsertDestinationBeforeSource()) {
								if (lexSortedTables.contains(association.destination)) {
									if (step == 1 || (association.getAggregationSchema() == AggregationSchema.NONE && association.reversalAssociation.getAggregationSchema() == AggregationSchema.NONE)) {
										depends = true;
										break;
									}
								}
							}
						}
					}
					if (!depends) {
						sortedTables.add(table);
						done = false;
					}
				}
				if (!done) {
					break;
				}
			}
			lexSortedTables.removeAll(sortedTables);
		}
		if (!lexSortedTables.isEmpty()) {
			_log.warn("remaining tables after sorting: " + PrintUtil.tableSetAsString(new HashSet<Table>(lexSortedTables)));
			sortedTables.addAll(lexSortedTables);
		}
		
		Set<Table> cyclicAggregatedTables = getCyclicAggregatedTables(progress);
		_log.info("cyclic aggregated tables: " + PrintUtil.tableSetAsString(cyclicAggregatedTables));

		Charset charset = Charset.defaultCharset();
		if (CommandLineParser.getInstance().uTF8) {
			charset = Charset.forName("UTF8");
		}
		
		XmlExportTransformer reader = new XmlExportTransformer(outputStream, commentHeader.toString(), entityGraph, progress, cyclicAggregatedTables,
				CommandLineParser.getInstance().xmlRootTag, CommandLineParser.getInstance().xmlDatePattern,
				CommandLineParser.getInstance().xmlTimeStampPattern, entityGraph.getTargetSession(), charset);

		for (Table table : sortedTables) {
			entityGraph.markRoots(table);
		}
		for (Table table : sortedTables) {
			_log.info("exporting table " + datamodel.getDisplayName(table));
			reader.setTable(table);
			entityGraph.readMarkedEntities(table, reader, reader.getTableMapping(table).selectionSchema, reader.getTableMapping(table).originalPKAliasPrefix, true);
		}
		reader.endDocument();

		outputStream.close();
		checkCompletenessOfXmlExport(cyclicAggregatedTables);
		_log.info("file '" + xmlFile + "' written.");
	}

	/**
	 * Gets set of cyclic aggregated tables.
	 */
	private Set<Table> getCyclicAggregatedTables(Set<Table> progress) {
		Set<Table> cyclicAggregatedTables = new HashSet<Table>(progress);

		for (;;) {
			Set<Table> nonAggregatedTables = new HashSet<Table>();
			for (Table t : cyclicAggregatedTables) {
				boolean isAggregated = false;
				for (Association association : t.associations) {
					if (association.reversalAssociation.getAggregationSchema() != AggregationSchema.NONE) {
						if (cyclicAggregatedTables.contains(association.destination)) {
							isAggregated = true;
							break;
						}
					}
				}
				if (!isAggregated) {
					nonAggregatedTables.add(t);
				}
			}
			if (nonAggregatedTables.isEmpty()) {
				break;
			}
			cyclicAggregatedTables.removeAll(nonAggregatedTables);
		}
		return cyclicAggregatedTables;
	}

	/**
	 * Checks whether some entities are not exported due to cyclic aggregation.
	 * 
	 * @param cyclicAggregatedTables
	 *            tables to check
	 */
	private void checkCompletenessOfXmlExport(Set<Table> cyclicAggregatedTables) throws SQLException {
		for (Table table : cyclicAggregatedTables) {
			entityGraph.readNonTraversedDependencies(table, new Session.ResultSetReader() {
				public void readCurrentRow(ResultSet resultSet) throws SQLException {
					String message = "Can't export all rows from table '" + resultSet.getString("TO_TYPE") + "' due to cyclic aggregation";
					throw new RuntimeException(message);
				}

				public void close() {
				}
			});
		}
	}

	/**
	 * Writes entities of independent tables.
	 * 
	 * @param result
	 *            a writer for the extract-script
	 * @param progress
	 *            set of tables involved in export
	 * 
	 * @return set of tables from which no entities are written
	 */
	Set<Table> writeEntitiesOfIndependentTables(final OutputStreamWriter result, final TransformerHandler transformerHandler, final ScriptType scriptType,
			Set<Table> progress, final String filepath) throws Exception {
		Set<Table> tables = new HashSet<Table>(progress);

		Set<Table> independentTables = datamodel.getIndependentTables(tables);
		while (!independentTables.isEmpty()) {
			_log.info("independent tables: " + asString(independentTables));
			List<JobManager.Job> jobs = new ArrayList<JobManager.Job>();
			for (final Table independentTable : independentTables) {
				if (ScriptFormat.DBUNIT_FLAT_XML.equals(CommandLineParser.getInstance().getScriptFormat()) || ScriptFormat.LIQUIBASE_XML.equals(CommandLineParser.getInstance().getScriptFormat())) {
					// export rows sequentially, don't mix rows of different
					// tables in a dataset!
					writeEntities(result, transformerHandler, scriptType, independentTable, true, filepath);
				} else {
					jobs.add(new JobManager.Job() {
						public void run() throws Exception {
							writeEntities(result, transformerHandler, scriptType, independentTable, false, filepath);
						}
					});
				}
			}
			if (!jobs.isEmpty()) {
				jobManager.executeJobs(jobs);
			}
			tables.removeAll(independentTables);
			independentTables = datamodel.getIndependentTables(tables);
		}

		return tables;
	}

	/**
	 * Prevents multiple shutdowns.
	 */
	private boolean isDown = false;

	/**
	 * Shuts the archiver down.
	 */
	public void shutDown() throws SQLException {
		if (!isDown) {
			jobManager.shutdown();
			entityGraph.shutDown();
			isDown = true;
		}
	}

	/**
	 * Stringifies progress-set.
	 * 
	 * @param progress
	 *            the progress-set
	 * @return the progress-set as string
	 */
	private static String asString(Set<Table> progress) {
		String str = "";
		for (Table table : progress) {
			if (!"".equals(str)) {
				str += ", ";
			}
			str += table.getName();
		}
		return str;
	}

	/**
	 * Total row-count at last runstats run.
	 */
	private long lastRunstats = 0;

	/**
	 * Runs script for updating the DB-statistics.
	 */
	private synchronized void runstats(boolean force) throws Exception {
		if (entityGraph != null) {
			Session session = entityGraph.getSession();
			if (force || lastRunstats == 0 || (lastRunstats * 2 <= entityGraph.getTotalRowcount() && entityGraph.getTotalRowcount() > 1000)) {
				lastRunstats = entityGraph.getTotalRowcount();
	
				StatisticRenovator statisticRenovator = Configuration.forDbms(session).getStatisticRenovator();
				if (statisticRenovator != null) {
					_log.info("gather statistics after " + lastRunstats + " inserted rows...");
					try {
						statisticRenovator.renew(session);
					} catch (Throwable t) {
						_log.warn("unable to update table statistics: " + t.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Main-method for CLI.
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		final Thread mainThread = Thread.currentThread();
		Thread shutdownHook;
		Runtime.getRuntime().addShutdownHook(shutdownHook = new Thread("shutdown-hook") {
		    public void run() {
		        CancellationHandler.cancel(null);
		        try {
					mainThread.join();
				} catch (InterruptedException e) {
					// ignore
				}
		    }
		});
		try {
			jailerMain(args, new StringBuffer());
		} catch (Exception e) {
			// Exception has already been logged
		} finally {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/**
	 * Main-method for GUI.
	 * 
	 * @param args
	 *            arguments
	 * @param warnings
	 *            string-buffer to print warnings into, may be <code>null</code>
	 * @return <code>false</code> iff something went wrong
	 */
	public static boolean jailerMain(String[] args, StringBuffer warnings) throws Exception {
		return jailerMain(args, warnings, null);
	}
	
	/**
	 * Main-method for GUI.
	 * 
	 * @param args
	 *            arguments
	 * @param warnings
	 *            string-buffer to print warnings into, may be <code>null</code>
	 * @param progressListener listens to progess events
	 * @return <code>false</code> iff something went wrong
	 */
	public static boolean jailerMain(String[] args, StringBuffer warnings, ProgressListener progressListener) throws Exception {
		CancellationHandler.reset(null);
		Session.closeTemporaryTableSession();

		try {
			ProgressListenerRegistry.setProgressListener(progressListener);
			
			if (!CommandLineParser.parse(args, false)) {
				return false;
			}

			if (Configuration.getDoMinimizeUPK()) {
				_log.info("minimize-UPK=" + Configuration.getDoMinimizeUPK());
			}

			CommandLineParser clp = CommandLineParser.getInstance();
			
			String command = clp.arguments.get(0);
			if (!"create-ddl".equalsIgnoreCase(command)) {
				if (!"find-association".equalsIgnoreCase(command)) {
					_log.info("Jailer " + VERSION);
				}
			}
			
			Session.setClassLoaderForJdbcDriver(ClasspathUtil.addJarToClasspath(clp.jdbcjar, clp.jdbcjar2));
			
			if ("check-domainmodel".equalsIgnoreCase(command)) {
				DataModel dataModel = new DataModel();
				for (String rm : clp.arguments.subList(1, clp.arguments.size())) {
					if (dataModel.getRestrictionModel() == null) {
						dataModel.setRestrictionModel(new RestrictionModel(dataModel));
					}
					dataModel.getRestrictionModel().addRestrictionDefinition(rm, null, new HashMap<String, String>());
				}
				new DomainModel(dataModel).check();
			} else if ("render-datamodel".equalsIgnoreCase(command)) {
				if (clp.arguments.size() <= 1) {
					CommandLineParser.printUsage();
				} else {
					new Jailer(1).renderDataModel(clp.arguments, clp.withClosures, clp.schema);
				}
			} else if ("import".equalsIgnoreCase(command)) {
				if (clp.arguments.size() != 6) {
					CommandLineParser.printUsage();
				} else {
					Session session = new Session(clp.arguments.get(2), clp.arguments.get(3), clp.arguments.get(4),
							clp.arguments.get(5), null, clp.transactional);
					try {
						SqlScriptExecutor.executeScript(clp.arguments.get(1), session, clp.transactional);
					} finally {
						try {
							session.shutDown();
						} catch (Exception e) {
							// ignore
						}
					}
				}
			} else if ("print-datamodel".equalsIgnoreCase(command)) {
				printDataModel(clp.arguments, clp.withClosures);
			} else if ("export".equalsIgnoreCase(command)) {
				if (clp.arguments.size() != 6) {
					CommandLineParser.printUsage();
				} else {
					if (clp.maxNumberOfEntities > 0) {
						EntityGraph.maxTotalRowcount = clp.maxNumberOfEntities;
						_log.info("max-rowcount=" + EntityGraph.maxTotalRowcount);
					}
					
					if (clp.exportScriptFileName == null) {
						System.out.println("missing '-e' option");
						CommandLineParser.printUsage();
					} else {
						export(clp.arguments.get(1), clp.exportScriptFileName, clp.deleteScriptFileName, clp.arguments.get(2), clp.arguments.get(3),
								clp.arguments.get(4), clp.arguments.get(5), clp.explain, clp.numberOfThreads, clp.getScriptFormat());
					}
				}
			} else if ("delete".equalsIgnoreCase(command)) {
				if (clp.arguments.size() != 6) {
					CommandLineParser.printUsage();
				} else {
					if (clp.deleteScriptFileName == null) {
						System.out.println("missing '-d' option");
						CommandLineParser.printUsage();
					} else {
						// note we are passing null for script format and the export script name, as we are using the export tool
						// to generate the delete script only.
						export(clp.arguments.get(1), /* clp.exportScriptFileName*/ null, clp.deleteScriptFileName, clp.arguments.get(2), clp.arguments.get(3),
									clp.arguments.get(4), clp.arguments.get(5), clp.explain, clp.numberOfThreads, /*scriptFormat*/ null);
					}
				}
			} else if ("find-association".equalsIgnoreCase(command)) {
				if (clp.arguments.size() < 3) {
					CommandLineParser.printUsage();
				} else {
					findAssociation(clp.arguments.get(1), clp.arguments.get(2), clp.arguments.subList(3, clp.arguments.size()), clp.undirected);
				}
			} else if ("create-ddl".equalsIgnoreCase(command)) {
				if (clp.arguments.size() == 5) {
					return DDLCreator.createDDL(clp.arguments.get(1), clp.arguments.get(2), clp.arguments.get(3), clp.arguments.get(4), clp
							.getTemporaryTableScope());
				}
				return DDLCreator.createDDL(null, null, null, null, clp.getTemporaryTableScope());
			} else if ("build-model".equalsIgnoreCase(command)) {
				if (clp.arguments.size() != 5) {
					CommandLineParser.printUsage();
				} else {
					_log.info("Building data model.");
					ModelBuilder.build(clp.arguments.get(1), clp.arguments.get(2), clp.arguments.get(3), clp.arguments.get(4), clp.schema, warnings);
				}
			} else {
				CommandLineParser.printUsage();
				return false;
			}
			return true;
		} catch (Exception e) {
			if (e instanceof CancellationException) {
				_log.warn("cancelled");
				throw e;
			}
			_log.error(e.getMessage(), e);
			System.out.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
			String workingDirectory = System.getProperty("user.dir");
			_log.error("working directory is " + workingDirectory);
			throw e;
		} finally {
			ProgressListenerRegistry.setProgressListener(null);
			Session.closeTemporaryTableSession();
		}
	}

	/**
	 * Render the data model.
	 * 
	 * @param schema schema to analyze
	 */
	private void renderDataModel(List<String> arguments, boolean withClosures, String schema) throws Exception {
		DataModel dataModel = new DataModel();
		for (String rm : arguments.subList(1, arguments.size())) {
			if (dataModel.getRestrictionModel() == null) {
				dataModel.setRestrictionModel(new RestrictionModel(dataModel));
			}
			dataModel.getRestrictionModel().addRestrictionDefinition(rm, null, new HashMap<String, String>());
		}
		DataModelRenderer renderer = Configuration.getRenderer();
		if (renderer == null) {
			throw new RuntimeException("no renderer found");
		}
		renderer.render(dataModel);
	}

	/**
	 * Exports entities.
	 */
	private static void export(String extractionModelFileName, String scriptFile, String deleteScriptFileName, String driverClassName, String dbUrl,
			String dbUser, String dbPassword, boolean explain, int threads, ScriptFormat scriptFormat) throws Exception {
		
		if (scriptFile != null) {
			_log.info("exporting '" + extractionModelFileName + "' to '" + scriptFile + "'");
		}
		
		Session session = new Session(driverClassName, dbUrl, dbUser, dbPassword, CommandLineParser.getInstance().getTemporaryTableScope(), false);
		if (CommandLineParser.getInstance().getTemporaryTableScope() == TemporaryTableScope.SESSION_LOCAL
		 || CommandLineParser.getInstance().getTemporaryTableScope() == TemporaryTableScope.TRANSACTION_LOCAL) {
			DDLCreator.createDDL(session, CommandLineParser.getInstance().getTemporaryTableScope());
		}

		ExtractionModel extractionModel = new ExtractionModel(extractionModelFileName, CommandLineParser.getInstance().getSourceSchemaMapping(), CommandLineParser.getInstance().getParameters());

		_log.info(Configuration.forDbms(session).getSqlDialect());
		
		EntityGraph entityGraph;
		if (CommandLineParser.getInstance().getTemporaryTableScope() == TemporaryTableScope.LOCAL_DATABASE) {
			entityGraph = LocalEntityGraph.create(extractionModel.dataModel, EntityGraph.createUniqueGraphID(), session);
		} else {
			entityGraph = RemoteEntityGraph.create(extractionModel.dataModel, EntityGraph.createUniqueGraphID(), session, extractionModel.dataModel.getUniversalPrimaryKey(session));
		}

		entityGraph.setExplain(explain);
		final Jailer jailer = new Jailer(threads);

		Charset charset = Charset.defaultCharset();
		if (CommandLineParser.getInstance().uTF8) {
			charset = Charset.forName("UTF8");
			jailer.appendCommentHeader("encoding " + charset.name());
			jailer.appendCommentHeader("");
		}
		jailer.appendCommentHeader("generated by Jailer " + VERSION + ", " + new Date() + " from " + getUsername());
		Set<Table> totalProgress = new HashSet<Table>();
		Set<Table> subjects = new HashSet<Table>();

		if (CommandLineParser.getInstance().where != null && CommandLineParser.getInstance().where.trim().length() > 0) {
			extractionModel.condition = CommandLineParser.getInstance().where;
		}

		jailer.appendCommentHeader("");
		String condition = (extractionModel.condition != null && !"1=1".equals(extractionModel.condition)) ? extractionModel.subject.getName() + " where " + extractionModel.condition
				: "all rows from " + extractionModel.subject.getName();
		jailer.appendCommentHeader("Extraction Model:  " + condition + " (" + extractionModelFileName + ")");
		for (AdditionalSubject as: extractionModel.additionalSubjects) {
			condition = (as.condition != null && as.condition.trim().length() > 0) ? as.subject.getName() + " where " + as.condition
					: "all rows from " + as.subject.getName();
			jailer.appendCommentHeader("                   Union " + condition);
		}
		if (CommandLineParser.getInstance().noSorting) {
			jailer.appendCommentHeader("                   unsorted");
		}
		jailer.appendCommentHeader("Database:          " + Configuration.forDbms(session).dbms);
		jailer.appendCommentHeader("Database URL:      " + dbUrl);
		jailer.appendCommentHeader("Database User:     " + dbUser);
		jailer.appendCommentHeader("");

		extractionModel.dataModel.checkForPrimaryKey(extractionModel.subject, deleteScriptFileName != null);

		extractionModel.condition = ParameterHandler.assignParameterValues(extractionModel.condition, CommandLineParser.getInstance().getParameters());
		
		if (!CommandLineParser.getInstance().getParameters().isEmpty()) {
			String suffix = "Parameters:        ";
			for (Map.Entry<String, String> e: CommandLineParser.getInstance().getParameters().entrySet()) {
				jailer.appendCommentHeader(suffix + e.getKey() + " = " + e.getValue());
				suffix = "                   ";
			}
			jailer.appendCommentHeader("");
		}
		
		EntityGraph graph = entityGraph;
		jailer.setEntityGraph(graph);
		jailer.setDataModel(extractionModel.dataModel);
		EntityGraph exportedEntities = null;
		
		try {
			jailer.runstats(false);
			ProgressListenerRegistry.getProgressListener().newStage("collecting rows", false, false);
			Set<Table> progress = jailer.exportSubjects(extractionModel);
			entityGraph.setBirthdayOfSubject(entityGraph.getAge());
			progress.addAll(jailer.export(extractionModel.subject, extractionModel.condition, progress, true));
			totalProgress.addAll(progress);
			subjects.add(extractionModel.subject);
	
			if (explain) {
				ProgressListenerRegistry.getProgressListener().newStage("generating explain-log", false, false);
				ExplainTool.explain(entityGraph, new HashSet<Table>(), session);
			}
	
			totalProgress = jailer.datamodel.normalize(totalProgress);
			subjects = jailer.datamodel.normalize(subjects);
	
			if (deleteScriptFileName != null) {
				exportedEntities = entityGraph.copy(EntityGraph.createUniqueGraphID(), session);
			}

			if (scriptFile != null) {
				ProgressListenerRegistry.getProgressListener().newStage("exporting rows", false, false);
				
				jailer.setEntityGraph(entityGraph);
				if (ScriptFormat.XML.equals(scriptFormat)) {
					jailer.writeEntitiesAsXml(scriptFile, totalProgress, subjects, session);
				} else {
					jailer.writeEntities(scriptFile, ScriptType.INSERT, totalProgress, session);
				}
			}
			entityGraph.delete();
			
			if (deleteScriptFileName != null) {
				ProgressListenerRegistry.getProgressListener().newStage("deletion-check", false, false);
				jailer.setEntityGraph(exportedEntities);
				jailer.deleteEntities(subjects, totalProgress, session, CommandLineParser.getInstance().getTabuTables(jailer.datamodel,
						CommandLineParser.getInstance().getSourceSchemaMapping()));
				ProgressListenerRegistry.getProgressListener().newStage("writing delete-script", false, false);
				jailer.datamodel.transpose();
				jailer.writeEntities(deleteScriptFileName, ScriptType.DELETE, totalProgress, session);
				exportedEntities.delete();
				exportedEntities.shutDown();
				jailer.setEntityGraph(entityGraph);
			}
			entityGraph.close();
		} catch (CancellationException e) {
			try {
				_log.info("cleaning up after cancellation...");
				CancellationHandler.reset(null);
				jailer.entityGraph.getSession().rollbackAll();
				jailer.entityGraph.delete();
				if (exportedEntities != null) {
					if (jailer.entityGraph.getSession().scope == TemporaryTableScope.GLOBAL) {
						exportedEntities.delete();
					} else {
						_log.info("skipping clean up of temporary tables");
					}
				}
				_log.info("cleaned up");
				entityGraph.close();
				jailer.shutDown();
			} catch (Throwable t) {
				_log.warn(t.getMessage());
			}
			throw e;
		} catch (Exception e) {
			try {
				_log.info("cleaning up...");
				jailer.entityGraph.delete();
				if (exportedEntities != null) {
					if (jailer.entityGraph.getSession().scope == TemporaryTableScope.GLOBAL) {
						exportedEntities.delete();
					} else {
						_log.info("skipping clean up of temporary tables");
					}
				}
				entityGraph.close();
				jailer.shutDown();
			} catch (Throwable t) {
				_log.warn(t.getMessage());
			}
			throw e;
		}
		jailer.shutDown();
	}

	/**
	 * Gets user-name.
	 * 
	 * @return the user-name
	 */
	private static String getUsername() {
		String host = "";
		try {
			host = "@" + InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		return System.getProperty("user.name") + host;
	}

	/**
	 * Calculates D=(E-T)-C*(U-(E-T)) where E is the entity-graph of this
	 * export-tool, see
	 * http://intra.*.de/dokuwiki/doku.php?id=projekte:sql-export-tool-phase2.
	 * 
	 * @param subjects
	 *            set of tables containing subjects of extraction-tasks
	 * @param allTables
	 *            set of tables from which there are entities in E
	 * @param statementExecutor
	 *            for executing SQL-statements
	 * @param tabuTables
	 *            never deletes entities of one of this tables
	 */
	private void deleteEntities(Set<Table> subjects, Set<Table> allTables, Session session, Set<Table> tabuTables) throws Exception {
		appendCommentHeader("");
		appendCommentHeader("Tabu-tables: " + PrintUtil.tableSetAsString(tabuTables, "--                 "));
		_log.info("Tabu-tables: " + PrintUtil.tableSetAsString(tabuTables, null));

		final Map<Table, Long> removedEntities = new HashMap<Table, Long>();

		// do not check tables in first step having exactly one 1:1 or 1:n
		// association
		// from another table
		Set<Table> dontCheckInitially = new HashSet<Table>();
		for (Table table : allTables) {
			int n = 0;
			boolean check = false;
			for (Association a : table.associations) {
				if (!a.reversalAssociation.isIgnored()) {
					if (tabuTables.contains(a.destination)) {
						check = true;
					} else if (a.reversalAssociation.getCardinality() == Cardinality.ONE_TO_MANY
							|| a.reversalAssociation.getCardinality() == Cardinality.ONE_TO_ONE) {
						++n;
					} else {
						check = true;
					}
				}
			}
			if ((!check) && n == 1) {
				dontCheckInitially.add(table);
			}
		}

		// remove tabu entities
		for (Table tabuTable : tabuTables) {
			long rc = entityGraph.deleteEntities(tabuTable);
			_log.info("excluded " + rc + " entities from " + datamodel.getDisplayName(tabuTable) + " (tabu)");
			allTables.remove(tabuTable);
		}

		// set of tables which are known to have no entities in entityGraph
		Set<Table> emptyTables = new HashSet<Table>();

		Set<Table> tablesToCheck = new HashSet<Table>(allTables);
		_log.info("don't check initially: " + PrintUtil.tableSetAsString(dontCheckInitially, null));
		tablesToCheck.removeAll(dontCheckInitially);

		boolean firstStep = true;
		// remove associated entities
		while (!tablesToCheck.isEmpty()) {
			_log.info("tables to check: " + PrintUtil.tableSetAsString(tablesToCheck, null));
			List<JobManager.Job> jobs = new ArrayList<JobManager.Job>();
			final Set<Table> tablesToCheckNextTime = new HashSet<Table>();
			for (final Table table : tablesToCheck) {
				for (final Association a : table.associations) {
					if (emptyTables.contains(table)) {
						continue;
					}
					if (!a.reversalAssociation.isIgnored()) {
						if (entityGraph.countEntities(table) == 0) {
							emptyTables.add(table);
							continue;
						}
						final boolean isFirstStep = firstStep;
						jobs.add(new JobManager.Job() {
							public void run() throws Exception {
								long rc = entityGraph.removeAssociatedDestinations(a.reversalAssociation, !isFirstStep);
								if (rc > 0) {
									synchronized (removedEntities) {
										Long oldRc = removedEntities.get(table);
										removedEntities.put(table, rc + (oldRc == null ? 0 : oldRc));
										_log.info("excluded " + rc + " entities from " + datamodel.getDisplayName(table) + " referenced by " + a);
										for (Association a2 : table.associations) {
											tablesToCheckNextTime.add(a2.destination);
										}
									}
								}
							}
						});
					}
				}
			}
			jobManager.executeJobs(jobs);
			tablesToCheck = tablesToCheckNextTime;
			tablesToCheck.retainAll(allTables);
			firstStep = false;
		}

		_log.info("entities to delete:");
		appendCommentHeader("");
		boolean firstLine = true;
		for (String line : entityGraph.getStatistics(datamodel, removedEntities.keySet())) {
			if (!firstLine) {
				Long re = removedEntities.get(datamodel.getTable(line.split(" ")[0]));
				if (re != null && re != 0L) {
					line += " (-" + re + ")";
				}
			}
			_log.info(line);
			String l = (firstLine ? "Deleted Entities: " : "     ") + line;
			appendCommentHeader(l);
			if (firstLine) {
				appendCommentHeader("");
			}
			firstLine = false;
		}
		appendCommentHeader("");
	}

	/**
	 * Prints shortest association between two tables.
	 */
	private static void findAssociation(String from, String to, List<String> restModels, boolean undirected) throws Exception {
		DataModel dataModel = new DataModel();
		for (String rm : restModels) {
			if (dataModel.getRestrictionModel() == null) {
				dataModel.setRestrictionModel(new RestrictionModel(dataModel));
			}
			dataModel.getRestrictionModel().addRestrictionDefinition(rm, null, new HashMap<String, String>());
		}
		Table source = dataModel.getTable(from);
		if (source == null) {
			throw new RuntimeException("unknown table: '" + from);
		}
		Table destination = dataModel.getTable(to);
		if (destination == null) {
			throw new RuntimeException("unknown table: '" + to);
		}

		Set<Table> tablesToIgnore = CommandLineParser.getInstance().getTabuTables(dataModel, null);
		if (!tablesToIgnore.isEmpty()) {
			System.out.println("ignoring: " + PrintUtil.tableSetAsString(tablesToIgnore));
		}
		System.out.println();
		System.out.println("Shortest path from " + source.getName() + " to " + destination.getName() + ":");

		Map<Table, Table> successor = new HashMap<Table, Table>();
		Map<Table, Association> outgoingAssociation = new HashMap<Table, Association>();
		List<Table> agenda = new ArrayList<Table>();
		agenda.add(destination);

		while (!agenda.isEmpty()) {
			Table table = agenda.remove(0);
			for (Association association : incomingAssociations(table, undirected)) {
				if (!tablesToIgnore.contains(association.source)) {
					if (!successor.containsKey(association.source)) {
						successor.put(association.source, table);
						outgoingAssociation.put(association.source, association);
						agenda.add(association.source);
						if (association.source.equals(source)) {
							agenda.clear();
							break;
						}
					}
				}
			}
		}
		if (successor.containsKey(source)) {
			String joinedSelect = "Select * From " + source.getName();
			System.out.println("    " + source.getName());
			for (Table table = source; !table.equals(destination); table = successor.get(table)) {
				Association association = outgoingAssociation.get(table);
				System.out.println("    " + association);
				joinedSelect += " join "
						+ association.destination.getName()
						+ " on "
						+ (association.reversed ? SqlUtil.replaceAliases(association.getJoinCondition(), association.destination.getName(), association.source
								.getName()) : SqlUtil.replaceAliases(association.getJoinCondition(), association.source.getName(), association.destination
								.getName()));
			}
			System.out.println();
			System.out.println();
			System.out.println("SQL query:");
			System.out.println("    " + joinedSelect);
		} else {
			System.out.println("tables are not associated");
		}
	}

	/**
	 * Prints restricted data-model.
	 */
	private static void printDataModel(List<String> restrictionModels, boolean printClosures) throws Exception {
		DataModel dataModel = new DataModel();
		if (printClosures) {
			DataModel.printClosures = true;
		}
		for (String rm : restrictionModels.subList(1, restrictionModels.size())) {
			if (dataModel.getRestrictionModel() == null) {
				dataModel.setRestrictionModel(new RestrictionModel(dataModel));
			}
			dataModel.getRestrictionModel().addRestrictionDefinition(rm, null, new HashMap<String, String>());
		}

		BufferedReader in = new BufferedReader(new StringReader(dataModel.toString()));
		String line;
		while ((line = in.readLine()) != null) {
			System.out.println(line);
			CancellationHandler.checkForCancellation(null);
		}

		printCycles(dataModel);
		printComponents(dataModel);
	}

	/**
	 * Searches cycles in a data-model and prints out all tables involved in a
	 * cycle.
	 * 
	 * @param dataModel
	 *            the data-model
	 */
	private static void printCycles(DataModel dataModel) {
		Set<Table> independentTables;
		Set<Table> tables = new HashSet<Table>(dataModel.getTables());
		do {
			independentTables = dataModel.getIndependentTables(tables);
			tables.removeAll(independentTables);
		} while (!independentTables.isEmpty());
		if (tables.isEmpty()) {
			System.out.println("no cyclic dependencies" + asString(tables));
		} else {
			System.out.println("tables in dependent-cycle: " + asString(tables));
		}
	}

	/**
	 * Searches components in a data-model and prints out all components.
	 * 
	 * @param dataModel
	 *            the data-model
	 */
	private static void printComponents(DataModel dataModel) {
		List<Set<Table>> components = new ArrayList<Set<Table>>();
		Set<Table> tables = new HashSet<Table>(dataModel.getTables());
		while (!tables.isEmpty()) {
			Table table = tables.iterator().next();
			Set<Table> closure = table.closure(new HashSet<Table>(), false);
			components.add(closure);
			tables.removeAll(closure);
		}
		System.out.println(components.size() + " components: ");
		for (Set<Table> component : components) {
			System.out.println(PrintUtil.tableSetAsString(component));
		}
	}

	/**
	 * Collects all non-ignored associations with a given table as destination.
	 * 
	 * @param table
	 *            the table
	 * @return all non-ignored associations with table as destination
	 */
	private static Collection<Association> incomingAssociations(Table table, boolean undirected) {
		Collection<Association> result = new ArrayList<Association>();
		for (Association association : table.associations) {
			if (association.reversalAssociation.getJoinCondition() != null || (undirected && association.getJoinCondition() != null)) {
				result.add(association.reversalAssociation);
			}
		}
		return result;
	}

	// initialize log4j
	static {
		InputStream in = Jailer.class.getResourceAsStream("/net/sf/jailer/resource/log4j.properties");
		Properties p = new Properties();
		try {
			p.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(p);
		_log = Logger.getLogger(Jailer.class);
	}

}
