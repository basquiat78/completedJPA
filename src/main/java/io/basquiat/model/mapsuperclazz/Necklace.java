package io.basquiat.model.mapsuperclazz;

import java.time.LocalDateTime;

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

//@Entity
@Table(name = "necklace")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DynamicUpdate
@ToString(callSuper = true)
public class Necklace extends Common {

	@Builder
	public Necklace(String customer, LocalDateTime orderAt, String luthiers, LocalDateTime completedAt,
			LocalDateTime deliveryAt, String material, String lineMaterial, String color, String shape) {
		super(customer, orderAt, luthiers, completedAt, deliveryAt);
		this.material = material;
		this.lineMaterial = lineMaterial;
		this.color = color;
		this.shape = shape;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 목걸이 메인 소재 */
	private String material;

	/** 목걸이 줄 소재 */
	private String lineMaterial;
	
	/** 색상 */
	private String color;
	
	/** 쉐입 */
	private String shape;
}
