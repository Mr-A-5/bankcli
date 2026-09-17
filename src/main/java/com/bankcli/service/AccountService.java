package com.bankcli.service;

import com.bankcli.domain.Account;

public interface AccountService {
	void getAccount(Account account);
	void getAccountStatus();
	void makeDeposit(double amount);
	void makeWithdrawal(double amount);
	void createAccount(String pin);
	void makeTransfer(int toAccountId, double amount);
	void logOut();
	void getAllLogs();
	void getTransactions();
}
