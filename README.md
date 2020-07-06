# Entity Mapping part2

이전 브랜치에서 우리는 @Entity와 @Table에 대해서 알아봤다.

거기에 이어서 쭉 알아보자.

## @DynamicUpdate

영속성 컨텍스트와 관련해서 우리는 dirty checking을 알게 되었다.    

하지만 눈썰미 좋으신 분들이라면 좀 의문이 들것이다.     


'나는 그냥 상품명만 바꿨는데 쿼리는 보니깐 상품명과 가격과 관련된 쿼리가 전부 날아가네요?'

이게 무슨 소린지 이전 코드를 한번 살펴보자.

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
        	
        	Item item = Item.builder().name("FODERddd").price(15000000).build();
        	em.persist(item);
        	em.flush();
        	em.detach(item);
        	
        	Item selectedItem = em.find(Item.class, 1L);
        	selectedItem.setName("Fodera");
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

로그를 보게 되면    

```
Hibernate: 
    /* update
        io.basquiat.model.Item */ update
            basquiat_item 
        set
            it_name=?,
            it_price=?,
            test=? 
        where
            id=?

```

이런 쿼리가 날아간 것을 볼 수 있다.     

어느 누눈가는 이렇게 생각할 것이다.     

'JPA가 이렇게 좋은데 저렇게만 보낼 리는 없다. 방법이 있지 않을까?'     

당연히 있다. @DynamicUpdate을 클래스에 붙이면 된다.    

자 그러면 

```

package io.basquiat.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_item")
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	//@Column(name = "it_name", insertable = false)
	//@Column(name = "it_name", updatable = false)
	@Column(name = "it_name", columnDefinition = "varchar(20) not null")
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;

	@Column(name = "test", precision = 10, scale = 8)
	private BigDecimal test;
	
	@Builder
	public Item(String name, Integer price) {
		this.price = price;
		this.name = name;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", price=" + price + "]";
	}
	
}


```

이렇게 해당 어노테이션을 걸고 위 코드를 다시 실행하면 결과는 어떻게 될까? 뭐 예상되로 나올것이다.    

```
Hibernate: 
    /* update
        io.basquiat.model.Item */ update
            basquiat_item 
        set
            it_name=? 
        where
            id=?

```

참고로 dirty checking은 말 그대로 변경감지이다. 기존의 같은 상품명을 넣게 되면 당연히 변경된 사항이 없다고 판단하기에 업데이트 쿼리가 날아가지 않는다.    


## @Column    

이 부분은 간단하게 설명과 몇 몇 테스트 코드를 작성해서 진행해 보고자 한다.     

테스트이기 때문에 hibernate.hbm2ddl.auto옵션은 create로 두고 진행하자.	

테스트의 간편화를 위해서 OtherItem의 경우에는 @Entity와 @Table을 주석처리 했다.	    

지금까지 잘 따라왔다면 앞으로 OtherItem은 JPA에서 관리하지 않기 때문에 테스트 코드에서 테이블을 생성하지도 않을 것이다.    

이 어노테이션의 설명은 말 그대로 컬럼과 실제 필드와의 매핑을 하기 위해서이다.    

'어 그런데 지금까지는 이 어노테이션을 안써도 잘 됬잖아요?'

그렇다. 하지만 언제나 어플리케이션내의 개발적인 측면에서 객체의 필드와 DB상의 필드명은 때론 달라질 수 있다.    

@Table의 경우에서도 언급하긴 했는데 이녀석 역시 그렇게 할 수 있도록 몇가지 속성들을 제공한다.

1. name: 느낌 팍!

딱 봐도 느낌이 온다. 지금까지 Item에서 아이템의 이름과 가격을 단순하게 name, price로 설정해 놨다.     

언제나 느끼는 거지만 이런 변수명 설정은 개발자들에게는 영원한 숙제일텐데 현재 재직하고 있는 회사의 경우에는 it_name, it_price로 설정되어 있다. 왜 이렇게 지었는지는 나는 모른다. 하지만  Item객체에 변수명을 itName, itPrice (난중에 언급하겠지만 스프링부트에서는 1.5이후 버전이었던가? 마치 관례처럼 저렇게 변수명을 설정하면 snake case라고 하는데 대문자를 만나면 '_'즉, 언더스코어붙이고 소문자로 변경한다. 물론 옵션으로 변경할 수 있다)로 짓고 싶지 않다. 이럴 때 이것을 쓴다.    

코드의 일부분만 보자    

```
@Column(name = "it_name")
private String name;

@Column(name = "it_price")
private Integer price;

```
별거 없다.    


2. insertable과 updatable: 뭐 굳이 설명할 필요가 있을까만은 등록과 변경 여부 설정이라는 것을 알 수 있다. 기본적으로 true

자 그럼 이게 뭐지? 등록과 변경 여부 설정이라는 것인데 이것을 설마 false로 두면 안들어가거나 변경이 안되는거야?

언제나 백문이 불여일타

```
package io.basquiat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	@Column(name = "it_name", insertable = false)
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;
	
	@Builder
	public Item(String name, Integer price) {
		/*
		 * 스프링을 사용하는 것이 아니라서 현재는 commons-lang3의 StringUtils를 사용해서 IllegalArgumentException를 던진다.
		 * 사실 스프링을 사용하게 되면 Assert를 이용해서 DB영역이 아닌 어플리케이션 영역에서 에러를 뱉어내게 하는게 주 목적이고
		 * 이렇게 함으로써 에러캐치를 빠르게 잡아서 대응하고 개발자의 실수를 줄인다.
		 * 번거롭지만 실제로 이렇게 하는 것이 차후에는 정신적으로 편해진다.
		 */
		if(StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("item name must be not null"); 
	    }
		if(price == null || price < 0) {
			throw new IllegalArgumentException("price must be not under 0"); 
	    }
		
		this.price = price;
		this.name = name;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", price=" + price + "]";
	}
	
}


```

name이라는 필드에 insertable을 false로 두고 다음과 같이 코드를 실행해 보자.

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
        	
        	Item item = Item.builder().name("FODERA").price(1000000).build();
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

그리고 로그를 보니깐 정말이다!

