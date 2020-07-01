# Persistence Context

앞서 브랜치에서 영속성 컨텍스트를 통해서 우리가 이득을 볼 수 있는 것이 장점이 누락되어서 일단 이 부분 먼저 설명하고 간다.

1. 1차 캐시    
2. 동일성(identity) 보장    
4. transactional write-behind, 트랜잭션을 지원하는 쓰기 지연라는 의미, 일단 이건 뒤에서 설명    
5. Dirty Checking, 변경 감지라는 의미, 이것도 뒤에서 설명



1차 캐시에 대해서는 우리는 전 브랜치에서 한번 경험했다. 사실 이것으로 엄청 큰 이득을 보기는 좀 힘들다.
어짜피 트랜잭션내에서 살아 있는 영속성 컨텍스트는 트랜잭션이 끝난 시점에는 날아가버리기 때문이다.

그래서 책에서도 JPA가 가지는 특성 또는 컨셉으로 표현한다.
물론 비지니스 로직이 만일 복잡하게 구성되어 있다면 장점으로 작용할 요소가 있지 않을까?

그럼 동일성 보장은 무엇인가?


일반적인 코드를 한번 수행해 보자

Test가 있다는 가정하에 다음과 같이 코드를 수행하면 어떤 결과를 알 수 있을까?


```
package io.basquiat;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test test = new Test();
		test.setId(1L);
		test.setName("basquiat");
		test.setType("artist");
		
		Test test1 = new Test();
		test1.setId(1L);
		test1.setName("basquiat");
		test1.setType("artist");
		
		System.out.println(test == test1);
	}

}

result -> false

```

자바를 안다면 test와 test1 객체는 다르다는 것을 알 수 있다. 너무 당연한건가?

하지만 JPA에서 다음과 같이 코드를 수행한다면 위와 다른 결과를 볼 수 있다.

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
        	Item item1 = em.find(Item.class, 1L);
        	
        	System.out.println("item과 item1은 동일한가요? " + (item == item1));
        	
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

결과는 

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
item과 item1은 동일한가요? true
```

동일성(identity) 보장을 하는 것이다. 이것이 가능한 이유는 1차 캐시때문이다.

테스트 해보지 않았지만 맨 위에 테스트 코드를 보면 myBatis에서는 같은 아이디로 2번 호출 해서 비교해 보면 false가 떨어질 것이라 예상한다.

아니라면 myBatis내부에서 객체에 담을 때 equals와 hasCode를 오버라이딩해서 같은 객체라고 볼 수 있게 '어떤 작업'을 할수도 있을 것이다. 

이건 myBatis를 뜯어보거나 테스트해보지 않았기 때문에 그냥 나의 뇌피셜인것이다.

'1차 캐시로 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공한다.'

라고 설명을 하고 있다. 

그렇다면 또 의문이 드는데 도대체 트랜잭션 격리수준이 뭐야?

[트랜잭션의 격리 수준이란?](https://medium.com/@sunnkis/database-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98%EC%9D%98-%EA%B2%A9%EB%A6%AC-%EC%88%98%EC%A4%80%EC%9D%B4%EB%9E%80-10224b7b7c0e)


역시 링크로...

이것은 데이터베이스와 관련된 내용이다. (JPA를 하지만 그럼에도 database를 공부해야 하는 이유) 


transactional write-behind, 즉 쓰기 지연은 무엇인가?

다른 프레임워크, myBatis나 nodeJs같은 경우에는 DB에 저장하기 위해서는 INSERT문을 생성해야하고 실행되는 순간 바로 DB로 쿼리가 날아가 데이터가 박히게 된다.

물론 중간에 어떤 로직을 수행해야한다면 batch를 사용할 수 있다. 

예를 들면 sql로 설명하자면 이런 방식이 가능하다.

```

INSERT INTO basquiat_item (name, price) VALUES ('Fender American Vintage 62 Reissue', 2500000), ('Fodera Emperor 5 Deluxe', 14000000), .....;


