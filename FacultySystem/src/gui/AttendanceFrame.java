package gui;

import model.Student;
import model.User;
import service.AttendanceService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class AttendanceFrame extends JFrame {

    private final AttendanceService svc = new AttendanceService();
    private final User user;
    private JPanel contentPanel;

    public AttendanceFrame(User user) { this.user = user; buildPanel(); }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0,16));
        contentPanel.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.label("Attendance Management", UITheme.TEXT_PRIMARY, new Font("Segoe UI",Font.BOLD,20)), BorderLayout.WEST);
        contentPanel.add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.setFont(UITheme.FONT_BODY);
        tabs.addTab("Individual Check", buildIndividualPanel());
        tabs.addTab("Batch Summary", buildBatchPanel());
        if(!user.getRole().equals("STUDENT")) tabs.addTab("Add Attendance", buildAddPanel());
        contentPanel.add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildIndividualPanel() {
        JPanel p = new JPanel(new BorderLayout(0,16)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));

        JPanel form = UITheme.card(); form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(6,8,6,8); gbc.fill=GridBagConstraints.HORIZONTAL;

        JTextField sidField = UITheme.styledField("Student ID");
        JTextField cidField = UITheme.styledField("Course ID");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"BOTH","THEORY","PRACTICAL"});
        typeCombo.setBackground(UITheme.BG_CARD); typeCombo.setForeground(UITheme.TEXT_PRIMARY);
        JButton checkBtn = UITheme.primaryButton("Check");

        // If student, pre-fill
        if(user.getRole().equals("STUDENT") && user instanceof Student s) {
            sidField.setText(s.getStudentId()); sidField.setEditable(false);
        }

        gbc.gridx=0; gbc.gridy=0; form.add(UITheme.label("Student ID", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), gbc);
        gbc.gridx=1; form.add(UITheme.label("Course ID",  UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), gbc);
        gbc.gridx=2; form.add(UITheme.label("Session Type", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), gbc);

        gbc.gridy=1; gbc.gridx=0; sidField.setPreferredSize(new Dimension(160,38)); form.add(sidField, gbc);
        gbc.gridx=1; cidField.setPreferredSize(new Dimension(160,38)); form.add(cidField, gbc);
        gbc.gridx=2; form.add(typeCombo, gbc);
        gbc.gridx=3; form.add(checkBtn, gbc);

        // Result cards
        JPanel resultRow = new JPanel(new GridLayout(1,3,12,0)); resultRow.setOpaque(false);
        JLabel pctLabel  = UITheme.label("—", UITheme.ACCENT,        new Font("Segoe UI",Font.BOLD,22));
        JLabel eligLabel = UITheme.label("—", UITheme.TEXT_SECONDARY, new Font("Segoe UI",Font.BOLD,16));
        JLabel medLabel  = UITheme.label("—", UITheme.ACCENT_YELLOW,  new Font("Segoe UI",Font.BOLD,22));

        resultRow.add(resultCard("Attendance %",       pctLabel,  UITheme.ACCENT));
        resultRow.add(resultCard("Eligible (≥80%)",    eligLabel, UITheme.ACCENT_GREEN));
        resultRow.add(resultCard("With Medicals %",    medLabel,  UITheme.ACCENT_YELLOW));

        checkBtn.addActionListener(e -> {
            String sid = sidField.getText().trim(); String cid = cidField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            if(sid.isEmpty()||cid.isEmpty()){ UITheme.showMessage(contentPanel,"Fill in Student ID and Course ID","Error",true); return; }
            double pct  = svc.getAttendancePercentage(sid,cid,type);
            double mPct = svc.getAttendanceWithMedicals(sid,cid);
            boolean elig = svc.isEligible(sid,cid);
            pctLabel.setText(String.format("%.1f%%", pct));
            medLabel.setText(String.format("%.1f%%", mPct));
            eligLabel.setText(elig ? "✅  Eligible" : "❌  Not Eligible");
            eligLabel.setForeground(elig ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        });

        p.add(form, BorderLayout.NORTH);
        p.add(resultRow, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildBatchPanel() {
        JPanel p = new JPanel(new BorderLayout(0,12)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); top.setOpaque(false);
        JTextField cidField = UITheme.styledField("Course ID"); cidField.setPreferredSize(new Dimension(200,38));
        JButton loadBtn = UITheme.primaryButton("Load Batch");
        top.add(UITheme.label("Course ID:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY));
        top.add(cidField); top.add(loadBtn);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"Student ID","Attendance %","Eligible"};
        DefaultTableModel model = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable table = new JTable(model);
        styleTable(table);
        p.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            model.setRowCount(0);
            Map<String,Double> map = svc.getBatchSummary(cidField.getText().trim());
            map.forEach((sid,pct) -> model.addRow(new Object[]{sid, String.format("%.1f%%",pct), pct>=80?"✅ YES":"❌ NO"}));
        });
        return p;
    }

    private JPanel buildAddPanel() {
        JPanel p = new JPanel(new BorderLayout(0,12)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
        JPanel form = UITheme.card(); form.setLayout(new GridLayout(0,2,12,12));
        JTextField sid   = UITheme.styledField("Student ID");
        JTextField cid   = UITheme.styledField("Course ID");
        JTextField hours = UITheme.styledField("Hours (e.g. 2)");
        JComboBox<String> stype = new JComboBox<>(new String[]{"Lecture","Practical"});
        stype.setBackground(UITheme.BG_CARD); stype.setForeground(UITheme.TEXT_PRIMARY);
        JComboBox<String> present = new JComboBox<>(new String[]{"Present","Absent"});
        present.setBackground(UITheme.BG_CARD); present.setForeground(UITheme.TEXT_PRIMARY);
        JTextField date = UITheme.styledField("Date (YYYY-MM-DD)");
        JButton addBtn = UITheme.primaryButton("Add Record");
        JLabel status = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);

        form.add(UITheme.label("Student ID",  UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(sid);
        form.add(UITheme.label("Course ID",   UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(cid);
        form.add(UITheme.label("Hours",       UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(hours);
        form.add(UITheme.label("Session Type",UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(stype);
        form.add(UITheme.label("Presence",    UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(present);
        form.add(UITheme.label("Date",        UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(date);
        form.add(addBtn); form.add(status);

        addBtn.addActionListener(e -> {
            try {
                int h = Integer.parseInt(hours.getText().trim());
                boolean ok = svc.addAttendance(sid.getText().trim(), cid.getText().trim(),
                    date.getText().trim(), (String)stype.getSelectedItem(),
                    (String)present.getSelectedItem(), h);
                status.setForeground(ok?UITheme.ACCENT_GREEN:UITheme.ACCENT_RED);
                status.setText(ok?"✅ Record added successfully":"❌ Failed to add record");
            } catch(NumberFormatException ex){ status.setForeground(UITheme.ACCENT_RED); status.setText("❌ Invalid hours value"); }
        });
        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private JPanel resultCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = UITheme.card(); card.setLayout(new BorderLayout(0,8));
        JLabel t = UITheme.label(title, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(t, BorderLayout.NORTH); card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void styleTable(JTable t) {
        t.setBackground(UITheme.BG_CARD); t.setForeground(UITheme.TEXT_PRIMARY);
        t.setFont(UITheme.FONT_BODY); t.setRowHeight(36); t.setGridColor(UITheme.BORDER_COLOR);
        t.setSelectionBackground(new Color(56,189,248,40)); t.setSelectionForeground(UITheme.ACCENT);
        t.getTableHeader().setBackground(UITheme.BG_DARK); t.getTableHeader().setForeground(UITheme.ACCENT);
        t.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
    }
}
