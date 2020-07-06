package io.basquiat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "basquiat_item_log")
public class ItemLog {

	@Id
	private String id;

	@Lob
	private String log;
	
	@Column(name = "changer")
	private String changeUserId;
	
}
