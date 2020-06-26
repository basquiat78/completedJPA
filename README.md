# Prerequisites

## Configuration
- macOS Catalina v10.15.5
- OpenJDK(AdoptOpenJDK) 1.8.0.222
- IDE: Spring Tool Suite version 3.9.9
- Lombok Plugin
- MySql 8.0.15

사족: TMI이다.


## Get Started
일단 흐름은 메이븐프로젝트를 통해서 JPA의 기본적인 것들을 살펴보면서 진행할 것이다.

책을 참조하면서 기본을 다시 다지고 있다보니 불편하지만 감수하고 있는데 사실 이건 최근 SpringBoot의 Spring Data JPA를 이용한 개발을 먼저 시작하신 분들에게는 좀 생소할 수 있다.

왜냐하면 대부분 숨겨져 있기 때문이다.

당연하게도 스프링부트의 탄생 배경을 보면 그럴것이다.

개발을 빠르게 편하게 하는것이 목적인 만큼 JPA를 편하게 만들어 놓은 것이 Spring Data JPA니까

하지만 이 공간은 JPA -> Spring Boot -> 최종적으로는 ~~criteria 잘가~~ queryDSL과 연계해서 하나의 작은 토이 프로젝트를 구성하는게 목적이니만큼 이 순서를 그대로 따를 것이다.

그리고 이 프로젝트는 이미 깔아놨던 mySql이 있어서 그냥 썼지만 만일 그런 상황이 아니라면 굳이 무거운 이 녀석을 설치하지 말자. 

메모리디비인 H2나 HSQLDB같은 작고 가벼운 녀석을 스탠드언론 형식으로 설치해서 사용하자.

관련 설정 정보들은 구글신이 아주 잘 알려줄 것이다.


## maven 설정  
요즘은 gradle을 많이 써서 gradle을 쓰면 참 좋겠지만 난 maven이 좀 더 편하다. 

xml 형식이 싫으면 gradle쓰고 아니라면 뭐가 되었든 로마로 가면 되는거 아닌가?

지금까지 많은 프로젝트에서 둘다 써가면서 해봤는데 나의 경우에는 그냥 맞춰주는 성격이라 딱히 호불호를 따지진 않는다. 

뭐 둘다 장단점이 있다.

하지만 지금 이 프로젝트를 만든 시점에는 그냥 maven이 편해서 쓴다.
 

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

```

이것이 현재 이 프로젝트의 maven설정이다. 5.4.17.Final를 선택한 이유는 차후 이 프로젝트가 SpringBoot 2.3.1버전을 사용해서 최종적으로 발전해 나갈 생각이기 때문이다. 

따라서 기존에 테스트 했던 것들에 대해 버전 이슈없이 쓸수 있을 것이다라고 생각하고 진행한다.

진행중에 스프링부트의 버전이 또 올라가더라도 2.3.1을 염두해 두고 진행한다.

그리고 예전에도 2.1대 버전의 경우에는 queryDSL관련 라이브러리를 dependency에 걸었던거 같은데 이 후 버전에는 이 녀석들이 같이 딸려오는 듯 싶다.

외국친구들도 비슷했던 모양인듯 이건 진행해보면서 확인할 생각이다.

## 우리의 마이에스큐엘이 (Persistence영역)와 연애편지 쓰기  

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
            <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/basquiat?useUnicode=yes&amp;characterEncoding=UTF-8&amp;serverTimezone=Asia/Seoul"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.MySQL5InnoDBDialect"/>
            <!-- option configuration -->
            <!-- 콘솔에 sql 날아가는거 보여줄지 여부를 체크하 옵션 -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.use_sql_comments" value="true"/>
            <!-- 이 옵션에 들어가는 것은 그냥 쓰지 말자. 테스트 용도 또는 개인 토이 프로젝트를 하는게 아니라면 validate정도까지만 그게 아니면 운영은 none으로 설정하자 -->
            <!-- 실제 none이라는 옵션은 없다. 따라서 none으로 하면 아무 일도 일어나지 않는다. -->
            <property name="hibernate.hbm2ddl.auto" value="create" />
        </properties>
    </persistence-unit>
</persistence>

```

