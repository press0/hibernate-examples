package org.jpwh.env;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.jpa.HibernateQuery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

/**
 * Starts and stops the JPA environment before/after a test class.
 * <p>
 * Create a subclass to write unit tests. Access the <code>EntityManagerFactory</code>
 * with {@link JPATest#JPA} and create <code>EntityManager</code> instances.
 * </p>
 * <p>
 * Drops and creates the SQL database schema of the persistence unit before and after
 * every test method. This means your database will be cleaned for every test method.
 * </p>
 * <p>
 * Override the {@link #configurePersistenceUnit} method to provide a custom
 * persistence unit name or additional <code>hbm.xml</code> file names to load for
 * your test class.
 * </p>
 * <p>
 * Override the {@link #afterJPABootstrap()} method to execute operations before the
 * test method but after the <code>EntityManagerFactory</code> is ready. At this point
 * you can create an <code>EntityManager</code> or <code>JPA.doJDBCWork()</code>. If
 * cleanup is needed, override the {@link #beforeJPAClose()} method.
 * </p>
 */
public class JPATest extends TransactionManagerTest {

    public String persistenceUnitName;
    public String[] hbmResources;
    public JPASetup JPA;

    @BeforeClass
    public void beforeClass() throws Exception {
        configurePersistenceUnit();
    }

    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit(null);
    }

    public void configurePersistenceUnit(String persistenceUnitName,
                                         String... hbmResources) throws Exception {
        this.persistenceUnitName = persistenceUnitName;
        this.hbmResources = hbmResources;
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        JPA = new JPASetup(TM.databaseProduct, persistenceUnitName, hbmResources);
        if ("true".equals(System.getProperty("keepSchema"))) {
            // If we keep the schema after the last method, we need
            // to drop it before the next method runs.
            JPA.dropSchema();
        }
        JPA.createSchema();
        afterJPABootstrap();
    }

    public void afterJPABootstrap() throws Exception {
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() throws Exception {
        if (JPA != null) {
            beforeJPAClose();
            if (!"true".equals(System.getProperty("keepSchema"))) {
                JPA.dropSchema();
            }
            JPA.getEntityManagerFactory().close();
        }
    }

    public void beforeJPAClose() throws Exception {

    }

    protected long copy(Reader input, Writer output) throws IOException {
        // hard-code buffer size for now.
        char[] buffer = new char[4096];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    protected String getTextResourceAsString(String resource) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + resource);
        }
        StringWriter sw = new StringWriter();
        copy(new InputStreamReader(is), sw);
        return sw.toString();
    }

    protected Throwable unwrapRootCause(Throwable throwable) {
        return unwrapCauseOfType(throwable, null);
    }

    protected Throwable unwrapCauseOfType(Throwable throwable, Class<? extends Throwable> type) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (type != null && type.isAssignableFrom(current.getClass()))
                return current;
            throwable = current;
        }
        return throwable;
    }

    public static Long getResultCount(final EntityManager entityManager,
                                      final Query query) {
        final String hqlQuery = query.unwrap(HibernateQuery.class).getHibernateQuery().getQueryString();
        final ASTQueryTranslatorFactory astQueryTranslatorFactory = new ASTQueryTranslatorFactory();
        final HibernateEntityManagerFactory hibernateEntityManagerFactory = (HibernateEntityManagerFactory) entityManager.getEntityManagerFactory();
        final SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) hibernateEntityManagerFactory.getSessionFactory();
        final QueryTranslator queryTranslator = astQueryTranslatorFactory.createQueryTranslator(hqlQuery, hqlQuery, Collections.EMPTY_MAP, sessionFactory, null);
        queryTranslator.compile(Collections.EMPTY_MAP, false);

        final String queryString =
                "select count(*) from (" +
                        queryTranslator.getSQLString()
                        + ")";

        final org.hibernate.Query countQuery = entityManager.createNativeQuery(queryString).unwrap(org.hibernate.Query.class);
        final ParameterTranslations parameterTranslations = queryTranslator.getParameterTranslations();

        final Set<Parameter<?>> parameters = query.getParameters();
        for (final Parameter<?> parameter : parameters) {
            final String name = parameter.getName(); // this works only on hibernate 4.1 .3.Final !!!
            final int[] positions =
                    parameterTranslations.getNamedParameterSqlLocations(name);
            for (final int p : positions) {
                countQuery.setParameter(p,
                        query.getParameterValue(parameter),

                        sessionFactory.getTypeHelper().basic(parameter.getParameterType()));
            }
        }

        final Object result = countQuery.uniqueResult();
        if (result instanceof BigDecimal) {
            return ((BigDecimal) result).longValue();
        } else if (result instanceof BigInteger) {
            return ((BigInteger) result).longValue();
        } else {
            throw new IllegalArgumentException("JDBC driver returned unsupported resultType from count.");
        }

    }
}
