# 값 타입


## 일단 시작하기 전에   

값 타입과 관련한 내용을 쭉 살펴보면 중요한 부분은 2가지이다.     

1. Embedded Type    

2. Collection Value Type

그 외에는 사실 자바의 기본적인 이론과 깊은 관련이 있다.       

Call By Reference vs. Call By Value와 관련된 내용들이기 때문에 이 부분은 그냥 링크로 대체한다.      

[Call By Reference vs. Call By Value](https://velog.io/@codemcd/Call-By-Value-VS-Call-By-Reference)

primitive type과 객체의 특성을 잘 파악하면 된다.

## 이렇게도 생각해 볼 수 있지 않을까?    

만일 Member, 즉 고객의 정보를 담는 테이블에는 보통 고객의 주소가 기입된다.      

지금까지 테스트하면서 그냥 전체적인 주소를 하나의 테이블에 담기 위해 String으로 받았다.       

하지만 이런 부분도 생각해 봐야 한다.     

만일 어느 지역을 중심으로 어떤 제품이 많이 팔렸는지 마케팅 측면에서 분석해야 할 경우 지금의 구조로는 할 수 가 없다.      

물론 여기서는 그런 분석은 던져버리고 주소를 좀 세부적으로 나누는 작업을 해볼려고 한다.    

직구를 하다 보면 외국 주소 체계는 보통 country, state/province, street, postal code이런 식인데 그냥 다음가 같이 간단하게 작성을 해 보자.

Member

```
package io.basquiat.model.embedded;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "embedded_member")
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Builder
	public Member(String id, String password, String name, String birth, String phone, String city, String street,
			String zipcode) {
		super();
		this.id = id;
		this.password = password;
		this.name = name;
		this.birth = birth;
		this.phone = phone;
		this.city = city;
		this.street = street;
		this.zipcode = zipcode;
	}

	/** 사용자 아이디 */
	@Id
	private String id;

	/** 사용자 비번 */
	private String password;
	
	/** 사용자 이름 */
	private String name;
	
	/** 사용자 생년월일 */
	private String birth;
	
	/** 사용자 전번 */
	private String phone;
	
	/** 시 */
	private String city;
	
	/** 동 */
	private String street;
	
	/** 우편 번호 */
	private String zipcode;
	
	
}
```
그런데 Delivery의 경우에는 또 어떤가? 또는 주문서에도 배송할 주소 정보가 있다.     

그러면 고민이 될것이다.     

'그냥 copy&paste로 붙이지 뭐...'       

하지만 이런 경우 변경에 대해 닫혀있게 된다. 하나가 바뀌면 어떻게 해야돼?      

변경된 내용을 해당 복붙복하나 모든 엔티티를 다 찾아서 변경해야 한다.      

물론 몇개 안되면 좋은 선택일 수 있지만 그렇게 되면 생각치 못한 휴먼에러를 100프로 피할 수 없다.   

Delivery

```
package io.basquiat.model.embedded;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.basquiat.model.item.DeliveryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "embedded_delivery")
@Table(name = "delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

	@Builder
	public Delivery(String courier, DeliveryStatus status, String city, String street, String zipcode) {
		super();
		this.courier = courier;
		this.status = status;
		this.city = city;
		this.street = street;
		this.zipcode = zipcode;
	}
	
	/** 배송 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	/** 택배사 코드 */
	private String courier;
	
	/** 배송 상태 */
	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;
	
	/** 시 */
	private String city;
	
	/** 동 */
	private String street;
	
	/** 우편 번호 */
	private String zipcode;
	
}
```
이렇게 바꿔야 겠지...

## 그럼 어떻게 해야하는데? 방법을 알려주고 말을 해!     

그래서 JPA에서는 @Embeddable과 @Embedded을 지원한다.      

그냥 뜻으로 해석하면 내장가능한, 내장된 이라고 해석할 수 있는데 이렇게 보면 이 어노테이션이 어디에 사용될지 머리 속에 들어온다.     

어떤 공통된 프로퍼티들을 모아놓은 POJO타입의 객체를 하나 만들 것이다.     


Address

```
package io.basquiat.model.embedded;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
@ToString
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
이렇게 @Embeddable를 붙여주면 된다. 객체로 작성하기 때문에 프로퍼티가 테이블의 컬럼으로 나갈 것이다.     

또한 결국 하나의 엔티티에 내장될 객체이기 때문에 컬럼명 역시 @Column을 통해서 정의할 수 있다.

하지만 우리는 필요하면 위와 같이 기능을 제공하는 함수를 작성할 수 있다.      

주의할 점은 기본 생성자는 필수이다. 또한 이것은 엔티티가 아니다.     

자 그럼 이제는 Delivery, Member 객체도 변경해 주면 된다.

Delivery

```
package io.basquiat.model.embedded;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.basquiat.model.item.DeliveryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "embedded_delivery")
@Table(name = "delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Delivery {

	@Builder
	public Delivery(Long id, String courier, DeliveryStatus status, Address address) {
		super();
		this.id = id;
		this.courier = courier;
		this.status = status;
		this.address = address;
	}
	
	/** 배송 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	/** 택배사 코드 */
	private String courier;
	
	/** 배송 상태 */
	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;
	
	/** 배송지 주소 */
	@Embedded
	private Address address;
	
}
```

Member

```
package io.basquiat.model.embedded;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "embedded_member")
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {

	@Builder
	public Member(String id, String password, String name, String birth, String phone, Address address) {
		super();
		this.id = id;
		this.password = password;
		this.name = name;
		this.birth = birth;
		this.phone = phone;
		this.address = address;
	}

	/** 사용자 아이디 */
	@Id
	private String id;

	/** 사용자 비번 */
	private String password;
	
	/** 사용자 이름 */
	private String name;
	
	/** 사용자 생년월일 */
	private String birth;
	
	/** 사용자 전번 */
	private String phone;
	
	/** 사용자 주소 */
	@Embedded
	private Address address;
	
}
```
그럼 일단 테이블 생성이 어떻게 되는지 먼저 확인해 보자.

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

result grid

Hibernate: 
    
    drop table if exists delivery
Hibernate: 
    
    drop table if exists member
Hibernate: 
    
    create table delivery (
       id bigint not null auto_increment,
        member_city varchar(255),
        member_street varchar(255),
        member_zipcode varchar(255),
        courier varchar(255),
        status varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table member (
       id varchar(255) not null,
        member_city varchar(255),
        member_street varchar(255),
        member_zipcode varchar(255),
        birth varchar(255),
        name varchar(255),
        password varchar(255),
        phone varchar(255),
        primary key (id)
    ) engine=InnoDB
```
예상대로 잘 생성된 것을 볼 수 있다.

