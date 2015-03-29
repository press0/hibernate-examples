package org.jpwh.model.complexschemas;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.mapping.AbstractAuxiliaryDatabaseObject;

public class CustomSchema
    extends AbstractAuxiliaryDatabaseObject {

    public CustomSchema() {
        addDialectScope("org.hibernate.dialect.Oracle9Dialect");
    }

    public String sqlCreateString(Dialect dialect,
                                  Mapping mapping,
                                  String defaultCatalog,
                                  String defaultSchema) {
        return "[CREATE statement]";
    }

    public String sqlDropString(Dialect dialect,
                                String defaultCatalog,
                                String defaultSchema) {
        return "[DROP statement]";
    }
}