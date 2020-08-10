package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
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
//@Entity
@Table(name = "locker")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "player")
public class Locker {

	@Builder
	public Locker(String name, String position) {
		this.name = name;
		this.position = position;
	}
	
	/** 락커 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 락커 이름 */
	private String name;

	/** 락커가 있는 위치 정보 */
	private String position;
	
	@OneToOne(mappedBy = "locker", fetch = FetchType.LAZY)
	private Player player;
	
	public void matchingPlayer(Player player) {
		this.player = player;
	}
}
