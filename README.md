# 단방향 연관관계 매핑     

일단 이 브랜치를 진행하기 앞서 우리는 JPA를 공부하지만 맨 처음 브랜치에서 언급했듯이 JPA는  database위에 있는 기술이다.     

이게 무슨 말이냐면 최소한 DB에 대한 기본 개념은 알아야 한다는 것이다.     

특히 테이블관련 테이블간의 관계에 대해서 고민을 해봐야 한다는 것이다.     

그리고 DB에서 테이블간의 관계는 사실 별거 없다. primary key, forein key에 대한 최소한의 개념을 알아야 한다.    

물론 정규화된 테이블의 경우에도 그렇고 비정규화된 테이블 즉 pk와 fk가 잡혀있지 않아도 조인해서 가져 올 수 있는 관계있는 컬럼이 있다면 얼마든지 조인해서 정보를 가져올 수 있다.    

그리고 객체간의 연관 관계와 db에서의 테이블간의 관계에 대한 차이점을 어느 정도 인지를 해야 한다. 왜냐하면 결국 이러한 db의 테이블을 객체로 추상화한 것이 JPA기 때문이다.     

그리고 단방향 연간관계 매핑이라고 했는데 그러면 당연히 단방향이 있으면 양방향이 있다는 것을 JPA를 공부하신 분들이라면 아실 것이다.     

하지만 SQL을 보면 테이블간에는 단방향, 양방향이라는 개념이 성립되지 않는다. 단지 연관관계만이 존재한다.     

[The 3 Types of Relationships in Database Design](https://database.guide/the-3-types-of-relationships-in-database-design/)    

입으로 한번 털어보자.     

책에서도 그렇고 어느 블로그를 가더라도 가장 기본적인 예제가 Member와 Team을 예로 든다.    

우리는 좀 더 현실적인 방식으로 축구에서 Player와 Club으로 한번 생각해 보자.    

가령 손흥민은 현재 토트넘에 소속된 선수이다.   

정말 특별한 경우라서 '홍길동'이라는 사람이 여러 부서에 소속되는 경우가 아니라면 (실제 이런 경우를 대기업 프로젝트에서 본적이 있어서) 대부분은 하나의 부서에만 소속되어 있듯이 손흥민은 토트넘 소속이다.     

하지만 토트넘이라는 클럽은 어떤가?     

감독인 조세 무리뉴에서 해리 케인, 스티븐 베르흐윈, 위고 요리스등 다양한 선수들이 포함되어 있다.     

자 그러면 클럽입장에서 선수와의 관계는 어떤 관계일까?     

![실행이미지](https://github.com/basquiat78/completedJPA/blob/6.unary-relation-mapping/capture/capture1.png)    

위와 같을 것이다. 일단 위 erd를 그릴 때 내가 처음 사용해봐서 잘 못 사용하고 있다는 것을 알았는데 뭐 설명하기에는 무리가 없어서 그대로 사용한다.    

즉 Club과 Player는 Club ->(one-to-many)Player, Player -> (many-to-one) Club의 관계를 파악해야 한다.    

위의 관계를 말로 풀면 클럽은 여러명의 선수들이 모여있는 집단이기 때문에 one-to-many이고 수많은 선수들이 하나의 클럽에만 소속할 수 있기에 many-to-one의 relationship을 가지게 된다.    

다만 방향성이 없다는 것은 단순하게 쿼리로 그냥 생각해 봐도 알 수 있다.     

선수가 팀이 없을 수 없다는 가정하에 Inner join으로 한번 쿼리를 짜 보자.

```

SELECT p.*,
		 c.*
	FROM player p
	JOIN club c ON p.club_id = c.id

또는

SELECT c.*,
		 p.*
	FROM club c
	JOIN player p ON c.id = p.club_id

```

방향성이라기보다는 무엇을 중심으로 질의를 하느냐에 따른 선택지라는 것이다.     

사실 데이터베이스 중심에서 생각하다가 이것을 객체에 올려두고 생각하면 그 사이의 차이점이 존재한다.    

한번 코드로 살펴보자. 일단 위에 db관점에서 테이블을 중심으로 엔티티를 설계하면 다음과 같이 하게 된다.    


Player

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
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "player")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Player {

	@Builder
	public Player(String name, int age, String position, Long clubId) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.clubId = clubId;
	}

	/** 선수 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 선수 명 */
	@Getter
	private String name;
	
	/** 선수 나이 */
	@Getter
	private int age;

	/** 선수 포지션 */
	@Getter
	private String position;
	
	/** 선수가 속한 클럽 아이디 */
	@Getter
	@Column(name = "club_id")
	private Long clubId;
	
}