```

하지만 어찌되었든 어플리케이션에서 실행되는 순간 바로 날아가버린다.

그럼 JPA에서는 어떻게 되는건지 우리는 코드로 확인해 보자.


```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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
코드를 실행하자.

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
        	
        	/*
        	Item foderaBass = Item.builder().name("Fodera Emperor2 5").price(15000000).build();
        	em.persist(foderaBass);
        	Item gibsonGuitar = Item.builder().name("Gibson Black Beauty").price(6500000).build();
        	em.persist(gibsonGuitar);
        	*/
        	for(int i = 0; i < 3; i++) {
        		OtherItem otherItem = OtherItem.builder().id((long)i).name("otherItem_" + i).price(i*100000).build();
        		em.persist(otherItem);
        		System.out.println("영속성 컨텍스트!!!!");
        	}

        	
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

결과는?


```

영속성 컨텍스트!!!!
영속성 컨텍스트!!!!
영속성 컨텍스트!!!!
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)

```

아하! 일단 영속성 컨텍스트에 우리가 알지 못하는 어떤 공간(지연쓰기 SQL 저장소)에 담아두었다고 flush()가 발생하는 시점에 한번에 날리는구나라는 것을 알 수 있다.

그러면 bulk, 즉 batch insert는 어떻게 할까?

H2에서는 가능할거 같은데 안타깝게도 우리의 마이에스큐엘이는 특성상 기본키 매핑 전략인 @GeneratedValue에서 Sequence전략은 쓸 수 없고 AUTO나 그냥 전략없이 id를 입력받거나 Table전략을 써야 한다.

왠만하면 Table은 쓰고 싶지 않은데...

그래도 써봐야 겠지?

일단 이것을 하기 전에 persistence.xml에 수정이 필요하다.


```
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2"
             xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <persistence-unit name="basquiat">
        <properties>
            <!-- database configuration -->
            <property name="javax.persistence.jdbc.driver" value="com.mysql.jdbc.Driver"/>
            <property name="javax.persistence.jdbc.user" value="basquiat"/>
            <property name="javax.persistence.jdbc.password" value="basquiat"/>
            <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&amp;useUnicode=yes&amp;characterEncoding=UTF-8&amp;serverTimezone=Asia/Seoul"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.MySQL5InnoDBDialect"/>
            <!-- option configuration -->
            <!-- 콘솔에 sql 날아가는거 보여주는 옵션 -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.use_sql_comments" value="true"/>
            <property name="hibernate.generate_statistics" value="true"/>
            <property name="hibernate.jdbc.batch_size" value="10"/>
            <property name="hibernate.order_inserts" value="true"/>
       
            <!-- 이 옵션에 들어가는 것은 그냥 쓰지 말자. 테스트 용도 또는 개인 토이 프로젝트를 하는게 아니라면 validate정도까지만 그게 아니면 운영은 none으로 설정하자 -->
            <!-- 실제 none이라는 옵션은 없다. 따라서 none으로 하면 아무 일도 일어나지 않는다. -->
            <property name="hibernate.hbm2ddl.auto" value="create" />
        </properties>
    </persistence-unit>
</persistence>

```

jdbc.url에서 rewriteBatchedStatements=true 옵션을 추가하고 쿼리 메트릭을 보기 위해 hibernate.generate_statistics옵션과 batch_size, order_inserts옵션도 설정한다.

첫 번째는 그냥 id를 입력하는 경우이다. 사실 이런 경우는 특정 유니크한 값, 예를 들면 UUID와 넘버 계열의 조합으로 쓰는데 일단 그냥 long 타입을 넣어서 테스트 하자.

```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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

그리고 다음과 같이 한번 코드를 실행해 보자.

```

package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
        	
        	for(int i = 0; i < 20; i++) {
        		OtherItem otherItem = OtherItem.builder().id((long)i).name("otherItem_" + i).price(i*100000).build();
        		em.persist(otherItem);
        		
        	}
        	
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

결과는 어떨까?


```

Hibernate: 
    
    drop table if exists basquiat_item
Jul 01, 2020 8:40:26 AM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@aa5455e] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    drop table if exists basquiat_other_item
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Jul 01, 2020 8:40:26 AM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@5bbbdd4b] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_other_item (
       id bigint not null,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Jul 01, 2020 8:40:26 AM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
.
.
.
도합 20건이 건당 날아가는 쿼리 찍힘

Jul 01, 2020 8:40:26 AM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    494015 nanoseconds spent acquiring 1 JDBC connections;
    485303 nanoseconds spent releasing 1 JDBC connections;
    12138619 nanoseconds spent preparing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC statements;
    5764052 nanoseconds spent executing 1 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    38440867 nanoseconds spent executing 1 flushes (flushing a total of 20 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)

```

