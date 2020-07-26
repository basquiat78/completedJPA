# Proxy

## 일단 시작하기 전에     
지금까지 우리가 연관관계 매핑을 하면서 특별히 fetch전략에 대해서 고민해 본적이 없다.    

단지 객체 그래프 탐색이라는 것에만 초점을 맞추고 진행해 왔는데 JPA를 처음 접하게 되면 이 경우 fetch전략에 대해서 많은 이야기를 듣게 된다.     

실제 쿼리에서는 이런 전략이라는게 없다.     

그냥 다 가져오거나 또는 외래키를 가지고 관계를 가지고 있는 대상 테이블에서 조회를 다시 해오거나 둘 중 하나이다.     

하지만 JPA에서는 이것을 좀 다른 방식으로 풀어가고 있는데 그것을 이해하기 위해서 Proxy에 대한 개념을 설명하고 있다.     

프록시에 대해서 찾으면 대부분 다음과 같은 이야기를 주제로 한다.

[Proxy](https://dany-it.tistory.com/107)

하지만 JPA에서는 Proxy란 무엇일까?      

예들 들면 지금까지 테스트해왔던 Player를 예로 들면 Player는 Locker, Club로 다시 한번 테스트 해보자.     

```
Player son = em.find(Player.class, 1L);
System.out.println(son.toString());
```

다음과 같이 손흥민에 대한 정보를 조회하게 되면 쿼리가 어떻게 날아갈까?

```
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
Player(id=1, name=손흥민, age=27, position=Striker)
```
다음과 같이 club과 locker를 조인해서 가져오게 된다.     

하지만 만일 어떤 로직에서 locker와 club에 대한 정보는 굳이 필요하지 않다고 한다면 이 쿼리가 정말 유의미한 쿼리일까? ~~나는 그냥 손흥민 정보만 알고 싶었는데....~~     

거기에 쿼리가 날아간 것은 LEFT OUTER JOIN으로 정보를 가져온다.      

예전 myBatis로 프로젝트 했을 때는 이것을 어떻게 처리하냐면 보통은 손흥민의 정보만 가져오는 쿼리, 그리고 다른 테이블과 조인을 해서 정보를 가져오는 쿼리를 만들었다.     

예를 들면

```
@Mapper
public interface PlayerMapper {

	/**
	 * 선수의 정보만 가져온다.
	 * @param playerId
	 */
	public Player fetchPlayerOnlyById(Long id);
	
	/**
	 * 선수의 정보와 그와 관련된 모든 정보를 가져온다.
	 * @param playerId
	 */
	public Player fetchPlayerWithInfoById(Long id);
	
}
```
또는 선수의 정보만 가져오는 쿼리와 외래키로 각각의 club과 locker를 가져오는 쿼리를 만들어서 따로 호출하는 방식을 선택할 수 있다.      

그러면 JPA에서는 이것을 어떻게 해결할까?     

그래서 JPA에서 말하는 proxy가 뭔지 한번 살펴보자. ~~책에 있는 내용을 그래도 가져와도 상관없을라나...~~

다시 한번 말하지만 이것은 책에 기초한 내용이기 때문에 출처를 다시 한번 명시해 본다.    

[자바 ORM 표준 JPA 프로그래밍](http://acornpub.co.kr/book/jpa-programmig)     

또한 그 내용을 토대로 나름대로 정리한 것이기 때문에 정확한 정보는 책을 구입하시기 바란다.            

### 지연 로딩을 이해하려면 프록시의 개념을 이해해야 한다.

1. JPA에서 em.find() 말고, em.getReference()라는 메서드도 제공 된다.    

2. em.find() vs  em.getReference()
	- em.find() 는 DB를 통해서 실제 엔티티 객체를 조회하는 메서드이고 em.getReference() 는 DB의 조회를 미루는 가짜(프록시) 엔티티 객체를 조회하는 메서드이다.

특징은 다음과 같다.     

1. 실제 클래스를 상속 받아서 만들어진다. 따라서 실제 클래스와 모양이 같다. 당연한 건가?     

2. 프록시 객체는 실제 객체의 참조값을 보관한다.     

3. 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드 호출하게 된다.     

4. 프록시 객체는 처음 사용할 때 한 번만 초기화된다. 이 때, 프록시 객체가 실제 엔티티로 바뀌는 것은 아니고 이 프록시 객체를 통해서 실제 엔티티에 접근 가능한 상태가 된다.    

5. 1에서 설명했듯이 프록시 객체는 원본 엔티티를 상속받아서 만들어진다. 따라서 타입 체크시 주의해야한다.      

6. 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출할 때 영속성 컨텍스트에 있는 엔티티를 반환하게 된다.    

7. 하지만 준영속 상태일 때, 프록시를 초기화하면  org.hibernate.LazyInitializationException 예외를 뱉는다.     

자 그럼 이제 코드로 이게 무엇인지 한번 확인해 보자.     

```
Player son = em.getReference(Player.class, 1L); 
```
콘솔에 찍는 코드를 지우고 저렇게만 해서 한번 코드를 실행하면 어떤 일이 벌어질까?     

```
7월 25, 2020 11:44:26 오전 org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    545600 nanoseconds spent acquiring 1 JDBC connections;
    541200 nanoseconds spent releasing 1 JDBC connections;
    14931900 nanoseconds spent preparing 1 JDBC statements;
    1789900 nanoseconds spent executing 1 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    15716100 nanoseconds spent executing 1 flushes (flushing a total of 3 entities and 1 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
7월 25, 2020 11:44:26 오전 org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]

```
어라?  em.getReference를 호출하면 쿼리를 날리지 않는다.     

그럼 이 코드에서 다음과 같이 한번 콘솔을 찍어보자. 즉, 실제로 값을 한번 뽑아보는 코드를 추가하는 것이다.      

```
Player son = em.getReference(Player.class, 1L);
System.out.println("======================START===========================");
System.out.println(son.getName());
System.out.println("======================END===========================");
```
그랬더니

```
======================START===========================
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
손흥민
======================END===========================
```
내가 원하는 정보를 얻기 위해서 getName()을 하는 순간 그때서야 쿼리를 날린다.     

이것은 위에 언급한 특징중 1,2,3,4에 해당한다.     

5번 특징은 잘 모르겠다. 하지만 6번의 경우에는 다음과 같이 코드를 짜면 알 수 있다.     

```
Player son1 = em.find(Player.class, 1L);
System.out.println("=================================FIND PLAYER=================================");
Player son2 = em.getReference(Player.class, 1L);
System.out.println("======================START===========================");
System.out.println(son2.getName());
System.out.println("======================END===========================");
```
결과는

```
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
=================================FIND PLAYER=================================
======================START===========================
손흥민
======================END===========================
```
즉, 첫 번째에서 코드를 통해서 손흥민 엔티티는 영속성 컨텍스트에서 관리하게 되고 프록시를 통해 호출할 때 영속성 컨텍스트에서 엔티티를 반환해서 가져오기 때문에 쿼리가 날아가지 않은 것이다.     

다음과 같이 조회를 하고 영속성 컨텍스트를 초기화 한 이후에 name을 가져오는 코드를 작성하면 어떻게 될까?     

```
Player son = em.find(Player.class, 1L);
em.clear();
System.out.println(son.getName());
```
로그를 보여주진 않고 그냥 입으로 털어보면 이녀석은 조회를 했는데 엔티티를 반환한 경우이다. 따라서 영속성 컨텍스트를 초기화하게 되면 해당 엔티티는 준영속성 상태이긴 해도 객체에 대한 정보는 있기 때문에 손흥민이라는 이름이 콘솔에 찍히게 된다.     

하지만 다음과 같은 코드를 작성하게 되면 어떻게 될까?    

```
try {
        	
    	Player son = em.getReference(Player.class, 1L);
    	em.clear();
    	System.out.println("======================START===========================");
    	System.out.println(son.getName());
    	System.out.println("======================END===========================");
    	tx.commit();
} catch(Exception e) {
	e.printStackTrace();
    tx.rollback();
} finally {
    em.close();
}
```
이런 경우라면     

```
======================START===========================
org.hibernate.LazyInitializationException: could not initialize proxy [io.basquiat.model.football.Player#1] - no Session
	at org.hibernate.proxy.AbstractLazyInitializer.initialize(AbstractLazyInitializer.java:170)
	at org.hibernate.proxy.AbstractLazyInitializer.getImplementation(AbstractLazyInitializer.java:310)
	at org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor.intercept(ByteBuddyInterceptor.java:45)
	at org.hibernate.proxy.ProxyConfiguration$InterceptorDispatcher.intercept(ProxyConfiguration.java:95)
	at io.basquiat.model.football.Player$HibernateProxy$EkaHCMcT.getName(Unknown Source)
	at io.basquiat.jpaMain.main(jpaMain.java:27)
```
org.hibernate.LazyInitializationException 예외를 뱉어 내는 것을 확인할 수 있다.    

그러면 프록시 객체와 실제 엔티티가 차이가 있는지 다음과 같은 코드롤 통해서 알아볼 수 있다. 5번의 특징에 해당할려나?      

```
Player son = em.find(Player.class, 1L);
System.out.println("what? : " + son.getClass());

결과는 
what? : class io.basquiat.model.football.Player
```

그럼 프록시는     

```
Player son = em.getReference(Player.class, 1L);
System.out.println("what? : " + son.getClass());

결과는 
what? : class io.basquiat.model.football.Player$HibernateProxy$qjcV1C3l
```
오호?

그럼 이렇게 해보면 어떨까?   

```
Player son1 = em.find(Player.class, 1L);
System.out.println("what? : " + son1.getClass());

Player son2 = em.getReference(Player.class, 1L);
System.out.println("what? : " + son2.getClass());
결과는 
what? : class io.basquiat.model.football.Player
what? : class io.basquiat.model.football.Player
```
아하! '6. 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출할 때 영속성 컨텍스트에 있는 엔티티를 반환하게 된다.' 이 특징이 여기서도 증명이 된다.     

오호... 지금까지의 내용을 곰곰히 생각해 본다면 이런 생각이 들것이다.     

'왠지 예상이 되네요. 연관관계로 매핑된 club과 locker도 프록시에 delegate하고 정보가 필요하다면 프록시, 즉 target에서 그때 정보를 가져오게 되는건가요?'    

만일 이런 생각이 들었다면 이제부터 바로 fetch전략에 대해서 알아볼 준비가 되었다는 의미이다.     

원래 책에는 더 많은 이야기들이 있는데 그것들은 난중에 한번에 모아서 다시 한번 설명을 가져볼까 한다.     

## EAGER Loading     

사실 내가 Lazy Loading에 대한 개념을 알게 된것은 사실 몇 년전에 모 대기업에서 sns를 만들면서였다.          

당시 페이스북을 벤치마킹해서 만들었는데 페이스북의 피드를 보면 잘 알 수 있다. 스크롤을 내릴때 어느 영역에 도달할 때 이전 피드를 가져온다는 것을 말이다.     

쇼핑몰을 예로 들어도 그렇다. 특히 모바일에서는 페이징보다는 스크롤 페이징을 하는데 그 수많은 상품들이 전부 화면에 표현되지 않는다.     

스크롤을 내릴 때 포지션을 체크하고 그때 마다 상품들을 가져와서 화면에 뿌리기 때문이다.     

JPA에서도 마찬가지이다. 바로 위에서 언급했던 Proxy개념을 통해서 필요하지 않을 때는 가져오지 않다가 해당 정보가 필요하다면 그 때 가져오는 방식을 지원한다.     

그럼 지금까지 우리가 예제로 들었던 Player로 다시 한번 돌아가자.     

우리는 특별이 어떤 전략을 선택하지 않았지만 쿼리가 나가는 것을 보면 요청시에는 자동으로 조인을 통해서 모든 정보를 가져온다.    

그럼 해당 어노테이션을 한번 따라가보자.    

@ManyToOne

```
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)

public @interface ManyToOne {
    FetchType fetch() default EAGER;
}
```
@OneToOne

```
public @interface OneToOne {
    FetchType fetch() default EAGER;
}
```

@ManyToMany

```
public @interface ManyToMany {
    FetchType fetch() default LAZY;
}
```

@OneToMany

```

public @interface OneToMany {
    FetchType fetch() default LAZY;
}
```
다른 건 다 지우고 FetchType에 대해서만 코드를 가져와 봤다.     

현재까지 우리가 Player를 중심으로 사용한 어노테이션의 기본값은 EAGER이다.     

하지만 실무에서는 이것을 사용하지 말라고 권고한다. 거의 뭐 그냥 사용하지 말라고 권고가 아니라 거의 강제적인데 그 이유에는 여러가지가 있지만 다음이 가장 큰 것 같다.   

```
1. 실무에서는 JPQL을 많이 사용할 텐데 이 때는 즉시로딩의 경우 N+1 문제가 발생한다.     
2. 즉시 로딩은 생각치 못한 쿼리가 나갈 수 있다.
```


2번째 경우를 먼저 설명하자. 즉 우리가 예상하는 것과는 다른 쿼리가 나갈 수 있다는 것이다. 

사실 코드를 보면 이해할 수 있다.     

```
Player son = em.find(Player.class, 1L);
```
만일 위와 같은 코드를 보면 실제 쿼리가 조인이 되서 날아가리라고는 에상할 수 있나? ~~린정?~~      

물론 이 엔티티를 작성한 사람이거나 JPA의 장인이라면 알 수도 있지만 그냥 코드만으로 보면 '난 조인한 적이 없어'라고 생각할 수 있다.     

그리고 1번의 경우에는 좀 심각하다.     

```
Player player = em.createQuery("SELECT p FROM Player p", Player.class).getResultList().get(0);
```
그냥 이렇게 코드를 짜고 실행하면 어떤 일이 벌어질까?     

```
Hibernate: 
    /* SELECT
        p 
    FROM
        Player p */ select
            player0_.id as id1_2_,
            player0_.age as age2_2_,
            player0_.club_id as club_id5_2_,
            player0_.locker_id as locker_i6_2_,
            player0_.name as name3_2_,
            player0_.position as position4_2_ 
        from
            player player0_
Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    /* load io.basquiat.model.football.Player */ select
        player0_.id as id1_2_2_,
        player0_.age as age2_2_2_,
        player0_.club_id as club_id5_2_2_,
        player0_.locker_id as locker_i6_2_2_,
        player0_.name as name3_2_2_,
        player0_.position as position4_2_2_,
        club1_.id as id1_0_0_,
        club1_.name as name2_0_0_,
        club1_.ranking as ranking3_0_0_,
        locker2_.id as id1_1_1_,
        locker2_.name as name2_1_1_,
        locker2_.position as position3_1_1_ 
    from
        player player0_ 
    left outer join
        club club1_ 
            on player0_.club_id=club1_.id 
    left outer join
        locker locker2_ 
            on player0_.locker_id=locker2_.id 
    where
        player0_.locker_id=?
```
어? 왜 3번이 날아가지?     

JPQL로 작성하서 날려보니 이건 뭐....      

1, 2번의 항목이 전부 적용되는 사례가  아닌가?     

이것은 JPQL의 문법을 쉽게 사용하기 위해 고안된 queryDSL에서도 당연히 발생할 수 있다.           

물론 Player정보를 가져올 때는 무조건 클럽 정보를 가져와야 한다는 전제가 있다면 즉시 로딩을 사용할 수 있지만 그럼에도 그런 경우에도 지연로딩을 사용하고 JPQL fetch join나 객체 그래프 탐색을 통해서 가져오는게 좋다라는 것이다.      

따라서 즉시 로딩에 대해서는 별로 할말이 없다. ~~그냥 지연로딩만 사용하자~~      

## LAZY Loading     
자 그럼 우리는 이제 기존 코드에서 Player, Club, Locker의 연관관계 매핑에 이 전략을 적용시켜 보자.     

Player

```
package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@ToString(exclude = { "footballClub", "locker" })
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
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club footballClub;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "locker_id")
	private Locker locker;
	
}
```
Club

```
package io.basquiat.model.football;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@OneToMany(mappedBy = "footballClub", fetch = FetchType.LAZY)
	private List<Player> players = new ArrayList<>();
	
}
```
Locker

```
package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@OneToOne(mappedBy = "locker", fetch = FetchType.LAZY)
	private Player player;
	
	public void matchingPlayer(Player player) {
		this.player = player;
	}
}
```
@ManyToOne의 경우에는 기본이 Lazy이지만 그냥 명시적으로 적어두자.     

다시 이전 실행했던 코드를 한번 다시 실행해보자.     

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.football.Player;

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
        	System.out.println(son.toString());
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
```
결과는 

```
Hibernate: 
    select
        player0_.id as id1_2_0_,
        player0_.age as age2_2_0_,
        player0_.club_id as club_id5_2_0_,
        player0_.locker_id as locker_i6_2_0_,
        player0_.name as name3_2_0_,
        player0_.position as position4_2_0_ 
    from
        player player0_ 
    where
        player0_.id=?
Player(id=1, name=손흥민, age=27, position=Striker)
```
오호? 조인을 하지 않고 선수에 대한 정보만 가져왔다.     

그러면 이제 왜 위에서 Proxy에 대한 개념을 설명했는지 확인을 해 볼 시간이다.    

```
Player son = em.find(Player.class, 1L);
System.out.println(son.toString());
System.out.println("===============손흥민의 클럽 정보 가져오기====================");
System.out.println(son.getFootballClub().getClass());
System.out.println(son.getFootballClub());
System.out.println("===============손흥민의 락커 정보 가져오기====================");
System.out.println(son.getLocker().getClass());
System.out.println(son.getLocker());
```
결과는 ??

```
Hibernate: 
    select
        player0_.id as id1_2_0_,
        player0_.age as age2_2_0_,
        player0_.club_id as club_id5_2_0_,
        player0_.locker_id as locker_i6_2_0_,
        player0_.name as name3_2_0_,
        player0_.position as position4_2_0_ 
    from
        player player0_ 
    where
        player0_.id=?
Player(id=1, name=손흥민, age=27, position=Striker)
===============손흥민의 클럽 정보 가져오기====================
class io.basquiat.model.football.Club$HibernateProxy$ijM4wlkE
Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Club(id=1, name=Tottenham Hotspur Football Club, ranking=9)
===============손흥민의 락커 정보 가져오기====================
class io.basquiat.model.football.Locker$HibernateProxy$gLLY0e4U
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Locker(id=1, name=손흥민의 락커, position=입구에서 4번째 위치)
```
아하 각각의 Proxy가 영속성 컨텍스트에 초기화를 요청해서 실제 엔티티를 생성해서 반환하는 것을 눈으로 확인하는 순간이다.     

## CASCADE

종속이라는 의미를 지니고 있는데 이것은 DB에서 어떤 의미인지 한번 알아보자.     

여러분이 지금까지 테스트하면서 생성된 player, club, locker의 경우를 한번 살펴보자.     

외래키를 가지고 있는 player는 club과 locker과 연관관계 ~~라 하고 종속관계라 말한다~~에 있다.     

그럼 한번 workbench같은 툴에서 club과 locker테이블을 삭제하거나 직접 DROP 명령어를 날려보자.     

```
drop table club;
drop table locker;
```
그러면 지워지지 않고 

```
drop table club	Error Code: 3730. Cannot drop table 'club' referenced by a foreign key constraint 'FKh60stqlv4r5dk5hp5gcwvo0n7' on table 'player'.	0.000 sec

drop table locker	Error Code: 3730. Cannot drop table 'locker' referenced by a foreign key constraint 'FKdh2ff6dcjjgupccm2pmddouhw' on table 'player'.	0.000 sec

```
이런 에러를 만나게 된다.     

그리고 신기한 것은 실제 내부의 데이터를 지워도 똑같은 에러가 발생한다.     

```
delete from club where id = 1;
delete from locker where id = 1;
```

결국 지울려면 player테이블에서 이 둘의 외래키를 가지고 있는 데이터를 지워야 가능하다.     

또는 데이터에서도 마찬가지로 club과 locker의 특정 데이터를 지우려면 해당 pk를 가지고 있는 선수 모두를 그냥 싹다 지워야 한다. ~~싹 다!!!!~~

일단 DB에서는 이런 내용이 있다는 것을 필히 기억하자.     

자 그럼 우리는 이전에 손흥민이 토트넘에 들어가고 자신의 락커를 얻는 코드로 돌아가자.     

그전에 hibernate.hbm2ddl.auto을 create로 변경하자.

```
// 1. 내가 들어가고 싶은 팀이 무엇인지 살펴본다.
Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
										   .ranking(9)
										   .build();
em.persist(tottenhamFootballClub);

// 2. 해당 클럽에는 내가 사용한 락커가 있다. 
Locker sonsLocker = Locker.builder().name("손흥민의 락커")
									.position("입구에서 4번째 위치")
									.build();
em.persist(sonsLocker);

// 3. 손흥민이 토트넘 소속이 되다!
Player son = Player.builder().name("손흥민")
							 .age(27)
							 .position("Striker")
							 .footballClub(tottenhamFootballClub)
							 .locker(sonsLocker)
							 .build();
em.persist(son);
```
이 코드에서는 클럽, 락커, 손흥민 객체를 em.persist를 통해서 영속성 상태로 만들었다.      

근데 이런 고민이 들것이다.     

'아니 어짜피 손흥민이 주인이니 여기서 클럽과 락커를 세팅할때 해당 엔티티를 영속성 상태로 만들어주면 안되나?'      

이게 무슨 말이냐면 

```
// 1. 내가 들어가고 싶은 팀이 무엇인지 살펴본다.
Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
										   .ranking(9)
										   .build();

// 2. 해당 클럽에는 내가 사용한 락커가 있다. 
Locker sonsLocker = Locker.builder().name("손흥민의 락커")
									.position("입구에서 4번째 위치")
									.build();

// 3. 손흥민이 토트넘 소속이 되다!
Player son = Player.builder().name("손흥민")
							 .age(27)
							 .position("Striker")
							 .footballClub(tottenhamFootballClub)
							 .locker(sonsLocker)
							 .build();
em.persist(son);
```
위와 같이 손흥민 객체만 영속성 상태로 만들고 실행하면 어떻게 될까?      

```
javax.persistence.RollbackException: Error while committing the transaction
	at org.hibernate.internal.ExceptionConverterImpl.convertCommitException(ExceptionConverterImpl.java:81)
	at org.hibernate.engine.transaction.internal.TransactionImpl.commit(TransactionImpl.java:104)
	at io.basquiat.jpaMain.main(jpaMain.java:46)
Caused by: java.lang.IllegalStateException: org.hibernate.TransientPropertyValueException: object references an unsaved transient instance - save the transient instance before flushing : io.basquiat.model.football.Player.footballClub -> io.basquiat.model.football.Club
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:151)
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:181)
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:188)
	at org.hibernate.internal.SessionImpl.doFlush(SessionImpl.java:1364)
	at org.hibernate.internal.SessionImpl.managedFlush(SessionImpl.java:451)
	at org.hibernate.internal.SessionImpl.flushBeforeTransactionCompletion(SessionImpl.java:3210)
	at org.hibernate.internal.SessionImpl.beforeTransactionCompletion(SessionImpl.java:2378)
	at org.hibernate.engine.jdbc.internal.JdbcCoordinatorImpl.beforeTransactionCompletion(JdbcCoordinatorImpl.java:447)
	at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl.beforeCompletionCallback(JdbcResourceLocalTransactionCoordinatorImpl.java:183)
	at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl.access$300(JdbcResourceLocalTransactionCoordinatorImpl.java:40)
	at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl$TransactionDriverControlImpl.commit(JdbcResourceLocalTransactionCoordinatorImpl.java:281)
	at org.hibernate.engine.transaction.internal.TransactionImpl.commit(TransactionImpl.java:101)
	... 1 more
