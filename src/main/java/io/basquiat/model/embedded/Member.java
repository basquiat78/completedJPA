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

//@Entity(name = "embedded_member")
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
