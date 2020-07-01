package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
        	
        	Item foderaBass = Item.builder().name("Fodera Emperor2 5").price(15000000).build();
        	em.persist(foderaBass);
        	em.flush();
        	em.clear();
        	
        	int salePrice = 1000000;
        	
        	Item selectBass = em.find(Item.class, 1L);
        	System.out.println("Bass Price is " + selectBass.getPrice());
        	System.out.println("beforeUpdate price");
        	selectBass.setPrice(selectBass.getPrice() - salePrice);
        	System.out.println("update price");
        	em.flush();
        	em.clear();
        	
        	Item againSameSelectBass = em.find(Item.class, 1L);
        	System.out.println("Bass Price is " + againSameSelectBass.getPrice());
        	
        	
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
