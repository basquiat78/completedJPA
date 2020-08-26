# Java Persistence Query Language

## Paging

사실 10년정도 밟아온 나의 경험에서 수많은 RDBMS를 경험해 보진 못했다. 아마도 대부분 잘 알려진 mySQL이나 postgreSQL, oracle정도가 가장 많이 경험해 본 것들이다.      

그외 큐브리드, 사이베이스, DB2 정도는 살짝 맛 본 경우이고 mssql같은 경우에는 만나 본 적이 없다.     

하지만 오라클의 경우에는 페이징 처리하는 방식이 상당히 복잡하다.     

이미 이 브랜치에서 살짝 언급하긴 했지만 

```
SELECT ITEM.id,
			    ITEM.name,
			    ITEM.price
			FROM (SELECT id,
						name,
						price 
					FROM item
				 ) ITEM 
		 WHERE rownum <= 10
```
처럼 2뎁스 이상의 쿼리를 작성해야 하고 정렬이 들어가게 되면 또 3뎁스로 들어가게 되어 있다.     

어떤 프로젝트에서 초반에 mySQL을 중심으로 작업했다가 다른 계열사에서는 오라클을 사용한다고 해서 오라클 중심의 myBatis작업을 따로 했던 경험이 있는데 그때 가장 고통받았던게 바로 이 페이징이었다.      

하지만 jpa에서는 내부적으로 각 디비의 방언에 맞춰서 쿼리를 작성해 주기 때문에 이런 부분을 고민할 이유가 없다.     

이것을 사용하기 위해서는 우리는 JPQL에서 사용하는 방식인 limit, offset의 방식을 이해해야 한다.    

