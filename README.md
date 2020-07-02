# Entity Mapping

이제는 본격적으로 엔티티와 물리적인 DB의 테이블과 어떻게 매핑하는지에 대해 알아 볼 것이다.

## @Entity    

지금까지 줄 곳 봐왔던 어노테이션이다. 이 어노테이션이 붙어있다면 이제부터 이 객체는 JPA가 관리하겠다는 의미를 가지고 있다.    

속성으로는 name을 가지고 있다.     

```
e.g @Entity(name = "Item")
```

기본적으로 name 속성을 주지 않으면 해당 어노테이션이 붙은 클래스의 이름을 기본으로 가져간다.    

Item.java, OtherItem.java의 예를 들면 지금까지는 name을 나의 입맛대로 썼지만 ~~이러면 안되는거야~~ 사실 이 것은 왠간하면 쓸 일이 없다.     

보통은 뒤에 설명할 @Table에 설정하는 경우가 많기 때문이다.    

그럼 언제 쓰는데??    

라는 의문을 가질 수 있다.    

만일 다른 패키지, 즉 io.basquiat.model에 속한 Item클래스 말고 com.basquiat.model에 Item이라는 똑같은 이름의 클래스가 있가 있고 그 녀석 역시 JPA에서 관리하는 엔티티라면 충돌을 방지하기 위해서 name속성을 통해 구분지을 수 있다.    

하지만 이런 경우는 못 만나 본거 같다.    

결론적으로 name은 JPA가 관리하기 위해 엔티티들을 구분하기 위한 일종의 아이디같은 녀석이라고 봐도 무방할 것이다.    

따라서 위와 같은 특별한 경우가 아니라면 가급적으로 기본으로 두는 것을 권장하고 있다. 


@Entity는 다음과 같은 특징을 가지고 있다.    

1. 파라미터가 없는 public 또는 protected로 만들어진 기본 생성자는 필수이다.    
2. final class, enum, interface, inner class에는 사용할 수 없다.
3. final 키워드가 붙은 필드에는 사용할 수 없다.


## @Table    
지금까지 @Entity에 name을 줘서 테스트해왔다. 눈썰미 있으신 분들은 알아챘을텐데 그 이름으로 table이 생성된 것을 봤을 것이다. 위에 언급했던 내용으로 유추해 보면 엔티티를 구분짓는 이 값을 가지고 그대로 테이블명으로 만들었다는 생각을 가지게 된다.
    
하지만 엄연히 @Table이라는 녀석이 존재한다.    

이 녀석도 다음과 같이 속성을 가지고 있다.    

1. name: 매핑할 테이블 이름, 기본적으로 설정하지 않으면 @Entitiy처럼 클래스명을 기본값으로 한다.     
2. catalog 데이터베이스의 catalog와 매핑한다.    
3. schema 데이터베이스의 schema 매핑와 매핑한다.     
4. uniqueConstraints: DDL 생성 시에 유니크 제약 조건을 생성한다.    


일단 name을 왜 쓰느냐는 실제 클래스명과 물리적인 DB의 테이블명과 불일치할 확률이 거의 99.9999%이기 때문이다.
많은 프로젝트에서 테이블명을 봐왔는데 대부분 이런 방식의 이름들이 많다.    

회사명이 만일 'pineapple'이라면 pa_member, pa_item, pa_order같은 경우가 거의 99.9999%로 였다. 진짜 희귀한 케이스로 그렇지 않은 경우를 보기 했지만 어째든 그렇다는 것이다.    

그렇다고 클래스명을 PineappleMember이러고 싶진 않.....    

이러한 불일치를 해결하기 위해서 name속성을 통해서 물리적인 디비 테이블 명과 매핑할 때 사용한다.

그러면 uniqueConstraints 이녀석은 뭔가요? ~~먹는거 아니에요~~    

catalog와 schema는 거의 설명이 없는데 이것은 uniqueConstraints 설명 이후에 하겠다.     

유니크 제약 조건이라는게 무엇일까?    

