package io.github.querysculptor;

import org.hibernate.query.Query;

public interface SelectionQueryCallback<ENTITY> {
    void execute(Query<ENTITY> query);
}
