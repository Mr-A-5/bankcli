package com.bankcli.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bankcli.domain.Account;
import com.bankcli.persistence.BankDAO;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

	@Mock
	private BankDAO bankDAO;

	private AccountServiceImpl accountService;

	@BeforeEach
	void setUp() {
		accountService = new AccountServiceImpl(bankDAO);
	}

	private static void assertAmount(String expected, BigDecimal actual) {
		assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
	}

	private Account logInWithBalance(String balance) {
		Account account = new Account(1, new BigDecimal(balance));
		when(bankDAO.createAccount("1234")).thenReturn(account);
		accountService.createAccount("1234");
		return account;
	}

	@Test
	void makeDeposit_addsAmountToBalanceAndRecordsTransaction() {
		Account account = new Account(1, "1234");
		when(bankDAO.createAccount("1234")).thenReturn(account);
		accountService.createAccount("1234");

		accountService.makeDeposit(new BigDecimal("100"));

		assertAmount("100", account.getBalance());
		verify(bankDAO).processTransaction(eq(account), eq(new BigDecimal("100.00")), eq(null), eq("DEPOSIT"));
	}

	@Test
	void makeDeposit_acceptsTwoDecimalPlaces() {
		Account account = logInWithBalance("0");

		accountService.makeDeposit(new BigDecimal("10.55"));

		assertAmount("10.55", account.getBalance());
		verify(bankDAO).processTransaction(eq(account), eq(new BigDecimal("10.55")), eq(null), eq("DEPOSIT"));
	}

	@Test
	void makeDeposit_rejectsMoreThanTwoDecimalPlaces() {
		Account account = logInWithBalance("0");

		assertThrows(IllegalArgumentException.class, () -> accountService.makeDeposit(new BigDecimal("10.555")));

		assertAmount("0", account.getBalance());
		verify(bankDAO, never()).processTransaction(any(), any(), any(), any());
	}

	@Test
	void makeWithdrawal_subtractsAmountFromBalanceAndRecordsTransaction() {
		Account account = logInWithBalance("100.00");

		accountService.makeWithdrawal(new BigDecimal("40"));

		assertAmount("60", account.getBalance());
		verify(bankDAO).processTransaction(eq(account), eq(new BigDecimal("40.00")), eq(null), eq("WITHDRAW"));
	}

	@Test
	void makeWithdrawal_rejectsMoreThanTwoDecimalPlaces() {
		Account account = logInWithBalance("100.00");

		assertThrows(IllegalArgumentException.class, () -> accountService.makeWithdrawal(new BigDecimal("40.001")));

		assertAmount("100", account.getBalance());
		verify(bankDAO, never()).processTransaction(any(), any(), any(), any());
	}

	@Test
	void makeTransfer_movesMoneyWhenAmountHasTwoDecimalPlaces() {
		Account account = logInWithBalance("100.00");
		when(bankDAO.getAccountBalanceByID(2)).thenReturn(new BigDecimal("5.00"));

		accountService.makeTransfer(2, new BigDecimal("25.25"));

		assertAmount("74.75", account.getBalance());
		verify(bankDAO).processTransaction(
			eq(account),
			eq(new BigDecimal("25.25")),
			any(Account.class),
			eq("TRANSFER")
		);
	}

	@Test
	void makeTransfer_rejectsMoreThanTwoDecimalPlaces() {
		Account account = logInWithBalance("100.00");

		assertThrows(IllegalArgumentException.class, () -> accountService.makeTransfer(2, new BigDecimal("25.255")));

		assertAmount("100", account.getBalance());
		verify(bankDAO, never()).processTransaction(any(), any(), any(), any());
	}
}
