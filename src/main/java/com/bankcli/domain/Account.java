package com.bankcli.domain;

import java.util.ArrayList;
import java.util.List;

public class Account {

	private int id;
	private String pin;
	private double balance;
	private List<Transaction> sessionTransactions;

	public Account(int id, String pin) {
		this.id = id;
		this.pin = pin;
		this.balance = 0;
		this.sessionTransactions = new ArrayList<>();
	}

	public Account(int id, double balance) {
		this.id = id;
		this.balance = balance;
	}

	public int getId() {
		return id;
	}

	public String getPin() {
		return pin;
	}

	public double getBalance() {
		return balance;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		String str = String.format(
			"%n Your account details: %n - Account ID: %d %n - Account PIN: %s %n - Balance: $%.2f %n",
			getId(),
			getPin(),
			getBalance()
		);
		return str;
	}

	public List<Transaction> getTransactions() {
		return sessionTransactions;
	}

	public void addTransaction(Transaction tx) {
		this.sessionTransactions.add(tx);
	}

	public void setTransactions(List<Transaction> transactions) {
		this.sessionTransactions = transactions;
	}
}