Caused by: org.hibernate.TransientPropertyValueException: object references an unsaved transient instance - save the transient instance before flushing : io.basquiat.model.football.Player.footballClub -> io.basquiat.model.football.Club
	at org.hibernate.engine.spi.CascadingActions$8.noCascade(CascadingActions.java:379)
	at org.hibernate.engine.internal.Cascade.cascade(Cascade.java:167)
	at org.hibernate.event.internal.AbstractFlushingEventListener.cascadeOnFlush(AbstractFlushingEventListener.java:158)
	at org.hibernate.event.internal.AbstractFlushingEventListener.prepareEntityFlushes(AbstractFlushingEventListener.java:148)
	at org.hibernate.event.internal.AbstractFlushingEventListener.flushEverythingToExecutions(AbstractFlushingEventListener.java:81)
	at org.hibernate.event.internal.DefaultFlushEventListener.onFlush(DefaultFlushEventListener.java:39)
	at org.hibernate.event.service.internal.EventListenerGroupImpl.fireEventOnEachListener(EventListenerGroupImpl.java:102)
	at org.hibernate.internal.SessionImpl.doFlush(SessionImpl.java:1360)
```
저런 에러가 발생하며 롤백이 실행된다.     

자 그럼 저것이 가능하게 할려면 어떻게 해주면 될까?     

```
package io.basquiat.model.football;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@ToString(exclude = { "footballClub", "locker" })
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
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "club_id")
	private Club footballClub;
	
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "locker_id")
	private Locker locker;
	
}
```

이러고 나서 나는 

```
Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
										   .ranking(9)
										   .build();
