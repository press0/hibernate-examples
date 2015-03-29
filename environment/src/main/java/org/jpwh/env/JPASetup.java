package org.jpwh.env;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.transaction.jta.platform.internal.BitronixJtaPlatform;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.jdbc.Work;
import org.hibernate.jpa.HibernateEntityManagerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates an EntityManagerFactory.
 * <p>
 * Configuration of the persistence units is taken from <code>META-INF/persistence.xml</code>
 * and other sources. Additional <code>hbm.xml</code> file names can be given to the
 * constructor.
 * </p>
 */
public class JPASetup {

    protected final String persistenceUnitName;
    protected final Map<String, String> properties = new HashMap<String, String>();
    protected final EntityManagerFactory entityManagerFactory;

    public JPASetup(DatabaseProduct databaseProduct,
                    String persistenceUnitName,
                    String... hbmResources) throws Exception {

        this.persistenceUnitName = persistenceUnitName;

        // No automatic scanning by Hibernate, all persistence units list explicit classes/packages
        properties.put(
            "hibernate.archive.autodetection",
            "none"
        );

        // Really the only way how we can get hbm.xml files into an explicit persistence unit
        properties.put(
            "hibernate.hbmxml.files",
            StringHelper.join(",", hbmResources != null ? hbmResources : new String[0])
        );

        // We don't want to repeat these settings for all units in persistence.xml, so
        // they are set here programmatically
        properties.put(
            "hibernate.format_sql",
            "true"
        );
        properties.put(
            "hibernate.use_sql_comments",
            "true"
        );

        // Always use the Bitronix JTA transaction manager
        properties.put(
            "hibernate.transaction.jta.platform",
            BitronixJtaPlatform.class.getName()
        );

        // Select database SQL dialect
        properties.put(
            "hibernate.dialect",
            databaseProduct.hibernateDialect
        );

        entityManagerFactory =
            Persistence.createEntityManagerFactory(getPersistenceUnitName(), properties);
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public void createSchema() {
        generateSchema("create");
    }

    public void dropSchema() {
        generateSchema("drop");
    }

    public void generateSchema(String action) {
        // Take exiting EMF properties, override the schema generation setting on a copy
        Map<String, String> createSchemaProperties = new HashMap<String, String>(properties);
        createSchemaProperties.put(
            "javax.persistence.schema-generation.database.action",
            action
        );
        Persistence.generateSchema(getPersistenceUnitName(), createSchemaProperties);
    }

    // Native API fallback

    public void doJDBCWork(Work work) {
        Session session = createEntityManager().unwrap(Session.class);
        try {
            session.doWork(work);
        } finally {
            session.close();
        }
    }

    /* TODO: Don't really know how to get a Configuration before the EMF is build
    public Configuration buildHibernateConfiguration() {
        List<ParsedPersistenceXmlDescriptor> units =
            PersistenceXmlParser.locatePersistenceUnits(properties);

        ParsedPersistenceXmlDescriptor persistenceUnit = null;
        for (ParsedPersistenceXmlDescriptor u : units) {
            if (u.getName().equals(persistenceUnitName)) {
                persistenceUnit = u;
                break;
            }
        }
        if (persistenceUnit == null)
            throw new RuntimeException("Persistence unit not found: " + persistenceUnitName);

        EntityManagerFactoryBuilderImpl emfBuilder =
            (EntityManagerFactoryBuilderImpl) Bootstrap.getEntityManagerFactoryBuilder(
                persistenceUnit, properties
            );

        return emfBuilder.buildHibernateConfiguration(emfBuilder.buildServiceRegistry());
    }
    */

}