```
7월 04, 2020 4:54:32 오후 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 04, 2020 4:54:32 오후 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 04, 2020 4:54:32 오후 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 04, 2020 4:54:33 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
7월 04, 2020 4:54:33 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 04, 2020 4:54:33 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 04, 2020 4:54:33 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 04, 2020 4:54:33 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 04, 2020 4:54:34 오후 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    drop table if exists basquiat_item
7월 04, 2020 4:54:35 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@7dcc91fd] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        it_name varchar(255),
        it_price integer,
        primary key (id)
    ) engine=InnoDB
7월 04, 2020 4:54:35 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@138a7441] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
7월 04, 2020 4:54:35 오후 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (it_price) 
        values
            (?)
7월 04, 2020 4:54:35 오후 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    576800 nanoseconds spent acquiring 1 JDBC connections;
    515900 nanoseconds spent releasing 1 JDBC connections;
    12159300 nanoseconds spent preparing 1 JDBC statements;
    3056500 nanoseconds spent executing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    8856600 nanoseconds spent executing 1 flushes (flushing a total of 1 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 04, 2020 4:54:35 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]


```

오호! 실제 인서트 쿼리에는 가격 정보만 들어간다. 

그럼 updatable옵션도 테스트 해보자.

```
@Column(name = "it_name", updatable = false)

```
요렇게 옵션을 설정하고     

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
        	
        	Item item = Item.builder().name("FODERA").price(1000000).build();
        	em.persist(item);
        	em.flush();
        	System.out.println("DB에 쿼리 바로 날리자");
        	em.clear();
        	System.out.println("영속성 클리어");
        	
        	Item selected = em.find(Item.class, 1L);
        	System.out.println(selected);
        	selected.setName("FODERAAAAAAAA");
        	System.out.println("update 쿼리가 날아갈까?");
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

결과는 예상대로 


```

Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (it_name, it_price) 
        values
            (?, ?)
DB에 쿼리 바로 날리자
영속성 클리어
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.it_name as it_name2_0_0_,
        item0_.it_price as it_price3_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=FODERA, price=1000000]
update 쿼리가 날아갈까?

```

날아가지 않았다.     
이런 옵션을 둔 이유가 무엇일까?      

사정에 따라서는 어떤 컬럼은 최초 INSERT만 허용하고 데이터변경이 일어나면 안되는 경우가 생길 수도 있다.

그래서 실수로 set을 통한 코드를 만들어도  이런 옵션의 조합을 통해서 제약을 둘 수 있다.

이런 옵션을 어떻게 활용하느냐는 아마도 비지니스 로직에 따라 결정될 것이다.

3. table: 일단 이건 뒤에서 설명하겠다.

table은 좀 독특한 케이스에 사용할 수 있는데 일단 이거는 뒤에서 설명하고 이제 설명할 것들은 hibernate.hbm2ddl.auto옵션을 create로 두었을 시에 테이블 생성에 관여하는, 즉 DDL과 관련된 것이다. 위에 옵션들중 name을 제외하고는 어플리케이션단에서 벌어질 수 있는 옵션들이라면 이후의 옵션들은 테이블 생성시에 좀 더 디테일함을 주기 위해서 사용되느 경우이다.


4. length: 길이를 조절한다. 참고로 이 옵션은 String타입인 경우에만 쓸 수 있고 기본 255이다.

지금까지 테이블이 생성된 로그를 한번 살펴보자.


```
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        it_name varchar(255),
        it_price integer,
        primary key (id)
    ) engine=InnoDB

```

Item을 예로 들면 우리가 주목할 부분은 it_name과 it_price컬럼에 대해서이다.

예를 들면 상품의 이름이 20자 이상을 넘을 수 없다는 회사의 방침이 있다면 테이블 생성시에 varchar(255)로 둘 이유가 없다는 것이다. 255인 이유는 위에 설명에 있다.

그럼 어떻게 해야할까?

눈썰미 좋으면 다음처럼 할 수 있다.

```
@Column(name = "it_name", length = 20)
private String name;
```

생성 쿼리는 ?    

```

Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        it_name varchar(20),
        it_price integer,
        primary key (id)
    ) engine=InnoDB

```

varchar(20)으로 생성하고 있다.    

실제로 데이터를 밀어넣을 때 name을 20자가 넘는 길이로 테스트 해보면 


```
ERROR: Data truncation: Data too long for column 'it_name' at row 1
```

이렇게 문자열이 너무 길어서 에러를 뱉어낸다.

5. nullable: '널' 보낼지 말지 결정한다.

이게 무슨 말이냐면 전 브랜치에서 무결정 제약 조건에 대해 링크를 하나 걸어둔게 있었다.    

이것도 무결정 제약 조건과 관련된 녀석인데 인서트 쿼리 실행시 해당 필드에는 무조건 데이터가 있어야 한다는 것을 결정짓는다.

기본적으로 true로 널 허용하고 있지만 false로 두면 어떻게 될까?

빌더 패턴 사용시 생성자내부에 유효성 코드가 있으니 이것을 싹 지워서 Item를 수정하자.

```

package io.basquiat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	//@Column(name = "it_name", insertable = false)
	//@Column(name = "it_name", updatable = false)
	@Column(name = "it_name", length = 20, nullable = false)
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;
	
	@Builder
	public Item(String name, Integer price) {
		this.price = price;
		this.name = name;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", price=" + price + "]";
	}
	
}


```

기존에는 name에 값을 넣지 않아도 쿼리가 날아갔지만 지금은 널 허용을 하지 않았으니 예상이 될것이다.

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
        	
        	Item item = Item.builder().name(null).price(1000000).build();
        	//Item item = Item.builder().name("").price(1000000).build();
        	em.persist(item);
        	em.flush();
        	System.out.println("Query????");
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

7월 04, 2020 5:52:38 오후 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 04, 2020 5:52:38 오후 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 04, 2020 5:52:39 오후 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 04, 2020 5:52:40 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
7월 04, 2020 5:52:40 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 04, 2020 5:52:40 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 04, 2020 5:52:40 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 04, 2020 5:52:40 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 04, 2020 5:52:41 오후 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    drop table if exists basquiat_item
7월 04, 2020 5:52:41 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@7dcc91fd] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        it_name varchar(20) not null,
        it_price integer,
        primary key (id)
    ) engine=InnoDB
