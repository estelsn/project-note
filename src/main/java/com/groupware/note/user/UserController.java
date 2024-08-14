package com.groupware.note.user;

import java.net.MalformedURLException;
import java.security.Principal;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.groupware.note.attendance.AttendanceService;
import com.groupware.note.files.FileService;
import com.groupware.note.files.Files;
import com.groupware.note.position.PositionService;
import com.groupware.note.position.Positions;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/user")
@Controller
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	private final UserDetailsService userDetailsService;
	private final FileService fileService;
	private final PositionService positionService;
	private final AttendanceService attendanceService;


	
	
	@GetMapping("/login")
	public String login(Principal principal) { // 0809 장진수 : 로그인 상태에서도 login.html 에 들어갈 수 있길래, 구분해둠
		if(principal != null) {
			Users user = this.userService.getUser(principal.getName());
			if(!user.getStatus().equals("출근")) {
				return "attendanceButton";
			}else {				
				return "redirect:/";
			}
		}else {			
			return "login";
		}
	}
	
	@GetMapping("/regist")
	public String regist(UserCreateForm userCreateForm) {
		return "regist";
	}
	
	@PostMapping("/regist")
	public String regist(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return "regist";
		}
		if(!userCreateForm.getPassword().equals(userCreateForm.getPasswordCheck())) { //비밀번호와 비밀번호 확인이 일치하지 않으면
			bindingResult.rejectValue("passwordCheck", "passwordInCorrect", "2개의 비밀번호가 일치하지 않습니다.");
			//bindingResult.rejectValue(잘못입력 된 값(필드명), 에러코드(내가 지정함), 에러 메세지) => bindingResult에서 에러 메세지를 하나 더 추가하여 넘겨주어야 할 때
			return "regist";
		}
		try { //중복검사
			Files files = this.fileService.findByFiles(1);
			Users users = this.userService.create(userCreateForm.getUsername(), userCreateForm.getPassword());
			this.userDetailsService.create(users, userCreateForm.getName(), userCreateForm.getBirthdate(), userCreateForm.getEmail(), files);
		} catch (DataIntegrityViolationException e) { //SiteUser에서 주었던 unique 제약조건 위반시 해당 에러클래스가 처리함 
			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
			return "regist";
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", e.getMessage());
			//bindingResult.reject(에러코드, 에러메세지)
			return "regist";
		}
		
		return "login";
	}
	
	@GetMapping("/find/id")
	public String findID() {
		return "findID";
	}
	
	@PostMapping("/find/id")
	public String findID(Model model, @RequestParam("email") String email) {
		System.out.println("email"+email);
		UserDetails userDetails = this.userDetailsService.findID(email);
		Users users = userDetails.getUser();
		model.addAttribute("users", users);
		return "findID";
	}
	
	@GetMapping("/find/pw")
	public String findPW(UserPasswordForm userPasswordForm) {
		return "findPW";
	}
	
	@PostMapping("/find/pw")
	public String findPW(Model model, @RequestParam(value = "username") String username, UserPasswordForm userPasswordForm) {
		Users users = this.userService.findPW(username);
		Boolean check = this.userService.checkPW(username);
		model.addAttribute("check", check);
		model.addAttribute("users", users);
		return "findPW";
	}
	
	@PostMapping("/change/pw")
	public String changePW(@Valid UserPasswordForm userPasswordForm, BindingResult bindingResult, Model model) {
		if(bindingResult.hasErrors()) {
			Users users = this.userService.findPW(userPasswordForm.getUsername());
			Boolean check = true;
			model.addAttribute("check", check);
			model.addAttribute("users", users);
			return "findPW";
		}
		if(!userPasswordForm.getPassword().equals(userPasswordForm.getPasswordCheck())) {
			bindingResult.rejectValue("passwordCheck", "passwordInCorrect", "2개의 비밀번호가 일치하지 않습니다.");
			Users users = this.userService.findPW(userPasswordForm.getUsername());
			Boolean check = true;
			model.addAttribute("check", check);
			model.addAttribute("users", users);
			return "findPW";
		}
		this.userService.changePW(userPasswordForm.getUsername(), userPasswordForm.getPassword());
		return "redirect:/user/login";
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/photo")
	public String photoUpdate(@RequestParam(value = "multipartFiles") MultipartFile multipartFile, Principal principal) {
		try {
			Users users = this.userService.getUser(principal.getName());
			Files files = this.fileService.uploadPhoto(multipartFile);
			this.userDetailsService.uploadPhoto(users, files);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/photo/{id}")
	public ResponseEntity<Resource> photo(@PathVariable("id") Integer id) throws MalformedURLException{
		Files file = this.fileService.findByFiles(id);
		return this.fileService.photoView(file);
	}
	@GetMapping("/list")
	public String userList(Model model) {
		List<UserDetails> userList = this.userDetailsService.userfindByAll();
		model.addAttribute("userList",userList);
		
		return "HR_list";
		
	}
	@GetMapping("/detail/{userId}")
	public String getUser(Model model,@PathVariable("userId") Integer userId) {
		Users users = this.userService.getUser(userId);
		Positions positions = this.positionService.findById(userId);
		UserDetails userDetails = this.userDetailsService.getUser(userId);
		List<Attendance> attendance = this.attendanceService.findById(userId);
		model.addAttribute("users", users);
		model.addAttribute("positions",positions);
		model.addAttribute("userDetails",userDetails);
		model.addAttribute("attendance", attendance);
		System.out.println("불러와 주우우웅우우우세야야야양야ㅑㅇ");
		
		return "HR_detail";
	}
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/update/{userId}")
	public String userUpdate(Model model,@PathVariable("userId") Integer userId) {
		//UserDetails userupdate = this.userDetailsService
		//model.addAttribute("userupdate",userupdate);
		
		return "HR_update";
		
	}

}
