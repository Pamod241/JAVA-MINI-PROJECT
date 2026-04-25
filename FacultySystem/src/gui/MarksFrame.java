package gui;

import model.Student;
import model.User;
import service.MarksService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MarksFrame extends JFrame {

    private final MarksService svc = new MarksService();
    private final User user;
    private JPanel contentPanel;

    public MarksFrame(User user) { this.user = user; buildPanel(); }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0,16)); contentPanel.setOpaque(false);
        contentPanel.add(UITheme.label("Marks, Grades & GPA", UITheme.TEXT_PRIMARY, new Font("Segoe UI",Font.BOLD,20)), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_CARD); tabs.setForeground(UITheme.TEXT_PRIMARY); tabs.setFont(UITheme.FONT_BODY);
        tabs.addTab("Individual", buildIndividualPanel());
        tabs.addTab("Batch Summary", buildBatchPanel());
        if(!user.getRole().equals("STUDENT")) tabs.addTab("Upload Marks", buildUploadPanel());
        contentPanel.add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildIndividualPanel() {
        JPanel p = new JPanel(new BorderLayout(0,16)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));

        // ── Input form ────────────────────────────────────────────────────────
        JPanel form = UITheme.card(); form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(6,8,6,8); gbc.fill=GridBagConstraints.HORIZONTAL;

        JTextField sidField = UITheme.styledField("Student ID");
        JTextField cidField = UITheme.styledField("Course ID");
        JButton checkBtn    = UITheme.primaryButton("Check Marks");
        JButton gpaBtn      = UITheme.secondaryButton("Calc SGPA / CGPA");

        if (user.getRole().equals("STUDENT") && user instanceof Student s) {
            sidField.setText(s.getStudentId()); sidField.setEditable(false);
        }

        gbc.gridx=0; gbc.gridy=0; form.add(UITheme.label("Student ID", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), gbc);
        gbc.gridx=1; form.add(UITheme.label("Course ID", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), gbc);
        gbc.gridy=1; gbc.gridx=0; sidField.setPreferredSize(new Dimension(160,38)); form.add(sidField, gbc);
        gbc.gridx=1; cidField.setPreferredSize(new Dimension(160,38)); form.add(cidField, gbc);
        gbc.gridx=2; form.add(checkBtn, gbc);
        gbc.gridx=3; form.add(gpaBtn, gbc);
        p.add(form, BorderLayout.NORTH);

        // Row 1: CA Mark, Final Mark, Grade
        JPanel marksRow = new JPanel(new GridLayout(1,3,12,0)); marksRow.setOpaque(false);
        JLabel caLabel    = UITheme.label("—", UITheme.ACCENT,        new Font("Segoe UI",Font.BOLD,22));
        JLabel finalLabel = UITheme.label("—", UITheme.ACCENT_GREEN,  new Font("Segoe UI",Font.BOLD,22));
        JLabel gradeLabel = UITheme.label("—", UITheme.ACCENT_YELLOW, new Font("Segoe UI",Font.BOLD,28));
        marksRow.add(resultCard("CA Mark",    caLabel,    UITheme.ACCENT));
        marksRow.add(resultCard("Final Mark", finalLabel, UITheme.ACCENT_GREEN));
        marksRow.add(resultCard("Grade",      gradeLabel, UITheme.ACCENT_YELLOW));

        // Row 2: SGPA (per semester) + CGPA
        JPanel gpaRow = new JPanel(new GridLayout(1,2,12,0)); gpaRow.setOpaque(false);
        JLabel sgpaLabel = UITheme.label("—", UITheme.ACCENT_PURPLE, new Font("Segoe UI",Font.BOLD,22));
        JLabel cgpaLabel = UITheme.label("—", new Color(251,113,133), new Font("Segoe UI",Font.BOLD,22));
        gpaRow.add(resultCard("SGPA (this semester)", sgpaLabel, UITheme.ACCENT_PURPLE));
        gpaRow.add(resultCard("CGPA (all semesters)", cgpaLabel, new Color(251,113,133)));

        JPanel center = new JPanel(new GridLayout(2,1,0,12)); center.setOpaque(false);
        center.add(marksRow);
        center.add(gpaRow);
        p.add(center, BorderLayout.CENTER);

        //  Check Marks button
        checkBtn.addActionListener(e -> {
            String sid = sidField.getText().trim();
            String cid = cidField.getText().trim();
            if (sid.isEmpty() || cid.isEmpty()) {
                UITheme.showMessage(contentPanel, "Fill in Student ID and Course ID", "Error", true); return;
            }
            double ca     = svc.getCAMark(sid, cid);
            double final_ = svc.getFinalMark(sid, cid);
            String grade  = svc.getGrade(final_);
            boolean elig  = svc.isCAEligible(sid, cid);
            caLabel.setText(String.format("%.1f", ca));
            caLabel.setForeground(elig ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
            finalLabel.setText(String.format("%.1f", final_));
            gradeLabel.setText(grade);
        });

        //  SGPA / CGPA button
        gpaBtn.addActionListener(e -> {
            String sid = sidField.getText().trim();
            if (sid.isEmpty()) {
                UITheme.showMessage(contentPanel, "Enter Student ID", "Error", true); return;
            }

            // Get semesters the student has marks in
            List<Integer> sems = svc.getStudentSemesters(sid);

            if (sems.isEmpty()) {
                sgpaLabel.setText("No data");
                cgpaLabel.setText("No data");
                return;
            }

            // SGPA — show latest semester
            int latestSem  = sems.get(sems.size() - 1);
            double sgpa    = svc.calculateSGPA(sid, latestSem);
            sgpaLabel.setText(String.format("%.2f", sgpa) + "  (Sem " + latestSem + ")");

            // CGPA — all semesters average
            double cgpa = svc.calculateCGPA(sid);
            cgpaLabel.setText(String.format("%.2f", cgpa));
        });

        return p;
    }

    private JPanel buildBatchPanel() {
        JPanel p = new JPanel(new BorderLayout(0,12)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); top.setOpaque(false);
        JTextField cid = UITheme.styledField("Course ID"); cid.setPreferredSize(new Dimension(200,38));
        JButton load = UITheme.primaryButton("Load Batch");
        top.add(UITheme.label("Course ID:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY));
        top.add(cid); top.add(load);
        p.add(top, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Student ID","Final Mark","Grade","CA Eligible"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table = new JTable(model); styleTable(table);

        // Color-code grade column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c) {
                super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                String grade = v!=null?v.toString():"";
                setForeground(grade.startsWith("A")?UITheme.ACCENT_GREEN : grade.startsWith("B")?UITheme.ACCENT :
                    grade.startsWith("C")?UITheme.ACCENT_YELLOW : UITheme.ACCENT_RED);
                setBackground(UITheme.BG_CARD); setHorizontalAlignment(CENTER);
                return this;
            }
        });

        p.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        load.addActionListener(e -> {
            model.setRowCount(0);
            List<Map<String,Object>> list = svc.getBatchSummary(cid.getText().trim());
            for(Map<String,Object> row : list)
                model.addRow(new Object[]{row.get("studentId"), String.format("%.1f",(double)row.get("finalMark")),
                    row.get("grade"), (boolean)row.get("caEligible")?"✅ YES":"❌ NO"});
        });
        return p;
    }

    private JPanel buildUploadPanel() {
        JPanel p = new JPanel(new BorderLayout(0,12)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
        JPanel form = UITheme.card(); form.setLayout(new GridLayout(0,2,12,12));
        JTextField sid  = UITheme.styledField("Student ID");
        JTextField cid  = UITheme.styledField("Course ID");
        JTextField mark = UITheme.styledField("Mark (0-100)");
        JComboBox<String> type = new JComboBox<>(new String[]{"QUIZ_1","QUIZ_2","QUIZ_3","ASSESSMENT","MID_EXAM","FINAL_EXAM"});
        type.setBackground(UITheme.BG_CARD); type.setForeground(UITheme.TEXT_PRIMARY);
        JButton addBtn = UITheme.primaryButton("Upload Mark");
        JLabel status  = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);

        form.add(UITheme.label("Student ID", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(sid);
        form.add(UITheme.label("Course ID",  UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(cid);
        form.add(UITheme.label("Exam Type",  UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(type);
        form.add(UITheme.label("Mark",       UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); form.add(mark);
        form.add(addBtn); form.add(status);

        addBtn.addActionListener(e -> {
            try {
                double m = Double.parseDouble(mark.getText().trim());
                if(m<0||m>100){ status.setForeground(UITheme.ACCENT_RED); status.setText("❌ Mark must be 0–100"); return; }
                boolean ok = svc.addMark(sid.getText().trim(), cid.getText().trim(), (String)type.getSelectedItem(), m);
                status.setForeground(ok?UITheme.ACCENT_GREEN:UITheme.ACCENT_RED);
                status.setText(ok?"✅ Mark uploaded successfully":"❌ Failed");
            } catch(NumberFormatException ex){ status.setForeground(UITheme.ACCENT_RED); status.setText("❌ Invalid mark value"); }
        });
        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private JPanel resultCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = UITheme.card(); card.setLayout(new BorderLayout(0,8));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(UITheme.label(title, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
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