Locker sonsLocker = Locker.builder().name("손흥민의 락커")
									.position("입구에서 4번째 위치")
									.build();
Player son = Player.builder().name("손흥민")
							 .age(27)
							 .position("Striker")
							 .footballClub(tottenhamFootballClub)
							 .locker(sonsLocker)
							 .build();
em.persist(son);
```
과 같이 손흥민 객체만 영속성 상태로 만들면 내부적으로 club, locker도 영속성 상태로 만들어준다.      

하지만 이 경우에는 어떤 문제가 있을까?      

```
em.flush();
em.clear();
Player son = em.find(Player.class, 1L);
em.remove(son);
```
손흥민을 디비에서 삭제하게 되면 어떤 일이 벌어질까?    

```
Hibernate: 
    select
        player0_.id as id1_2_0_,
        player0_.age as age2_2_0_,
        player0_.club_id as club_id5_2_0_,
        player0_.locker_id as locker_i6_2_0_,
        player0_.name as name3_2_0_,
        player0_.position as position4_2_0_ 
    from
        player player0_ 
    where
        player0_.id=?
Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Club */ delete 
        from
            club 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
```
어라? club과 locker까지 지워지네????      

아니 선수 한명을 지웠을 뿐인데 어떻게 보면 부모입장이라고 할 수 있는 club과 locker까지 지워버린다.     

뭐 locker는 이해할 수 있는데 club을 지워???? ~~엄청 위험한 녀석인데?~~

그래서 보통은 이런 관계를 고려해 본다면 cascade의 옵션을 ALL로 두면 안된다.     

CascadeType.ALL -> CascadeType.PERSIST로 변경시켜주면 된다. 즉 영속성 전이를 persist즉, 해당 엔티티에 대해서는 영속성 상태로 만들어 주는 전이만 설정하면 된다.      

~~일단 변경하지 말자~~     

또한 다음과 같은 경우를 한번 보자.     

```
Player son = em.find(Player.class, 2L);
em.remove(son.getFootballClub());
em.remove(son.getLocker());
tx.commit();
```
손흥민의 정보를 가져와서 club과 locker의 정보를 가져와서 remove할려고 하면 사실 delete쿼리가 나가지 않는다.      

그 이유는 무엇일까? ~~이미 위에 DB에서 설명했잖아요!~~      

에러 조차도 없는 것 보니 애초에 지울려고 하면 지워지지 않고 에러를 뱉어낼 것이 자명하니 JPA에서는 이 관계를 알고 delete쿼리를 날리지도 않는듯 싶다.          

그러면 나는 락커의 경우에는 지우고 싶은데?라는 생각이 들것이다.      

그래서 다음과 같이     

```
package io.basquiat.model.football;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@OneToOne(mappedBy = "locker", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Player player;
	
	public void matchingPlayer(Player player) {
		this.player = player;
	}
}
```
영속성 전이 옵션을 설정했다.     

그리고...

```
Player son = em.find(Player.class, 1L);
em.remove(son.getLocker());
```
그리고는 비명소리가 들린다.      

```
Hibernate: 
    select
        player0_.id as id1_2_0_,
        player0_.age as age2_2_0_,
        player0_.club_id as club_id5_2_0_,
        player0_.locker_id as locker_i6_2_0_,
        player0_.name as name3_2_0_,
        player0_.position as position4_2_0_ 
    from
        player player0_ 
    where
        player0_.id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Club */ delete 
        from
            club 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