어? 그냥 한건씩 20건의 INSERT쿼리가 날아갔다. 
하지만 실제로 저것은 콘솔에서 저렇게 찍힌 것이고 밑에 우리가 hibernate.generate_statistics옵션을 줬는데 이것이 SesseionFactory에서 벌어진 측정항목들을 보여주는데 다음과 같다.

쿼리가 찍힌 밑 라인에 찍힌다.

```
INFO: Session Metrics {
    494015 nanoseconds spent acquiring 1 JDBC connections;
    485303 nanoseconds spent releasing 1 JDBC connections;
    12138619 nanoseconds spent preparing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC statements;
    5764052 nanoseconds spent executing 1 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    38440867 nanoseconds spent executing 1 flushes (flushing a total of 20 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)

```

특히 이 부분을 주목해 보자.

```
 batch_size, order_inserts 옵션을 줬을 경우
 
 494015 nanoseconds spent acquiring 1 JDBC connections;
 485303 nanoseconds spent releasing 1 JDBC connections;
 12138619 nanoseconds spent preparing 1 JDBC statements;
 0 nanoseconds spent executing 0 JDBC statements;
 5764052 nanoseconds spent executing 1 JDBC batches;

```

뭔가 감이 오지 않나?

실제로 그러면 persistence.xml에서 batch_size, order_inserts옵션을 주석처리하고 실행하게 되면 다음과 같은 측정항목 정보를 찍게 된다.


```
batch_size, order_inserts 옵션을 주석처리 했을 경우 

526102 nanoseconds spent acquiring 1 JDBC connections;
427671 nanoseconds spent releasing 1 JDBC connections;
15407082 nanoseconds spent preparing 20 JDBC statements;
11331745 nanoseconds spent executing 20 JDBC statements;

```

그럼 우리가 처음 JPA를 공부하면서 만나는 그림을 떠올려 보자. 

![실행이미지](https://github.com/basquiat78/java-oop/blob/master/img/capture1.PNG)

EntitiyManager가 하나의 커넥션을 가져다 썼고 커넥션을 통해서 sql을 실제로 실행하는 PreparedStatement가 20건, 즉 건당 하나씩 썼다는 것을 알 수 있다.


근데 batch_size, order_inserts옵션을 줬을 때 로그를 다시 한번 보자.

하나의 PreparedStatement을 사용해 batch로 한방에 인서트를 했다는 것을 알 수 있다.


자 그럼 이제 키매핑 전략을 Table로 설정하자. 

Table 설정에 대한 어노테이션 정보를 잘 보면 이것은 하나의 테이블을 생성하고 그것을 마치 sequence를 사용하듯 흉내내고 있다는 것을 알 수 있는데 암튼 옵션이 복잡하기도 하지만 다음과 같이 OtherItem을 설정하자.


```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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
	@GenericGenerator(
            name = "SequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "hibernate_sequence"),
                    @Parameter(name = "optimizer", value = "pooled"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "50")
            }
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "SequenceGenerator"
    )
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

그리고 다음과 같이 코드를 실행해보자.

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
        	
        	/*
        	Item foderaBass = Item.builder().name("Fodera Emperor2 5").price(15000000).build();
        	em.persist(foderaBass);
        	Item gibsonGuitar = Item.builder().name("Gibson Black Beauty").price(6500000).build();
        	em.persist(gibsonGuitar);
        	*/
        	for(int i = 0; i < 20; i++) {
        		OtherItem otherItem = OtherItem.builder().name("otherItem_" + i).price(i*100000).build();
        		em.persist(otherItem);
        		
        	}
        	
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


결과는 어떻게 나올까?

```

insert into hibernate_sequence values ( 1 )
Jul 01, 2020 9:18:09 AM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    select
        next_val as id_val 
    from
        hibernate_sequence for update
            
Hibernate: 
    update
        hibernate_sequence 
    set
        next_val= ? 
    where
        next_val=?
Hibernate: 
    select
        next_val as id_val 
    from
        hibernate_sequence for update
            
Hibernate: 
    update
        hibernate_sequence 
    set
        next_val= ? 
    where
        next_val=?
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)

도합 20건 쿼리 날아간 로그 찍힘 

