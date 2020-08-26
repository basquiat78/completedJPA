package io.basquiat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import io.basquiat.jpql.Member;
import io.basquiat.jpql.MemberDTO;
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
        	
        	Member member = Member.builder().id("user_id")
        			.name("basquiat")
        			.age(33)
        			.team(myTeam)
        			.build();
        	em.persist(member);
        	
        	em.flush();
        	em.clear();
        	
        	TypedQuery<Member> selected = em.createQuery("SELECT sub FROM (SELECT sm FROM Member sm) AS sub", Member.class);
        	List<Member> members = selected.getResultList();
        	System.out.println(members);
        	
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