그럼 실제로 어떻게 저장하고 가져오는지 확인해 볼 시간이다.

```
// 1.address 생성
Address address = Address.builder().city("서울시")
								   .street("논현동")
								   .zipcode("50000")
								   .build();

// 신규가입자 
Member newMember = Member.builder().id("basquiat")
									.password("mypassword")
									.name("Jean-Michel-Basquiat")
									.birth("1960-12-22")
									.phone("010-0000-00000")
									.address(address)
									.build();
em.persist(newMember);

Delivery delivery = Delivery.builder().courier("CJ택배")
									 .status(DeliveryStatus.READY)
									 .address(address)
									 .build();
em.persist(delivery);
em.flush();
em.clear();

Member selectedMember = em.find(Member.class, newMember.getId());
System.out.println(selectedMember.toString());

Delivery selectedDelivery = em.find(Delivery.class, delivery.getId());
System.out.println(selectedDelivery.toString());

System.out.println(selectedMember.getAddress().totalAddress());
System.out.println(selectedDelivery.getAddress().totalAddress());

result grid

Hibernate: 
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.embedded.Delivery
        */ insert 
        into
            delivery
            (member_city, member_street, member_zipcode, courier, status) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    select
        member0_.id as id1_1_0_,
        member0_.member_city as member_c2_1_0_,
        member0_.member_street as member_s3_1_0_,
        member0_.member_zipcode as member_z4_1_0_,
        member0_.birth as birth5_1_0_,
        member0_.name as name6_1_0_,
        member0_.password as password7_1_0_,
        member0_.phone as phone8_1_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Member(id=basquiat, password=mypassword, name=Jean-Michel-Basquiat, birth=1960-12-22, phone=010-0000-00000, address=Address(city=서울시, street=논현동, zipcode=50000))
Hibernate: 
    select
        delivery0_.id as id1_0_0_,
        delivery0_.member_city as member_c2_0_0_,
        delivery0_.member_street as member_s3_0_0_,
        delivery0_.member_zipcode as member_z4_0_0_,
        delivery0_.courier as courier5_0_0_,
        delivery0_.status as status6_0_0_ 
    from
        delivery delivery0_ 
    where
        delivery0_.id=?
Delivery(id=1, courier=CJ택배, status=READY, address=Address(city=서울시, street=논현동, zipcode=50000))
서울시 논현동, 500001
서울시 논현동, 500001
```
원하는 정보가 나왔다.      

하지만 이 코드는 약간 문제가 있다. 불변 객체에 대한 이야기가 나오는데 그럼 다음과 같이 코드를 수정해서 테스트 하면 어떤 일이 벌어질까?

```
// 1.address 생성
Address address = Address.builder().city("서울시")
								   .street("논현동")
								   .zipcode("50000")
								   .build();

// 신규가입자 
Member newMember = Member.builder().id("basquiat")
									.password("mypassword")
									.name("Jean-Michel-Basquiat")
									.birth("1960-12-22")
									.phone("010-0000-00000")
									.address(address)
									.build();
em.persist(newMember);

Address deliveryAddress = address;
// 배송지에 들어가는 zipcode는 500001로 수정하자
deliveryAddress.setZipcode("500001");
Delivery delivery = Delivery.builder().courier("CJ택배")
									 .status(DeliveryStatus.READY)
									 .address(deliveryAddress)
									 .build();
em.persist(delivery);
em.flush();
em.clear();

Member selectedMember = em.find(Member.class, newMember.getId());
System.out.println(selectedMember.toString());

Delivery selectedDelivery = em.find(Delivery.class, delivery.getId());
System.out.println(selectedDelivery.toString());

System.out.println(selectedMember.getAddress().totalAddress());
System.out.println(selectedDelivery.getAddress().totalAddress());
```
위 코드에서 처럼 중간에 배송지에 들어가는 주소는 위에서 생성한 주소를 그냥 복사한 것처럼 사용해서 수정해서 넣었다.     

그러면 결과는 어떻게 될까? 원래 의도했던 것은 사용자의 주소의 zipcode는 50000이고 배송지에 들어가는 zipcode는 500001를 희망했을 것이다.      

하지만 결과는 다음과 같다.

```
Hibernate: 
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.embedded.Delivery
        */ insert 
        into
            delivery
            (member_city, member_street, member_zipcode, courier, status) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    /* update
        io.basquiat.model.embedded.Member */ update
            member 
        set
            member_city=?,
            member_street=?,
            member_zipcode=?,
            birth=?,
            name=?,
            password=?,
            phone=? 
        where
            id=?
Hibernate: 
    select
        member0_.id as id1_1_0_,
        member0_.member_city as member_c2_1_0_,
        member0_.member_street as member_s3_1_0_,
        member0_.member_zipcode as member_z4_1_0_,
        member0_.birth as birth5_1_0_,
        member0_.name as name6_1_0_,
        member0_.password as password7_1_0_,
        member0_.phone as phone8_1_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Member(id=basquiat, password=mypassword, name=Jean-Michel-Basquiat, birth=1960-12-22, phone=010-0000-00000, address=Address(city=서울시, street=논현동, zipcode=500001))
Hibernate: 
    select
        delivery0_.id as id1_0_0_,
        delivery0_.member_city as member_c2_0_0_,
        delivery0_.member_street as member_s3_0_0_,
        delivery0_.member_zipcode as member_z4_0_0_,
        delivery0_.courier as courier5_0_0_,
        delivery0_.status as status6_0_0_ 
    from
        delivery delivery0_ 
    where
        delivery0_.id=?
Delivery(id=1, courier=CJ택배, status=READY, address=Address(city=서울시, street=논현동, zipcode=500001))
서울시 논현동, 500001
서울시 논현동, 500001
```
어라? 사용자의 zipcode까지 바껴서 들어가 버렸다.     

객체를 다룰 때 reference에 대한 개념을 이해해야만 한다.      

