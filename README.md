# 양방향 연관관계 매핑     

이전 브랜치에서는 단방향 연관관계 매핑에 대해서 배웠다.    

하지만 이런 의문이 들 것이다.     

'Player엔티티 기준으로 Club엔티티쪽으로 ManyToOne으로 그리고 Locker엔티티로 OneToOne으로 매핑한건은 잘 알겠지만 이 구조로는 Club쪽 또는 Locker쪽에서는 Player를 조회할 수 없는건가요?'

지금 구조로는 객체 그래프 탐색이 Player에서뿐이 할 수 없다. 그래서 양방향 연관관계 매핑이 존재하고 방법은 반대쪽에서도 마찬가지로 연관관계를 맺으면 된다.    

반대 쪽, 그러니깐 Club에서 Player로 ManyToOne의 반대 개념으로 받아드릴 수 있는 OneToMany로 그리고 OneToOne은 그 반대도 OneToOne이니 Locker에서 Player로 OneToOne매핑을 해주면 된다.    

결국 양방향 매핑이라는 것은 2개의 단방향 매핑이라는 것을 알 수 있다.    

하지만 양방향 매핑을 하게 될 경우에는 연관관계의 주인이 어디인지 분석을 해야 한다.    

## mappedBy    

연관관계의 주인을 이야기할 때 이 mappedBy라는 것이 있다.     

사실 이게 가장 어렵다고 말하는데 예를 들면 SNS같은 것을 생각해 보자. 간혹 가다보면 어떻게 해서 내가 예전에 알던 지인을 추천하는 경우를 보게 된다.     

소셜 네트워크에서 이런 경우 가장 중요한 키워드가 '키맨'이다. 그 키맨이 중요한 위치에 있는 사람인지 아닌지는 중요하지 않다. 그 키맨을 중심으로 뻗어가는 관계망 네트워크가 더 중요하다.     

즉, 그 '키맨'이라는 것은 그중 가장 많은 인맥을 가지고 있는 사람이라는 것이다.     

그렇다면 객체 입장에서도 한번 생각해 보자.     

이전에 봤던 이미지를 다시 한번 보자.     

