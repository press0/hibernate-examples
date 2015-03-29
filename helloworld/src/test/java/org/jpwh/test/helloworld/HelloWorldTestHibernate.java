package org.jpwh.test.helloworld;

import org.hibernate.Session;
import org.jpwh.env.HibernateTest;
import org.jpwh.helloworld.Message;
import org.testng.annotations.Test;

import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class HelloWorldTestHibernate extends HibernateTest {

    @Test
    public void storeLoadMessage() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {

            tx.begin();
            Session session = HIBERNATE.getCurrentSession(); // Scoped to this transaction

            Message message = new Message();
            message.setText("Hello...");

            session.persist(message);

            message.setText("Hello World!"); // Managed, in session scope

            tx.commit();  // "Current" session closed automatically on commit!

            message.setText("Goodbye!"); // No effect, outside of session scope

            tx.begin();
            session = HIBERNATE.getCurrentSession();

            List<Message> messages =
                session.createCriteria(Message.class).list();

            tx.commit();

            assertEquals(messages.size(), 1);
            assertEquals(messages.get(0).getText(), "Hello World!");

        } finally {
            TM.rollback();
        }
    }

    @Test
    public void storeLoadMessage2() throws Exception {
        storeLoadMessage();
    }
}
