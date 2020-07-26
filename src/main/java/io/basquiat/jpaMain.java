package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.football.Club;
import io.basquiat.model.football.Locker;
import io.basquiat.model.football.Player;

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
        	
        	Locker sonsLocker = Locker.builder().name("손흥민의 락커")
												.position("입구에서 4번째 위치")
												.build();

        	Player son = Player.builder().name("손흥민")
										 .age(27)
										 .position("Striker")
										 .locker(sonsLocker)
										 .build();
        	
        	Locker hugoLocker = Locker.builder().name("위고 요리스의 락커")
												.position("입구에서 1번째 위치")
												.build();
					        	
			Player hugo = Player.builder().name("Hugo Hadrien Dominique Lloris")
										 .age(33)
										 .position("Goal Keeper")
										 .locker(hugoLocker)
										 .build();
			
			Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
													   .ranking(9)
													   .build();
        	tottenhamFootballClub.scoutPlayer(hugo);
        	tottenhamFootballClub.scoutPlayer(son);
        	em.persist(hugo);
        	em.persist(son);
        	em.persist(hugoLocker);
        	em.persist(sonsLocker);
			em.persist(tottenhamFootballClub);
        	em.flush();
        	em.clear();
        	
        	Club selected = em.find(Club.class, 1L);
        	System.out.println("이적했으니 클럽 선수 명단에서 지운다.");
        	selected.getPlayers().remove(0); // 리스트에서 첫 번째 인덱스를 지운다.
        	tx.commit();
        } catch(Exception e) {
        	e.printStackTrace();
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
