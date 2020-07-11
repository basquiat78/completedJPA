# Primary Key Mapping    

이제는 기본 키 매핑에 대해서 알아보고자 한다.     

이미 이전 브랜치에서 맛을 보긴 했지만 테이블 전략은 좀 부족한 감이 있다.     

아무래도 mySql기준으로 sequence전략을 선택시 자동으로 테이블 전략을 선택하는 방식이기 때문이다.     

암튼 이제 하나씩 세세하게 해보자.    

## @Id    

가장 기본적인 방법으로 이 경우에는 직접적으로 pk를 넣어주는 방법이다.     

예를 들면 우리가 책을 사게 되면 ISBN이라고 해서 International Standard Book Number(국제 표준 도서 번호) 같은 것을 보게 된다. 이것은 해당 서적의 고유한 번호를 국제 표준 도서 번호에 맞춰서 생성하고 붙이게 된다.     

유니크한 값이라는 의미이다. 만일 저렇게 생성해서 직접적으로 할당하게 되는 경우에는 그냥 어떤 옵션을 주지 않고 저렇게만 걸어두면 된다.

이 방식은 그냥 테스트 코드 없이 입코딩으로만 진행하겠다.      

근데 서적의 예제나 관련 경험 이야기들을 들어보면 이 부분은 상당히 고심을 해야 하는 부분이다.     

왜냐하면 주민번호같은 경우가 그 예이다.    

이와 관련된 글들과 여러 커뮤니티에 보면 사례글들이 참 많은데 이유는 정보보호법으로 인해 주민번호를 보관하면 안되기 때문이다.    

또는 복호화할수 없는 방식으로 암호화해서 저장하는 방식으로 변경해야 한다.    

실제로 첫 직장에서 모 대기업 그룹웨어 관련 작업하면서 이런 경우를 봤는데 다행이도 거기에선 이것을 pk로 설정하지 않아서 큰 문제가 없었지만 이것을 pk로 잡은 경우 어마어마한 마이그레이션 비용을 들였다는 이야기들은 이제 ~~전설은 아니고~~ 레전드가 되버리긴 했다.    

따라서 일반적으로 mySql의 경우에는 제공하는 auto_increment옵션으로 자동 생성하거나 오라클같은 경우에는 sequence생성해서 pk를 따와서 사용하는 경우가 일반적이다.     

현재 재직중인 회사는 상품 코드의 경우에는 코드를 젠해서 사용한다.    

## IDENTITY Strategy    

위에서 언급했듯이 데이터베이스에 pK를 따는 것을 위임하는 방식을 의미한다.    

내 경험상 많은 DB(H2, HSQL제외)를 사용해 보진 못했다. mySQL, mariaDB, oracle, DB2, sybase(SAP관련 프로젝트로 딱 한번 사용), postGreSQL정도인데 그럼 이것을 지원하는 DB가 뭔지 어느정도는 알아야겠다.     

내가 알고 있는 녀석은 mySql, mariaDB(뭐 이건 당연한건가?), IBM에서 만든 DB2 (이넘에 대한 안좋은 기억이....), postGres(이넘은 둘다 지원한다.)정도이다. 더 있겠지만 뭐...     

postgres는 Serial 타입으로 컬럼을 생성하게 된다.    

일단 엔티티에는 다음과 같이    

```

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

```

일단 눈으로 확인해 보자.     

```
Hibernate: 
    
    create table basquiat_item (
       id  bigserial not null,
        badge int4,
        createdAt timestamp,
        it_name varchar(255),
        it_price int4,
        updatedAt timestamp,
        primary key (id)
    )

```

postgres는 bigserial로 생성한 것을 볼 수 있다. 아마도 차후의 사이즈를 고려해서 가장 큰 bigserial로 생성하는 듯 싶다.

하지만 실질적으론? 시퀀스 생성해서 한다는 것이다. basquiat_item_id_seq 요렇게 생겨먹은 시퀀스를 하나 생성하게 되는 것을 볼 수 있다.     

