package com.bankcli.service;

import com.bankcli.domain.Account;
import com.bankcli.domain.Transaction;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
	Account getAccount(Account account);
	Account getAccountStatus();
	Account makeDeposit(BigDecimal amount);
	Account makeWithdrawal(BigDecimal amount);
	Account createAccount(String pin);
	Account makeTransfer(int toAccountId, BigDecimal amount);
	void logOut();
	List<Transaction> getTransactions();
}
