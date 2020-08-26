package io.basquiat.jpql;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "basquiat_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "team")
@DynamicUpdate
public class Member {
	
	@Builder
	public Member(String id, String name, int age, Address address, Team team) {
		super();
		this.id = id;
		this.name = name;
		this.age = age;
		this.address = address;
		this.team = team;
	}

	@Id
	private String id;
	
	private String name;
	
	@Setter
	private int age;

	@Embedded
	private Address address;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

}
