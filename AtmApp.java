import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class AtmApp extends JFrame {

    private static final String URL = "jdbc:mysql://localhost:3306/atm_system";
    private static final String DB_USER = System.getenv("DB_USER") == null ? "root" : System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") == null ? "" : System.getenv("DB_PASSWORD");

    private static final Color GREEN = new Color(0, 130, 60);
    private static final Color RED = new Color(190, 30, 30);

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final JTextField accountField = new JTextField(18);
    private final JPasswordField pinField = new JPasswordField(18);
    private final JLabel loginMessage = new JLabel(" ", SwingConstants.CENTER);

    private final JLabel welcomeLabel = new JLabel(" ");
    private final JLabel balanceLabel = new JLabel(" ");
    private final JTextField amountField = new JTextField(12);
    private final JLabel atmMessage = new JLabel(" ", SwingConstants.CENTER);
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Type", "Amount", "Balance After", "Date & Time"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private String currentAccount;

    public AtmApp() {
        super("Simple ATM System");
        root.add(buildLoginPanel(), "login");
        root.add(buildAtmPanel(), "atm");
        add(root);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640, 520);
        setMinimumSize(new Dimension(560, 460));
        setLocationRelativeTo(null);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Welcome to Simple ATM", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        panel.add(title, c);

        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        panel.add(new JLabel("Account Number"), c);
        c.gridx = 1;
        panel.add(accountField, c);

        c.gridy = 2;
        c.gridx = 0;
        panel.add(new JLabel("PIN"), c);
        c.gridx = 1;
        panel.add(pinField, c);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        pinField.addActionListener(e -> login());
        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 2;
        panel.add(loginButton, c);

        c.gridy = 4;
        panel.add(loginMessage, c);
        return panel;
    }

    private JPanel buildAtmPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 18f));
        balanceLabel.setFont(balanceLabel.getFont().deriveFont(Font.BOLD, 16f));
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(welcomeLabel);
        top.add(balanceLabel);
        panel.add(top, BorderLayout.NORTH);

        JTable table = new JTable(historyModel);
        table.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Last 10 Transactions"));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel amountRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        amountRow.add(new JLabel("Amount"));
        amountRow.add(amountField);

        JButton balanceButton = new JButton("Check Balance");
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton logoutButton = new JButton("Logout");
        balanceButton.addActionListener(e -> checkBalance());
        depositButton.addActionListener(e -> transact("DEPOSIT"));
        withdrawButton.addActionListener(e -> transact("WITHDRAW"));
        logoutButton.addActionListener(e -> logout());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonRow.add(balanceButton);
        buttonRow.add(depositButton);
        buttonRow.add(withdrawButton);
        buttonRow.add(logoutButton);

        JPanel bottom = new JPanel(new GridLayout(3, 1));
        bottom.add(amountRow);
        bottom.add(buttonRow);
        bottom.add(atmMessage);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    private static String money(BigDecimal value) {
        return String.format("Rs. %,.2f", value);
    }

    private void showMessage(JLabel label, String text, boolean success) {
        label.setForeground(success ? GREEN : RED);
        label.setText(text);
    }

    private void login() {
        String account = accountField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        if (account.isEmpty() || pin.isEmpty()) {
            showMessage(loginMessage, "Enter both account number and PIN.", false);
            return;
        }

        String sql = "SELECT holder_name, balance FROM accounts WHERE account_number = ? AND pin_hash = SHA2(?, 256)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, account);
            ps.setString(2, pin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentAccount = account;
                    welcomeLabel.setText("Welcome, " + rs.getString("holder_name"));
                    balanceLabel.setText("Balance: " + money(rs.getBigDecimal("balance")));
                    amountField.setText("");
                    showMessage(atmMessage, " ", true);
                    loadHistory();
                    pinField.setText("");
                    loginMessage.setText(" ");
                    cards.show(root, "atm");
                } else {
                    showMessage(loginMessage, "Invalid account number or PIN.", false);
                }
            }
        } catch (SQLException ex) {
            showMessage(loginMessage, "Database error: " + ex.getMessage(), false);
        }
    }

    private void checkBalance() {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, currentAccount);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal balance = rs.getBigDecimal("balance");
                    balanceLabel.setText("Balance: " + money(balance));
                    showMessage(atmMessage, "Current balance: " + money(balance), true);
                }
            }
        } catch (SQLException ex) {
            showMessage(atmMessage, "Database error: " + ex.getMessage(), false);
        }
    }

    private void transact(String type) {
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
        } catch (NumberFormatException ex) {
            showMessage(atmMessage, "Enter a valid numeric amount.", false);
            return;
        }

        if (amount.signum() <= 0) {
            showMessage(atmMessage, "Amount must be greater than zero.", false);
            return;
        }
        if (amount.scale() > 2) {
            showMessage(atmMessage, "Amount can have at most 2 decimal places.", false);
            return;
        }
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            showMessage(atmMessage, "Maximum amount per transaction is Rs. 10,00,000.", false);
            return;
        }

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                BigDecimal balance;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE")) {
                    ps.setString(1, currentAccount);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        balance = rs.getBigDecimal("balance");
                    }
                }

                if (type.equals("WITHDRAW") && balance.compareTo(amount) < 0) {
                    con.rollback();
                    showMessage(atmMessage, "Insufficient funds. Available balance: " + money(balance), false);
                    return;
                }

                BigDecimal newBalance = type.equals("DEPOSIT") ? balance.add(amount) : balance.subtract(amount);

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance = ? WHERE account_number = ?")) {
                    ps.setBigDecimal(1, newBalance);
                    ps.setString(2, currentAccount);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO transactions (account_number, transaction_type, amount, balance_after) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, currentAccount);
                    ps.setString(2, type);
                    ps.setBigDecimal(3, amount);
                    ps.setBigDecimal(4, newBalance);
                    ps.executeUpdate();
                }

                con.commit();
                balanceLabel.setText("Balance: " + money(newBalance));
                amountField.setText("");
                String action = type.equals("DEPOSIT") ? "Deposited " : "Withdrew ";
                showMessage(atmMessage, action + money(amount) + ". New balance: " + money(newBalance), true);
                loadHistory();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            showMessage(atmMessage, "Transaction failed: " + ex.getMessage(), false);
        }
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        String sql = "SELECT transaction_type, amount, balance_after, created_at FROM transactions "
                + "WHERE account_number = ? ORDER BY id DESC LIMIT 10";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, currentAccount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historyModel.addRow(new Object[]{
                            rs.getString("transaction_type"),
                            money(rs.getBigDecimal("amount")),
                            money(rs.getBigDecimal("balance_after")),
                            rs.getTimestamp("created_at")
                    });
                }
            }
        } catch (SQLException ex) {
            showMessage(atmMessage, "Could not load history: " + ex.getMessage(), false);
        }
    }

    private void logout() {
        currentAccount = null;
        accountField.setText("");
        pinField.setText("");
        historyModel.setRowCount(0);
        showMessage(loginMessage, "You have been logged out.", true);
        cards.show(root, "login");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AtmApp().setVisible(true));
    }
}
