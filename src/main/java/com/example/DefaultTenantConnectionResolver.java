package com.example;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.narayana.NarayanaTransactionIntegration;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.postgresql.Driver;

import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
@PersistenceUnitExtension
public class DefaultTenantConnectionResolver implements TenantConnectionResolver {

    private final jakarta.transaction.TransactionManager transactionManager;
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private final AgroalDataSource defaultDataSource;


    public DefaultTenantConnectionResolver(
            TransactionManager transactionManager,
            TransactionSynchronizationRegistry transactionSynchronizationRegistry, AgroalDataSource defaultDataSource) {
        this.transactionManager = transactionManager;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    public ConnectionProvider resolve(String tenantId) {
        if ("allweb".equals(tenantId) || "pps".equals(tenantId)) {
            return new QuarkusConnectionProvider(createDatasource(tenantId));
        }

        return new QuarkusConnectionProvider(defaultDataSource);
    }

    private AgroalDataSource createDatasource(String tenantId) {
        try {
            final var txIntegration = new NarayanaTransactionIntegration(
                    transactionManager, transactionSynchronizationRegistry, null, false, null);
            // Fetch JDBC URL, username, password & other values from a per-tenant dynamic source
            final var dataSourceConfig = new AgroalDataSourceConfigurationSupplier()
                    .connectionPoolConfiguration(pc -> pc.initialSize(2)
                            .maxSize(10)
                            .minSize(2)
                            .maxLifetime(Duration.of(5, ChronoUnit.MINUTES))
                            .acquisitionTimeout(Duration.of(30, ChronoUnit.SECONDS))
                            .transactionIntegration(txIntegration)
                            .connectionFactoryConfiguration(
                                    cf -> cf
                                            .connectionProviderClass(Driver.class)
                                            .jdbcUrl("jdbc:postgresql://localhost:5432/postgres?currentSchema=" + tenantId)
                                            .credential(new NamePrincipal("postgres"))
                                            .credential(new SimplePassword("root"))));
            return AgroalDataSource.from(dataSourceConfig.get());
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Failed to create a new data source based on the existing datasource configuration", ex);
        }
    }
}