아마도 이름 명명규칙은 테이블 명에 _id_seq를 붙이는 형식인듯....     

하지만 mySql은? 일단 눈으로 보자.     

```
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        badge integer,
        createdAt datetime,
        it_name varchar(255),
        it_price integer,
        updatedAt datetime,
        primary key (id)
    ) engine=InnoDB


```

bigint타입으로 auto_increment옵션을 주고 있다는 것을 알 수 있다.    

앞서 설명했듯이 이렇게 DB에 위임을 한 경우이기 때문에 영속성 컨텍스트 입장에서는 당연하게도 DB에 꼽히지 않는다면 pk를 알수 가 없다.    

즉 영속성 컨텍스트 입장에서는 그것을 알기 위해서는 DB에 일단 넣어두고 pk를 가져와야 하는 것이다.    

단점으로 언급하는 점은 지연쓰기, 즉 버퍼링을 하고 쓰기하는 방법을 쓸 수 없다고 언급을 하고 있다.     

그러면서도 성능상 이점은 그렇게 크지 않다고 하니 이 방법이 상황에 맞다면 쓰는게 좋은 듯 싶다.     

언제나 이런 것들은 상황에 맞춰서 사용하는게 좋고 무리가 없다면 사용하는데 주저할 필요는 없는 거 같다.     


## SEQUENCE Strategy    

mySql은 시퀀스를 지원하지 않으니 나의 경우에는 postgres로 진행하겠다. (뭐 결국 IDENTITY도 시퀀스인 마당에) 만일 H2나 HSQL같은 라이트한 DB를 사용하는 분들이라면 그대로 진행해도 무방하다.     

지금까지 mySQL과 postgres를 깔아보시고 진행한 분들이라면 persistence.xml를 수정해서 postgres로 진행해 보자.    

대표적으로 오라클, DB2는 시퀀스를 지원하니 오라클이나 지원하는 DB가 깔려 있다면 그것으로 테스트해보면 되겠다.     

그럼 시퀀스가 뭐니?라는 의문이 드는데 대부분 검색을 해보면 오라클 오브젝트를 이야기한다.     

'데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트를 의미한다.'    

하지만 시퀀스를 지원하는 DB로 넓게 생각해도 된다. 시퀀스에는 특징이 하나 있는데 보통 롤백이 되는 경우가 생기면 이 시퀀스는 롤백 대상이 되지 않는다는 것이다.     

이 정도만 알고 지나가자.     

'그럼 위 말대로라면 결국 DB를 통해서 시퀀스를 조회해서 id를 가져오겠군요?'     

~~네 그렇읍니다.~~    

하지만 롤백의 타겟이 되지 않는 점과 유일한 id를 생성하기 때문에 시퀀스를 활용해서 pk를 사용하게 된다.     

자 그럼 우리는 이제부터 코드로 이야기 하자. 

일반적인 방식은 다음과 같이 설정하면 된다.

```

@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE)
private Long id;


```

가장 기본적인 방식이다. 그러면 이제 한번 실행을 해보자.

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

위 코드를 실행하게 되면   


```
Hibernate: 
    
    drop sequence if exists hibernate_sequence
Hibernate: create sequence hibernate_sequence start 1 increment 1
7월 06, 2020 11:47:05 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@3301500b] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_item (
       id int8 not null,
        badge int4,
        createdAt timestamp,
        it_name varchar(255),
        it_price int4,
        updatedAt timestamp,
        primary key (id)
    )
7월 06, 2020 11:47:05 오후 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    select
        nextval ('hibernate_sequence')
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (badge, createdAt, it_name, it_price, updatedAt, id) 
        values
            (?, ?, ?, ?, ?, ?)

```

위와 같이 쿼리가 날아가는 것을 알 수 있다. 그러면 이제 hibernate.hbm2ddl.auto 옵션을 none으로 하고 데이터를 하나 더 넣어보자.


```
Hibernate: 
    select
        nextval ('hibernate_sequence')
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (badge, createdAt, it_name, it_price, updatedAt, id) 
        values
            (?, ?, ?, ?, ?, ?)

```

