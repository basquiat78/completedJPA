package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.Id;

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
@Entity(name = "basquiat_other_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class OtherItem {

	@Id
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