한마디로 어떤 컬럼에는 같은 값이 들어가면 안된다는 것을 의미한다. 무결성 제약이라는 표현을 많이 들어봤을텐데 예를 들면 기본적으로 기본키 매핑의 경우에는 무결성 제약 조건이 그냥 들어간다. 유니크해야한다는 의미지.    

굳이 설명할 필요가 있을까만은 설명충이니깐 설명하고 넘어가자.    
만일 이런 무결성 제약 조건이 없다면 아이디가 1인 녀석이 몇 개가 더 생길 수 있다는 것이다.

나는 id가 1인 녀석을 조회하고 싶은데 2, 3개가 넘어오는 상황은 좀 이상하지 않을까?    

불필요하고 원치 않은 데이터가 쌓일 수 있는 여지 자체를 주지 않는 것이다.    

실제로 이런 제약 조건이 걸려 있는 컬럼에 같은 값이 들어가면 바로 무결성 제약 조건에 해당하는 에러를 볼 수 있다.    

일단 그렇다는 것이고 그러면 유니크 제약 조건이라는 것은 무엇일까 Primary Key의 경우에는 오직 이 하나에 대해서 걸린다.   

하지만 유니크 제약 조건은 하나의 컬럼에 걸수도 있고 몇 개의 컬럼에 걸 수 있다.

만일 그냥 예시로 지금 당장 생각나는게 없고 그런걸로 고민하고 싶지 않으니 그냥 a_type, b_type이라는 컬럼에 테이블에 있다고 하자. a_type과 b_type에 똑같은 값이 들어가는 것을 막고 싶으면 이 두개의 컬럼을 묶어서 유니크 제약 조건을 걸면 된다.   

그렇게 되면 item이라는 테이블에 다음과 같이

```

|id    |item_code|a_type|b_type|
|------|---------|------|------|
|     1|  ITEM001|     A|     B| 

```

이런 값이 들어가 있다면 다음과 같이 쿼리를 날리게 되면 이 유니크 제약 조건에 걸려서 인서트가 되지 않고 에러가 난다.

```
INSERT INTO item
	(item_code, a_type, b_type)
	VALUES
	(ITEM002, A, B )

```

저 중에 B가 아니라 C나 D를 넣으면 유니크 제약 조건에 걸리지 않으니 들어가게 된다.

설명으로 하자니 뭔가 어설픈데 이것도 그냥 링크로...    