![실행이미지](https://github.com/basquiat78/completedJPA/blob/6.unary-relation-mapping/capture/capture3.png)    

여기서 '키맨'은 무엇일까?     

바로 외래키를 갖고 있는 Player이다. 이 '키맨'을 통해서 Club, Locker의 정보를 알 수 있기 때문이다.     

바로 이 '키맨'이라는 단어를 Player입장에서 외래키라고 생각한다면 연관관계의 주인이라는 의미를 좀 더 쉽게 생각할 수 있지 않을까?     

물리적인 DB테이블에서 Player가 가장 많은 외래키를 가지고 있다는 것은 위에 언급한 관계망 네트워크를 생각할 때 Player를 기준으로 엮여 있는 테이블들을 알 수 있다.        

그리고 객체 입장에 생각해보면 객체 그래프 탐색에 있어서 가장 이점을 가지고 있는 Player가 바로 위에서 언급했던 '키맨'의 역할로 연관관계의 주인이라고 할 수 있다.     

책에서도 양방향 연관관계에서의 외래 키를 관리하는 객체가 주인이 될 수 있다고 말한다.     

이때 양방향에서 고려해야 하는 부분은 주인이 아닌 객체에서 참조하는 대상 엔티티에 대해서는 READ-ONLY, 즉 읽기만 가능하게 된다.    

하지만 확실히 객채 그래프 탐색이라는 의미에서는 아주 적절하다.     

이때 mappedBy로 주인을 지정하게 된다.    

그럼 거두절미하고 코드로 보자.    

기존의 단방향 관계에 있던 Locker와 Club에도 Player로 단방향 매핑을 하는 것으로부터 일단 시작한다.    

Club

```
package io.basquiat.model.football;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@ToString(exclude = "players")
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
	private String name;
	
	/** 클럽 랭킹 순위 */
	private int ranking;
	
	@OneToMany(mappedBy = "club")
	private List<Player> players = new ArrayList<>();
	
}

```
기존에는 없던 

```
@OneToMany(mappedBy = "club")
private List<Player> players = new ArrayList<>();
```
을 통해서 Player와 연관관계를 맺게 되는데 당연히 OneToMany이기 때문에 배열로 지정한다.    

하지만 이 경우 null문제를 피하기 위해선 초기화하는 코드가 들어가게 된다.    

N:1 문제로 발생할 수 있는 경우를 대비해서 List보다는 중복을 허용하지 않는 Set으로 하는 경우도 있는데 타입에 대한 관점에서는 이와 관련한 많은 블로그나 글을 보면 List를 권장하는 분위기이다.     

그리고 나서 mappedBy에 club을 지정하고 있다.     

'저기요? 왜 뜬금없이 club이죠? 주인을 지정하는 거라면서요? 그러면 player가 아닌가요?'     

~~그렇네요?~~        

하지만 저 club은 Player객체에 선언되어 있는 필드를 지칭한다.    

```
@ManyToOne
@JoinColumn(name = "club_id")
private Club club;
```
Player에 있는 필드중에 위 필드가 club인것이 보이는가?    

바로 저것을 의미하는 것이다.     

'아하! 그렇군요. 그러면 Locker에서도 mappedBy는 locker가 되겠군요?'

그렇다면 player에서 

```
@ManyToOne
@JoinColumn(name = "club_id")
private Club footballClub;
```
이라고 되어 있다면 mappedBy = "footballClub"이 되겠군요? 맞읍니까?    

맞읍니다.    

결국 Club이 Player에게 이렇게 메세지를 보낸다고 생각하면 된다.    

'야 나도 너랑 연관관계가 있는데 그게 뭐냐면 너가 가지고 있는 footballClub이고 그 footballClub의 주인이 너야~'    

라고 메세지를 보내는 방식을 mappedBy로 표현한다고 생각하면 좀 쉬워질라나?    


Locker

```
package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "locker")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "player")
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
	
	@OneToOne(mappedBy = "locker") // 'Player야 니가 품고 있는 locker가 있는데 그 locker의 주인이 너야'
	private Player player;
	
	public void matchingPlayer(Player player) {
		this.player = player;
	}
}

```
'아하? 그렇군요? 근데 저 밑에 못보던 코드가 보이는데요?'     

이것은 연관관계 편의 메소드를 작성한 것이다. 일단 이것은 Player코드를 한번 보자.    

Plyaer

```
package io.basquiat.model.football;

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
@ToString(exclude = {"club", "locker"})
public class Player {

	@Builder
	public Player(String name, int age, String position, Club club, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.club = club;
		club.getPlayers().add(this);
		this.locker = locker;
		locker.matchingPlayer(this);
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
빌드 패턴에 작성된 코드를 한번 살펴보기 바란다.     

이렇게 하는 이유는 객체 그래프 탐색을 위해서 작성하게 된다. 이것도 이후에 설명을 해볼까 한다.     

또한 기존에는 편의를 위해서 롬복에서 제공하는 @ToString을 사용했지만 양방향인 경우에는 이것을 그대로 사용하면 안된다.    

그래서 양방향으로 잡힌 필드는 제외를 해줘야 한다.     

보통 두가지 방식으로 하나는 of를 써서 오버라이딩할때 사용할 필드명을 명시하던가 또는 exclude를 써서 오버라이딩할 때 제외할 필드를 명시할 수 있다.    

사용할 필드가 제외할 필드보다 많으면 exclude로 제외할 필드를 명시하면 된다.    

'근데요? 왜, 굳이 그렇게 해야 하나요?'    

왜냐하면 Player가 club, locker를 가져오면 club과 locker입장에서는 또 player나 players를 다시 가져올 것이다. player에서는 또 .......

이거 반복하다 stackoverflowe에러를 직면하게 될것이기 때문이다.     

'진짜로 그래요? 진짜에요?'    

말이 필요없다. 코드로 한번 보자.    

Club

```
package io.basquiat.model.football;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	public Club(String name, int ranking) {
		this.name = name;
		this.ranking = ranking;
	}

	/** 클럽 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 클럽 명 */
	private String name;
	
	/** 클럽 랭킹 순위 */
	private int ranking;
	
	@OneToMany(mappedBy = "footballClub")
	private List<Player> players = new ArrayList<>();
	
}
```

Locker

```
package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	
	@OneToOne(mappedBy = "locker")
	private Player player;
	
	public void matchingPlayer(Player player) {
		this.player = player;
	}
}
```

Player

```
package io.basquiat.model.football;

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
	public Player(String name, int age, String position, Club footballClub, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.footballClub = footballClub;
		footballClub.getPlayers().add(this);
		this.locker = locker;
		locker.matchingPlayer(this);
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
	private Club footballClub;
	
	@OneToOne
	@JoinColumn(name = "locker_id")
	private Locker locker;
	
}
```
@ToString에서 exclude한 것을 다 지우고 한번 실행해 보자.

```
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
							 .footballClub(tottenhamFootballClub)
							 .locker(sonsLocker)
							 .build();