Jul 01, 2020 9:18:09 AM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    18808809 nanoseconds spent acquiring 3 JDBC connections;
    1198006 nanoseconds spent releasing 3 JDBC connections;
    11191068 nanoseconds spent preparing 5 JDBC statements;
    3558677 nanoseconds spent executing 4 JDBC statements;
    3452745 nanoseconds spent executing 1 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    23156462 nanoseconds spent executing 1 flushes (flushing a total of 20 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}

```

어? 그런데 좀 달라졌다. 3개의 커넥션을 물고 5개의 PreparedStatement사용중 4개는 건당 쿼리를 날렸고 1개의 배치를 날렸다.

Table 전략으로 생성한 hibernate_sequence테이블에 한건의 INSERT 쿼리, 2건의 셀렉트, 2건의 업데이트 쿼리를 위해 4개의 statemaent를 소모하고 하나의 statemaent를 소비 해서 배치 쿼리가 날아갔다.

이유는 우리가 batch_size를 20으로 잡아뒀고 Table 전략을 위해 다음과 같이 어노테이션 설정을 했기 때문이다.

```

	@GenericGenerator(
            name = "SequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "hibernate_sequence"),
                    @Parameter(name = "optimizer", value = "pooled"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "50")
            }
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "SequenceGenerator"
    )

```

하이버네이트에서 권장하는 방식으로 hibernate_sequence라는 테이블을 생성하고 초기값인 initial_value를 1로 설정했다.

즉 해당 테이블에 next_val이라는 컬럼에 초기값 1을 설정하는데 하나의 statement 소비하고, INSERT쿼리를 날리기 전에 초기값인 id를 가져온다. 그리고 increment_size에 설정된 50단위로 업데이트를 치게 된다.

이 때 이것은 50만큼의 id를 확보하기 위한 방법이다. 차후에 설명을 하겠지만 그렇다고 알아두자.

그리고 다시 셀렉트를 해서 현재 인덱스를 값을 50단위로 업데이트 해서 최종 101의 next_val값을 세팅한다.

왜 이렇게 해야하는지는 이유를 잘 모르겠지만 아마도 미리 그만큼 확보하기 위한 방법이 아닌가 생각을 하게 되는데 어째든 저런 방식으로 hibernate_sequence에서 다음에 사용한 id값을 가져와서 순차적으로 1씩 증가시켜서 배치 쿼리를 만들고 날린다는 것이다.

만일 이렇게 생각을 해보자. batch_size는 20인데 iterate를 40까지 반복하면 어떻게 될까?

확보된 id값 안에서 다음과 같이 spent executing 2 JDBC batches가 찍힐 것이다. batch_size는 결국 그 수만큼 묶어서 배치 쿼리를 만들고 날리고 또 그 수만큼 배치 쿼리를 만들어서 날릴 것이다.

30만큼 돌리면 20개 묶어서 날리고 10개 묶어서 총 2번을 날리겠지...

근데 이 전략은 성능문제가 발생할 소지가 있다. 그리 아이디를 따오는 방식이 좀 불합리하다는 생각이 든다.

일단 batch_size나 미리 id값을 확보하기 위해 값을 설정하는데 만일 엑셀의 정보를 파싱해서 무언의 작업을 실행한다고 생각해 보자. (실제로 회사내부에서 상품 일괄 등록시 사용한다.)

얼마의 row가 발생할지 어떻게 알지??? 어느 날 대량의 상품 일괄 등록을 해야하는데 그 수는 분명히 정해져 있지 않을 것이다. 그때 그때 다르니깐.

그 사이의 갭을 어떻게 컨트롤 할것인가? ~~아마 난 안될꺼야....~~


그리고 그냥 편하자고 AUTO방식을 선택하게 되면 이건 최악이다.

이유는 다음과 같다.


```

package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
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

백문이 불여일타!

```

package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
        	
        	/*
        	Item foderaBass = Item.builder().name("Fodera Emperor2 5").price(15000000).build();
        	em.persist(foderaBass);
        	Item gibsonGuitar = Item.builder().name("Gibson Black Beauty").price(6500000).build();
        	em.persist(gibsonGuitar);
        	*/
        	for(int i = 0; i < 20; i++) {
        		OtherItem otherItem = OtherItem.builder().name("otherItem_" + i).price(i*100000).build();
        		em.persist(otherItem);
        		
        	}
        	
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

```
Hibernate: 
    
    insert into hibernate_sequence values ( 1 )