```

Club

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
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "club")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Club {

	@Builder
	public Club(String name, int ranking) {
		this.name = name;
		this.ranking = ranking;
	}

	/** 클럽 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 클럽 명 */
	@Getter
	private String name;
	
	/** 클럽 랭킹 순위 */
	@Getter
	private int ranking;
	
}


```

자 코드를 보면 확실히 테이블을 객체로 잘 매핑했다. 그럼 뭐가 문제일까?     

사실 문제는 없다. 일단 코드로 확인해보는게 최고다.    

여기서 foreign key를 가지고 있는 객체 또는 테이블은 Player이다. 

실제 상황에서의 관점으로 살펴보자.     

선수의 입장에서는 내가 경기를 뛰기 위해서는 무엇이 필요할까? 당연히 팁이 있어야 한다. 팀에 들어가야 하는 것이다.         

OOP는 이러한 현상들을 관찰하고 영역안으로 녹여내는것이 중요하다.     

자 그럼 코드는 어떻게 짜야 할까?    

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.Club;
import io.basquiat.model.Player;

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
        	Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
        											   .ranking(9)
        											   .build();
        	em.persist(tottenhamFootballClub);
        	System.out.println("기본키 매핑은 Identity다. 그래서 쿼리 날아감.");
        	
        	// 2. 손흥민이 토트넘으로 들어간다.
        	Player son = Player.builder().name("손흥민")
        								 .age(27)
        								 .position("Striker")
        								 .clubId(tottenhamFootballClub.getId())
        								 .build();
        	em.persist(son);
        	System.out.println("기본키 매핑은 Identity다. 그래서 쿼리 날아감.");
        	
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

코드 흐름도 문제가 없다. 토트넘이라는 클럽을 선정하고 그 클럽의 아이디를 가져와서 세팅을 하는 코드 자체는 문제가 없어 보인다.     

그러면 이제 조회를 해보자.    


```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.Club;
import io.basquiat.model.Player;

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
        	// 손흥민을 조회한다.
        	Player son = em.find(Player.class, 1L);
        	System.out.println(son.toString());
        	
        	// 손흥민의 클럽 아이디로 클럽을 조회한다.
        	Club sonsClub = em.find(Club.class, son.getClubId());
        	System.out.println(sonsClub.toString());
        	
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
7월 12, 2020 12:55:13 오전 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 12, 2020 12:55:13 오전 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 12, 2020 12:55:13 오전 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 12, 2020 12:55:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
7월 12, 2020 12:55:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 12, 2020 12:55:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 12, 2020 12:55:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 12, 2020 12:55:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 12, 2020 12:55:15 오전 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
7월 12, 2020 12:55:16 오전 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    select
        player0_.id as id1_3_0_,
        player0_.age as age2_3_0_,
        player0_.club_id as club_id3_3_0_,
        player0_.name as name4_3_0_,
        player0_.position as position5_3_0_ 
    from
        player player0_ 
    where
        player0_.id=?
Player(id=1, name=손흥민, age=27, position=Striker, clubId=1)
Hibernate: 
    select
        club0_.id as id1_2_0_,
        club0_.name as name2_2_0_,
        club0_.ranking as ranking3_2_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Club(id=1, name=Tottenham Hotspur Football Club, ranking=9)
7월 12, 2020 12:55:16 오전 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    580700 nanoseconds spent acquiring 1 JDBC connections;
    443100 nanoseconds spent releasing 1 JDBC connections;
    11827500 nanoseconds spent preparing 2 JDBC statements;
    2317700 nanoseconds spent executing 2 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    9383300 nanoseconds spent executing 1 flushes (flushing a total of 2 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 12, 2020 12:55:16 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
```

뭐가 문제인가요???? 코드도 깔끔한거 같고 조회도 잘 되었는데요?     

