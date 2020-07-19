package io.basquiat.model.item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
@Entity
@Table(name = "basquiat_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"member", "delivery", "orderDetails"})
public class Order {

	@Builder
	public Order(OrderStatus status, Member member, Delivery delivery, List<OrderDetail> orderDetails) {
		this.status = status;
		this.member = member;
		this.delivery = delivery;
		this.orderDetails = orderDetails;
	}

	/** 주문 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 주문 상태 */
	@Enumerated(EnumType.STRING)
	private OrderStatus status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "delivery_id")
	private Delivery delivery;
	
	/** OrderDetail과 매핑을 한다. 이 때 주인은 나의 pk를 관리하는 대상이 OrderDetail이기 때문에 주인이라는 것을 명시한다. */
	@OneToMany(mappedBy = "order")
	private List<OrderDetail> orderDetails = new ArrayList<>();
	
	/** 주문 일자 */
	@Column(name = "order_at")
	private LocalDateTime orderAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpSignupAt() {
    	orderAt = LocalDateTime.now();
    }
	
}
