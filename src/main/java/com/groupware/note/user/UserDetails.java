package com.groupware.note.user;

import java.time.LocalDate;

import com.groupware.note.files.Files;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class UserDetails {
	
	@Id
	private Integer userId; //아래의 user와 같음 (기본키이면서 외래키)
	
	@OneToOne
	@MapsId
	@JoinColumn(name="user_id")
	private Users user;
	
	private String name;
	
	private LocalDate birthdate;
	
	private String email;
	
	@ManyToOne
	private Files photo;
	
	private long leave;
	
	private long points;
}