7월 04, 2020 5:52:41 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@138a7441] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
7월 04, 2020 5:52:42 오후 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
7월 04, 2020 5:52:42 오후 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    426200 nanoseconds spent acquiring 1 JDBC connections;
    404600 nanoseconds spent releasing 1 JDBC connections;
    0 nanoseconds spent preparing 0 JDBC statements;
    0 nanoseconds spent executing 0 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    0 nanoseconds spent executing 0 flushes (flushing a total of 0 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 04, 2020 5:52:42 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]



```

오호? 좀 의문이 든다. 여러분도 의문이 드는가? ~~어떤 의문이 드는지 모르겠어요~~

테이블 생성시 로그를 보면 해당 컬럼에 옵션을 주니 not null이 붙어있었다.    

그리고 나서 고의적으로 null을 name에 세팅을 하고 flush()를 했음에도 쿼리가 날아가지 않았다.

내부적으로 JPA는 이렇게 생각하는 거 같다.     

'이거 날리면 에러나는데 그냥 내 선에서 처리하지 뭐.'

약간 이런 츤데레 느낌. ~~그래도 에러라도 좀 알려주지~~

그래서 아마도 쿼리를 생성해서 날리지 않는 거 같다.    

그럼 우리는 이런 작은 의문에서 디테일하게 좀 더 가보자.    

지금 테이블은 not null로 생성되어 있다.    

 hibernate.hbm2ddl.auto 옵션을 none으로 두고 Item에서 다음과 같이 수정하자.    
 
 ```
 @Column(name = "it_name", length = 20)
	private String name;
 
 ```

그냥 옵션을 지우자.   

이러고 실행하면 어떻게 될까?


```
7월 04, 2020 5:54:17 오후 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 04, 2020 5:54:17 오후 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 04, 2020 5:54:17 오후 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 04, 2020 5:54:18 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
7월 04, 2020 5:54:18 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 04, 2020 5:54:18 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 04, 2020 5:54:18 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 04, 2020 5:54:18 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 04, 2020 5:54:19 오후 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
7월 04, 2020 5:54:20 오후 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (it_name, it_price) 
        values
            (?, ?)
7월 04, 2020 5:54:20 오후 org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1048, SQLState: 23000
7월 04, 2020 5:54:20 오후 org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Column 'it_name' cannot be null
7월 04, 2020 5:54:20 오후 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    508500 nanoseconds spent acquiring 1 JDBC connections;
    397500 nanoseconds spent releasing 1 JDBC connections;
    14736600 nanoseconds spent preparing 1 JDBC statements;
    14378800 nanoseconds spent executing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    0 nanoseconds spent executing 0 flushes (flushing a total of 0 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 04, 2020 5:54:20 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]


```

오호! 로그를 보면 일단 날렸다. 아무래도 해당 옵션이 없으니 그런거 같다. 그리고 'ERROR: Column 'it_name' cannot be null' 오류를 볼 수 있게 된다.    

단순하게 DDL만 관여하는게 아니라는 의미이다.    

그러면 이런 경우도 생각해야 한다.

만일 테이블에서는 null을 허용한다고 할 때 만일 해당 필드에 'nullable = false'가 붙으면 어떻게 될까?     

실제로 해보면 쿼리를 날리지 않는다.    

즉, JPA에서는 이 옵션이 붙어 있기 때문인듯 싶다.

참고로 빈 문자열 ""로 하게 되면 또 들어간다.    

따라서 객체를 만들 때 이런 부분을 고려해서 좀 더 디테일하게 만줘줄 필요가 있다.    

또한 스프링를 좀 아는 분들은 @NotNull에 대해서도 알것이다. 기능은 같다. 하지만 차이점이라면 뭐...DDL에 관여하냐 안하냐 정도??


6. unique: 유니크 제약 조건걸기, 기본 false    

이미 이전 브랜치에서 유니크 제약 조건을 거는 방법을 본 적이 있다.
이것은 그냥 한 필드에 쉽게 걸기 위한 방법이다.


자 그럼 실제로 봐야한다.


```
@Column(name = "it_name", unique = true)
private String name;

```

실행해 보자.    

이제부터 특별한 경우가 아니면 코드를 쓰지 않을 생각이다. 너무 길어진다...     


결과는 어떻게 될까?

```
    alter table basquiat_item 
       add constraint UK_axhh8fncbkb5ykrm4suvyjtav unique (it_name)

```

테이블을 생성하고 유니크 조건을 거는 alter 쿼리를 날렸다. 하지만 이름이 'UK_axhh8fncbkb5ykrm4suvyjtav'처럼 뭔가 괴랄하다.    

이전 브랜치에서도 알 수 있듯이 이 유니크 조건에 의해 에러가 발생하게 되면 해당 유니크 명을 화면에 보여주게 되어 있다.

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
        	
        	Item item1 = Item.builder().name("basquiat").price(1000000).build();
        	Item item2 = Item.builder().name("basquiat").price(1000000).build();
        	em.persist(item1);
        	em.persist(item2);
        	em.flush();
        	System.out.println("Query????");
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

이렇게 코드를 실행하면 

```

7월 04, 2020 6:13:18 오후 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 04, 2020 6:13:18 오후 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 04, 2020 6:13:18 오후 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 04, 2020 6:13:19 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
7월 04, 2020 6:13:19 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 04, 2020 6:13:19 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 04, 2020 6:13:19 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 04, 2020 6:13:19 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 04, 2020 6:13:21 오후 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    drop table if exists basquiat_item
7월 04, 2020 6:13:21 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@7dcc91fd] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        it_name varchar(255),
        it_price integer,
        primary key (id)
    ) engine=InnoDB
7월 04, 2020 6:13:21 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@7e31ce0f] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    alter table basquiat_item 
       add constraint UK_axhh8fncbkb5ykrm4suvyjtav unique (it_name)
7월 04, 2020 6:13:22 오후 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (it_name, it_price) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (it_name, it_price) 
        values
            (?, ?)
