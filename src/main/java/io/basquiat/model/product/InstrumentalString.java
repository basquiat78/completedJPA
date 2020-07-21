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

//@Entity
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
