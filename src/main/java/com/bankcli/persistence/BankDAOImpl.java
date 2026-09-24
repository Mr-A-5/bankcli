package com.bankcli.persistence;

import com.bankcli.domain.Account;
import com.bankcli.domain.Transaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BankDAOImpl implements BankDAO {

	private static final String CREATE_TABLE_SQL = """
	CREATE TABLE IF NOT EXISTS account (
	    account_id SERIAL PRIMARY KEY,
	    pin VARCHAR(255) NOT NULL,
	    balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00
	);

	CREATE TABLE  IF NOT EXISTS transaction (
		transaction_id SERIAL PRIMARY KEY,
		account_id INTEGER NOT NULL REFERENCES account(account_id),
		type VARCHAR(20) NOT NULL,
		amount NUMERIC(12, 2) NOT NULL,
		related_account_id INTEGER REFERENCES account(account_id),
		timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
	);
	""";

	private static final String INSERT_ACCOUNT_SQL = "INSERT INTO account (pin) VALUES (?) RETURNING account_id";
	private static final String FIND_ACCOUNT_BY_ID = "SELECT balance from account WHERE account_id = ?";
	private static final String FIND_ACCOUNT_BY_LOGIN_SQL =
		"SELECT balance FROM account WHERE account_id = ? AND pin = ?";
	private static final String UPDATE_BALANCE_OF_ACCOUNT = "UPDATE account SET balance = ? WHERE account_id = ?;";
	// private static final String INSERT_DEPOSIT_TRANSACTION =
	// 	"INSERT INTO transaction (account_id, type, amount, related_account_id, timestamp) VALUES (?, ?, ?, ?, ?) RETURNING transaction_id,account_id, type, amount, related_account_id, timestamp;";

	private static final String INSERT_DEPOSIT_TRANSACTION =
		"INSERT INTO transaction (account_id, type, amount, related_account_id, timestamp) VALUES (?, ?, ?, ?, ?);";
	private static final String FIND_TRANSACTIONS_BY_ID =
		"SELECT transaction_id ,account_id, type, amount, related_account_id, timestamp FROM transaction WHERE account_id = ? ORDER BY timestamp DESC";

	public BankDAOImpl() {
		initializeSchema();
	}

	@Override
	public void processTransaction(Account account, BigDecimal amount, Account toAccount, String type) {
		if (amount.signum() < 0) {
			throw new IllegalArgumentException("Transaction amount must be positive");
		}

		if (type.equals("DEPOSIT") || type.equals("WITHDRAW")) {
			try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
				connection.setAutoCommit(false);
				BigDecimal newBalance;
				if (type.equals("DEPOSIT")) {
					newBalance = account.getBalance().add(amount);
				} else {
					newBalance = account.getBalance().subtract(amount);
				}

				try (
					PreparedStatement updateBalance = connection.prepareStatement(UPDATE_BALANCE_OF_ACCOUNT);
					PreparedStatement insertTransaction = connection.prepareStatement(INSERT_DEPOSIT_TRANSACTION)
				) {
					updateBalance.setBigDecimal(1, newBalance);
					updateBalance.setInt(2, account.getId());

					if (updateBalance.executeUpdate() != 1) {
						throw new SQLException("Account not found or PIN incorrect");
					}

					insertTransaction.setInt(1, account.getId());
					insertTransaction.setString(2, type);
					insertTransaction.setBigDecimal(3, type.equals("WITHDRAW") ? amount.negate() : amount);
					insertTransaction.setNull(4, Types.INTEGER);
					insertTransaction.setTimestamp(5, Timestamp.from(Instant.now()));
					if (insertTransaction.executeUpdate() != 1) {
						throw new SQLException("Could not record deposit transaction");
					}

					connection.commit();
				} catch (SQLException e) {
					connection.rollback();
					throw e;
				}
			} catch (SQLException e) {
				throw databaseError("Could not process transaction", e);
			}
		} else if (type.equals("TRANSFER")) {
			try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
				connection.setAutoCommit(false);
				BigDecimal fromAccNewBalance = account.getBalance().subtract(amount);
				BigDecimal toAccNewBalance = toAccount.getBalance().add(amount);
				try (
					PreparedStatement updateBalanceFromAcc = connection.prepareStatement(UPDATE_BALANCE_OF_ACCOUNT);
					PreparedStatement updateBalanceToAcc = connection.prepareStatement(UPDATE_BALANCE_OF_ACCOUNT);
					PreparedStatement insertTransactionFromAcc = connection.prepareStatement(
						INSERT_DEPOSIT_TRANSACTION
					);
					PreparedStatement insertTransactionToAcc = connection.prepareStatement(INSERT_DEPOSIT_TRANSACTION)
				) {
					updateBalanceFromAcc.setBigDecimal(1, fromAccNewBalance);
					updateBalanceFromAcc.setInt(2, account.getId());
					if (updateBalanceFromAcc.executeUpdate() != 1) {
						throw new SQLException("Account not found or PIN incorrect");
					}

					updateBalanceToAcc.setBigDecimal(1, toAccNewBalance);
					updateBalanceToAcc.setInt(2, toAccount.getId());
					if (updateBalanceToAcc.executeUpdate() != 1) {
						throw new SQLException("Account not found or PIN incorrect");
					}

					insertTransactionFromAcc.setInt(1, account.getId());
					insertTransactionFromAcc.setString(2, type);
					insertTransactionFromAcc.setBigDecimal(3, amount.negate());
					insertTransactionFromAcc.setInt(4, toAccount.getId());
					insertTransactionFromAcc.setTimestamp(5, Timestamp.from(Instant.now()));
					if (insertTransactionFromAcc.executeUpdate() != 1) {
						throw new SQLException("Could not record deposit transaction");
					}

					insertTransactionToAcc.setInt(1, toAccount.getId());
					insertTransactionToAcc.setString(2, type);
					insertTransactionToAcc.setBigDecimal(3, amount);
					insertTransactionToAcc.setInt(4, account.getId());
					insertTransactionToAcc.setTimestamp(5, Timestamp.from(Instant.now()));
					if (insertTransactionToAcc.executeUpdate() != 1) {
						throw new SQLException("Could not record deposit transaction");
					}

					connection.commit();
				} catch (SQLException e) {
					connection.rollback();
					throw e;
				}
			} catch (SQLException e) {
				throw databaseError("Could not process transaction", e);
			}
		}
	}

	@Override
	public Account createAccount(String pin) {
		try (
			Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
			PreparedStatement statement = connection.prepareStatement(INSERT_ACCOUNT_SQL, new String[] { "account_id" })
		) {
			statement.setString(1, pin);
			statement.executeUpdate();
			try (ResultSet keys = statement.getGeneratedKeys()) {
				keys.next();
				int accountId = keys.getInt(1);
				return new Account(accountId, pin);
			} catch (SQLException e) {
				throw databaseError("Could not retrieve account id", e);
			}
		} catch (SQLException e) {
			throw databaseError("Could not add student", e);
		}
	}

	private void initializeSchema() {
		try (
			Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
			PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)
		) {
			statement.executeUpdate();
		} catch (SQLException e) {
			throw databaseError("Could not initialize database schema", e);
		}
	}

	private IllegalStateException databaseError(String message, SQLException cause) {
		return new IllegalStateException(message, cause);
	}

	@Override
	public Account getAccountByLogIn(Account account) {
		try (
			Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
			PreparedStatement statement = connection.prepareStatement(FIND_ACCOUNT_BY_LOGIN_SQL)
		) {
			statement.setInt(1, account.getId());
			statement.setString(2, String.valueOf(account.getPin()));
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					account.setBalance(resultSet.getBigDecimal("balance"));
					return account;
				}
			}
			return null;
		} catch (SQLException e) {
			throw databaseError("Could not find account", e);
		}
	}

	@Override
	public BigDecimal getAccountBalanceByID(int id) {
		try (
			Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
			PreparedStatement statement = connection.prepareStatement(FIND_ACCOUNT_BY_ID)
		) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getBigDecimal("balance");
				}
			}
			return null;
		} catch (SQLException e) {
			throw databaseError("Could not find account", e);
		}
	}

	@Override
	public List<Transaction> getAllTransactionsByAccountId(Account account) {
		List<Transaction> txs = new ArrayList<>();
		try (
			Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
			PreparedStatement statement = connection.prepareStatement(FIND_TRANSACTIONS_BY_ID)
		) {
			statement.setInt(1, account.getId());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				txs.add(mapTransactions(resultSet));
			}
			return txs;
		} catch (SQLException e) {
			throw databaseError("Could not find account", e);
		}
	}

	private Transaction mapTransactions(ResultSet resultSet) throws SQLException {
		int relatedAccountId = resultSet.getInt("related_account_id");
		Integer relatedAccount = resultSet.wasNull() ? null : relatedAccountId;
		int ownerAccount = resultSet.getInt("account_id");
		BigDecimal amount = resultSet.getBigDecimal("amount");
		String type = resultSet.getString("type");

		// Money-out rows are stored negative, so a positive TRANSFER row is one this account received.
		boolean receivedTransfer = type.equals("TRANSFER") && relatedAccount != null && amount.signum() > 0;
		int fromAccount = receivedTransfer ? relatedAccount : ownerAccount;
		Integer toAccount = receivedTransfer ? Integer.valueOf(ownerAccount) : relatedAccount;

		return new Transaction(
			resultSet.getInt("transaction_id"),
			amount,
			resultSet.getTimestamp("timestamp"),
			fromAccount,
			toAccount,
			type
		);
	}
}
