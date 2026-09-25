package com.bankcli.api;

import com.bankcli.persistence.BankDAO;
import com.bankcli.persistence.BankDAOImpl;
import com.bankcli.service.AccountService;
import com.bankcli.service.AccountServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
			// Create instance of object that allows access to database
			BankDAO acc = new BankDAOImpl();
			// Pass Data Access Object to Service Layer that Controls how the data will be accessed
			AccountService service = new AccountServiceImpl(acc);
			// Pass service layer to API layer
			new BankRepl(service).run();
		} catch (IllegalStateException e) {
			logger.error("Database unavailable at startup"); // technical detail goes to the log only
			System.out.println("Service unavailable. Please try again later.");
		}
	}
}
