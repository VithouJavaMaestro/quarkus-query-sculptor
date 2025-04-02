package com.example;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.query.NamedHqlQueryDefinition;
import org.hibernate.boot.query.NamedNativeQueryDefinition;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.schema.Action;
import org.hibernate.tool.schema.SourceType;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.internal.ExceptionHandlerCollectingImpl;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToWriter;
import org.hibernate.tool.schema.spi.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Supplier;

public class HibernateOrmController {
    private static final HibernateOrmController INSTANCE = new HibernateOrmController();

    public static HibernateOrmController get() {
        return INSTANCE;
    }

    private HibernateOrmInfo info = new HibernateOrmInfo();

    private HibernateOrmController() {
    }

    public HibernateOrmInfo getInfo() {
        return info;
    }

    void pushPersistenceUnit(String persistenceUnitName,
                             Metadata metadata, ServiceRegistry serviceRegistry, String importFile) {
        List<HibernateOrmInfo.Entity> managedEntities = new ArrayList<>();
        for (PersistentClass entityBinding : metadata.getEntityBindings()) {
            managedEntities
                    .add(new HibernateOrmInfo.Entity(entityBinding.getClassName(), entityBinding.getTable().getName()));
        }

        List<HibernateOrmInfo.Query> namedQueries = new ArrayList<>();
        {
            List<NamedHqlQueryDefinition> namedQueriesHqlDefs = new ArrayList<>();
            metadata.visitNamedHqlQueryDefinitions(namedQueriesHqlDefs::add);
            for (NamedHqlQueryDefinition queryDefinition : namedQueriesHqlDefs) {
                namedQueries.add(new HibernateOrmInfo.Query(queryDefinition));
            }
        }

        List<HibernateOrmInfo.Query> namedNativeQueries = new ArrayList<>();
        {
            List<NamedNativeQueryDefinition> namedNativeQueriesNativeDefs = new ArrayList<>();
            metadata.visitNamedNativeQueryDefinitions(namedNativeQueriesNativeDefs::add);
            for (NamedNativeQueryDefinition staticQueryDefinition : namedNativeQueriesNativeDefs) {
                namedNativeQueries.add(new HibernateOrmInfo.Query(staticQueryDefinition));
            }
        }

        HibernateOrmController.DDLSupplier createDDLSupplier = new HibernateOrmController.DDLSupplier(Action.CREATE, metadata, serviceRegistry, importFile);
        HibernateOrmController.DDLSupplier dropDDLSupplier = new HibernateOrmController.DDLSupplier(Action.DROP, metadata, serviceRegistry, importFile);
        HibernateOrmController.DDLSupplier updateDDLSupplier = new HibernateOrmController.DDLSupplier(Action.UPDATE, metadata, serviceRegistry, importFile);

        info.add(new HibernateOrmInfo.PersistenceUnit(persistenceUnitName, managedEntities,
                namedQueries, namedNativeQueries, createDDLSupplier, dropDDLSupplier, updateDDLSupplier));
    }

    class DDLSupplier implements Supplier<String> {

        private final Action action;
        private final Metadata metadata;
        private final ServiceRegistry serviceRegistry;
        private final String importFile;

        DDLSupplier(Action action, Metadata metadata, ServiceRegistry serviceRegistry, String importFile) {
            this.action = action;
            this.metadata = metadata;
            this.serviceRegistry = serviceRegistry;
            this.importFile = importFile;
        }

        @Override
        public String get() {
            return generateDDL(action, metadata, serviceRegistry, importFile);
        }
    }

    void clearData() {
        info = new HibernateOrmInfo();
    }

    private static String generateDDL(Action action, Metadata metadata, ServiceRegistry ssr,
                                      String importFiles) {

        final HibernateSchemaManagementTool tool = (HibernateSchemaManagementTool) ssr.getService(SchemaManagementTool.class);
        Map<String, Object> config = new HashMap<>(ssr.getService(ConfigurationService.class).getSettings());
        config.put(AvailableSettings.HBM2DDL_DELIMITER, ";");
        config.put(AvailableSettings.FORMAT_SQL, true);
        config.put(AvailableSettings.HBM2DDL_IMPORT_FILES, importFiles);
        ExceptionHandlerCollectingImpl exceptionHandler = new ExceptionHandlerCollectingImpl();
        try {
            final ExecutionOptions executionOptions = SchemaManagementToolCoordinator.buildExecutionOptions(
                    config,
                    exceptionHandler);
            StringWriter writer = new StringWriter();
            final SourceDescriptor source = new SourceDescriptor() {
                @Override
                public SourceType getSourceType() {
                    return SourceType.METADATA;
                }

                @Override
                public ScriptSourceInput getScriptSourceInput() {
                    return null;
                }
            };
            final TargetDescriptor target = new TargetDescriptor() {
                @Override
                public EnumSet<TargetType> getTargetTypes() {
                    return EnumSet.of(TargetType.SCRIPT);
                }

                @Override
                public ScriptTargetOutput getScriptTargetOutput() {
                    return new ScriptTargetOutputToWriter(writer) {
                        @Override
                        public void accept(String command) {
                            super.accept(command);
                        }
                    };
                }
            };
            if (action == Action.DROP) {
                SchemaDropper schemaDropper = tool.getSchemaDropper(executionOptions.getConfigurationValues());
                schemaDropper.doDrop(metadata, executionOptions, ContributableMatcher.ALL, source, target);
            } else if (action == Action.CREATE) {
                SchemaCreator schemaCreator = tool.getSchemaCreator(executionOptions.getConfigurationValues());
                schemaCreator.doCreation(metadata, executionOptions, ContributableMatcher.ALL, source, target);
            } else if (action == Action.UPDATE) {
                SchemaMigrator schemaMigrator = tool.getSchemaMigrator(executionOptions.getConfigurationValues());
                schemaMigrator.doMigration(metadata, executionOptions, ContributableMatcher.ALL, target);
            }
            return writer.toString();
        } catch (RuntimeException e) {
            //TODO unroll the exceptionHandler ?
            StringWriter stackTraceWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTraceWriter));
            return "Could not generate DDL: \n" + stackTraceWriter.toString();
        }
    }
}
