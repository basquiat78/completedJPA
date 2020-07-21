# 상속 매핑하기     

## 일단 시작하기 전에     

이전에 만들었던 모든 객체에 붙어 있는 @Entity를 주석처리하고 시작한다.     

사실 이 매핑에 대해서 책을 보면서 이걸 언제 쓰는건가라는 생각을 해왔다.     

왜냐하면 보통은 단일 테이블에 속성들을 담는 형식이 대부분이였기 때문이다.     

물론 DB에서는 상속관계라는게 존재하기는 한건가라는 생각도 있다.    

만일 지금 내가 이커머스쪽을 경험하지 않았다면 이 부분은 그냥 대충 떼우고 넘어갔을지도 모른다.    

소개하는 이유는 몇 가지 이유가 있다.    

일단 회사 입사할 때 상품에 대한 테이블은 다음과 같은 구조였다.     

![실행이미지](https://github.com/basquiat78/completedJPA/blob/8.inheritance-mapping/capture/capture1.png)    

한 테이블에 모든 정보를 그냥 다 때려박았다. 컬럼이 대충 40개가 넘었던거 같은데??      

현재는 이 테이블을 다음과 같이 나눴다.     

![실행이미지](https://github.com/basquiat78/completedJPA/blob/8.inheritance-mapping/capture/capture2.png)     

사실 조인되는 테이블이 더 많긴 하다. 간략하게 어떻게 나눴는지에 대한 부분을 표현한 것 뿐이다.     

하지만 이것은 단지 정규화시킨 구조로 단순 OneToOne의 관계를 맺을 뿐 상속관계와는 다르다.     

말 그대로 공통 관심사를 분리시킨 구조에 지나지 않는다.    

하지만 이것을 써야 할 필요가 있는가에 대해서는 바로 11번가와 연동을 하는 과정에 발생한 테이블 구조의 차이를 알게 되면서이다.     

옷, 악세사리등을 파는 쇼핌몰에서 다른 카테고리의 상관과는 별반 상관이 없었지만 11번가의 그 수많은 카테고리와 상품의 구조는 지금 설명하기 딱 좋기도 하고 실제 책에서도 이와 비슷한 이야기를 하기 때문이다.     

또한 내가 자주 가는 악기 쇼핑몰의 경우에도 적용될 수 있다.    

자 그럼 나는 내가 자주 가는 악기 쇼핑몰을 예로 들 것이다.     

악기 쇼핑몰에서는 단순하게 악기만 팔지는 않는다. 악기를 다루기 위한 교재도 팔기도 하고 관련 음반도 팔기도 하며 악기에 달리는 스트링도 팔고 픽업, 프리앰프, 앰프, 스탠드도 팔고 암튼 악기와 관련된 모든 상품들을 판다.    

악기의 경우에는 현수, 또는 악기를 만들 때 사용한 나무의 종류들이 기재될 것이고 교재의 경우에는 저자 (공동 저자도 있을 수 있고)가 있고 음반은 아티스트 명, 메인 악기 (베이스인지 기타인지), 발매 년도, 장르, 레이블의 정보를 가질 수 있다.    

스탠드도 기타용인지 키보드용인지 구분을 지어야 하는 구분값, 재질 정보도 포함해야한다. 스트링은? 스댕인지 니켈인지 코팅줄인지도 표기해야하고 4,5,6현 낱줄 판매인지 어디 회사인지도 기재해야 한다.    

그럼 하나의 테이블에 이 모든 정보를 다 때려박아야 하는건가? 라는 생각이 퍼득 들었다.    

그러면  테이블 구조을 과연 어떻게 가져가야 할까라는 고민이 확 든다.     

왜냐하면 쇼핑몰입장에서는 어째든 이 모든 것은 그냥 '상품'일 뿐이기 때문이다.     

이런 방식이라면 아마도 테이블 구조를 어떻게 가져가야 하는지 고민을 해봐야 하는 것이다.        

## 단일 테이블 전략          

그냥 하나의 테이블에 모든 것을 때려 박는다이다. 하지만 이 경우에는 단점이 뭐냐면 모든 상품을 검색하는 쿼리가 있다면 자신과 관계없는 컬럼의 정보도 가져와야 한다.     

상품 정보를 인서트할 때 대상 상품 정보에 해당하는 정보는 다 null로 들어가야 하기 때문인데  null이겠지만 어째든 가져오고 봐야 한다.    

또한 상품의 타입이 추가가 된다면 어떻게 될까?    

컬럼이 옆으로 늘어나는 것이 좀 걸리긴 하지만 뭐 어쩔 수 없다. ~~여기 회사에서는 컬럼이 처음 40개가 넘었다니깐???~~         

다만 장점은 성능에 장점이 있을 것이다.     

데이터를 밀어 넣을 때나 조회할 때 또는 업데이트 할때 조인이 걸린 테이블이 없으니 한 테이블에 대고 실행하니 이 부분에서는 속도에 대한 장점이 고려될 것이다.    

일단 이것을 erd로 한번 그려보자.     

간략하게 product라는 테이블이 존재하고 그 테이블에는 악기의 정보, 교재 정보, 스트링 정보만 간략하게 한번 만들어 보기로 한다.    

![실행이미지](https://github.com/basquiat78/completedJPA/blob/8.inheritance-mapping/capture/capture3.png)     

지금이야 테스트이기 때문에 그나마 컬럼이 pk포함 14개뿐이 안되지만 만일 이 악기사 쇼핑몰의 product type이 늘어난다고 생각해 보자.     

아마 컬럼이 그때마다 길어질 것은 자명한 일이다.      

자 일단 엔티티 설계를 하기 전에 어떤 것이 있는지 먼저 알아보자.     

### @Inheritance     

상속 매핑 전략을 선택하겠다는 어노테이션이며 이 어노테이션에는 그 중 어떤 방식으로 가져갈 것인지에 대한 정보를 명시할 수 있다.     

기본으로 저렇게만 걸면 단일 테이블 전략을 선택한다.     

이것은 뒤에 예제를 통해서 하나씩 알아보자.     

### @DiscriminatorColumn     

위에 erd를 살펴보면 product_type이라는 컬럼이 있는 것을 볼 수 있다.    

어떤 정보도 명시하지 않으면 기본으로 DTYPE이라는 컬럼을 만들게 된다.     

이것도 뒤에 예제를 통해서 하나씩 알아보자.    

### @DiscriminatorValue     

위에 erd를 다시 살펴보자. product_type은 우리가 데이터를 인서트시에 지정해서 넣어줄 수 있지만 이것을 통해서 해당 엔티티의 경우에는 이 어노테이션에 명시해서 그 값을 자동으로 세팅하게 해줄 수 있다.    

이것도 뒤에 예제를 통해서 하나씩 알아보자.     

* 작업하기에 앞서서...한 시간을 삽질했는데 왠간하면 예약어는 안쓰는게 좋겠다. 

```
inheritance Could not determine type for:, at table: product, for columns: [org.hibernate.mapping.Column(maker)]
```
보통 악기의 줄을 String이라고 하기때문에 엔티티 객체 면을 String으로 했다가 마주한 에러앞에서 무릎을 꿇고 말았다. 혹시나 해서 클래스명을 바꾸니 잘 되어버린.....

자 그럼 첫 번째 우리는 단일 테이블 전략을 구성해 보기로 했으니 하나씩 만들어 보자.     

Product

```
package io.basquiat.model.product;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Inheritance
@DiscriminatorColumn(name = "product_type")  
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Product {
	
	public Product(String name, String maker) {
		this.name = name;
		this.maker = maker;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 생산품 명 */
	private String name;
	
	/** 제조사 */
	private String maker;

}
```
erd기준으로 Product에 대한 공통 관심사를 Product클래스에 담아냈다. 이녀석이 부모가 되기 때문에 @Inheritance를 붙였다. 기본값이 단일 테이블이니 생략하고 상품을 구분짓은 컬럼은 "product_type으로 지정했다.    

그리고 Product의 클래스 앞에 abstract가 붙은 것을 확인하자.     

이유가 무엇일까? 보통 추상 클래스는 부모 클래스를 extends하는 자식 클래스에 의해서 완성되어 진다.    

만일 Product가 추상 클래스가 아니라면 Product클래스 내부에 선언된 name, maker만으로도 사용할 수 있게 된다.     

물론 Product가 추상클래스가 아니라도 구성하는데는 문제가 없지만 자칫하면 Product자체만을 사용할 수 있는 소지가 생길 수 있기에 이 경우에는 추상 클래스로 선언하자. ~~자바 기초인가요?~~     

Book

```
package io.basquiat.model.product;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@DiscriminatorValue("BOOK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public class Book extends Product {
	
	@Builder
	public Book(String name, String maker, String author, String type) {
		super(name, maker);
		this.author = author;
		this.type = type;
	}

	/** 교재 저자 */
	@Column(name = "book_author")
	private String author;
	
	/** 교재 악기 타입 */
	@Column(name = "book_type")
	private String type;

}
```
역시 erd에서 교재와 관련된 공통 관심사를 분리했다. 단일 테이블 전략이기 때문에 @Table은 붙이지 않는다. 또한 구분값은 "BOOK"으로 지정했다.     

이 부분은 Enum으로 코드를 관리해도 괜찮을 것이다.    

Instrumental     

```
package io.basquiat.model.product;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@DiscriminatorValue("INSTRUMENTAL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public class Instrumental extends Product {

	@Builder
	public Instrumental(String name, String maker, int stringCnt, String type, String bodyWood, String neckWood,
			String fingerboardWood) {
		super(name, maker);
		this.stringCnt = stringCnt;
		this.type = type;
		this.bodyWood = bodyWood;
		this.neckWood = neckWood;
		this.fingerboardWood = fingerboardWood;
	}
	
	/** 악기의 현수 */
	@Column(name = "ins_string_cnt")
	private int stringCnt;

	/** 
	 * 악기 타입 
	 * 지금까지 배운 것을 토대로 이 부분은 Enum으로 체크 할 수 있다.
	 * 테스트에서는 그냥 스트링 타입으로 한다.
	 * 
	 */
	@Column(name = "ins_type")
	private String type;
	
	/** 바디 나무 재질 */
	@Column(name = "ins_body_wood")
	private String bodyWood;
	
	/** 넥 나무 재질 */
	@Column(name = "int_neck_wood")
	private String neckWood;
	
	/** 핑거보드 나무 재질 */
	@Column(name = "ins_finger_wood")
	private String fingerboardWood;
	
}
```

InstrumentalString

```
package io.basquiat.model.product;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@DiscriminatorValue("STRING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(callSuper = true)
public class InstrumentalString extends Product {

	@Builder
	public InstrumentalString(String name, String maker, String material, String isCoating, int count) {
		super(name, maker);
		this.material = material;
		this.isCoating = isCoating;
		this.count = count;
	}

	/** 교재 저자 */
	@Column(name = "string_material")
	private String material;
	
	/** 교재 저자 */
	@Column(name = "is_coating")
	private String isCoating;
	
	/** 교재 저자 */
	@Column(name = "string_cnt")
	private int count;
}
```
역시 관심사를 분류해서 각각의 엔티티를 만들었다.     

DDL을 한번 생성해 보면 다음과 같이 로그를 볼 수 있다.      

```
Hibernate: 
    
    drop table if exists product
Hibernate: 
    
    create table product (
       product_type varchar(31) not null,
        id bigint not null auto_increment,
        maker varchar(255),
        name varchar(255),
        string_cnt integer,
        is_coating varchar(255),
        string_material varchar(255),
        ins_body_wood varchar(255),
        ins_finger_wood varchar(255),
        int_neck_wood varchar(255),
        ins_string_cnt integer,
        ins_type varchar(255),
        book_author varchar(255),
        book_type varchar(255),
        primary key (id)
    ) engine=InnoDB
```
자...상속한 엔티티들의 정보를 보고 product라는 테이블에 모든 정보를 담은 테이블이 생성된 것을 알 수 있다. 또한 우리가 설정한 product_type컬럼도 잘 생성되었다.    

실제로 인서트하는 코드를 한번 짜보자.     

아...만들고 보니 가격이 누락되었는데 그냥 가보자.      

```
Instrumental foderaBassGuitar = Instrumental.builder().name("Fodera Emperor 5")
        														  .maker("Fodera Guitar")
        														  .type("BASS")
        														  .bodyWood("Alder")
        														  .neckWood("Maple")
        														  .fingerboardWood("Ebony")
    														  	  .stringCnt(5)
    														  	  .build();
em.persist(foderaBassGuitar);

Book rockSchoolVolOne = Book.builder().name("Rock School Grade 5")
									  .maker("Hal Leonard")
									  .author("Stuart Clayton")
									  .type("BOOKFORBASS")
									  .build();
em.persist(rockSchoolVolOne);

InstrumentalString dadarioString = InstrumentalString.builder().name("Dadario XL String")
															   .maker("Dadario")
															   .material("Nickel")
															   .count(5)
															   .build();
em.persist(dadarioString);
em.flush();
em.clear();

Product product1 = em.find(Product.class, foderaBassGuitar.getId());
System.out.println(product1);

Product product2 = em.find(Product.class, rockSchoolVolOne.getId());
System.out.println(product2);

Product product3 = em.find(Product.class, dadarioString.getId());
System.out.println(product3);

```

결과도 한번 확인해 보자.     

```
Hibernate: 
    /* insert io.basquiat.model.product.Instrumental
        */ insert 
        into
            product
            (maker, name, ins_body_wood, ins_finger_wood, int_neck_wood, ins_string_cnt, ins_type, product_type) 
        values
            (?, ?, ?, ?, ?, ?, ?, 'INSTRUMENTAL')
Hibernate: 
    /* insert io.basquiat.model.product.Book
        */ insert 
        into
            product
            (maker, name, book_author, book_type, product_type) 
        values
            (?, ?, ?, ?, 'BOOK')
Hibernate: 
    /* insert io.basquiat.model.product.InstrumentalString
        */ insert 
        into
            product
            (maker, name, string_cnt, is_coating, string_material, product_type) 
        values
            (?, ?, ?, ?, ?, 'STRING')
Hibernate: 
    select
        product0_.id as id2_0_0_,
        product0_.maker as maker3_0_0_,
        product0_.name as name4_0_0_,
        product0_.string_cnt as string_c5_0_0_,
        product0_.is_coating as is_coati6_0_0_,
        product0_.string_material as string_m7_0_0_,
        product0_.ins_body_wood as ins_body8_0_0_,
        product0_.ins_finger_wood as ins_fin9_0_0_,
        product0_.int_neck_wood as int_nec10_0_0_,
        product0_.ins_string_cnt as ins_str11_0_0_,
        product0_.ins_type as ins_typ12_0_0_,
        product0_.book_author as book_au13_0_0_,
        product0_.book_type as book_ty14_0_0_,
        product0_.product_type as product_1_0_0_ 
    from
        Product product0_ 
    where
        product0_.id=?            
Instrumental(super=Product(id=1, name=Fodera Emperor 5, maker=Fodera Guitar), stringCnt=5, type=BASS, bodyWood=Alder, neckWood=Maple, fingerboardWood=Ebony)
    select
        product0_.id as id2_0_0_,
        product0_.maker as maker3_0_0_,
        product0_.name as name4_0_0_,
        product0_.string_cnt as string_c5_0_0_,
        product0_.is_coating as is_coati6_0_0_,
        product0_.string_material as string_m7_0_0_,
        product0_.ins_body_wood as ins_body8_0_0_,
        product0_.ins_finger_wood as ins_fin9_0_0_,
        product0_.int_neck_wood as int_nec10_0_0_,
        product0_.ins_string_cnt as ins_str11_0_0_,
        product0_.ins_type as ins_typ12_0_0_,
        product0_.book_author as book_au13_0_0_,
        product0_.book_type as book_ty14_0_0_,
        product0_.product_type as product_1_0_0_ 
    from
        Product product0_ 
    where
        product0_.id=?  
Book(super=Product(id=2, name=Rock School Grade 5, maker=Hal Leonard), author=Stuart Clayton, type=BOOKFORBASS)
    select
        product0_.id as id2_0_0_,
        product0_.maker as maker3_0_0_,
        product0_.name as name4_0_0_,
        product0_.string_cnt as string_c5_0_0_,
        product0_.is_coating as is_coati6_0_0_,
        product0_.string_material as string_m7_0_0_,
        product0_.ins_body_wood as ins_body8_0_0_,
        product0_.ins_finger_wood as ins_fin9_0_0_,
        product0_.int_neck_wood as int_nec10_0_0_,
        product0_.ins_string_cnt as ins_str11_0_0_,
        product0_.ins_type as ins_typ12_0_0_,
        product0_.book_author as book_au13_0_0_,
        product0_.book_type as book_ty14_0_0_,
        product0_.product_type as product_1_0_0_ 
    from
        Product product0_ 
    where
        product0_.id=?  
InstrumentalString(super=Product(id=3, name=Dadario XL String, maker=Dadario), material=Nickel, isCoating=null, count=5)
```
사실 InstrumentalString의 경우에는 해당 스트링이 기타용인지 베이스용인지 구분이 필요한데 누락되었다.    

그리고 Book의 경우에도 isbn같은 고유 정보도 있는데 이 부분도 누락되었다.     

뭐 테스트니깐 그냥 애교로 넘어가자.       

보면 셀렉트 쿼리도 그냥 pk하나로 가져온다.    

이렇게 단일 테이블 전략을 한번 알아봤다.     

## 조인 전략     

조인 테이블의 경우에는 맨 위에서 언급했던 erd를 참조해 볼 수 있다.     

마치 엔티티의 상속 관계처럼 테이블도 그와 비슷하게 만들어서 그 구조를 가져가는 방식이다.     

그럼 erd로 한번 그려보자.     

![실행이미지](https://github.com/basquiat78/completedJPA/blob/8.inheritance-mapping/capture/capture4.png)     

기존에 설정된 엔티티에서 그다지 바뀔 것은 없다.     

```
package io.basquiat.model.product;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "product")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "product_type")  
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public abstract class Product {
	
	public Product(String name, int price, String maker) {
		this.name = name;
		this.price = price;
		this.maker = maker;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 생산품 명 */
	private String name;
	
	/** 상품 가격 */
	private int price;
	
	/** 제조사 */
	private String maker;

}
```
다음과 같이 전략을 JOINED로 설정만 바꿨을 뿐이다.    

또한 각 엔티티마다 @Table을 통해서 이름을 따로 지정한 정도? ~~일단 price와 book에서 누락된 isbn도 추가했어~~    

자 그리고 기존의 코드를 그냥 한번 실행해보자.     


```
Instrumental foderaBassGuitar = Instrumental.builder().name("Fodera Emperor 5")
        														  .price(15000000)
        														  .maker("Fodera Guitar")
        														  .type("BASS")
        														  .bodyWood("Alder")
        														  .neckWood("Maple")
        														  .fingerboardWood("Ebony")
    														  	  .stringCnt(5)
    														  	  .build();
em.persist(foderaBassGuitar);

Book rockSchoolVolOne = Book.builder().name("Rock School Grade 5")
									  .price(35000)
									  .maker("Hal Leonard")
									  .author("Stuart Clayton")
									  .type("BOOKFORBASS")
									  .isbn("ISBN-11111111111111111")
									  .build();
em.persist(rockSchoolVolOne);

InstrumentalString dadarioString = InstrumentalString.builder().name("Dadario XL String")
															   .price(30000)
															   .maker("Dadario")
															   .material("Nickel")
															   .isCoating("N")
															   .count(5)
															   .build();
em.persist(dadarioString);
em.flush();
em.clear();

Product product1 = em.find(Product.class, foderaBassGuitar.getId());
System.out.println(product1);

Product product2 = em.find(Product.class, rockSchoolVolOne.getId());
System.out.println(product2);

Product product3 = em.find(Product.class, dadarioString.getId());
System.out.println(product3);

```
실행을 하면 어떻게 될까?     

```
Hibernate: 
    
    alter table book 
       drop 
       foreign key FK8cjf4cjanicu58p2l5t8d9xvu
Hibernate: 
    
    alter table instrumental 
       drop 
       foreign key FKsb4yjag7nk3lvtnd5lslwn67p
Hibernate: 
    
    alter table instrumental_string 
       drop 
       foreign key FKidxnw9ymltl6kfrwtgq5ldh0n
Hibernate: 
    
    drop table if exists book
Hibernate: 
    
    drop table if exists instrumental
Hibernate: 
    
    drop table if exists instrumental_string
Hibernate: 
    
    drop table if exists product
Hibernate: 
    
    create table book (
       book_author varchar(255),
        isbn varchar(255),
        book_type varchar(255),
        id bigint not null,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table instrumental (
       ins_body_wood varchar(255),
        ins_finger_wood varchar(255),
        int_neck_wood varchar(255),
        ins_string_cnt integer,
        ins_type varchar(255),
        id bigint not null,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table instrumental_string (
       string_cnt integer,
        is_coating varchar(255),
        string_material varchar(255),
        id bigint not null,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table product (
       product_type varchar(31) not null,
        id bigint not null auto_increment,
        maker varchar(255),
        name varchar(255),
        price integer not null,
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    alter table book 
       add constraint FK8cjf4cjanicu58p2l5t8d9xvu 
       foreign key (id) 
       references product (id)
Hibernate: 
    
    alter table instrumental 
       add constraint FKsb4yjag7nk3lvtnd5lslwn67p 
       foreign key (id) 
       references product (id)
Hibernate: 
    
    alter table instrumental_string 
       add constraint FKidxnw9ymltl6kfrwtgq5ldh0n 
       foreign key (id) 
       references product (id)
Hibernate: 
    /* insert io.basquiat.model.product.Instrumental
        */ insert 
        into
            product
            (maker, name, price, product_type) 
        values
            (?, ?, ?, 'INSTRUMENTAL')
Hibernate: 
    /* insert io.basquiat.model.product.Instrumental
        */ insert 
        into
            instrumental
            (ins_body_wood, ins_finger_wood, int_neck_wood, ins_string_cnt, ins_type, id) 
        values
            (?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.product.Book
        */ insert 
        into
            product
            (maker, name, price, product_type) 
        values
            (?, ?, ?, 'BOOK')
Hibernate: 
    /* insert io.basquiat.model.product.Book
        */ insert 
        into
            book
            (book_author, isbn, book_type, id) 
        values
            (?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.product.InstrumentalString
        */ insert 
        into
            product
            (maker, name, price, product_type) 
        values
            (?, ?, ?, 'STRING')
Hibernate: 
    /* insert io.basquiat.model.product.InstrumentalString
        */ insert 
        into
            instrumental_string
            (string_cnt, is_coating, string_material, id) 
        values
            (?, ?, ?, ?)

    select
        product0_.id as id2_3_0_,
        product0_.maker as maker3_3_0_,
        product0_.name as name4_3_0_,
        product0_.price as price5_3_0_,
        product0_1_.string_cnt as string_c1_2_0_,
        product0_1_.is_coating as is_coati2_2_0_,
        product0_1_.string_material as string_m3_2_0_,
        product0_2_.ins_body_wood as ins_body1_1_0_,
        product0_2_.ins_finger_wood as ins_fing2_1_0_,
        product0_2_.int_neck_wood as int_neck3_1_0_,
        product0_2_.ins_string_cnt as ins_stri4_1_0_,
        product0_2_.ins_type as ins_type5_1_0_,
        product0_3_.book_author as book_aut1_0_0_,
        product0_3_.isbn as isbn2_0_0_,
        product0_3_.book_type as book_typ3_0_0_,
        product0_.product_type as product_1_3_0_ 
    from
        Product product0_ 
    left outer join
        instrumental_string product0_1_ 
            on product0_.id=product0_1_.id 
    left outer join
        instrumental product0_2_ 
            on product0_.id=product0_2_.id 
    left outer join
        book product0_3_ 
            on product0_.id=product0_3_.id 
    where
        product0_.id=?     
Instrumental(super=Product(id=1, name=Fodera Emperor 5, price=15000000, maker=Fodera Guitar), stringCnt=5, type=BASS, bodyWood=Alder, neckWood=Maple, fingerboardWood=Ebony)

    select
        product0_.id as id2_3_0_,
        product0_.maker as maker3_3_0_,
        product0_.name as name4_3_0_,
        product0_.price as price5_3_0_,
        product0_1_.string_cnt as string_c1_2_0_,
        product0_1_.is_coating as is_coati2_2_0_,
        product0_1_.string_material as string_m3_2_0_,
        product0_2_.ins_body_wood as ins_body1_1_0_,
        product0_2_.ins_finger_wood as ins_fing2_1_0_,
        product0_2_.int_neck_wood as int_neck3_1_0_,
        product0_2_.ins_string_cnt as ins_stri4_1_0_,
        product0_2_.ins_type as ins_type5_1_0_,
        product0_3_.book_author as book_aut1_0_0_,
        product0_3_.isbn as isbn2_0_0_,
        product0_3_.book_type as book_typ3_0_0_,
        product0_.product_type as product_1_3_0_ 
    from
        Product product0_ 
    left outer join
        instrumental_string product0_1_ 
            on product0_.id=product0_1_.id 
    left outer join
        instrumental product0_2_ 
            on product0_.id=product0_2_.id 
    left outer join
        book product0_3_ 
            on product0_.id=product0_3_.id 
    where
        product0_.id=?
Book(super=Product(id=2, name=Rock School Grade 5, price=35000, maker=Hal Leonard), author=Stuart Clayton, type=BOOKFORBASS, isbn=ISBN-11111111111111111)

    select
        product0_.id as id2_3_0_,
        product0_.maker as maker3_3_0_,
        product0_.name as name4_3_0_,
        product0_.price as price5_3_0_,
        product0_1_.string_cnt as string_c1_2_0_,
        product0_1_.is_coating as is_coati2_2_0_,
        product0_1_.string_material as string_m3_2_0_,
        product0_2_.ins_body_wood as ins_body1_1_0_,
        product0_2_.ins_finger_wood as ins_fing2_1_0_,
        product0_2_.int_neck_wood as int_neck3_1_0_,
        product0_2_.ins_string_cnt as ins_stri4_1_0_,
        product0_2_.ins_type as ins_type5_1_0_,
        product0_3_.book_author as book_aut1_0_0_,
        product0_3_.isbn as isbn2_0_0_,
        product0_3_.book_type as book_typ3_0_0_,
        product0_.product_type as product_1_3_0_ 
    from
        Product product0_ 
    left outer join
        instrumental_string product0_1_ 
            on product0_.id=product0_1_.id 
    left outer join
        instrumental product0_2_ 
            on product0_.id=product0_2_.id 
    left outer join
        book product0_3_ 
            on product0_.id=product0_3_.id 
    where
        product0_.id=?
InstrumentalString(super=Product(id=3, name=Dadario XL String, price=30000, maker=Dadario), material=Nickel, isCoating=N, count=5)
```

오호라 로그가 쪼옴 많긴 하다. 왜냐하면 erd대로 테이블을 생성하는 과정이 있을 것이다. 또한 현재는 각 테이블마다 조인으로 연결되어 있기에 인서트 쿼리도 한 상품당 2번의 쿼리 즉, product와 각 상품의 product_type에 따른 테이블에 인서트를 하는 쿼리가 날아가는 것을 알 수 있다.     

또한 셀렉트 쿼리도 Join을 통해서 가져온다.     

책에서도 그렇고 대부분의 블로그를 보면 이 방식이 가장 장점이 많다고 말한다.     

장점은 테이블의 정규화로 인한 장점으로 저장공간의 효율화를 꼽는다.     

하지만 언제나 trade-off, Prop and cons가 항상 존재하듯이 조인이 많으면 생길 수 있는 성능의 저하를 얘기한다.    

당연히 조회 쿼리가 조인을 하기 때문에 단일 테이블에 비해 조금 복잡함? 그리고 위에서 언급한 인서트 쿼리가 2번 날아가는 점등....      

그럼에도 이것이 최선으로 받아드리는 듯. ~~이것이 최선입니까?~~         

여기서에서 고려해 볼 수 있는 것은 테이블의 성격이나 비지니스의 요구에 따라서 단일 또는 조인 전략을 선택하는 것이 중요한 듯 싶다.     

그리고 마지막 왠간하면? 또는 절대 쓰지 말라고 하는 구현 클래스별 테이블 전략을 한번 알아보자.     

## 구현 클래스별 테이블 전략    

이것은 말 그대로 각 엔티티별로 테이블을 생성하는 방식이다. 책에서는 왠간하면 쓰지 말라고 하는 전략이지만 그래도 스펙이 있으니 한번 구현을 해보자.     

erd는 다음과 같이 변경된다.    

![실행이미지](https://github.com/basquiat78/completedJPA/blob/8.inheritance-mapping/capture/capture5.png)     

뭔가 중복된 컬럼들이 각 테이블에 다 들어가 있다.     

자 그럼 일단 Product쪽의 상속 매핑 전략을 변경해 보자.

```
package io.basquiat.model.product;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name = "product_type")  
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public abstract class Product {
	
	public Product(String id, String name, int price, String maker) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.maker = maker;
	}

	@Id
	private String id;
	
	/** 생산품 명 */
	private String name;
	
	/** 상품 가격 */
	private int price;
	
	/** 제조사 */
	private String maker;

}
```
잘 보면 기본키 매핑이 그냥 직접 매핑이다. 이 이유는 mySql의 경우 Identity전략을 쓰면 오류가 발생한다. 곰곰히 생각해 보니 그럴 수 밖에 없는 것이 클래스별로 테이블을 생성하기 때문에 그런 듯 싶다.     

그 이유는 뒤에서 설명하겠다.     

또한 지금은 직접 매핑이지만 시퀀스 전략을 이용해 볼 수 있다. 하지만 mySql이라 테이블을 생성해서 시퀀스를 흉내내기 때문에 직접 매핑으로 가져갔다.    

그리고 단지  InheritanceType.TABLE_PER_CLASS로 변경했다. 사실 @DiscriminatorColumn도 의미가 없다. 그것은 위에 언급한 대로 클래스별로 테이블을 생성하니 구분값이 의미가 없다.    

기본키를 직접 매핑으로 했기 때문에 각 각의 엔티티도 변경이 좀 생겼다.    

Book

```
package io.basquiat.model.product;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "book")
@DiscriminatorValue("BOOK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public class Book extends Product {
	
	@Builder
	public Book(String id, String name, int price, String maker, String author, String type, String isbn) {
		super(id, name, price, maker);
		this.author = author;
		this.type = type;
		this.isbn = isbn;
	}

	/** 교재 저자 */
	@Column(name = "book_author")
	private String author;
	
	/** 교재 악기 타입 */
	@Column(name = "book_type")
	private String type;
	
	/** 부여된 고유 번호  */
	private String isbn;

}
```

Instrumental

```
package io.basquiat.model.product;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "instrumental")
@DiscriminatorValue("INSTRUMENTAL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public class Instrumental extends Product {

	@Builder
	public Instrumental(String id, String name, int price, String maker, int stringCnt, String type, String bodyWood, String neckWood,
			String fingerboardWood) {
		super(id, name, price, maker);
		this.stringCnt = stringCnt;
		this.type = type;
		this.bodyWood = bodyWood;
		this.neckWood = neckWood;
		this.fingerboardWood = fingerboardWood;
	}
	
	/** 악기의 현수 */
	@Column(name = "ins_string_cnt")
	private int stringCnt;

	/** 
	 * 악기 타입 
	 * 지금까지 배운 것을 토대로 이 부분은 Enum으로 체크 할 수 있다.
	 * 테스트에서는 그냥 스트링 타입으로 한다.
	 * 
	 */
	@Column(name = "ins_type")
	private String type;
	
	/** 바디 나무 재질 */
	@Column(name = "ins_body_wood")
	private String bodyWood;
	
	/** 넥 나무 재질 */
	@Column(name = "int_neck_wood")
	private String neckWood;
	
	/** 핑거보드 나무 재질 */
	@Column(name = "ins_finger_wood")
	private String fingerboardWood;
	
}
```

InstrumentalString

```
package io.basquiat.model.product;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "instrumental_string")
@DiscriminatorValue("STRING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(callSuper = true)
public class InstrumentalString extends Product {

	@Builder
	public InstrumentalString(String id, String name, int price, String maker, String material, String isCoating, int count) {
		super(id, name, price, maker);
		this.material = material;
		this.isCoating = isCoating;
		this.count = count;
	}

	/** 교재 저자 */
	@Column(name = "string_material")
	private String material;
	
	/** 교재 저자 */
	@Column(name = "is_coating")
	private String isCoating;
	
	/** 교재 저자 */
	@Column(name = "string_cnt")
	private int count;
}
```

자 그럼 한번 실행을 해보자.    

```
Instrumental foderaBassGuitar = Instrumental.builder().id("ITEM1DT1LX")
        														  .name("Fodera Emperor 5")
        														  .price(15000000)
        														  .maker("Fodera Guitar")
        														  .type("BASS")
        														  .bodyWood("Alder")
        														  .neckWood("Maple")
        														  .fingerboardWood("Ebony")
    														  	  .stringCnt(5)
    														  	  .build();
em.persist(foderaBassGuitar);

Book rockSchoolVolOne = Book.builder().id("ITEMTD6LLX")
									  .name("Rock School Grade 5")
									  .price(35000)
									  .maker("Hal Leonard")
									  .author("Stuart Clayton")
									  .type("BOOKFORBASS")
									  .isbn("ISBN-11111111111111111")
									  .build();
em.persist(rockSchoolVolOne);

InstrumentalString dadarioString = InstrumentalString.builder().id("ITEMWDT9UT")
															   .name("Dadario XL String")
															   .price(30000)
															   .maker("Dadario")
															   .material("Nickel")
															   .isCoating("N")
															   .count(5)
															   .build();
em.persist(dadarioString);
em.flush();
em.clear();

Product product1 = em.find(Product.class, foderaBassGuitar.getId());
System.out.println(product1);

Product product2 = em.find(Product.class, rockSchoolVolOne.getId());
System.out.println(product2);

Product product3 = em.find(Product.class, dadarioString.getId());
System.out.println(product3);
```

그럼 결과는 어떻게 나올까?    

```
Hibernate: 
    
    drop table if exists book
Hibernate: 
    
    drop table if exists instrumental
Hibernate: 
    
    drop table if exists instrumental_string
Hibernate: 
    
    create table book (
       id varchar(255) not null,
        maker varchar(255),
        name varchar(255),
        price integer not null,
        book_author varchar(255),
        isbn varchar(255),
        book_type varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table instrumental (
       id varchar(255) not null,
        maker varchar(255),
        name varchar(255),
        price integer not null,
        ins_body_wood varchar(255),
        ins_finger_wood varchar(255),
        int_neck_wood varchar(255),
        ins_string_cnt integer,
        ins_type varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table instrumental_string (
       id varchar(255) not null,
        maker varchar(255),
        name varchar(255),
        price integer not null,
        string_cnt integer,
        is_coating varchar(255),
        string_material varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    /* insert io.basquiat.model.product.Instrumental
        */ insert 
        into
            instrumental
            (maker, name, price, ins_body_wood, ins_finger_wood, int_neck_wood, ins_string_cnt, ins_type, id) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.product.Book
        */ insert 
        into
            book
            (maker, name, price, book_author, isbn, book_type, id) 
        values
            (?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.model.product.InstrumentalString
        */ insert 
        into
            instrumental_string
            (maker, name, price, string_cnt, is_coating, string_material, id) 
        values
            (?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    select
        product0_.id as id1_3_0_,
        product0_.maker as maker2_3_0_,
        product0_.name as name3_3_0_,
        product0_.price as price4_3_0_,
        product0_.string_cnt as string_c1_2_0_,
        product0_.is_coating as is_coati2_2_0_,
        product0_.string_material as string_m3_2_0_,
        product0_.ins_body_wood as ins_body1_1_0_,
        product0_.ins_finger_wood as ins_fing2_1_0_,
        product0_.int_neck_wood as int_neck3_1_0_,
        product0_.ins_string_cnt as ins_stri4_1_0_,
        product0_.ins_type as ins_type5_1_0_,
        product0_.book_author as book_aut1_0_0_,
        product0_.isbn as isbn2_0_0_,
        product0_.book_type as book_typ3_0_0_,
        product0_.clazz_ as clazz_0_ 
    from
        ( select
            id,
            maker,
            name,
            price,
            string_cnt,
            is_coating,
            string_material,
            null as ins_body_wood,
            null as ins_finger_wood,
            null as int_neck_wood,
            null as ins_string_cnt,
            null as ins_type,
            null as book_author,
            null as isbn,
            null as book_type,
            1 as clazz_ 
        from
            instrumental_string 
        union
        all select
            id,
            maker,
            name,
            price,
            null as string_cnt,
            null as is_coating,
            null as string_material,
            ins_body_wood,
            ins_finger_wood,
            int_neck_wood,
            ins_string_cnt,
            ins_type,
            null as book_author,
            null as isbn,
            null as book_type,
            2 as clazz_ 
        from
            instrumental 
        union
        all select
            id,
            maker,
            name,
            price,
            null as string_cnt,
            null as is_coating,
            null as string_material,
            null as ins_body_wood,
            null as ins_finger_wood,
            null as int_neck_wood,
            null as ins_string_cnt,
            null as ins_type,
            book_author,
            isbn,
            book_type,
            3 as clazz_ 
        from
            book 
    ) product0_ 
where
    product0_.id=?
Instrumental(super=Product(id=ITEM1DT1LX, name=Fodera Emperor 5, price=15000000, maker=Fodera Guitar), stringCnt=5, type=BASS, bodyWood=Alder, neckWood=Maple, fingerboardWood=Ebony)
Hibernate: 
    select
        product0_.id as id1_3_0_,
        product0_.maker as maker2_3_0_,
        product0_.name as name3_3_0_,
        product0_.price as price4_3_0_,
        product0_.string_cnt as string_c1_2_0_,
        product0_.is_coating as is_coati2_2_0_,
        product0_.string_material as string_m3_2_0_,
        product0_.ins_body_wood as ins_body1_1_0_,
        product0_.ins_finger_wood as ins_fing2_1_0_,
        product0_.int_neck_wood as int_neck3_1_0_,
        product0_.ins_string_cnt as ins_stri4_1_0_,
        product0_.ins_type as ins_type5_1_0_,
        product0_.book_author as book_aut1_0_0_,
        product0_.isbn as isbn2_0_0_,
        product0_.book_type as book_typ3_0_0_,
        product0_.clazz_ as clazz_0_ 
    from
        ( select
            id,
            maker,
            name,
            price,
            string_cnt,
            is_coating,
            string_material,
            null as ins_body_wood,
            null as ins_finger_wood,
            null as int_neck_wood,
            null as ins_string_cnt,
            null as ins_type,
            null as book_author,
            null as isbn,
            null as book_type,
            1 as clazz_ 
        from
            instrumental_string 
        union
        all select
            id,
            maker,
            name,
            price,
            null as string_cnt,
            null as is_coating,
            null as string_material,
            ins_body_wood,
            ins_finger_wood,
            int_neck_wood,
            ins_string_cnt,
            ins_type,
            null as book_author,
            null as isbn,
            null as book_type,
            2 as clazz_ 
        from
            instrumental 
        union
        all select
            id,
            maker,
            name,
            price,
            null as string_cnt,
            null as is_coating,
            null as string_material,
            null as ins_body_wood,
            null as ins_finger_wood,
            null as int_neck_wood,
            null as ins_string_cnt,
            null as ins_type,
            book_author,
            isbn,
            book_type,
            3 as clazz_ 
        from
            book 
    ) product0_ 
where
    product0_.id=?
Book(super=Product(id=ITEMTD6LLX, name=Rock School Grade 5, price=35000, maker=Hal Leonard), author=Stuart Clayton, type=BOOKFORBASS, isbn=ISBN-11111111111111111)
Hibernate: 
    select
        product0_.id as id1_3_0_,
        product0_.maker as maker2_3_0_,
        product0_.name as name3_3_0_,
        product0_.price as price4_3_0_,
        product0_.string_cnt as string_c1_2_0_,
        product0_.is_coating as is_coati2_2_0_,
        product0_.string_material as string_m3_2_0_,
        product0_.ins_body_wood as ins_body1_1_0_,
        product0_.ins_finger_wood as ins_fing2_1_0_,
        product0_.int_neck_wood as int_neck3_1_0_,
        product0_.ins_string_cnt as ins_stri4_1_0_,
        product0_.ins_type as ins_type5_1_0_,
        product0_.book_author as book_aut1_0_0_,
        product0_.isbn as isbn2_0_0_,
        product0_.book_type as book_typ3_0_0_,
        product0_.clazz_ as clazz_0_ 
    from
        ( select
            id,
            maker,
            name,
            price,
            string_cnt,
            is_coating,
            string_material,
            null as ins_body_wood,
            null as ins_finger_wood,
            null as int_neck_wood,
            null as ins_string_cnt,
            null as ins_type,
            null as book_author,
            null as isbn,
            null as book_type,
            1 as clazz_ 
        from
            instrumental_string 
        union
        all select
            id,
            maker,
            name,
            price,
            null as string_cnt,
            null as is_coating,
            null as string_material,
            ins_body_wood,
            ins_finger_wood,
            int_neck_wood,
            ins_string_cnt,
            ins_type,
            null as book_author,
            null as isbn,
            null as book_type,
            2 as clazz_ 
        from
            instrumental 
        union
        all select
            id,
            maker,
            name,
            price,
            null as string_cnt,
            null as is_coating,
            null as string_material,
            null as ins_body_wood,
            null as ins_finger_wood,
            null as int_neck_wood,
            null as ins_string_cnt,
            null as ins_type,
            book_author,
            isbn,
            book_type,
            3 as clazz_ 
        from
            book 
    ) product0_ 
where
    product0_.id=?
InstrumentalString(super=Product(id=ITEMWDT9UT, name=Dadario XL String, price=30000, maker=Dadario), material=Nickel, isCoating=N, count=5)
```
각 테이블이 생성된 것을 볼 수 있는데 셀렉트 쿼리가 UNION ALL로 가져오는 쿼리를 볼 수 있다.     

아마도 sql을 좀 잘 아시는 분이라면 이것이 당연하다는 것을 알 수 있다.    

각각의 테이블을 UNIO ALL로 묶어서 하나의 Product로 가져오기 위한 필연의 선택이 되버린다.    

~~하...저렇게 쓰고 싶은가요?~~     

물론 장점이 없지 않을 것이다.     

각각의 상품 타입이 명확하다는 거? 하지만 조회시에 저런 식이라면 일단 성능에서부터 벌써 엄청 손해를 얻게 된다. 장점보다 단점이 더 크다.     

## @MappedSuperclass     

이것은 사실 지금까지 알아본 상속 매핑과는 좀 상관이 없다.    

신입 시절 myBatis기반의 BPM 프로젝트를 처음 시작할 때 팀장님이 erd를 가져오시더니 가장 먼저 하셨던 것은 공통된 것들을 모아둔 CommonVO를 만들고 그 안에 넣어던 것은 다음과 같다.    

1. 처음 생성된 날짜      

2. 처음 생성한 담당자     

3. 수정된 날짜     

4. 수정한 담당자     

5. 업무 형태 (task type)     

6. 부서 아이디      

7. 업무 Role type     

이런 필드를 가진 VO였다.     

물론 모든 테이블에 저런 정보를 담고 있는 것은 아니지만 BPM을 이루는 수많은 업무 액티비티의 공통된 정보였기 때문이다.     

우리가 위에서 쭈욱 공부한 것은 상속 매핑이지만 지금같은 경우에는 특정 엔티티로 작동하는 것이 아니다. 말 그대로 공통 관심사를 분리시켜 상속하기 위한 단순 객체이다.     

io.basquiat.mapsuperclazz에 있는 커스텀, 즉 핸드메이드로 만들어지는 악기과 수제 목걸이를 만드는 공방이 있다고 가정을 하자.    

1. 커스터머: 오더한 사람     

2. 오더 날짜: 주문한 날짜     

3. 완료 날짜: 만들어진 날짜     

4. 루띠어: 만든 사람     

5. 출고 일자: 작업이 완료되서 오더한 사람한테 출고된 날짜     

이런 공통적인 오더 쉬트지를 작성하게 된다.     

예제로 들기에 뭔가 좀 부족해 보이지만 @MappedSuperclass의 예제로 적합해 보여서 일단 이렇게 진행해 볼까 한다.

Common

```
package io.basquiat.model.mapsuperclazz;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@MappedSuperclass
public class Common {

	public Common(String customer, LocalDateTime orderAt, String luthiers, LocalDateTime completedAt,
			LocalDateTime deliveryAt) {
		this.customer = customer;
		this.orderAt = orderAt;
		this.luthiers = luthiers;
		this.completedAt = completedAt;
	}

	/** 커스터머 */
	private String customer;
	
	/** 오더 일자 */
	@Column(name = "order_at")
	private LocalDateTime orderAt;
	
	/** 악기 빌더 */
	private String luthiers;
	
	/** 악기 제작 완료 일자 */
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
	
	/** 악기 츨고 일자 */
	@Column(name = "delivery_at")
	private LocalDateTime deliveryAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUOorderAt() {
    	orderAt = LocalDateTime.now();
    }
    
    public void completedAt() {
    	completedAt = LocalDateTime.now();
    }
    
    public void deliveryAt() {
    	deliveryAt = LocalDateTime.now();
    }
	
}
```

BassGuitar

```
package io.basquiat.model.mapsuperclazz;

import java.time.LocalDateTime;

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
import lombok.ToString;

@Entity
@Table(name = "bass_guitar")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DynamicUpdate
@ToString(callSuper = true)
public class BassGuitar extends Common {

	@Builder
	public BassGuitar(String customer, LocalDateTime orderAt, String luthiers, LocalDateTime completedAt,
			LocalDateTime deliveryAt, String neckWood, String bodyWood, String fingerboardWood, String pickupType,
			String preamp) {
		super(customer, orderAt, luthiers, completedAt, deliveryAt);
		this.neckWood = neckWood;
		this.bodyWood = bodyWood;
		this.fingerboardWood = fingerboardWood;
		this.pickupType = pickupType;
		this.preamp = preamp;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 넥 우드 */
	@Column(name = "neck_wood")
	private String neckWood;
	
	/** 바디 우드 */
	@Column(name = "body_wood")
	private String bodyWood;
	
	/** 핑거보드 우드 */
	@Column(name = "finger_wood")
	private String fingerboardWood;
	
	/** 픽업 타입 */
	@Column(name = "pickup_type")
	private String pickupType;
	
	/** 온보드 프리앰프 이큐 */
	@Column(name = "preamp")
	private String preamp;
	
}
```
진짜 말 그대로 분리된 공통 부분만 상속한다. 그래서 부모 객체, 즉 Common은 자신은 엔티티가 아니고 단지 필드를 공통으로 사용하기 위한 클래스라는 것을 표시하는 @MappedSuperclass를 붙여준다.     

즉 이것이 붙은 녀석은 엔티티의 역할을 전혀 하지 못한다.    

자 그럼 테스트를 해볼 시간이다.    

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.mapsuperclazz.BassGuitar;

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
        	
        	BassGuitar bassGuitar = BassGuitar.builder().bodyWood("Maple")
        												.neckWood("Roasted Flame Maple")
        												.fingerboardWood("Roasted Flame Maple")
        												.pickupType("HH")
        												.preamp("Mike Pope 5Knob Preamp")
        												.customer("Basquiat87")
        												.luthiers("Vinny Fodera")
        												.build();
        	
        	em.persist(bassGuitar);
        	em.flush();
        	em.clear();
        	
        	BassGuitar completedGuitar = em.find(BassGuitar.class, 1L);
        	System.out.println(completedGuitar.toString());
        	completedGuitar.completedAt();
        	em.flush();
        	em.clear();
        	
        	BassGuitar deliveryGuitar = em.find(BassGuitar.class, 1L);
        	System.out.println(deliveryGuitar.toString());
        	deliveryGuitar.deliveryAt();
        	em.flush();
        	em.clear();
        	
        	BassGuitar selected = em.find(BassGuitar.class, 1L);
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
다음과 같이 순차적으로 처음 오더한 시간부터 기타가 완료되서 업데이트 하고 배송 준비가 되서 배송하고 나서 다시 셀렉트 이후 정보를 보여주는 단순한 테스트이다.      

```
Hibernate: 
    
    drop table if exists bass_guitar
Hibernate: 
    
    create table bass_guitar (
       id bigint not null auto_increment,
        completed_at datetime,
        customer varchar(255),
        delivery_at datetime,
        luthiers varchar(255),
        order_at datetime,
        body_wood varchar(255),
        finger_wood varchar(255),
        neck_wood varchar(255),
        pickup_type varchar(255),
        preamp varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    /* insert io.basquiat.model.mapsuperclazz.BassGuitar
        */ insert 
        into
            bass_guitar
            (completed_at, customer, delivery_at, luthiers, order_at, body_wood, finger_wood, neck_wood, pickup_type, preamp) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    select
        bassguitar0_.id as id1_0_0_,
        bassguitar0_.completed_at as complete2_0_0_,
        bassguitar0_.customer as customer3_0_0_,
        bassguitar0_.delivery_at as delivery4_0_0_,
        bassguitar0_.luthiers as luthiers5_0_0_,
        bassguitar0_.order_at as order_at6_0_0_,
        bassguitar0_.body_wood as body_woo7_0_0_,
        bassguitar0_.finger_wood as finger_w8_0_0_,
        bassguitar0_.neck_wood as neck_woo9_0_0_,
        bassguitar0_.pickup_type as pickup_10_0_0_,
        bassguitar0_.preamp as preamp11_0_0_ 
    from
        bass_guitar bassguitar0_ 
    where
        bassguitar0_.id=?
BassGuitar(super=Common(customer=Basquiat87, orderAt=2020-07-21T23:27:34, luthiers=Vinny Fodera, completedAt=null, deliveryAt=null), id=1, neckWood=Roasted Flame Maple, bodyWood=Maple, fingerboardWood=Roasted Flame Maple, pickupType=HH, preamp=Mike Pope 5Knob Preamp)
Hibernate: 
    /* update
        io.basquiat.model.mapsuperclazz.BassGuitar */ update
            bass_guitar 
        set
            completed_at=? 
        where
            id=?
Hibernate: 
    select
        bassguitar0_.id as id1_0_0_,
        bassguitar0_.completed_at as complete2_0_0_,
        bassguitar0_.customer as customer3_0_0_,
        bassguitar0_.delivery_at as delivery4_0_0_,
        bassguitar0_.luthiers as luthiers5_0_0_,
        bassguitar0_.order_at as order_at6_0_0_,
        bassguitar0_.body_wood as body_woo7_0_0_,
        bassguitar0_.finger_wood as finger_w8_0_0_,
        bassguitar0_.neck_wood as neck_woo9_0_0_,
        bassguitar0_.pickup_type as pickup_10_0_0_,
        bassguitar0_.preamp as preamp11_0_0_ 
    from
        bass_guitar bassguitar0_ 
    where
        bassguitar0_.id=?
BassGuitar(super=Common(customer=Basquiat87, orderAt=2020-07-21T23:27:34, luthiers=Vinny Fodera, completedAt=2020-07-21T23:27:34, deliveryAt=null), id=1, neckWood=Roasted Flame Maple, bodyWood=Maple, fingerboardWood=Roasted Flame Maple, pickupType=HH, preamp=Mike Pope 5Knob Preamp)
Hibernate: 
    /* update
        io.basquiat.model.mapsuperclazz.BassGuitar */ update
            bass_guitar 
        set
            delivery_at=? 
        where
            id=?
Hibernate: 
    select
        bassguitar0_.id as id1_0_0_,
        bassguitar0_.completed_at as complete2_0_0_,
        bassguitar0_.customer as customer3_0_0_,
        bassguitar0_.delivery_at as delivery4_0_0_,
        bassguitar0_.luthiers as luthiers5_0_0_,
        bassguitar0_.order_at as order_at6_0_0_,
        bassguitar0_.body_wood as body_woo7_0_0_,
        bassguitar0_.finger_wood as finger_w8_0_0_,
        bassguitar0_.neck_wood as neck_woo9_0_0_,
        bassguitar0_.pickup_type as pickup_10_0_0_,
        bassguitar0_.preamp as preamp11_0_0_ 
    from
        bass_guitar bassguitar0_ 
    where
        bassguitar0_.id=?
BassGuitar(super=Common(customer=Basquiat87, orderAt=2020-07-21T23:27:34, luthiers=Vinny Fodera, completedAt=2020-07-21T23:27:34, deliveryAt=2020-07-21T23:27:34), id=1, neckWood=Roasted Flame Maple, bodyWood=Maple, fingerboardWood=Roasted Flame Maple, pickupType=HH, preamp=Mike Pope 5Knob Preamp)
```
상속한 Common의 필드를 포함한 테이블이 생성되고 그 이후부터는 그냥 익숙한 로그들이 보인다.     

만든 김에 목걸이도 만들어 보자.    

Necklace

```
package io.basquiat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import io.basquiat.model.mapsuperclazz.BassGuitar;
import io.basquiat.model.mapsuperclazz.Necklace;

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
        	
        	Necklace necklace = Necklace.builder().material("14K Gold")
        										  .lineMaterial("14K Gold")
        										  .color("Gold")
        										  .shape("Stardust")
        										  .customer("아리")
        										  .luthiers("STONEHENGE")
        										  .build();
        	
        	em.persist(necklace);
        	em.flush();
        	em.clear();
        	
        	Necklace completedNecklace = em.find(Necklace.class, 1L);
        	System.out.println(completedNecklace.toString());
        	completedNecklace.completedAt();
        	em.flush();
        	em.clear();
        	
        	Necklace deliveryNecklace = em.find(Necklace.class, 1L);
        	System.out.println(deliveryNecklace.toString());
        	deliveryNecklace.deliveryAt();
        	em.flush();
        	em.clear();
        	
        	Necklace selected = em.find(Necklace.class, 1L);
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

실행을 해보면    


```
Hibernate: 
    
    drop table if exists bass_guitar
Hibernate: 
    
    drop table if exists necklace
Hibernate: 
    
    create table bass_guitar (
       id bigint not null auto_increment,
        completed_at datetime,
        customer varchar(255),
        delivery_at datetime,
        luthiers varchar(255),
        order_at datetime,
        body_wood varchar(255),
        finger_wood varchar(255),
        neck_wood varchar(255),
        pickup_type varchar(255),
        preamp varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    
    create table necklace (
       id bigint not null auto_increment,
        completed_at datetime,
        customer varchar(255),
        delivery_at datetime,
        luthiers varchar(255),
        order_at datetime,
        color varchar(255),
        lineMaterial varchar(255),
        material varchar(255),
        shape varchar(255),
        primary key (id)
    ) engine=InnoDB
Hibernate: 
    /* insert io.basquiat.model.mapsuperclazz.Necklace
        */ insert 
        into
            necklace
            (completed_at, customer, delivery_at, luthiers, order_at, color, lineMaterial, material, shape) 
        values
            (?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: 
    select
        necklace0_.id as id1_1_0_,
        necklace0_.completed_at as complete2_1_0_,
        necklace0_.customer as customer3_1_0_,
        necklace0_.delivery_at as delivery4_1_0_,
        necklace0_.luthiers as luthiers5_1_0_,
        necklace0_.order_at as order_at6_1_0_,
        necklace0_.color as color7_1_0_,
        necklace0_.lineMaterial as linemate8_1_0_,
        necklace0_.material as material9_1_0_,
        necklace0_.shape as shape10_1_0_ 
    from
        necklace necklace0_ 
    where
        necklace0_.id=?
Necklace(super=Common(customer=아리, orderAt=2020-07-21T23:39:30, luthiers=STONEHENGE, completedAt=null, deliveryAt=null), id=1, material=14K Gold, lineMaterial=14K Gold, color=Gold, shape=Stardust)
Hibernate: 
    /* update
        io.basquiat.model.mapsuperclazz.Necklace */ update
            necklace 
        set
            completed_at=? 
        where
            id=?
Hibernate: 
    select
        necklace0_.id as id1_1_0_,
        necklace0_.completed_at as complete2_1_0_,
        necklace0_.customer as customer3_1_0_,
        necklace0_.delivery_at as delivery4_1_0_,
        necklace0_.luthiers as luthiers5_1_0_,
        necklace0_.order_at as order_at6_1_0_,
        necklace0_.color as color7_1_0_,
        necklace0_.lineMaterial as linemate8_1_0_,
        necklace0_.material as material9_1_0_,
        necklace0_.shape as shape10_1_0_ 
    from
        necklace necklace0_ 
    where
        necklace0_.id=?
Necklace(super=Common(customer=아리, orderAt=2020-07-21T23:39:30, luthiers=STONEHENGE, completedAt=2020-07-21T23:39:30, deliveryAt=null), id=1, material=14K Gold, lineMaterial=14K Gold, color=Gold, shape=Stardust)
Hibernate: 
    /* update
        io.basquiat.model.mapsuperclazz.Necklace */ update
            necklace 
        set
            delivery_at=? 
        where
            id=?
Hibernate: 
    select
        necklace0_.id as id1_1_0_,
        necklace0_.completed_at as complete2_1_0_,
        necklace0_.customer as customer3_1_0_,
        necklace0_.delivery_at as delivery4_1_0_,
        necklace0_.luthiers as luthiers5_1_0_,
        necklace0_.order_at as order_at6_1_0_,
        necklace0_.color as color7_1_0_,
        necklace0_.lineMaterial as linemate8_1_0_,
        necklace0_.material as material9_1_0_,
        necklace0_.shape as shape10_1_0_ 
    from
        necklace necklace0_ 
    where
        necklace0_.id=?
Necklace(super=Common(customer=아리, orderAt=2020-07-21T23:39:30, luthiers=STONEHENGE, completedAt=2020-07-21T23:39:30, deliveryAt=2020-07-21T23:39:30), id=1, material=14K Gold, lineMaterial=14K Gold, color=Gold, shape=Stardust)
```

이렇게 해서 단순하게 필드만 상속하는 @MappedSuperclass도 알아보았다.    