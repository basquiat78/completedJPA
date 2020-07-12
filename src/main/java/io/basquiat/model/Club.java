package io.basquiat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "club")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Club {

	@Builder
	public Club(String name, int ranking) {
		this.name = name;
		this.ranking = ranking;
	}

	/** 클럽 아이디 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/** 클럽 명 */
	private String name;
	
	/** 클럽 랭킹 순위 */
	private int ranking;
	
}
