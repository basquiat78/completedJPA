package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.embedded.Address;
import io.basquiat.model.embedded.DeliveryAddress;
import io.basquiat.model.embedded.Member;

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
        	
        	// 1. embedded address 생성
        	Address address = Address.builder().city("서울시")
        									   .street("논현동")
        									   .zipcode("50000")
        									   .build();
        	
        	// 2. delivery address
        	DeliveryAddress myHomeAddress = DeliveryAddress.builder().city("서울시")
																   	 .street("논현동 우리집")
																   	 .zipcode("50000")
																   	 .build();
        	
        	DeliveryAddress myOfficialAddress = DeliveryAddress.builder().city("서울시")
																	   	 .street("논현동 회사")
																	   	 .zipcode("50000")
																	   	 .build();
        	
        	// 신규가입자 
        	Member newMember = Member.builder().id("basquiat")
    										   .password("mypassword")
    										   .name("Jean-Michel-Basquiat")
    										   .birth("1960-12-22")
    										   .phone("010-0000-00000")
    										   .address(address)
    										   .build();
        	
        	newMember.getDeliveryAddress().add(myHomeAddress);
        	newMember.getDeliveryAddress().add(myOfficialAddress);
        	
        	newMember.getFavoriteCoffeeShop().add("별다방");
        	newMember.getFavoriteCoffeeShop().add("커피 자판기 일명 벽다방");
        	newMember.getFavoriteCoffeeShop().add("커피콩");
        	
        	em.persist(newMember);
        	
        	em.flush();
        	em.clear();
        	
        	Member selectedMember = em.find(Member.class, newMember.getId());
        	DeliveryAddress updateAddress = selectedMember.getDeliveryAddress().get(0);
        	updateAddress.setStreet("논현동 우리집 !!!!!!");
        	selectedMember.getDeliveryAddress().remove(1);
        	
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
