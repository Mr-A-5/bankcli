package com.bankcli.api;

import com.bankcli.domain.Account;
import com.bankcli.domain.Transaction;
import com.bankcli.service.AccountService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class BankRepl {

	private final AccountService service;
	private final Scanner scanner = new Scanner(System.in);

	public BankRepl(AccountService service) {
		this.service = service;
	}

	public void run() {
		System.out.println("Welcome to BANK CLI");
		while (true) {
			System.out.print("Please enter a command (type help for the list of commands): \n> ");
			String command = scanner.nextLine().trim();

			if (command.equals("exit")) {
				return;
			}

			try {
				handle(command);
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			} catch (IllegalStateException e) {
				System.out.println("Error: Something went wrong on our end. Please try again later.");
			}
		}
	}

	private void handle(String command) {
		switch (command) {
			case "1" -> printAccount(service.getAccount(readAccount()));
			case "2" -> {
				Account created = service.createAccount(readString("Please enter a PIN to create your account: \n> "));
				System.out.println("Welcome to bank CLI:");
				printAccount(created);
			}
			case "3" -> printAccount(
				service.makeDeposit(readAmount("Please enter the amount you want to deposit: \n> $"))
			);
			case "4" -> printAccount(
				service.makeWithdrawal(readAmount("Please enter the amount you want to withdraw: \n> $"))
			);
			case "5" -> printAccount(
				service.makeTransfer(
					readInt("Please enter the Account ID of the account you wish to transfer to: \n> "),
					readAmount("Please enter the amount you want to transfer: \n> $")
				)
			);
			case "6" -> printAccount(service.getAccountStatus());
			case "7" -> printTransactions(service.getTransactions());
			case "8" -> service.logOut();
			case "help" -> printHelp();
			default -> System.out.println("Unknown command. Type help to see the list of commands.");
		}
	}

	private void printAccount(Account account) {
		System.out.println(account);
	}

	private void printTransactions(List<Transaction> txs) {
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
	}

	private Account readAccount() {
		int id = readInt("Please enter your Account ID: \n> ");
		String pin = readString("Please enter your PIN: \n> ");
		Account acc = new Account(id, pin);
		return acc;
	}

	private String readString(String prompt) {
		System.out.print(prompt);
		String str = scanner.nextLine().trim();
		return str;
	}

	private int readInt(String prompt) {
		System.out.print(prompt);
		return Integer.parseInt(scanner.nextLine().trim());
	}

	private BigDecimal readAmount(String prompt) {
		System.out.print(prompt);
		try {
			return new BigDecimal(scanner.nextLine().trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Please enter a valid amount (e.g. 12.34).");
		}
	}

	private void printHelp() {
		System.out.println("\nAvailable commands:");
		System.out.println("1 - Log in to an account using your Account ID and PIN");
		System.out.println("2 - Create a new account");
		System.out.println("3 - Deposit money into your account");
		System.out.println("4 - Withdraw money from your account");
		System.out.println("5 - Transfer money to another account");
		System.out.println("6 - View your account status");
		System.out.println("7 - View your transaction history");
		System.out.println("8 - Log out of your account");
		System.out.println("help - Show this help message");
		System.out.println("exit - Exit the application\n");
	}
}
