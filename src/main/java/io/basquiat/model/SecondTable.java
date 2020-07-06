package io.basquiat.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//@Entity
//@Table(name = "second")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SecondTable {

	@Builder
	public SecondTable(String one, String two) {
		super();
		this.one = one;
		this.two = two;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Setter
	@Getter
	private String one;
	
	@Setter
	@Getter
	private String two;
	
}
