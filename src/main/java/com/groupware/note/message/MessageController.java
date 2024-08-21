package com.groupware.note.message;

import java.security.Principal;
import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.groupware.note.user.UserDetails;
import com.groupware.note.user.UserDetailsService;
import com.groupware.note.user.UserService;
import com.groupware.note.user.Users;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MessageController {
	private final MessageService mService;
	private final UserService uService;
	private final UserDetailsService udService;
	
	//대화방에 사용자 리스트 출력
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/message/list")
	public String get(Model model, Principal principal) {
		List<UserDetails> nameList = this.udService.userfindByAll();
		UserDetails _acessUser = this.udService.findByUser(this.uService.getUser(principal.getName()));
		nameList.remove(_acessUser);
		
		List<Users> userList = this.uService.getAllUsers();
		Users accessUser = this.uService.getUser(principal.getName());
		userList.remove(accessUser);
		
		model.addAttribute("userList", userList);
		model.addAttribute("nameList", nameList);
		return "message";
	}
	//대화내용을 saveMessage로 보내 저장한다
	@MessageMapping("/message.sendMessage")
	@SendTo("/topic/messages")
	public Messages sendMessage(Messages message, Principal principal) {
		Users sender = this.uService.getUser(principal.getName());
		Users reseiver = this.uService.getUser(message.getChatRoom().getUser2().getUsername());
		String content = message.getContent();
		return this.mService.saveMessage(sender, reseiver, content);
	}
	
	//기존 대화방 대화내용 불러오기
	@GetMapping("/message/messages/{username2}")
	@ResponseBody
	public List<Messages> getMessage(@PathVariable("username2") String username2, Principal principal){
		return this.mService.getMessagesBetweenUsers(principal.getName(), username2);
	}
	

	
}
