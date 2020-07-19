package io.basquiat.model.item;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "basquiat_delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "order")
public class Delivery {

	@Builder
	public Delivery(String courier, DeliveryStatus status, String place, Order order) {
		this.courier = courier;
		this.status = status;
		this.place = place;
		this.order = order;
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
	
	/** 배송지 */
	private String place;

	@OneToOne(mappedBy = "delivery")
	private Order order;
	
	/** 배송 시작일 */
	@Column(name = "delivery_at")
	private LocalDateTime deliveryAt;
	
	/** 배송 상태가 변경될 때마다 업데이트 */
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpDeliveryAt() {
    	deliveryAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdate() {
    	completedAt = LocalDateTime.now();
    }
	
	
}
