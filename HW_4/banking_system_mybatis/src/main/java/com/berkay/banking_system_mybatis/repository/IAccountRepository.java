package com.berkay.banking_system_mybatis.repository;



import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;
import com.berkay.banking_system_mybatis.models.Account;



@Mapper
public interface IAccountRepository {

	public boolean createRecord(Account account);

	
	public boolean updateRecord(Account account);

	public Account findById(int id);
	
	public boolean deleteById(HashMap<String, Object> map);
}
