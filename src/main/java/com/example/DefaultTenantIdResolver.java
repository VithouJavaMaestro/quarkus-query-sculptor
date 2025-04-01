package com.example;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.RequestScoped;

@PersistenceUnitExtension
@RequestScoped
public class DefaultTenantIdResolver implements TenantResolver {

    private final jakarta.ws.rs.core.HttpHeaders headers;

    public DefaultTenantIdResolver(jakarta.ws.rs.core.HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public String getDefaultTenantId() {
        return "default";
    }

    @Override
    public String resolveTenantId() {
        return headers.getHeaderString("x-tenant-id");
    }
}
