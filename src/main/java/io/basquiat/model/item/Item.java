package io.basquiat.model.item;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
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
@Table(name = "basquiat_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Item {
	
	@Builder
	public Item(String id, String name, String model) {
		this.id = id;
		this.name = name;
		this.model = model;
	}
	
	/** 상품 코드 */
	@Id
	private String id;
	
	/** 상품 명 */
	@Column(name = "it_name")
	private String name;

	/** 상품 모델명 */
	@Column(name = "it_model")
	private String model;
	
	/** 상품 생성일 */
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	/** 상품 수정일 */
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
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
