package org.jpwh.shared;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.jpa.HibernateQuery;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.Collections;
import java.util.Set;

public class CustomSQLQuery {

    final protected EntityManager em;
    final protected Query query;
    final protected SessionFactoryImplementor sessionFactory;
    final protected QueryTranslator queryTranslator;

    final protected String sql;

    protected Query nativeQuery;

    public CustomSQLQuery(EntityManager em, Query query) {
        this.em = em;
        this.query = query;

        HibernateEntityManagerFactory hibernateEntityManagerFactory =
                (HibernateEntityManagerFactory) em.getEntityManagerFactory();
        sessionFactory =
                (SessionFactoryImplementor) hibernateEntityManagerFactory.getSessionFactory();

        QueryTranslatorFactory astQueryTranslatorFactory = new ASTQueryTranslatorFactory();

        String jpql = query.unwrap(HibernateQuery.class).getHibernateQuery().getQueryString();

        queryTranslator =
                astQueryTranslatorFactory.createQueryTranslator(
                        jpql, jpql, Collections.EMPTY_MAP, sessionFactory, null
                );
        queryTranslator.compile(Collections.EMPTY_MAP, false);

        sql = getQueryTranslator().getSQLString();

        prepareNativeQuery();
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public Query getQuery() {
        return query;
    }

    public SessionFactoryImplementor getSessionFactory() {
        return sessionFactory;
    }

    public QueryTranslator getQueryTranslator() {
        return queryTranslator;
    }

    public String getSql() {
        return sql;
    }

    public Query getNativeQuery() {
        return nativeQuery;
    }

    protected void prepareNativeQuery() {
        nativeQuery = createNativeQuery(sql);
        copyParameters();
    }

    protected Query createNativeQuery (String sql){
        return getEntityManager().createNativeQuery(sql);
    }

    protected void copyParameters() {
        // Copy the parameters to the new native query (from named to positional)
        org.hibernate.Query hibernateNativeQuery = nativeQuery.unwrap(org.hibernate.Query.class);
        ParameterTranslations parameterTranslations = queryTranslator.getParameterTranslations();
        Set<Parameter<?>> originalParameters = query.getParameters();
        for (Parameter<?> originalParameter : originalParameters) {

            // Doesn't work with Hibernate < 4.0.3 implicit criteria parameter binding, they have no name!
            String name = originalParameter.getName();
            if (name == null)
                throw new IllegalArgumentException("Query must use named parameters");

            int[] positions = parameterTranslations.getNamedParameterSqlLocations(name);
            for (int p : positions) {
                // We need the Hibernate API to set the type
                // TODO This is restricted to basic types...
                hibernateNativeQuery.setParameter(p, query.getParameterValue(originalParameter),
                        sessionFactory.getTypeHelper().basic(originalParameter.getParameterType()));
            }
        }
    }

}
