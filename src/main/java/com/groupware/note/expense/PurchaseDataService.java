package com.groupware.note.expense;

import java.time.LocalDate;
import static java.time.temporal.TemporalAdjusters.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import com.groupware.note.department.DepartmentRepository;
import com.groupware.note.department.Departments;
import com.groupware.note.user.UserDetails;
import com.groupware.note.user.UserDetailsService;
import com.groupware.note.user.UserRepository;
import com.groupware.note.user.Users;
import com.groupware.note.welfaremall.Purchase;
import com.groupware.note.welfaremall.PurchaseRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class PurchaseDataService {
	private final PurchaseRepository pRepo;
	private final UserDetailsService udService;
	private final UserRepository uRepo;
	private final DepartmentRepository dRepo;
	
	@Getter
	@Setter
	public class PData{
		private UserDetails userDetail;
		private Departments dep;
		
		private int year;
		private int month; 
		private Integer totalPrice;
	}
	
	public List<PData> fpcList(String purchaseType){
		List<PData> list = new ArrayList<>();
		if(purchaseType.equals("group")) {
			List<Departments> depList = this.dRepo.findAll();
			for(Departments d : depList) {
				List<Purchase> pList = this.pRepo.findByPurchaseStatusAndPurchaseTypeAndDepartment("complete", purchaseType, d);
				for(int i=2010; i<=LocalDateTime.now().getYear(); i++) {
				for(int j=1; j<=12; j++) {
					Integer price = 0; 
					for(Purchase p : pList) {
						if(p.getPurchaseDate().getMonthValue()==j && p.getPurchaseDate().getYear()==i) {
							price = price + p.getTotalPrice();
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
			return list;
		} else {
			List<Users> userList = this.uRepo.findAll();
			for(Users u : userList) {
				List<Purchase> pList = this.pRepo.findByPurchaseStatusAndPurchaseTypeAndUser("complete", "personal", u);
				for(int i=2010; i<=LocalDateTime.now().getYear(); i++) {
					for(int j=1; j<=12; j++) {
						Integer price = 0; 
						for(Purchase p : pList) {
							if(p.getPurchaseDate().getMonthValue()==j && p.getPurchaseDate().getYear()==i) {
								price = price + p.getTotalPrice();
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
			return list;
		}
	}
	
	
	
	public List<Purchase> findPurchaseList( int year, int month, int id, String purchaseType){
		List<Purchase> list = new ArrayList<>();
		LocalDate baseDate = LocalDate.of(year, month, 15);
		LocalDateTime startDate = baseDate.with(firstDayOfMonth()).atStartOfDay(); // 00:00:00.00000000
	    LocalDateTime endDate = baseDate.with(lastDayOfMonth()).atTime(LocalTime.MAX); // 23:59:59.999999
		
	    if(purchaseType.equals("personal")) {
	    	Users user = this.uRepo.findById(id).get();
	    	list = this.pRepo.findByPurchaseStatusAndPurchaseTypeAndUserAndPurchaseDateBetween("complete", purchaseType, user, startDate, endDate);
	    } else if(purchaseType.equals("group")) {
	    	Departments dep = this.dRepo.findById(id).get();
	    	list = this.pRepo.findByPurchaseStatusAndPurchaseTypeAndDepartmentAndPurchaseDateBetween("complete", purchaseType, dep, startDate, endDate);
	    } 
		return list;
	}

}