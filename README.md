# Persistence Context

일단 전 브랜치에서 봤던 코드를 다시 한번 살펴보자.

지루한 내용이 될것이다.

```

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

        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}


```

아마도 SpringBoot를 활용해 개발하거나 접한 분들이라면 저런 코드를 본적이 없을 것이다. 

try catch로 묶고 사용한 자원들을 close하는 코드가 낯설 수도 있다.

앞서 말했듯이 그 모든 것을 스프링이 대처해주기 때문인데 그럼에도 한번쯤은 짚고 넘어가야 하는 부분이다.

앞에서 DB와 관련된 링크를 통해 트랜잭션이란 것이 무엇인지 봤을 것이다.

트랜잭션이라는 것은 하나의 어떤 일렬의 작업의 묶음이라고 봐도 무방하다. 결국 트랜잭션을 가져와서 시작하고 커밋을 날리기 전까지의 어떤 작업들은 하나의 트랜잭션으로 묶이게 된다.

에러가 나면 롤백을 수행하기도 한다.

저 위의 코드는 과거 preparedStatement의 코드와 비슷한 면이 있다. 

커넥션을 맺고 preparedStatement를 통해 DB와 do something을 하고 사용한 리소스를 반환하기 위해서 close를 하는 코드를 본적이 있을지 모르겠지만...

```

try {
    Connection conn = null;
    PreparedStatement pstmt = null;
     
    String sql = "INSERT item (name, price) VALUES(?, ?)";
     
    conn = DriverManager.getConnection("jdbc:url, user, password);
    pstmt = conn.prepareStatement(sql);
     
    pstmt.setString(1, name);
    pstmt.setInt(2, price);
    pstmt.execute(sql);
    stmt.close();
    conn.close();
} catch(Exception e) {
    e.printStackTrace();
}

```

대충 이런 느낌인데 비슷하지 않은가?

어쨰든 jpa코드로 돌아가서 코드를 그림으로 표현을 해보자면 

