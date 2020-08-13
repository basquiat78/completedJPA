# Java Persistence Query Language

일명 JPQL이라고 한다.      

JPQL에는 몇 가지 특징이 있는데     

1. 테이블이 아닌 객체를 검색하는 객체지향 쿼리다. 

2. SQL을 추상화 했기 때문에 특정 벤더에 종속적이지 않다.     

3. JPA는 JPQL을 분석하여 SQL을 생성한 후 DB에서 조회한다. 이 말인 즉 결국 SQL로 변환한다.

이제부터 하나씩 알아가 보자.      

## 일단 시작하기 전에   
기본적으로 JPQL은 일반적인 SQL문과 상당히 유사하다. 거의 같다고 보면 되는데 단지 위에서 언급한 1번 특징을 가지고 있다.      

어째든 기본적인 SQL문법에 대해서 어느 정도 지식이 필요하다.      

여기서는 그런 기본적인 문법을 알고 있다는 가정하에 진행할 생각이다.      

그리고 책에서는 Criteria, queryDSL, 네이티브 SQL, JDBC를 직접 사용하는 방법등을 소개하고 있는데 여기서는 다 제껴두겠다.      

Criteria는 뭐... queryDSL로 대체가능하다.       

현재 queryDSL은 따로 진행해서 완료한 상태이기 떄문에 그 부분을 참조하면 될거 같고 SpringJDBCTemaplate이나 myBatis와의 연계는 또 따로 진행할 예정이기 때문이다.      

책을 기준으로 하고 있지만 후반부 고급에 속한 내용들은 일단 제외한다.     

결국 Spring Framework, 정확히는 SpringBoot와의 연계는 위에 언급한 대로 따로 진행할 생각이다.

어쨰든 가장 중요한 것은 JPQL이 아닌가 생각하기 때문이고 여기에 더 초점을 맞추고 진행한다.      

## Intro     
지금까지 쭉 테스트를 진행하면서 저장, 수정, 삭제같은 경우는 상당히 심플하다.      

하지만 실제 업무에서는 가장 많은 부분을 차지하는 것인 조회부분이다.      

오죽하면 DB를 레플리카로 운영해서 Writer, Reader로 분리할까?      

실제로 통계상 8:2라고 하는데 생각해 보면 인터넷을 통해 우리가 행하는 대부분의 액션은 거의 검색이라는 것을 알 수 있다.      

예를 들면 웹쇼핑을 하더라도 여러분들은 대부분 상품 조회를 하는데 많은 시간을 할애하지 주문에는 많은 시간을 소비하지 않는다.       

결국에 주문하고 구입하는 시간은 진짜 몇 초에 불과하다는 것을 이미 알고 있다.      

그만큼 조회라는 부분은 대부분의 업무에서도 큰 비중을 차지한다.     

지금까지 테스트는 그냥 단순하게 엔티티를 중심으로    

```
em.find(Entityt.class, pk);
```
요런 식의 테스트만 진행했지만 다양한 조건을 통해서 원하는 정보를 조회하는게 중요하다.     

그래서 기본적으로 위에서 언급했던 SQL의 기본적인 개념은 알고 있어야 한다.      

이런 모든 것을 기본적으로 가져가야 한다. 항상 언급하는 것이지만 JPA는 만능이 아니다.     

결국 RDBMS위에 서있는 기술이기 때문이다.


## Basic JPQL

위에서처럼 간단한 기능의 조회로만 테스트를 해왔는데 이제부터 기본적인 문법을 하나씩 알아가 보고자 한다.

jpql이라는 패키지에 Member라는 클래스를 하나 만들어 볼 것이다. 그래서 기존에 테스트했던 엔티티들은 어노테이션을 주석처리하고 시작한다.


Member

```
package io.basquiat.jpql;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "basquiat_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {
	
	@Builder
	public Member(String id, String name, int age, Address address) {
		super();
		this.id = id;
		this.name = name;
		this.age = age;
		this.address = address;
	}

	@Id
	private String id;
	
	private String name;
	
	private int age;

	@Embedded
	private Address address;

}
```

Address

