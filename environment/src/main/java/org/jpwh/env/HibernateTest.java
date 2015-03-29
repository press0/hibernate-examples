package org.jpwh.env;

import org.hibernate.Hibernate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Starts and stops Hibernate before/after every test class.
 * <p>
 * Create a subclass to write unit tests. Access the <code>SessionFactory</code>
 * with {@link HibernateTest#HIBERNATE} and create <code>Session</code> instances.
 * </p>
 * <p>
 * Drops and creates the SQL database schema of the persistence unit before and after
 * every test method. This means your database will be cleaned for every test method.
 * </p>
 */
public class HibernateTest extends TransactionManagerTest {

    public HibernateSetup HIBERNATE;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        HIBERNATE = new HibernateSetup();
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        HIBERNATE.getSessionFactory().close();
    }

}