![실행이미지](https://github.com/basquiat78/java-oop/blob/master/img/capture1.PNG)

어디서 많이 본 그림이지?

그럼 이제 Persistence Context, 즉 영속성 컨텍스트라는 것은 무엇인가이다.

아는 분들은 아실 수 있겠지만 일반적으로 myBatis같은 녀석을 쓰게 되면 DB에 무언가를 실행하는 순간 그것으로 끝이다.

특히 xml에 객체와 매핑을 하고 그것을 실제로 짜놓은 sql과 연계해서 무언가를 하는 것이 아니고 바로 실행을 해버린다는 것이다.

하지만 JPA의 경우에는 그 흐름이 다르다.

책에서는 이와 관련해서 '엔티티를 영구 저장하는 환경'이라는 표현을 쓴다.

또한 논리적인 개념으로 논리적인 공간을 의미하고 EntitiyManger를 통해서 이 공간에 접근한다고 표현한다.

일단 말이 어렵고 이것이 무엇인가는 우리같은 개발자는 코드로 직접 보는것이 최고이다.


```

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

        	Item item = Item.builder().name("Sandberg California II TT5 Masterpiece")
        							  .price(5400000).build();
        	
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}

```

위와 같이 코드를 짜고 실행하게 되면 어떤 일이 벌어질까?

아무일도 일어나지 않는다. 아! persistence.xml에 hibernate.hbm2ddl.auto옵션을 create로 해놨다면 스키마를 드롭하고 다시 생성하는 쿼리 정도만 볼 수 있다.

왜 그럴까? 생성한 객체는 @Entity를 붙여놨는데? 이거면 되는거 아니었엉?

하지만 사실 Item은 단지 객체만 생성된 것 뿐이다.

이것을 이해하기 위해서는 Entity의 life cycle을 알아야 한다.


1. 비영속 (new/transient)
영속성 컨텍스트와 전혀 관계가 없는 상태

2. 영속(managed)
영속성 컨텍스트에 저장된 상태를 의미한다. 즉, 엔티티가 영속성 컨텍스트에 의해 관리되어진다는 것이다.
그렇다고 DB에 곧바로 쿼리가 날라가지 않는다.
트랜잭션의 커밋 시점에 영속성 컨텍스트에 있는 정보들이 DB에 쿼리로 날라간다.

3. 준영속(detached)
영속성 컨텍스트에 저장되었다가 분리된 상태를 의미한다.

4. 삭제(removed)
삭제된 상태이다. 이 경우네는 DB에서도 날린다.



그러면 지금 위에 코드의 Item은 논리적인 영역인 영속성 컨텍스트와는 상관없는 비영속 상태라는 것이다.
이젠 저 객체를 영속 상태로 만들어 보자.

```

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

        	Item item = Item.builder().name("Sandberg California II TT5 Masterpiece")
        							  .price(5400000).build();
        	em.persist(item);
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}

```

em.perisist(item)이라는 코드 한 줄로 우리는 이제 item이라는 객체를 영속성 컨텍스트가 관리하는 영속 상태로 만들었다.

눈치 빠른 분들이나 이미 아는 분들이라면 tx.commit()이 실행되는 순간 인서트 쿼리가 날아갈 것이라는 것을 알 수 있다.

EntityTransaction 내부를 따라가다보면 AbstractSharedSessionContract를 만나게 되는데 결국 이녀석은 jdbc를 통해 commit를 날리게 된다.

하지만 영속성 상태로 만들었다 해서 바로 쿼리로 날아가서 디비에 데이터가 꽂히는 것은 아니다.

```
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
        	Item item = Item.builder().name("Fodera Emperor5 Elite")
        							  .price(14000000).build();
        	
        	System.out.println("영속성 상태로 만들면 바로 쿼리로 날아가는거 아니였엉??");
        	em.persist(item);
        	System.out.println("영속성 상태로 만들고 나면 날아가는 거였엉????");
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}


```

하지만 지금 내가 만들어 논 경우에는 우리가 예상하는 것과는 다른 결과를 보게 될 것이다.

```

영속성 상태로 만들면 바로 쿼리로 날아가는거 아니였엉??
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (name, price) 
        values
            (?, ?)
영속성 상태로 만들고 나면 날아가는 거였엉????


```

위에 언급했던 말대로라면 사실 밑의 결과를 예상했을 것이다.


```

영속성 상태로 만들면 바로 쿼리로 날아가는거 아니였엉??
영속성 상태로 만들고 나면 날아가는 거였엉????
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (name, price) 
        values
            (?, ?)

```

알고 있는 지식으로는 tx.commit()이 실행될 때 내부적으로 영속성 컨텍스트에 있는 정보를 flush()를 하게 되고 이때 쿼리가 날아간다. 

그리고 commit를 치게 되는데 지금 상황은 무엇인가??

의문이 들것이다. 이것은 사실 Item의 id의 기본 키 매핑 전략중 @GeneratedValue(strategy = GenerationType.IDENTITY)를 사용했기 때문이다.

이와 관련된 궁금증은 이후에 알아 볼것이다.


만일 사실 여부를 알고 싶다면

OtherItem.java

```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity(name = "basquiat_other_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class OtherItem {

	@Id
	private Long id;

	@Setter
	@Getter
	private String name;

	@Setter
	@Getter
	private Integer price;
	
	@Builder
	public OtherItem(Long id, String name, Integer price) {
		if(StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("item name must be not null"); 
	    }
		if(price == null || price < 0) {
			throw new IllegalArgumentException("price must be not under 0"); 
	    }
		
		this.id = id;
		this.price = price;
		this.name = name;
	}
	
}


```

```

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
        	//Item item = Item.builder().name("Fodera Emperor5 Elite")
        	//						  .price(14000000).build();
        	
        	OtherItem item = OtherItem.builder().id(1L)
        										.name("Fender Custom Shop 63 Precision")
        										.price(5400000)
        										.build();
        	
        	System.out.println("영속성 상태로 만들면 바로 쿼리로 날아가는거 아니였엉??");
        	em.persist(item);
        	System.out.println("영속성 상태로 만들고 나서 커밋을 만날 때 날아가는 거였엉????");
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}


```

이렇게 하면 원하는 결과를 얻을 수 있다.


em.persist(item)라는 코드외에도 영속 상태로 만드는 또 다른 케이스는 셀렉트를 했을 경우이다.

예를 들면 다음 코드를 살펴보자.

```
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
        	
        	Item item = em.find(Item.class, 1L);
        	System.out.println(item.toString());
        
         // 똑같은 녀석을 또 한번 검색을 한다.
        	Item item1 = em.find(Item.class, 1L); 
        	System.out.println(item1.toString());
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}

```

다음과 같이 코드를 실행하게 되면 이런 결과를 얻게 된다.

```
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=Fodera Emperor5 Elite, price=14000000]
Item [id=1, name=Fodera Emperor5 Elite, price=14000000]

```

셀렉트 쿼리는 단 한번만 날아갔다. 하지만 item1의 정보도 함께 뿌려주게 된다. 

myBatis나 다른 방식이라면 쿼리가 두번 날아갈것이다. 

하지만 JPA는 셀렉트한 정보를 영속성 컨텍스트에 캐시 (여기서는 1차 캐시라며 표현함)로 담아놓게 된다.

그리고 두 번째 같은 id값이 1로 검색했을 때 JPA는 쿼리를 바로 날리는 것이 아니라 영속성 컨텍스트의 1차 캐시에서 한번 조회를 해보고 없으면 그때 쿼리를 날리게 된다.

그럼 과연 그럴까?


TMI: 일단 jpa가 처음이신 분이라면 앞에서 데이터를 하나 집어넣고 persistence.xml의 hibernate.hbm2ddl.auto옵션을 none으로 두자. 그래야 데이터를 조회할 수 있기 때문이다.


```
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
        	
        	Item item = em.find(Item.class, 1L);
        	System.out.println(item.toString());
        
        	Item item1 = em.find(Item.class, 1L);
        	System.out.println(item1.toString());
        	
        	Item item2 = em.find(Item.class, 2L);
        	System.out.println(item2.toString());
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}


```

다음과 같이 코드를 실행하게 되면 

```
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=Fodera Emperor5 Elite, price=14000000]
Item [id=1, name=Fodera Emperor5 Elite, price=14000000]
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?

```

![실행이미지](https://github.com/basquiat78/java-oop/blob/master/img/capture2.PNG)

id값이 1인 녀석을 2번 조회하고 2인 녀석을 한번 조회하는 코드를 실행하게 되면 맨 처음 DB에서 조회하고 영속성 컨텍스트의 1차 캐시에 저장하고 출력하게 되고 그 다음에 한번 더 조회하면 영속성 컨텍스트의 1차 캐시을 우선적으로 조회한다. 앞서 저장된 아이디 1이 존재하기에 쿼리를 날리지 않고 캐시에서 가져와 출력하고 그 다음 코드는 아이디가 2인 녀석을 조회하는데 1차 캐시에 없기 때문에 쿼리를 날려서 가져오게 된다.

그렇다면 준영속이라는 것은 무엇일까?
설명에 의하면 '영속성 컨텍스트에 저장되었다가 분리된 상태를 의미한다'라고 말하고 있다.

결국 영속성 컨텍스트의 기능을 사용할 수 없다는 의미이다.

1. em.detach(entitiy)를 통해서 준영속상태로 만든다.

```
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
        	
        	Item item = em.find(Item.class, 1L);
        	System.out.println(item.toString());
        
        	// 첫번재 엔티티를 detach한다.
        	em.detach(item);
        	
        	Item item1 = em.find(Item.class, 1L);
        	System.out.println(item1.toString());
        	
        	Item item2 = em.find(Item.class, 2L);
        	System.out.println(item2.toString());
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}

```

지금까지 따라왔다면 저 위의 결과는 예상이 될것이다.

```
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=Fodera Emperor5 Elite, price=14000000]
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=Fodera Emperor5 Elite, price=14000000]
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
```

첫번째 아이디가 1인 녀석을 조회하면 jpa는 1차 캐시를 조회하고 없으면 DB를 조회해서 데이터를 가져오고 해당 엔티티를 1차 캐시에 세팅한다고 했다.
근데 그러고 나서 엔티티를 영속성 상태에서 em.detach를 통해 분리했기 때문에 2번째 똑같은 아이이 1을 조회했을 때는 영속성 컨텍스트의 1차 캐시에 조회를 해도 이미 분리되서 사라졌기때문에 다시 쿼리를 날리게 된다.

준영속 상태로 만드는 방법은 em.clear()를 통해서 전부 초기화를 하게 된다.

예를 들면 디비에 2개의 데이터가 있다고 가정을 해보자. 

```
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
        	
        	Item item1 = em.find(Item.class, 1L);
        	Item item2 = em.find(Item.class, 2L);
        	System.out.println(item1.toString());
        	System.out.println(item2.toString());
        	em.clear();
        	Item item3 = em.find(Item.class, 1L);
        	Item item4 = em.find(Item.class, 2L);
        	System.out.println(item3.toString());
        	System.out.println(item4.toString());
        	
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}

```
다음과 같은 결과를 예상할 수 있다.

```
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=Fodera Emperor5 Elite, price=14000000]
Item [id=2, name=Fender American Jazz Bass 5 Deluxe, price=2400000]
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=Fodera Emperor5 Elite, price=14000000]
Item [id=2, name=Fender American Jazz Bass 5 Deluxe, price=2400000]

```

아이디가 1과 2인 녀석을 조회하면 1차 캐시에 없기 때문에 쿼리를 날려서 조회를 하고 1차 캐시에 담을 것이다.
하지만 중간에 em.clear()를 통해서 영속성 컨텍스트를 초기화했기 때문에 쿼리를 다시 한번 날려서 조회하는 것을 알수 있다.

그리고 마지막 방식은 em.close()인 경우인데 하나의 트랜잭션이 종료되었기 때문에 영속성 컨텍스트가 초기화된다.

이것은 앞서 설명이 빠진 부분이긴 한테 영속성 컨텍스트는 EntityManagerFactory로부터 EntityManager를 가져오게 되면 그에 해당하는 Persistence Context가 매핑된다.

이말인 즉 em.close()가 되는 순간 EntitiyManager와 매핑된 영속성 컨텍스트는 사라진다는 의미이다.


삭세 상태는 말 그대로 삭제를 하는 것이다. 설명대로 DB에서도 삭제를 하는데 그럼 실제로 확인을 해봐야겠지?

```
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
        	
        	Item item = em.find(Item.class, 1L);
        	System.out.println(item);
        	em.remove(item);
        	
        	Item item1 = em.find(Item.class, 1L);
        	System.out.println(item1);
        	
        	
        	tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}

````

결과가 역시 예상된다.

```
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=Fodera Bass, price=15000000]
null
Hibernate: 
    /* delete io.basquiat.model.Item */ delete 
        from
            basquiat_item 
        where
            id=?
```

아이디 1을 조회한 후에 remove(item)을 날리고 다시 조회를 하면 null이 찍힌 것을 볼 수 있다.

그리고 commit이 되는 순간 delete쿼리가 날아간 것을 확인할 수 있다.

# Next 

너무 길어져서 영속성 컨텍스트의 이점을 설명해야하는데 다음 브랜치에 앞에서 설명을 할 예정이다.
