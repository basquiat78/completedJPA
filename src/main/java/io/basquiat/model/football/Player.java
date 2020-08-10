package io.basquiat.model.football;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
//@Entity
@Table(name = "player")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = { "footballClub", "locker" })
public class Player {

	@Builder
	public Player(String name, int age, String position, Club footballClub, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.footballClub = footballClub;
		this.locker = locker;
		locker.matchingPlayer(this);
	}

	/** 선수 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 선수 명 */
	private String name;
	
	/** 선수 나이 */
	@Setter
	private int age;

	/** 선수 포지션 */
	private String position;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club footballClub;
	
	@OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "locker_id")
	private Locker locker;

	/** 클럽에 소속되다 */
	public void entryClub(Club club) {
		this.footballClub = club;
	}
}
