package com.bankcli.service;

import com.bankcli.domain.Account;
import java.math.BigDecimal;

public interface AccountService {
	void getAccount(Account account);
	void getAccountStatus();
	void makeDeposit(BigDecimal amount);
	void makeWithdrawal(BigDecimal amount);
	void createAccount(String pin);
	void makeTransfer(int toAccountId, BigDecimal amount);
	void logOut();
	void getTransactions();
}
