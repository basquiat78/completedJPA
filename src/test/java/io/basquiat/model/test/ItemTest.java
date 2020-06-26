package io.basquiat.model.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.basquiat.model.Item;

public class ItemTest {

	@Test
	public void itemEntityExceptionNameTest() {
		Item.builder().price(15000000).build();
	}
	
	@Test
	public void itemEntityExceptionPriceTest() {
		Item.builder().name("Fodera Emperor5 Deluxe").build();
	}
	
	@Test
	public void itemEntityTest() {
		Item item = Item.builder().name("Fodera Emperor5 Deluxe").price(15000000).build();
		assertEquals("Fodera Emperor5 Deluxe", item.getName());
		assertEquals(15000000, item.getPrice());
	}
	
}
