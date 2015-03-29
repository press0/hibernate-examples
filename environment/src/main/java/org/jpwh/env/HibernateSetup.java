package org.jpwh.env;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.transaction.internal.jta.JtaTransactionFactory;
import org.hibernate.engine.transaction.jta.platform.internal.BitronixJtaPlatform;

/**
 * Bootstrap of a native Hibernate SessionFactory.
 * <p>
 * Hibernate will use the bundled <code>ImprovedH2Dialect</code> unless the 'derby' system
 * property is set to 'true', then the built-in <code>DerbyDialect</code> will be used.
 * </p>
 */
public class HibernateSetup {

    protected final Configuration configuration;

    protected final SessionFactory sessionFactory;

    public HibernateSetup() throws Exception {
        configuration = new Configuration().configure(); // Load hibernate.cfg.xml

        configuration
            .setProperty(
                "hibernate.format_sql",
                "true"
            )
            .setProperty(
                "hibernate.use_sql_comments",
                "true"
            )
            .setProperty(
                "hibernate.transaction.factory_class",
                JtaTransactionFactory.class.getName()
            )
            .setProperty(
                "hibernate.transaction.jta.platform",
                BitronixJtaPlatform.class.getName()
            )
            .setProperty(
                "hibernate.dialect",
                "true".equals(System.getProperty("derby"))
                    ? org.hibernate.dialect.DerbyTenSevenDialect.class.getName()
                    : org.jpwh.shared.ImprovedH2Dialect.class.getName()
            )
            .setProperty(
                "hibernate.hbm2ddl.auto",
                "create-drop" // Drops when SessionFactory is closed
            );

        sessionFactory = configuration.buildSessionFactory();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