시퀀스에서 pk를 가져오는 쿼리가 한번 날아가는 것을 확인할 수 있다.    

실제로 pgAdmin에서 시퀀스를 조회하면 2번의 데이터를 넣었으니 

```
select * from hibernate_sequence;

Data Output

last_value | log_cnt | is_called
2          | 32      | true

```

이렇게 마지막으로 가져간 pk가 2라는 것을 알 수 있다.     

흐름을 따져보면 최초에 0이 있었을 것이고 인서트 되는 순간 nextval을 통해서 1을 증가시켜서 pk로 가져오고 시퀀스에 1로 업데이트 할것이다.     

그리고 두 번째 데이터가 들어오면 1인 녀석을 1을 증가시켜서 pk로 2를 가져오고 시퀀스 정보에 2로 업데이트 하는 흐름이라는 것을 알 수 있다.     

사실 IDENTITY와 관련해서 그럼 위에서 postgres는 시퀀스인가요?     

궁금한건 못참는다.     

다시 IDENTITY로 바꿔보자.     

그리고 실행을 하게 되면    

```
Hibernate: 
    
    drop table if exists basquiat_item cascade
7월 06, 2020 11:58:39 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@6b85300e] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_item (
       id  bigserial not null,
        badge int4,
        createdAt timestamp,
        it_name varchar(255),
        it_price int4,
        updatedAt timestamp,
        primary key (id)
    )
7월 06, 2020 11:58:39 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@6058e535] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
7월 06, 2020 11:58:39 오후 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (badge, createdAt, it_name, it_price, updatedAt) 
        values
            (?, ?, ?, ?, ?)

```

음? 분명 디비를 보면 basquiat_item_id_seq라는 시퀀스가 생성된 것을 볼 수 있지만 실제로 생성 쿼리와 nextval을 하는 쿼리가 전혀 보이지 않는다.    

hibernate.hbm2ddl.auto 옵션을 none으로 하고 데이터를 하나 더 밀어넣어보자

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
        	System.out.println("sequence에서 뭔짓거리 하는거야?");
        	em.detach(item);
        	
        	Item selected = em.find(Item.class, 3L);
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


```

결과는??

```

7월 07, 2020 12:03:18 오전 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 07, 2020 12:03:18 오전 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 07, 2020 12:03:18 오전 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 07, 2020 12:03:19 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
7월 07, 2020 12:03:19 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [org.postgresql.Driver] at URL [jdbc:postgresql://localhost/basquiat]
7월 07, 2020 12:03:19 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=postgres, password=****}
7월 07, 2020 12:03:19 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 07, 2020 12:03:19 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 07, 2020 12:03:19 오전 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.PostgreSQLDialect
7월 07, 2020 12:03:20 오전 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (badge, createdAt, it_name, it_price, updatedAt) 
        values
            (?, ?, ?, ?, ?)
sequence에서 뭔짓거리 하는거야?
Hibernate: 
    select
        item0_.id as id1_0_0_,
        item0_.badge as badge2_0_0_,
        item0_.createdAt as createda3_0_0_,
        item0_.it_name as it_name4_0_0_,
        item0_.it_price as it_price5_0_0_,
        item0_.updatedAt as updateda6_0_0_ 
    from
        basquiat_item item0_ 
    where
        item0_.id=?
