package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.BadgeType;
import io.basquiat.model.Item;

/**
 * 
 * created by basquiat
 *
 */
public class jpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("basquiat");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
        	
        	Item item = Item.builder().name("Fodera").price(15000000).badge(BadgeType.NEW).build();
        	em.persist(item);
        	em.flush();
        	em.clear();
        	
        	Item selected = em.find(Item.class, 1L);
        	System.out.println(selected.toString());
        	
        	Thread.sleep(1000);
        	
        	selected.setBadge(BadgeType.BEST);
        	em.flush();
        	em.clear();
        	
        	Item selected1 = em.find(Item.class, 1L);
        	System.out.println(selected1.toString());
        	
        	
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
