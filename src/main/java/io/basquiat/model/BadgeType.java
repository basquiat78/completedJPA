package io.basquiat.model;

import java.util.Arrays;

public enum BadgeType {

	BEST("BEST", 1),
	MD("MD", 2),
	NEW("NEW", 3),
	OVERSEA("OVERSEA", 4);
	
	/** enum code */
	private String code;

	/** enum index */
	private int index;
	
	public int index() {
		return this.index;
	}
	
	public String code() {
		return this.code;
	}
	
	BadgeType(String code, int index) {
		this.index = index;
		this.code = code;
	}
	
	/**
	 * get Enum Object from dbdata
	 * @param dbData
	 * @return BadgeType
	 */
	public static BadgeType indexFromDB(int dbData) {
		return Arrays.stream(BadgeType.values())
					 .filter( badgeType -> badgeType.index() == dbData )
					 .findAny()
					 .orElseThrow(() -> new RuntimeException("없는 녀석이야!"));
    }
	
}