```
package io.basquiat.jpql;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@Embeddable
@EqualsAndHashCode
public class Address {

	@Builder
	public Address(String city, String street, String zipcode) {
		super();
		this.city = city;
		this.street = street;
		this.zipcode = zipcode;
	}

	/** 시 */
	@Column(name = "member_city")
	private String city;
	
	/** 동 */
	@Column(name = "member_street")
	private String street;
	
	/** 우편 번호 */
	@Column(name = "member_zipcode")
	private String zipcode;

	/** 전체 주소 가져오 */
	public String totalAddress() {
		return city + " " + street + ", " + zipcode;
	}
	
}
```
지금까지 테스트해왔던 것과 별반 차이없다.     

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
        	e.printStackTrace();
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
    
}
```
테이블을 한번 생성해 보자.      

```
INSERT INTO basquiat_member 
	(
	 id, member_city, member_street, member_zipcode, age, name
	)
	VALUES 
    (
	 'user1', 'city1', 'street1', '5000', 30, 'basquiat'
    );
INSERT INTO basquiat_member 
	(
	 id, member_city, member_street, member_zipcode, age, name
	)
	VALUES 
    (
	 'user2', 'city2', 'street2', '5001', 31, 'basquiat1'
    );
    
INSERT INTO basquiat_member 
	(
	 id, member_city, member_street, member_zipcode, age, name
	)
	VALUES 
    (
	 'user3', 'city3', 'street3', '5003', 32, 'basquiat2'
    );
```
코드레벨에서 넣을 수 있지만 스크립트로 그냥 한번에 데이터를 넣고 persistence.xml에서 create를 none으로 변경하고 테스트를 해보자.

전통적인 방식의 sql은 다음과 같이 날리게 된다.

```
SELECT * FROM basquiat_member;
```
다음 밑의 코드는 이것을 JPQL로 표현한 것읻.

```
TypedQuery<Member> member = em.createQuery("SELECT m FROM Member m", Member.class);
```
여기서 반환 타입인 TypedQuery에 주목해 보자. 보통 뒤에 어떤 클래스에 매핑되는지 대한 정보가 존재할 경우 사용하게 된다.      

이때 내부에 셀렉트 비슷한 형식의 문법에서 Member는 엔티티를 지칭하는데 만일 Member라는 객체가 여러개 있어서 다음과 같이 작성을 했다고 생각을 해보자.     

```
package io.basquiat.jpql;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "BM")
@Table(name = "basquiat_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {
	
	@Builder
	public Member(String id, String name, int age, Address address) {
		super();
		this.id = id;
		this.name = name;
		this.age = age;
		this.address = address;
	}

	@Id
	private String id;
	
	private String name;
	
	private int age;

	@Embedded
	private Address address;

}
```
@Entity는 name을 지칭하지 않으면 클래스 이름을 따른다. 즉 @Entity(name = "Member")가 기본이 된다. 그것을 @Entity(name = "BM")이라고 지칭하면

```
TypedQuery<Member> member = em.createQuery("SELECT m FROM BM m", Member.class);
```
이라고 작성하게 된다.

이때 JPQL문법의 특징 중 하나는 객체 대상에 대한 별칭을 줘야 한다. 일반적으로 AS는 생략이 가능하기 때문에 위 코드에서는 특별히 AS를 넣지 않았다.      

가장 중요한 것이 이것인데 이 별칭을 통해서 엔티티의 프로퍼티, 즉 변수명으로 접근하게 된다.               

어쨰든 이런 코드 방식을 따르는 것을 보면         

1. 테이블이 아닌 객체를 검색하는 객체지향 쿼리다.       

이 특징이 적용되는 것이다.      

이 코드는 반환 타입이 TypedQuery라는 객체가 되는데 이 코드 자체만으로는 실제로 쿼리가 나가지 않는다.      

그러면 실제 쿼리를 날려서 데이터를 가져와야 하는데 여기서 몇 가지 API를 제공한다.

### .getResultList()     
메소드 명만 봐도 이것은 모든 리스트 객체를 반환하는 것을 알 수 있다.      

말보다는 코드와 결과로 직접 확인하자.

```
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m", Member.class);
        	System.out.println("Start !!!!!!!!!");
        	List<Member> list = queryMember.getResultList();
        	System.out.println(list);

result grid

Start !!!!!!!!!
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
            member0_.name as name6_0_ 
        from
            basquiat_member member0_
