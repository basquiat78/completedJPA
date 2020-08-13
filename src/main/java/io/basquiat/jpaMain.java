package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import io.basquiat.jpql.Member;
import io.basquiat.jpql.Team;

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
        	Team myTeam = Team.builder().teamName("Default Team").build();
        	em.persist(myTeam);
        	
        	// address는 그냥 비우자.
        	Member member = Member.builder().id("basquiat")
        									.name("Jean-Michel Basquiat")
        									.age(28)
        									.team(myTeam)
        									.build();
        	em.persist(member);
        	em.flush();
        	em.clear();
        	
        	TypedQuery<Team> selected = em.createQuery("SELECT m.team FROM Member m", Team.class);
        	Team selectedTeam = selected.getSingleResult();
        	selectedTeam.setTeamName("Basquiat Team");
        	System.out.println(selectedTeam);
			
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