[제약조건, Constraint](https://runtoyourdream.tistory.com/129)
 

그리고 코드를 보면 알겠지만 

```

@Table(name = "basquiat_other_item", 
	   uniqueConstraints = {
				@UniqueConstraint(
					name = "UNIQUE_AType_BType",
					columnNames = {"aType", "bType"}
				)
			}		
	 )


```
처럼 추가한다. 뭔가 복잡하긴 한데...  그 중에 name = "UNIQUE_AType_BType" 이 부분을 잠깐 설명하자면 이 것을 주지 않으면 UKp0een3epa6t8km6k75x48593o <-- 이렇게 생긴 괴랄한 유니크 이름이 생성된다.    

사실 DB의 테이블의 속성 정보를 보면 해당 테이블에 조건 정보들을 볼 수 있는데 이런 이름으로 이 녀석이 뭔지 알 수 없다.

따라서 이렇게 이름을 지정하면 지정된 이름으로 생성하게 된다.    

대부분 좀 큰 회사에는 이런 이름을 작성하는 규칙이 있을텐데 그에 맞춰서 작성하면 된다.    

유니크 제약 관련해서 그냥 컬럼 하나에만 걸고 싶은 경우에는 필드에 직접 주는 방법도 있는데 난 중에 알아보겠지만 이렇게 이름을 직접 명명할 수 있어서 이 방법이 다소 뭔가 복잡해 보여도 이 방법을 선호한다.

그러면 백문이 불여일타!    

다음과 같이 2개의 테스트용 필드를 추가했다.    


```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
@Table(name = "basquiat_other_item", 
	   uniqueConstraints = {
				@UniqueConstraint(
					name = "UNIQUE_AType_BType",
					columnNames = {"aType", "bType"}
				)
			}		
	 )
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class OtherItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	private String name;

	@Setter
	@Getter
	private Integer price;

	@Setter
	private String aType;
	
	@Setter
	private String bType;
	
	
	@Builder
	public OtherItem(Long id, String name, Integer price, String aType, String bType) {
		if(StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("item name must be not null"); 
	    }
		if(price == null || price < 0) {
			throw new IllegalArgumentException("price must be not under 0"); 
	    }
		
		this.id = id;
		this.price = price;
		this.name = name;
		this.aType = aType;
		this.bType = bType;
	}
	
}


```

그리고 다음과 같이 코드를 작성해 보자.

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
        	
        	OtherItem foderaBass = OtherItem.builder().name("Fodera Emperor2 5")
        									     .price(15000000)
        									     .aType("A")
        									     .bType("B")
        									     .build();
        	
        	em.persist(foderaBass);
        	OtherItem fenderJBass = OtherItem.builder().name("Fender Jazz Bass")
				     .price(15000000)
				     .aType("A")
				     .bType("C")
				     .build();
        	
        	em.persist(fenderJBass);
        	OtherItem fenderPBass = OtherItem.builder().name("Fender Precision Bass")
				     .price(15000000)
				     .aType("A")
				     .bType("B")
				     .build();

        	em.persist(fenderPBass);
        	
        	
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
ERROR: Duplicate entry 'A-B' for key 'basquiat_other_item.UNIQUE_AType_BType'

```

요렇게 한줄의 무결성 제약 조건에 위배된다는 에러를 뱉어 낸다.    

실제 지금처럼 코드를 차면 아마 데이터가 하나도 들어가지 않을것이다.    

롤백을 해버리기 때문인데 실제로 눈으로 확인하고 싶다면 몇개의 데이터를 집어넣고 hibernate.hbm2ddl.auto 옵션을 none으로 해서 테스트해보면 명확하게 알 수 있다.    


자 그럼..이제 catalog와 schema와 관련해서 이야기해보고자 한다.    

긴 이야기가 될것이다. 어쩌면 이것으로 이번 브랜치는 마무리할 것이다.    

사실 이부분에 대해서 자세하게 설명한 것이 없어서 대부분 이런 옵션이 있는지 모르는 분들이 많다.     

알고 있어서 잘 쓰지도 않기도 하고....     

나의 경우에는 이전 암호화폐 거래소에서 거래소 API서버 관련 작업을 하면서 이것을 써보게 된 계기가 되었는데 당시 사용한 DB가 postgresql이었기 때문에 가능했을지도 모른다.

일단 mySQL 얘기를 먼저 해보자.

그전에 다음 링크를 한번 훝어 보고 오길 바란다.

[스택오버플로](https://stackoverflow.com/questions/7942520/relationship-between-catalog-schema-user-and-database-instance/7944489)

mySql관련 database == schema == catalog == a namespace within the server.    

이런 문구를 볼 수 있다.   

namespace라는 말에 주목을 해야 하는 것이다.    

그건 뭔데?    

다음 링크로...

[namespace](https://docs.microsoft.com/ko-kr/cpp/cpp/namespaces-cpp?view=vs-2019)

링크는 좀 어렵게 풀었는데 쉽게 말하면 어떤 공간을 떠올리면 된다.    


예를 들면 우리는 mySql을 깔면 기본적으로 sys라는 database를 처음 보게 된다.    

sys라는 공간을 의미하고 있으며 이 공간안에 뭔지 몰라도 데이터가 존재할 것이다.     

암튼 실제로 운영에서는 sys를 쓰지 않는다. 프로젝트나 어플리케이션에 맞게 데이터베이스를 생성한다.

```
create database basquiat;
```

그리고 oracle같은 경우에는 회사 상황에 맞춰서 유저를 추가하고 그에 맞는 권한을 준다.    

근데 저 위에 말대로라면 mySql은 postgres와는 좀 다르다. 

database == schema == catalog == a namespace within the server. 이 말그대로 똑같은 녀석이라는 의미인데 mySql의 경우 저 말은 반은 맞고 반은 틀리다.

예를 들면 workBench에서 database를 삭제하면 drop schema라고 뜬다.     

오케이!! 그래 database == schema라는 말이 맞는가보다. 하지만 JPA에서는 schema 옵션은 database와 동일하지 않은 듯 싶다.    

일단 우리는 그것을 확인하기 위해 persistene.xml중 다음을 보자.

```
<property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/basquiat?rewriteBatchedStatements=true&amp;useUnicode=yes&amp;characterEncoding=UTF-8&amp;serverTimezone=Asia/Seoul"/>
            
```

통상적으로 jdbc:mysql://localhost:3306/뒤에 database명을 기입한다.    

일단 schema의 경우에는 mySql에서는 지원하지 않는듯 싶다.    

그럼 catalog == database라는 말에 대해서 한번 고민해 보자.

그전에 schema에 대한 테스트 코드 먼저 시작할 것이다.    

우리는 그러기 위해서는 persistenc.xml의 저 부분을 다음과 같이 수정을 하자.

```
<property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306?rewriteBatchedStatements=true&amp;useUnicode=yes&amp;characterEncoding=UTF-8&amp;serverTimezone=Asia/Seoul"/>
            
```

어떤 database에 접근할 건지를 명시하지 않고 mySql Server에만 접속한다고 명시한다.

그리고 우리는 DBeaver나 workBench등 툴을 이용하든 일단 jpa라는 database를 생성하자.

그다음에 우리는 기존의 코드를 살펴볼까 한다. 일단 전 브랜치의 코드를 사용해서 기본키 전략 매핑으로 테이블 전략을 사용한 것까지 돌아가자.
 
 OtherItem
 
```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Entity
@Table(name = "basquiat_other_item", schema = "jpa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class OtherItem {

	@Id
	@GenericGenerator(
            name = "SequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "hibernate_sequence"),
                    @Parameter(name = "schema", value = "jpa"),
                    @Parameter(name = "optimizer", value = "pooled"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "100")
            }
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
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

위 코드에서 @Parameter(name = "schema", value = "jpa") 코드도 한번 살펴보기 바란다.

hibernate_sequence테이블 생성시 마치 @Tabel의 schema속성처럼 작동하게 된다.

Item

```
package io.basquiat.model;

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
@Table(name = "basquiat_item", schema = "jpa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	private String name;

	@Setter
	@Getter
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

그리고 다음과 같이 실행해보자.

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
        	
        	OtherItem foderaBass = OtherItem.builder().name("Fodera Emperor2 5")
        									     .price(15000000)
        									     .build();
        	
        	em.persist(foderaBass);
        	OtherItem fenderJBass = OtherItem.builder().name("Fender Jazz Bass")
				     .price(15000000)
				     .build();
        	
        	em.persist(fenderJBass);
        	OtherItem fenderPBass = OtherItem.builder().name("Fender Precision Bass")
				     .price(15000000)
				     .build();

        	em.persist(fenderPBass);
        	
        	
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

최종적으로는 다음과 같은 에러가 발생한다.

```
at org.hibernate.tool.schema.internal.exec.GenerationTargetToDatabase.accept(GenerationTargetToDatabase.java:67)

```

database를 찾을 수 없어서 나는 에러이다.

그럼 지금 엔티티 객체들의 코드에서 schema를 catalog로 바꿔보고 실행해 보자.

물론 OtherItem의 @GenericGenerator에 설정한 값중 @Parameter(name = "catalog", value = "jpa") 이 부분도 바꿔주자 그렇지 않으면 hibernate_sequence을 어디에 생성할지 모르기 때문에 생성을 하지 못한다.


자 실행하면 어떤 일이 벌어질까?


```
Hibernate: 
    
    drop table if exists jpa.basquiat_item
Jul 02, 2020 4:49:45 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@5bb3d42d] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    drop table if exists jpa.basquiat_other_item
Hibernate: 
    
    drop table if exists jpa.hibernate_sequence
Hibernate: 
    
    create table jpa.basquiat_item (
       id bigint not null auto_increment,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Jul 02, 2020 4:49:45 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@61019f59] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table jpa.basquiat_other_item (
       id bigint not null,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table jpa.hibernate_sequence (
       next_val bigint
    ) engine=InnoDB
Hibernate: 
    
    insert into jpa.hibernate_sequence values ( 1 )
Jul 02, 2020 4:49:45 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    select
        next_val as id_val 
    from
        jpa.hibernate_sequence for update
            
Hibernate: 
    update
        jpa.hibernate_sequence 
    set
        next_val= ? 
    where
        next_val=?
Hibernate: 
    select
        next_val as id_val 
    from
        jpa.hibernate_sequence for update
            
Hibernate: 
    update
        jpa.hibernate_sequence 
    set
        next_val= ? 
    where
        next_val=?
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            jpa.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            jpa.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            jpa.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Jul 02, 2020 4:49:45 PM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    18221742 nanoseconds spent acquiring 3 JDBC connections;
    1070613 nanoseconds spent releasing 3 JDBC connections;
    12659413 nanoseconds spent preparing 7 JDBC statements;
    4865636 nanoseconds spent executing 7 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    18015202 nanoseconds spent executing 1 flushes (flushing a total of 3 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
Jul 02, 2020 4:49:45 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306?rewriteBatchedStatements=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]

```

뭔가 좀 이상하지 않은가? ~~잘봐요! 로그가 좀 달라졌어요~~   

이미 경험있거나 눈썰미 좋다면 쿼리 로그에 엔티티객체앞에 jpa.이 붙은 것을 알 수 있을것이다.

mySql에서 보통 여러개의 database가 있다고 치면 툴이나 cli에서는 다음과 같이 조회를 하게 된다.


```
use basquiat;

SELECT * FROM basquiat_item;

```

하지만 sql browser에서 다른 데이터베이스의 테이블을 조회할려면 어떻게 하겠나? 

```
use jpa;
SELECT * FROM basquiat_item;

```

이렇게 하겠지? 하지만 다음과 같이 할수도 있다.

```
use basquiat;

SELECT * FROM jpa.basquiat_item;


```

물론 다른 데이터베이스끼리의 테이블 조인도 가능하다.


자 그럼 이렇게도 할수 있지 않을까?

Item은 catalog정보를 "basquiat"라고 주자.

```
package io.basquiat.model;

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
@Table(name = "basquiat_item", catalog = "basquiat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	private String name;

	@Setter
	@Getter
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

그리고 실행해서 직접 눈으로 보자.    

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
        	
        	Item bass = Item.builder().name("Fodera Emperor2 5")
								     .price(15000000)
								     .build();
        	
        	em.persist(bass);
        	
        	OtherItem foderaBass = OtherItem.builder().name("Fodera Emperor2 5")
        									     .price(15000000)
        									     .build();
        	
        	em.persist(foderaBass);
        	OtherItem fenderJBass = OtherItem.builder().name("Fender Jazz Bass")
				     .price(15000000)
				     .build();
        	
        	em.persist(fenderJBass);
        	OtherItem fenderPBass = OtherItem.builder().name("Fender Precision Bass")
				     .price(15000000)
				     .build();

        	em.persist(fenderPBass);
        	
        	
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
    
    drop table if exists basquiat.basquiat_item
Jul 02, 2020 5:01:15 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@5bb3d42d] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    drop table if exists jpa.basquiat_other_item
Hibernate: 
    
    drop table if exists jpa.hibernate_sequence
Hibernate: 
    
    create table basquiat.basquiat_item (
       id bigint not null auto_increment,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Jul 02, 2020 5:01:15 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@1162410a] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table jpa.basquiat_other_item (
       id bigint not null,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table jpa.hibernate_sequence (
       next_val bigint
    ) engine=InnoDB
Hibernate: 
    
    insert into jpa.hibernate_sequence values ( 1 )
Jul 02, 2020 5:01:15 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat.basquiat_item
            (name, price) 
        values
            (?, ?)
Hibernate: 
    select
        next_val as id_val 
    from
        jpa.hibernate_sequence for update
            
Hibernate: 
    update
        jpa.hibernate_sequence 
    set
        next_val= ? 
    where
        next_val=?
Hibernate: 
    select
        next_val as id_val 
    from
        jpa.hibernate_sequence for update
            
Hibernate: 
    update
        jpa.hibernate_sequence 
    set
        next_val= ? 
    where
        next_val=?
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            jpa.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            jpa.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            jpa.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Jul 02, 2020 5:01:15 PM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    17455910 nanoseconds spent acquiring 3 JDBC connections;
    1256340 nanoseconds spent releasing 3 JDBC connections;
    13233585 nanoseconds spent preparing 8 JDBC statements;
    6980246 nanoseconds spent executing 8 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    14157332 nanoseconds spent executing 1 flushes (flushing a total of 4 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}

```
 
일단 schema 옵션의 경우에는 mySql이 지원하지 않는다고 보면 될것 같다. 

어쨰든 catalog를 활용한 이 방법이 뭔가 멋져 보이고 할 수 있지만 이렇게 설계하기 위해서는 독립된 database에 대한 명확한 경계가 필요하게 된다.

또한 Multi Tenancy를 위해서 분리를 한것이라면 더욱 더 주의가 필요하고 그만큼 관리 포인트가 늘어나게 된다.

물론 확인해 본적은 없지만 이렇게 database를 catalog에 따라 스왑하는 것에 대한 비용도 존재하지 않을까???

~~여러분이 multi tenancy별 통계비스무리한 것을 위해 이런 괴랄한 쿼리를 날릴리가 없다..~~     

```

SELECT jpa.*, 
       basquiat.* 
   FROM jpa.basquiat_other_item jpa 
   LEFT JOIN basquiat.basquiat_item basquiat ON jpa.id = basquiat.id; 

result

id |			name	         |   	price  | id   |			name		|  price   |
1  |	Fodera Emperor2 5	 	| 15000000 |	1    |	Fodera Emperor2 5	| 15000000 |
2  |	Fender Jazz Bass	 	| 15000000 |	null |		null			|		  |
3  |	Fender Precision Bass	| 15000000 |	null |		null			|		  |	


```

이제는 schema에 대해서 알아보자.    

그러기 위해서는 이것을 지원하는 postgresql를 깔자.    

이제부터 옵션인데 '나는 이딴거 알고 싶지 않아!!!! 그리고 그딴거 깔고 싶지 않아'라고 한다면 여기서 멈추면 되는 것이다.     

~~이런 거..겁쟁이 같으니!!~~

postgresql설치 방법은 구글에서 찾아보시기 바란다.
.     
.      
.     
.      
.     
.     
.     
.    
.     
.    
.     
.    
.        
.             
.              
.               
.                
.                 
.                  
.                  
라고 하고 링크 걸어주기     

[PostgreSql 설치하러 가자!](https://dora-guide.com/postgresql-install/)    

다 설치하면 이 글을 쓰고 있는 현재 최신 버전 기준 (12.3.1) 으로 코끼리 모양의 pgAdmin 4를 볼 수 있다.    

웹 형식으로 뜨게 되고 설치시 입력한 비번을 치면 화면을 볼 수 있다.    


브라우져내에서 상당에 보면 돋보기에서 왼쪽 끝에 있는 아이콘을 클릭하면 query Editor를 볼 수 있다. 클릭해서 데이터베이스를 생성하자.

나는 평소에 쓰던 basquiat라고 생성했다.

```

create database basquiat;


```

평소에 보던 mySql과는 많이 다를 것이다. 

```
PostgreSQL
 ㄴ Databases
 	ㄴ basquiat
 		ㄴ Casts
 		ㄴ Catalogs
 		ㄴ
 		.
 		.
 		ㄴSchemas
			ㄴ public
				.
				.
				ㄴ Sequence
				ㄴ Tables
				.
				.

```

이렇게 무언가 복잡한 hierachy 구조가 보일 것이다.    

우리는 이 중에 schemas에 관심을 가져야 한다. 일단 저렇게 하고 @Table에 name을 제외한 옵션을 제외하고 코드를 실행하게 되면 기본적인 schemas의 값은 public내부로 타고 들어간다.

그 public안에 우리에게 익숙한 것들이 눈에 들어 올것이다.    


일단 maven과 persistence.xml 설정을 새로 하자.

pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>io.basquiat</groupId>
	<artifactId>completedJPA</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
		<maven.test.skip>true</maven.test.skip>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-entitymanager</artifactId>
			<version>5.4.17.Final</version>
		</dependency>
		
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>8.0.20</version>
		</dependency>
	
		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<version>42.2.14</version>
		</dependency>
	
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.10</version>
		</dependency>
	
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<version>1.18.12</version>
			<scope>provided</scope>
		</dependency>
	
		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-api</artifactId>
			<version>5.6.2</version>
			<scope>test</scope>
		</dependency>
	
		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-engine</artifactId>
			<version>5.6.2</version>
			<scope>test</scope>
		</dependency>
	
	</dependencies>

</project>

````

persistence.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2"
             xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <persistence-unit name="basquiat">
        <properties>
            <!-- database configuration -->
            <property name="javax.persistence.jdbc.driver" value="org.postgresql.Driver"/>
            <property name="javax.persistence.jdbc.user" value="postgres"/>
            <property name="javax.persistence.jdbc.password" value="basquiat"/>
            <property name="javax.persistence.jdbc.url" value="jdbc:postgresql://localhost/basquiat"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
            
            <!-- 
            <property name="javax.persistence.jdbc.driver" value="com.mysql.jdbc.Driver"/>
            <property name="javax.persistence.jdbc.user" value="basquiat"/>
            <property name="javax.persistence.jdbc.password" value="basquiat"/>
            <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306?rewriteBatchedStatements=true&amp;useUnicode=yes&amp;characterEncoding=UTF-8&amp;serverTimezone=Asia/Seoul"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.MySQL5InnoDBDialect"/>
             -->
            <!-- option configuration -->
            <!-- 콘솔에 sql 날아가는거 보여주는 옵션 -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.use_sql_comments" value="true"/>
            <property name="hibernate.generate_statistics" value="true"/>
            <!--
            <property name="hibernate.jdbc.batch_size" value="20"/>
            <property name="hibernate.order_inserts" value="true"/>
            -->
            <!-- 이 옵션에 들어가는 것은 그냥 쓰지 말자. 테스트 용도 또는 개인 토이 프로젝트를 하는게 아니라면 validate정도까지만 그게 아니면 운영은 none으로 설정하자 -->
            <!-- 실제 none이라는 옵션은 없다. 따라서 none으로 하면 아무 일도 일어나지 않는다. -->
            <property name="hibernate.hbm2ddl.auto" value="create" />
        </properties>
    </persistence-unit>
</persistence>

```

참고로 postgres는 

```
<property name="javax.persistence.jdbc.url" value="jdbc:postgresql://localhost:5432"/>
```

와 같이 mySql처럼 사용할 수 가 없다.

일단 앞서 본인이 만들어둔 database로 접근하자.

그리고 나서 우리는 schema를 만들것이다. 결국 basquiat라는 database안에 공간인 schema를 만들것이다.

나는 다음과 같이 schema를 만들었다.

```

create schema sales_team;

create schema management_team;

```

이제부터는 다음과 같이 코드를 작성하자.

Item

```
package io.basquiat.model;

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
@Table(name = "basquiat_item", schema = "sales_team")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	private String name;

	@Setter
	@Getter
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


OtherItem

```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Entity
@Table(name = "basquiat_other_item", schema = "management_team")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class OtherItem {

	@Id
	@GenericGenerator(
            name = "SequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "hibernate_sequence"),
                    @Parameter(name = "schema", value = "management_team"),
                    @Parameter(name = "optimizer", value = "pooled"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "100")
            }
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
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

Postgres는 sequence를 지원한다.

전략은 테이블 전략을 썼지만 시퀀스를 생성할 것이다.

자 그럼 우리는 실행을 해보는 것이다.

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
        	
        	Item bass = Item.builder().name("Fodera Emperor2 5")
								     .price(15000000)
								     .build();
        	
        	em.persist(bass);
        	
        	OtherItem foderaBass = OtherItem.builder().name("Fodera Emperor2 5")
        									     .price(15000000)
        									     .build();
        	
        	em.persist(foderaBass);
        	OtherItem fenderJBass = OtherItem.builder().name("Fender Jazz Bass")
				     .price(15000000)
				     .build();
        	
        	em.persist(fenderJBass);
        	OtherItem fenderPBass = OtherItem.builder().name("Fender Precision Bass")
				     .price(15000000)
				     .build();

        	em.persist(fenderPBass);
        	
        	
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

결과는 두둥!!

```
Jul 02, 2020 6:50:55 PM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
Jul 02, 2020 6:50:55 PM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
Jul 02, 2020 6:50:55 PM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
Jul 02, 2020 6:50:55 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Jul 02, 2020 6:50:55 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [org.postgresql.Driver] at URL [jdbc:postgresql://localhost/basquiat]
Jul 02, 2020 6:50:55 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=postgres, password=****}
Jul 02, 2020 6:50:55 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jul 02, 2020 6:50:56 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Jul 02, 2020 6:50:56 PM org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.PostgreSQLDialect
Hibernate: 
    
    drop table if exists management_team.basquiat_other_item cascade
Jul 02, 2020 6:50:56 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@2d0566ba] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    drop sequence if exists management_team.hibernate_sequence
Hibernate: 
    
    drop table if exists sales_team.basquiat_item cascade
Hibernate: create sequence management_team.hibernate_sequence start 1 increment 100
Jul 02, 2020 6:50:56 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@2c444798] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table management_team.basquiat_other_item (
       id int8 not null,
        name varchar(255),
        price int4,
        primary key (id)
    )
Hibernate: 
    
    create table sales_team.basquiat_item (
       id  bigserial not null,
        name varchar(255),
        price int4,
        primary key (id)
    )
Jul 02, 2020 6:50:56 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            sales_team.basquiat_item
            (name, price) 
        values
            (?, ?)
Hibernate: 
    select
        nextval ('management_team.hibernate_sequence')
Hibernate: 
    select
        nextval ('management_team.hibernate_sequence')
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            management_team.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            management_team.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.OtherItem
        */ insert 
        into
            management_team.basquiat_other_item
            (name, price, id) 
        values
            (?, ?, ?)
Jul 02, 2020 6:50:56 PM org.hibernate.engine.internal.StatisticalLoggingSessionEventListener end
INFO: Session Metrics {
    24987 nanoseconds spent acquiring 1 JDBC connections;
    24483 nanoseconds spent releasing 1 JDBC connections;
    432543 nanoseconds spent preparing 6 JDBC statements;
    2913387 nanoseconds spent executing 6 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    0 nanoseconds spent performing 0 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    11517981 nanoseconds spent executing 1 flushes (flushing a total of 4 entities and 0 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
}
Jul 02, 2020 6:50:56 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:postgresql://localhost/basquiat]


```

오호라! 이것은 앞서 mySql에서 테스트했던 그것과 똑같네.    

```
SELECT mng.*,
       sales.* 
	FROM management_team.basquiat_other_item mng 
	LEFT JOIN sales_team.basquiat_item sales ON mng.id = sales.id

이것도 똑같다.
```

사실 내가 기대했던 것은 이런 것이다. catalog.schema.table로 접근을 기대했는데 코드상에서 되지 않는다.

아니면 내가 놓친게 있을텐데 자료가 거의 전무해서 일단 schema 테스트에 의의를 두고 싶다.

하긴 위에서 걸어두었던 스택오버플로우에서 

```
database == catalog == single database within db cluster, isolated from other databases in same db cluster
```
내용을 보면 mySql과는 다르게 databas는 서로 독립된 공간이라고 표현했으니 이게 맞는지도 모르겠다.


나는 개인적으로 postgres가 참 맘에 든다. 사실 이 녀석도 역사가 있는 넘인데 몇 년전 블록체인을 하면서 LISK가 내부적으로 PostgreSQL을 사용하는 것을 보고 많은 생각을 했다.

괜찮은 녀석인데????

뭔가 장황하게 설명해놨지만 사실 catalog와 schema를 얼마나 잘 활용하는지는 모르겠다.    

하지만 이것은 multi tenancy를 고려할 때 서로 독립적인 namespace를 가지고 있어서 모듈별로 관리하기 용이할 수 있을 거 같다.

별로 중요한 거 같지 않은 녀석들에게 많은 힘을 썼다.

암튼 이후의 엔티티 매핑과 관련해 다음 브랜치에서 쭉 이어가겠다.     