[
	Member(id=user1, name=basquiat, age=30, address=Address(city=city1, street=street1, zipcode=5000)), 
	Member(id=user2, name=basquiat1, age=31, address=Address(city=city2, street=street2, zipcode=5001)), 
	Member(id=user3, name=basquiat2, age=32, address=Address(city=city3, street=street3, zipcode=5003))
]
```

### .getSingleResult()
역시 메소드 명으로 알 수 있다.    

코드로 한번 보자.

```
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m", Member.class)
								.setMaxResults(1);
System.out.println("Start !!!!!!!!!");
Member list = queryMember.getSingleResult();
System.out.println(list);

result grid

Start !!!!!!!!!
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
            member0_.name as name6_0_ 
        from
            basquiat_member member0_ limit ?
Member(id=user1, name=basquiat, age=30, address=Address(city=city1, street=street1, zipcode=5000))
```
JPQL 문법에서 LIMIT절을 지원하지 않는데 (왜 안될까???) 그래서 setMaxResults(int maxResult)를 통해서 하나만 가져오게 만들고 테스트 한다.     

이 녀석은 queryDSL에서 fetchOne()와 비슷하다. 하지만 차이점이 있다. fetchOne()은 없으면 null을 반환하게 되어 있는데 이 녀석은 그렇지 않다.      

물론 스프링부트와 연계해서 쓰게 되면 최근에는 null처리를 위해 Optional을 사용하게 되지만 하이버네이트 자체 문법에서는 에러를 뱉는다.     

예를 들면

```
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m", Member.class);
System.out.println("Start !!!!!!!!!");
Member list = queryMember.getSingleResult();
System.out.println(list);

result grid

Start !!!!!!!!!
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
            member0_.name as name6_0_ 
        from
            basquiat_member member0_
javax.persistence.NonUniqueResultException: query did not return a unique result: 3
```
이것은 예상되는 에러인데 아직 배우지 않았지만 다음과 같은 코드의 경우에는      

```
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m WHERE m.id = :id", Member.class)
								.setParameter("id", "dddddd");
System.out.println("Start !!!!!!!!!");
Member list = queryMember.getSingleResult();
System.out.println(list);

result grid

Start !!!!!!!!!
Hibernate: 
    /* SELECT
        m 
    FROM
        Member m 
    WHERE
        m.id = :id */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_ 
        from
            basquiat_member member0_ 
        where
            member0_.id=?
javax.persistence.NoResultException: No entity found for query
	at org.hibernate.query.internal.AbstractProducedQuery.getSingleResult(AbstractProducedQuery.java:1583)
	at io.basquiat.jpaMain.main(jpaMain.java:30)
```
NoResultException을 뱉어 낸다.     

getResultList()의 경우에는 조회된 결과가 없으면 null을 반환하니 신경쓰지 않아도 되지만 getSingleResult()을 사용시에는 주의를 요한다.      

~~하지만 결국 우리는 스프링부트와 쓰게 되겠지~~      

### Parameter Binding
실무에서는 결국에 우리는 데이터를 조회하기 위해서는 조건을 통해서 가져와야 한다.     

이 방식은 preparedStatement의 방식과 거의 유사하다.     

말한 김에 한번 preparedStatement로 어떻게 사용하는지 한번 살펴보자.

```
Connection conn;
PreparedStatement ps = con.prepareStatement("SELECT * FROM basquiat_member where id = ? AND city = ?");
//일반적인 방식 -> index position
ps.setString(1, "id");
ps.setString(2, "city");

/** name parameter binding */
NamedParameterStatement ps = new NamedParameterStatement(con, "SELECT * FROM basquiat_member where id = :id AND city = :city");
ps.setString("id", "id");
ps.setString("city", "city");
```
이렇게 2가지 방식을 제공하는데 JPQL에서도 똑같이 사용할 수 있다.       

1. NamedParameter Binding     

일단 코드로 바로 살펴보자.     

```
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m WHERE m.id = :id AND m.name = :name", Member.class)
								.setParameter("id", "user1")
								.setParameter("name", "basquiat");
System.out.println("Start !!!!!!!!!");
List<Member> list = queryMember.getResultList();
System.out.println(list);

result grid

Start !!!!!!!!!
Hibernate: 
    /* SELECT
        m 
    FROM
        Member m 
    WHERE
        m.id = :id 
        AND m.name = :name */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_ 
        from
            basquiat_member member0_ 
        where
            member0_.id=? 
            and member0_.name=?
