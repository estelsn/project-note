package com.groupware.note.leave;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveService {
	
	private final LeaveRepository leaveRepository;

}
