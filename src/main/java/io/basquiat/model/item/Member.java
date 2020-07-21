package io.basquiat.model.item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
//@Entity
@Table(name = "basquiat_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "orders")
public class Member {

	@Builder
	public Member(String name, String email, String phone, String address) {
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.address = address;
	}
	
	/** 고객 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 고객 명 */
	@Column(name = "mb_name")
	private String name;
	
	/** 고객 이메일 */
	@Column(name = "mb_email")
	private String email;
	
	/** 고객 폰번호 */
	@Column(name = "mb_phone")
	private String phone;
	
	/** 고객 주소 */
	@Column(name = "mb_address")
	private String address;
	
	/** 가입일 */
	@Column(name = "signup_at")
	private LocalDateTime signupAt;
	
	@OneToMany(mappedBy = "member")
	private List<Order> orders = new ArrayList<>();
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpSignupAt() {
    	signupAt = LocalDateTime.now();
    }
	
}
