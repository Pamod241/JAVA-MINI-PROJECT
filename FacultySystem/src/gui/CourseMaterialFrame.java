package gui;

import model.User;
import service.CourseMaterialService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class CourseMaterialFrame extends JFrame {

    private final CourseMaterialService svc = new CourseMaterialService();
    private final User user;
    private JPanel contentPanel;

    public CourseMaterialFrame(User user) {
        this.user = user;
        buildPanel();
    }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);
        contentPanel.add(UITheme.label("Course Materials", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 20)), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.setFont(UITheme.FONT_BODY);
        tabs.addTab("📂  Browse Materials", buildBrowsePanel());
        tabs.addTab("➕  Add Material",     buildAddPanel());

        contentPanel.add(tabs, BorderLayout.CENTER);
    }


    private JPanel buildBrowsePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        List<String> courseIds = svc.getAllCourseIds();
        String[] courseArr = new String[courseIds.size() + 1];
        courseArr[0] = "All Courses";
        for (int i = 0; i < courseIds.size(); i++) courseArr[i + 1] = courseIds.get(i);

        JComboBox<String> courseFilter = new JComboBox<>(courseArr);
        styleCombo(courseFilter);
        courseFilter.setPreferredSize(new Dimension(180, 36));

        JButton loadBtn   = UITheme.primaryButton("🔍  Filter");
        JButton refreshBtn = UITheme.secondaryButton("🔄  All");
        filterBar.add(UITheme.label("Course:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY));
        filterBar.add(courseFilter); filterBar.add(loadBtn); filterBar.add(refreshBtn);
        p.add(filterBar, BorderLayout.NORTH);


        String[] cols = {"ID", "Course", "Title", "Type", "Description", "File / URL", "Uploaded By", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);


        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.BG_CARD);
                String type = v != null ? v.toString() : "";
                setForeground(switch (type) {
                    case "LECTURE_NOTE" -> UITheme.ACCENT;
                    case "ASSIGNMENT"   -> UITheme.ACCENT_YELLOW;
                    case "LAB_SHEET"    -> UITheme.ACCENT_GREEN;
                    case "REFERENCE"    -> UITheme.ACCENT_PURPLE;
                    default             -> UITheme.TEXT_SECONDARY;
                });
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });


        JPanel editPanel = buildEditPanel(table, model);
        editPanel.setVisible(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (table.getSelectedRow() >= 0) editPanel.setVisible(true);
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            UITheme.scrollPane(table), editPanel);
        split.setOpaque(false); split.setBackground(UITheme.BG_DARK);
        split.setDividerLocation(320); split.setDividerSize(5); split.setBorder(null);

        p.add(split, BorderLayout.CENTER);

        Runnable loadAll = () -> {
            model.setRowCount(0);
            List<Map<String, Object>> data = svc.getAll();
            for (Map<String, Object> m : data)
                model.addRow(new Object[]{
                    m.get("material_id"), m.get("course_id"), m.get("title"),
                    m.get("material_type"), orDash(str(m, "description")),
                    orDash(str(m, "file_url")), m.get("uploader_name"), m.get("uploaded_at")
                });
        };
        loadAll.run();

        loadBtn.addActionListener(e -> {
            String selected = (String) courseFilter.getSelectedItem();
            if ("All Courses".equals(selected)) { loadAll.run(); return; }
            model.setRowCount(0);
            for (Map<String, Object> m : svc.getByCourse(selected))
                model.addRow(new Object[]{
                    m.get("material_id"), m.get("course_id"), m.get("title"),
                    m.get("material_type"), orDash(str(m, "description")),
                    orDash(str(m, "file_url")), m.get("uploader_name"), m.get("uploaded_at")
                });
        });
        refreshBtn.addActionListener(e -> { courseFilter.setSelectedIndex(0); loadAll.run(); });

        return p;
    }


    private JPanel buildEditPanel(JTable table, DefaultTableModel model) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.label("✏️  Edit Selected Material", UITheme.ACCENT, UITheme.FONT_SUBTITLE),
            BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 4, 10, 8));
        fields.setOpaque(false);

        JTextField fTitle = UITheme.styledField("Title");
        JTextField fDesc  = UITheme.styledField("Description");
        JTextField fUrl   = UITheme.styledField("File / URL");
        JComboBox<String> typeCombo = new JComboBox<>(
            new String[]{"LECTURE_NOTE", "ASSIGNMENT", "LAB_SHEET", "REFERENCE", "OTHER"});
        styleCombo(typeCombo);

        fields.add(lbl("Title"));       fields.add(fTitle);
        fields.add(lbl("Type"));        fields.add(typeCombo);
        fields.add(lbl("Description")); fields.add(fDesc);
        fields.add(lbl("File / URL"));  fields.add(fUrl);
        card.add(fields, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton saveBtn   = UITheme.primaryButton("💾  Save");
        JButton deleteBtn = UITheme.dangerButton("🗑️  Delete");
        JLabel  statusLbl = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
        btnRow.add(statusLbl); btnRow.add(saveBtn); btnRow.add(deleteBtn);
        card.add(btnRow, BorderLayout.SOUTH);
        wrapper.add(card);


        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            statusLbl.setText("");
            fTitle.setText(str(model, row, 2));
            typeCombo.setSelectedItem(str(model, row, 3));
            fDesc.setText(dashToEmpty(str(model, row, 4)));
            fUrl.setText(dashToEmpty(str(model, row, 5)));
        });

        saveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String mid = (String) model.getValueAt(row, 0);
            String err = svc.update(mid, fTitle.getText().trim(), fDesc.getText().trim(),
                fUrl.getText().trim(), (String) typeCombo.getSelectedItem());
            if (err == null) {
                setStatus(statusLbl, "✅ Updated.", false);
                // Refresh browse list via re-loading all
                model.setRowCount(0);
                for (Map<String, Object> m : svc.getAll())
                    model.addRow(new Object[]{
                        m.get("material_id"), m.get("course_id"), m.get("title"),
                        m.get("material_type"), orDash(str(m, "description")),
                        orDash(str(m, "file_url")), m.get("uploader_name"), m.get("uploaded_at")
                    });
            } else setStatus(statusLbl, "❌ " + err, true);
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String mid = (String) model.getValueAt(row, 0);
            String title = str(model, row, 2);
            int ok = JOptionPane.showConfirmDialog(contentPanel,
                "Delete \"" + title + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            String err = svc.delete(mid, user.getProfileId());
            if (err == null) {
                setStatus(statusLbl, "✅ Deleted.", false);
                model.setRowCount(0);
                for (Map<String, Object> m : svc.getAll())
                    model.addRow(new Object[]{
                        m.get("material_id"), m.get("course_id"), m.get("title"),
                        m.get("material_type"), orDash(str(m, "description")),
                        orDash(str(m, "file_url")), m.get("uploader_name"), m.get("uploaded_at")
                    });
                wrapper.setVisible(false);
            } else setStatus(statusLbl, "❌ " + err, true);
        });

        return wrapper;
    }


    private JPanel buildAddPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        List<String> courseIds = svc.getAllCourseIds();
        JComboBox<String> courseCombo = new JComboBox<>(courseIds.toArray(new String[0]));
        styleCombo(courseCombo);
        courseCombo.setPreferredSize(new Dimension(220, 38));

        JComboBox<String> typeCombo = new JComboBox<>(
            new String[]{"LECTURE_NOTE", "ASSIGNMENT", "LAB_SHEET", "REFERENCE", "OTHER"});
        styleCombo(typeCombo);

        JTextField fTitle = UITheme.styledField("Material title (e.g. Week 3 Lecture Notes)");
        JTextField fDesc  = UITheme.styledField("Short description (optional)");
        JTextField fUrl   = UITheme.styledField("File path or URL (optional)");

        fTitle.setPreferredSize(new Dimension(400, 38));
        fDesc.setPreferredSize(new Dimension(400, 38));
        fUrl.setPreferredSize(new Dimension(400, 38));

        int row = 0;
        gbc.gridy = row++; gbc.gridx = 0; card.add(lbl("Course"),      gbc);
        gbc.gridx = 1; card.add(courseCombo, gbc);

        gbc.gridy = row++; gbc.gridx = 0; card.add(lbl("Type"),        gbc);
        gbc.gridx = 1; card.add(typeCombo, gbc);

        gbc.gridy = row++; gbc.gridx = 0; card.add(lbl("Title *"),     gbc);
        gbc.gridx = 1; card.add(fTitle, gbc);

        gbc.gridy = row++; gbc.gridx = 0; card.add(lbl("Description"), gbc);
        gbc.gridx = 1; card.add(fDesc, gbc);

        gbc.gridy = row++; gbc.gridx = 0; card.add(lbl("File / URL"),  gbc);
        gbc.gridx = 1; card.add(fUrl, gbc);

        JButton addBtn    = UITheme.primaryButton("➕  Add Material");
        JButton clearBtn  = UITheme.secondaryButton("🔄  Clear");
        JLabel  statusLbl = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.add(addBtn); btnRow.add(clearBtn); btnRow.add(statusLbl);

        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 10, 8, 10);
        card.add(btnRow, gbc);

        addBtn.addActionListener(e -> {
            if (courseCombo.getItemCount() == 0) {
                setStatus(statusLbl, "❌ No courses available.", true); return;
            }
            String courseId   = (String) courseCombo.getSelectedItem();
            String type       = (String) typeCombo.getSelectedItem();
            String title      = fTitle.getText().trim();
            String desc       = fDesc.getText().trim();
            String url        = fUrl.getText().trim();

            if (title.isEmpty()) { setStatus(statusLbl, "❌ Title is required.", true); return; }

            String err = svc.add(courseId, user.getProfileId(), title, desc, url, type);
            if (err == null) {
                setStatus(statusLbl, "✅ Material added to " + courseId + "!", false);
                fTitle.setText(""); fDesc.setText(""); fUrl.setText("");
            } else setStatus(statusLbl, "❌ " + err, true);
        });

        clearBtn.addActionListener(e -> {
            fTitle.setText(""); fDesc.setText(""); fUrl.setText("");
            typeCombo.setSelectedIndex(0);
            statusLbl.setText("");
        });

        p.add(card, BorderLayout.NORTH);
        return p;
    }

    private JLabel lbl(String text) {
        return UITheme.label(text, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
    }

    private void styleTable(JTable t) {
        t.setBackground(UITheme.BG_CARD); t.setForeground(UITheme.TEXT_PRIMARY);
        t.setFont(UITheme.FONT_BODY); t.setRowHeight(36); t.setGridColor(UITheme.BORDER_COLOR);
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
    private String str(DefaultTableModel m, int row, int col) { Object v = m.getValueAt(row, col); return v != null ? v.toString() : ""; }
    private String orDash(String s)     { return (s == null || s.isEmpty()) ? "—" : s; }
    private String dashToEmpty(String s){ return "—".equals(s) ? "" : s; }
}