```
db에서 cascade에서 설명했던 것 기억나나?     

결국 locker입장에서는 내 자신을 지우려니 일단 player을 지워야 한다. 그리고 Player에서는 club과의 cascade옵션이 ALL로 두었으니 이에 따라 club도 지우고 자신도 지운다.      

~~어마무시한 대참사!!!! 하지만 실제로 있었던 일이다....~~     

자 그래서 다음과 같이 한번 수정을 해봤다.     

Player

```
package io.basquiat.model.football;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@ToString(exclude = { "footballClub", "locker" })
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
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "club_id")
	private Club footballClub;
	
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "locker_id")
	private Locker locker;
	
}
```
즉 영속성 상태로 만드는 것까지만 영속성 전이를 할 것이라고 명시한다.     

그리고 Locker쪽에는 

```
package io.basquiat.model.football;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@OneToOne(mappedBy = "locker", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Player player;
	
	public void matchingPlayer(Player player) {
		this.player = player;
	}
}
```
삭제일때만 영속성 전이를 할것이다라고 명시하고 

```
Player son = em.find(Player.class, 1L);
em.remove(son.getLocker());
```
실행하게 되면?    

```
Hibernate: 
    select
        player0_.id as id1_2_0_,
        player0_.age as age2_2_0_,
        player0_.club_id as club_id5_2_0_,
        player0_.locker_id as locker_i6_2_0_,
        player0_.name as name3_2_0_,
        player0_.position as position4_2_0_ 
    from
        player player0_ 
    where
        player0_.id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
