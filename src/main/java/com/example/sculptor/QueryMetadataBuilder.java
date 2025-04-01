package com.example.sculptor;
/**
 * @author Chanthavithou THEN
 */
public interface QueryMetadataBuilder<ENTITY> {
    QueryMetadata<ENTITY> buildQuery(QuerySculptorExecutor<ENTITY> executor);
}