이와 관련 테스트 코드가 있는데 [callbyvalue](https://github.com/basquiat78/call-by-value-vs-call-by-reference-)를 한번 살펴보기 바란다.      

암튼 그래서 이것을 방지하기 위해서라도 불변객체로 바꾸는 것을 강제한다.      

가장 기본적인 방법은 Setter를 없애고 기존의 객체를 복사하기 보다는 생성자를 이용하거나 빌드 패턴으로 새로운 객체를 생성해서 사용하는 것이다.      

Address

```
package io.basquiat.model.embedded;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@Embeddable
@ToString
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
@Setter를 없애고 다음과 같이 코드를 수정해서 사용하는 것이다.

```
// 1.address 생성
Address address = Address.builder().city("서울시")
								   .street("논현동")
								   .zipcode("50000")
								   .build();

// 신규가입자 
Member newMember = Member.builder().id("basquiat")
									.password("mypassword")
									.name("Jean-Michel-Basquiat")
									.birth("1960-12-22")
									.phone("010-0000-00000")
									.address(address)
									.build();
em.persist(newMember);

Address deliveryAddress = Address.builder().city(address.getCity())
										   .street(address.getStreet())
										   .zipcode("500001")
										   .build();
Delivery delivery = Delivery.builder().courier("CJ택배")
									 .status(DeliveryStatus.READY)
									 .address(deliveryAddress)
									 .build();
em.persist(delivery);
em.flush();
em.clear();

Member selectedMember = em.find(Member.class, newMember.getId());
System.out.println(selectedMember.toString());

Delivery selectedDelivery = em.find(Delivery.class, delivery.getId());
System.out.println(selectedDelivery.toString());

System.out.println(selectedMember.getAddress().totalAddress());
System.out.println(selectedDelivery.getAddress().totalAddress());

result grid

Hibernate: 
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.embedded.Delivery
        */ insert 
        into
            delivery
            (member_city, member_street, member_zipcode, courier, status) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    select
        member0_.id as id1_1_0_,
        member0_.member_city as member_c2_1_0_,
        member0_.member_street as member_s3_1_0_,
        member0_.member_zipcode as member_z4_1_0_,
        member0_.birth as birth5_1_0_,
        member0_.name as name6_1_0_,
        member0_.password as password7_1_0_,
        member0_.phone as phone8_1_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Member(id=basquiat, password=mypassword, name=Jean-Michel-Basquiat, birth=1960-12-22, phone=010-0000-00000, address=Address(city=서울시, street=논현동, zipcode=50000))
Hibernate: 
    select
        delivery0_.id as id1_0_0_,
        delivery0_.member_city as member_c2_0_0_,
        delivery0_.member_street as member_s3_0_0_,
        delivery0_.member_zipcode as member_z4_0_0_,
        delivery0_.courier as courier5_0_0_,
        delivery0_.status as status6_0_0_ 
    from
        delivery delivery0_ 
    where
        delivery0_.id=?
Delivery(id=1, courier=CJ택배, status=READY, address=Address(city=서울시, street=논현동, zipcode=500001))
서울시 논현동, 50000
서울시 논현동, 500001
```
참고로 이 방식의 경우에는 Address 객체가 null이거나 엔티티 자체에 세팅을 하지 않을 경우 테이블에는 해당 컬럼이 전부 null로 세팅된다.

## Collection Value Type vs. @OneToMany      
이 부분은 어떻게 보면 @OneToMany와 상당부분 유사하다.     

예를 들면 쇼핑몰을 통해서 주문을 하다보면 배송지를 선택하는데 대부분 여러개의 배송지를 등록할 수 있게 되어 있다.     

보통 자신이 근무하는 회사 주소랑 집 주소를 등록해서 사용하는 경우가 많은데 그러면 이것을 어떻게 표현할 수 있는가 할 것이다.      

딱 보면 1:N의 연관관계를 가지고 있다는 것을 알 수 있으니 @OneToMany로 결정지을 수 있다.      

하지만 이 경우에는 기존에 만들어 둔 Address객체를 엔티티로 격상시켜야 하는 경우가 생기게 된다.      

그러면 선택지는 둘이다. 

1. @ElementCollection, @CollectionTable를 활용해 Address를 Collection Value Type로 표현한다.      

2. 그냥 Address를 엔티티로 격상시킨다.     

즉, 설계 초기에 이미 고려되어야 하는 부분이라는 생각이 든다.      

그럼 이제 이것을 어떻게 풀어갈지 한번 보자.      

일단 이런 생각도 할 수 있다.      

'여러개의 배송지 주소를 JSON스트링으로 보관하고 가져와서 파싱해서 사용해도 될거 같은데?'      

물론 가능하긴 하다. 하지만 이럴 경우 등록될 배송지의 수가 많아지면 lob을 사용해야 할 수도 있다.      

그외에도 몇가지 제약들이 좀 걸린다. 그렇다고 DB가 Collection타입을 저장하는 것을 지원하지 않는다. ~~혹시... 지원하는 DB가 있니?~~     

그러면 결국 다음과 같은 erd구조를 가져가야 한다.     

![실행이미지](https://github.com/basquiat78/completedJPA/blob/10.value-type/capture/capture1.png)     

사실 모든 컬럼에 중복 허용을 방지할 것인지에 대한 표현이 좀 안되긴 하지만 어째든 저런 식으로 테이블을 따로 두고 컬렉션 정보를 저장해야 한다.     

하지만 이 방법은 몇가지 제약사항을 가지고 있다.     

erd를 보면 알겠지만 pk가 없다. 그럴 수 밖에 없는 것이 이 녀석은 엔티티가 아니라 값 타입이기 때문에 pk라는 개념이 없는 것이다.      

이것은 변경사항에 대한 추적이 불가능하다.      

이게 무슨 소리인지 하나씩 알아가 보자.      

Member

```
package io.basquiat.model.embedded;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "embedded_member")
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {

	@Builder
	public Member(String id, String password, String name, String birth, String phone, Address address) {
		super();
		this.id = id;
		this.password = password;
		this.name = name;
		this.birth = birth;
		this.phone = phone;
		this.address = address;
	}

	/** 사용자 아이디 */
	@Id
	private String id;

	/** 사용자 비번 */
	private String password;
	
	/** 사용자 이름 */
	private String name;
	
	/** 사용자 생년월일 */
	private String birth;
	
	/** 사용자 전번 */
	private String phone;
	
	/** 사용자 주소 */
	@Embedded
	private Address address;
	
	@ElementCollection
	@CollectionTable(name = "delivery_address", 
				    joinColumns = @JoinColumn(name = "member_id"))
	private Set<Address> deliveryAddress = new HashSet<>();
	
}
```
다음과 같이 두개의 어노테이션을 사용하게 된다.      

@ElementCollection은 해당 프로퍼티가 컬렉션 값 타입이라는 것을 지정하는 것이고 테이블을 생성하기 때문에 @CollectionTable을 이용해서 테이블의 이름을 설정할 수 있다.     

당연히 erd를 보면 알겠지만 생성할 delivery_address와의 연관관계를 알기 위해서는 조인 컬럼이 필요하다.     

그러면 일단 테이블이 어떻게 생성되는지 한번 확인해 보자.

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

result grid
Hibernate: 
    
    drop table if exists delivery
Hibernate: 
    
    drop table if exists delivery_address
Hibernate: 
    
    drop table if exists member
Hibernate: 
    
    create table delivery (
       id bigint not null auto_increment,
        member_city varchar(255),
        member_street varchar(255),
        member_zipcode varchar(255),
        courier varchar(255),
        status varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table delivery_address (
       member_id varchar(255) not null,
        member_city varchar(255),
        member_street varchar(255),
        member_zipcode varchar(255)
    ) engine=InnoDB
Hibernate: 
    
    create table member (
       id varchar(255) not null,
        member_city varchar(255),
        member_street varchar(255),
        member_zipcode varchar(255),
        birth varchar(255),
        name varchar(255),
        password varchar(255),
        phone varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    alter table delivery_address 
       add constraint FK7txh3nxg2wpt4lmsgalnxpcm5 
       foreign key (member_id) 
       references member (id)

```
delivery_address테이블이 생성된 것을 알 수 있다.      

