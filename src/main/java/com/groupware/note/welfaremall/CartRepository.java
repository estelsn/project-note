package com.groupware.note.welfaremall;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.groupware.note.user.Users;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
	List<Cart> findByUserAndType(Users user , String type);
	Optional<Cart> findByProduct(WelfareMall product);
	Optional<Cart> findByUserAndProduct(Users user , WelfareMall product);
}