Jul 01, 2020 9:46:04 AM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    select
        next_val as id_val 
    from
        hibernate_sequence for update
            
Hibernate: 
    update
        hibernate_sequence 
    set
        next_val= ? 
    where
        next_val=?
 
셀렉트/업데이트 쌍으로 총 40번의 쿼리?????? 
 
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)

도합 20건의 로그 찍힘

Jul 01, 2020 9:46:04 AM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    24537455 nanoseconds spent acquiring 21 JDBC connections;
    6646822 nanoseconds spent releasing 21 JDBC connections;
    17670031 nanoseconds spent preparing 41 JDBC statements;
    22276928 nanoseconds spent executing 40 JDBC statements;
    2822073 nanoseconds spent executing 1 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    22264847 nanoseconds spent executing 1 flushes (flushing a total of 20 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
```

끔찍하다. 로그를 보게 되면 AUTO는 mySql에 맞는 전략을 설정해서 하게 되는데 Sequence를 쓸 수 없으니 결국 Table 전략을 선택해서 권장하는 방식으로 hibernate_sequence 테이블을 생성하고 앞서 말한 방식처럼 동작할텐데 차이점이 있다.

왜냐하면 최종 20개의 entity를 한번에 모아서 batch를 할텐데 이런 방식으로 작동한다. 그냥 말로 쉽게 풀어보자면 인간의 방식으로 생각해 보자.

'어디보자. 20개가 있네. 그럼 hibernate_sequence조회를 해볼까?
1이구만!!! 알았어~ 첫 번째 엔티티에는 id값을 1로 세팅해야지. 그전에 다음에 사용한 아이디값을 위해 update를 치자 1증가 시켜서 2로 업데이트하고 첫번째로 영속성 컨텍스트에 올라온 엔티티 녀석에 1을 세팅하자.

자 그럼 다음 엔티티를 세팅해보자. 아차! 그럼 hibernate_sequence조회를 해볼까?

2이구만!!! 알았어~ 두 번째 엔티티에는 id값을 2로 세팅해야지. 그전에 다음에 사용한 아이디값을 위해  update를 치자 1증가 시켜서 3로 업데이트하고 첫번째로 영속성 컨텍스트에 올라온 엔티티 녀석에 2을 세팅하자.

자 그럼 다음 엔티티를 세팅해보자. 아차! 그럼 hibernate_sequence조회를 해볼까?

3이구만!!!! 알았어~ 세 번째 엔티티에는 id값을 3로 세팅해야지. 그전에 다음에 사용한 아이디값을 위해  update를 치자 1증가 시켜서 4로 업데이트하고 첫번째로 영속성 컨텍스트에 올라온 엔티티 녀석에 3을 세팅하자.

4.....

5.....
.
.
.
'

이해가 가는가? 우리가 코드를 통해 말해주지 않으면 저렇게 무식한 방법으로 20번을 돌면서 채번을 해서 세팅하고 영속성 컨텍스트에 올리고 어떤 공간(지연쓰기 SQL 저장소)에 올려놨다가 배치 코드를 만들어서 보내버릴 것이다.

어 그럼 이게 그냥 IDENTITY방식이랑 뭐가 다른거야??? 더 쓸데 없넹??

오히려 20번이면 끝날 것을 무려 2배 이상의 statement를 사용하고 있다는 것이다.

아무튼 영속성 컨텍스트를 설명하다 삼천포로 쭈~~~~~~욱 빠졌다.

일단 이와 관련해서는 링크 하나 걸어두겠다.

[Hibernate disables insert batching at the JDBC level transparently if you use an identity identifier generator.](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#batch)

뭐 하이버네이트가 IDENTITY전략을 쓰게 되면 자동으로 insert batching을 비활성화시킨다니...어쩌겠나?

스택오버플로우에서 따로 myBatis나 jdbc를 이용하거나 jOOQ를 사용하라는데 jOOQ랑 queryDSL이란 비슷한 녀석이니 아마도 queryDSL에서 할 수 있지 않을까?

어째든 이 이야기가 나온 것은 transactional write-behind와 1차캐시와 관련된 부분이 있다.

예를 들면 IDENTITIY전략은 RDBMS에서 지원한다면 insert 시점에는 해당 엔티티의 id를 알 수 없다.

특히 mySQL의 경우에는 스키마 생성시 primary key에 auto_increment 옵션을 주게 되면 create시점에 primary key값을 따게 된다.

자꾸 이야기기 삼천포로 빠지는데 myBatis를 다뤄본 분이라면 알겠지만 이런 의문이 들것이다.

'내가 그런 코드를 본적이 있다. 인서트 쿼리를 실행하고 반환된 객체에는 id가 박혀 들어오던데요?'


자 그럼 myBatis의 경우에는 어떻게 동작을 할까?


```
<insert id="createItem" parameterType="io.basquiat.Item">
	INSERT INTO basquiat_item
	 (
	  name,
	  price
      ) 
      VALUES
      (
       #{name},
       #{price},
      )
	<selectKey keyProperty="id" resultType="long" order="AFTER">
    		SELECT last_insert_id()
    </selectKey>
</insert>

```

간만에 보는 코드이긴 한데 해당 xml을 보면 insert실행 SELECT last_insert_id()를 통해서 Item 객에 삽입하는 것을 볼 수 있다.

손을 좀 봐야하는 것이다.

하나의 트랜잭션안에서 인서트 실행해 생성된 primary key의 마지막으로 생성된 값을 세팅하게 되는 것이다.

NodeJs는요?

궁금해요?

```
return new Promise((resolve, reject) => {
		let sql= 'INSERT INTO basquiat_item
					(
					 name, price
					)
					VALUES
					(
					 ?, ?
					)';
					
		connection.query(sql, ['FORDERA', 14500000], (err, result) => {
		  	if(err) {
		  		reject(err);
		  	} else {
		  		resolve(insertId);
		  	}
		});
});
```


그럼 JPA에서는 IDENTITY의 경우에는 영속 상태를 만들기 위해서 em.persist를 하는 순간 tx.commit()시점에 쿼리가 날아가는 것이 아니라 영속성 컨텍스트에 담는 순간 날아가는 이유가 설명이 된다.

AUTO, Table, Sequence전략의 경우에는 엔티티 자체는 지연쓰기 SQL저장소에 저장되서 한번에 날아가긴 하지만 그 이전 그러니까 영속성 상태로 만들기 전에 id를 가져오기 위해서는 몇 번의 io가 발생하게 되는 것이다.


앞서 설명한 그림에서 1차 캐시를 설명할 때로 다시 넘어가서 내부에 id와 엔티티를 매핑하는 공간이라고 설명했다. 

즉 이 아이디를 알기 위해서는 결국 쿼리를 날려서 받아와야 하는 것이다.

그래서 이런 것을 방지하기 위해서 primary key를 UUID같은 유니크한 값과 특정 값의 조합으로 직접 생성해서 넣는 것을 많이 보게 되지만 현재 회사에서는 mySql이고 스키마생성시 primary key는auto increment나 상품 고유의 item_code에 유니크 제약을 걸어서 사용한다.

그래서 일반적인 방식으로는 IDENTITY 전략을 사용할 수 없다.

이로써 영속성 컨텍스트의 지연쓰기에 대해서 어느 정도 많은 이야기를 한거 같다. 사실 여기에 언급한 것은 한 챕터로 할애해야 하는 부분이지만 진행하다보니 이렇게 왔다. 또 뒤에서도 언급하게 될 이야기긴 하지만 테스트하는김에 같이 해보는 것도 나쁘진 않다.

자 그럼 마지막 dirty checking이다.

그냥 이건 간단하게 설명하고 넘어가겠다.

우리는 지금까지 1차 캐시, 동일성 보장, 지연쓰기에 대해서 쭉 알아봤는데 결국 이 모든 것은 단독적인 것이 아니고 서로 엮여 있다고 보면 된다.

dirty checking을 직역하자면 '더러워진 거 체크하자'라고 할 수 있는데 데이터 관점에서 보면 기존의 데이터가 뭔가 '지져분해진', 즉 무언가 바꼈다 또는 변경되었다고 볼 수 있다.
즉, 변경된 게 있나 체크하는 '변경 감지'라고 표현하게 되는 거 같다.

통상적으로 우리가 db관점에서 바라볼 때 어떤 데이터를 변경하기 위해서는 다음과 같은 쿼리를 날리게 된다.

```

UPDATE basquiat_item
   SET price = ?
 WHERE id = ?

```

그러면 JPA에서는 어떻게 할까?

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
        	
        	Item foderaBass = Item.builder().name("Fodera Emperor2 5").price(15000000).build();
        	em.persist(foderaBass);
        	em.flush();
        	em.detach(foderaBass);
        	// 강제로 flush해서 디비에 데이터꼽아놓고 준영속 상태로 만들자.
        	
        	int salePrice = 1000000;
        	
        	// 다시 셀렉트하
        	Item selectBass = em.find(Item.class, 1L);
        	System.out.println("Bass Price is " + selectBass.getPrice());
        	System.out.println("beforeUpdate price");
        	selectBass.setPrice(selectBass.getPrice() - salePrice);
        	System.out.println("update price");
        	em.flush();
        	em.detach(selectBass);
        	
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


```

로그를 보자.

```

Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (name, price) 
        values
            (?, ?)
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Bass Price is 15000000
beforeUpdate price
update price
Hibernate: 
    /* update
        io.basquiat.model.Item */ update
            basquiat_item 
        set
            name=?,
            price=? 
        where
            id=?
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.name as name2_0_0_,
        item0_.price as price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Bass Price is 14000000

```

특이한 점이 로그에 보이게 된다. 위에서 언급했듯이 우리는 테이블내에 어떤 값을 변경하기 위해서는 update를 통해 정보를 갱신하게 된다.

예를 들면 이런 코드가 필요하지 않을까?

```

em.update(entitiy);


```

myBatis라면 어떻게 했을까?

```

<update id="updateItem" parameterType="io.basquiat.Item">
	UPDATE basquiat_item
	   SET name = #{name},
		   price = #{price}
	WHERE id = #{id}
</update>

```


하지만 코드에는 그런 코드가 어디에 없다. 단지 나는 셀렉트해 온 엔티티에 그냥 가격을 할인 가격으로 변경했을 뿐이다.

맨 위에서 1차 캐시, 동일성 보장, 지연 쓰기와 관련된 이 모든 것은 하나로 엮여 있다고 했는데 하나씩 생각을 정리해 보면 다음과 같이 생각할 수 있다.


1. 영속성 상태의 엔티티는 1차 캐시에 올라가 있다.
2. 영속성 상태의 엔티티의 필드가 변경이 되면 변경된 내역을 JPA에서 알아챈다.
3. 갱신할 정보가 있다는 것을 알게 된 JPA는 이것을 지연쓰기 SQL 저장소에 올린다.
4. 최종적으로 flush() 또는 tx.commit이 발생하게 되면 갱신된 정보를 쿼리로 날린다.

단지 영속성 컨텍스트가 관리하는 엔티티는 이런 방식으로 갱신된 정보를 알아채고 그에 맞는 쿼리를 알아서 생성해 준다.


그에 앞서 꾸준히 등장하는 flush라는 녀석이 있다.

쭉 진행해 오면서 우리는 tx.commit()을 만날 때 디비에 실질적으로 쿼리가 날아간다는 것을 배웠다.

하지만 어떤 이유로 해당 정보는 tx.commit()을 만나기 전에 디비에 꼽아버려야하는 일이 생긴다면 강제적으로 디비에 꼽을 수 있어야 한다.

flush라는 단어가 사실 화장실에서 물내릴때 물을 흘려내려버린다는 의미도 있고 다양한 의미가 있는데 말 그대로 강제로 내가 변기 버튼을 눌러서 물을 내린다고 생각하면 된다.

좀 더러운 이야기지만 응가를 할때 이런 경험을 말할 수있겠다.

오늘 응가가 엄청 마렵고 많이 나올거 같은데 한번 응가를 했어. 근데 일단 버튼을 눌러서 1차로 나온 응가를 흘려버리고 싶은 맘이 드는거야. 그래서 버튼을 눌렀찌?

그리고 응가는 물을 타고 내려가겠지. 그리고 또 응가가 나올려고 하는 이 상황인거다.

하지만 이 flush는 그렇다고 해당 엔티티가 영속성컨텍스트에서도 사라지는 것은 아니다.

한 트랜잭션 내에서라면 단지 지연쓰기 SQL저장소에서 디비로 정보를 흘려 내려서 꼽히게 하는 것일 뿐 영속상태는 계속 유지된다는 거 참고하시면 될 거 같다.

다음 브랜치는 이제부터 본격적인 엔티티와 테이블 매핑에 대해 떠들어 보겠다.