맞다. 문제가 없다. 하지만 뭔가가 불합리해 보인다. 만일 쿼리로 한다면 어떻게 할까?    

```
SELECT p.*,
	   c.name,
       c.ranking
	FROM player p
    INNER JOIN club c ON p.club_id = c.id
```

![실행이미지](https://github.com/basquiat78/completedJPA/blob/6.unary-relation-mapping/capture/capture2.png)    

그냥 한방 쿼리로 모든 것을 조회해 올 수 있다. 그런 면에서 저런 방식은 무언가가 불합리해 보인다는 것이다. 코드의 낭비도 있어 보인다.    

그냥 쿼리처럼 한번에 player를 조회할 때 클럽도 조회해 올 수 없는 거니?    

당연히 있다. 이제부터 그 방법을 배워 볼 것이다.    

책에서는 굳이 모든 것을 양방향으로 맺을 필요는 없다고 말한다. 필요하면 그때 가서 양방향 매핑을 해도 무방하다고 말하고 있기 때문에 일단 우리는 단방향을 먼저 고민해 보자.     

자 그럼 저 위의 예제를 중심으로 설명을 해보자.    

## @ManyToOne    

다대일 또는 N:1이라는 표현으로 가장 많이 사용되는 매핑이다.    

양방향을 염두해 둔다면 그 반대는 @OneToMany가 될 것이라는 것은 딱 봐도 알 수 있다.    

이제부터는 다음과 같은 방식으로 사용할 수 있다.    

Player

```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "player")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Player {

	@Builder
	public Player(String name, int age, String position, Club club) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.club = club;
	}

	/** 선수 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 선수 명 */
	private String name;
	
	/** 선수 나이 */
	private int age;

	/** 선수 포지션 */
	private String position;
	
	/** 선수가 속한 클럽 객체 */
	@ManyToOne
	@JoinColumn(name = "club_id")
	private Club club;
	
}
```

잘 보면 기존에는 클럽의 아이디를 필드로 가지고 있었는데 지금은 Club이라는 객체를 가지게 된다.    

특이점은 @JoinColumn이다. Club 엔티티가 정확하게 작성되어 있다면 Player입장에서는 Club의 pk를 알 수 있다.    

다시 위에 이미지인 Player와 Club의 erd 이미지를 다시 보게 되면 Player의 fk는 club_id로 잡혀있다. 바로 이 JoinColumn의 name은 Player테이블이 가지게 되는 club_id임을 알 수 있다.     

이 경우에는 @JoinColumn을 명시하지 않아도 기본값으로 설정된다.    

그럼 그냥 JPA를 통해 DDL을 생성하게 되면 어떻게 될까?     

DDL을 생성하면 erd와 똑같이 테이블이 생성된 것을 확인 할 수 있다.    

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.Club;
import io.basquiat.model.Player;

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
        	Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
        											   .ranking(9)
        											   .build();
        	em.persist(tottenhamFootballClub);
        	System.out.println("기본키 매핑은 Identity다. 그래서 쿼리 날아감.");
        	
        	// 2. 손흥민이 토트넘으로 들어간다.
        	Player son = Player.builder().name("손흥민")
        								 .age(27)
        								 .position("Striker")
        								 .club(tottenhamFootballClub)
        								 .build();
        	em.persist(son);
        	System.out.println("기본키 매핑은 Identity다. 그래서 쿼리 날아감.");
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

이제는 클럽의 아이디가 아닌 엔티티 객체 자체를 세팅해서 실행해 보자. 여기까지는 기존과 다를 바 없다. 단지 Club에서 아이디를 꺼내와서 세팅하는 것이 아닌 그냥 객체 자체를 세팅한다.    

그리고 이제 조회를 해보자.     

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.Player;

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
        	
        	Player son = em.find(Player.class, 1L);
        	System.out.println(son);
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

Club를 조회하는 코드가 없는데요?     

하지만 결과를 보면 어떻까?


```
Hibernate: 
    select
        player0_.id as id1_3_0_,
        player0_.age as age2_3_0_,
        player0_.club_id as club_id5_3_0_,
        player0_.name as name3_3_0_,
        player0_.position as position4_3_0_,
        club1_.id as id1_2_1_,
        club1_.name as name2_2_1_,
        club1_.ranking as ranking3_2_1_ 
    from
        player player0_ 
    left outer join
        club club1_ 
            on player0_.club_id=club1_.id 
    where
        player0_.id=?
Player(id=1, name=손흥민, age=27, position=Striker, club=Club(id=1, name=Tottenham Hotspur Football Club, ranking=9))
```

오호라? Club의 정보까지 한번에 가져왔다는 것을 알 수 있다.    

자 그럼 위에서 언급했던 @JoinColumn에 대해서 좀 알아보자.    

### @JoinColumn    

1. name: 매핑할 외래 키 이름이다. 보통은 명시적으로 쓰긴 한다. 하지만 설정을 하지 않게된다면 이것은 필드명 (club)_대상테이블(Club에 설정된 pk), 즉 club_id가 된다.     

2. referencedColumnName:  외래 키가 참조하는 대상 테이블의 컬럼명이라고 하는데 이것은 @EmbeddedId에서 언급할 것이다. 기본값으로는 참조하는 테이블의 기본키 컬럼명이다.     

3. foreignKey: 외래 키 제약조건을 직접 지정한다. DDL에만 관여하는 녀석으로 유니크 제약 조건처럼 값을 설정하지 않으면 랜덤키 조합 방식으로 자동생성된다.    

4. @Column의 속성값들 기본적으로 포함한다. (unique, nullable, insertable, updatable, columnDefinition, table)     


## @OneToMany     

개인적으로 이해가 되지 않는 JPA에서 제공하는 표준 스펙이다. 책에서도 잘 사용하지 않는다고 하는데 그 이유는 운영상에 문제가 있다는 것이다.    

사실 1:N라고 표현하지만 실제 erd는 손을 대지 않는다.     

사실 database입장에서는 외래키는 다쪽, 그러니깐 Player쪽에 있어야 한다.    

하지만 객체 입장에서는 역으로 생각해 볼 수 있다. 즉 Player입장에서는 클럽 정보를 알고 싶지 않은 경우를 의미한다.    

연관관계 대상이 변경되었기 때문에 엔티티를 다시 정의하자.    

Player

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
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "player")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Player {

	@Builder
	public Player(String name, int age, String position) {
		this.name = name;
		this.age = age;
		this.position = position;
	}

	/** 선수 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 선수 명 */
	private String name;
	
	/** 선수 나이 */
	private int age;

	/** 선수 포지션 */
	private String position;
	
}

