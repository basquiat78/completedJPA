package io.basquiat.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 
 * created by basquiat
 *
 *
 */
@Converter(autoApply = true)
public class BadgeConverter implements AttributeConverter<BadgeType, Integer> {

	/**
	 * 엔티티에서 디비로 보낼 때 BadgeType에 정의한 인덱스 정보를 가져와서 세팅한다.
	 */
	public Integer convertToDatabaseColumn(BadgeType badgeType) {
		return badgeType.index();
	}

	/**
	 * DB에 있는 인트 타입의 정보를 Enum으로 반환하는 코드이다.
	 */
	public BadgeType convertToEntityAttribute(Integer dbData) {
		return BadgeType.indexFromDB(dbData);
	}

}