```
클럽은 지우지 않았다. 왜냐하면 cascade옵션이 persist로 설정되어 있기 때문에 remove에 대한 전이는 여기서 막히게 된다.     

하지만 위에서 손흥민의 락커를 지우려니 결국 손흥민이라는 정보를 먼저 지우고 자신을 지울수 밖에 없다.      

자 그럼 지금까지는 Player의 입장에서 생각을 했는데 반대의 상황 , 즉 Club의 입장에서 한번 생각을 해보자.    

만일 다음과 같은 엔티티를 작성했다과 보자.     

기존의 엔티티와 크게 변경된 것은 없지만 일단     

Club

```
package io.basquiat.model.football;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@OneToMany(mappedBy = "footballClub", fetch = FetchType.LAZY)
	private List<Player> players = new ArrayList<>();
	
	/** 선수를 영입하다 */
	public void scoutPlayer(Player player) {
		this.getPlayers().add(player);
	}
}
```

Player

```
package io.basquiat.model.football;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@ToString(exclude = { "footballClub", "locker" })
public class Player {

	@Builder
	public Player(String name, int age, String position, Club footballClub, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.footballClub = footballClub;
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
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club footballClub;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "locker_id")
	private Locker locker;
	
	/** 클럽에 소속되다 */
	public void entryClub(Club club) {
		this.footballClub = club;
	}
}
```
Locker 

```
package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@OneToOne(mappedBy = "locker", fetch = FetchType.LAZY)
	private Player player;
	
	public void matchingPlayer(Player player) {
		this.player = player;
	}
}
```

그리고 우리가 기존에 했던 방식대로라면 옵션을 돌려놨기에

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.football.Club;
import io.basquiat.model.football.Locker;
import io.basquiat.model.football.Player;

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
        	Locker sonsLocker = Locker.builder().name("손흥민의 락커")
												.position("입구에서 4번째 위치")
												.build();
									        	
        	Player son = Player.builder().name("손흥민")
										 .age(27)
										 .position("Striker")
										 .locker(sonsLocker)
										 .build();
        	
        	Locker hugoLocker = Locker.builder().name("위고 요리스의 락커")
												.position("입구에서 1번째 위치")
												.build();
					        	
			Player hugo = Player.builder().name("Hugo Hadrien Dominique Lloris")
										 .age(33)
										 .position("Goal Keeper")
										 .locker(hugoLocker)
										 .build();
			
			Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
													   .ranking(9)
													   .build();
        	tottenhamFootballClub.scoutPlayer(hugo);
        	tottenhamFootballClub.scoutPlayer(son);
        	em.persist(hugo);
        	em.persist(son);
        	em.persist(hugoLocker);
        	em.persist(sonsLocker);
			em.persist(tottenhamFootballClub);
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
```
위와 같이 각 엔티티별로 모두 영속성 상태로 만들어줘야 한다.     

확실히 저렇게 좀 많아지면 이게 여간 귀찮은게 아니다. 딱 보면 결국 마지막 club정보가 모든 것을 갖는 형식인데 이런 생각을 해볼 수 있지 않을까?     

'아니 왜 꼭 저렇게 일일히 해줘야 해요? 그냥 tottenhamFootballClub만 해주면 그냥 다 영속성 상태로 만들어주면 안되요??'      

그래서 위에서 한번 해봤던 것처럼 club에다가 이 영속성 전이 옵션을 넣으면 된다.     

자 그럼  club이 선수를 영입하는 코드를 살펴보자.      

```
@OneToMany(mappedBy = "footballClub", fetch = FetchType.LAZY)
private List<Player> players = new ArrayList<>();
	
/** 선수를 영입하다 */
public void scoutPlayer(Player player) {
	this.getPlayers().add(player);
}
```
결국 선수가 추가될 때마다 뭔가 해당 객체를 영속성 상태로 전이를 해주면 되지 않을까? 그래서 다음과 같이 옵션을 주면 된다.      

위에서 진행했던 것과 다르지 않다.           

```
@OneToMany(mappedBy = "footballClub", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
private List<Player> players = new ArrayList<>();

/** 선수를 영입하다 */
public void scoutPlayer(Player player) {
	this.getPlayers().add(player);
}
```
자 근데 우리는 위에서 player에서 locker를 주입받는다. 그럼 여기도?     
Player코드에서 

```
@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
@JoinColumn(name = "locker_id")
private Locker locker;
```
와 같이 cascade를 설정해 주고 

```
Locker sonsLocker = Locker.builder().name("손흥민의 락커")
									.position("입구에서 4번째 위치")
									.build();

Player son = Player.builder().name("손흥민")
							 .age(27)
							 .position("Striker")
							 .locker(sonsLocker)
							 .build();

Locker hugoLocker = Locker.builder().name("위고 요리스의 락커")
									.position("입구에서 1번째 위치")
									.build();
		        	
Player hugo = Player.builder().name("Hugo Hadrien Dominique Lloris")
							 .age(33)
							 .position("Goal Keeper")
							 .locker(hugoLocker)
							 .build();

Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
										   .ranking(9)
										   .build();
tottenhamFootballClub.scoutPlayer(hugo);
tottenhamFootballClub.scoutPlayer(son);
em.persist(tottenhamFootballClub);
```
이렇게 해주면 어떨까?     

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
```
오! 만일 저 위의 실행 코드를 실행할 때 각 엔티티에 설정했던 cascade옵션을 지우면 클럽 정보만 딸랑 들어가는 것을 보게 될것이다.      

자. 근데 이런 걸 한번 생각해 보자.     

그럴 일이 없겠지만 만일 club이 사라졌다고 하자.     

```
토트넘 구단이 너무 어려워져서 구단 해체!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 
```
이런 기사가 뜬거야!!!  그래서  다음과 같이     

```
Locker sonsLocker = Locker.builder().name("손흥민의 락커")
												.position("입구에서 4번째 위치")
												.build();

Player son = Player.builder().name("손흥민")
							 .age(27)
							 .position("Striker")
							 .locker(sonsLocker)
							 .build();

Locker hugoLocker = Locker.builder().name("위고 요리스의 락커")
									.position("입구에서 1번째 위치")
									.build();
		        	
Player hugo = Player.builder().name("Hugo Hadrien Dominique Lloris")
							 .age(33)
							 .position("Goal Keeper")
							 .locker(hugoLocker)
							 .build();

Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
										   .ranking(9)
										   .build();
tottenhamFootballClub.scoutPlayer(hugo);
tottenhamFootballClub.scoutPlayer(son);
em.persist(tottenhamFootballClub);
em.flush();
em.clear();

Club selected = em.find(Club.class, 1L);
em.remove(selected);

List<Player> list = em.createQuery("SELECT p FROM Player p", Player.class).getResultList();
System.out.println(list.toString());
```
이렇게 한번 실행해 보자. 

```
Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Hibernate: 
    select
        players0_.club_id as club_id5_2_0_,
        players0_.id as id1_2_0_,
        players0_.id as id1_2_1_,
        players0_.age as age2_2_1_,
        players0_.club_id as club_id5_2_1_,
        players0_.locker_id as locker_i6_2_1_,
        players0_.name as name3_2_1_,
        players0_.position as position4_2_1_ 
    from
        player players0_ 
    where
        players0_.club_id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Club */ delete 
        from
            club 
        where
            id=?
Hibernate: 
    /* SELECT
        p 
    FROM
        Player p */ select
            player0_.id as id1_2_,
            player0_.age as age2_2_,
            player0_.club_id as club_id5_2_,
            player0_.locker_id as locker_i6_2_,
            player0_.name as name3_2_,
            player0_.position as position4_2_ 
        from
            player player0_