[Order By And Paging](https://github.com/basquiat78/jpa-with-querydsl/tree/3.query-dsl-orderby-n-paging)     

queryDSL에서 한번 설명했던 내용이니 한번 살펴보기 바란다.      

JPQL에서는 이것을 위해 두 개의 API를 제공한다.

1. .setFirstResult(int startPosition): offset에 해당하는 API, 즉 어떤 row 위치에서 조회해 올지 결정한다.          

2. .setMaxResults(int maxResult): limit에 해당하는 API, 즉 조회할 데이터의 수를 결정한다.      

그럼 긴말이 필요없다. 코드로 한번 살펴보자.

```
package io.basquiat;

import java.util.List;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import io.basquiat.jpql.Member;
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
        	
        	// 0 ~ 9까지 루프 돌면서 10개의 정보를 집어넣자.
        	IntStream.rangeClosed(0, 9).forEach(num -> {

        		Team myTeam = Team.builder().teamName("Default Team_" + num).build();
            	em.persist(myTeam);
            	
            	Member member = Member.builder().id("user_id_" + num)
            								 .name("user_name_" + num)
            								 .age(num)
            								 .team(myTeam)
            								 .build();
            	em.persist(member);
        		
        	});
        	
        	
        	em.flush();
        	em.clear();
        	
        	TypedQuery<Member> selected = em.createQuery("SELECT m FROM Member m", Member.class);
        	List<Member> members = selected.setFirstResult(0)
        								.setMaxResults(10)
        								.getResultList();
			
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

result grid

Hibernate: 
    /* SELECT
        m 
    FROM
        Member m */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_,
            member0_.team_id as team_id7_0_ 
        from
            basquiat_member member0_ limit ?
[
	Member(id=user_id_0, name=user_name_0, age=0, address=null), 
	Member(id=user_id_1, name=user_name_1, age=1, address=null), 
	Member(id=user_id_2, name=user_name_2, age=2, address=null), 
	Member(id=user_id_3, name=user_name_3, age=3, address=null), 
	Member(id=user_id_4, name=user_name_4, age=4, address=null), 
	Member(id=user_id_5, name=user_name_5, age=5, address=null), 
	Member(id=user_id_6, name=user_name_6, age=6, address=null), 
	Member(id=user_id_7, name=user_name_7, age=7, address=null), 
	Member(id=user_id_8, name=user_name_8, age=8, address=null), 
	Member(id=user_id_9, name=user_name_9, age=9, address=null)
]
```
어짜피 데이터가 10개뿐이니 모든 정보를 가져오게 될것이다. 이때 setFirstResult(0)를 줬기 때문에 offset의 경우에는 생략이 되서 쿼리가 날아간것을 알 수 있다.

그러면 정렬과 함께 사용해 보면 어떻까?      

아이디를 기준으로 내림차순으로 정렬하고 5개의 정보를 가져와 보자.      

```
// 0 ~ 9까지 루프 돌면서 10개의 정보를 집어넣자.
IntStream.rangeClosed(0, 9).forEach(num -> {

		Team myTeam = Team.builder().teamName("Default Team_" + num).build();
	    	em.persist(myTeam);
	    	
	    	Member member = Member.builder().id("user_id_" + num)
	    									.name("user_name_" + num)
	    									.age(num)
	    									.team(myTeam)
	    									.build();
	    	em.persist(member);
});
	
	
em.flush();
em.clear();
	
TypedQuery<Member> selected = em.createQuery("SELECT m FROM Member m ORDER BY m.id DESC", Member.class);
List<Member> members = selected.setFirstResult(0)
						    .setMaxResults(5)
						    .getResultList();
	
System.out.println(members);

result grid

Hibernate: 
    /* SELECT
        m 
    FROM
        Member m 
    ORDER BY
        m.id DESC */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_,
            member0_.team_id as team_id7_0_ 
        from
            basquiat_member member0_ 
        order by
            member0_.id DESC limit ?
[
	Member(id=user_id_9, name=user_name_9, age=9, address=null), 
	Member(id=user_id_8, name=user_name_8, age=8, address=null), 
	Member(id=user_id_7, name=user_name_7, age=7, address=null), 
	Member(id=user_id_6, name=user_name_6, age=6, address=null), 
	Member(id=user_id_5, name=user_name_5, age=5, address=null)
]
```
user_id_9부터 총 5개를 가져오게 된다. offset의 경우에는 0이기 때문에 실제 쿼리에서는 생략이 되었다.     

그렇다면 위 링크에도 offset과 관련해서 설명한게 있지만 해당 위치에서 그 밑으로 5개를 가져오기 때문에 만일 setFirstResult(1)로 하면 어떻게 될까?

```
Hibernate: 
    /* SELECT
        m 
    FROM
        Member m 
    ORDER BY
        m.id DESC */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_,
            member0_.team_id as team_id7_0_ 
        from
            basquiat_member member0_ 
        order by
            member0_.id DESC limit ?,
            ?
[
	Member(id=user_id_8, name=user_name_8, age=8, address=null), 
	Member(id=user_id_7, name=user_name_7, age=7, address=null), 
	Member(id=user_id_6, name=user_name_6, age=6, address=null), 
	Member(id=user_id_5, name=user_name_5, age=5, address=null), 
	Member(id=user_id_4, name=user_name_4, age=4, address=null)
]
```
이런 성격을 가지고 있기 때문에 실제 페이징의 가져오는 사이즈와 offset설정을 위한 유틸을 만들 때 이 부분을 잘 고려해야 한다.      

정렬의 경우에는 위 코드에서 알 수 있듯이 일반적인 쿼리와 같은 방식으로 작성하면 된다.      

## JOIN

[JOIN](https://github.com/basquiat78/jpa-with-querydsl/tree/5.query-dsl-join-and-aggregation)

조인과 관련된 내용은 위 링크에서 설명을 하고 있으니 한번 훝어보자.     

이것은 기본적으로 SQL에 대한 지식이 필요하기 때문인데 특히 INNER JOIN, LEFT JOIN에 대한 특징은 확실히 알아야 정확한 데이터를 조회할 수 있다.      

JPQL에서도 이 방식을 그대로 사용한다. 다만 객체를 대상으로 쿼리를 작성할 뿐이다.     

그전에 다음 코드를 한번 살펴보자.   

```
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

TypedQuery<Team> selected = em.createQuery("SELECT m.team FROM Member m", Team.class);
Team team = selected.getSingleResult();
System.out.println(team);

result grid

Hibernate: 
    /* SELECT
        m.team 
    FROM
        Member m */ select
            team1_.id as id1_1_,
            team1_.name as name2_1_ 
        from
            basquiat_member member0_ 
        inner join
            basquiat_team team1_ 
                on member0_.team_id=team1_.id
Team(id=1, teamName=Default Team)
```
우리가 이전에 배웠던 프로젝션과 관련해서 위와 같이 멤버의 팀을 구하는 쿼리를 날린 예제를 한번 했었다.       

특이한 점은 이런 경우에는 기본적으로 내부에서 inner join이 되서 쿼리가 날아가는 것을 확인할 수 있는데 이것은 쿼리로 생각해 보면 당연하다.      

그래서 보통 이런 경우에는 조인을 명시적으로 작성해서 해주게 된다.      

### INNER JOIN
그러면 위 기준으로 INNER JOIN을 JPQL에서 어떻게 작성하는지 한번 확인해 보자.      

보통은 INNER의 경우에는 생략해도 된다.

```
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

TypedQuery<Team> selected = em.createQuery("SELECT m.team FROM Member m INNER JOIN m.team t", Team.class);
Team team = selected.getSingleResult();
System.out.println(team);

result grid

Hibernate: 
    /* SELECT
        m.team 
    FROM
        Member m 
    INNER JOIN
        m.team t */ select
            team1_.id as id1_1_,
            team1_.name as name2_1_ 
        from
            basquiat_member member0_ 
        inner join
            basquiat_team team1_ 
                on member0_.team_id=team1_.id
Team(id=1, teamName=Default Team)
```

### LEFT OUTER JOIN
통상적으로 OUTER의 경우에는 생략가능한데 JPQL에서도 마찬가지이다.

```
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

TypedQuery<Team> selected = em.createQuery("SELECT m.team FROM Member m LEFT JOIN m.team t", Team.class);
Team team = selected.getSingleResult();
System.out.println(team);

result grid

Hibernate: 
    /* SELECT
        m.team 
    FROM
        Member m 
    LEFT JOIN
        m.team t */ select
            team1_.id as id1_1_,
            team1_.name as name2_1_ 
        from
            basquiat_member member0_ 
        left outer join
            basquiat_team team1_ 
                on member0_.team_id=team1_.id
Team(id=1, teamName=Default Team)
```
기본적인 방식은 비슷하지만 객체를 대상으로 쿼리를 작성한다는 것을 알 수 있다.      

독특한 점은 보통 SQL을 아시는 분들은 위 JPQL의 쿼리가 일반 SQL과 다르다는 것을 알 수 있다.      

예를 들면

```
SELECT * 
	FROM Member m INNER JOIN Team t

또는

SELECT * 
	FROM Member m LEFT JOIN Team t
```
저렇게 짜야 할거 같은데 'SELECT m.team FROM Member m LEFT JOIN m.team t'처럼 작성하는 것을 알 수 있다.     

하지만 연관관계 없는 테이블끼리 작성을 할 때는 다음과 같이 작성할 수 있다.       

보통 세타 조인이라고 표현하는데 현재 Member와 Team은 연관관계를 가지고 있지만 없다고 가장하고 작성을 해보자. 

```
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

TypedQuery<Team> selected = em.createQuery("SELECT t FROM Member m, Team t WHERE m.name = t.teamName", Team.class);
List<Team> teams = selected.getResultList();
System.out.println(teams);

result grid

Hibernate: 
    /* SELECT
        t 
    FROM
        Member m,
        Team t 
    WHERE
        m.name = t.teamName */ select
            team1_.id as id1_1_,
            team1_.name as name2_1_ 
        from
            basquiat_member member0_ cross 
        join
            basquiat_team team1_ 
        where
            member0_.name=team1_.name
[]
```
CROSS JOIN으로 조인이 되는 것을 알 수 있다.

이런 세타 조인의 경우에는 ON절을 활용해서 사용해 볼 수 도 있다.      

정확하게 기억은 못하겠지만 아마도 하이버네이트 버전이 5.x대부터 가능하게 되어 있는 걸로 아는데 

```
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

// INNER JOIN
//TypedQuery<Team> selected = em.createQuery("SELECT t FROM Member m INNER JOIN Team t ON m.name = t.teamName", Team.class);
TypedQuery<Team> selected = em.createQuery("SELECT t FROM Member m LEFT JOIN Team t ON m.name = t.teamName", Team.class);

List<Team> teams = selected.getResultList();
System.out.println(teams);

result grid

Hibernate: 
    /* SELECT
        t 
    FROM
        Member m 
    LEFT JOIN
        Team t 
            ON m.name = t.teamName */ select
                team1_.id as id1_1_,
                team1_.name as name2_1_ 
        from
            basquiat_member member0_ 
        left outer join
            basquiat_team team1_ 
                on (
                    member0_.name=team1_.name
                )
[null]
```
처럼 사용할 수 있다.     

'어? 그러면 연관관계를 가지는 경우에도 ON절을 사용할 수 있나요?'      

맨 처음 예제를 들었던 

```
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

TypedQuery<Team> selected = em.createQuery("SELECT m.team FROM Member m", Team.class);
Team team = selected.getSingleResult();
System.out.println(team);

result grid

Hibernate: 
    /* SELECT
        m.team 
    FROM
        Member m */ select
            team1_.id as id1_1_,
            team1_.name as name2_1_ 
        from
            basquiat_member member0_ 
        inner join
            basquiat_team team1_ 
                on member0_.team_id=team1_.id
Team(id=1, teamName=Default Team)
```
를 살펴보자.     

잘 보면 우리는 ON이나 WHERE절을 사용해 id를 비교하는 쿼리를 작성한 적이 없다. 하지만 JPQL에서는 이들 연관관계에 대해서 자동적으로 JOIN 이후에 연관관계에 있는 id의 값을 생성한다.     

하지만 그 이후에 ON절을 작성하게 되면 어떻게 될까?

```
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

TypedQuery<Team> selected = em.createQuery("SELECT t FROM Member m JOIN m.team t ON t.teamName = :teamName", Team.class);
List<Team> teams = selected.setParameter("teamName", myTeam.getTeamName())
						   .getResultList();
System.out.println(teams);

result grid

Hibernate: 
    /* SELECT
        t 
    FROM
        Member m 
    JOIN
        m.team t 
            ON t.teamName = :teamName */ select
                team1_.id as id1_1_,
                team1_.name as name2_1_ 
        from
            basquiat_member member0_ 
        inner join
            basquiat_team team1_ 
                on member0_.team_id=team1_.id 
                and (
                    team1_.name=?
                )
[Team(id=1, teamName=Default Team)]
```
오호라? 자동적으로 AND로 다음 조건을 생성해서 쿼리를 날리는 것을 확인할 수 있다. LEFT JOIN도 마찬가지이다.      

실무에서는 다른 건 잘 몰라도 INNER JOIN, LEFT JOIN은 정말 많은 곳에서 사용하게 된다. 따라서 이 2가지의 경우에는 특성을 잘 파악하는게 좋다.      

조인에 대해 설명하면서 걸어둔 링크는 queryDSL 관련 링크지만 해당 내용에 대해 좀 세세하게 설명해 놨다.      

SQL, 특히 이 조인에 대한 내용을 모른다면 위에 걸아둔 링크를 지나치지 말고 한번 훝어보는 것을 권장한다.       

## Sub Query
보통 서브 쿼리는 일반적인 SQL에서는 SELECT, FROM, WHERE, HAVING절에서 사용할 수 있다.     

예를 들면 다음과 같이 사용가능하다.     

```
SELECT Clause

SELECT id, 
	   name,
	   (SELECT AVG(age) FROM basquiat_member) AS avg_age
   FROM basquiat_member;


FROM Clause

SELECT iv.id, 
	   iv.name,
       iv.age
   FROM (SELECT * FROM basquiat_member) AS iv;
   

WHERE Clause

SELECT id, 
	   name,
       age
   FROM basquiat_member
   WHERE age >= (SELECT AVG(age) FROM basquiat_member);
   
HAVING Clause

SELECT id, 
	   AVG(age) AS avg_age
   FROM basquiat_member
   GROUP BY id
   HAVING AVG(age) > (SELECT SUM(age) FROM basquiat_member);   
```
이렇게 사용이 가능하다.       

하지만 JPQL에서는 보통 인라인뷰라고 하는 FROM절의 서브 쿼리는 지원하지 않는다.     

일단 이렇다는 것을 알고 한번 위 쿼리를 표현해 보자.      

1. SELECT절에서 서브쿼리 사용하기  

```
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
        	
TypedQuery<MemberDTO> selected = em.createQuery("SELECT new io.basquiat.jpql.MemberDTO(m.id, m.name, (SELECT SUM(sm.age) FROM Member sm), '', '', '') FROM Member m", MemberDTO.class);
List<MemberDTO> members = selected.getResultList();
System.out.println(members);

result grid

Hibernate: 
    /* SELECT
        new io.basquiat.jpql.MemberDTO(m.id,
        m.name,
        (SELECT
            SUM(sm.age) 
        FROM
            Member sm),
        '',
        '',
        '') 
    FROM
        Member m */ select
            member0_.id as col_0_0_,
            member0_.name as col_1_0_,
            (select
                sum(member1_.age) 
            from
                basquiat_member member1_) as col_2_0_,
            '' as col_3_0_,
            '' as col_4_0_,
            '' as col_5_0_ 
        from
            basquiat_member member0_
[MemberDTO(id=user_id, name=basquiat, age=33, city=, street=, zipcode=)]
```
좀 거시기 하지만 예를 들면 다음처럼 작성할 수 있다.      

이때 JPA에서는 SUM같은 Aggregation함수의 경우에는 long으로 타입이 반환되기 때문에 MemberDTO의 age의 변수 타입을 long으로 바꾸고 테스트하면 된다.     


2. WHERE절에서 서브쿼리 사용하기       

```
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

TypedQuery<Member> selected = em.createQuery("SELECT m FROM Member m WHERE m.age >= (SELECT MAX(sm.age) FROM Member sm)", Member.class);
List<Member> members = selected.getResultList();
System.out.println(members);

result grid

Hibernate: 
    /* SELECT
        m 
    FROM
        Member m 
    WHERE
        m.age >= (
            SELECT
                MAX(sm.age) 
            FROM
                Member sm
        ) */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_,
            member0_.team_id as team_id7_0_ 
        from
            basquiat_member member0_ 
        where
            member0_.age>=(
                select
                    max(member1_.age) 
                from
                    basquiat_member member1_
            )
[Member(id=user_id, name=basquiat, age=33, address=null)]
```

3. HAVING절에서 서브쿼리 사용하기  

```
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

TypedQuery<Member> selected = em.createQuery("SELECT m FROM Member m GROUP BY m.id HAVING AVG(age) >= (SELECT SUM(sm.age) FROM Member sm)", Member.class);
List<Member> members = selected.getResultList();
System.out.println(members);

result grid

Hibernate: 
    /* SELECT
        m 
    FROM
        Member m 
    GROUP BY
        m.id 
    HAVING
        AVG(age) >= (
            SELECT
                SUM(sm.age) 
            FROM
                Member sm
        ) */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_,
            member0_.team_id as team_id7_0_ 
        from
            basquiat_member member0_ 
        group by
            member0_.id 
        having
            avg(member0_.age)>=(
                select
                    sum(member1_.age) 
                from
                    basquiat_member member1_
            )
[Member(id=user_id, name=basquiat, age=33, address=null)]
```
어거지긴 하지만 일단 이런 방식도 가능하다.     

아쉽게도 FROM절의 경우에는 여러분이 생각하는 인라인뷰 쿼리를 작성하게 되면 바로 에러가 발생한다.      

그래서 네이티브 쿼리나 조인으로 풀어서 해결하는 것을 권장하는데 이것도 상황에 따라 달라질 수 있기 때문에 답은 없다.   

# At A Glance

쿼리에서 가장 많은 영역을 차지하는게 아마도 페이징과 조인이 아닌가 싶다.      

물론 GROUP BY도 빈번하게 사용한다. 그만큼 데이터를 가져오는데 있어서 가장 많은 영역을 차지한다.      

두서없이 작성하다 보니 사실 이 전 브랜치에 이어서 SELECT절에서 사용할 수 있는 몇가지 조건들, 예를 들면 조건식같은 녀석들을 queryDSL처럼 진행을 했어야 하는게 아닌가 싶어서 다음 브랜치에서는 이와 관련된 부분을 진행할 생각이다.       

그래서 이번 브랜치는 분량이 좀 짧아졌다.     

하지만 적어도 이 브랜치에서는 단순하게 JPQL에만 한정해서 생각하기 보다는 실제 쿼리에서 JOIN방식에 따른 데이터를 가져오는 부분을 잘 고려하고 이것을 JPQL에서도 자유롭게 사용하길 기대해 본다.      

하지만 결국 queryDSL을 사용하게 되지 않을까....

