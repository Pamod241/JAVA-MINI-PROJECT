package gui;

import model.User;
import service.CourseService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class CourseManagementFrame extends JFrame {

    private final CourseService svc = new CourseService();
    private final User currentUser;
    private JPanel contentPanel;

    public CourseManagementFrame(User currentUser) {
        this.currentUser = currentUser;
        buildPanel();
    }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);


        contentPanel.add(UITheme.label("Course Management", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 20)), BorderLayout.NORTH);


        String[] cols = {"Course ID", "Course Name", "Credits", "Type", "Lecturer ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getColumnModel().getColumn(2).setMaxWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);


        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.BG_CARD);
                String type = v != null ? v.toString() : "";
                setForeground(switch (type) {
                    case "THEORY"    -> UITheme.ACCENT;
                    case "PRACTICAL" -> UITheme.ACCENT_GREEN;
                    case "BOTH"      -> UITheme.ACCENT_YELLOW;
                    default -> UITheme.TEXT_SECONDARY;
                });
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });


        JPanel formCard = UITheme.card();
        formCard.setLayout(new BorderLayout(0, 12));

        JLabel formTitle = UITheme.label("➕  Create New Course", UITheme.ACCENT, UITheme.FONT_SUBTITLE);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 6, 10, 8));
        fields.setOpaque(false);

        JTextField fCourseId   = UITheme.styledField("e.g. ICT2106");
        JTextField fCourseName = UITheme.styledField("e.g. Data Structures");
        JTextField fCredits    = UITheme.styledField("Credits");
        JTextField fDept       = UITheme.styledField("Lecturer ID (optional)");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"BOTH", "THEORY", "PRACTICAL"});
        styleCombo(typeCombo);

        fields.add(label("Course ID"));   fields.add(fCourseId);
        fields.add(label("Course Name")); fields.add(fCourseName);
        fields.add(label("Credits"));     fields.add(fCredits);
        fields.add(label("Type"));        fields.add(typeCombo);
        fields.add(label("Lecturer ID")); fields.add(fDept);
        fields.add(new JLabel());         fields.add(new JLabel());

        formCard.add(fields, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);

        JButton createBtn = UITheme.primaryButton("✅  Create Course");
        JButton updateBtn = UITheme.secondaryButton("💾  Update Selected");
        JButton deleteBtn = UITheme.dangerButton("🗑️  Delete Selected");
        JButton clearBtn  = UITheme.secondaryButton("🔄  Clear");
        JLabel  statusLbl = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);

        btnRow.add(createBtn); btnRow.add(updateBtn); btnRow.add(deleteBtn);
        btnRow.add(clearBtn);  btnRow.add(statusLbl);
        formCard.add(btnRow, BorderLayout.SOUTH);


        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            statusLbl.setText("");
            formTitle.setText("✏️  Edit Course: " + model.getValueAt(row, 0));
            fCourseId.setText(model.getValueAt(row, 0).toString());
            fCourseId.setEditable(false); // can't change primary key
            fCourseName.setText(model.getValueAt(row, 1).toString());
            fCredits.setText(model.getValueAt(row, 2).toString());
            typeCombo.setSelectedItem(model.getValueAt(row, 3).toString());
            fDept.setText(orEmpty(model.getValueAt(row, 4).toString()));
        });


        Runnable loadAll = () -> loadTable(model);
        loadAll.run();

        createBtn.addActionListener(e -> {
            String courseId   = fCourseId.getText().trim();
            String courseName = fCourseName.getText().trim();
            String creditsStr = fCredits.getText().trim();
            String dept       = fDept.getText().trim();
            String type       = (String) typeCombo.getSelectedItem();

            if (courseId.isEmpty() || courseName.isEmpty() || creditsStr.isEmpty()) {
                setStatus(statusLbl, "❌ Course ID, Name and Credits are required.", true); return;
            }
            int credits;
            try { credits = Integer.parseInt(creditsStr); }
            catch (NumberFormatException ex) { setStatus(statusLbl, "❌ Credits must be a number.", true); return; }

            fCourseId.setEditable(true); // allow new ID
            String err = svc.create(courseId, courseName, credits, type, dept);
            if (err == null) {
                setStatus(statusLbl, "✅ Course created successfully!", false);
                clearForm(fCourseId, fCourseName, fCredits, fDept, typeCombo, formTitle);
                loadAll.run();
            } else {
                setStatus(statusLbl, "❌ " + err, true);
            }
        });

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { setStatus(statusLbl, "❌ Select a course to update.", true); return; }
            String courseId   = fCourseId.getText().trim();
            String courseName = fCourseName.getText().trim();
            String creditsStr = fCredits.getText().trim();
            String dept       = fDept.getText().trim();
            String type       = (String) typeCombo.getSelectedItem();

            if (courseName.isEmpty() || creditsStr.isEmpty()) {
                setStatus(statusLbl, "❌ Course Name and Credits are required.", true); return;
            }
            int credits;
            try { credits = Integer.parseInt(creditsStr); }
            catch (NumberFormatException ex) { setStatus(statusLbl, "❌ Credits must be a number.", true); return; }

            String err = svc.update(courseId, courseName, credits, type, dept);
            if (err == null) {
                setStatus(statusLbl, "✅ Course updated.", false);
                clearForm(fCourseId, fCourseName, fCredits, fDept, typeCombo, formTitle);
                loadAll.run();
            } else {
                setStatus(statusLbl, "❌ " + err, true);
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { setStatus(statusLbl, "❌ Select a course to delete.", true); return; }
            String courseId = model.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(contentPanel,
                "Delete course \"" + courseId + "\"?\nThis will fail if attendance/marks records exist.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            String err = svc.delete(courseId);
            if (err == null) {
                setStatus(statusLbl, "✅ Course deleted.", false);
                clearForm(fCourseId, fCourseName, fCredits, fDept, typeCombo, formTitle);
                loadAll.run();
            } else {
                setStatus(statusLbl, "❌ " + err, true);
            }
        });

        clearBtn.addActionListener(e -> {
            table.clearSelection();
            clearForm(fCourseId, fCourseName, fCredits, fDept, typeCombo, formTitle);
            statusLbl.setText("");
        });


        JPanel summaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        summaryRow.setOpaque(false);
        JLabel summaryLbl = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
        JButton refreshBtn = UITheme.secondaryButton("🔄  Refresh");
        summaryRow.add(refreshBtn); summaryRow.add(summaryLbl);

        refreshBtn.addActionListener(e -> {
            loadAll.run();
            summaryLbl.setText("Total courses: " + model.getRowCount());
        });
        summaryLbl.setText("Total courses: 0");
        loadAll.run();
        summaryLbl.setText("Total courses: " + model.getRowCount());


        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            UITheme.scrollPane(table), formCard);
        split.setOpaque(false);
        split.setBackground(UITheme.BG_DARK);
        split.setDividerLocation(300);
        split.setDividerSize(5);
        split.setBorder(null);

        contentPanel.add(summaryRow, BorderLayout.AFTER_LAST_LINE);
        contentPanel.add(split, BorderLayout.CENTER);
    }

    private void loadTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Map<String, Object> c : svc.getAll()) {
            model.addRow(new Object[]{
                c.get("course_id"),
                c.get("course_name"),
                c.get("credits"),
                c.get("type"),
                orDash(str(c, "lec_id"))
            });
        }
    }

    private void clearForm(JTextField id, JTextField name, JTextField credits,
                            JTextField dept, JComboBox<?> type, JLabel title) {
        id.setText(""); id.setEditable(true);
        name.setText(""); credits.setText(""); dept.setText("");
        type.setSelectedIndex(0);
        title.setText("➕  Create New Course");
    }


    private JLabel label(String text) {
        return UITheme.label(text, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
    }

    private void styleTable(JTable t) {
        t.setBackground(UITheme.BG_CARD);
        t.setForeground(UITheme.TEXT_PRIMARY);
        t.setFont(UITheme.FONT_BODY);
        t.setRowHeight(36);
        t.setGridColor(UITheme.BORDER_COLOR);
        t.setSelectionBackground(new Color(56, 189, 248, 40));
        t.setSelectionForeground(UITheme.ACCENT);
        t.getTableHeader().setBackground(UITheme.BG_DARK);
        t.getTableHeader().setForeground(UITheme.ACCENT);
        t.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        t.getTableHeader().setReorderingAllowed(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setForeground(UITheme.TEXT_PRIMARY);
                setFont(UITheme.FONT_BODY);
                setBackground(sel ? new Color(56, 189, 248, 40) :
                    r % 2 == 0 ? UITheme.BG_CARD : new Color(28, 35, 44));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(UITheme.BG_CARD);
        c.setForeground(UITheme.TEXT_PRIMARY);
        c.setFont(UITheme.FONT_BODY);
    }

    private void setStatus(JLabel lbl, String msg, boolean isError) {
        lbl.setText(msg);
        lbl.setForeground(isError ? UITheme.ACCENT_RED : UITheme.ACCENT_GREEN);
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key); return v != null ? v.toString() : "";
    }

    private String orDash(String s)  { return (s == null || s.isEmpty() || s.equals("null")) ? "—" : s; }
    private String orEmpty(String s) { return (s == null || s.equals("null") || s.equals("—")) ? "" : s; }
}
