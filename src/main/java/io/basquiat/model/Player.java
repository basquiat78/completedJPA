package io.basquiat.model;

import javax.persistence.Entity;
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
import lombok.ToString;

/**
 * 
 * created by basquiat
 *
 */
@Entity
@Table(name = "player")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Player {

	@Builder
	public Player(String name, int age, String position, Club club, Locker locker) {
		this.name = name;
		this.age = age;
		this.position = position;
		this.club = club;
		this.locker = locker;
	}

	/** 선수 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 선수 명 */
	private String name;
	
	/** 선수 나이 */
	private int age;

	/** 선수 포지션 */
	private String position;
	
	@ManyToOne
	@JoinColumn(name = "club_id")
	private Club club;
	
	@OneToOne
	@JoinColumn(name = "locker_id")
	private Locker locker;
	
}
