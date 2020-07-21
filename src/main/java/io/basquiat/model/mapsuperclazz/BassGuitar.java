package io.basquiat.model.mapsuperclazz;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "bass_guitar")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DynamicUpdate
@ToString(callSuper = true)
public class BassGuitar extends Common {

	@Builder
	public BassGuitar(String customer, LocalDateTime orderAt, String luthiers, LocalDateTime completedAt,
			LocalDateTime deliveryAt, String neckWood, String bodyWood, String fingerboardWood, String pickupType,
			String preamp) {
		super(customer, orderAt, luthiers, completedAt, deliveryAt);
		this.neckWood = neckWood;
		this.bodyWood = bodyWood;
		this.fingerboardWood = fingerboardWood;
		this.pickupType = pickupType;
		this.preamp = preamp;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 넥 우드 */
	@Column(name = "neck_wood")
	private String neckWood;
	
	/** 바디 우드 */
	@Column(name = "body_wood")
	private String bodyWood;
	
	/** 핑거보드 우드 */
	@Column(name = "finger_wood")
	private String fingerboardWood;
	
	/** 픽업 타입 */
	@Column(name = "pickup_type")
	private String pickupType;
	
	/** 온보드 프리앰프 이큐 */
	@Column(name = "preamp")
	private String preamp;
	
}
