package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
        	
        	// 1. 내가 들어가고 싶은 팀이 무엇인지 살펴본다.
			/*
			 * Club tottenhamFootballClub =
			 * Club.builder().name("Tottenham Hotspur Football Club") .ranking(9) .build();
			 * em.persist(tottenhamFootballClub);
			 * 
			 * Locker sonsLocker = Locker.builder().name("손흥민의 락커") .position("입구에서 4번째 위치")
			 * .build(); em.persist(sonsLocker);
			 * 
			 * // 2. 손흥민이 토트넘으로 들어간다. Player son = Player.builder().name("손흥민") .age(27)
			 * .position("Striker") .footballClub(tottenhamFootballClub) .locker(sonsLocker)
			 * .build(); em.persist(son); System.out.println(son.toString());
			 * System.out.println(sonsLocker.getPlayer());
			 * System.out.println(tottenhamFootballClub.getPlayers());
			 */
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