```
워!!!! club하나 지웠더니 cascade가 ALL이면 모든 옵션에 대해서이기 때문에 remove가 전파가 되고 Player에서도 locker에 대한 옵션도 ALL이니 클럽 하나 지우기 위해서 locker를 먼저 싹다 지우고 등록된 선수를 전부 싹다 지우고 자신을 지우는 어마무시한 쿼리가 날아간다.     

뭔가..... 느껴지는 게 있나?     

이런 기능이 있고 뭔가 좋아보이니 여기저기 사용하다 보면 어떤 일이 벌어질지 모른다.    

이거 하나 잘못 쓰면 기존의 쿼리에서는 상상할 수 없는 짓을 JPA가 하게 된다.      

만일 실수로라도 club테이블에서 어느 특정 클럽을 지우는 쿼리를 날린다 해도 위에서 언급했던 cascade설정으로 인해서 바로 오류를 내보내는데 JPA는 그냥 얄짤없다.     

```
이 데이터는 cascade가 설정되어 있는 녀석이라 지울 수 없는데 그런데도 지워줄까? ~~CascadeType.ALL이야~~

어 그래? 알았어! 그럼 외래키를 가지고 있는 player의 모든 정보를 지워야지? 어 근데 player와 연관괸 locker도 있는데 이넘도  CascadeType.ALL이네?    

이넘도 지우고 player도 지우고 club을 지워야지~~~
```
이렇게 되버리는 것이다....     

물론 locker의 경우는 그렇다고 생각할 수 있다. 선수가 사라졌으니 그 정보가 존재할 필요가 없을 수도 있으니깐.      

하지만 이런 옵션들이 여기저기 퍼져있으면 각 각의 테이블이 어떤 이유로 pk-fk가 엮어 있다면 진짜 무슨 일이 벌어질지 모른다. ~~무서버~~      

원하지 않는 정보까지 그냥 지워져버리는 이런 일이 벌어지는 것을 묵도할 것인가?      

그래서 보통은 이런 기능을 사용하기 위해서는 해당 기능을 사용하는 곳의 테이블과의 관계를 잘 파악해야 한다.      

일단 이 cascade의 옵션에 대한 정보를 한번 살펴보자.     

1. CascadeType.RESIST     
	- 엔티티를 생성하고, 연관 엔티티를 추가하였을 때 persist() 를 수행하면 연관 엔티티도 함께 persist()가 수행된다.     
	    만약 연관 엔티티가 DB에 저장이 되어있으면 다시하며 persist 를 하는 것이기때문에  detached entity passed to persist Exception이 발생한다.     
	   이경우에는 CascadeType.MERGE를 사용한다.     

2. CascadeType.MERGE     
	- 트랜잭션이 종료되고 detach 상태에서 연관 엔티티를 추가하거나 변경된 이후에 부모 엔티티가 merge()를 수행하게 되면 변경사항이 적용된다.     
	   연관 엔티티의 추가 및 수정 모두 반영된다.    

3. CascadeType.REMOVE     
	- 삭제 시 연관된 엔티티도 같이 삭제된다.    
	
4. CascadeType.DETACH    
	- 부모 엔티티가 detach()를 수행하게 되면, 연관된 엔티티도 detach() 상태가 되어 변경사항이 반영되지 않는다.

5. CascadeType.ALL
	- 모든 Cascade 적용한다.      

그래서 보통은 실무에서는 이것을 쓰는 경우에는 보통 CascadeType.PERSIST와 상황에 따라서 CascadeType.MERGE옵션을 배열로 설정해서 사용한다고 하는데...

```
cascade={CascadeType.REFRESH, CascadeType.MERGE}

cascade={CascadeType.PERSIST, CascadeType.MERGE}
```
뭐 이런식으로?

이것은 사실 연관관계와는 좀 상관이 없는 것 같다. 잘 쓰면 정말 좋지만 잘못쓰면 독이 되는데 그래서 보통은 stackoverflow나 여러 블로그 글들을 보면 이에 대해서 다양한 방법을 제시하는데 공통된 것이 몇가지 있다.

1. 부모와 자식의 관계가 그 둘에 한정된 경우, 즉 참조하는 곳이 한군데일 경우           

2. 한쪽에만 걸어서 제한적으로만 사용한다.      

물론 이와 관련해서 이런 부분은 고려해 볼만하다.      

게시판마다 특징이 다른 곳이 있는데 댓글에 답글이 달린 형식인 경우에는 답글이 있으면 댓글이 지워지지 않는 경우도 있지만 댓글을 지우면 그 하위에 붙은 답글도 지워지는 게시판도 있을 것이다.      

정책적인 부분일 수도 있지만 이런 경우에는 한번 고려해 볼만 하다. 또는 게시판에 댓글의 경우에도 마찬가지.          

댓글이 달린 게시판은 지울 수 없는 정책이거나 지울 수 있다면 그 게시판에 달린 댓글도 전부 지운다든가?           

그리고 cascade와는 결이 좀 약간 다르긴 하지만 다음과 같은 녀석이 있다.     

## orphanRemoval     

고아를 지운다? 왠 뜬금없는 고아?      

일단 지금까지 설정해 두었던 모든 cascade옵션을 각 엔티티에서 지우고 시작한다.     

그리고 다음과 같은 시나리오를 한번 생각해 보자.     

위에서 예제를 들었던 토트넘 구단이 해제되었다는 예제를 들어서 다음과 같이 코딩을 하면 우리는 예상할 수 있는 것은 바로 무결성 조건, 즉 다른 테이블들과의 관계로 인해서 지워지지 않는다.    

```
Locker sonsLocker = Locker.builder().name("손흥민의 락커")
											.position("입구에서 4번째 위치")
											.build();

Player son = Player.builder().name("손흥민")
							 .age(27)
							 .position("Striker")
							 .locker(sonsLocker)
							 .build();

Locker hugoLocker = Locker.builder().name("위고 요리스의 락커")
									.position("입구에서 1번째 위치")
									.build();
		        	
Player hugo = Player.builder().name("Hugo Hadrien Dominique Lloris")
							 .age(33)
							 .position("Goal Keeper")
							 .locker(hugoLocker)
							 .build();

Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
										   .ranking(9)
										   .build();
tottenhamFootballClub.scoutPlayer(hugo);
tottenhamFootballClub.scoutPlayer(son);
em.persist(hugo);
em.persist(son);
em.persist(hugoLocker);
em.persist(sonsLocker);
em.persist(tottenhamFootballClub);
em.flush();
em.clear();

Club selected = em.find(Club.class, 1L);
em.remove(selected);
tx.commit();

result 