항상 오래전부터 SpringFramewokr를 쓸때마다 궁금했었지만 왜 설정 파일들을 특정 폴더에 넣어야 하는 것일까였다.

관례라고 누군가는 말하는데 아무튼 우리의 어플리케이션(JPA)과 마이에스큐엘이와 '연결고리'를 만들기 위에서는 그와 관련된 설정파일이 필요하다.

resources폴더밑에 [META-INF]폴더를 만들고 persistence.xml를 생성한 이후에 위와 같이 설정들을 기입하자.

하이버네이트가 해당 폴더 밑에 persistence.xml를 읽어가기 때문에 이름도 고정이다. 또한 xml파일에 설정 내용중  persistence-unit name="basquiat" 이부분은 기억하자.

name에 나는 basquiat라고 써놨는데 보통은 해당 프로젝트 이름을 넣는다.

이 이름은 차후 EntityManagerFactory를 생성할 때 넣는 파라미터 값으로 이 이름을 찾기 때문이다. ~~에러나고 실행이 안되는데요? 하면서 은근히 모르는 사람 많더라...~~

hibernate.dialect와 관련해서 dialect는 '방언'이라는 의미이다.

경험이 많은 개발자들은 oracle과 mysql의 차이점을 잘 알것이다. 

그외에도 얼마나 많은 RDBMS가 있는가? postgresql, sybase, mssql등등등..

간단한 예로 paging처리하는 영역에서부터 일단 짜증이 확 몰려온다.

```
// offset, limit 활용 
// 0(offset)번째 로우부터 10(limit)개를 가져온다.
// LIMIT 10, 10;이라면 10째부터 10개 가져올게 라는 의미이다.
mysql -> SELECT id,
			   name,
			   price 
			FROM item 
			LIMIT 0, 10; 

// 그냥 primary key로 정렬되서 조회된 정보에서 몇개만 가져오겠다면 limit만 걸어서 
// 이 경우는 조회된 결과에서 10개만 가져온다.
mysql -> SELECT id,
			   name,
			   price 
			FROM item 
			LIMIT 10; 

// 하지만 오라클 이 xx넘은 좀 다르다.
oracle -> SELECT ITEM.id,
			    ITEM.name,
			    ITEM.price
			FROM (SELECT id,
						name,
						price 
					FROM item
				 ) ITEM 
		 WHERE rownum <= 10

```
오라클의 페이징을 보자니 벌써부터 머리가...

게다가 오라클도 안써본지 너무 오래됬넹...

아무튼 persistence.xml은 어떤 RDBMS와 연결할 것인지에 대한 정보를 하이버네이트에게 알려주는 일종의 명세서라고 보면 된다. 

즉, JPA에서는 이 명세서를 보고 '내가 바라볼 DB와 관련 정보들이 뭐시기이니 그에 맞춰서 쿼리를 만들어줘야겠다' 생각할 것이다.

이 말인 즉 이제부터 우리는 JPA와 JPA가 지원해주는 RDBMS와 함께라면 행복해 질 수 있는것이다.

그럼 이제 persistence.xml 밑에 걸어둔 옵션들은 뭐하는 녀석들인지 살펴보자. ~~TMI~~


```
1. <property name="hibernate.show_sql" value="true"/>

이렇게 true를 하게 되면 어떤 쿼리가 날아갔는지 알 수있다.
하지만 쿼리가 길어진다면 가독성이 상당히 떨어지게 된다.

Hibernate: drop table if exists basquiat_item
Hibernate: create table basquiat_item (id bigint not null auto_increment, name varchar(255), price integer, primary key (id)) engine=InnoDB
Hibernate: insert into basquiat_item (name, price) values (?, ?)
   
   
2. <property name="hibernate.format_sql" value="true"/>
그래서 이녀석이 존재한다.
보면 알겠지만 흔히 사용하는 sql 작성 권장 형식으로 변경된다.

Hibernate: 
    
    drop table if exists basquiat_item
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    insert 
    into
        basquiat_item
        (name, price) 
    values
        (?, ?)


3. <property name="hibernate.use_sql_comments" value="true"/>
그럼 이녀석은 뭐하는 녀석인고??
위와 다른 점은 CRUD중 CUD, 즉 insert, update, delete가 발생할 때 알 수 있는데 보면 알겠지만 

/* insert io.basquiat.model.Item
        */
        
위처럼 INSERT할때 어떤 객체의 정보가 넘어갔는지 힌트 형식으로 보여주게 된다.

Hibernate: 
    
    drop table if exists basquiat_item
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    /* insert io.basquiat.model.Item
        */ insert 
        into
            basquiat_item
            (name, price) 
        values
            (?, ?)

```

