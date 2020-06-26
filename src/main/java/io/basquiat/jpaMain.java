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

    	// persistence.xml에 등록된 <persistence-unit name="basquiat">의 name값으로
    	// EntityManagerFactory를 생성하자. 
    	// Factory라는 말이 붙은 것을 보면 Factory Pattern이 적용된것이다. TMI적이고~ 
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("basquiat");
        // EntityManger를 emf로부터 꺼내오자.
        EntityManager em = emf.createEntityManager();
        // 이제부터 트랜잭션 관리하기 위해서 EntityTransaction을 생성하자.
        EntityTransaction tx = em.getTransaction();
        // transaction!!!!
        tx.begin();
        try {

        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
