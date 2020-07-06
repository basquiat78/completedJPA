package io.basquiat.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//@Entity
//@Table(name = "first")
//@SecondaryTables({
//				@SecondaryTable(name = "second")
//})
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@ToString
public class FirstTable {
	
	@Builder
	public FirstTable(String one, String two) {
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
	
	@Setter
	@Getter
	@Column(name = "one", table = "second")
	private String secondOne;
	
	@Setter
	@Getter
	@Column(name = "two", table = "second")
	private String secondTwo;
}