em.persist(son);
System.out.println(son.toString());
tx.commit();
```

어떤 일이 벌어질까?    


```
7월 18, 2020 12:28:11 오후 org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
7월 18, 2020 12:28:11 오후 org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
7월 18, 2020 12:28:12 오후 org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
7월 18, 2020 12:28:13 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
7월 18, 2020 12:28:13 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.cj.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
7월 18, 2020 12:28:13 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
7월 18, 2020 12:28:13 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
7월 18, 2020 12:28:13 오후 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
7월 18, 2020 12:28:14 오후 org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    alter table player 
       drop 
       foreign key FKh60stqlv4r5dk5hp5gcwvo0n7
7월 18, 2020 12:28:15 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@6443b128] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
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
7월 18, 2020 12:28:15 오후 org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@847f3e7] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
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
7월 18, 2020 12:28:15 오후 org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.football.Club
        */ insert 
        into
            club
            (name, ranking) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.model.football.Locker
        */ insert 
        into
            locker
            (name, position) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.model.football.Player
        */ insert 
        into
            player
            (age, club_id, locker_id, name, position) 
        values
            (?, ?, ?, ?, ?)
Exception in thread "main" java.lang.StackOverflowError
	at java.lang.Long.toString(Long.java:396)
	at java.lang.Long.toString(Long.java:1032)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Club.toString(Club.java:28)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at io.basquiat.model.football.Player.toString(Player.java:27)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)
	at java.util.AbstractCollection.toString(AbstractCollection.java:462)
	at org.hibernate.collection.internal.PersistentBag.toString(PersistentBag.java:622)
	at java.lang.String.valueOf(String.java:2994)
	at java.lang.StringBuilder.append(StringBuilder.java:131)

```
'뭐야 이 긴 에러는?' 하고 놀랐을 수도 있다.     

그냥 내 콘솔에 찍힌거 그대로 복사해서 붙였다.    

보면 각 엔티티에서 서로를 계속 참조하다가 Exception in thread "main" java.lang.StackOverflowError를 보게 된다.    

자 그리고 연관관계 편의 메소드가 없다면 어떤 일이 일어날까?    

Player

```
	@Builder
	public Player(String name, int age, String position, Club footballClub, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.footballClub = footballClub;
		//footballClub.getPlayers().add(this);
		this.locker = locker;
		//locker.matchingPlayer(this);
	}
```
주석 처리를 하고 

```
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
							 .footballClub(tottenhamFootballClub)
							 .locker(sonsLocker)
							 .build();
em.persist(son);
System.out.println(son.toString());
System.out.println(sonsLocker.getPlayer());
System.out.println(tottenhamFootballClub.getPlayers());
```

이 코드를 실행하게 되면    

```
Hibernate: 
    /* insert io.basquiat.model.football.Club
        */ insert 
        into
            club
            (name, ranking) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.model.football.Locker
        */ insert 
        into
            locker
            (name, position) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.model.football.Player
        */ insert 
        into
            player
            (age, club_id, locker_id, name, position) 
        values
            (?, ?, ?, ?, ?)
