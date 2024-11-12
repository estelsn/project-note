package com.groupware.note.expense;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.groupware.note.department.DepartmentRepository;
import com.groupware.note.department.Departments;
import com.groupware.note.user.UserDetails;
import com.groupware.note.user.UserDetailsRepository;
import com.groupware.note.user.UserDetailsService;
import com.groupware.note.user.UserRepository;
import com.groupware.note.user.Users;
import com.groupware.note.welfaremall.Cart;
import com.groupware.note.welfaremall.CartRepository;
import static java.time.temporal.TemporalAdjusters.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class PurchaseDataService {
	private final UserDetailsService udService;
	private final UserRepository uRepo;
	private final UserDetailsRepository udRepo;
	private final DepartmentRepository dRepo;
	private final CartRepository cRepo;
	
	@Getter
	@Setter
	public class PData{
		private UserDetails userDetail;
		private Departments dep;
		private int year;
		private int month; 
		private Integer totalPrice;
	}
	
	//구매내역 findPurchaseList 제일 기본. 부서/개인별
	public List<PData> fpcList(String type){
		List<PData> list = new ArrayList<>();
		
		if(type.equals("group")) {	
			List<Cart> cList = this.cRepo.findByStatusAndType("complete", type);
			List<Departments> _dList = this.cRepo.findDepByStatusAndType("complete", type);
			List<Departments> dList = new ArrayList<>();
			for(Departments d : _dList) {
			   if(!dList.contains(d)) {
			      dList.add(d);
			   } 
			}
			
			cList.sort((o1, o2)-> o1.getAddDate().compareTo(o2.getAddDate()));
			for(int i=LocalDateTime.now().getYear(); i>=2010; i--) {
			for(int j=12; j>=1; j--) {
				for(Departments d : dList) {
					int price = 0;
					for(Cart c : cList) {
						if(d.equals(c.getUser().getPosition().getDepartment()) && c.getAddDate().getYear()==i && c.getAddDate().getMonthValue()==j) {
							price = price + (c.getPoint()*c.getQuantity() );
						}
					}
					if(price!=0) {
						PData pd = new PData();
						pd.setDep(d);
						pd.setTotalPrice(price);
						pd.setYear(i);
						pd.setMonth(j);
						list.add(pd);
					}
				}
			}
			}
//			for(int i=LocalDateTime.now().getYear(); i>=2020; i--) {
//			for(int j=12; j>=1; j--) {
//				LocalDate baseDate = LocalDate.of(i	, j, 15);
//				LocalDateTime startDate = baseDate.with(firstDayOfMonth()).atStartOfDay(); // 00:00:00.00000000
//			    LocalDateTime endDate = baseDate.with(lastDayOfMonth()).atTime(LocalTime.MAX); // 23:59:59.999999
//			    
//			    
//			    for(Departments d : depList) {
//					List<Cart> cList = this.cRepo.findByStatusAndTypeAndDepAndAddDateBetween("complete", type, d, startDate, endDate);
//					Integer price = 0;
//					for(Cart c : cList) {
//						if(c.getAddDate().getMonthValue()==j && c.getAddDate().getYear()==i) {
//							price = price + c.getPoint();
//						}
//					}
//					if(price!=0) {
//						PData pd = new PData();
//						pd.setDep(d);
//						pd.setTotalPrice(price);
//						pd.setYear(i);
//						pd.setMonth(j);
//						list.add(pd);
//					}
//			    }
//			}	
//			}
			
			return list;
		} else {
			
		List<Cart> cList = this.cRepo.findByStatusAndType("complete", type);
		List<Users> _cuList = this.cRepo.findUserByStatusAndType("complete", type);
		List<Users> cuList = new ArrayList<>();
		for(Users u : _cuList) {
			if(!cuList.contains(u)) {
				cuList.add(u);
			}
		}
		
		cList.sort((o1, o2) -> o1.getAddDate().compareTo(o2.getAddDate()));
			for(int i=LocalDateTime.now().getYear(); i>=2010; i--) {
			for(int j=12; j>=1; j--) {
				for(Users u : cuList) {
					int price = 0;
					for(Cart c : cList) {
						if(u.equals(c.getUser()) && c.getAddDate().getYear()==i && c.getAddDate().getMonthValue()==j) {
							price = price + (c.getPoint()*c.getQuantity());
						}
					}
					if(price!=0) {
						PData pd = new PData();
						pd.setUserDetail(this.udService.findByUser(u));
						pd.setTotalPrice(price);
						pd.setYear(i);
						pd.setMonth(j);
						list.add(pd);
					}
				}
			}	
			}
			
			
//			List<Users> userList = this.uRepo.findAll();
//			for(int i=LocalDateTime.now().getYear(); i>=2010; i--) {
//			for(int j=12; j>=1; j--) {
//				LocalDate baseDate = LocalDate.of(i	, j, 15);
//				LocalDateTime startDate = baseDate.with(firstDayOfMonth()).atStartOfDay(); // 00:00:00.00000000
//			    LocalDateTime endDate = baseDate.with(lastDayOfMonth()).atTime(LocalTime.MAX); // 23:59:59.999999
//				
//				
//				for(Users u : userList) {
//					List<Cart> cList = this.cRepo.findByStatusAndTypeAndUserAndAddDateBetween("complete", type, u, startDate, endDate );
//					Integer price = 0;
//			    
//					for(Cart c : cList) {
//						if(c.getAddDate().getMonthValue()==j && c.getAddDate().getYear()==i) {
//							price = price + c.getPoint();
//						}
//					}
//					if(price!=0) {
//						PData pd = new PData();
//						pd.setUserDetail(this.udService.findByUser(u));
//						pd.setTotalPrice(price);
//						pd.setYear(i);
//						pd.setMonth(j);
//						list.add(pd);
//					}
//				}
//			}	
//			}
			return list;
		}
	}
	
	//기본 리스트 가져다가 해당 월에 올라온 데이터만 추출
	public List<PData> findByDateList(List<PData> list, int year, int month){
		List<PData> dateList = new ArrayList<>();
		for(PData p : list) {
			if(p.getYear()==year && p.getMonth()==month) {
				dateList.add(p);
			}
		}
		return dateList;
	}
	//기본 리스트 가져다가 해당 아이디 별로(dep, personal) 추출
	public List<PData> findByIdList(List<PData> list, int id, String pt){
		List<PData> idList = new ArrayList<>();
		if(pt.equals("group")) {
			Departments d = this.dRepo.findById(id).get();
			for(PData p : list) {
				if(p.getDep().equals(d)) {
					idList.add(p);
				}
			}
			
		} else {
			UserDetails u = this.udRepo.findById(id).get();
			for(PData p : list) {
				if(p.getUserDetail().equals(u)) {
					idList.add(p);
				}
			}
		}
		
		return idList;
	}
	
	
	//해당 월의 사원/부서 구매목록 전체
	public List<Cart> findPurchaseDetailList(Model model, int year, int month, int id, String purchaseType) {
		List<Cart> list = new ArrayList<>();
		LocalDate baseDate = LocalDate.of(year, month, 15);
		LocalDateTime startDate = baseDate.with(firstDayOfMonth()).atStartOfDay(); // 00:00:00.00000000
	    LocalDateTime endDate = baseDate.with(lastDayOfMonth()).atTime(LocalTime.MAX); // 23:59:59.999999
	    String name = "오류";
	    if(purchaseType.equals("personal")) {
	    	Users user = this.uRepo.findById(id).get();
	    	 name= this.udRepo.findById(id).get().getName();
	    	list = this.cRepo.findByStatusAndTypeAndUserAndAddDateBetween("complete", purchaseType, user, startDate, endDate);
	    	String details = user.getPosition().getPositionName() + "/" +user.getPosition().getDepartment().getDepartmentName();
	    	model.addAttribute("details", details);
	    	
	    } else if(purchaseType.equals("group")) {
	    	Departments dep = this.dRepo.findById(id).get();
	    	name = dep.getDepartmentName();
	    	list = this.cRepo.findByStatusAndTypeAndDepAndDateBetOrderBydate("complete", purchaseType, dep, startDate, endDate);
	    } 
	    model.addAttribute("titleName", name);
		return list;
	}
}