package org.jpwh.test.conversation;

import org.jpwh.env.JPATest;
import org.jpwh.model.conversation.Image;
import org.jpwh.model.conversation.Item;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import java.io.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ExtendedPC extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ConversationPU");
    }

    // TODO: Broken on MySQL https://hibernate.atlassian.net/browse/HHH-8402
    @Test(groups = {"H2", "ORACLE", "POSTGRESQL"})
    public void conversationCreateItem() throws Exception {
        Item item = new Item("Some Item");
        item.getImages().add(new Image("Foo", "foo.jpg", 800, 600));

        CreateEditItemService conversation = new CreateEditItemService();
        conversation.storeItem(item);
        conversation.commit();

        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                item = em.find(Item.class, item.getId());
                assertEquals(item.getName(), "Some Item");
                assertEquals(item.getImages().size(), 1);
            } finally {
                TM.rollback();
            }
        }
    }

    // TODO: Broken on MySQL https://hibernate.atlassian.net/browse/HHH-8402
    @Test(groups = {"H2", "ORACLE", "POSTGRESQL"})
    public void conversationEditItem() throws Exception {
        final TestData testData = storeItemImagesTestData();
        Long ITEM_ID = testData.getFirstId();

        // First event in conversation
        CreateEditItemService conversation = new CreateEditItemService();
        Item item = conversation.getItem(ITEM_ID);

        item.setName("New Name");
        // Persistence context is still open, load collection on demand!
        item.getImages().add(new Image("Foo", "foo.jpg", 800, 600));

        // Possibly more events, loading necessary data into conversation...

        // Final event in conversation
        conversation.commit(); // Flush and close persistence context

        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                item = em.find(Item.class, item.getId());
                assertEquals(item.getName(), "New Name");
                assertEquals(item.getImages().size(), 4);
            } finally {
                TM.rollback();
            }
        }
    }

    public class CreateEditItemService {

        final EntityManager em;

        /* 
           This <code>CreateEditItemService</code> class has one instance per conversation,
           associated with one extended persistence context. You create the
           <code>EntityManager</code> when the service is created, when the first event of
           the conversation has to be handled by the service. The client now re-uses this
           service until the conversation completes. No transaction is started at
           this time, only a persistence context is opened.
         */
        public CreateEditItemService() throws Exception {
            em = JPA.createEntityManager();
        }

        /* 
           This method handles the "load an <code>Item</code> for editing" event. With the
           <code>EntityManager</code> you retrieve the <code>Item</code> instance by
           identifier and return it to the client. Because the persistence context is still
           open when the method returns, the client can access the <code>Item#images</code>
           collection when needed. It will be initialized on-demand; you do not have to
           pre-load it here.
         */
        protected Item getItem(Long itemId) {
            return em.find(Item.class, itemId);
        }

        /* 
            This method handles the "store an <code>Item</code> after editing" event. The
            client will only call this to store a new, transient <code>Item</code>.
            The persistence context automatically detects modifications to any loaded and still
            persistent/managed <code>Item</code> instance when we flush the persistence context.
         */
        protected void storeItem(Item item) {
            em.persist(item);
        }

        /* 
           Part of the service contract is that the client must call this
           <code>commit()</code> method when the conversation should end successfully.
           This method will join the <code>EntityManager</code> with a system
           transaction, flush the persistence context, commit the transaction, and
           close the <code>EntityManager</code>.
         */
        protected void commit() throws Exception {
            try {
                UserTransaction tx = TM.getUserTransaction();
                tx.begin();
                em.joinTransaction();
                tx.commit();
            } finally {
                em.close();
            }
        }
    }

    /*
     TODO
     Some frameworks, for example EJB stateful beans, handle this for you. (Or at least, should?!)
     This was the ManagedEntityInterceptor in Seam 2, should we really talk about
     this or simply say: "Don't serialize/passivate anything with an extended PC in it"?
     What is the state of s.marlow's work in JBoss AS SFSBs?

     http://docs.jboss.org/seam/snapshot/en-US/html/ClusteringAndEJBPassivation.html

     https://hibernate.onjira.com/browse/HHH-6897

     Servlet 3.0, 7.7.2

     http://tomcat.apache.org/tomcat-7.0-doc/config/manager.html#Disable_Session_Persistence

     http://www.eclipse.org/jetty/documentation/current/using-persistent-sessions.html
    */

    // TODO: This test fails with an NPE in org.hibernate.engine.spi.EntityEntry.isUnequivocallyNonDirty if you
    // instrument entity classes... https://hibernate.onjira.com/browse/HHH-4451
    // @Test
    public void serializationIssues() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {

            long ITEM_ID;
            {
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                Item item = new Item("Some Item");
                em.persist(item);
                tx.commit();
                em.close();
                ITEM_ID = item.getId();
            }

            // If we load an entity instance into the persistence context...
            EntityManager em = JPA.createEntityManager();
            Item a = em.find(Item.class, ITEM_ID);

            // ...and the HttpSession containing the EntityManager is serialized...
            byte[] serializedEM = serialize(em);
            // Hibernate's EntityManagerImpl is java.io.Serializable but JPA spec is not!

            // ... then on joining of the persistence context with a new transaction...
            tx.begin();
            em = (EntityManager) deserialize(serializedEM);
            em.joinTransaction();

            // ...the Item 'a' is no longer "in" the persistence context ...
            assertFalse(em.contains(a));
            // ... because our reference 'a' is outdated.
            Item b = em.find(Item.class, a.getId()); // No SELECT, found by primary key in PC!
            assertFalse(a == b); // We lost identity due to serialization!

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    /* ################################################################################### */

    public TestData storeItemImagesTestData() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();
        Long[] ids = new Long[1];
        Item item = new Item();
        item.setName("Some Item");
        em.persist(item);
        ids[0] = item.getId();
        for (int i = 1; i <= 3; i++) {
            item.getImages().add(
                new Image("Image " + i, "image" + i + ".jpg", 640, 480));
        }
        tx.commit();
        em.close();
        return new TestData(ids);
    }


    protected byte[] serialize(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            return bos.toByteArray();
        } finally {
            if (out != null)
                out.close();
            bos.close();
        }
    }

    protected Object deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return in.readObject();
        } finally {
            bis.close();
            if (in != null)
                in.close();
        }
    }

}
