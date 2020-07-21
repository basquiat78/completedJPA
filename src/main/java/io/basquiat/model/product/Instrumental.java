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