```

Club

```
package io.basquiat.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "club")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Club {

	@Builder
	public Club(String name, int ranking, List<Player> players) {
		this.name = name;
		this.ranking = ranking;
		this.players = players;
	}

	/** 클럽 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 클럽 명 */
	private String name;
	
	/** 클럽 랭킹 순위 */
	private int ranking;
	
	@OneToMany
	@JoinColumn(name = "club_id")
	List<Player> players = new ArrayList<>();
	
}

```

좀 독특하게 Player쪽에는 fk가 전혀 없다. 그리고 Club쪽에 @OneToMany로 설정하고 @JoinColumn의 name을 club_id로 설정했다.     

특히 이 경우에는 @JoinColumn을 생략하면 조인 테이블을 생성하는 전략을 사용하게 된다. 일단 이거는 뒤에 가서 확인해 보고 이렇게 했는데도 테이블을 생성하면 기존의 erd와 똑같이 테이블이 생성된다.    

다만 이럴 경우는 관계가 역전되어진 상황이기 때문에 내부적으로 돌아가는 방식도 좀 특이하다.     

주절이 말로 하면 좀 어려우니 코드와 결과를 보고 고민해 보자.     

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.Club;
import io.basquiat.model.Player;

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
        	
        	// 1. 손흥민은 축구 선수이다.
        	Player son = Player.builder().name("손흥민")
					        			 .age(27)
					        			 .position("Striker")
					        			 .build();
        	em.persist(son);

        	// 2. 토트넘이 손흥민 선수를 영입했다. 
        	Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
        											   .ranking(9)
        											   .build();
        	tottenhamFootballClub.getPlayers().add(son);
        	em.persist(tottenhamFootballClub);
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

입으로 설명하자면 이것도 상당히 합리적인것처럼 보이긴 한다. 로직의 흐름 자체가 손흥민 선수를 토트넘이 손흥민을 영입한 모양새를 띄고 있기 때문이다.
    
하지만 토트넘이 손흥민을 영입하는 코드 자체가 뭔가 부자연스럽다.     

그럼 결과를 보자.

```
Hibernate: 
    /* insert io.basquiat.model.Player
        */ insert 
        into
            player
            (age, name, position) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.Club
        */ insert 
        into
            club
            (name, ranking) 
        values
            (?, ?)
