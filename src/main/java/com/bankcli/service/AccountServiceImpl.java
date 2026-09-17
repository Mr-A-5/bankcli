package com.bankcli.service;

import com.bankcli.domain.Account;
import com.bankcli.domain.Log;
import com.bankcli.domain.Transaction;
import com.bankcli.persistence.BankDAO;
import com.bankcli.persistence.LogDAO;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class AccountServiceImpl implements AccountService {

	private final BankDAO bankDAO;
	private final LogDAO logDAO;
	private Account account;

	public AccountServiceImpl(BankDAO bankDAO, LogDAO logDAO) {
		this.bankDAO = bankDAO;
		this.logDAO = logDAO;
	}

	@Override
	public void makeDeposit(double amount) {
		if (!loggedIn()) {
			logMessage(
				"A User has attempted to make a deposit without login in first. Deposit Amount: $" + amount,
				"ERROR"
			);
			return;
		} else if (amount <= 0) {
			System.out.println("Deposit amounts can only be positive integers");
			logMessage(
				"Account Id: " +
					account.getId() +
					" attempted to deposit a negative amount. Deposit Amount: $" +
					amount,
				"ERROR"
			);
		} else {
			bankDAO.processTransaction(account, amount, null, "DEPOSIT");
			account.setBalance(account.getBalance() + amount);
			logMessage("Account - " + account.getId() + " has deposited an amount of $" + amount, "INFO");
		}
	}

	@Override
	public void makeWithdrawal(double amount) {
		if (!loggedIn()) {
			logMessage(
				"A User has attempted to make a withdraw without login in first. Withdraw Amount: $" + amount,
				"ERROR"
			);
			return;
		} else if (amount <= 0) {
			System.out.println("Withdraw amounts can only be positive integers");
			logMessage(
				"Account Id: " +
					account.getId() +
					" attempted to withdraw a negative amount. Withdraw Amount: " +
					amount,
				"ERROR"
			);
		} else if (!overdraft(account, amount)) {
			bankDAO.processTransaction(account, amount, null, "WITHDRAW");
			account.setBalance(account.getBalance() - amount);
			logMessage("Account Id " + account.getId() + " has withdrawn an amount of $" + amount, "INFO");
		}
	}

	/**	This method creates an account in the
	 *
	 * @param pin - the pin that will be used to create the account
	 */
	@Override
	public void createAccount(String pin) {
		if (pin.length() < 4 || !pin.matches("[0-9]*")) {
			System.out.println("Please ensure your pin is only numbers and at least for 4 characters long");
			logMessage("User attempted to create an account with an invalid PIN. PIN: " + pin, "ERROR");
		} else {
			this.account = bankDAO.createAccount(pin);
			logMessage("User has created an account with valid credentials. Account Id: " + account.getId(), "INFO");
			System.out.print("Welcome to bank CLI: \n " + this.account.toString());
		}
	}

	@Override
	public void makeTransfer(int toAccountId, double amount) {
		if (!loggedIn()) {
			logMessage(
				"A User has attempted to make a transfer without login in first. Transfer Amount:" + amount,
				"ERROR"
			);
			return;
		} else if (amount <= 0) {
			System.out.println("Transfer amounts can only be positive integers");
			logMessage(
				"Account Id: " +
					account.getId() +
					" attempted to transfer an invalid amount. Transfer Amount " +
					amount,
				"ERROR"
			);
		} else if (account.getId() == toAccountId) {
			System.out.println("You cannot transfer money to the same account.");
			logMessage("Account Id: " + account.getId() + " attempted to transfer to its own account", "ERROR");
		} else if (!overdraft(account, amount)) {
			Double balance = bankDAO.getAccountBalanceByID(toAccountId);
			if (balance == null) {
				System.out.println("Receiving account does not exists.");
			}
			Account toAccount = new Account(toAccountId, balance);
			bankDAO.processTransaction(account, amount, toAccount, "TRANSFER");
			account.setBalance(account.getBalance() - amount);
			logMessage("Account Id " + account.getId() + " transferred $" + amount + " to " + toAccountId, "INFO");
		}
	}

	private void logMessage(String message, String type) {
		Log l = new Log(Timestamp.from(Instant.now()).toString(), type, message);
		logDAO.writeLog(l);
	}

	@Override
	public void getAccount(Account account) {
		Account acc = bankDAO.getAccountByLogIn(account);
		if (acc == null) {
			System.out.println("Log in does not have a match in the system. Please try again.");
			logMessage(
				"A user has attempt to log in using invalid credentials. Account Id: " +
					account.getId() +
					" PIN: " +
					account.getPin(),
				"ERROR"
			);
		} else {
			this.account = acc;
			logMessage("User successfully logged into their account with account id: " + account.getId(), "INFO");
			System.out.println(this.account.toString());
		}
	}

	private boolean overdraft(Account account, double amount) {
		if (account.getBalance() - amount < 0) {
			System.out.println(
				"Transaction will lead to overdraft and cannot proceed. Please try again with a different amount."
			);
			logMessage(
				"Account Id " +
					account.getId() +
					" attempted an overdraft. Balance: " +
					account.getBalance() +
					" - Amount: " +
					amount,
				"ERROR"
			);
			return true;
		}
		return false;
	}

	private boolean loggedIn() {
		if (account == null) {
			System.out.println(
				"You have not logged into your account yet, please log in or create an account to continue."
			);
			return false;
		}
		return true;
	}

	@Override
	public void logOut() {
		if (loggedIn()) {
			logMessage("Account Id " + account.getId() + " logged out from their account", "INFO");
			account = null;
		}
	}

	@Override
	public void getAccountStatus() {
		if (!loggedIn()) {
			logMessage("A user attempted to get their account status without login in first", "ERROR");
			return;
		} else {
			logMessage("Account Id: " + account.getId() + " checked their account status", "INFO");
			System.out.println(account.toString());
		}
	}

	@Override
	public void getTransactions() {
		if (!loggedIn()) {
			logMessage("A user attempted to get their account transaction without login in first", "ERROR");
			return;
		} else {
			List<Transaction> txs = bankDAO.getAllTransactionsByAccountId(account);
			System.out.printf(
				"| %-30s | %-20s | %-20s | %-20s | %-20s | %-20s |%n",
				"TimeStamp",
				"Transaction Id",
				"Type of Transaction",
				"Origin Account",
				"Amount",
				"Target Account"
			);
			for (Transaction tx : txs) {
				System.out.println(tx);
			}
			logMessage("Account Id: " + account.getId() + "checked all their transactions", "INFO");
		}
	}

	@Override
	public void getAllLogs() {
		List<Log> logs = logDAO.getAllLogs();
		logs = logs.reversed();
		System.out.printf("| %-30s | %-7s | %-90s |%n", "Timestamp", "Type", "Message");
		for (Log log : logs) {
			System.out.println(log);
		}
	}
}
