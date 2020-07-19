package io.basquiat.model.item;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_order_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"item", "order"})
public class OrderDetail {

	/** 주문 번호 생성 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 주문한 상품 */
	@ManyToOne
	@JoinColumn(name = "item_id")
	private Item item;
	
	/** 내가 속해 있는 Order정보 */
	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;
	
	/** 주문한 상품의 수량 */
	private int quantity;
	
	/** 주문한 상품의 전체 가격 */
	private int price;
	
	/** 주문한 상품의 옵션 아이디 */
	@Column(name = "io_id")
	private int optionId;
	
	/** 주문한 상품의 옵션명 */
	@Column(name = "io_name")
	private String optionName;
	
}