Player(id=1, name=손흥민, age=27, position=Striker)
null
[]
```
어라? 아무것도 없넹.    

엔티티입장에서는 당연히 그럴 것이라는 생각이 든다. 값을 세팅받은 적이 없기 때문인데 책에서는 비록 연관관계에 주인이 아니더라도 양쪽으로 값을 매핑해 주는 것을 권장한다.    

이유는 조금 생각해 보면 비지니스 로직이 들어가는 경우에는 객체 그래프 탐색을 통해서 양쪽에서 값을 꺼내와야 하는 경우도 생길 수 있기 때문이다.    

자 우리는 심플하게 양방향 매핑에 대해서 어느 정도 감을 잡았다.     

간략하게 요략을 하자면    

1. 단방향에서 양방향으로 매핑시 대상 객체에서 그와 반대대는, 예를 들면 OneToMany이면 대상 객체에서는 ManyToOne, ManyToOne이면 OneToMany, OneToOne이면 OneToOne으로 매핑을 한다.    

2. 연관관계 주인을 설정할 때는 외래키를 관리하는 엔티티가 주인이 되기 때문에 양방향 매핑시에는 주인을 설정하는 mappedBy로 그 관계를 명시한다.    

3. Lombok의 ToString이나 toString을 오버라이딩해서 직접 생성하는 경우에는 양방향으로 걸린 엔티티는 제외한다. (stackoverflowerror) 코딩 당시 또는 서버가 올라갈 때는 모르지만 이와 관련된 메소드 호출시 발생하는 잠재적인 오류     

## 지금까지 배운 연관관계 매핑을 실무로 한번 옮겨보자.    

이제 실제로 배운 것을 여러분들의 회사의 erd를 보고 실제로 매핑하는 것을 해볼 시간이다.     

지금 하는 예제는 회사의 주문 테이블을 살짝 가져왔다. 물론 컬럼들이 좀 많아서 필요한 요소들만 가져왔다.     

한번 erd를 보고 또는 여러분이 만들고자 하는 어플리케이션의 erd나 이것은 꼭 JPA로 매핑해보고싶다는 erd를 보고 직접 해보는 시간을 가벼보길 바란다.   

일단 주문과 관련된 간략한 erd를 보자. 실제로 회사에서 사용하는 테이블인데 basquiat를 prefix로 붙인 테이블명을 사용하고 있다.    

![실행이미지](https://github.com/basquiat78/completedJPA/blob/7.bidirectional-relation-mapping/capture/capture1.png)    

ManyToMany의 경우에는 Item과 Order의 관계를 erd로는 표현할 수 없어서 실제 JPA를 ManyToMany로 잡을 때 생성되는 브릿지 테이블을 생각하고 만들었다.    

이것을 보면 많은 테이블과의 연관관계에서 일단 '키맨'을 찾아야 한다.     

배송 정보와 관련한 Delivery테이블의 경우에도 OneToOne인데 Order가 외래키를 갖는게 객체 탐색 그래프에서 더 유리하다고 판단해서 Order가 외래키를 갖는 erd로 그렸다.    

눈썰미 있으신 분이라면 Order가 '키맨'임을 알 수 있다.    

자 그러면 이제 한번 이것을 중심으로 엔티티를 만들어보자.    

Member

```
package io.basquiat.model.item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
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
@Table(name = "basquiat_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "orders")
public class Member {

	@Builder
	public Member(String name, String email, String phone, String address) {
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.address = address;
	}
	
	/** */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 고객 명 */
	@Column(name = "mb_name")
	private String name;
	
	/** 고객 이메일 */
	@Column(name = "mb_email")
	private String email;
	
	/** 고객 폰번호 */
	@Column(name = "mb_phone")
	private String phone;
	
	/** 고객 주소 */
	@Column(name = "mb_address")
	private String address;
	
	/** 가입일 */
	@Column(name = "signup_at")
	private LocalDateTime signupAt;
	
	@OneToMany(mappedBy = "member")
	private List<Order> orders = new ArrayList<>();
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpSignupAt() {
    	signupAt = LocalDateTime.now();
    }
	
}
```

Order와 Member의 연관관계에서 Member기준을 보면 erd에서도 알 수 있듯이 OneToMany관계임을 알 수 있다.    
    
따라서 OneToMany관계를 설정하고 연관관계의 주인이 Order가 품고 있는 member가 주인이라는 메세지를 보내는 mappedBy를 설정했다.

Order

```
package io.basquiat.model.item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"member", "delivery", "items"})
public class Order {

