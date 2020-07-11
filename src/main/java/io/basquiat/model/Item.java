package io.basquiat.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "basquiat_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@TableGenerator(
		 name = "seq_generator",
		 table = "sequence_table",
		 pkColumnValue = "item_seq", 
		 initialValue = 0,
		 allocationSize = 1) 
@ToString
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_generator")
	private Long id;

	@Setter
	@Getter
	@Column(name = "it_name")
	private String name;

	@Setter
	@Getter
	@Column(name = "it_price")
	private Integer price;

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
	
}
