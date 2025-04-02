package com.example;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.RequestScoped;

@PersistenceUnitExtension
@RequestScoped
public class DefaultTenantIdResolver implements TenantResolver {



    @Override
    public String getDefaultTenantId() {
        return "default";
    }

    @Override
    public String resolveTenantId() {
     return "pps";
    }
}