그렇다면 hibernate.hbm2ddl.auto 옵션은 뭘까? ~~위험한 넘인건 확실하다~~

예전 초창기 jpa프로젝트 할때 에피소드가 있었는데 그것은 한 동료가 계속 나에게 이상한 버그가 있다고 하는 것이다.

그것은 자신이 테스트했던 데이터들이 서버를 재실행 할테마다 다 사라진다는 것이다. 

바로 이 옵션때문이었는데 지금 생각해도 등짝에 식은땀이 흐른다. 만일 이 친구가 실수로 운영쪽에다가 했으면 어떤 일이 벌어졌을까?

create : SessionFactory 시작시 스키마, 즉 테이블을 삭제하고 다시 생성
create-drop : SessionFactory 종료시 스키마 삭제
update : SessionFactory 시작시 객체 구성와 스키마를 비교하여 컬럼 추가/삭제 작업을 진행함. 기존의 스키마를 삭제하지 않고 유지.
validate : SessionFactory 시작시 객체구성과 스키마가 다르다면 예외 발생시킴.


옵션이 더 있었던거 같은데 그냥 다 알 필요없다. 저 위에꺼만 알고 가자.

설명에도 잘 나와있듯이 create붙은 녀석은 엄청 무서운 녀석이다. 그냥 묻지도 따지지도 않고 테이블을 삭제해 버린다.

그냥 가볍게 테스트하면서 지우고 다시 생성하고 하면 그 무엇보다 세상 편한 녀석이긴 하지만 세상 가장 무서운 녀석인 이중적인 녀석이다.

테스트에서는 내가 굳이 DDL을 생성할 필요가 없기 때문인데 하지만 실제 DB를 설계하다보면 이 녀석만으로는 부족한 부분이 많다.

필요한 인덱스는 따로 만줘져야 하고 운영상태로 넘어가면 이미 스키마는 만들어진 상태일 것이다.

update도 매력적이긴 한데 이게 문제가 될 소지가 있다.


JPA와 관련된 건 아니지만 경험을 하나 말해보자면, 이 커머스 업체들 디비를 보면 가장 많은 데이터를 가진 테이블이 아마도 주문, 상품, 클레임 쪽일 확률이 높다.
당연하겠지만 그러한데 지금 있는 회사에서는 테스트/스테이징 서버의 DB는 주기적으로 운영DB를 레플리카한다. 실제로 쌓이는 데이터이다.


어느날 요청에 의해 NodeJS로 상상의 비즈니스 로직을 짜고 테스트 디비에 컬럼 하나를 추가하는 Alter를 날렸다 10분이라는 시간동안 해당 테이블에 락이 걸려 아무것도 하지 못했던 일이 있었다. 그때 그 테이블 바라보고 작업하던 후배 동료분께서는 나에게 엄청나게 따가운 눈초리를 보냈다. 

결국 걸린 락 아이디를 찾아서 kill을 해야만 했다.


이게 무슨 말이냐면 update라는 옵션이 '기존의 스키마를 삭제하지 않고 유지'한다고 하지만 만일 해당 Entity 객체에 정말 가벼운 마음으로 변수 하나 추가했다가 해당 테이블에 락이 걸려버리게 되면 관련 서비스는 그 락이 풀릴때까지 멈추게 되는 것이다. 언제 풀릴지도 미지수....

만일 그 테이블이 주문쪽이라면?? 

