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

