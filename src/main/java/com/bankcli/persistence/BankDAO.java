package com.bankcli.persistence;

import com.bankcli.domain.Account;
import com.bankcli.domain.Transaction;
import java.math.BigDecimal;
import java.util.List;

public interface BankDAO {
	Account createAccount(String pin);
	Account getAccountByLogIn(Account account);
	void processTransaction(Account account, BigDecimal amount, Account toAccount, String type);
	BigDecimal getAccountBalanceByID(int id);
	List<Transaction> getAllTransactionsByAccountId(Account account);
}
