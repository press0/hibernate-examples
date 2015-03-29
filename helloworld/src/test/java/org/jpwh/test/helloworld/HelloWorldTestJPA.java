package org.jpwh.test.helloworld;

import org.jpwh.env.JPATest;
import org.jpwh.helloworld.Message;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class HelloWorldTestJPA extends JPATest {

    @Test
    public void storeLoadMessage() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {

            tx.begin(); // JTA UserTransaction
            EntityManager em = JPA.createEntityManager(); // JPA utility class

            Message message = new Message();
            message.setText("Hello World!");

            em.persist(message);

            tx.commit();
            em.close(); // You create it, you close it!

            tx.begin();
            em = JPA.createEntityManager();

            List<Message> messages =
                em.createQuery("select m from Message m").getResultList();

            tx.commit();
            em.close();

            assertEquals(messages.size(), 1);
            assertEquals(messages.get(0).getText(), "Hello World!");

        } finally {
            TM.rollback();
        }
    }

    @Test
    public void storeLoadMessage2() throws Exception {
        // This is testing if our infrastructure works, after/before method cleanup etc.
        storeLoadMessage();
    }

}
