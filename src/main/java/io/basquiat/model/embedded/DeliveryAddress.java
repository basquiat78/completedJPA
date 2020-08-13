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


//@Entity
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
