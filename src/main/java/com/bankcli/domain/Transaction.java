package com.bankcli.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {

	private int id;
	private BigDecimal amount;
	private Timestamp timestamp;
	private int fromAccount;
	private Integer toAccount;
	private String type;

	public Transaction(int id, BigDecimal amount, Timestamp timestamp, int fromAccount, Integer toAccount, String type) {
		this.id = id;
		this.amount = amount;
		this.timestamp = timestamp;
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.type = type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(int fromAccount) {
		this.fromAccount = fromAccount;
	}

	public Integer getToAccount() {
		return toAccount;
	}

	public void setToAccount(Integer toAccount) {
		this.toAccount = toAccount;
	}

	@Override
	public String toString() {
		return String.format(
			"| %-30s | %-20d | %-20s | %-20d | %-20s | %-20s |",
			getTimestamp().toString(),
			getId(),
			getType(),
			getFromAccount(),
			(getAmount().signum() < 0 ? "-$" : "$") + String.format("%.2f", getAmount().abs()),
			getToAccount() == null ? "-" : getToAccount()
		);
	}
}
