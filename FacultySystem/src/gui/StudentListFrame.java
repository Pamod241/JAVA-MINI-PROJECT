package gui;

import db.DBConnection;
import model.User;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentListFrame extends JFrame {

    private final User currentUser;
    private JPanel contentPanel;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public StudentListFrame(User currentUser) {
        this.currentUser = currentUser;
        buildPanel();
    }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(UITheme.label("Student Details", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 20)), BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        searchField = UITheme.styledField("Search by name, ID or type...");
        searchField.setPreferredSize(new Dimension(260, 36));
        JButton searchBtn = UITheme.primaryButton("🔍 Search");
        JButton resetBtn  = UITheme.secondaryButton("Reset");
        searchPanel.add(searchField); searchPanel.add(searchBtn); searchPanel.add(resetBtn);
        header.add(searchPanel, BorderLayout.EAST);
        contentPanel.add(header, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.setOpaque(false);
        filterBar.add(UITheme.label("Filter:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY));

        // ── CHANGED: "Suspended" renamed to "Batch Missed" ──────────────────
        JButton allBtn          = filterButton("All Students",  null);
        JButton properBtn       = filterButton("Proper",        "Proper");
        JButton repeatBtn       = filterButton("Repeat",        "Repeat");
        JButton batchMissedBtn  = filterButton("Batch Missed",  "BatchMissed");
        // ────────────────────────────────────────────────────────────────────

        filterBar.add(allBtn); filterBar.add(properBtn); filterBar.add(repeatBtn); filterBar.add(batchMissedBtn);

        filterBar.add(Box.createHorizontalStrut(20));
        JLabel totalLabel = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
        filterBar.add(totalLabel);
        contentPanel.add(filterBar, BorderLayout.AFTER_LAST_LINE);

        // ── CHANGED: added "Batch" column ────────────────────────────────────
        String[] cols = {"#", "Reg No", "Full Name", "Email", "DOB", "Age", "Status", "Batch", "Department"};
        // ────────────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setBackground(UITheme.BG_CARD); table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY); table.setRowHeight(38); table.setGridColor(UITheme.BORDER_COLOR);
        table.setSelectionBackground(new Color(56, 189, 248, 40)); table.setSelectionForeground(UITheme.ACCENT);
        table.getTableHeader().setBackground(UITheme.BG_DARK); table.getTableHeader().setForeground(UITheme.ACCENT);
        table.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(190);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setMaxWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);  // Status
        table.getColumnModel().getColumn(7).setPreferredWidth(80);   // Batch (new)
        table.getColumnModel().getColumn(8).setPreferredWidth(90);   // Department

        // ── Status column colour renderer (col 6) ────────────────────────────
        // ── CHANGED: "Suspended" rows now show label "Batch Missed" in red ───
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (c == 6) {
                    String type = v != null ? v.toString() : "";
                    switch (type) {
                        case "Proper"       -> { setForeground(UITheme.ACCENT_GREEN);  setFont(new Font("Segoe UI", Font.BOLD, 12)); setText("Proper"); }
                        case "Repeat"       -> { setForeground(UITheme.ACCENT_RED);    setFont(new Font("Segoe UI", Font.BOLD, 12)); setText("Repeat"); }
                        case "BatchMissed" -> { setForeground(UITheme.ACCENT_YELLOW); setFont(new Font("Segoe UI", Font.BOLD, 12)); setText("Batch Missed"); }
                        default             -> { setForeground(UITheme.TEXT_SECONDARY); setFont(UITheme.FONT_BODY); }
                    }
                    setHorizontalAlignment(CENTER);
                } else {
                    setForeground(UITheme.TEXT_PRIMARY); setFont(UITheme.FONT_BODY); setHorizontalAlignment(LEFT);
                }
                setBackground(sel ? new Color(56, 189, 248, 40) : r % 2 == 0 ? UITheme.BG_CARD : new Color(28, 35, 44));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        // ─────────────────────────────────────────────────────────────────────

        JPanel detailPanel = buildDetailPanel();
        detailPanel.setVisible(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                showDetail(detailPanel,
                    tableModel.getValueAt(row, 1).toString(),
                    tableModel.getValueAt(row, 2).toString(),
                    tableModel.getValueAt(row, 3).toString(),
                    tableModel.getValueAt(row, 4).toString(),
                    tableModel.getValueAt(row, 5).toString(),
                    tableModel.getValueAt(row, 6).toString(),
                    tableModel.getValueAt(row, 7).toString(),
                    tableModel.getValueAt(row, 8).toString());
                detailPanel.setVisible(true);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            UITheme.scrollPane(table), detailPanel);
        split.setOpaque(false); split.setBackground(UITheme.BG_DARK);
        split.setDividerLocation(380); split.setDividerSize(6); split.setBorder(null);
        contentPanel.add(split, BorderLayout.CENTER);

        Runnable loadAll = () -> loadStudents(null, null, totalLabel);
        loadAll.run();
        searchBtn.addActionListener(e  -> loadStudents(searchField.getText().trim(), null, totalLabel));
        resetBtn.addActionListener(e   -> { searchField.setText(""); loadAll.run(); });
        allBtn.addActionListener(e          -> loadStudents(null, null,        totalLabel));
        properBtn.addActionListener(e       -> loadStudents(null, "Proper",    totalLabel));
        repeatBtn.addActionListener(e       -> loadStudents(null, "Repeat",    totalLabel));
        // ── CHANGED: filter button passes "BatchMissed" to DB query ─────────────
        batchMissedBtn.addActionListener(e  -> loadStudents(null, "BatchMissed", totalLabel));
        // ────────────────────────────────────────────────────────────────────
    }

    private void loadStudents(String search, String typeFilter, JLabel totalLabel) {
        tableModel.setRowCount(0);
        StringBuilder sql = new StringBuilder(
            "SELECT Reg_no, Fullname, Email, DOB, Age, Type, Dep_id FROM Student ");

        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasType   = typeFilter != null && !typeFilter.isEmpty();

        if (hasSearch || hasType) sql.append("WHERE ");
        if (hasSearch) sql.append("(Fullname LIKE ? OR Reg_no LIKE ?) ");
        if (hasSearch && hasType) sql.append("AND ");
        if (hasType) sql.append("Type=? ");
        sql.append("ORDER BY Reg_no ASC");

        try {
            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql.toString());
            int idx = 1;
            if (hasSearch) {
                String like = "%" + search + "%";
                stmt.setString(idx++, like); stmt.setString(idx++, like);
            }
            if (hasType) stmt.setString(idx, typeFilter);

            ResultSet rs = stmt.executeQuery();
            int row = 0;
            while (rs.next()) {
                row++;
                String regNo = rs.getString("Reg_no");
                String rawType = rs.getString("Type");

                // ── CHANGED: DB now stores BatchMissed directly — no mapping needed ────
                String displayType = rawType;
                // ─────────────────────────────────────────────────────────────

                // ── CHANGED: derive batch from Reg_no prefix (e.g. TG → "TG Batch") ─
                String batch = deriveBatch(regNo);
                // ─────────────────────────────────────────────────────────────

                tableModel.addRow(new Object[]{
                    row,
                    regNo,
                    rs.getString("Fullname"),
                    rs.getString("Email")  != null ? rs.getString("Email")  : "—",
                    rs.getDate("DOB")      != null ? rs.getDate("DOB").toString() : "—",
                    rs.getInt("Age"),
                    displayType,
                    batch,
                    rs.getString("Dep_id") != null ? rs.getString("Dep_id") : "—"
                });
            }

            long total      = countByType(null);
            long proper     = countByType("Proper");
            long repeat     = countByType("Repeat");
            long batchMissed = countByType("BatchMissed");
            totalLabel.setText(String.format(
                "Showing %d  |  Total: %d  |  Proper: %d  |  Repeat: %d  |  Batch Missed: %d",
                row, total, proper, repeat, batchMissed));
        } catch (SQLException e) { System.err.println("Error loading students: " + e.getMessage()); }
    }

    // ── CHANGED: new helper — extracts batch prefix from Reg_no ─────────────
    // e.g. "TG0001" → "TG",  "ICT2022001" → "ICT2022",  "TG0006" → "TG"
    private String deriveBatch(String regNo) {
        if (regNo == null || regNo.isEmpty()) return "—";
        // Extract all leading letters as the batch identifier
        StringBuilder batch = new StringBuilder();
        for (char ch : regNo.toCharArray()) {
            if (Character.isLetter(ch)) batch.append(ch);
            else break;
        }
        return batch.length() > 0 ? batch.toString() : regNo;
    }
    // ─────────────────────────────────────────────────────────────────────────

    private long countByType(String type) {
        String sql = type == null
            ? "SELECT COUNT(*) FROM Student"
            : "SELECT COUNT(*) FROM Student WHERE Type=?";
        try {
            if (type == null) {
                ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(sql);
                if (rs.next()) return rs.getLong(1);
            } else {
                PreparedStatement s = DBConnection.getConnection().prepareStatement(sql);
                s.setString(1, type);
                ResultSet rs = s.executeQuery();
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    private JPanel buildDetailPanel() {
        JPanel p = UITheme.card();
        p.setLayout(new BorderLayout(16, 0));
        p.setPreferredSize(new Dimension(0, 160));
        p.setName("DETAIL");
        return p;
    }

    // ── CHANGED: added batch parameter to showDetail ─────────────────────────
    private void showDetail(JPanel panel, String regNo, String name, String email,
                             String dob, String age, String type, String batch, String dept) {
        panel.removeAll();
        panel.add(UITheme.label("  " + name, UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 15)), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 4, 16, 8));
        fields.setOpaque(false);

        Color typeColor = switch (type) {
            case "Repeat"       -> UITheme.ACCENT_RED;
            case "BatchMissed" -> UITheme.ACCENT_YELLOW;
            default             -> UITheme.ACCENT_GREEN;
        };

        fields.add(detailField("Reg No",     regNo,  UITheme.ACCENT));
        fields.add(detailField("Status",     type,   typeColor));
        fields.add(detailField("Batch",      batch,  UITheme.ACCENT_PURPLE));  // new Batch field
        fields.add(detailField("Department", dept,   UITheme.ACCENT_PURPLE));
        fields.add(detailField("Age",        age,    UITheme.TEXT_SECONDARY));
        fields.add(detailField("Email",      email,  UITheme.TEXT_PRIMARY));
        fields.add(detailField("DOB",        dob,    UITheme.TEXT_PRIMARY));

        panel.add(fields, BorderLayout.CENTER);
        panel.revalidate(); panel.repaint();
    }
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel detailField(String label, String value, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(UITheme.label(label, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), BorderLayout.NORTH);
        p.add(UITheme.label(value != null ? value : "—", valueColor, new Font("Segoe UI", Font.BOLD, 13)), BorderLayout.CENTER);
        return p;
    }

    private JButton filterButton(String text, String type) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? UITheme.BG_HOVER : UITheme.BG_CARD;
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.BORDER_COLOR); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12)); btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }
}
