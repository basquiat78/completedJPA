package io.basquiat.model.mapsuperclazz;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@MappedSuperclass
public class Common {

	public Common(String customer, LocalDateTime orderAt, String luthiers, LocalDateTime completedAt,
			LocalDateTime deliveryAt) {
		this.customer = customer;
		this.orderAt = orderAt;
		this.luthiers = luthiers;
		this.completedAt = completedAt;
	}

	/** 커스터머 */
	private String customer;
	
	/** 오더 일자 */
	@Column(name = "order_at")
	private LocalDateTime orderAt;
	
	/** 악기 빌더 */
	private String luthiers;
	
	/** 악기 제작 완료 일자 */
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
	
	/** 악기 츨고 일자 */
	@Column(name = "delivery_at")
	private LocalDateTime deliveryAt;
	
	/** insert할때 현재 시간으로 인서트한다. */
    @PrePersist
    protected void setUOorderAt() {
    	orderAt = LocalDateTime.now();
    }
    
    public void completedAt() {
    	completedAt = LocalDateTime.now();
    }
    
    public void deliveryAt() {
    	deliveryAt = LocalDateTime.now();
    }
	
}
