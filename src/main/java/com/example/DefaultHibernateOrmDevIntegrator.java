package com.example;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import static org.hibernate.cfg.SchemaToolingSettings.HBM2DDL_IMPORT_FILES;

public class DefaultHibernateOrmDevIntegrator implements Integrator {
    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactoryImplementor,
                          SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        HibernateOrmController.get().pushPersistenceUnit(
                (String) sessionFactoryImplementor.getProperties()
                        .get(org.hibernate.cfg.AvailableSettings.PERSISTENCE_UNIT_NAME),
                metadata, sessionFactoryServiceRegistry,
                (String) sessionFactoryImplementor.getProperties().get(HBM2DDL_IMPORT_FILES));
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor,
                             SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        HibernateOrmController.get().clearData();
    }
}