그러면 이제 어떻게 사용하는지 살펴보자. 기본과 크게 다를 바가 없다.

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.embedded.Address;
import io.basquiat.model.embedded.Member;

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
        	
        	// 1.address 생성
        	Address address = Address.builder().city("서울시")
        									   .street("논현동")
        									   .zipcode("50000")
        									   .build();
        	
        	Address myHomeAddress = Address.builder().city("서울시")
												   	 .street("논현동 우리집")
												   	 .zipcode("50000")
												   	 .build();
        	
        	Address myOfficialAddress = Address.builder().city("서울시")
													   .street("논현동 회사")
													   .zipcode("50000")
													   .build();
        	
        	// 신규가입자 
        	Member newMember = Member.builder().id("basquiat")
        										.password("mypassword")
        										.name("Jean-Michel-Basquiat")
        										.birth("1960-12-22")
        										.phone("010-0000-00000")
        										.address(address)
        										.build();
        	
        	newMember.getDeliveryAddress().add(myHomeAddress);
        	newMember.getDeliveryAddress().add(myOfficialAddress);
        	em.persist(newMember);
        	
        	em.flush();
        	em.clear();
        	
        	Member selectedMember = em.find(Member.class, newMember.getId());
        	System.out.println(selectedMember.toString());
        	System.out.println(selectedMember.getDeliveryAddress());
        	System.out.println("-------------");
        	
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
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    select
        member0_.id as id1_2_0_,
        member0_.member_city as member_c2_2_0_,
        member0_.member_street as member_s3_2_0_,
        member0_.member_zipcode as member_z4_2_0_,
        member0_.birth as birth5_2_0_,
        member0_.name as name6_2_0_,
        member0_.password as password7_2_0_,
        member0_.phone as phone8_2_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Hibernate: 
    select
        deliveryad0_.member_id as member_i1_1_0_,
        deliveryad0_.member_city as member_c2_1_0_,
        deliveryad0_.member_street as member_s3_1_0_,
        deliveryad0_.member_zipcode as member_z4_1_0_ 
    from
        delivery_address deliveryad0_ 
    where
        deliveryad0_.member_id=?
Member(id=basquiat, password=mypassword, name=Jean-Michel-Basquiat, birth=1960-12-22, phone=010-0000-00000, address=Address(city=서울시, street=논현동, zipcode=50000), deliveryAddress=[Address(city=서울시, street=논현동 회사, zipcode=50000), Address(city=서울시, street=논현동 우리집, zipcode=50000)])
[Address(city=서울시, street=논현동 회사, zipcode=50000), Address(city=서울시, street=논현동 우리집, zipcode=50000)]
-------------
Hibernate: 
    /* delete collection io.basquiat.model.embedded.Member.deliveryAddress */ delete 
        from
            delivery_address 
        where
            member_id=?
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)

```
결과를 보니 기본적으로 이 경우에는 LAZY가 기본이라는 것을 알 수 있다.     

그런데 뒤에 delete 쿼리가 나가고 다시 인서트하는 로그가 찍힌다.     

그래서 Member에서 List타입으로 변경을 시도해 봤다.

```
package io.basquiat.model.embedded;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "embedded_member")
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "deliveryAddress")
public class Member {

	@Builder
	public Member(String id, String password, String name, String birth, String phone, Address address) {
		super();
		this.id = id;
		this.password = password;
		this.name = name;
		this.birth = birth;
		this.phone = phone;
		this.address = address;
	}

	/** 사용자 아이디 */
	@Id
	private String id;

	/** 사용자 비번 */
	private String password;
	
	/** 사용자 이름 */
	private String name;
	
	/** 사용자 생년월일 */
	private String birth;
	
	/** 사용자 전번 */
	private String phone;
	
	/** 사용자 주소 */
	@Embedded
	private Address address;
	
	@ElementCollection
	@CollectionTable(name = "delivery_address", 
					 joinColumns = @JoinColumn(name = "member_id"))
	private List<Address> deliveryAddress = new ArrayList<>();
	
}

result grid