[
	Member(id=user1, name=basquiat, age=30, address=Address(city=city1, street=street1, zipcode=5000))
]
```
원하는대로 쿼리도 잘나가고 결과도 잘 가져왔다.     

2. Index Position Parameter Binding     

위치 기반 파라미터 바인딩이라고 하는데 이것은 다음과 같이 사용하게 된다.      

```
int index = 1;
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m WHERE m.id = ?1 AND m.name = ?2", Member.class)
						         .setParameter(index++, "user1")
        							.setParameter(index++, "basquiat");
System.out.println("Start !!!!!!!!!");
List<Member> list = queryMember.getResultList();
System.out.println(list);

result grid

Start !!!!!!!!!
Hibernate: 
    /* SELECT
        m 
    FROM
        Member m 
    WHERE
        m.id = ?1 
        AND m.name = ?2 */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_ 
        from
            basquiat_member member0_ 
        where
            member0_.id=? 
            and member0_.name=?
[
	Member(id=user1, name=basquiat, age=30, address=Address(city=city1, street=street1, zipcode=5000))
]
```
선호하는 방식에 따라 입맛에 맞게 선택하면 될거 같다. 

```
int index = 1;
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m WHERE m.age = ?3 AND m.id = ?1 AND m.name = ?2", Member.class)
      							.setParameter(index++, "user1")
        							.setParameter(index++, "basquiat")
        							.setParameter(index++, 30);
System.out.println("Start !!!!!!!!!");
List<Member> list = queryMember.getResultList();
System.out.println(list);
```
이렇게만 위치를 잘 선정해 준다면 순서와는 상관없이 사용할 수 있다.      

preparedStatement의 경우에는 이 위치가 상당히 중요하기 때문에 무언가가 중간에 껴들어가거나 하면 바인딩부분도 손을 좀 봐야하긴 하지만 JPQL은 문법 자체에 위치를 지정하기 때문에 오히려 휴먼 예러를 좀 방지할 수 있다.

물론 파라미터 바인딩을 사용하지 않고 직접적으로

```
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m WHERE m.age = 30 AND m.id = 'user1' AND m.name = 'basquiat'", Member.class);
System.out.println("Start !!!!!!!!!");
List<Member> list = queryMember.getResultList();
System.out.println(list);
```
사용도 가능하지만 좀 불편하다.      

아주 짧은 예제였지만 뭔가 불편하다. 왜냐하면 쿼리 자체를 작성할 때 스트링의 조합과 스트링을 더해야하는 불편함이 있다.       

또한 이런 이유로 JPQL문법을 작성할 때 발생할 수 있는 오타나 여타 여러가지 휴먼에러로 인해 발생할 수 있는 실수에 대해서도 미리 알 수 없다는 것은 단점이다. ~~그래서 queryDSL같은 녀석이 있다~~     

당연히 스트링의 조합이니 오타가 난다 해서 에러라고 생각하지 않을 테니 말이다.      

예를 들면 동적 쿼리가 참 문제이다.      

회사내부에서도 딱히 어떤 ORM을 사용하는게 아니라서 동적 쿼리를 작성할 때는 일일이 작업을 해줘야 한다.      

예를 들면     

```
let { name, age, id, blah blah... } = req.body;

let sql = 'SELECT * FROM basquiat_item WHERE 1 = 1';

