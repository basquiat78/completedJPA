package io.basquiat.model.product;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

//@Entity
@Table(name = "book")
@DiscriminatorValue("BOOK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public class Book extends Product {
	
	@Builder
	public Book(String id, String name, int price, String maker, String author, String type, String isbn) {
		super(id, name, price, maker);
		this.author = author;
		this.type = type;
		this.isbn = isbn;
	}

	/** 교재 저자 */
	@Column(name = "book_author")
	private String author;
	
	/** 교재 악기 타입 */
	@Column(name = "book_type")
	private String type;
	
	/** 부여된 고유 번호  */
	private String isbn;

}
