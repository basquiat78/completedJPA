package io.basquiat.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_item")
//@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Getter
	@Column(name = "it_name", columnDefinition = "varchar(20) not null")
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;

	@Column(name = "test", precision = 10, scale = 8)
	private BigDecimal test;
	
	@Setter
	@Getter
	private BadgeType badge;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	@Builder
	public Item(String name, Integer price, BadgeType badge) {
		this.price = price;
		this.name = name;
		this.badge = badge;
	}


	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUpCreatedAt() {
    	createdAt = LocalDateTime.now();
    }

    /** update 이벤트가 발생시에 업데이트된 시간으로 update */
    @PreUpdate
    protected void onUpdate() {
    	updatedAt = LocalDateTime.now();
    }

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", price=" + price + ", test=" + test + ", badge=" + badge
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
}