if(searchType) {
 sql += ` AND name = '${name}'
}
.
.
.
```
이런 식의 쿼리를 작성해야 한다.     

실제로 발생했던 에러중 다음과 같은 에러도 있었다.

```
console.log(connection.format(sql, []);

result;

SELECT *
	FROM basquiat_item
	WHERE 1=1AND name = ?AND id = ?
```
스트링을 다루는 도중에 빈 공백을 넣지 않아 쿼리가 '1=1AND name...'같은 식으로 날아가 에러를 발생한 경우도 봤는데 실제로 JPQL에서도 은근히 저런 오류가 많다.      

따라서 좀 길어지는 쿼리를 들여쓰기 방식이나 동적 쿼리 생성시 상당히 주의를 요구하는데 저게 실제로 상당한 스트레스를 준다.      

일단 if문도 많아지게 되면... 암튼 그렇다.       

그러면 이런 의문이 들것이다.      

'지금 우리가 JPQL에 대해 배우고 있는걸 알겠는데 영속성 컨텍스트에 대한 이야기가 없네요.'      

아주 중요한 질문이다.       

하지만 그와 관련된 내용은 밑에 프로젝션을 배워가면서 알아 볼 것이다.

어째든 우리는 실무에서 객체로만 조회해 오지 않는다. 필요에 따라서는 원하는 컬럼의 정보만 가져오게 되는데 결국 Projection에 대한 처리도 당연히 알아야 한다.     

### Projection

1. Scalar Type Projection       

~~scala 아니다~~     

책에서도 언급되긴 하지만 스칼라 타입이라는 것은 보통 DBA분들이 많이 쓰는 용어이다.      

처음 알게 된게 첫 직장에서 나갔던 첫 프로젝트의 현업분이자 기억으로는 차장님이셨던 분이 DBA분이셨는데 이 말을 자주 쓴 기억이 난다.     

스택오버플로우에 [이런 글](https://stackoverflow.com/questions/6623130/scalar-vs-primitive-data-type-are-they-the-same-thing)이 있다.

아무튼 다음 쿼리를 보면 실무에서 볼 수 있는 가장 일반적인 방식이라는 것을 알 수 있다.

```
요구사항: 특정 아이디에 대한 이름과 나이만 알고 싶어.

SELECT name, age FROM basquiat_member WHERE id = ?;
```
쿼리로 치면 위와 같을 것이다. 물론 JPQL도 마찬가지이다.      

```
Query queryMember = em.createQuery("SELECT m.name, m.age FROM Member m WHERE m.id = :id")
					.setParameter("id", "user1");
        	
System.out.println("Start !!!!!!!!!");
@SuppressWarnings("unchecked")
List<Object[]> object = queryMember.getResultList();
object.stream().forEach(obj -> System.out.println(obj[0] + ", " + obj[1]));

result grid

Start !!!!!!!!!
Hibernate: 
    /* SELECT
        m.name,
        m.age 
    FROM
        Member m 
    WHERE
        m.id = :id */ select
            member0_.name as col_0_0_,
            member0_.age as col_1_0_ 
        from
            basquiat_member member0_ 
        where
            member0_.id=?
basquiat, 30
```
튜플은 일종의 어떤 값들의 배열을 의미한다. 그래서 JPQL에서는 반환타입으로 Query객체를 반환하게 된다.      

예들 들면 다음과 같은 쿼리를 날렸다고 생각해 보자.

```
SELECT name, age FROM basquiat_member;
```
만일 여러개의 row데이터가 조회되었다고 한다면 그 형태는 

```
[
	[name, age],
	[name, age],
	[name, age],
	[name, age],
	.
	.
	.
	
]
```
이런 형태로 SELECT 절에 명시한 컬럼의 순서대로 담을 것이다.      

원래는 key/value로 담는게 맞지만 

```
SELECT name, age, member_city, member_street, member_zipcode FROM basquiat_member;

expected result grid

[
	[name, age, member_city, member_street, member_zipcode],
	[name, age, member_city, member_street, member_zipcode],
	[name, age, member_city, member_street, member_zipcode],
	[name, age, member_city, member_street, member_zipcode],
	.
	.
	.
]
```
그것을 결국 Object라는 객체에 뭉뚱그려서 담아내는 형식이라는 것을 알 수 있다.      

당연히... 실무에서는 그대로 쓸 수 없다.      

그래서 DTO를 만들고 거기에다가 담는 방식을 사용해야 한다.      

MemberDTO

```
package io.basquiat.jpql;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class MemberDTO {

	private String id;
	
	private String name;
	
	private int age;
	
	private String city;

	private String street;
	
	private String zipcode;

}
```
Immutable하게 만들기 위해서 @Value를 붙였으며 스칼라 타입의 프로젝션의 경우 DTO에 담기 위해서는 new를 사용하기 때문에 생성자가 필요하다.      

그러면 코드로 한번 살펴보자.     

```
StringBuffer sb = new StringBuffer();
sb.append("SELECT ")
  .append("new io.basquiat.jpql.MemberDTO")
  .append("(")
  .append("m.id, m.name, m.age, m.address.city, m.address.street, m.address.zipcode")
  .append(") ")
  .append("FROM Member m ")
  .append("WHERE m.id = :id");
        	
Query queryMember = em.createQuery(sb.toString(), MemberDTO.class)
				     .setParameter("id", "user1");
        	
System.out.println("Start !!!!!!!!!");
List<MemberDTO> member = queryMember.getResultList();
member.stream().forEach(m -> System.out.println(m.toString()));

result grid

Start !!!!!!!!!
Hibernate: 
    /* SELECT
        new io.basquiat.jpql.MemberDTO(m.id,
        m.name,
        m.age,
        m.address.city,
        m.address.street,
        m.address.zipcode) 
    FROM
        Member m 
    WHERE
        m.id = :id */ select
            member0_.id as col_0_0_,
            member0_.name as col_1_0_,
            member0_.age as col_2_0_,
            member0_.member_city as col_3_0_,
            member0_.member_street as col_4_0_,
            member0_.member_zipcode as col_5_0_ 
        from
            basquiat_member member0_ 
        where
            member0_.id=?
MemberDTO(id=user1, name=basquiat, age=30, city=city1, street=street1, zipcode=5000)


============================== 스칼라 타입과 앞으로 배울 임베디드 타입을 혼용할 수도 있다. ==============================

StringBuffer sb = new StringBuffer();
sb.append("SELECT ")
  .append("new io.basquiat.jpql.MemberDTO")
  .append("(")
  .append("m.id, m.name, m.age, m.address")
  .append(") ")
  .append("FROM Member m ")
  .append("WHERE m.id = :id");
        	
Query queryMember = em.createQuery(sb.toString(), MemberDTO.class)
				  	.setParameter("id", "user1");
        	
System.out.println("Start !!!!!!!!!");
List<MemberDTO> member = queryMember.getResultList();
member.stream().forEach(m -> System.out.println(m.toString()));

result grid

Start !!!!!!!!!
Hibernate: 
    /* SELECT
        new io.basquiat.jpql.MemberDTO(m.id,
        m.name,
        m.age,
        m.address) 
    FROM
        Member m 
    WHERE
        m.id = :id */ select
            member0_.id as col_0_0_,
            member0_.name as col_1_0_,
            member0_.age as col_2_0_,
            member0_.member_city as col_3_0_,
            member0_.member_street as col_3_1_,
            member0_.member_zipcode as col_3_2_ 
        from
            basquiat_member member0_ 
        where
            member0_.id=?
MemberDTO(id=user1, name=basquiat, age=30, address=Address(city=city1, street=street1, zipcode=5000))

```
하지만 코드를 가만히 살펴보면 DTO의 모든 패키지를 전부 다 명시를 해줘야 한다.      

게다가 스트링이기 때문에 만일 매핑할 컬럼이 많아지면 이것도 영 골치가 아프다.      

JPA 양반! 꼭 이렇게 뿐이 안되는거요? ~~이래서 queryDSL을 써야 하는건가....~~      

2. Embedded Type Projection       

위 예제에서는 임베디드 타임과 함께 쓰는 방식의 테스트도 넣어놨다.      

그러면 실제로 임베디드 타입의 정보만 가져올 수 있을까라는 의문이 들텐데 당연히 객체를 대상으로 하기 때문에 가능하다.      

```
TypedQuery<Address> queryMember = em.createQuery("SELECT m.address FROM Member m WHERE m.id = :id", Address.class)
								 .setParameter("id", "user1");
        	
System.out.println("Start !!!!!!!!!");
List<Address> address = queryMember.getResultList();
address.stream().forEach(addr -> System.out.println(addr.toString()));

result grid

Start !!!!!!!!!
Hibernate: 
    /* SELECT
        m.address 
    FROM
        Member m 
    WHERE
        m.id = :id */ select
            member0_.member_city as col_0_0_,
            member0_.member_street as col_0_1_,
            member0_.member_zipcode as col_0_2_ 
        from
            basquiat_member member0_ 
        where
            member0_.id=?
Address(city=city1, street=street1, zipcode=5000)
```

3. Entity Type Projection

말 그대로 우리가 지금까지 객체로 가져왔던 방식이다.      

특히 이 녀석의 경우에는 영속성 컨텍스트의 영향을 받는다.     

말이 필요없다. 그냥 코드로 보자.

그 전에 Member엔티티에 @DynamicUpdate를 붙여보고 age프로퍼티에 @Setter를 달고 시작하자.     

```
TypedQuery<Member> queryMember = em.createQuery("SELECT m FROM Member m WHERE m.id = :id", Member.class)
								.setParameter("id", "user1");
        	
System.out.println("Start !!!!!!!!!");
List<Member> member = queryMember.getResultList();
member.stream().forEach(mbr -> {
				mbr.setAge(31);
				System.out.println(mbr.toString());
			});
			
result grid

Start !!!!!!!!!
Hibernate: 
    /* SELECT
        m 
    FROM
        Member m 
    WHERE
        m.id = :id */ select
            member0_.id as id1_0_,
            member0_.member_city as member_c2_0_,
            member0_.member_street as member_s3_0_,
            member0_.member_zipcode as member_z4_0_,
            member0_.age as age5_0_,
            member0_.name as name6_0_ 
        from
            basquiat_member member0_ 
        where
            member0_.id=?
Member(id=user1, name=basquiat, age=31, address=Address(city=city1, street=street1, zipcode=5000))
Hibernate: 
    /* update
        io.basquiat.jpql.Member */ update
            basquiat_member 
        set
            age=? 
        where
            id=?
```
30에서 31로 변경하면서 dirty checking이 발생하며 age를 업데이트하는 쿼리가 나가는 것을 확인할 수 있다.      

그러면 이런 생각도 해볼 수 있겠다.     

'멤버가 있으면 팀이 있을 수도 있는데 이 경우에도 가능한가요?'       

그것을 테스트하기 위해서는 일단 다음과 같이 Team이라는 엔티티를 만들어보자.      

아~ 그전에 옵션을 none에서 create로 변경하자.      

Team

```
package io.basquiat.jpql;

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
import lombok.ToString;

@Entity
@Table(name = "basquiat_team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Team {

	@Builder
	public Team(String teamName) {
		super();
		this.teamName = teamName;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "name")
	@Setter
	private String teamName;
	
}
```

Member입장에서는 @ManyToOne의 관계를 가질 수 있기 때문에 Member 엔티티도 변경하자. 테스트에서는 단방향만 잡아보자.     

```
package io.basquiat.jpql;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "basquiat_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@DynamicUpdate
public class Member {
	
	@Builder
	public Member(String id, String name, int age, Address address, Team team) {
		super();
		this.id = id;
		this.name = name;
		this.age = age;
		this.address = address;
		this.team = team;
	}

	@Id
	private String id;
	
	private String name;
	
	@Setter
	private int age;

	@Embedded
	private Address address;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

}
```
이제 코드를 한번 실행해 보자.

```
Team myTeam = Team.builder().teamName("Default Team").build();
em.persist(myTeam);
        	
// address는 그냥 비우자.
Member member = Member.builder().id("basquiat")
								.name("Jean-Michel Basquiat")
								.age(28)
								.team(myTeam)
								.build();
em.persist(member);
em.flush();
em.clear();

TypedQuery<Team> selected = em.createQuery("SELECT m.team FROM Member m", Team.class);
Team selectedTeam = selected.getSingleResult();
selectedTeam.setTeamName("Basquiat Team");
System.out.println(selectedTeam);

result grid

Hibernate: 
    /* insert io.basquiat.jpql.Team
        */ insert 
        into
            basquiat_team
            (name) 
        values
            (?)
Hibernate: 
    /* insert io.basquiat.jpql.Member
        */ insert 
        into
            basquiat_member
            (member_city, member_street, member_zipcode, age, name, team_id, id) 
        values
            (?, ?, ?, ?, ?, ?, ?)
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
Team(id=1, teamName=Basquiat Team)
Hibernate: 
    /* update
        io.basquiat.jpql.Team */ update
            basquiat_team 
        set
            name=? 
        where
            id=?
```
이 경우도 결국 엔티티를 반환하기에 영속성 컨텍스트에서 관리되고 변경 감지에 대해서 반응을 하게 된다.        

여기까지가 가장 기본적인 조회 방식이다.      

다음 브랜치에서는 본격적인 페이징과 정렬, 조인등을 한번 알아보자.

# At A Glance      

그냥 지금까지 문법에 대해서 쭉 알아봤는데 간과하면 안되는 것은 결국 앞으로 할 내용들은 SQL에 대한 이해도가 어느 정도 있어야 한다.      