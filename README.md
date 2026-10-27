# Simple ATM System

A beginner-friendly desktop ATM built with Java Swing, JDBC and MySQL.

## Features
- Login with account number and PIN (PIN stored as a SHA-256 hash)
- Check balance, deposit and withdraw money
- Every transaction saved in a history table
- Handles invalid credentials and insufficient funds

## Setup
1. Install JDK 21+, MySQL Server 8 and MySQL Workbench.
2. Download MySQL Connector/J (platform independent) and copy the JAR into `lib/`.
3. Run `database.sql` in MySQL Workbench.
4. Run `run.bat` (Windows) and enter your MySQL password when asked.

Test login: account `1001001001`, PIN `1234`