	/** 주문 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 주문 상태 */
	@Enumerated(EnumType.STRING)
	private OrderStatus status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "delivery_id")
	private Delivery delivery;
	
	/**
	 * ManyToMany는 내부적으로 bridge Table을 생성하게 된다.
	 * 1. order_item테이블을 생성한다.
	 * 2. Order입장에서는 생성된 테이블과의 조인되는 컬럼을 order_id로 설정한다.
	 * 3. 대상 테이블에서의 item으로 가는 방향도 고려해서 inverseJoinColumns으로 설정한다.
	 */
	@ManyToMany
	@JoinTable(name = "order_item",
			   joinColumns = @JoinColumn(name = "order_id"),
			   inverseJoinColumns = @JoinColumn(name = "item_id")
	)
	private List<Item> items = new ArrayList<>();
	
	/** 주문 일자 */
	@Column(name = "order_at")
	private LocalDateTime orderAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpSignupAt() {
    	orderAt = LocalDateTime.now();
    }
	
}

```
Order의 경우 사실 내부적으로 밀리세컨드로 변경한 날짜와 고객 아이디의 조합으로 고유한 주문 번호를 생성하지만 여기서는 좀 귀찮아서....    

일단 ManyToMany의 관계를 설정하기 위해서 다음과 같은 방식을 선택했다.    

1. 애초에 DB관점에서는 ManyToMany라는 관계 자체가 없기 때문에 내부적으로 중간 테이블을 중간에 두고 연관관계를 맺는다.    

3. Order엔티티를 기준으로 새로 생성된 테이블과의 관계에서 조인할 컬럼을 설정한다.     

4. 양방향으로 설정된 관계에서 중간 테이블에서 item으로 연관관계가 설정될 때 Item과 중간 테이블이 조인될 컬럼을 inverseJoinColumns를 통해서 매핑해 준다.    

이런 형식으로 진행한다.     

그럼 Item쪽도 보자.     

```
package io.basquiat.model.item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
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
@Table(name = "basquiat_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "orders")
public class Item {
	
	@Builder
	public Item(String id, String name, String model) {
		this.id = id;
		this.name = name;
		this.model = model;
	}
	
	/** 상품 코드 */
	@Id
	private String id;
	
	/** 상품 명 */
	@Column(name = "it_name")
	private String name;

	/** 상품 모델명 */
	@Column(name = "it_model")
	private String model;
	
	@ManyToMany(mappedBy = "items")
	private List<Order> orders = new ArrayList<>();
	
	/** 상품 생성일 */
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	/** 상품 수정일 */
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
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
Order와의 ManyToMany에서 주인관계 설정하는 것으로 끝.    

마지막 Delivery

```
package io.basquiat.model.item;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "basquiat_delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "order")
public class Delivery {
	
	/** 배송 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 택배사 코드 */
	private String courier;
	
	/** 배송 상태 */
	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;
	
	/** 배송지 */
	private String place;

	@OneToOne(mappedBy = "delivery")
	private Order order;
	
	/** 배송 시작일 */
	@Column(name = "delivery_at")
	private LocalDateTime deliveryAt;
	
	/** 배송 상태가 변경될 때마다 업데이트 */
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpDeliveryAt() {
    	deliveryAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdate() {
    	completedAt = LocalDateTime.now();
    }
	
	
}
```
Order와의 OneToOne설정후 주인 설정하는 것으로 끝냈다.    

자 그럼 이제 메핑이 전부 완료가 되었으니 hibernate.hbm2ddl.auto를 create로 두고 제대로 생성이 되는지 한번 확인을 해보자.    

```
Hibernate: 
    
    alter table basquiat_order 
       drop 
       foreign key FK563g01judd0kln0c307jt85x7
Hibernate: 
    
    alter table basquiat_order 
       drop 
       foreign key FKf2swmba3h8fiegrxcoek3dgvw
Hibernate: 
    
    alter table order_item 
       drop 
       foreign key FKbin80ha4ltv41q82ylsh8q7um
Hibernate: 
    
    alter table order_item 
       drop 
       foreign key FKd3gknfwli98awr8t5kvjqanyv
Hibernate: 
    
    drop table if exists basquiat_delivery
Hibernate: 
    
    drop table if exists basquiat_item
Hibernate: 
    
    drop table if exists basquiat_member
Hibernate: 
    
    drop table if exists basquiat_order
Hibernate: 
    
    drop table if exists order_item
Hibernate: 
    
    create table basquiat_delivery (
       id bigint not null auto_increment,
        completed_at datetime,
        courier varchar(255),
        delivery_at datetime,
        place varchar(255),
        status varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table basquiat_item (
       id varchar(255) not null,
        created_at datetime,
        it_model varchar(255),
        it_name varchar(255),
        updated_at datetime,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table basquiat_member (
       id bigint not null auto_increment,
        mb_address varchar(255),
        mb_email varchar(255),
        mb_name varchar(255),
        mb_phone varchar(255),
        signup_at datetime,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table basquiat_order (
       id bigint not null auto_increment,
        order_at datetime,
        status varchar(255),
        delivery_id bigint,
        member_id bigint,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table order_item (
       order_id bigint not null,
        item_id varchar(255) not null
    ) engine=InnoDB
Hibernate: 
    
    alter table basquiat_order 
       add constraint FK563g01judd0kln0c307jt85x7 
       foreign key (delivery_id) 
       references basquiat_delivery (id)
Hibernate: 
    
    alter table basquiat_order 
       add constraint FKf2swmba3h8fiegrxcoek3dgvw 
       foreign key (member_id) 
       references basquiat_member (id)
Hibernate: 
    
    alter table order_item 
       add constraint FKbin80ha4ltv41q82ylsh8q7um 
       foreign key (item_id) 
       references basquiat_item (id)
Hibernate: 
    
    alter table order_item 
       add constraint FKd3gknfwli98awr8t5kvjqanyv 
       foreign key (order_id) 
       references basquiat_order (id)
```
다른 로그는 다 지우고 테이블이 생성된 쿼리를 한번 보자.    

기존의 있는 테이블을 Drop하는 쿼리가 보인다. 지워진 테이블은 다음과 같다.     

basquiat_delivery, basquiat_item, basquiat_member, basquiat_order, order_item    

'저기요? 잠시만요? 이 생뚱맞은 order_item은 어디서 나온건가요?'     

'ManyToMany, 즉 다대다 양방향 매핑중에 생긴 넘입지요. Order엔티티의 매핑 관계를 잘 살펴보아요'     

그 이후에 alter를 통해 외래키 설정을 하는 쿼리가 날아간 것을 볼 수 있다. ManyToMany에서 생성된 이 생뚱 맞은 테이블에도 item_id, order_id를 외래키로 잡는 모습도 포착되었다.     

하지만 우리는 책에서 그리고 수많은 블로그의 이야기들을 보면 이것은 절대로 ~Naver Die~~ 쓰지말라고 권장한다.     

자 그러면 우리는 이것을 그전에 중간에 저렇게 생성되는 테이블을 엔티티로 격상시켜서 이것을 발라버릴 것이다.    

그리고 실제로도 다음과 같이 회사내에서는 basquiat_order_detail이라는 테이블을 통해서 이 부분을 처리하고 있다.     

아마도 대부분 이런 방식을 이커머스에서는 사용하지 않을까 싶은데?     

보통 우리가 구입하기 전에 장바구니에 상품을 담는 모습을 생각해 보자. 한번의 주문에 여러개의 상품이 리스트로 들어와 있는 형태말이다.     

그리고 그 중에 체크 박스로 실제 사고 싶은 상품을 모아서 하나의 주문으로 묶는 것을 너무나 잘 알수 있다.    

~~이거 왜이래요? 다들 쇼핑몰에서 상품 사본적 없는 것처럼!~~     

그럼 erd를 한번 그려보자.     

![실행이미지](https://github.com/basquiat78/completedJPA/blob/7.bidirectional-relation-mapping/capture/capture2.png)    

자 중간에 baquiat_order_detail부분을 한번 살펴보자.    

저 erd를 보면 Order와 OrderDetail의 관계를 살펴봐야 한다. 사실 Order가 중심으로 OrderDetail과의 관계를 따져보면 Order가 왠지 주인이 될 것 같다.     

하지만 OrderDetail이 Order의 pk를 외래키로 관리하고 있는 것을 알 수 있다.    

'어? 그럼 OrderDetail이 Item, Order와의 관계에서 주인이 되는건가요?'     

하지만 한가지 누락된게 있다. Item입장에서 내가 OrderDetail에 속해 있는지 알 필요가 있을까?     

물론 통계와 관련해서 필요할 지 모르지만 내 관점에서 볼 때는 굳이 OrderDetail과 Item과 양방향을 잡을 필요는 없어 보인다.    

단지 OrderDetail만 Item으로 단방향 매핑만으로도 문제 없어보인다. 또한 차후에 양방향이 필요하면 당연히 양방향 매핑을 하면 된다.    

그것은 어디까지나 비지니스에서 요구사항에 따라 유연하게 변경하면 되는 부분이다.     

Item

```
package io.basquiat.model.item;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
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
@Table(name = "basquiat_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Item {
	
	@Builder
	public Item(String id, String name, String model) {
		this.id = id;
		this.name = name;
		this.model = model;
	}
	
	/** 상품 코드 */
	@Id
	private String id;
	
	/** 상품 명 */
	@Column(name = "it_name")
	private String name;

	/** 상품 모델명 */
	@Column(name = "it_model")
	private String model;
	
	/** 상품 생성일 */
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	/** 상품 수정일 */
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
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
기존의 ManyToMany관계를 없애버리자.    

자 그럼 OrderDetail을 만들어보자.    

OrderDetail

```
package io.basquiat.model.item;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_order_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"item", "order"})
public class OrderDetail {

	/** 주문 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 주문한 상품 */
	@ManyToOne
	@JoinColumn(name = "item_id")
	private Item item;
	
	/** 내가 속해 있는 Order정보 */
	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;
	
	/** 주문한 상품의 수량 */
	private int quantity;
	
	/** 주문한 상품의 전체 가격 */
	private int price;
	
	/** 주문한 상품의 옵션 아이디 */
	@Column(name = "io_id")
	private int optionId;
	
	/** 주문한 상품의 옵션명 */
	@Column(name = "io_name")
	private String optionName;
	
}

```
자 그럼 이제 Order부분이 변경될 차례다.    

Order

```
package io.basquiat.model.item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"member", "delivery", "orderDetails"})
public class Order {

