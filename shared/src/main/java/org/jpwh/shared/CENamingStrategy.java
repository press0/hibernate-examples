package org.jpwh.shared;

import org.hibernate.cfg.EJB3NamingStrategy;

/**
 * Prefixes all SQL table names with "CE_", for CaveatEmptor.
 */
public class CENamingStrategy extends EJB3NamingStrategy {

    @Override
    public String tableName(String tableName) {
        return "CE_" + tableName;
    }

    @Override
    public String classToTableName(String className) {
        return tableName(super.classToTableName(className));
    }

}
