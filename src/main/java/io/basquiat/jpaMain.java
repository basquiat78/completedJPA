package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.mapsuperclazz.BassGuitar;
import io.basquiat.model.mapsuperclazz.Necklace;

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
        	
        	Necklace necklace = Necklace.builder().material("14K Gold")
        										  .lineMaterial("14K Gold")
        										  .color("Gold")
        										  .shape("Stardust")
        										  .customer("아리")
        										  .luthiers("STONEHENGE")
        										  .build();
        	
        	em.persist(necklace);
        	em.flush();
        	em.clear();
        	
        	Necklace completedNecklace = em.find(Necklace.class, 1L);
        	System.out.println(completedNecklace.toString());
        	completedNecklace.completedAt();
        	em.flush();
        	em.clear();
        	
        	Necklace deliveryNecklace = em.find(Necklace.class, 1L);
        	System.out.println(deliveryNecklace.toString());
        	deliveryNecklace.deliveryAt();
        	em.flush();
        	em.clear();
        	
        	Necklace selected = em.find(Necklace.class, 1L);
        	System.out.println(selected.toString());
        	
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