	/** 주문 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 주문 상태 */
	@Enumerated(EnumType.STRING)
	private OrderStatus status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "delivery_id")
	private Delivery delivery;
	
	/** OrderDetail과 매핑을 한다. 이 때 주인은 나의 pk를 관리하는 대상이 OrderDetail이기 때문에 주인이라는 것을 명시한다. */
	@OneToMany(mappedBy = "order")
	private List<OrderDetail> orderDetails = new ArrayList<>();
	
	/** 주문 일자 */
	@Column(name = "order_at")
	private LocalDateTime orderAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpSignupAt() {
    	orderAt = LocalDateTime.now();
    }
	
}
```
제대로 연관관계 매핑이 완료되었다면 그려진 ERD의 형태대로 테이블이 생성된 것을 확인 할 수 있다.    

```
Hibernate: 
    
    alter table basquiat_order 
       drop 
       foreign key FK563g01judd0kln0c307jt85x7
Hibernate: 
    
    alter table basquiat_order 
       drop 
       foreign key FKf2swmba3h8fiegrxcoek3dgvw
Hibernate: 
    
    alter table basquiat_order_detail 
       drop 
       foreign key FKfwm50dk49vqmbgte9nbiigpbf
Hibernate: 
    
    alter table basquiat_order_detail 
       drop 
       foreign key FKf5m8vo7i2pyhd4x0ccb288b3f
