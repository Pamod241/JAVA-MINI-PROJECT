package gui;

import model.TechnicalOfficer;
import model.User;
import service.AttendanceService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Map;

/**
 * Technical Officer's Attendance management panel.
 * Provides: Add attendance records, batch summary view, individual check.
 * (Same data as AttendanceFrame but scoped to Tech Officer role.)
 */
public class TechOfficerAttendanceFrame extends JFrame {

    private final AttendanceService svc = new AttendanceService();
    private final User user;
    private JPanel contentPanel;

    public TechOfficerAttendanceFrame(User user) {
        this.user = user;
        buildPanel();
    }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        header.add(UITheme.label("Attendance Management", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 20)), BorderLayout.WEST);

        // Department badge
        if (user instanceof TechnicalOfficer to && to.getDepartment() != null) {
            JLabel deptBadge = UITheme.badge("Dept: " + to.getDepartment(), UITheme.ACCENT);
            header.add(deptBadge, BorderLayout.EAST);
        }
        contentPanel.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.setFont(UITheme.FONT_BODY);
        tabs.addTab("➕  Add Attendance",    buildAddPanel());
        tabs.addTab("👤  Individual Check",  buildIndividualPanel());
        tabs.addTab("👥  Batch Summary",     buildBatchPanel());
        contentPanel.add(tabs, BorderLayout.CENTER);
    }

    // ── ADD ATTENDANCE ────────────────────────────────────────────────────
    private JPanel buildAddPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel form = UITheme.card();
        form.setLayout(new GridLayout(0, 2, 12, 12));

        JTextField sid   = UITheme.styledField("Student ID (e.g. ICT2022001)");
        JTextField cid   = UITheme.styledField("Course ID (e.g. ICT2101)");
        JTextField hours = UITheme.styledField("Hours (e.g. 2)");
        JTextField date  = UITheme.styledField("Date (YYYY-MM-DD)");
        JComboBox<String> stype   = new JComboBox<>(new String[]{"Lecture", "Practical"});
        JComboBox<String> present = new JComboBox<>(new String[]{"Present", "Absent"});
        styleCombo(stype); styleCombo(present);

        JButton addBtn = UITheme.primaryButton("➕  Add Record");
        JLabel  status = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);

        form.add(lbl("Student ID"));  form.add(sid);
        form.add(lbl("Course ID"));   form.add(cid);
        form.add(lbl("Hours"));       form.add(hours);
        form.add(lbl("Session Type")); form.add(stype);
        form.add(lbl("Presence"));    form.add(present);
        form.add(lbl("Date"));        form.add(date);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.add(addBtn); btnRow.add(status);

        p.add(form, BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            try {
                int h = Integer.parseInt(hours.getText().trim());
                boolean ok = svc.addAttendance(
                    sid.getText().trim(), cid.getText().trim(),
                    date.getText().trim(), (String) stype.getSelectedItem(),
                    (String) present.getSelectedItem(), h);
                setStatus(status, ok ? "✅ Record added." : "❌ Failed — check Student ID, Course ID and date.", !ok);
                if (ok) { sid.setText(""); cid.setText(""); hours.setText(""); date.setText(""); }
            } catch (NumberFormatException ex) {
                setStatus(status, "❌ Hours must be a number.", true);
            }
        });
        return p;
    }

    // ── INDIVIDUAL CHECK ──────────────────────────────────────────────────
    private JPanel buildIndividualPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel form = UITheme.card(); form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField sidField = UITheme.styledField("Student ID");
        JTextField cidField = UITheme.styledField("Course ID");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"BOTH", "THEORY", "PRACTICAL"});
        styleCombo(typeCombo);
        JButton checkBtn = UITheme.primaryButton("Check");

        gbc.gridx = 0; gbc.gridy = 0; form.add(lbl("Student ID"), gbc);
        gbc.gridx = 1; form.add(lbl("Course ID"), gbc);
        gbc.gridx = 2; form.add(lbl("Session Type"), gbc);
        gbc.gridy = 1; gbc.gridx = 0; sidField.setPreferredSize(new Dimension(160, 38)); form.add(sidField, gbc);
        gbc.gridx = 1; cidField.setPreferredSize(new Dimension(160, 38)); form.add(cidField, gbc);
        gbc.gridx = 2; form.add(typeCombo, gbc);
        gbc.gridx = 3; form.add(checkBtn, gbc);

        JPanel resultRow = new JPanel(new GridLayout(1, 3, 12, 0)); resultRow.setOpaque(false);
        JLabel pctLabel  = bigLabel("—", UITheme.ACCENT);
        JLabel eligLabel = bigLabel("—", UITheme.TEXT_SECONDARY);
        JLabel medLabel  = bigLabel("—", UITheme.ACCENT_YELLOW);
        resultRow.add(resultCard("Attendance %",    pctLabel,  UITheme.ACCENT));
        resultRow.add(resultCard("Eligible (≥80%)", eligLabel, UITheme.ACCENT_GREEN));
        resultRow.add(resultCard("With Medicals %", medLabel,  UITheme.ACCENT_YELLOW));

        checkBtn.addActionListener(e -> {
            String sid = sidField.getText().trim(); String cid = cidField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            if (sid.isEmpty() || cid.isEmpty()) {
                UITheme.showMessage(contentPanel, "Enter Student ID and Course ID.", "Error", true); return;
            }
            double pct  = svc.getAttendancePercentage(sid, cid, type);
            double mPct = svc.getAttendanceWithMedicals(sid, cid);
            boolean elig = svc.isEligible(sid, cid);
            pctLabel.setText(String.format("%.1f%%", pct));
            medLabel.setText(String.format("%.1f%%", mPct));
            eligLabel.setText(elig ? "✅ Eligible" : "❌ Not Eligible");
            eligLabel.setForeground(elig ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        });

        p.add(form, BorderLayout.NORTH);
        p.add(resultRow, BorderLayout.CENTER);
        return p;
    }

    // ── BATCH SUMMARY ─────────────────────────────────────────────────────
    private JPanel buildBatchPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); top.setOpaque(false);
        JTextField cidField = UITheme.styledField("Course ID"); cidField.setPreferredSize(new Dimension(200, 36));
        JButton loadBtn = UITheme.primaryButton("Load Batch");
        top.add(UITheme.label("Course ID:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY));
        top.add(cidField); top.add(loadBtn);
        p.add(top, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Student ID", "Attendance %", "Eligible"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model); styleTable(table);
        p.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            model.setRowCount(0);
            svc.getBatchSummary(cidField.getText().trim()).forEach((sid, pct) ->
                model.addRow(new Object[]{sid, String.format("%.1f%%", pct), pct >= 80 ? "✅ YES" : "❌ NO"}));
        });
        return p;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────
    private JPanel resultCard(String title, JLabel val, Color accent) {
        JPanel c = UITheme.card(); c.setLayout(new BorderLayout(0, 8));
        val.setHorizontalAlignment(SwingConstants.CENTER);
        c.add(UITheme.label(title, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), BorderLayout.NORTH);
        c.add(val, BorderLayout.CENTER);
        return c;
    }
    private JLabel bigLabel(String t, Color c) { return UITheme.label(t, c, new Font("Segoe UI", Font.BOLD, 22)); }
    private JLabel lbl(String t) { return UITheme.label(t, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL); }
    private void styleCombo(JComboBox<?> c) { c.setBackground(UITheme.BG_CARD); c.setForeground(UITheme.TEXT_PRIMARY); c.setFont(UITheme.FONT_BODY); }
    private void setStatus(JLabel l, String msg, boolean err) { l.setText(msg); l.setForeground(err ? UITheme.ACCENT_RED : UITheme.ACCENT_GREEN); }
    private void styleTable(JTable t) {
        t.setBackground(UITheme.BG_CARD); t.setForeground(UITheme.TEXT_PRIMARY);
        t.setFont(UITheme.FONT_BODY); t.setRowHeight(36); t.setGridColor(UITheme.BORDER_COLOR);
        t.setSelectionBackground(new Color(56, 189, 248, 40)); t.setSelectionForeground(UITheme.ACCENT);
        t.getTableHeader().setBackground(UITheme.BG_DARK); t.getTableHeader().setForeground(UITheme.ACCENT);
        t.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setForeground(UITheme.TEXT_PRIMARY); setFont(UITheme.FONT_BODY);
                setBackground(sel ? new Color(56,189,248,40) : r%2==0 ? UITheme.BG_CARD : new Color(28,35,44));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }
}
