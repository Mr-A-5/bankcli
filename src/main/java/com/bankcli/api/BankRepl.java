package com.bankcli.api;

import com.bankcli.domain.Account;
import com.bankcli.service.AccountService;
import java.util.Scanner;

public class BankRepl {

	private final AccountService service;
	private final Scanner scanner = new Scanner(System.in);

	public BankRepl(AccountService service) {
		this.service = service;
	}

	public void run() {
		while (true) {
			System.out.print("Please input a command(help for command list): \n> ");
			String command = scanner.nextLine().trim();

			if (command.equals("exit")) {
				return;
			}

			try {
				handle(command);
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}

	private void handle(String command) {
		switch (command) {
			case "create" -> service.createAccount(readString("Please input a pin to create your account: \n> "));
			case "log-in" -> service.getAccount(readAccount());
			case "deposit" -> service.makeDeposit(readDouble("Please input the amount you want to deposit. \n> $"));
			case "withdraw" -> service.makeWithdrawal(
				readDouble("Please input the amount you want to withdraw. \n> $")
			);
			case "transfer" -> service.makeTransfer(
				readInt("Please input the account id of the account you which to transfer to. \n> "),
				readDouble("Please input the amount that you want to transfer. \n> $")
			);
			case "log-out" -> service.logOut();
			case "status" -> service.getAccountStatus();
			case "transactions" -> service.getTransactions();
			case "help" -> printHelp();
			default -> System.out.println("Unknown command");
		}
	}

	/**
	 * This method reads a username and pin from the standard input
	 *
	 * @return the account object created from the username and pin
	 */
	private Account readAccount() {
		int id = readInt("Please input your user name: \n> ");
		String pin = readString("Please input your pin: \n> ");
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

	private double readDouble(String prompt) {
		System.out.print(prompt);
		return Double.parseDouble(scanner.nextLine().trim());
	}

	private void printHelp() {
		System.out.println("Available commands:");
		System.out.println("balance - To get the balance of the account");
		System.out.println("log - To log in into an account using their id and pin");
		System.out.println("create - To create an account in the bank");
		System.out.println("transfer - To transfer to another account");
		System.out.println("withdraw - Withdraw money from the account");
		System.out.println("deposit - Deposit money into the account");
		System.out.println("help - Show this help message");
		System.out.println("exit - Exit the application");
	}
}