Hibernate: 
    
    drop table if exists basquiat_delivery
Hibernate: 
    
    drop table if exists basquiat_item
Hibernate: 
    
    drop table if exists basquiat_member
Hibernate: 
    
    drop table if exists basquiat_order
Hibernate: 
    
    drop table if exists basquiat_order_detail
Hibernate: 
    
    create table basquiat_delivery (
       id bigint not null auto_increment,
        completed_at datetime,
        courier varchar(255),
        delivery_at datetime,
        place varchar(255),
        status varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table basquiat_item (
       id varchar(255) not null,
        created_at datetime,
        it_model varchar(255),
        it_name varchar(255),
        updated_at datetime,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table basquiat_member (
       id bigint not null auto_increment,
        mb_address varchar(255),
        mb_email varchar(255),
        mb_name varchar(255),
        mb_phone varchar(255),
        signup_at datetime,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table basquiat_order (
       id bigint not null auto_increment,
        order_at datetime,
        status varchar(255),
        delivery_id bigint,
        member_id bigint,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table basquiat_order_detail (
       id bigint not null auto_increment,
        io_id integer,
        io_name varchar(255),
        price integer not null,
        quantity integer not null,
        item_id varchar(255),
        order_id bigint,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    alter table basquiat_order 
       add constraint FK563g01judd0kln0c307jt85x7 
       foreign key (delivery_id) 
       references basquiat_delivery (id)
Hibernate: 
    
    alter table basquiat_order 
       add constraint FKf2swmba3h8fiegrxcoek3dgvw 
       foreign key (member_id) 
       references basquiat_member (id)
Hibernate: 
    
    alter table basquiat_order_detail 
       add constraint FKfwm50dk49vqmbgte9nbiigpbf 
       foreign key (item_id) 
       references basquiat_item (id)
Hibernate: 
    
    alter table basquiat_order_detail 
       add constraint FKf5m8vo7i2pyhd4x0ccb288b3f 
       foreign key (order_id) 
       references basquiat_order (id)
```

참고로 pk-fk가 걸린 테이블은 한번에 지워지지 않는다. 즉 외래키 관련 생성된 정보를 drop하고 난 이후에 지우는 것을 알 수 있다.     

이것은 DB와 관련된 지식으로 차후 cascade를 알아 볼 때 좀 더 세세하게 알아 볼 예정이다.    

쿼리가 날아간 흔적을 따라가면 각각의 테이블이 존재하면 지우고 새로 생성하는 쿼리가 먼저 날아간다.    

그리고 연관관계 매핑의 내용을 보고 테이블에 그와 관련된 외래키 설정을 위한 alter table쿼리를 날리는 것을 확인 할 수 있다.    

basquiat_order table

```
Hibernate: 
    
    alter table basquiat_order 
       add constraint FK563g01judd0kln0c307jt85x7 
       foreign key (delivery_id) 
       references basquiat_delivery (id)
Hibernate: 
    
    alter table basquiat_order 
       add constraint FKf2swmba3h8fiegrxcoek3dgvw 
       foreign key (member_id) 
       references basquiat_member (id)
```
우리가 erd기준으로 연관관계 매핑을 한 대로 delivery_id, member_id를 생성하는 것을 알 수 있다.     

sql에 대해 익숙하지 않는 분들을 위해서 입으로 한번 털어보겠다.     

1. 이제부터 내가 외래키를 잡을 예정입니다.     

2. 그 외래키의 이름은 FK563g01judd0kln0c307jt85x7이고 외래키로 delivery_id를 생성할 겁니다.     

3. 참조되는 테이블은 basquiat_delivery테이블이면 거기의 pk인 id가 되겠습니다.    

이런 식으로 alter를 날리는 것이다.    

basquiat_order_detail에 대해서도 마찬가지이다.     


# At A Glance    

JPA와 관련되서 많은 사람들을 만나보면 가끔 장인을 만나게 된다.     

SQL에 대해선 잘 모르지만 JPA만 깊게 파서 이 모든 것을 아우르는 분을 가끔 보게 된다.      

또는 SQL에 대한 깊은 조예가 있어서 이런 ORM매핑에 대해서 남다른 큰 그림을 빠르게 그리는 분도 있다.     

나의 경우에는 전자도 후자도 아니다. 그래서 꾸준히 회사의 ERD를 보고 나름대로 잘못된 부분은 수정해 보기도 하고 이것을 JPA로 어떻게 풀어갈 지 항상 고민한다.     

언젠가는 나도 장인이 되지 않을까?     

지금까지 단방향, 양방향에 대해서 조금 알아 보았다.     

실제로 회사의 erd를 기준으로 만든 것이라 나에게는 나름대로 jpa를 다시 공부하고 적용하는데 도움이 된다.     

지금 이 깃헙을 보는 분들도 그렇게 해보길 권한다.    

왜냐하면 실제로 토이 프로젝트, 자신이 생각한 프로젝트에 대해서는 누구나 쉽게 한다. 일단 간단하게 시작하기 때문이다.     

굉장히 복잡한 erd는 대부분 회사의 디비를 보면 느낄 수 있다. 일단 이커머스라서 그런지 정말 복잡하다. 테이블도 많고....      

하지만 대부분은 이미 기획단계에서 분석되고 만들어진 ERD를 기분으로 ORM을 해야 하는 경우가 많다.    

결국 연습이 필요하다. 이런 연습은 어플리케이션을 만들고 할 것도 없이 이런 방식으로 다양한 erd를 정말 한 번보고 딱 떠올라서 매핑할 수 있을 정도로 연습하면 고수가 되지 않을까?     

