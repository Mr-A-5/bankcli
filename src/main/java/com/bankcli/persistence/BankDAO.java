package com.bankcli.persistence;

import com.bankcli.domain.Account;
import com.bankcli.domain.Transaction;
import java.util.List;

public interface BankDAO {
	Account createAccount(String pin);
	Account getAccountByLogIn(Account account);
	void processTransaction(Account account, double amount, Account toAccount, String type);
	Double getAccountBalanceByID(int id);
	List<Transaction> getAllTransactionsByAccountId(Account account);
}