Hibernate: 
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    select
        member0_.id as id1_2_0_,
        member0_.member_city as member_c2_2_0_,
        member0_.member_street as member_s3_2_0_,
        member0_.member_zipcode as member_z4_2_0_,
        member0_.birth as birth5_2_0_,
        member0_.name as name6_2_0_,
        member0_.password as password7_2_0_,
        member0_.phone as phone8_2_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Member(id=basquiat, password=mypassword, name=Jean-Michel-Basquiat, birth=1960-12-22, phone=010-0000-00000, address=Address(city=서울시, street=논현동, zipcode=50000))
Hibernate: 
    select
        deliveryad0_.member_id as member_i1_1_0_,
        deliveryad0_.member_city as member_c2_1_0_,
        deliveryad0_.member_street as member_s3_1_0_,
        deliveryad0_.member_zipcode as member_z4_1_0_ 
    from
        delivery_address deliveryad0_ 
    where
        deliveryad0_.member_id=?
Address(city=서울시, street=논현동 우리집, zipcode=50000)
Address(city=서울시, street=논현동 회사, zipcode=50000)
```
나는 순서는 중요하지 않고 중복을 방지하기 위해서 Set를 사용했는데 List로 변경해야 하나 생각했다가 구글신에게 물어 본 결과 다음과 같은 결론을 얻었다.     

```
Set에 저장할 Value Type의 경우에는 equals와 hashCode를 구현해라.       
```
따라서 Address에 다음과 같이 

```
package io.basquiat.model.embedded;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@Embeddable
@ToString
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
롬복의 어노테이션을 이용했다.     

그랬더니 delete 쿼리를 날리고 다시 인서트하는 로그가 사라졌다.      

'어.... 의도치 않긴 하지만 이런 사실을 모르면 약간 위험한데? 쓸데없는 쿼리가 나간다니'       

만일 이런 사실을 몰랐다면 N+1의 쿼리가 계속 나간다는 거 아닌가?     

stackoverflow를 살펴보니 이것은 이 경우 뿐만 아니라 연관관계 매핑을 사용할 때도 값은 현상이 발생한다는 것을 찾았다.     

그 이유는 Set의 특징때문이라고 하는데 중복을 허용하지 않기 때문에 객체가 추가될때마다 equals 메서드로 같은 객체를 비교한다고 한다.    

게다가 HashSet은 해시 알고리즘을 사용하기 때문에 해시코드를 사용해서 데이터를 분류해서 저장한다.     

따라서 equals와 hashcode 두개를 같이 사용한다.      

아니..... 근데 이거와 별개로 왜 위와 같은 현상이 발생하는지는 잘 모르겠다.       

다만 이런 현상이 있다는 것을 아는 것이 더 중요하다고 생각한다.     

그리고 책에서도 이런 내용을 그냥 건성으로 봐서 건너뛴거 같은데 값타입을 사용할 때는 특히 equals와 hashCode를 구현해야한다는 내용이 있다.     

그래서 한번 다음과 같이도 사용해 보고자 한다.     

```
package io.basquiat.model.embedded;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "embedded_member")
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "deliveryAddress")
public class Member {

	@Builder
	public Member(String id, String password, String name, String birth, String phone, Address address) {
		super();
		this.id = id;
		this.password = password;
		this.name = name;
		this.birth = birth;
		this.phone = phone;
		this.address = address;
	}

	/** 사용자 아이디 */
	@Id
	private String id;

	/** 사용자 비번 */
	private String password;
	
	/** 사용자 이름 */
	private String name;
	
	/** 사용자 생년월일 */
	private String birth;
	
	/** 사용자 전번 */
	private String phone;
	
	/** 사용자 주소 */
	@Embedded
	private Address address;
	
	@ElementCollection
	@CollectionTable(name = "delivery_address", 
					 joinColumns = @JoinColumn(name = "member_id"))
	private Set<Address> deliveryAddress = new HashSet<>();
	
	@ElementCollection
	@CollectionTable(name = "favorite_coffee_shop", 
					 joinColumns = @JoinColumn(name = "member_id"))
	private Set<String> favoriteCoffeeShop = new HashSet<>();
	
}
```
객체가 아닌 String타입의 좋아하는 커피숍이라는 컬렉션 타입을 한번 저장해 보자.

```
package io.basquiat;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.embedded.Address;
import io.basquiat.model.embedded.Member;

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
        	
        	// 1.address 생성
        	Address address = Address.builder().city("서울시")
        									   .street("논현동")
        									   .zipcode("50000")
        									   .build();
        	
        	Address myHomeAddress = Address.builder().city("서울시")
												   	 .street("논현동 우리집")
												   	 .zipcode("50000")
												   	 .build();
        	
        	Address myOfficialAddress = Address.builder().city("서울시")
													   .street("논현동 회사")
													   .zipcode("50000")
													   .build();
        	
        	// 신규가입자 
        	Member newMember = Member.builder().id("basquiat")
        										.password("mypassword")
        										.name("Jean-Michel-Basquiat")
        										.birth("1960-12-22")
        										.phone("010-0000-00000")
        										.address(address)
        										.build();
        	
        	newMember.getDeliveryAddress().add(myHomeAddress);
        	newMember.getDeliveryAddress().add(myOfficialAddress);
        	
        	newMember.getFavoriteCoffeeShop().add("별다방");
        	newMember.getFavoriteCoffeeShop().add("커피 자판기 일명 벽다방");
        	newMember.getFavoriteCoffeeShop().add("커피콩");
        	
        	em.persist(newMember);
        	
        	em.flush();
        	em.clear();
        	
        	Member selectedMember = em.find(Member.class, newMember.getId());
        	System.out.println(selectedMember.toString());
        	
        	Set<Address> selectedAddress = selectedMember.getDeliveryAddress();
        	selectedAddress.stream().forEach(add -> System.out.println(add.toString()));
        	
        	Set<String> selectedCoffeeShop = selectedMember.getFavoriteCoffeeShop();
        	selectedCoffeeShop.stream().forEach(System.out::println);
        	
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
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favoriteCoffeeShop) 
        values
            (?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favoriteCoffeeShop) 
        values
            (?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favoriteCoffeeShop) 
        values
            (?, ?)
Hibernate: 
    select
        member0_.id as id1_3_0_,
        member0_.member_city as member_c2_3_0_,
        member0_.member_street as member_s3_3_0_,
        member0_.member_zipcode as member_z4_3_0_,
        member0_.birth as birth5_3_0_,
        member0_.name as name6_3_0_,
        member0_.password as password7_3_0_,
        member0_.phone as phone8_3_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Hibernate: 
    select
        favoriteco0_.member_id as member_i1_2_0_,
        favoriteco0_.favoriteCoffeeShop as favorite2_2_0_ 
    from
        favorite_coffee_shop favoriteco0_ 
    where
        favoriteco0_.member_id=?
Member(id=basquiat, password=mypassword, name=Jean-Michel-Basquiat, birth=1960-12-22, phone=010-0000-00000, address=Address(city=서울시, street=논현동, zipcode=50000), favoriteCoffeeShop=[별다방, 커피콩, 커피 자판기 일명 벽다방])
Hibernate: 
    select
        deliveryad0_.member_id as member_i1_1_0_,
        deliveryad0_.member_city as member_c2_1_0_,
        deliveryad0_.member_street as member_s3_1_0_,
        deliveryad0_.member_zipcode as member_z4_1_0_ 
    from
        delivery_address deliveryad0_ 
    where
        deliveryad0_.member_id=?
Address(city=서울시, street=논현동 회사, zipcode=50000)
Address(city=서울시, street=논현동 우리집, zipcode=50000)
별다방
커피콩
커피 자판기 일명 벽다방
```
primitive type의 경우에는 뭐.... 그런가보다.      

