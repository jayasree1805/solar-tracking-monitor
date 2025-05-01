package solar_tracker;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;

public class STL {
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private boolean isViewingCurrentData = true;
    private Timer refreshTimer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new STL().createLoginGUI());
    }

    private void createLoginGUI() {
        JFrame frame = new JFrame("Solar Tracker Login");
        frame.setSize(400, 250);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(34, 139, 34));
        JLabel titleLabel = new JLabel("Solar Tracking Monitor");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel());
        formPanel.add(loginBtn);

        frame.add(formPanel, BorderLayout.CENTER);

        loginBtn.addActionListener(_ -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticate(username, password)) {
                frame.dispose();
                showTrackerGUI();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials");
            }
        });

        frame.setVisible(true);
    }

    private boolean authenticate(String username, String password) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/solartracker", "root", "root123*")) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showTrackerGUI() {
        JFrame frame = new JFrame("Solar Tracker Monitor");
        frame.setSize(700, 400);
        frame.setLayout(new BorderLayout());

        String[] columnNames = {"Azimuth", "Tilt", "Produced (Wh)", "Remaining (Wh)", "Efficiency (%)", "Dust Level"};
        tableModel = new DefaultTableModel(columnNames, 0);
        dataTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(dataTable);

        JPanel topPanel = new JPanel();
        JButton toggleBtn = new JButton("Show Historical Data");
        topPanel.add(toggleBtn);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        toggleBtn.addActionListener(_ -> {
            isViewingCurrentData = !isViewingCurrentData;
            if (isViewingCurrentData) {
                toggleBtn.setText("Show Historical Data");
                showCurrentData();
            } else {
                toggleBtn.setText("Show Current Data");
                showHistoricalData();
            }
        });

        setupAutoRefresh();
        showCurrentData(); // initial load
        frame.setVisible(true);
    }

    private void showCurrentData() {
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\JAYASREE G KALKURA\\JAVA\\sensor_data.txt"))) {
            String line = null;
            String temp;
            while ((temp = br.readLine()) != null) {
                line = temp; // Get the last line
            }

            if (line != null) {
                String[] parts = line.split(",");
                int azimuth = Integer.parseInt(parts[0].trim());
                int tilt = Integer.parseInt(parts[1].trim());
                double energyProduced = Double.parseDouble(parts[2].trim());
                double energyRemaining = Double.parseDouble(parts[3].trim());
                int dustLevel = Integer.parseInt(parts[4].trim());
                double efficiency = (energyProduced / (energyProduced + energyRemaining)) * 100;

                tableModel.setRowCount(0);
                tableModel.addRow(new Object[]{azimuth, tilt, energyProduced, energyRemaining, String.format("%.2f", efficiency), dustLevel});

                insertIntoHistory(azimuth, tilt, energyProduced, energyRemaining, dustLevel);

                if (dustLevel > 300) {
                    JOptionPane.showMessageDialog(null, "⚠ High Dust Level! Please clean the solar panel.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showHistoricalData() {
        String[] columnNames = {"Timestamp", "Azimuth", "Tilt", "Produced", "Remaining", "Dust"};
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/solartracker", "root", "root123*")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM history ORDER BY timestamp DESC LIMIT 20");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getTimestamp("timestamp"),
                        rs.getInt("azimuth"),
                        rs.getInt("tilt"),
                        rs.getDouble("energyProduced"),
                        rs.getDouble("energyRemaining"),
                        rs.getInt("dustLevel")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void insertIntoHistory(int azimuth, int tilt, double energyProduced, double energyRemaining, int dustLevel) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/solartracker", "root", "root123*")) {
            String query = "INSERT INTO history (azimuth, tilt, energyProduced, energyRemaining, dustLevel, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, azimuth);
            ps.setInt(2, tilt);
            ps.setDouble(3, energyProduced);
            ps.setDouble(4, energyRemaining);
            ps.setInt(5, dustLevel);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupAutoRefresh() {
        refreshTimer = new Timer(5000, _ -> {
            if (isViewingCurrentData) {
                showCurrentData();
            }
        });
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }
}
