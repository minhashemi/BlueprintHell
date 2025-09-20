package dev.aminhashemi.blueprinthell.view.ui;

import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.view.GameFrame;
import dev.aminhashemi.blueprinthell.utils.PlayerManager;
import dev.aminhashemi.blueprinthell.utils.LeaderboardManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Leaderboard panel displaying player records and statistics
 */
public class LeaderboardPanel extends JPanel {
    
    private final GameFrame gameFrame;
    private LeaderboardData leaderboardData;
    private JTabbedPane tabbedPane;
    private JTable globalTable;
    private JTable levelTable;
    private JLabel playerStatsLabel;
    
    public LeaderboardPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.leaderboardData = new LeaderboardData(); // Initialize with empty data
        initUI();
        updateTables();
        updatePlayerStats();
        
        // Load leaderboard data asynchronously
        LeaderboardManager.loadLeaderboard().thenAccept(data -> {
            if (data != null) {
                this.leaderboardData = data;
                SwingUtilities.invokeLater(() -> {
                    updateTables();
                    updatePlayerStats();
                });
            }
        });
    }
    
    private void initUI() {
        setName(GameFrame.LEADERBOARD_PANEL);
        setLayout(new BorderLayout());
        setBackground(new Color(25, 25, 35));
        
        // Create title
        JLabel titleLabel = new JLabel("LEADERBOARDS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(25, 25, 35));
        tabbedPane.setForeground(Color.WHITE);
        
        // Global leaderboard tab
        createGlobalLeaderboardTab();
        
        // Level-specific leaderboard tab
        createLevelLeaderboardTab();
        
        // Player statistics tab
        createPlayerStatsTab();
        
        // Back button
        JButton backButton = new JButton("Back to Main Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 18));
        backButton.setFocusable(false);
        backButton.setPreferredSize(new Dimension(200, 40));
        backButton.addActionListener(e -> gameFrame.switchToPanel(GameFrame.MAIN_MENU_PANEL));
        
        // Layout
        add(titleLabel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);
        
        setPreferredSize(new Dimension(1000, 700));
    }
    
    private void createGlobalLeaderboardTab() {
        JPanel globalPanel = new JPanel(new BorderLayout());
        globalPanel.setBackground(new Color(25, 25, 35));
        
        // Table for global records
        String[] globalColumns = {"Rank", "Player", "Level", "Time", "XP", "Packet Loss %", "Coins"};
        DefaultTableModel globalModel = new DefaultTableModel(globalColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        globalTable = new JTable(globalModel);
        globalTable.setBackground(new Color(40, 40, 50));
        globalTable.setForeground(Color.WHITE);
        globalTable.setSelectionBackground(new Color(70, 130, 180));
        globalTable.setFont(new Font("Arial", Font.PLAIN, 14));
        globalTable.setRowHeight(25);
        globalTable.getTableHeader().setBackground(new Color(60, 60, 70));
        globalTable.getTableHeader().setForeground(Color.WHITE);
        globalTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane globalScrollPane = new JScrollPane(globalTable);
        globalScrollPane.setBackground(new Color(25, 25, 35));
        globalScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2), 
            "Global Leaderboard - Best Times", 
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            Color.WHITE
        ));
        
        globalPanel.add(globalScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Global", globalPanel);
    }
    
    private void createLevelLeaderboardTab() {
        JPanel levelPanel = new JPanel(new BorderLayout());
        levelPanel.setBackground(new Color(25, 25, 35));
        
        // Level selection combo box
        JComboBox<String> levelComboBox = new JComboBox<>();
        levelComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        levelComboBox.setBackground(new Color(40, 40, 50));
        levelComboBox.setForeground(Color.WHITE);
        levelComboBox.addItem("Level 1");
        levelComboBox.addItem("Level 2");
        levelComboBox.addItem("Level 3");
        
        // Table for level records
        String[] levelColumns = {"Rank", "Player", "Time", "XP", "Packet Loss %", "Coins", "Date"};
        DefaultTableModel levelModel = new DefaultTableModel(levelColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        levelTable = new JTable(levelModel);
        levelTable.setBackground(new Color(40, 40, 50));
        levelTable.setForeground(Color.WHITE);
        levelTable.setSelectionBackground(new Color(70, 130, 180));
        levelTable.setFont(new Font("Arial", Font.PLAIN, 14));
        levelTable.setRowHeight(25);
        levelTable.getTableHeader().setBackground(new Color(60, 60, 70));
        levelTable.getTableHeader().setForeground(Color.WHITE);
        levelTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane levelScrollPane = new JScrollPane(levelTable);
        levelScrollPane.setBackground(new Color(25, 25, 35));
        levelScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2), 
            "Level Leaderboard", 
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            Color.WHITE
        ));
        
        // Level selection panel
        JPanel levelSelectionPanel = new JPanel(new FlowLayout());
        levelSelectionPanel.setBackground(new Color(25, 25, 35));
        levelSelectionPanel.add(new JLabel("Select Level: "));
        levelSelectionPanel.add(levelComboBox);
        
        levelComboBox.addActionListener(e -> updateLevelTable(levelComboBox.getSelectedItem().toString()));
        
        levelPanel.add(levelSelectionPanel, BorderLayout.NORTH);
        levelPanel.add(levelScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("By Level", levelPanel);
    }
    
    private void createPlayerStatsTab() {
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(new Color(25, 25, 35));
        
        // Player statistics display
        playerStatsLabel = new JLabel();
        playerStatsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        playerStatsLabel.setForeground(Color.WHITE);
        playerStatsLabel.setVerticalAlignment(SwingConstants.TOP);
        playerStatsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JScrollPane statsScrollPane = new JScrollPane(playerStatsLabel);
        statsScrollPane.setBackground(new Color(25, 25, 35));
        statsScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2), 
            "Your Statistics", 
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            Color.WHITE
        ));
        
        statsPanel.add(statsScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Your Stats", statsPanel);
    }
    
    private void loadSampleData() {
        // Sample data loading removed - now using persistent storage
        // Data is loaded from JSON file in constructor
    }
    
    private void addSampleRecord(String levelName, String playerName, long completionTime, 
                               int levelNumber, double packetLossPercentage, int coinsEarned) {
        int xpEarned = LeaderboardData.calculateXP(completionTime, packetLossPercentage, coinsEarned);
        LeaderboardData.PlayerRecord record = new LeaderboardData.PlayerRecord(
            playerName, completionTime, xpEarned, levelNumber, packetLossPercentage, coinsEarned
        );
        leaderboardData.addRecord(levelName, record);
    }
    
    public void updateTables() {
        updateGlobalTable();
        updateLevelTable("Level 1");
    }
    
    private void updateGlobalTable() {
        DefaultTableModel model = (DefaultTableModel) globalTable.getModel();
        model.setRowCount(0);
        
        List<LeaderboardData.PlayerRecord> globalRecords = leaderboardData.getGlobalRecords();
        for (int i = 0; i < globalRecords.size(); i++) {
            LeaderboardData.PlayerRecord record = globalRecords.get(i);
            model.addRow(new Object[]{
                i + 1,
                record.playerName,
                "Level " + record.levelNumber,
                record.getFormattedTime(),
                record.xpEarned,
                String.format("%.1f%%", record.packetLossPercentage),
                record.coinsEarned
            });
        }
    }
    
    private void updateLevelTable(String levelName) {
        DefaultTableModel model = (DefaultTableModel) levelTable.getModel();
        model.setRowCount(0);
        
        List<LeaderboardData.PlayerRecord> levelRecords = leaderboardData.getLevelRecords(levelName);
        for (int i = 0; i < levelRecords.size(); i++) {
            LeaderboardData.PlayerRecord record = levelRecords.get(i);
            model.addRow(new Object[]{
                i + 1,
                record.playerName,
                record.getFormattedTime(),
                record.xpEarned,
                String.format("%.1f%%", record.packetLossPercentage),
                record.coinsEarned,
                new java.util.Date(record.timestamp).toString()
            });
        }
    }
    
    public void updatePlayerStats() {
        LeaderboardData.PlayerStats stats = leaderboardData.getCurrentPlayerStats();
        String currentPlayerName = PlayerManager.getInstance().getPlayerName();
        
        StringBuilder statsText = new StringBuilder();
        statsText.append("<html><body style='font-family: Arial; line-height: 1.6;'>");
        statsText.append("<h2 style='color: #FFD700;'>").append(currentPlayerName).append("</h2>");
        statsText.append("<br>");
        statsText.append("<h3 style='color: #00FFFF;'>Overall Statistics</h3>");
        statsText.append("<p><b>Total XP:</b> ").append(stats.totalXP).append("</p>");
        statsText.append("<p><b>Total Coins:</b> ").append(stats.totalCoins).append("</p>");
        statsText.append("<p><b>Levels Completed:</b> ").append(stats.levelsCompleted).append("</p>");
        
        if (stats.bestTime != Long.MAX_VALUE) {
            long seconds = stats.bestTime / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            statsText.append("<p><b>Best Time:</b> ").append(String.format("%d:%02d", minutes, seconds)).append("</p>");
        }
        
        statsText.append("<p><b>Best XP in Single Game:</b> ").append(stats.bestXP).append("</p>");
        
        statsText.append("<br>");
        statsText.append("<h3 style='color: #00FFFF;'>Level Records</h3>");
        
        for (int level : stats.levelBestTimes.keySet()) {
            long time = stats.levelBestTimes.get(level);
            int xp = stats.levelBestXP.getOrDefault(level, 0);
            long seconds = time / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            
            statsText.append("<p><b>Level ").append(level).append(":</b> ");
            statsText.append("Best Time: ").append(String.format("%d:%02d", minutes, seconds));
            statsText.append(", Best XP: ").append(xp).append("</p>");
        }
        
        statsText.append("</body></html>");
        
        playerStatsLabel.setText(statsText.toString());
    }
    
    /**
     * Add a new record from game completion
     */
    public void addGameRecord(String levelName, String playerName, long completionTime, 
                            int levelNumber, double packetLossPercentage, int coinsEarned) {
        addSampleRecord(levelName, playerName, completionTime, levelNumber, packetLossPercentage, coinsEarned);
        leaderboardData.updatePlayerStats(playerName, levelNumber, completionTime, 
            LeaderboardData.calculateXP(completionTime, packetLossPercentage, coinsEarned), coinsEarned);
        updateTables();
        updatePlayerStats();
    }
    
    /**
     * Add a new record using the current player's name
     */
    public void addGameRecord(String levelName, long completionTime, 
                            int levelNumber, double packetLossPercentage, int coinsEarned) {
        String playerName = PlayerManager.getInstance().getPlayerName();
        addGameRecord(levelName, playerName, completionTime, levelNumber, packetLossPercentage, coinsEarned);
        
        // Show a message that the record was added
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "New record added for " + playerName + "!\n" +
                "Level: " + levelName + "\n" +
                "Time: " + new LeaderboardData.PlayerRecord("", completionTime, 0, levelNumber, packetLossPercentage, coinsEarned).getFormattedTime() + "\n" +
                "XP: " + LeaderboardData.calculateXP(completionTime, packetLossPercentage, coinsEarned),
                "Record Added!",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    /**
     * Get the leaderboard data instance
     */
    public LeaderboardData getLeaderboardData() {
        return leaderboardData;
    }
}
