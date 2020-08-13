package io.basquiat.model.embedded;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.basquiat.model.item.DeliveryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

//@Entity(name = "embedded_delivery")
@Table(name = "delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Delivery {

	@Builder
	public Delivery(String courier, DeliveryStatus status, Address address) {
		super();
		this.courier = courier;
		this.status = status;
		this.address = address;
	}
	
	/** 배송 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	/** 택배사 코드 */
	private String courier;
	
	/** 배송 상태 */
	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;
	
	/** 배송지 주소 */
	@Embedded
	private Address address;
	
}