이런 경우에는 다음과 같이 

```
@ElementCollection
@CollectionTable(name = "favorite_coffee_shop", 
				 joinColumns = @JoinColumn(name = "member_id"))
@Column(name = "favorite_coffee_shop")
private Set<String> favoriteCoffeeShop = new HashSet<>();
```
컬럼 명을 지정할 수 있다. ~~favoriteCoffeeShop이라는 컬럼명은 너무 하잖아~~    

어째든 Collection Value의 첫 번째 특징이 기본적으로 LAZY방식으로 작동하고 pk, 즉 식별자 개념이 없다는 것 그리고 equals와 hashCode메소드를 오버라이딩해서 구현해야한다는 것을 알게 되었다.      

그렇다면 이제 수정 및 삭제와 관련해서 한번 테스트를 해보자.       

우리는 위에서 식별자 개념이 없다는 것을 배웠다. 이게 무슨 말이냐면 어떤 특정 row만 업데이트할 수 없다는 의미이다.      

물론 네이티브 쿼리로 수정하면 할 수 있겠지만....      

```
// 1.address 생성
Address address = Address.builder().city("서울시")
								   .street("논현동")
								   .zipcode("50000")
								   .build();

Address myHomeAddress = Address.builder().city("서울시")
									   	 .street("논현동 우리집")
									   	 .zipcode("50000")
									   	 .build();

Address myOfficialAddress = Address.builder().city("서울시")
										   .street("논현동 회사")
										   .zipcode("50000")
										   .build();

// 신규가입자 
Member newMember = Member.builder().id("basquiat")
									.password("mypassword")
									.name("Jean-Michel-Basquiat")
									.birth("1960-12-22")
									.phone("010-0000-00000")
									.address(address)
									.build();

newMember.getDeliveryAddress().add(myHomeAddress);
newMember.getDeliveryAddress().add(myOfficialAddress);

newMember.getFavoriteCoffeeShop().add("별다방");
newMember.getFavoriteCoffeeShop().add("커피 자판기 일명 벽다방");
newMember.getFavoriteCoffeeShop().add("커피콩");

em.persist(newMember);

em.flush();
em.clear();

Member selectedMember = em.find(Member.class, newMember.getId());
Set<Address> selectedAddress = selectedMember.getDeliveryAddress();
Set<String> favoriteCoffeeShop = selectedMember.getFavoriteCoffeeShop();

// 업데이트가 아니라 업데이트할 address을 삭제하고 새로 추가해야 한다.
Address removeAddress = selectedAddress.stream().filter(ra -> ra.getStreet().equals("논현동 회사"))
												.findFirst()
												.get();
selectedAddress.remove(removeAddress);
Address myNewOfficialAddress = Address.builder().city("서울시")
											    .street("논현동 회사요")
											    .zipcode("50000")
											    .build();
selectedAddress.add(myNewOfficialAddress);

// 이녀석도 마찬가지
favoriteCoffeeShop.remove("커피콩");
favoriteCoffeeShop.add("커피콩콩");

result grid 

Hibernate: 
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    select
        member0_.id as id1_3_0_,
        member0_.member_city as member_c2_3_0_,
        member0_.member_street as member_s3_3_0_,
        member0_.member_zipcode as member_z4_3_0_,
        member0_.birth as birth5_3_0_,
        member0_.name as name6_3_0_,
        member0_.password as password7_3_0_,
        member0_.phone as phone8_3_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Hibernate: 
    select
        deliveryad0_.member_id as member_i1_1_0_,
        deliveryad0_.member_city as member_c2_1_0_,
        deliveryad0_.member_street as member_s3_1_0_,
        deliveryad0_.member_zipcode as member_z4_1_0_ 
    from
        delivery_address deliveryad0_ 
    where
        deliveryad0_.member_id=?
Hibernate: 
    select
        favoriteco0_.member_id as member_i1_2_0_,
        favoriteco0_.favorite_coffee_shop as favorite2_2_0_ 
    from
        favorite_coffee_shop favoriteco0_ 
    where
        favoriteco0_.member_id=?
Hibernate: 
    /* delete collection io.basquiat.model.embedded.Member.deliveryAddress */ delete 
        from
            delivery_address 
        where
            member_id=?
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* delete collection row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ delete 
        from
            favorite_coffee_shop 
        where
            member_id=? 
            and favorite_coffee_shop=?
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
```
다음과 같이 수정할 객체를 지우고 다시 새로 끼워넣어야 한다.      

값 타입 컬렉션은 cascade와 orphanRemoval 속성이 자동으로 추가된 형식이라는 것을 알 수 있다.      

특히 Address의 경우에는 해당 member_id에 해당하는 모든 값을 지우고 다시 집어 넣는다는 것을 통해 위 해당 기능이 자동으로 설정되어 있다는 것을 알 수 있다.      

pk, 즉 식별자가 없으니 jpa에서는 느낌적으로 이렇게 생각하는 것 같다.           

```
에라! 모르겠다!!! 일단 그냥 member_id에 해당하는 값을 지우고 다시 집어넣으면 되잖아!
```
문제는 이것이 성능상에 이슈가 발생 할 수 있다는 것이다.      

