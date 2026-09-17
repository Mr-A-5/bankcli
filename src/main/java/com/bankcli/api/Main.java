package com.bankcli.api;

import com.bankcli.persistence.BankDAO;
import com.bankcli.persistence.BankDAOImpl;
import com.bankcli.persistence.LogDAO;
import com.bankcli.persistence.LogDAOImpl;
import com.bankcli.service.AccountService;
import com.bankcli.service.AccountServiceImpl;
import java.sql.*;

public class Main {

	public static void main(String[] args) throws SQLException {
		// Create instance of object that allows writing to logging file
		LogDAO log = new LogDAOImpl("log.txt");
		// Create instance of object that allows access to database
		BankDAO acc = new BankDAOImpl();
		// Pass Data Access Object to Service Layer that Controls how the data will be accessed
		AccountService service = new AccountServiceImpl(acc, log);
		// Pass service layer to API layer
		new BankRepl(service).run();
	}
}
