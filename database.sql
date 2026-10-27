CREATE DATABASE IF NOT EXISTS atm_system;
USE atm_system;

CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(12) PRIMARY KEY,
    holder_name VARCHAR(100) NOT NULL,
    pin_hash CHAR(64) NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(12) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAW') NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);

INSERT IGNORE INTO accounts (account_number, holder_name, pin_hash, balance) VALUES
('1001001001', 'Arun Kumar', SHA2('1234', 256), 5000.00),
('1001001002', 'Priya Sharma', SHA2('4321', 256), 10000.00);

SELECT account_number, holder_name, balance FROM accounts;
