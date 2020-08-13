package io.basquiat.jpql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "basquiat_team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Team {

	@Builder
	public Team(String teamName) {
		super();
		this.teamName = teamName;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "name")
	@Setter
	private String teamName;
	
}
