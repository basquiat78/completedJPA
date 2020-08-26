package io.basquiat.jpql;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class MemberDTO {

	private String id;
	
	private String name;
	
	private long age;
	
	//private Address address;
	
	private String city;

	private String street;
	
	private String zipcode;
}
