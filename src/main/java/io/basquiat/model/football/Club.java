package io.basquiat.model.football;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
@Table(name = "club")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "players")
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
	
	@OneToMany(mappedBy = "footballClub", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Player> players = new ArrayList<>();
	
	/** 선수를 영입하다 */
	public void scoutPlayer(Player player) {
		player.entryClub(this);
		this.getPlayers().add(player);
	}
}
