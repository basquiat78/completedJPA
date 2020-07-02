package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.Item;
import io.basquiat.model.OtherItem;

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
        	
        	Item bass = Item.builder().name("Fodera Emperor2 5")
								     .price(15000000)
								     .build();
        	
        	em.persist(bass);
        	
        	OtherItem foderaBass = OtherItem.builder().name("Fodera Emperor2 5")
        									     .price(15000000)
        									     .build();
        	
        	em.persist(foderaBass);
        	OtherItem fenderJBass = OtherItem.builder().name("Fender Jazz Bass")
				     .price(15000000)
				     .build();
        	
        	em.persist(fenderJBass);
        	OtherItem fenderPBass = OtherItem.builder().name("Fender Precision Bass")
				     .price(15000000)
				     .build();

        	em.persist(fenderPBass);
        	
        	
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
