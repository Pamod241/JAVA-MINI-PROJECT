package gui;

import db.DBConnection;
import model.User;
import service.AttendanceService;
import service.MarksService;
import service.MedicalService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class LecturerStudentView extends JFrame {

    private final AttendanceService attSvc  = new AttendanceService();
    private final MarksService      markSvc = new MarksService();
    private final MedicalService    medSvc  = new MedicalService();
    private final User user;
    private JPanel contentPanel;

    public LecturerStudentView(User user) {
        this.user = user;
        buildPanel();
    }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);
        contentPanel.add(UITheme.label("Undergraduate Overview", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 20)), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.setFont(UITheme.FONT_BODY);

        tabs.addTab("🎓  Students & Eligibility", buildEligibilityTab());
        tabs.addTab("📊  Marks, Grades & GPA",    buildMarksTab());
        tabs.addTab("✅  Attendance Records",      buildAttendanceTab());
        tabs.addTab("🏥  Medical Records",         buildMedicalTab());

        contentPanel.add(tabs, BorderLayout.CENTER);
    }


    private JPanel buildEligibilityTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));


        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        JTextField cidField = UITheme.styledField("Course ID (e.g. ICT2101)");
        cidField.setPreferredSize(new Dimension(220, 36));
        JButton loadBtn    = UITheme.primaryButton("Load Eligibility");
        JButton allStuBtn  = UITheme.secondaryButton("All Students");
        filterBar.add(UITheme.label("Course:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY));
        filterBar.add(cidField); filterBar.add(loadBtn); filterBar.add(allStuBtn);
        p.add(filterBar, BorderLayout.NORTH);


        String[] cols = {"Student ID", "Name", "Type", "Batch",
                         "Att % (raw)", "Att % (w/ med)", "CA Mark", "Att Elig", "CA Elig", "Overall"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(55);


        for (int col : new int[]{7, 8, 9}) {
            table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setBackground(UITheme.BG_CARD);
                    String s = v != null ? v.toString() : "";
                    setForeground(s.startsWith("✅") ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setHorizontalAlignment(CENTER);
                    return this;
                }
            });
        }

        p.add(UITheme.scrollPane(table), BorderLayout.CENTER);


        JPanel detailCard = UITheme.card();
        detailCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 8));
        JLabel eligible   = UITheme.label("", UITheme.ACCENT_GREEN,  UITheme.FONT_SUBTITLE);
        JLabel ineligible = UITheme.label("", UITheme.ACCENT_RED,    UITheme.FONT_SUBTITLE);
        JLabel total      = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
        detailCard.add(UITheme.label("✅ Eligible:", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL));
        detailCard.add(eligible);
        detailCard.add(UITheme.label("❌ Not Eligible:", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL));
        detailCard.add(ineligible);
        detailCard.add(total);
        p.add(detailCard, BorderLayout.SOUTH);


        Runnable loadAll = () -> {
            model.setRowCount(0);
            List<Map<String, Object>> students = getAllStudents();
            for (Map<String, Object> s : students) {
                String sid = str(s, "student_id");
                model.addRow(new Object[]{
                    sid, str(s, "fullname"),
                    studentType(s), orDash(str(s, "dep_id")),
                    "—", "—", "—", "—", "—", "—"
                });
            }
            eligible.setText("—"); ineligible.setText("—");
            total.setText("Select a course and click 'Load Eligibility' to check.");
        };


        loadBtn.addActionListener(e -> {
            String cid = cidField.getText().trim();
            if (cid.isEmpty()) { UITheme.showMessage(contentPanel, "Enter a Course ID.", "Error", true); return; }
            model.setRowCount(0);
            List<Map<String, Object>> students = getAllStudents();
            int eligCount = 0, ineligCount = 0;
            for (Map<String, Object> s : students) {
                String sid = str(s, "student_id");
                if (sid.isEmpty()) continue;
                double attRaw  = attSvc.getAttendancePercentage(sid, cid, "BOTH");
                double attMed  = attSvc.getAttendanceWithMedicals(sid, cid);
                double caMark  = markSvc.getCAMark(sid, cid);
                boolean attEl  = attSvc.isEligible(sid, cid);
                boolean caEl   = markSvc.isCAEligible(sid, cid);
                boolean overall = attEl && caEl;
                if (overall) eligCount++; else ineligCount++;
                model.addRow(new Object[]{
                    sid, str(s, "fullname"),
                    studentType(s), orDash(str(s, "dep_id")),
                    String.format("%.1f%%", attRaw),
                    String.format("%.1f%%", attMed),
                    String.format("%.1f", caMark),
                    attEl  ? "✅ Yes" : "❌ No",
                    caEl   ? "✅ Yes" : "❌ No",
                    overall? "✅ Yes" : "❌ No"
                });
            }
            eligible.setText(String.valueOf(eligCount));
            ineligible.setText(String.valueOf(ineligCount));
            total.setText("Total: " + (eligCount + ineligCount) + " students for " + cid);
        });

        allStuBtn.addActionListener(e -> loadAll.run());
        loadAll.run();
        return p;
    }


    private JPanel buildMarksTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JTabbedPane sub = new JTabbedPane();
        sub.setBackground(UITheme.BG_CARD);
        sub.setForeground(UITheme.TEXT_PRIMARY);
        sub.setFont(UITheme.FONT_BODY);
        sub.addTab("Individual",    buildMarksIndividual());
        sub.addTab("Batch Summary", buildMarksBatch());
        sub.addTab("Upload Marks",  buildMarksUpload());
        p.add(sub, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildMarksIndividual() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel form = UITheme.card(); form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField sidField = UITheme.styledField("Student ID");
        JTextField cidField = UITheme.styledField("Course ID");
        JButton checkBtn    = UITheme.primaryButton("Check");
        JButton sgpaBtn     = UITheme.secondaryButton("Calc SGPA");

        gbc.gridx = 0; gbc.gridy = 0; form.add(lbl("Student ID"), gbc);
        gbc.gridx = 1; form.add(lbl("Course ID"), gbc);
        gbc.gridy = 1; gbc.gridx = 0; sidField.setPreferredSize(new Dimension(160, 38)); form.add(sidField, gbc);
        gbc.gridx = 1; cidField.setPreferredSize(new Dimension(160, 38)); form.add(cidField, gbc);
        gbc.gridx = 2; form.add(checkBtn, gbc);
        gbc.gridx = 3; form.add(sgpaBtn, gbc);
        p.add(form, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0)); cards.setOpaque(false);
        JLabel caLbl    = bigLabel("—", UITheme.ACCENT);
        JLabel finalLbl = bigLabel("—", UITheme.ACCENT_GREEN);
        JLabel gradeLbl = bigLabel("—", UITheme.ACCENT_YELLOW);
        JLabel sgpaLbl  = bigLabel("—", UITheme.ACCENT_PURPLE);
        JLabel caElLbl  = bigLabel("—", UITheme.TEXT_SECONDARY);
        cards.add(resultCard("CA Mark",      caLbl,    UITheme.ACCENT));
        cards.add(resultCard("Final Mark",   finalLbl, UITheme.ACCENT_GREEN));
        cards.add(resultCard("Grade",        gradeLbl, UITheme.ACCENT_YELLOW));
        cards.add(resultCard("SGPA",         sgpaLbl,  UITheme.ACCENT_PURPLE));
        cards.add(resultCard("CA Eligible",  caElLbl,  UITheme.ACCENT_GREEN));
        p.add(cards, BorderLayout.CENTER);

        checkBtn.addActionListener(e -> {
            String sid = sidField.getText().trim(); String cid = cidField.getText().trim();
            if (sid.isEmpty() || cid.isEmpty()) { UITheme.showMessage(contentPanel, "Enter Student ID and Course ID.", "Error", true); return; }
            double ca    = markSvc.getCAMark(sid, cid);
            double final_ = markSvc.getFinalMark(sid, cid);
            boolean caEl = markSvc.isCAEligible(sid, cid);
            caLbl.setText(String.format("%.1f", ca));
            caLbl.setForeground(caEl ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
            finalLbl.setText(String.format("%.1f", final_));
            gradeLbl.setText(markSvc.getGrade(final_));
            caElLbl.setText(caEl ? "✅ Yes" : "❌ No");
            caElLbl.setForeground(caEl ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        });
        sgpaBtn.addActionListener(e -> {
            String sid = sidField.getText().trim();
            if (sid.isEmpty()) { UITheme.showMessage(contentPanel, "Enter Student ID.", "Error", true); return; }
            sgpaLbl.setText(String.format("%.2f", markSvc.calculateSGPA(sid)));
        });
        return p;
    }

    private JPanel buildMarksBatch() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); top.setOpaque(false);
        JTextField cid = UITheme.styledField("Course ID"); cid.setPreferredSize(new Dimension(200, 36));
        JButton load = UITheme.primaryButton("Load Batch");
        top.add(UITheme.label("Course ID:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY));
        top.add(cid); top.add(load);
        p.add(top, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Student ID", "Final Mark", "Grade", "CA Eligible"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.BG_CARD); setHorizontalAlignment(CENTER);
                String g = v != null ? v.toString() : "";
                setForeground(g.startsWith("A") ? UITheme.ACCENT_GREEN : g.startsWith("B") ? UITheme.ACCENT :
                    g.startsWith("C") ? UITheme.ACCENT_YELLOW : UITheme.ACCENT_RED);
                return this;
            }
        });
        p.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        load.addActionListener(e -> {
            model.setRowCount(0);
            for (Map<String, Object> row : markSvc.getBatchSummary(cid.getText().trim()))
                model.addRow(new Object[]{row.get("studentId"),
                    String.format("%.1f", (double) row.get("finalMark")),
                    row.get("grade"), (boolean) row.get("caEligible") ? "✅ YES" : "❌ NO"});
        });
        return p;
    }

    private JPanel buildMarksUpload() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel form = UITheme.card(); form.setLayout(new GridLayout(0, 2, 12, 12));
        JTextField sid  = UITheme.styledField("Student ID");
        JTextField cid  = UITheme.styledField("Course ID");
        JTextField mark = UITheme.styledField("Mark (0-100)");
        JComboBox<String> type = new JComboBox<>(
            new String[]{"QUIZ_1", "QUIZ_2", "QUIZ_3", "ASSESSMENT", "MID_EXAM", "FINAL_EXAM"});
        styleCombo(type);
        JButton addBtn  = UITheme.primaryButton("Upload Mark");
        JLabel  status  = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);

        form.add(lbl("Student ID")); form.add(sid);
        form.add(lbl("Course ID"));  form.add(cid);
        form.add(lbl("Exam Type"));  form.add(type);
        form.add(lbl("Mark"));       form.add(mark);
        form.add(addBtn);            form.add(status);

        addBtn.addActionListener(e -> {
            try {
                double m = Double.parseDouble(mark.getText().trim());
                if (m < 0 || m > 100) { setStatus(status, "❌ Mark must be 0–100.", true); return; }
                boolean ok = markSvc.addMark(sid.getText().trim(), cid.getText().trim(),
                    (String) type.getSelectedItem(), m);
                setStatus(status, ok ? "✅ Mark uploaded." : "❌ Failed.", !ok);
            } catch (NumberFormatException ex) { setStatus(status, "❌ Invalid mark value.", true); }
        });

        p.add(form, BorderLayout.NORTH);
        return p;
    }


    private JPanel buildAttendanceTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); top.setOpaque(false);
        JTextField sidField = UITheme.styledField("Student ID"); sidField.setPreferredSize(new Dimension(180, 36));
        JTextField cidField = UITheme.styledField("Course ID");  cidField.setPreferredSize(new Dimension(180, 36));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"BOTH", "THEORY", "PRACTICAL"});
        styleCombo(typeCombo);
        JButton checkBtn = UITheme.primaryButton("Check");
        top.add(UITheme.label("Student:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY)); top.add(sidField);
        top.add(UITheme.label("Course:",  UITheme.TEXT_SECONDARY, UITheme.FONT_BODY)); top.add(cidField);
        top.add(UITheme.label("Type:",    UITheme.TEXT_SECONDARY, UITheme.FONT_BODY)); top.add(typeCombo);
        top.add(checkBtn);
        p.add(top, BorderLayout.NORTH);


        JPanel resultRow = new JPanel(new GridLayout(1, 3, 12, 0)); resultRow.setOpaque(false);
        JLabel pctLabel  = bigLabel("—", UITheme.ACCENT);
        JLabel eligLabel = bigLabel("—", UITheme.TEXT_SECONDARY);
        JLabel medLabel  = bigLabel("—", UITheme.ACCENT_YELLOW);
        resultRow.add(resultCard("Attendance %",    pctLabel,  UITheme.ACCENT));
        resultRow.add(resultCard("Eligible (≥80%)", eligLabel, UITheme.ACCENT_GREEN));
        resultRow.add(resultCard("With Medicals %", medLabel,  UITheme.ACCENT_YELLOW));
        p.add(resultRow, BorderLayout.CENTER);


        JPanel batchPanel = new JPanel(new BorderLayout(0, 8)); batchPanel.setOpaque(false);
        JPanel batchTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); batchTop.setOpaque(false);
        JTextField batchCid = UITheme.styledField("Course ID for batch"); batchCid.setPreferredSize(new Dimension(200, 36));
        JButton batchBtn = UITheme.secondaryButton("Load Batch Summary");
        batchTop.add(batchCid); batchTop.add(batchBtn);

        DefaultTableModel bModel = new DefaultTableModel(
            new String[]{"Student ID", "Attendance %", "Eligible"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable bTable = new JTable(bModel); styleTable(bTable);
        batchPanel.add(batchTop, BorderLayout.NORTH);
        batchPanel.add(UITheme.scrollPane(bTable), BorderLayout.CENTER);
        p.add(batchPanel, BorderLayout.SOUTH);

        checkBtn.addActionListener(e -> {
            String sid = sidField.getText().trim(); String cid = cidField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            if (sid.isEmpty() || cid.isEmpty()) { UITheme.showMessage(contentPanel, "Enter Student ID and Course ID.", "Error", true); return; }
            double pct  = attSvc.getAttendancePercentage(sid, cid, type);
            double mPct = attSvc.getAttendanceWithMedicals(sid, cid);
            boolean elig = attSvc.isEligible(sid, cid);
            pctLabel.setText(String.format("%.1f%%", pct));
            medLabel.setText(String.format("%.1f%%", mPct));
            eligLabel.setText(elig ? "✅ Eligible" : "❌ Not Eligible");
            eligLabel.setForeground(elig ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        });

        batchBtn.addActionListener(e -> {
            bModel.setRowCount(0);
            attSvc.getBatchSummary(batchCid.getText().trim())
                .forEach((sid, pct) -> bModel.addRow(new Object[]{
                    sid, String.format("%.1f%%", pct), pct >= 80 ? "✅ YES" : "❌ NO"}));
        });

        return p;
    }


    private JPanel buildMedicalTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); top.setOpaque(false);
        JTextField sidFilter = UITheme.styledField("Filter by Student ID (leave blank for all)");
        sidFilter.setPreferredSize(new Dimension(300, 36));
        JButton loadBtn = UITheme.primaryButton("Load");
        top.add(sidFilter); top.add(loadBtn);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Student ID", "From Date", "To Date", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model); styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        p.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        Runnable loadAll = () -> {
            model.setRowCount(0);
            medSvc.getAll().forEach(m -> model.addRow(new Object[]{
                m.getMedicalId(), m.getStudentId(), m.getSDate(), m.getEDate(), m.getDescription()
            }));
        };

        loadBtn.addActionListener(e -> {
            String sid = sidFilter.getText().trim();
            if (sid.isEmpty()) { loadAll.run(); return; }
            model.setRowCount(0);
            medSvc.getByStudent(sid).forEach(m -> model.addRow(new Object[]{
                m.getMedicalId(), m.getStudentId(), m.getSDate(), m.getEDate(), m.getDescription()
            }));
        });
        loadAll.run();
        return p;
    }


    private List<Map<String, Object>> getAllStudents() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(
                "SELECT Reg_no, Fullname, Dep_id, Type FROM Student ORDER BY Reg_no");
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("student_id", rs.getString("Reg_no"));
                m.put("fullname",   rs.getString("Fullname"));
                m.put("dep_id",     rs.getString("Dep_id"));
                m.put("type",       rs.getString("Type"));
                list.add(m);
            }
        } catch (SQLException e) { System.err.println("LecturerStudentView.getAllStudents: " + e.getMessage()); }
        return list;
    }

    private String studentType(Map<String, Object> s) {
        String type = (String) s.getOrDefault("type", "Proper");
        return type != null ? type : "Proper";
    }

    private JPanel resultCard(String title, JLabel val, Color accent) {
        JPanel c = UITheme.card(); c.setLayout(new BorderLayout(0, 8));
        val.setHorizontalAlignment(SwingConstants.CENTER);
        c.add(UITheme.label(title, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), BorderLayout.NORTH);
        c.add(val, BorderLayout.CENTER);
        return c;
    }

    private JLabel bigLabel(String text, Color color) {
        return UITheme.label(text, color, new Font("Segoe UI", Font.BOLD, 22));
    }

    private JLabel lbl(String text) { return UITheme.label(text, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL); }

    private void styleTable(JTable t) {
        t.setBackground(UITheme.BG_CARD); t.setForeground(UITheme.TEXT_PRIMARY);
        t.setFont(UITheme.FONT_BODY); t.setRowHeight(36); t.setGridColor(UITheme.BORDER_COLOR);
        t.setSelectionBackground(new Color(56, 189, 248, 40)); t.setSelectionForeground(UITheme.ACCENT);
        t.getTableHeader().setBackground(UITheme.BG_DARK); t.getTableHeader().setForeground(UITheme.ACCENT);
        t.getTableHeader().setFont(UITheme.FONT_SUBTITLE); t.getTableHeader().setReorderingAllowed(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setForeground(UITheme.TEXT_PRIMARY); setFont(UITheme.FONT_BODY);
                setBackground(sel ? new Color(56, 189, 248, 40) :
                    r % 2 == 0 ? UITheme.BG_CARD : new Color(28, 35, 44));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(UITheme.BG_CARD); c.setForeground(UITheme.TEXT_PRIMARY); c.setFont(UITheme.FONT_BODY);
    }

    private void setStatus(JLabel l, String msg, boolean err) {
        l.setText(msg); l.setForeground(err ? UITheme.ACCENT_RED : UITheme.ACCENT_GREEN);
    }

    private String str(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? v.toString() : ""; }
    private String orDash(String s) { return (s == null || s.isEmpty()) ? "—" : s; }
}