Hibernate: 
    /* create one-to-many row io.basquiat.model.Club.players */ update
        player 
    set
        club_id=? 
    where
        id=?

```

'어라? 근데 업데이트는 뭐지?'라는 생각이 들게 된다.     

당연하게도 JPA는 테이블의 연관관계를 객체로 추상화하는 과정에서 약간은 좀 억지로 끼워 맞춘 듯한 느낌도 살짝 든다. 아무래도 그 테이블과 객체와의 간극이 분명이 존재하기 때문인데 그런 관점에서 보자면 이것은 어찌보면 필연적인 것이다.     

테이블의 erd의 모양새와는 좀 다르게 맵핑된 이 경우에는 손흥민를 클럽에서 영입을 했다. 그리고 나서 언론이나 신문사에 대대적으로 기사를 날릴 것이다.    

'토트넘! 손흥민 영입'     

그럼 이것을 객체의 관점이 아닌 데이터베이스의 관점에서 보자면 손흥민에 대한 선수 정보는 토트넘에 영입되기 전에는 '바이어 레버쿠젠'에 소속되어 있었기 때문에 Club입장에서는 손흥민을 영입했으니 손흥민의 소속팀 정보를 갱신해야 하는 것이다.    

따라서 1:N 매핑을 시도한 JPA에서는 이 간극을 좁히기 위해 결국 DB에 손흥민의 정보에서 club_id를 갱신하는 쿼리를 날릴 수 밖에 없다.   

결국 이 전략은 외래키가 대상 테이블에 존재하면서 생기는 단점으로 꼽는다.      

책에서는 그래서 N:1 맵핑을 중심으로 이런 부분은 양방향으로 풀라고 권장하고 있다.    

어째든 1:N에 대해서 알아봤다.    

## @OneToOne     

1:1 매핑에 대해서 알아보자.     

지금 예제를 이어가보자. 선수는 물리적인 정보들 외에 연봉 정보와 국적, 취미등 외적인 정보를 담는 일종의 PlayerInformation객체를 가질 수 있다.     

이런 경우라면 Player와 PlayerInformation의 관계는 1:1임을 알 수 있다.     

또는 선수가 사용할 수 있는 락커를 예로 들수도 있겠다.        

물론 한 선수가 1개 이상의 락커를 쓸 수도 있겠지만 일반적으로 선수 한명당 하나의 락커를 소유하게 된다.    

실무의 예를 들면 Item, 즉 상품의 기본적인 정보들 외에도 상품에 걸려있는 정책이나 배송정책들이 상품마다 다 다를 수 있기 때문에 Item_policy, Item_Delivery같은 1:1로 매핑되는 테이블이 존재하기도 한다.    

다시 우리는 예제로 돌아가서 가장 흔하게 예를 드는 락커에 포커스를 맞추자.    

그러면 이런 고민이 들 것이다. 외래키는 어디에 있어야 하는가?    

이 경우에는 락커에 선수 아이디를 외래키로 가질 수 있고 아니면 선수가 락커 아이디를 외래키로 가질 수 있다.     

이것은 비지니스에 따라 달라질 것이다.    

하지만 보통은 선수를 검색했을 때 락커의 정보, 클럽의 정보를 가져오는게 더 합리적이지 않을까? 물론 이 생각은 어플리케이션을 어떻게 해야하느냐에 따라서 달라질 것이다.     

보통은 DBA가 있다면 정할 수 있지만 만일 내가 해야 한다면 어떻게 할까? 내 입장에서는 선수쪽에 락커 아이디를 외래키로 있는게 더 나아 보인다.    

![실행이미지](https://github.com/basquiat78/completedJPA/blob/6.unary-relation-mapping/capture/capture3.png)    

하지만 이건 어디까지나 나의 생각이다. 선택지일뿐 답은 없을 뿐더러 차후 배울 양방향 매핑으로 걸어버리면 끝날 일이다. 그러나 뒤에서 이와 관련 설명을 다시 한번 해볼것이다.    

일단 Locker를 만들자.     

Locker

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
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "locker")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Locker {

	@Builder
	public Locker(String name, String position) {
		this.name = name;
		this.position = position;
	}
	
	/** 락커 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 락커 이름 */
	private String name;

	/** 락커가 있는 위치 정보 */
	private String position;
}

```

