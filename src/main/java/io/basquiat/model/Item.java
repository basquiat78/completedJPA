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
@Table(name = "basquiat_item")
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