사용자들은 주문이 안된다고 Q&A에 엄청난 분노를 남기게 되는 현상이 발생할 것이다..... ~~끔찍해~~


다만 좀 유의미한건 validate이다.

컬럼이 빠지거나 새로 생기면서 벌어질 수 있는 휴먼 에러를 바로 알아챌 수 있기 때문이다. 

# 이제 개발할 환경이 갖춰졌는지 확인해 볼 시간  

이 브랜치는 Persistance Context(영속성 컨텍스트)나 이런 것들을 알아보기 전에 환경이 갖춰졌는지 확인만 할 생각이다.

나는 일단 Item과 jpaMain을 하나 만들고 다음과 같이 코드를 짰다.


Item.java

```
package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
@Entity(name = "basquiat_item")
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

    	// persistence.xml에 등록된 <persistence-unit name="basquiat">의 name값으로
    	// EntityManagerFactory를 생성하자. 
    	// Factory라는 말이 붙은 것을 보면 Factory Pattern이 적용된것이다. TMI적이고~ 
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("basquiat");
        // EntityManger를 emf로부터 꺼내오자.
        EntityManager em = emf.createEntityManager();
        // 이제부터 트랜잭션 관리하기 위해서 EntityTransaction을 생성하자.
        EntityTransaction tx = em.getTransaction();
        // transaction!!!!
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

일단 Transaction관련 내용은 구글에서 검색해서 괜찮다 싶은거 하나 그냥 링크로 던지고 말자.

[[DB기초] 트랜잭션이란 무엇인가?](https://coding-factory.tistory.com/226)


이제 저것을 실행해서 

```
Jun 26, 2020 4:01:16 PM org.hibernate.jpa.internal.util.LogHelper logPersistenceUnitInformation
INFO: HHH000204: Processing PersistenceUnitInfo [name: basquiat]
Jun 26, 2020 4:01:17 PM org.hibernate.Version logVersion
INFO: HHH000412: Hibernate ORM core version 5.4.17.Final
Jun 26, 2020 4:01:17 PM org.hibernate.annotations.common.reflection.java.JavaReflectionManager <clinit>
INFO: HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
Jun 26, 2020 4:01:17 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure
WARN: HHH10001002: Using Hibernate built-in connection pool (not for production use!)
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
Jun 26, 2020 4:01:17 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001005: using driver [com.mysql.jdbc.Driver] at URL [jdbc:mysql://localhost:3306/basquiat?useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]
Jun 26, 2020 4:01:17 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001001: Connection properties: {user=basquiat, password=****}
Jun 26, 2020 4:01:17 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator
INFO: HHH10001003: Autocommit mode: false
Jun 26, 2020 4:01:17 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>
INFO: HHH000115: Hibernate connection pool size: 20 (min=1)
Jun 26, 2020 4:01:17 PM org.hibernate.dialect.Dialect <init>
INFO: HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
Hibernate: 
    
    drop table if exists basquiat_item
Jun 26, 2020 4:01:18 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@1c05a54d] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Hibernate: 
    
    create table basquiat_item (
       id bigint not null auto_increment,
        name varchar(255),
        price integer,
        primary key (id)
    ) engine=InnoDB
Jun 26, 2020 4:01:18 PM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@e27ba81] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
Jun 26, 2020 4:01:18 PM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
Jun 26, 2020 4:01:18 PM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://localhost:3306/basquiat?useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul]

```

이와 같이 콘솔에서 디비에 스키마 생성문이 날아가는 것을 본다면 환경설정은 끝이 난것이다.


# At A Glance

가끔 후배 동료분들과 얘기해 보면 JPA가 db를 몰라도 되는 마치 '마법의 단어'로 알고 있는 경우가 많다.

절대 아니다. 

애초에 JPA는 데이터베이스라는 기술위에서 살고 있는 녀석이기 때문에 최소한의 DB에 대한 기초와 SQL를 간지나게 작성하지 못해도 특징 정도는 알아야 한다는 것이다.

트랜잭션이라는 것도 마찬가지이다.

뭐 어째든 시작은 했으니 벌써 반은 온거야?