Player

```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "player")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Player {

	@Builder
	public Player(String name, int age, String position, Club club, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.club = club;
		this.locker = locker;
	}

	/** 선수 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 선수 명 */
	private String name;
	
	/** 선수 나이 */
	private int age;

	/** 선수 포지션 */
	private String position;
	
	@ManyToOne
	@JoinColumn(name = "club_id")
	private Club club;
	
	@OneToOne
	@JoinColumn(name = "locker_id")
	private Locker locker;
	
}

```

그럼 한번 코드를 실행해 보자.

```

package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.Club;
import io.basquiat.model.Locker;
import io.basquiat.model.Player;

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
        	Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
        											   .ranking(9)
        											   .build();
        	em.persist(tottenhamFootballClub);

        	Locker sonsLocker = Locker.builder().name("손흥민의 락커")
        										.position("입구에서 4번째 위치")
        										.build();
        	em.persist(sonsLocker);
        	
        	// 2. 손흥민이 토트넘으로 들어간다.
        	Player son = Player.builder().name("손흥민")
        								 .age(27)
        								 .position("Striker")
        								 .club(tottenhamFootballClub)
        								 .locker(sonsLocker)
        								 .build();
        	em.persist(son);
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

결과는 뭐 안봐도 비디오    

```
7월 13, 2020 12:23:12 오전 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 13, 2020 12:23:12 오전 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 13, 2020 12:23:13 오전 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 13, 2020 12:23:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
7월 13, 2020 12:23:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 13, 2020 12:23:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 13, 2020 12:23:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 13, 2020 12:23:14 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 13, 2020 12:23:15 오전 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    alter table player 
       drop 
       foreign key FKh60stqlv4r5dk5hp5gcwvo0n7
7월 13, 2020 12:23:16 오전 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@28952dea] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    alter table player 
       drop 
       foreign key FKdh2ff6dcjjgupccm2pmddouhw
Hibernate: 
    
    drop table if exists club
Hibernate: 
    
    drop table if exists locker
Hibernate: 
    
    drop table if exists player
Hibernate: 
    
    create table club (
       id bigint not null auto_increment,
        name varchar(255),
        ranking integer not null,
        primary key (id)
    ) engine=InnoDB
