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

//@Entity
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