지금이야 딸랑 2개지만 만일 어떤 유저가 배송지 주소를 10개를 세팅했다고 가정을 해보자.     

그중에 이 유저가 하나의 배송지 주소를 바꾸겠다?      

딱 느낌이 와야한다. 일단 그 유저의 member_id로 테이블에서 값을 지우고 인서트 쿼리가 10번을 날라갈 것이기 때문이다.      

![실행이미지](https://github.com/basquiat78/completedJPA/blob/10.value-type/capture/capture2.png)     
난 단지 하나를 바꾸고 싶었을 뿐이라고!!!      

그래서 @OrderColumn이라는게 있는데 이것을 한번 사용해 보자.       

```
@OrderColumn(name = "address_id")
@ElementCollection
@CollectionTable(name = "delivery_address", 
				 joinColumns = @JoinColumn(name = "member_id"))
private List<Address> deliveryAddress = new ArrayList<>();
```
이 경우에는 Set이 아니라 List로 받아야 한다.      

그리고 다시 실행을 하게 되면 

```
Hibernate: 
    
    alter table delivery_address 
       drop 
       foreign key FK7txh3nxg2wpt4lmsgalnxpcm5
Hibernate: 
    
    alter table favorite_coffee_shop 
       drop 
       foreign key FKgt7c8qhvvlfy1u1y5dtud3lku
Hibernate: 
    
    drop table if exists delivery
Hibernate: 
    
    drop table if exists delivery_address
Hibernate: 
    
    drop table if exists favorite_coffee_shop
Hibernate: 
    
    drop table if exists member
Hibernate: 
    
    create table delivery (
       id bigint not null auto_increment,
        member_city varchar(255),
        member_street varchar(255),
        member_zipcode varchar(255),
        courier varchar(255),
        status varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table delivery_address (
       member_id varchar(255) not null,
        member_city varchar(255),
        member_street varchar(255),
        member_zipcode varchar(255),
        address_id integer not null,
        primary key (member_id, address_id)
    ) engine=InnoDB
Hibernate: 
    
    create table favorite_coffee_shop (
       member_id varchar(255) not null,
        favorite_coffee_shop varchar(255)
    ) engine=InnoDB
Hibernate: 
    
    create table member (
       id varchar(255) not null,
        member_city varchar(255),
        member_street varchar(255),
        member_zipcode varchar(255),
        birth varchar(255),
        name varchar(255),
        password varchar(255),
        phone varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    alter table delivery_address 
       add constraint FK7txh3nxg2wpt4lmsgalnxpcm5 
       foreign key (member_id) 
       references member (id)
Hibernate: 
    
    alter table favorite_coffee_shop 
       add constraint FKgt7c8qhvvlfy1u1y5dtud3lku 
       foreign key (member_id) 
       references member (id)




Hibernate: 
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, address_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.deliveryAddress */ insert 
        into
            delivery_address
            (member_id, address_id, member_city, member_street, member_zipcode) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    select
        member0_.id as id1_3_0_,
        member0_.member_city as member_c2_3_0_,
        member0_.member_street as member_s3_3_0_,
        member0_.member_zipcode as member_z4_3_0_,
        member0_.birth as birth5_3_0_,
        member0_.name as name6_3_0_,
        member0_.password as password7_3_0_,
        member0_.phone as phone8_3_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Hibernate: 
    select
        deliveryad0_.member_id as member_i1_1_0_,
        deliveryad0_.member_city as member_c2_1_0_,
        deliveryad0_.member_street as member_s3_1_0_,
        deliveryad0_.member_zipcode as member_z4_1_0_,
        deliveryad0_.address_id as address_5_0_ 
    from
        delivery_address deliveryad0_ 
    where
        deliveryad0_.member_id=?
Hibernate: 
    select
        favoriteco0_.member_id as member_i1_2_0_,
        favoriteco0_.favorite_coffee_shop as favorite2_2_0_ 
    from
        favorite_coffee_shop favoriteco0_ 
    where
        favoriteco0_.member_id=?
Hibernate: 
    /* update
        collection row io.basquiat.model.embedded.Member.deliveryAddress */ update
            delivery_address 
        set
            member_city=?,
            member_street=?,
            member_zipcode=? 
        where
            member_id=? 
            and address_id=?
Hibernate: 
    /* delete collection row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ delete 
        from
            favorite_coffee_shop 
        where
            member_id=? 
            and favorite_coffee_shop=?
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
```
delivery_address 테이블이 생성되는 쿼리를 살펴보면 @OrderColumn에 설정한 값과 fk, 즉 member_id를 하나의 pk로 묶는 것을 볼 수 있다.        

그리고 실제로 값을 변경시 삭제하고 다시 인서트하는 것이 아닌 해당 row에 대해서만 업데이트 쿼리를 날린다.       

'그럼 이거 쓰면 되겠는데요?'      

사실 나는 이런 값타입을 사용할 때는 @Embedded쪽만 사용해 봤고 사실 Collection Type Value를 사용한 경험이 없다.      

하지만 책에서나 또는 여타 블로그에서는 예상치 못한 사이드 이펙트가 발생할 수 있기 때문에 정말로 단순한 경우가 아니면 사용을 그다지 권하지 않는 것 같다.       

느낌적으로 위에 예제에서 본 favoriteCoffeeShop정도?     

복잡한 스키마를 가지게 되면 그냥 차라리 값타입 컬렉션을 엔티티로 승격시켜서 @OneToMany로 풀어가는게 훨씬 유리하다는 것이다.      

엔티티로 승격하게 되면 해당 엔티티로 조회할 수도 있고 여러모로 유리하다. 어짜피 비슷하게 만드는데 차라리 이게 더 낫지 않을까?       

## Collection Value Type move @OneToMany      

기존의 사용하는 녀석이 있으니 엔티티로 승격시키기 위해서 클래스를 하나 만들어 보자.     

DeliveryAddress

```
package io.basquiat.model.embedded;

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
@Table(name = "delivery_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class DeliveryAddress {

	@Builder
	public DeliveryAddress(String city, String street, String zipcode) {
		super();
		this.city = city;
		this.street = street;
		this.zipcode = zipcode;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 시 */
	@Column(name = "delivery_city")
	private String city;
	
	/** 동 */
	@Setter
	@Column(name = "delivery_street")
	private String street;
	
	/** 우편 번호 */
	@Column(name = "delivery_zipcode")
	private String zipcode;

	/** 전체 주소 가져오 */
	public String totalAddress() {
		return city + " " + street + ", " + zipcode;
	}
	
}

```

Member는 다음과 같이

```
package io.basquiat.model.embedded;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "embedded_member")
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"deliveryAddress", "favoriteCoffeeShop"})
public class Member {

	@Builder
	public Member(String id, String password, String name, String birth, String phone, Address address) {
		super();
		this.id = id;
		this.password = password;
		this.name = name;
		this.birth = birth;
		this.phone = phone;
		this.address = address;
	}

	/** 사용자 아이디 */
	@Id
	private String id;

	/** 사용자 비번 */
	private String password;
	
	/** 사용자 이름 */
	private String name;
	
	/** 사용자 생년월일 */
	private String birth;
	
	/** 사용자 전번 */
	private String phone;
	
	/** 사용자 주소 */
	@Embedded
	private Address address;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "member_id")
	private List<DeliveryAddress> deliveryAddress = new ArrayList<>();
	
	@ElementCollection
	@CollectionTable(name = "favorite_coffee_shop", 
					 joinColumns = @JoinColumn(name = "member_id"))
	@Column(name = "favorite_coffee_shop")
	private Set<String> favoriteCoffeeShop = new HashSet<>();
	
}
```
OneToMany로 사용한다.

```
// 1. embedded address 생성
Address address = Address.builder().city("서울시")
								   .street("논현동")
								   .zipcode("50000")
								   .build();

// 2. delivery address
DeliveryAddress myHomeAddress = DeliveryAddress.builder().city("서울시")
													   	 .street("논현동 우리집")
													   	 .zipcode("50000")
													   	 .build();

DeliveryAddress myOfficialAddress = DeliveryAddress.builder().city("서울시")
														   	 .street("논현동 회사")
														   	 .zipcode("50000")
														   	 .build();

// 신규가입자 
Member newMember = Member.builder().id("basquiat")
								   .password("mypassword")
								   .name("Jean-Michel-Basquiat")
								   .birth("1960-12-22")
								   .phone("010-0000-00000")
								   .address(address)
								   .build();

newMember.getDeliveryAddress().add(myHomeAddress);
newMember.getDeliveryAddress().add(myOfficialAddress);

newMember.getFavoriteCoffeeShop().add("별다방");
newMember.getFavoriteCoffeeShop().add("커피 자판기 일명 벽다방");
newMember.getFavoriteCoffeeShop().add("커피콩");

em.persist(newMember);

em.flush();
em.clear();

Member selectedMember = em.find(Member.class, newMember.getId());
DeliveryAddress updateAddress = selectedMember.getDeliveryAddress().get(0);
updateAddress.setStreet("논현동 우리집 !!!!!!");
selectedMember.getDeliveryAddress().remove(1);

result grid

Hibernate: 
    /* insert io.basquiat.model.embedded.Member
        */ insert 
        into
            member
            (member_city, member_street, member_zipcode, birth, name, password, phone, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.embedded.DeliveryAddress
        */ insert 
        into
            delivery_address
            (delivery_city, delivery_street, delivery_zipcode) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.embedded.DeliveryAddress
        */ insert 
        into
            delivery_address
            (delivery_city, delivery_street, delivery_zipcode) 
        values
            (?, ?, ?)
Hibernate: 
    /* create one-to-many row io.basquiat.model.embedded.Member.deliveryAddress */ update
        delivery_address 
    set
        member_id=? 
    where
        id=?
Hibernate: 
    /* create one-to-many row io.basquiat.model.embedded.Member.deliveryAddress */ update
        delivery_address 
    set
        member_id=? 
    where
        id=?
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    /* insert collection
        row io.basquiat.model.embedded.Member.favoriteCoffeeShop */ insert 
        into
            favorite_coffee_shop
            (member_id, favorite_coffee_shop) 
        values
            (?, ?)
Hibernate: 
    select
        member0_.id as id1_3_0_,
        member0_.member_city as member_c2_3_0_,
        member0_.member_street as member_s3_3_0_,
        member0_.member_zipcode as member_z4_3_0_,
        member0_.birth as birth5_3_0_,
        member0_.name as name6_3_0_,
        member0_.password as password7_3_0_,
        member0_.phone as phone8_3_0_ 
    from
        member member0_ 
    where
        member0_.id=?
Hibernate: 
    select
        deliveryad0_.member_id as member_i5_1_0_,
        deliveryad0_.id as id1_1_0_,
        deliveryad0_.id as id1_1_1_,
        deliveryad0_.delivery_city as delivery2_1_1_,
        deliveryad0_.delivery_street as delivery3_1_1_,
        deliveryad0_.delivery_zipcode as delivery4_1_1_ 
    from
        delivery_address deliveryad0_ 
    where
        deliveryad0_.member_id=?
Hibernate: 
    /* delete one-to-many row io.basquiat.model.embedded.Member.deliveryAddress */ update
            delivery_address 
        set
            member_id=null 
        where
            member_id=? 
            and id=?
Hibernate: 
    /* delete io.basquiat.model.embedded.DeliveryAddress */ delete 
        from
            delivery_address 
        where
            id=?
```
좋은 예제는 아니지만 street프로퍼티에 setter를 설정해서 변경 감지로 업데이트 하거나 cascade와 orpahanRemoval 설정에 의해서 삭제도 가능하게 할 수 있다.       

또한 엔티티로 승격했기때문에

```
DeliveryAddress selected = em.find(DeliveryAddress.class, 1L);
```
처럼 해당 테이블을 조회할 수도 있다.     


# At A Glance      

개인적으로 이 부분은 참 쉬우면서도 어려운 부분이 아닌가 싶다. 그만큼 고민도 많이 해야하고 전략을 어떻게 가져가야하는지에 대한 부분도 가장 큰 거 같다.      

많은 부분을 좀 생략하긴 했지만 적어도 어느 정도의 사용하는데에 단초를 제공하는데에는 부족하지 않다고 생각한다.     

다음 브랜치에서는 JPQL과 관련된 부분을 진행할 예정이다.      