7월 04, 2020 6:13:22 오후 org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1062, SQLState: 23000
7월 04, 2020 6:13:22 오후 org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Duplicate entry 'basquiat' for key 'UK_axhh8fncbkb5ykrm4suvyjtav'
7월 04, 2020 6:13:22 오후 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    505800 nanoseconds spent acquiring 1 JDBC connections;
    448400 nanoseconds spent releasing 1 JDBC connections;
    11036600 nanoseconds spent preparing 2 JDBC statements;
    17220600 nanoseconds spent executing 2 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    0 nanoseconds spent executing 0 flushes (flushing a total of 0 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 04, 2020 6:13:22 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]



```
ERROR: Duplicate entry 'basquiat' for key 'UK_axhh8fncbkb5ykrm4suvyjtav' 보여주는 것이 보이는가?    

하지만 이렇게 두면 참 뭔지 알수가 없다.    

하지만 필요할 때 쓰라고 만든 만큼 저것이 별 의미가 없고 해당 필드에 대해서는 걸어야 하는 이유가 있다면 이 옵션을 줘서 스키마를 생성하자.    

다만 이녀석은 단순하게 DDL생성시에만 관여하는 옵션이다.   


7. columnDefinition: 컬럼 정의서? 이것은 스키마를 쿼리로 만들 줄 아는 경우 좀 더 디테일한 값을 넣어주기 위해서 만들 수 있다.

직접적으로 내가 만들 필드에 옵션을 세심하게 만들어 주는 역할이다.

```
@Column(name = "it_name", columnDefinition = "varchar(20) not null comment '상품명'")
private String name;

```

여러개 옵션보다는 이렇게 컬럼 타입과 NULLABLE, 주석까지 한번에 처리하고 싶은 경우라면 이런 방식을 사용할 수 있다. 

또는 default설정으로 기본 설정값을 넣어줄 수 있다.    

이 녀석도 DDL생성시에만 관여한다. 이 여부는 이렇게 해 놓고 name에 null을 넣으면 쿼리가 날아가고 에러 로그를 찍는 걸 보면 알 수 있다.     


8. precision과 scale: BigDecimal, BigInteger타입인 경우 사용할 수 있다.

자 어떤 분들은 BigDecimal, BigInteger타입에 대해 생소할 수 있다.     

나의 경우에는 암호화폐 거래소하면서 줄기차게 봤던 것들이고 실제 코인 값이 소수 8자리까지 가는 것도 있기 때문이다.    

비트코인과 이더리움때문이다. 비트코인중에는 소수점으로부터 사토시라는 단위를 사용하기도 하고 이더리움 역시 wei, gas같은 단위가 존재하기 위해서이다.     

잠깐 설명하자면 이거 많이 봤을 것이다.     


당장 크롬의 콘솔창에서 

```
let a = 0.3;
let b = 0.6;

a + b = ??

let c = 0.4;
let d = 0.5;

c + d = ??

```

소수점 계산을 염두해둬야 하고 비트 코인의 경우에는 0.1 또는 0.01도 엄청난 가격이기 때문에 무시할 수 없다. 따라서 BigDecimal, BigInteger를 사용한다.    

뭐 그외에도 c#의 경우에도 이런 것들을 제공한다.    

precision은 보통 소수점을 포함한 전체 자릿수를 말하고 scale은 소수점 몇자리까지 다루겠냐는 의미를 둔다.

기본값으로 precision=19, scale=2로 잡혀있다.

어짜피 테스트로 스키마 생성되는 쿼리를 확인하기 위한 것이니 Item에 다음과 같은 필드 하나 놓고 테스트 해보자

```
@Column(name = "test", precision = 10, scale = 8)
private BigDecimal test;
```

그럼 실행해서 생성되는 쿼리 한번 확인해 보자.


```
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        it_name varchar(20) not null,
        it_price integer,
        test decimal(10,8),
        primary key (id)
    ) engine=InnoDB

```

그럼 언제 쓸까? 일반적인 이커머스에서 쓸일이 있을까만은 보통은 할인율은 소수점은 버리거나 하는데 아까 언급했듯이 이러한 소수점이나 계산에 정밀함을 요구하는 곳에 사용할 수 있겠다.     

좋은 예를 뭐....거래소겠지...     

그럼 이제 table옵션은 뭐에요??     

그냥 편하게 쓰지 마라고 하고 싶다.     

하지만 뭔지는 알고 넘어가자. 재미있는 것은 이런 것을 알게되면 왠지 써보고 싶어지는 분들 있는데 경험상 골치 아픈 경험을 하게 될것이다.    

이런 고민해 본적 있나?     

"2개의 테이블을 조회해서 정보를 담는 엔티티가 있으면 좋겠어요!!!!"

오호? 그게 가능해? 물론 가능하다.      

업무를 하다 보면 이런 케이스의 테이블을 자주 보게 된다.

1. 그냥 다른 두개의 테이블 하나로 엮에서 정보를 가져오는 경우.

2. 두 개의 테이블이 연관이 있는데 하나의 엔티티에 정보를 담고 싶은 경우.

즉 하나의 엔티티에 2개 이상의 테이블을 매핑해서 보고 싶은 경우 @SecondaryTable이라는 것을 쓸 수 있다.    

다음과 같은 테이블이 있다고 가정하자.    

```
CREATE TABLE `first` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `one` varchar(255) DEFAULT NULL,
  `two` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci


CREATE TABLE `second` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `one` varchar(255) DEFAULT NULL,
  `two` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_cifirstfirst

