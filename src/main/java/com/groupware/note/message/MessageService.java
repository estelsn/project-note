package com.groupware.note.message;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.groupware.note.department.DepartmentRepository;
import com.groupware.note.department.Departments;
import com.groupware.note.user.UserDetails;
import com.groupware.note.user.UserDetailsRepository;
import com.groupware.note.user.UserRepository;
import com.groupware.note.user.UserService;
import com.groupware.note.user.Users;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class MessageService {
	private final MessageRepository mRepo;
	private final ChatRoomRepository crRepo;
	private final UserService uService;
	private final DepartmentRepository dRepo;
	private final UserDetailsRepository udRepo;
	private final UserRepository uRepo;
	
	@Getter
	@Setter
	public class UserListForDep {
		private Departments dep;
		private List<UserDetails> udList;
		
	}
	
	//사용자 리스트 출력
	public List<UserListForDep> messageListForDep(Model model, Principal principal) {
		List<UserListForDep> userListForDep = new ArrayList<>();
		for(Departments d : this.dRepo.findAll()) {
			List<UserDetails> udList = new ArrayList<>();
			udList = this.udRepo.findByDepOrderByPositionAndName(d);
			UserDetails acessUser = this.udRepo.findByUser(this.uRepo.findByUsername(principal.getName()).get()).get();
			model.addAttribute("sessionName", acessUser);
			if(acessUser.getUser().getPosition().getDepartment().equals(d)) {
				udList.remove(acessUser);
			}
			UserListForDep ulfd = new UserListForDep();
			ulfd.setDep(d);
			ulfd.setUdList(udList);
			userListForDep.add(ulfd);
		}
		return userListForDep;
	}
	
	
	//대화방 생성
	private ChatRooms createChatRoom(Users user1, Users user2) {
		ChatRooms chatRoom = new ChatRooms();
		chatRoom.setUser1(user1);
		chatRoom.setUser2(user2);
		return this.crRepo.save(chatRoom);
	}
	
	//대화 읽음 표시
	public void readMessage(Messages message) {
		this.mRepo.save(message);
	}
	
	//대화내용 저장
	public Messages saveMessage(Users sender, Users receiver, String content) {
		Messages message = new Messages();
		message.setContent(content);
		message.setSendTime(LocalDateTime.now());
		message.setSeenByR(0);
		Optional<ChatRooms> _chatRoom1 = this.crRepo.findByUser1AndUser2(sender, receiver);
		if(_chatRoom1.isPresent()) {
			ChatRooms chatRoom = _chatRoom1.get();
			message.setSender(sender);
			message.setChatRoom(chatRoom);
			return this.mRepo.save(message);
		} else {
			Optional<ChatRooms> _chatRoom2 = this.crRepo.findByUser1AndUser2(receiver, sender);
			if(_chatRoom2.isPresent()) {
				ChatRooms chatRoom = _chatRoom2.get();
				message.setSender(sender);
				message.setChatRoom(chatRoom);
				return this.mRepo.save(message);
			} else {
				ChatRooms chatRoom = createChatRoom(sender, receiver);
				message.setSender(sender);
				message.setChatRoom(chatRoom);
				return this.mRepo.save(message);
			} //if : _chatRoom2.isPresent
		} //if : _chatRoom1.isPresent
	}// method : saveMessage
	
	//전체 안읽은 대화 목록
	public int getAllUnreadMessage(Users user) {
		List<ChatRooms> crList = new ArrayList<>();
		crList = this.crRepo.allRoomList(user);
		int noRM = 0;
		for(ChatRooms cr : crList) {
			List<Messages> _mlist = this.mRepo.findByNotSender(cr, user);
			noRM = noRM + _mlist.size();
		}
		return noRM;
	}
	
	@Getter
	@Setter
	public class URMessageData {
		private int no;
		private String senderName;
		private String positionName;
		private String depName;
	}
	
	//안읽은 대화 사용자 목록
	public List<URMessageData> getUnreadUser(Users user) {
		List<URMessageData> urMsgDataList = new ArrayList<>();
		List<ChatRooms> crListByUser = this.crRepo.allRoomList(user);
		
		int count = 0;
		for(ChatRooms cr : crListByUser) {
			for(Messages m : cr.getMessages()) {
				if(!m.getSender().equals(user) && m.getSeenByR()==0) {
					Optional<UserDetails> _ud1 = this.udRepo.findByUser( m.getSender());
					if(_ud1.isPresent()) {
						count++;
						URMessageData _ur = new URMessageData();
						_ur.setNo(count);
						_ur.setSenderName(_ud1.get().getName());
						_ur.setPositionName(m.getSender().getPosition().getPositionName());
						_ur.setDepName(m.getSender().getPosition().getDepartment().getDepartmentName());
						urMsgDataList.add(_ur);
						break;
					}
				}
			}
		}
		return urMsgDataList;
	}
	
	//기존 대화내용 불러오기
	public List<Messages> getMessagesBetweenUsers(String username1, String username2){
		Users user1 = this.uService.getUser(username1);
		Users user2 = this.uService.getUser(username2);
		
		Optional<ChatRooms> cr1 = this.crRepo.findByUser1AndUser2(user1, user2);
		
		if(cr1.isPresent()) {
			return cr1.get().getMessages();
		} else {
			Optional<ChatRooms> cr2 = this.crRepo.findByUser1AndUser2(user2, user1);
			if(cr2.isPresent()) {
				return cr2.get().getMessages();
			} else {
				return new ArrayList<>();
			}//if : cr2.isPresent
		} //if : cr1.isPresent
	}


	

}
