package com.groupware.note.message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.groupware.note.user.Users;

public interface MessageRepository extends JpaRepository<Messages, Long> {

	List<Messages> findBySender(Users users);


	
	//1. 내가 있는 방을 찾고, 그 방에 있는 데이터 중 sender가 내가 아닌 메세지를 찾아야 함.
	List<Messages> findBySenderAndSeenByR (Users sender, int seenByR);
	
	//2. 해당 방에서 sender가 내가 아닌 메세지를 찾아야 함.
	@Query("select m from Messages m where m.chatRoom = :chatRoom and m.sender <> :sender and m.seenByR = 0")
	List<Messages> findByNotSender(@Param("chatRoom") ChatRooms chatRoom, @Param("sender") Users sender);
	

}