Item [id=3, name=Fodera, price=15000000, badge=NEW, createdAt=2020-07-07T00:03:20.712, updatedAt=null]
7월 07, 2020 12:03:20 오전 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    22100 nanoseconds spent acquiring 1 JDBC connections;
    19900 nanoseconds spent releasing 1 JDBC connections;
    498500 nanoseconds spent preparing 2 JDBC statements;
    2514500 nanoseconds spent executing 2 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    7501900 nanoseconds spent executing 2 flushes (flushing a total of 2 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 07, 2020 12:03:20 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:postgresql://localhost/basquiat]


```

오호...

하지만 pgAdmin4에서 

```
select * from basquiat_item_id_seq;

```
를 날리면 실제 시퀀스처럼 작동하고 있는 것을 확인 할 수 있다. 아마도 매커니즘이 다른듯 싶다.     

아무튼 이런 차이점이 있다는 것을 한번 확인하고 넘어간다.     

앞서서 영속성 컨텍스트에서 우리는 bulk와 관련된 테스트를 한 적이 있다. 당시에는 해당 @Id에 @GenericGenerator와 시퀀스 전략을 활용했는데 @SequenceGenerator을 활용하는 방법도 있다.    

@SequenceGenerator    

1. name: 식별자 생성기 이름으로 이것은 시퀀스 전략시 매핑될 이름이기 때문에 필수이다.    

2. sequenceName: 데이터베이스에 생성할 시퀀스의 이름으로 설정하지 않으면 기본값으로 hibernate_sequence이다.    

3. initialValue: 시퀀스 생성시 최초값으로 보통 1로 잡아둔다. 기본값 1이다.    

4. allocationSize: 시퀀스를 한 번 호출할때마다 증가하는 수로 일반적으로 1로 잡는다. 잡지 않으면 기본값이 50이다.    

5. catalog: catalog 이전 브랜치에서 설명한 것으로 db에 따라 설정할 수 있다.    

6. schema: schema 이전 브랜치에서 설명한 것으로 db에 따라 설정할 수 있다.    

자 그럼 설정 코드를 한번 보자.    

```
package io.basquiat.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
@Entity
@Table(name = "basquiat_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "basquiat_item_seq",
				   sequenceName = "item_sequence",
				   initialValue = 1, 
				   allocationSize = 1)
@ToString
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basquiat_item_seq")
	private Long id;

	@Setter
	@Getter
	@Column(name = "it_name")
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;

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
	
}

```

이 코드를 먼저 살펴보자.

```
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basquiat_item_seq")
private Long id;

```
GeneratedValue의 전략은 시퀀스이고 generator의 값이 basquiat_item_seq이다 이 generator의 값은 다음 코드를 보면 알 수 있다.

```

@SequenceGenerator(name = "basquiat_item_seq",
				   sequenceName = "item_sequence",
				   initialValue = 1, 
				   allocationSize = 1)

```

즉, SequenceGenerator의 이름과 같은 것을 알 수 있는데 바로 이와 매핑되게 된다.     

실제로 디비를 보면 sequenceName의 값으로 생성된 시퀀스를 확인 할 수 있으며 최초 1값으로 인서트 쿼리를 날릴 때마다 allocationSize에 설정한 수만큼 늘어나게 된다.    

특히 이것은 batch에서 최적화를 위해 사용되어지는데 지금까지 테스트해온 로그들을 분석해 보면 잘 알 수 있다.

배치를 하든 뭐를 하든 PK를 따기 위해서는 그만큼 디비를 왔다갔다하면서 시퀀스에서 아이디를 받아와야 한다.    

그 수가 빈번하면 성능저하를 우려할 수 있다. 당연한 이야기이다.     

그래서 그것을 한번에 미리 저 수만큼 받아서 메모리에서 사용하는 방식을 사용하는 것이다.     

이것을 입코딩으로 한번 설명해 보겠다.


```
나는 총 10개의 배치성 인서트 쿼리를 만들게 될거 같아. 그래서 10번 왔다 갔다 하는것보다는 일단 시퀀스에서 초기값을 보고 allocationSize만큼 증가시킨 이후에 그 증가된 아이디갯수만큼 내 메모리에 올려두겠어.


예를 들면 총 10개니깐 일단 시퀀스에 가서 확인해 보니 4네? 그럼 50을 더해서 5부터 50을 더해서 5~55만큼 확보를 하자구? 이해됬지?

그리고 총 10개니깐 5,6,7,8,9,10,11,12,13,14 뭐 이렇게 설정하지 뭐.

그리고 트랜잭션이 종료되면? 뭐 어째겠어 나머지는 그냥 날아가는거지.