ERROR: Cannot delete or update a parent row: a foreign key constraint fails (`basquiat`.`player`, CONSTRAINT `FKh60stqlv4r5dk5hp5gcwvo0n7` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`))
javax.persistence.RollbackException: Error while committing the transaction
	at org.hibernate.internal.ExceptionConverterImpl.convertCommitException(ExceptionConverterImpl.java:81)
	at org.hibernate.engine.transaction.internal.TransactionImpl.commit(TransactionImpl.java:104)
	at io.basquiat.jpaMain.main(jpaMain.java:63)
Caused by: javax.persistence.PersistenceException: org.hibernate.exception.ConstraintViolationException: could not execute batch
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:154)
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:181)
	at org.hibernate.internal.ExceptionConverterImpl.convertCommitException(ExceptionConverterImpl.java:65)
	... 2 more
Caused by: org.hibernate.exception.ConstraintViolationException: could not execute batch
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:109)
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:42)
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:113)
	at org.hibernate.engine.jdbc.batch.internal.BatchingBatch.performExecution(BatchingBatch.java:129)
	at org.hibernate.engine.jdbc.batch.internal.BatchingBatch.doExecuteBatch(BatchingBatch.java:105)
	at org.hibernate.engine.jdbc.batch.internal.AbstractBatchImpl.execute(AbstractBatchImpl.java:148)
	at org.hibernate.engine.jdbc.internal.JdbcCoordinatorImpl.executeBatch(JdbcCoordinatorImpl.java:198)
	at org.hibernate.engine.spi.ActionQueue.executeActions(ActionQueue.java:633)
	at org.hibernate.engine.spi.ActionQueue.lambda$executeActions$1(ActionQueue.java:478)
	at java.util.LinkedHashMap.forEach(LinkedHashMap.java:684)
	at org.hibernate.engine.spi.ActionQueue.executeActions(ActionQueue.java:475)
	at org.hibernate.event.internal.AbstractFlushingEventListener.performExecutions(AbstractFlushingEventListener.java:348)
	at org.hibernate.event.internal.DefaultFlushEventListener.onFlush(DefaultFlushEventListener.java:40)
	at org.hibernate.event.service.internal.EventListenerGroupImpl.fireEventOnEachListener(EventListenerGroupImpl.java:102)
	at org.hibernate.internal.SessionImpl.doFlush(SessionImpl.java:1360)
	at org.hibernate.internal.SessionImpl.managedFlush(SessionImpl.java:451)
	at org.hibernate.internal.SessionImpl.flushBeforeTransactionCompletion(SessionImpl.java:3210)
	at org.hibernate.internal.SessionImpl.beforeTransactionCompletion(SessionImpl.java:2378)
	at org.hibernate.engine.jdbc.internal.JdbcCoordinatorImpl.beforeTransactionCompletion(JdbcCoordinatorImpl.java:447)
	at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl.beforeCompletionCallback(JdbcResourceLocalTransactionCoordinatorImpl.java:183)
	at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl.access$300(JdbcResourceLocalTransactionCoordinatorImpl.java:40)
	at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl$TransactionDriverControlImpl.commit(JdbcResourceLocalTransactionCoordinatorImpl.java:281)
	at org.hibernate.engine.transaction.internal.TransactionImpl.commit(TransactionImpl.java:101)
	... 1 more
Caused by: java.sql.BatchUpdateException: Cannot delete or update a parent row: a foreign key constraint fails (`basquiat`.`player`, CONSTRAINT `FKh60stqlv4r5dk5hp5gcwvo0n7` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`))
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
	at com.mysql.cj.util.Util.handleNewInstance(Util.java:192)
	at com.mysql.cj.util.Util.getInstance(Util.java:167)
	at com.mysql.cj.util.Util.getInstance(Util.java:174)
	at com.mysql.cj.jdbc.exceptions.SQLError.createBatchUpdateException(SQLError.java:224)
	at com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchSerially(ClientPreparedStatement.java:853)
	at com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchInternal(ClientPreparedStatement.java:435)
	at com.mysql.cj.jdbc.StatementImpl.executeBatch(StatementImpl.java:796)
	at org.hibernate.engine.jdbc.batch.internal.BatchingBatch.performExecution(BatchingBatch.java:119)
	... 20 more
