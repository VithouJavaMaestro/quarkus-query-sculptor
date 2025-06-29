package io.github.querysculptor;
/**
 * @author Chanthavithou THEN
 */
interface QueryMetadataBuilder<ENTITY> {
    QueryMetadata<ENTITY> buildQuery(QuerySculptorExecutor<ENTITY> executor);
}