7월 13, 2020 12:23:16 오전 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@9f6e406] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table locker (
       id bigint not null auto_increment,
        name varchar(255),
        position varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table player (
       id bigint not null auto_increment,
        age integer not null,
        name varchar(255),
        position varchar(255),
        club_id bigint,
        locker_id bigint,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    alter table player 
       add constraint FKh60stqlv4r5dk5hp5gcwvo0n7 
       foreign key (club_id) 
       references club (id)
Hibernate: 
    
    alter table player 
       add constraint FKdh2ff6dcjjgupccm2pmddouhw 
       foreign key (locker_id) 
       references locker (id)
7월 13, 2020 12:23:16 오전 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Club
        */ insert 
        into
            club
            (name, ranking) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.model.Locker
        */ insert 
        into
            locker
            (name, position) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.model.Player
        */ insert 
        into
            player
            (age, club_id, locker_id, name, position) 
        values
            (?, ?, ?, ?, ?)
7월 13, 2020 12:23:16 오전 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    426000 nanoseconds spent acquiring 1 JDBC connections;
    413700 nanoseconds spent releasing 1 JDBC connections;
    15898000 nanoseconds spent preparing 3 JDBC statements;
    6655000 nanoseconds spent executing 3 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    7436200 nanoseconds spent executing 1 flushes (flushing a total of 3 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 13, 2020 12:23:16 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]

```

옵션을 none으로 두고 셀렉트를 한번 해보게 되면

```
Player son = em.find(Player.class, 1L);
System.out.println(son.toString());

```

결과는 

```
7월 13, 2020 12:25:25 오전 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 13, 2020 12:25:25 오전 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 13, 2020 12:25:25 오전 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 13, 2020 12:25:26 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
7월 13, 2020 12:25:26 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 13, 2020 12:25:26 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 13, 2020 12:25:26 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 13, 2020 12:25:26 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 13, 2020 12:25:27 오전 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
7월 13, 2020 12:25:28 오전 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    select
        player0_.id as id1_2_0_,
        player0_.age as age2_2_0_,
        player0_.club_id as club_id5_2_0_,
        player0_.locker_id as locker_i6_2_0_,
        player0_.name as name3_2_0_,
        player0_.position as position4_2_0_,
        club1_.id as id1_0_1_,
        club1_.name as name2_0_1_,
        club1_.ranking as ranking3_0_1_,
        locker2_.id as id1_1_2_,
        locker2_.name as name2_1_2_,
        locker2_.position as position3_1_2_ 
    from
        player player0_ 
    left outer join
        club club1_ 
            on player0_.club_id=club1_.id 
    left outer join
        locker locker2_ 
            on player0_.locker_id=locker2_.id 
    where
        player0_.id=?
Player(id=1, name=손흥민, age=27, position=Striker, club=Club(id=1, name=Tottenham Hotspur Football Club, ranking=9), locker=Locker(id=1, name=손흥민의 락커, position=입구에서 4번째 위치))
7월 13, 2020 12:25:28 오전 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    526400 nanoseconds spent acquiring 1 JDBC connections;
    421700 nanoseconds spent releasing 1 JDBC connections;
    12557400 nanoseconds spent preparing 1 JDBC statements;
    1784200 nanoseconds spent executing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    8766800 nanoseconds spent executing 1 flushes (flushing a total of 3 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 13, 2020 12:25:28 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]

```

책에서는 1:1과 관련해서 많은 이야기들을 하고 있다.    

특히 이런 관계에서 외래키를 어디에 두고 매핑을 하냐에 대한 부분인데 둘다 장단점을 이야기하고 있다.    

간략하게 위 예제로 설명하자면 다음과 같다.    

1. Player가 외래키를 관리할 때    
	- Player만 조회해도 Locker정보를 함께 가져올 수 있다. 즉 JPA입장에서 매핑하기가 편하다.
	- 하지만 단점으로는 만일 Locker가 없다면 (예를 들면 새로 영입되서 아직 Locker가 배정되지 않는 상황을 생각해보자.) 해당 locker_id는 null을 허용해야 한다.    

2. Locker가 외래키를 관리할 때    
	- 1:1 매핑이 깨질경우. 위에서 언급했던 선수가 1개 이상의 락커를 사용할수 있게 변경되었을 때 테이블을 변경하지 않아도 된다.    
	- 단점으로는 차후 뒤에서 배울 lazy옵션을 걸어도 eager방식으로 작동하게 된다.    
	
	
이 이야기는 사실 DBA관점, 개발자의 관점에 대한 부분이다.    

만일 DBA가 존재한다면 이 부분에 대해서 많은 이야기를 해야한다는 것이다.    

특히 나는 locker_id에 null을 허용해야 하는게 단점이라는 것일 이해하지 못했다.       

하지만 DB입장에서는 그게 부담이 되는 모양이다.    

암튼 이것은 그냥 넘어가자. 난 DBA가 아니라서....     


## @ManyToMany    

일단 책에서는 JPA의 표준스펙이긴 하지만 실무에서 사용하는 것을 권장하지 않는다.    

이것은 잘 생각해보면 알 수 있는데 DB관점에서 테이블을 두고 생각하면 정규화된 (pk, fk) 테이블애서는 말이 안되는 관계이기 때문이다.     

하지만 의외로 커머스에서는 이런 관계를 생각해 볼 수 있다.    

예를 들면 고객은 여러개의 상품을 선택해서 주문할 수 있고 상품 역시 여러 명의 고객들의 주문에 속할 수 있다.        

그래서 보통 이것들을 해소하기 위해 매핑하는 테이블이 중간에 껴 있다는 것을 알 수 있다.    

디비관점에서는 그렇다는 것이고 객체 관점에서는 사용자는 상품의 리스트를 가질 수 있고 상품 역시 자신을 구입하거나 장바구니에 담은 고객의 리스트를 가질 수 있다.    

그렇다면 @ManyToMany는 중간에 어떤 테이블을 생성해서 1:N, N:1로 서로 연결될 것이라는 것을 알 수 있다.    

일단 한번 코딩으로 해보자.    

Member

```
package io.basquiat.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {

	@Builder
	public Member(String name, String address) {
		this.name = name;
		this.address = address;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	private String address;
	
	@ManyToMany
	@JoinTable(name = "member_item")
	private List<Item> items = new ArrayList<>();
	
}

```
이때 @JoinTable을 통해서 member_item이라는 테이블을 생성한다는 것을 알 수 있다.



Item

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
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Item {
	
	@Builder
	public Item(String name, int stock) {
		this.name = name;
		this.stock = stock;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;

	private int stock;
	
}

```

이렇게 엔티티를 만들고 DDL을 생성하게 되면 

```
7월 13, 2020 1:17:22 오전 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 13, 2020 1:17:22 오전 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 13, 2020 1:17:22 오전 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 13, 2020 1:17:23 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
7월 13, 2020 1:17:23 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 13, 2020 1:17:23 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 13, 2020 1:17:23 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 13, 2020 1:17:23 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 13, 2020 1:17:24 오전 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    alter table member_item 
       drop 
       foreign key FKo2np23h92vspxdhcxaojylsp3
7월 13, 2020 1:17:25 오전 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@43b0ade] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    alter table member_item 
       drop 
       foreign key FKii0c9jys90jtoqh48pji2w8ip
Hibernate: 
    
    drop table if exists item
Hibernate: 
    
    drop table if exists member
Hibernate: 
    
    drop table if exists member_item
Hibernate: 
    
    create table item (
       id bigint not null auto_increment,
        name varchar(255),
        stock integer not null,
        primary key (id)
    ) engine=InnoDB
7월 13, 2020 1:17:25 오전 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@599f571f] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table member (
       id bigint not null auto_increment,
        address varchar(255),
        name varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table member_item (
       Member_id bigint not null,
        items_id bigint not null
    ) engine=InnoDB
Hibernate: 
    
    alter table member_item 
       add constraint FKo2np23h92vspxdhcxaojylsp3 
       foreign key (items_id) 
       references item (id)
Hibernate: 
    
    alter table member_item 
       add constraint FKii0c9jys90jtoqh48pji2w8ip 
       foreign key (Member_id) 
       references member (id)
7월 13, 2020 1:17:26 오전 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
7월 13, 2020 1:17:26 오전 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    380400 nanoseconds spent acquiring 1 JDBC connections;
    451900 nanoseconds spent releasing 1 JDBC connections;
    0 nanoseconds spent preparing 0 JDBC statements;
    0 nanoseconds spent executing 0 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    0 nanoseconds spent executing 0 flushes (flushing a total of 0 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 13, 2020 1:17:26 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]

```

생성 쿼리를 보면 

```
Hibernate: 
    
    create table member_item (
       Member_id bigint not null,
        items_id bigint not null
    ) engine=InnoDB
Hibernate: 
    
    alter table member_item 
       add constraint FKo2np23h92vspxdhcxaojylsp3 
       foreign key (items_id) 
       references item (id)
Hibernate: 
    
    alter table member_item 
       add constraint FKii0c9jys90jtoqh48pji2w8ip 
       foreign key (Member_id) 
       references member (id)

```

member_item테이블을 생성하고 각 컬럼을 item과 member를 참조해 fk를 잡는 alter쿼리를 볼 수 있다.    

하지만 이 방식을 권유하지 않는 이유는 이 중간 테이블만으로는 무언가를 제대로 할 수 없다는 것이다.     

사용자가 상품을 몇개를 언제 주문했는지에 대한 정보를 담을 수 있어야 한다는 것이다.    

결국 책에서는 저 중간 테이블을 엔티티로 격상시켜서 1:N, N:1로 매핑하는 것을 권유한다.     

다음에는 양방향 매핑을 알아보겠다.