Caused by: java.sql.SQLIntegrityConstraintViolationException: Cannot delete or update a parent row: a foreign key constraint fails (`basquiat`.`player`, CONSTRAINT `FKh60stqlv4r5dk5hp5gcwvo0n7` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`))
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:117)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:97)
	at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping.java:122)
	at com.mysql.cj.jdbc.ClientPreparedStatement.executeInternal(ClientPreparedStatement.java:953)
	at com.mysql.cj.jdbc.ClientPreparedStatement.executeUpdateInternal(ClientPreparedStatement.java:1092)
	at com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchSerially(ClientPreparedStatement.java:832)
	... 23 more
```
근데 만일 club을 지워야만 한다면 어떻게 할까?     

아마도 club에 속한 모든 player를 지우고 (locker도 지워주면 더 좋고) 클럽을 지우면 될 것이다.      

근데 상상을 해보자. 그럴려면 해당 선수를 지우는 로직을 또 태워야 한다.      

물론 이전 테스트처럼 cascade옵션을 주면 되지만 orphanRemoval을 이용할 수 도 있다.    

자 그럼 Club엔티티에 다음과 같이 코드를 추가해 보자.     

```
package io.basquiat.model.football;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@OneToMany(mappedBy = "footballClub", fetch = FetchType.LAZY, orphanRemoval = true)
	private List<Player> players = new ArrayList<>();
	
	/** 선수를 영입하다 */
	public void scoutPlayer(Player player) {
		player.entryClub(this);
		this.getPlayers().add(player);
	}
}
```
그리고 이전에 테스트했던 코드를 다시 실행해 보면 어떤 일이 벌어질까?    

```
Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Hibernate: 
    select
        players0_.club_id as club_id5_2_0_,
        players0_.id as id1_2_0_,
        players0_.id as id1_2_1_,
        players0_.age as age2_2_1_,
        players0_.club_id as club_id5_2_1_,
        players0_.locker_id as locker_i6_2_1_,
        players0_.name as name3_2_1_,
        players0_.position as position4_2_1_ 
    from
        player players0_ 
    where
        players0_.club_id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Club */ delete 
        from
            club 
        where
            id=?
```
결과 로그만 보면 해당 클럽에 속한 player정보를 지우고 club을 지운다.      

즉 어떻게 보면 부모입장인 club에서는 자신이 지워지기에 자신만 지워진다면 해당 클럽에 속한 player는 말 그대로 쓸데 없이 남아있는 데이터, 즉 고아가 된다. 그런 입장에서 이것은 자식 정보들을 모두 지우는 역할을 한다.      

일단 locker의 경우는 지워지지가 않으니 그럼 한번 Player에도 locker에 대해서 한번 이것을 설정해 보자.     

```
package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import lombok.Setter;
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
@ToString(exclude = { "footballClub", "locker" })
public class Player {

	@Builder
	public Player(String name, int age, String position, Club footballClub, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.footballClub = footballClub;
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
	@Setter
	private int age;

	/** 선수 포지션 */
	private String position;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club footballClub;
	
	@OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "locker_id")
	private Locker locker;

	/** 클럽에 소속되다 */
	public void entryClub(Club club) {
		this.footballClub = club;
	}
}
```
이렇게 설정하고 다시 코드를 실행하면     

```
Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Hibernate: 
    select
        players0_.club_id as club_id5_2_0_,
        players0_.id as id1_2_0_,
        players0_.id as id1_2_1_,
        players0_.age as age2_2_1_,
        players0_.club_id as club_id5_2_1_,
        players0_.locker_id as locker_i6_2_1_,
        players0_.name as name3_2_1_,
        players0_.position as position4_2_1_ 
    from
        player players0_ 
    where
        players0_.club_id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Club */ delete 
        from
            club 
        where
            id=?
```
locker도 같이 지워진다.     

근데 orphanRemoval을 사용할 때는 다음과 같이 주의를 해야 한다. 

1. 참조하는 곳이 하나일 때 사용해야한다. 이것은 cascade와 비슷하다.     

2. 특정 엔티티가 개인 소유할 때 사용한다. 1번과 일맥상통한 내용이다.     

3. @OneToOne, @OneToMany만 가능하다. 이것은 IDE에서 테스트해보면 이 경우에만 사용할 수 있는것을 알 수 있다.    

그런데 orphanRemoval은 이렇게 단독으로 사용할 수 있지만 CASCADE와 함께 사용할 경우 몇가지 특징이 추가 된다.     

1. 두 옵션을 모두 활성화 하면 부모 엔티티를 통해서 자식의 생명주기를 관리할 수 있다.     

2. 도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 때 유용하다.      

그럼 첫 번째 사항부터 살펴보자.      

일단 지금까지 각 엔티티에 설정한 cascade와 orphanRemoval설정을 지워보자.     

그리고 다음과 같은 시나리오를 설정해 보자.     

```
클럽에서 선수 한명이 이적을 했다.
```

그렇다면 코드로 대충 구현을 해보면     

```
Locker sonsLocker = Locker.builder().name("손흥민의 락커")
												.position("입구에서 4번째 위치")
												.build();

Player son = Player.builder().name("손흥민")
							 .age(27)
							 .position("Striker")
							 .locker(sonsLocker)
							 .build();

Locker hugoLocker = Locker.builder().name("위고 요리스의 락커")
									.position("입구에서 1번째 위치")
									.build();
		        	
Player hugo = Player.builder().name("Hugo Hadrien Dominique Lloris")
							 .age(33)
							 .position("Goal Keeper")
							 .locker(hugoLocker)
							 .build();

Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
										   .ranking(9)
										   .build();
tottenhamFootballClub.scoutPlayer(hugo);
tottenhamFootballClub.scoutPlayer(son);
em.persist(hugo);
em.persist(son);
em.persist(hugoLocker);
em.persist(sonsLocker);
em.persist(tottenhamFootballClub);
em.flush();
em.clear();

Club selected = em.find(Club.class, 1L);
Player transferPlayer = selected.getPlayers().get(0);
System.out.println("이적했으니 클럽 선수 명단에서 지운다.");
em.remove(transferPlayer);
Locker removeLocker = transferPlayer.getLocker();
em.remove(removeLocker);

result 

Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
Hibernate: 
    select
        players0_.club_id as club_id5_2_0_,
        players0_.id as id1_2_0_,
        players0_.id as id1_2_1_,
        players0_.age as age2_2_1_,
        players0_.club_id as club_id5_2_1_,
        players0_.locker_id as locker_i6_2_1_,
        players0_.name as name3_2_1_,
        players0_.position as position4_2_1_ 
    from
        player players0_ 
    where
        players0_.club_id=?
이적했으니 클럽 선수 명단에서 지운다.
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
```
이린 식의 코드를 구현해 볼 수 있다.     

근데 

```
1. 두 옵션을 모두 활성화 하면 부모 엔티티를 통해서 자식의 생명주기를 관리할 수 있다.     
```

이 이야기가 무슨 말인가?      

club과 player에 다음과 같이 한번 설정을 해보자.    

Club

```
package io.basquiat.model.football;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@OneToMany(mappedBy = "footballClub", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Player> players = new ArrayList<>();
	
	/** 선수를 영입하다 */
	public void scoutPlayer(Player player) {
		player.entryClub(this);
		this.getPlayers().add(player);
	}
}
```

Player

```
package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import lombok.Setter;
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
@ToString(exclude = { "footballClub", "locker" })
public class Player {

	@Builder
	public Player(String name, int age, String position, Club footballClub, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.footballClub = footballClub;
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
	@Setter
	private int age;

	/** 선수 포지션 */
	private String position;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club footballClub;
	
	@OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "locker_id")
	private Locker locker;

	/** 클럽에 소속되다 */
	public void entryClub(Club club) {
		this.footballClub = club;
	}
}
```

그럼 코드로는 어떻게 구현해 볼 수 있을까?     

```
Locker sonsLocker = Locker.builder().name("손흥민의 락커")
												.position("입구에서 4번째 위치")
												.build();

Player son = Player.builder().name("손흥민")
							 .age(27)
							 .position("Striker")
							 .locker(sonsLocker)
							 .build();

Locker hugoLocker = Locker.builder().name("위고 요리스의 락커")
									.position("입구에서 1번째 위치")
									.build();
		        	
Player hugo = Player.builder().name("Hugo Hadrien Dominique Lloris")
							 .age(33)
							 .position("Goal Keeper")
							 .locker(hugoLocker)
							 .build();

Club tottenhamFootballClub = Club.builder().name("Tottenham Hotspur Football Club")
										   .ranking(9)
										   .build();
tottenhamFootballClub.scoutPlayer(hugo);
tottenhamFootballClub.scoutPlayer(son);
em.persist(hugo);
em.persist(son);
em.persist(hugoLocker);
em.persist(sonsLocker);
em.persist(tottenhamFootballClub);
em.flush();
em.clear();

Club selected = em.find(Club.class, 1L);
System.out.println("이적했으니 클럽 선수 명단에서 지운다.");
selected.getPlayers().remove(0); // 리스트에서 첫 번째 인덱스를 지운다.
tx.commit();
```
코드를 보면 그냥 Club에서 선수 명단을 가져오고 첫 번째 인덱스를 지우는 행위를 했다.     

하지만 결과는?

```
Hibernate: 
    select
        club0_.id as id1_0_0_,
        club0_.name as name2_0_0_,
        club0_.ranking as ranking3_0_0_ 
    from
        club club0_ 
    where
        club0_.id=?
이적했으니 클럽 선수 명단에서 지운다.
Hibernate: 
    select
        players0_.club_id as club_id5_2_0_,
        players0_.id as id1_2_0_,
        players0_.id as id1_2_1_,
        players0_.age as age2_2_1_,
        players0_.club_id as club_id5_2_1_,
        players0_.locker_id as locker_i6_2_1_,
        players0_.name as name3_2_1_,
        players0_.position as position4_2_1_ 
    from
        player players0_ 
    where
        players0_.club_id=?
Hibernate: 
    select
        locker0_.id as id1_1_0_,
        locker0_.name as name2_1_0_,
        locker0_.position as position3_1_0_ 
    from
        locker locker0_ 
    where
        locker0_.id=?
Hibernate: 
    /* delete io.basquiat.model.football.Player */ delete 
        from
            player 
        where
            id=?
Hibernate: 
    /* delete io.basquiat.model.football.Locker */ delete 
        from
            locker 
        where
            id=?
```
어라? 그와 관련된 locker와 player를 지우는 쿼리가 날아간다.     

실제로 쿼리를 날려서 조회해 봐도 알 수 있다.      

즉 부모 엔티티에서 자식들의 생명주기를 관리하는 것이 보인다.      

그럼 두 번째 항목은 어떤 이야기일까? 이미 위에서 경험한 것이다.     

[Aggregate Root개념](https://medium.com/@SlackBeck/%EC%95%A0%EA%B7%B8%EB%A6%AC%EA%B2%8C%EC%9E%87-%ED%95%98%EB%82%98%EC%97%90-%EB%A6%AC%ED%8C%8C%EC%A7%80%ED%86%A0%EB%A6%AC-%ED%95%98%EB%82%98-f97a69662f63)    

위 글에 대한 이야기라면 우리는 테이블의 관계와 엔티티들의 연관관계에 대해서 잘 알아야 한다.      

# At A Glance     

지금까지 프록시, 결국 지연로딩, 즉시 로딩에 대한 개념과 영속성 전이에 대해서 알아 봤다.     

여기서 영속성 전이의 경우에는 주의를 요하는 옵션이다. 따라서 잘 된 설계위에서 위에서 언급했던 상황들을 고려하고 적용해야 한다.     

연습도 많이 필요하고 나의 경우에도 지금까지 JPA를 하면서 굉장히 많이 사용한 옵션도 아니였기 때문에 많은 테스트 케이스를 고려하고 사용했던 경험이 있다.     

다시 그것을 상기하려고 테스트를 이것저것 했더니 나 자신도 좀 정리가 살짝 안되긴 하지만 그래도 끊임없이 상상하고 테스트를 하게 되는 것 같다.     