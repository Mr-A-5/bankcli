package com.bankcli.service;

import com.bankcli.domain.Account;
import com.bankcli.domain.Transaction;
import com.bankcli.persistence.BankDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
	public Account makeDeposit(BigDecimal amount) {
		if (!loggedIn()) {
			logger.error("A User has attempted to make a deposit without login in first. Deposit Amount: ${}", amount);
			throw notLoggedIn();
		}
		if (amount.signum() <= 0) {
			logger.error(
				"Account Id: {} attempted to deposit a negative amount. Deposit Amount: ${}",
				account.getId(),
				amount
			);
			throw new IllegalArgumentException("Deposit amounts can only be positive integers");
		}
		BigDecimal strippedAmount = checkDecimals(amount);
		bankDAO.processTransaction(account, strippedAmount, null, "DEPOSIT");
		account.setBalance(account.getBalance().add(strippedAmount));
		logger.info("Account - {} has deposited an amount of ${}", account.getId(), strippedAmount);
		return account;
	}

	@Override
	public Account makeWithdrawal(BigDecimal amount) {
		if (!loggedIn()) {
			logger.error(
				"A User has attempted to make a withdraw without login in first. Withdraw Amount: ${}",
				amount
			);
			throw notLoggedIn();
		}
		if (amount.signum() <= 0) {
			logger.error(
				"Account Id: {} attempted to withdraw a negative amount. Withdraw Amount: {}",
				account.getId(),
				amount
			);
			throw new IllegalArgumentException("Withdraw amounts can only be positive integers");
		}
		BigDecimal strippedAmount = checkDecimals(amount);
		checkOverdraft(account, strippedAmount);
		bankDAO.processTransaction(account, strippedAmount, null, "WITHDRAW");
		account.setBalance(account.getBalance().subtract(strippedAmount));
		logger.info("Account Id {} has withdrawn an amount of ${}", account.getId(), strippedAmount);
		return account;
	}

	/**	This method creates an account in the
	 *
	 * @param pin - the pin that will be used to create the account
	 */
	@Override
	public Account createAccount(String pin) {
		if (pin.length() < 4 || !pin.matches("[0-9]*")) {
			logger.error("User attempted to create an account with an invalid PIN (length={})", pin.length());
			throw new IllegalArgumentException(
				"Please ensure your pin is only numbers and at least for 4 characters long"
			);
		}
		this.account = bankDAO.createAccount(pin);
		logger.info("User has created an account with valid credentials. Account Id: {}", account.getId());
		return account;
	}

	@Override
	public Account makeTransfer(int toAccountId, BigDecimal amount) {
		if (!loggedIn()) {
			logger.error("A User has attempted to make a transfer without login in first. Transfer Amount: {}", amount);
			throw notLoggedIn();
		}
		if (amount.signum() <= 0) {
			logger.error(
				"Account Id: {} attempted to transfer an invalid amount. Transfer Amount {}",
				account.getId(),
				amount
			);
			throw new IllegalArgumentException("Transfer amounts can only be positive integers");
		}
		if (account.getId() == toAccountId) {
			logger.error("Account Id: {} attempted to transfer to its own account", account.getId());
			throw new IllegalArgumentException("You cannot transfer money to the same account.");
		}
		BigDecimal strippedAmount = checkDecimals(amount);
		checkOverdraft(account, strippedAmount);
		BigDecimal balance = bankDAO.getAccountBalanceByID(toAccountId);
		if (balance == null) {
			throw new IllegalArgumentException("Receiving account does not exist.");
		}
		Account toAccount = new Account(toAccountId, balance);
		bankDAO.processTransaction(account, strippedAmount, toAccount, "TRANSFER");
		account.setBalance(account.getBalance().subtract(strippedAmount));
		logger.info("Account Id {} transferred ${} to {}", account.getId(), strippedAmount, toAccountId);
		return account;
	}

	@Override
	public Account getAccount(Account account) {
		Account acc = bankDAO.getAccountByLogIn(account);
		if (acc == null) {
			logger.error("A user has attempt to log in using invalid credentials. Account Id: {}", account.getId());
			throw new IllegalArgumentException("Log in does not have a match in the system. Please try again.");
		}
		this.account = acc;
		logger.info("User successfully logged into their account with account id: {}", account.getId());
		return this.account;
	}

	private BigDecimal checkDecimals(BigDecimal amount) {
		if (amount.scale() > 2) {
			if (amount.scale() > 10) {
				logger.error(
					"Account Id: {} entered an amount with more than 2 decimal places. Amount: {}",
					account.getId(),
					amount.setScale(10, RoundingMode.UNNECESSARY) + "..."
				);
			} else {
				logger.error(
					"Account Id: {} entered an amount with more than 2 decimal places. Amount: {}",
					account.getId(),
					amount
				);
			}
			throw new IllegalArgumentException(
				"Amounts can have at most 2 decimal places (e.g. $12.34). Please try again."
			);
		} else {
			return amount.setScale(2, RoundingMode.UNNECESSARY);
		}
	}

	private void checkOverdraft(Account account, BigDecimal amount) {
		if (account.getBalance().subtract(amount).signum() < 0) {
			logger.error(
				"Account Id {} attempted an overdraft. Balance: {} - Amount: {}",
				account.getId(),
				account.getBalance(),
				amount
			);
			throw new IllegalArgumentException(
				"Transaction will lead to overdraft and cannot proceed. Please try again with a different amount."
			);
		}
	}

	private boolean loggedIn() {
		return account != null;
	}

	private IllegalArgumentException notLoggedIn() {
		return new IllegalArgumentException(
			"You have not logged into your account yet, please log in or create an account to continue."
		);
	}

	@Override
	public void logOut() {
		if (!loggedIn()) {
			throw notLoggedIn();
		}
		logger.info("Account Id {} logged out from their account", account.getId());
		account = null;
	}

	@Override
	public Account getAccountStatus() {
		if (!loggedIn()) {
			logger.error("A user attempted to get their account status without login in first");
			throw notLoggedIn();
		}
		logger.info("Account Id: {} checked their account status", account.getId());
		return account;
	}

	@Override
	public List<Transaction> getTransactions() {
		if (!loggedIn()) {
			logger.error("A user attempted to get their account transaction without login in first");
			throw notLoggedIn();
		}
		List<Transaction> txs = bankDAO.getAllTransactionsByAccountId(account);
		logger.info("Account Id: {} checked all their transactions", account.getId());
		return txs;
	}
}