```

단점이라고 한다면 id값이 중간에 붕 뜨는 그런 현상이 생길 수 있다.

```
id   badge		createdAt							name		price
303		3		"2020-07-09 01:42:12.803"	"Fodera"	15000000	
353		3		"2020-07-09 01:42:12.804"	"Fodera"	15000000	
354		3		"2020-07-09 01:43:12.26"		"Fodera"	15000000	
355		3		"2020-07-09 01:43:12.278"	"Fodera"	15000000	
356		3		"2020-07-09 01:43:12.278"	"Fodera"	15000000	
404		3		"2020-07-09 01:45:09.501"	"Fodera"	15000000	
405		3		"2020-07-09 01:45:09.525"	"Fodera"	15000000	
406		3		"2020-07-09 01:45:09.525"	"Fodera"	15000000	

```

그런데 아쉽게도 postgres에서는 그냥 설정만 하면 오류가 발생한다.

테스트로 allocationSize값을 1이 아닌 다른 값으로 변경하게 되면 다음과 같은 에러 로그를 마주하게 된다.     

```
Caused by: org.hibernate.MappingException: The increment size of the [item_sequence] sequence is set to [3] in the entity mapping while the associated database sequence increment size is [1].

```

기본적으로 1로 설정되어 있기 때문이다.    

그래서 postgres의 경우에는 해당 시퀀스를 alter를 통해서 해당 옵션을 변경시켜줘야 한다.

하지만 좀 불편한게 이 사이즈와 디비에 설정된 값이 다르면 이런 오류를 내기 때문에 설정 초기에 적절한 값 세팅을 해줘야 한다.

```

ALTER SEQUENCE item_sequence INCREMENT BY 50 (allocationSize you want);

```

위와 같이 해당 시퀀스의 increment의 값을 변경해주고 테스트해보시기 바란다.

사실 나는 이 방법도 불합리하다고 생각한다. 예를 들면 이런 방식을 채택할 수 있었을텐데 

pseudo code로 표현하자면

```
List<Entity> list = Arrays.asList(entity1, entity2, entity3)
em.persistBatch(list)

```

뭐 대충 저렇게 리스트로 받고 시퀀스에서 id 따올때 저 숫자만큼 증가시켜서 가져오면 참 좋지 않을까 하는? 뭐 그런 생각들... 뭐 문제가 있고 저 방식이 더 불합리해서 없는건지도 모르겠다.     

암튼 시퀀스 전략은 심플하기 때문에 간략하게 알아봤다.

## TABLE Strategy    

테이블 전략을 쓰는 이유는 단 하나이다. 일단 DB의 특성과는 상관없이 테이블을 생성해서 테이블을 대상으로 시퀀스를 가져오고 증가시키는 방식이다.    

말 그대로 장점이라고 하면 어떤 DB에서든 사용할 수 있으며 또한 여러개의 시퀀스를 생성하지도 않고 테이블 하나에 대상 테이블의 시퀀스를 가질 수 있다.     

내가 첫 입사한 회사에서는 바로 이 테이블 전략을 통해서 시퀀스를 생성했었고 그 안에는 무수한 테이블들의 시퀀스를 관리하는 방식이었다.    

이게 무슨말이냐면 만일 A라는 테이블과 B라는 테이블이 있다고 가정하자.

그리고 테이블을 하나 생성한다.    

대충 컬럼이 table_seq, next_val이라는 컬럼을 두어서 table_seq에서는 어떤 테이블의 시퀀스인지 값을 넣고 next_val을 관리하는 것이다.    

대충 a_seq, b_seq같은 느낌으로?    

일단 무엇이 있는지 살펴보자.     

###@TableGenerator

1. name: 식별자 생성기 이름으로 어느 generatValue에서 지정할 값이기 때문에 당연히 필수이다.    
    
2. table: 생성할 테이블명으로 설정하지 않으면 기본값이 hibernate_sequences이다.    

3. pkColumnName: 시퀀스 컬럼명이다. 설정하지 않으면 기본값으로 sequence_name이다.    

4. valueColumnName: 시퀀스를 담을 컬럼명이다. 설정하지 않으면 기본값으로 next_val이다.    

5. pkColumnValue: 키로 사용할 값으로 대략 {table name}_seq같은 형식을 지정한다. 설정하지 않으면 엔티티명으로 지정된다.    

6. initialValue: 시퀀스 생성시 최초값, 기본값은 0이다.    

7. allocationSize: 시퀀스 한 번 호출에 증가하는 수로 기본값이  50이다. 시퀀스 전략과 동일하다.     

8. catalog, schema: 이번 영속성 컨텍스트 관련 내용과 동일하다. DB특성에 따라 사용할 수 있다.        

9. uniqueConstraints: 유니크 제약 조건을 거는 것으로 DDL 생성시에만 관여하는 녀석이다.    


자 그럼 다음과 같이 세팅을 하자.

Item

```