```
생성하고 각 테이블마다 데이터를 넣어보자.



FirstTable

```
package io.basquiat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "first")
@SecondaryTables({
				@SecondaryTable(name = "second")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class FirstTable {
	
	@Builder
	public FirstTable(String one, String two) {
		super();
		this.one = one;
		this.two = two;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Setter
	@Getter
	private String one;
	
	@Setter
	@Getter
	private String two;
	
	@Setter
	@Getter
	@Column(name = "one", table = "second")
	private String secondOne;
	
	@Setter
	@Getter
	@Column(name = "two", table = "second")
	private String secondTwo;
}


```

SecontTable

```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SecondTable {

	@Builder
	public SecondTable(String one, String two) {
		super();
		this.one = one;
		this.two = two;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Setter
	@Getter
	private String one;
	
	@Setter
	@Getter
	private String two;
	
}


```

좀 어거지이긴 하지만 이렇게 FirstTable엔티티 기준으로 작성한 코드를 살펴보기 바란다.

이 엔티티에서 SecondTable의 정보를 매핑하는데 FirstTable입장에서는 secondOne, secondTwo는 어떤 컬럼인지 모른다.     

느낌 오지? 그렇다. 해당 필드는 어떤 테이블 (@Table에 명시된, 즉 JPA가 관리하는 그 값)의 컬럼이라는 것을 명시하는 것이다.    

그럼 실행하면??

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.FirstTable;

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
        	
        	FirstTable first = em.find(FirstTable.class, 1L);
        	System.out.println(first.toString());
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

결과는??


```

Hibernate: 
    select
        firsttable0_.id as id1_1_0_,
        firsttable0_.one as one2_1_0_,
        firsttable0_.two as two3_1_0_,
        firsttable0_1_.one as one1_2_0_,
        firsttable0_1_.two as two2_2_0_ 
    from
        first firsttable0_ 
    left outer join
        second firsttable0_1_ 
            on firsttable0_.id=firsttable0_1_.id 
    where
        firsttable0_.id=?
FirstTable(id=1, one=빠스트테이블One, two=빠스트테이블Two, secondOne=쎄깐드One, secondTwo=쎄칸드Two)

```

그냥 지들끼리 primary key로 LEFT JOIN해서 가져온다.

```
 pkJoinColumns = @PrimaryKeyJoinColumn(name = "id")

```

물론 요런 식으로 직접적인 매핑이 가능하긴 하다. 아마 기본적인 세팅인 듯.           

하지만 그냥 쓰지 않는게 좋지 않을까?     

뭔가 좋아보이긴 한다. 또는 어떻게 보면 VIEW를 흉내낸 것처럼 보이기도 한다.     

하지만 VIEW를 겨냥하고 사용했다가는 해당 테이블들의 변경점이 생기면 이 녀석도 수정하는 관리 포인트가 늘게된다.

실제 경험을 이야기 하자면 팀원들끼리 이야기하면서 이렇게 VIEW기능을 하는 엔티티를 하나 만들길  원했고 만들었지만 프로젝트 기간중 변경되는 요구사항이 상당했다.     

또한 실제 만들어 놓고 거의 쓰지 않는 경우가 생기면서 결국 해당 엔티티를 지우게 되었다.      

자 어째든 @Column의 속성들을 알아봤다.    


## @Enumerated    

일단 이름에서 딱 느낌이 온다. enum타입을 매핑할 수 있게 하는 그것!

속성으로는 다음과 같이 제공한다.

1. EnumType.ORDINAL: enum의 순서를 데이터베이스에 저장한다. 명시하지 않으면 이것이 기본으로 들어간다.

2. EnumType.STRING: enum 이름을 데이터베이스에 저장한다.


근데 우리는 왜 enum을 쓸까?     

일단 이 이야기는 JPA와는 상관없다. 쓰는 이유는 어떤 기준이 없으면 특정 필드, 예를 들면 남녀성별을 담는 컬럼이라든가 이런 컬럼에 약속이 없으면 중구난방으로 들어갈 수 있는 소지가 있다.    

어떤 개발자는 man/woman, 어떤 개발자는 male/female, 어떤 개발자는 0/1(음....) 이렇게 세팅할지도 모른다.    

물론 실무에서는 그럴리 없겠지만 타입을 정하게 되면 엄격하게 이런 코드들을 관리할 수 있고 휴먼 에러를 방지하게 된다.    

자 그럼 이렇게 하기 위해서는 Item을 기준으로 계속 진행해 보자.    

회사에서 상품의 속성중 뱃지라는 컬럼이 있다. 이 뱃지라는 것은 일종의 상품이 리스트에 들어올 때 신상인지 베스트 상품인지 MD추천 상품인지 해외병행상품인지 표현하는 컬럼이다.    

아쉽게도 회사에서는 이것을 그냥 0,1,2,3 이렇게 지정해 놓고선 테이블에 코멘트를 달아놨다. 결국 내 입장에서는 뭔가 새로 추가되거나 할 때마다 이 테이블에 주석을 추가하거나 비지니스 로직을 짤 때 테이블을 참조해야 한다. 불편하다는 의미이다.    

그래서 여기서는 '내가 만일 테이블을 설계한다면 tinyint(1)타입이 아닌 스트링 타입으로 받을 수 있게 하고 인서트를 하거나 업데이트를 할 때 enum을 통해서 어떤 걸 넣을지 엄격하게 관리해서 잘못된 값이 들어가지 못하게 하겠다.

그럼 Badge 또는 BadgeType(Code) enum을 하나 만들자. 개발자마다 다르겠지만 나의 경우에는 Type이나 Code를 붙이는 것을 선호한다. 이유는 클래스 명에서 명확하게 판단하기 위해서이다.

```

package io.basquiat.model;

public enum BadgeType {

	BEST,
	MD,
	OVERSEA;
	
}


```

더 많은 뱃지가 있지만 일단 저렇게 4개를 넣어둔다.     

그리고 Item을 수정하자.


```
package io.basquiat.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_item")
//@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	//@Column(name = "it_name", insertable = false)
	//@Column(name = "it_name", updatable = false)
	@Column(name = "it_name", columnDefinition = "varchar(20) not null")
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;

	@Column(name = "test", precision = 10, scale = 8)
	private BigDecimal test;
	
	@Column(name = "badge")
	private BadgeType badge;
	
	
	@Builder
	public Item(String name, Integer price, BadgeType badge) {
		this.price = price;
		this.name = name;
		this.badge = badge;
	}


	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", price=" + price + ", badge=" + badge + "]";
	}
	
	
}


```

백문이 불여일타!!

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.BadgeType;
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
        	
        	Item item = Item.builder().name("Fodera").price(15000000).badge(BadgeType.OVERSEA).build();
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

쿼리 당연히 날아가고~

```
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (badge, it_name, it_price, test) 
        values
            (?, ?, ?, ?)
            
db에서 확인해보면    
id  badge	    name     price    test
1	OVERSEA	Fodera  	15000000	null

```

조슈아 블로크의 이펙티브 자바를 보면 enum타입의 경우에는  ordinal()를 가급적 사용하지 말라고 권고한다.     

이것은 JPA에서도 마찬가지인데 기본으로 설정된 EnumType.ORDINAL 사용을 사용하지 말라고 권고한다. ~~가급적이라도 사용하지 마란 마이야!~~     

이유가 뭘까?    

일단 코드로 한번 테스트해보자. Item의 badge필드에 붙은 녀석을 @Enumerated(EnumType.ORDINAL)이렇게 수정하든가 아니면 기본값이 ORDIANL이니  @Enumerated만 붙이고 기존 코드를 그대로 실행해 보자.     

DB에서 체크를 해보면 인덱스로 따지기 때문에 OVERSEA의 경우는 인덱스가 2이기 때문에 3이라는 값이 들어가 있을 것이다.     

그리고 요청에 의해서 이 뱃지 타입이 늘었다고 생각해 보자. 일단 나의 경우에는 BadgeType의 나열된 값을 알파벳 순서대로 해놨다.     

만일 이런 규칙을 가지고 개발하는 개발자의 경우라면 요구에 의해서 새로 입고된 신상품이라는 뱃지를 달고 싶어진 것이다. 그래서 NEW라고 추가를 하나 한다.     

자 그러면 이런 식이겠지?

```
BEST,
MD,
NEW,
OVERSEA;

```

자 그럼 이제는 실행 코드를 다음과 같이 실행하자.     


```

package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.BadgeType;
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
        	
        	Item item = Item.builder().name("Fodera").price(15000000).badge(BadgeType.NEW).build();
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

이제부터 난리가 나는 것이다. 기존의 OVERSEA의 인덱스가 2였는데 이제는 NEW가 인덱스 2로 바꼈다.     

그러면 이전에 OVERSEA였던 녀석은 이제 이 상품이 OVERSEA인지 NEW인지 모르게 되고 그냥 데이터 상으로는 NEW가 되버리는 상황이 바뀌게 된다.     

테이블의 컬럼 변경시점을 기준으로 기존의 상품 데이터의 업데이트 날짜를 기준으로 이 값들을 싸그리 바꿔야 하는 무시무시한 일이 발생하게 된다.

물론 신규 추가되는 코드값은 그 뒤로 붙이면 문제가 없을 것이다.    

하지만 휴먼 에러는 예상치 않은 곳에서 발생한다! 왠간하면 STRING을 쓰자.

## 번외 @Convert    

하지만 실무에서는 저렇게 단순하게 사용하지 않는다. 또는 테이블을 변경할 수 없고 기존의 코드를 활용해야 하는 경우가 생긴다면 여지없이 ORDINAL을 써야 하는 상황에 직면하는데 그것을 해결해 주기위해서는 조치가 필요한 것이다. 

그러기위해서는 @Convert를 활용하는 방법이 있다.

일단 우리는 AttributeConverter를 구현하는 특별한 Converter를 만들어야 한다.

그 전에 우리는 BadgeType클래스를 손 좀 봐줘야 한다.

그 전에 관련 github    

[enum을 알아보자!](https://github.com/basquiat78/do-you-know-enum)

회사의 예를 들고 있어서 회사에서는 이런 타입의 컬럼에는 0을 쓰지 않는다. 그래서 인덱스 부분은 1부터 시작하게 설정했다. 

물론 테스트에서도 원하는 방향으로 가는지 확인하기 위해서이기도 하다.    

```
package io.basquiat.model;

import java.util.Arrays;

public enum BadgeType {

	BEST("BEST", 1),
	MD("MD", 2),
	NEW("NEW", 3),
	OVERSEA("OVERSEA", 4);
	
	/** enum code */
	private String code;

	/** enum index */
	private int index;
	
	public int index() {
		return this.index;
	}
	
	public String code() {
		return this.code;
	}
	
	BadgeType(String code, int index) {
		this.index = index;
		this.code = code;
	}
	
	/**
	 * get Enum Object from dbdata
	 * @param dbData
	 * @return BadgeType
	 */
	public static BadgeType indexFromDB(int dbData) {
		return Arrays.stream(BadgeType.values())
					 .filter( badgeType -> badgeType.index() == dbData )
					 .findAny()
					 .orElseThrow(() -> new RuntimeException("없는 녀석이야!"));
    }
	
}

```

일단 오류 처리부분도 손을 보긴 해야한다. 예를 들면 어떤 에러에 대한 Exception을 따로 만들고 처리하는 방법을 선택해야하는데 여기선 일단 테스트이니 그냥 런타입에러로 보내버리자.     

기존 코드와 다른 점은 코드값과 손수 정해온 인덱스를 가져오는 get함수를 만들었다. 그리고 디비에서 넘어오는 정보를 즉, 1,2,3 같은 정보들을 통해서 BadgeType을 던지는 녀석을 하나 만들었다.    

왜 만들었는지는 이제 만들 BadgeConverter에서 알 수 있게 될것이다.     


```

package io.basquiat.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 
 * created by basquiat
 *
 *
 */
@Converter
public class BadgeConverter implements AttributeConverter<BadgeType, Integer> {

	/**
	 * 엔티티에서 디비로 보낼 때 BadgeType에 정의한 인덱스 정보를 가져와서 세팅한다.
	 */
	public Integer convertToDatabaseColumn(BadgeType badgeType) {
		return badgeType.index();
	}

	/**
	 * DB에 있는 인트 타입의 정보를 Enum으로 반환하는 코드이다.
	 */
	public BadgeType convertToEntityAttribute(Integer dbData) {
		return BadgeType.indexFromDB(dbData);
	}

}

```

javax패키지에서 제공하는 AttributeConverter을 구현하면 된다. 뒤에 붙는 파라미터는 코드를 보면 알 수 있다. 이 코드 예제는 실무에서는 바뀔것이다. 상황에 따라서 말이다.    

오버라이딩하는 메소드명을 보면 명확하지 않는가?     

convertToDatabaseColumn -> DB로 보낼때 Integer로 바꿔서 보낸다.     

convertToEntityAttribute -> DB에 있는 정보를 통해 해당 enum을 반환한다.     

이게 전부이다.     

그리고 Item도 변경해 주자.

```
@Convert(converter = BadgeConverter.class)
@Setter
@Getter
private BadgeType badge;

```

보면 알겠지만 

이렇게 해두면 기존의 짜놓은 로직을 변경하지 않아도 된다.    

그럼 백문이 불여일타     

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.BadgeType;
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
        	
        	Item item = Item.builder().name("Fodera").price(15000000).badge(BadgeType.NEW).build();
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

기존 코드 그대로 활용해서 결과를 보면 db에 해당 badge컬럼은 int type으로 그리고 NEW의 인덱스 값인 3이 들어간 것을 볼 수 있다.    

그럼 옵션을 none으로 바꿔서 셀렉트도 해보자.     

```

package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.BadgeType;
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
        	
        	//Item item = Item.builder().name("Fodera").price(15000000).badge(BadgeType.NEW).build();
        	//em.persist(item);
        	Item item = em.find(Item.class, 1L);
        	System.out.println(item.toString());
        	
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
Item [id=1, name=Fodera, price=15000000, badge=NEW]
```

또 다른 방법.     

이것은 해당 필드에 getter/setter외에는 어노테이션을 붙이지 않는 경우이다.

```
@Setter
@Getter
private BadgeType badge;
```

위에서 처럼 @Convert를 쓰지 않고 않고 BadgeConverter의 달려있는 어노테이션을 밑에서처럼 바꿔보자. 

```
@Converter -> @Converter(autoApply = true)

```

위에서처럼 속성값으로 autoApply = true를 달아놓으면 AttributeConverter<BadgeType, Integer>에서 알 수 있듯이 어디에 적용해야하는지를 알아서 찾아서 컨버터를 실행한다.    

테이블과 비지니스 로직등 여러 상황에 따라서 커스터마이징하면서 사용하게 되면 괜찮은 방법이다.    

아무튼 이런 방식도 있음을 알아봤는데 이런 방식을 사용하게 될 경우, 만일 어플리케이션이 거대해져서 이런 컨버터가 많아질 경우를 생각해 볼 수도 있다.    

물론 내 경험에는 없었다. 그리 많이 만들 이유가 없었기도 했었고 아무튼 이럴 경우에는 interface를 활용해 구현하는 방법도 있다. 어짜피 enum역시 클래스이기 때문인데 여기서는 그 범위를 벗어나기 때문에 여러분들이 찾아서 구현해 보길 바란다.    

차 후 시간이 된다면 이 브랜치에 업데이트를 할 것이다.     

## @Temporal     

일단 이녀석은 어떤 속성을 가질 수 있나 먼저 살펴보자.    

1. TemporalType.DATE: year, month, day형식으로 매핑하며 다음과 같다. 2020-07-05    

2. TemporalType.TIME: time형식으로 매핑하며 형태는 다음과 같다. 12:00:00    

3. TemporalType.TIMESTAMP: timestamp형식으로  형태는 다음과 같다. 2020–07–05 12:00:00 

Date타입의 필드에 이 어노테이션을 명시하지 않으면 기본적으로 @Temporal(TemporalType.TIMESTAMP)이 붙게 된다.         

상황에 따라 다르겠지만 단순하게 그냥 그 당시의 날짜만을 사용할 것이라면 TemporalType.DATE, 로그를 위해서 정확한 시간대를 모두 넣겠다면 TemporalType.TIMESTAMP를 쓸 것이다. TemporalType.TIME은 잘 쓰질 않아서 모르겠지만 시간대가 필요하면 고려해 볼 수 있는 옵션이다.    

하지만 대부분 우리는 java8을 쓸 것이다. ~~아직도 자바 7쓰는데가 있옹?????~~

따라서 우리는 좀 더 좋아진 LocalDate를 쓸 일이 많을 텐데 LocalDate, LocalDateTime의 경우에는 이 어노테이션을 사용하지 않아도 된다.

이것은 생성쿼리에서 그 차이점만 확인하고 넘어갈 것이다.

현재 Item에 다음을 추가해 보자.

```
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	private LocalDate testAt;

```

이에 따른 차이점이 뭘까 생성 쿼리를 보면 알 수 있다.     

```
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        badge integer,
        createdAt datetime,
        it_name varchar(20) not null,
        it_price integer,
        test decimal(10,8),
        testAt date,
        updatedAt datetime,
        primary key (id)
    ) engine=InnoDB

```
LocalDateTime의 경우에는 datetime으로 LocalDate는 date로 생성되는 것을 볼 수 있다.     

[mysql datetime, date, timestamp](https://blog.naver.com/nieah914/221810697040)     

링크로 차이점을 풀어놓은 글이 있어서 링크로 걸어둔다.

근데 다음으로 넘어가기 전에 이와 관련된 번외편이 또 있다.     

일반적으로 우리가 insert/update쿼리를 날릴 때 쿼리가 실행된 시점의 시간대를 설정하는 것이 보통이다. 하지만 이것을 DB에 위임해서 우리는 신경쓰지 않아도 알아서 DB에서 해주게 할 수 있는 방법이 있다.     

물론 테이블 생성시 컬럼에 옵션을 줘서 생성할 수 있다. 예를 들면 columnDefinition을 이용한다든가 하는 방식을 말이다.     

mysql의 경우에는 다음과 같은 옵션을 줘서 컬럼을 생성하면 되지만 당연히 mySql 5.6버전 이후부터 가능하다.     

```

create table basquiat_item (

id bigint not null auto_increment,
createdAt datetime DEFAULT CURRENT_TIMESTAMP,
updatedAt datetime ON UPDATE CURRENT_TIMESTAMP,
.
.
.

)


```

당연히 mySql은 그렇고 오라클은 좀 다른거 같은데 암튼 이런 방식으로 처리할 수 있지만 JPA, 즉 어플리케이션 영역에서도 저렇게 할 수 있도록 해보자.    


```

package io.basquiat.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_item")
//@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	@Column(name = "it_name", columnDefinition = "varchar(20) not null")
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;

	@Column(name = "test", precision = 10, scale = 8)
	private BigDecimal test;
	
	@Setter
	@Getter
	private BadgeType badge;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	@Builder
	public Item(String name, Integer price, BadgeType badge) {
		this.price = price;
		this.name = name;
		this.badge = badge;
	}


	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpCreatedAt() {
    	createdAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdate() {
    	updatedAt = LocalDateTime.now();
    }

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", price=" + price + ", test=" + test + ", badge=" + badge
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
}



```

바로 @PrePersist/@PreUpdate 어노테이션 활용법이다.     

이렇게 하고 다음과 같이 

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.BadgeType;
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
        	
        	Item item = Item.builder().name("Fodera").price(15000000).badge(BadgeType.NEW).build();
        	em.persist(item);
        	em.flush();
        	em.clear();
        	
        	Item selected = em.find(Item.class, 1L);
        	System.out.println(selected.toString());
        	selected.setBadge(BadgeType.BEST);
        	em.flush();
        	
        	Item selected1 = em.find(Item.class, 1L);
        	System.out.println(selected1.toString());
        	
        	
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

실행을 하게 되면 

```
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (badge, createdAt, it_name, it_price, test, updatedAt) 
        values
            (?, ?, ?, ?, ?, ?)
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.badge as badge2_0_0_,
        item0_.createdAt as createda3_0_0_,
        item0_.it_name as it_name4_0_0_,
        item0_.it_price as it_price5_0_0_,
        item0_.test as test6_0_0_,
        item0_.updatedAt as updateda7_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=1, name=Fodera, price=15000000, test=null, badge=NEW, createdAt=2020-07-05T21:52:36, updatedAt=null]
Hibernate: 
    /* update
        io.basquiat.model.Item */ update
            basquiat_item 
        set
            badge=?,
            createdAt=?,
            it_name=?,
            it_price=?,
            test=?,
            updatedAt=? 
        where
            id=?
Item [id=1, name=Fodera, price=15000000, test=null, badge=BEST, createdAt=2020-07-05T21:52:36, updatedAt=2020-07-05T21:52:35.687]

````

실행 코드에 순서대로 최초 인서트시에는 createdAt이 그 시간에 맞춰 생성되었고 그 이후 dirty checking을 통해서 업데이트 하게 되면 업데이트한 시간을 자동으로 해주게 되는 것을 볼 수 있다.    

이것은 아까 위에서 언급했든 오라클, mysql마다 특성이 다른데 이런 것을 통해서 우리는 어떤 db이든 원하는 방식으로 구현할 수 있게 된다.    

물론 이 조합을 통해 하나의 컬럼에 인서트, 업데이트시 동작할 수 있게 만들 수 있다. 그것은 어디까지나 상황에 맞춰서 사용하면 되는 것이다.

## @Lob     

이것은 필드에 따라 자동으로 매핑해 주는 녀석이다.    

보통 CLOB, BLOB과 관련되는데 mySql은 text속성으로 들어가게 된다.

BLOB은 나도 그렇게 많이 본적은 없다. 게다가 요즘은 mySql만 써와서 보통 CLOB을 많이 쓰게 되는데 예를 들면 데이터의 로그성 데이터를 json스트링 형식으로 넣는 경우가 있다.    

하지만 json스트링의 형식의 길이는 상황에 따라서 엄청난 길이를 가질 수 있는데 (상품의 옵션이 무지막지한 경우들이 좀 있어서) 이런 경우에는 일반적인 varchar타입으로는 넣을 수가 없다. 그래서 만일 로그성의 정보를 넣게 된다면 @Lob으로 매핑해서 넣으면 된다.    

ItemLog.java

```
package io.basquiat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "basquiat_item_log")
public class ItemLog {

	@Id
	private String id;

	@Lob
	private String log;
	
	@Column(name = "changer")
	private String changeUserId;
	
}


```

내부에서는 상품의 변경이 생길 경우, 특히 가격이나 옵션 변경의 경우에는 로그 테이블로 따로 변경 정보를 넣게 된다.     

단순하게 상품 코드와 변경한 사용자의 아이디, 그리고 가격 변동 또는 옵션 변경 내역을 담은 json스트링을 담는 엔티티이다.     

```

Hibernate: 
    
    create table basquiat_item_log (
       id varchar(255) not null,
        changer varchar(255),
        log longtext,
        primary key (id)
    ) engine=InnoDB

```

DDL생성한 부분을 보면 @Lob가 붙은 필드는 clob에 해당하는 longtext로 만들어진 것을 알 수 있다. 사실 text타입도 long, medium, tiny가 있는데 가장 큰 longtext로 생성하는 듯 싶다.    


## @Transient     

자바에는 transient라는 키워드가 있다. 보통은 어떤 객체를 serialize/deserialize과정에서 제외할 변수에 선언하는 키워드인데 JPA에서도 이것을 지원한다.     

그렇다면 이것은 언제 쓰지? 라는 의문을 가지게 되는데 예전에 회원 가입시에 비번을 입력하고 비번을 한번 더 확인하는 부분에서 해당 필드에 이것을 걸어 둔것을 본 적이 있다.     

일단 프론트 엔트에서 스크립트로 한번 처리를 했다고 하더라도 서버단에서 한번 더 체크를 해야 하는 경우가 있는데 (당연한것이다. 왜냐하면 매크로같은 것으로 인해서 이 부분을 강제로 패스할 수도 있기 때문이다. 일명 captcha같은 경우에도 서버에서 한번 더 체크한다.) 이 필드에 걸어두었다가 체크할 때 쓰고 그 이후에는 사용할 일이 없는 필드이고 디비에도 저장할 필요가 없기 때문이라는 것이다.     

그 때 나는 단지 스펙상으로만 있던 이 어노테이션에 대해서 다시 한번 생각을 하게 되었다.     

이것은 단지 메모리상에서 어떤 비지니스 로직에는 필요하지만 디비에서 관리할 필요가 없는 temp성 필드에 걸어두고 사용하면 된다.     


## At A Glance     

다음 브랜치에는 키매핑 전략에 대해 설명할 예정이다. 아마 이전 브랜치들에서 어느 정도 설명했놨기 때문에 디테일한 부분의 설명만 더하고 끝낼 예정이다.     