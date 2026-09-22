package com.bankcli.service;

import com.bankcli.domain.Account;
import com.bankcli.domain.Transaction;
import com.bankcli.persistence.BankDAO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountServiceImpl implements AccountService {

	private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

	private final BankDAO bankDAO;
	private Account account;

	public AccountServiceImpl(BankDAO bankDAO) {
		this.bankDAO = bankDAO;
	}

	@Override
	public void makeDeposit(double amount) {
		if (!loggedIn()) {
			logger.error("A User has attempted to make a deposit without login in first. Deposit Amount: ${}", amount);
			return;
		} else if (amount <= 0) {
			System.out.println("Deposit amounts can only be positive integers");
			logger.error(
				"Account Id: {} attempted to deposit a negative amount. Deposit Amount: ${}",
				account.getId(),
				amount
			);
		} else {
			bankDAO.processTransaction(account, amount, null, "DEPOSIT");
			account.setBalance(account.getBalance() + amount);
			logger.info("Account - {} has deposited an amount of ${}", account.getId(), amount);
		}
	}

	@Override
	public void makeWithdrawal(double amount) {
		if (!loggedIn()) {
			logger.error(
				"A User has attempted to make a withdraw without login in first. Withdraw Amount: ${}",
				amount
			);
			return;
		} else if (amount <= 0) {
			System.out.println("Withdraw amounts can only be positive integers");
			logger.error(
				"Account Id: {} attempted to withdraw a negative amount. Withdraw Amount: {}",
				account.getId(),
				amount
			);
		} else if (!overdraft(account, amount)) {
			bankDAO.processTransaction(account, amount, null, "WITHDRAW");
			account.setBalance(account.getBalance() - amount);
			logger.info("Account Id {} has withdrawn an amount of ${}", account.getId(), amount);
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
			logger.error("User attempted to create an account with an invalid PIN (length={})", pin.length());
		} else {
			this.account = bankDAO.createAccount(pin);
			logger.info("User has created an account with valid credentials. Account Id: {}", account.getId());
			System.out.print("Welcome to bank CLI: \n " + this.account.toString());
		}
	}

	@Override
	public void makeTransfer(int toAccountId, double amount) {
		if (!loggedIn()) {
			logger.error(
				"A User has attempted to make a transfer without login in first. Transfer Amount: {}",
				amount
			);
			return;
		} else if (amount <= 0) {
			System.out.println("Transfer amounts can only be positive integers");
			logger.error(
				"Account Id: {} attempted to transfer an invalid amount. Transfer Amount {}",
				account.getId(),
				amount
			);
		} else if (account.getId() == toAccountId) {
			System.out.println("You cannot transfer money to the same account.");
			logger.error("Account Id: {} attempted to transfer to its own account", account.getId());
		} else if (!overdraft(account, amount)) {
			Double balance = bankDAO.getAccountBalanceByID(toAccountId);
			if (balance == null) {
				System.out.println("Receiving account does not exists.");
			}
			Account toAccount = new Account(toAccountId, balance);
			bankDAO.processTransaction(account, amount, toAccount, "TRANSFER");
			account.setBalance(account.getBalance() - amount);
			logger.info("Account Id {} transferred ${} to {}", account.getId(), amount, toAccountId);
		}
	}

	@Override
	public void getAccount(Account account) {
		Account acc = bankDAO.getAccountByLogIn(account);
		if (acc == null) {
			System.out.println("Log in does not have a match in the system. Please try again.");
			logger.error("A user has attempt to log in using invalid credentials. Account Id: {}", account.getId());
		} else {
			this.account = acc;
			logger.info("User successfully logged into their account with account id: {}", account.getId());
			System.out.println(this.account.toString());
		}
	}

	private boolean overdraft(Account account, double amount) {
		if (account.getBalance() - amount < 0) {
			System.out.println(
				"Transaction will lead to overdraft and cannot proceed. Please try again with a different amount."
			);
			logger.error(
				"Account Id {} attempted an overdraft. Balance: {} - Amount: {}",
				account.getId(),
				account.getBalance(),
				amount
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
			logger.info("Account Id {} logged out from their account", account.getId());
			account = null;
		}
	}

	@Override
	public void getAccountStatus() {
		if (!loggedIn()) {
			logger.error("A user attempted to get their account status without login in first");
			return;
		} else {
			logger.info("Account Id: {} checked their account status", account.getId());
			System.out.println(account.toString());
		}
	}

	@Override
	public void getTransactions() {
		if (!loggedIn()) {
			logger.error("A user attempted to get their account transaction without login in first");
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
			logger.info("Account Id: {} checked all their transactions", account.getId());
		}
	}
}