package io.basquiat.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

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
@Entity
@Table(name = "basquiat_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@TableGenerator(
		 name = "seq_generator",
		 table = "sequence_table",
		 pkColumnValue = "item_seq", 
		 initialValue = 0,
		 allocationSize = 1) 
@ToString
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_generator")
	private Long id;

	@Setter
	@Getter
	@Column(name = "it_name")
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;

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
	
}


```

OtherItem

```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

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
@Entity
@Table(name = "basquiat_other_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@TableGenerator(
		 name = "other_seq_generator",
		 table = "sequence_table",
		 pkColumnValue = "other_item_seq", 
		 initialValue = 0,
		 allocationSize = 1) 
@ToString
public class OtherItem {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "other_seq_generator")
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


Item과 OtherItem을 테이블 전략으로 사용해 보자.     

그런데 독특한 점은 

```
Item.java

@TableGenerator(
		 name = "seq_generator",
		 table = "sequence_table",
		 pkColumnValue = "item_seq", 
		 initialValue = 0,
		 allocationSize = 1) 


OtherItem.java
@TableGenerator(
		 name = "other_seq_generator",
		 table = "sequence_table",
		 pkColumnValue = "other_item_seq", 
		 initialValue = 0,
		 allocationSize = 1) 

```

보면 table명이 같다. 이렇게 하면 하나의 테이블을 사용할 수 있다. 다만 각 테이블의 시퀀스 정보를 구분하기 위해서 pkColumnValue만 다르다.     

즉, pkColumnValue은 해당 엔티티가 id를 가져올 때 사용할 키값이라고 보면 된다.    

실제로 ddl을 생성하면 어떻게 될까?    

![실행이미지](https://github.com/basquiat78/completedJPA/blob/5.PrimaryKeyMapping/capture/capture1.png)     

하나의 테이블에서 각 테이블의 시퀀스를 관리하게 만들 수 있다.     

뭔가 좋아보인다. 테이블 하나에 전부 관리 할 수 있다니?     

하지만 이 방식은 아무래도 다른 전략에 비해 성능 문제가 발생할 소지가 있다.    

만일 여러개의 시퀀스를 저렇게 한 테이블에 관리하면 LOCK의 문제가 발생할 수 있다. 여러개의 테이블에서 동시에 시퀀스를 활당받기 위해 해당 테이블을 오가고 업데이트 하게 되면 어떻게 될까?     

요즘은 거의 쓰지 않는 전략인듯 싶다. 하지만 상황에 의해 꼭 써야할 수도 있어야 하기에 한번 쯤은 살펴보면 좋을 것 같다.    

# At A Glance     

간략하게 기본키 매핑 전략에 대해 알아봤다.     

사실 답은 없다.     

그리고 만일 직접 매핑 전략의 경우에는 대리키를 사용하라고 권장하고 있다.    

유일한 키 값의 조합을 권장하는데 어째든 이것은 상황에 따라서 언제든지 결정해서 사용할 수 있는 전략이다.    

참고로 회사에서는 대부분 Identity전략과 코드성의 기본키는 키를 제너레이터하는 로직을 활용해 매핑하는 방식을 채택하고 있다.    

다음에는 어쩌면 JPA의 꽃이라고 할 수 있는 연관관계 매핑을 진행해 보겠다.