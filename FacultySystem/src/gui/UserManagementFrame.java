package gui;

import model.User;
import service.UserService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class UserManagementFrame extends JFrame {

    private final UserService svc = new UserService();
    private final User currentUser;
    private JPanel contentPanel;

    public UserManagementFrame(User currentUser) {
        this.currentUser = currentUser;
        buildPanel();
    }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(UITheme.label("User Profile Management", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 20)), BorderLayout.WEST);
        contentPanel.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_CARD); tabs.setForeground(UITheme.TEXT_PRIMARY); tabs.setFont(UITheme.FONT_BODY);

        tabs.addTab("👥  All Users",        buildUserTab(null));
        tabs.addTab("🎓  Students",          buildUserTab("STUDENT"));
        tabs.addTab("📖  Lecturers",         buildUserTab("LECTURER"));
        tabs.addTab("🔧  Tech Officers",     buildUserTab("TECHNICAL_OFFICER"));
        tabs.addTab("🛡️  Admins",            buildUserTab("ADMIN"));
        tabs.addTab("➕  Create User",       buildCreatePanel());

        contentPanel.add(tabs, BorderLayout.CENTER);
    }

    // ── USER LIST TAB ─────────────────────────────────────────────────────
    private JPanel buildUserTab(String roleFilter) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);
        JTextField searchField = UITheme.styledField("Search name, ID...");
        searchField.setPreferredSize(new Dimension(260, 36));
        JButton searchBtn  = UITheme.primaryButton("🔍  Search");
        JButton refreshBtn = UITheme.secondaryButton("🔄  Refresh");
        topBar.add(searchField); topBar.add(searchBtn); topBar.add(refreshBtn);
        p.add(topBar, BorderLayout.NORTH);

        String[] cols = {"User ID", "Profile ID", "Role", "Full Name", "Email", "Dept", "Type"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model);
        styleTable(tbl);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(70);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(100);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(120);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(160);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(200);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(80);
        tbl.getColumnModel().getColumn(6).setPreferredWidth(90);

        // Role badge colour
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.BG_CARD);
                String role = v != null ? v.toString() : "";
                setForeground(switch (role) {
                    case "ADMIN"             -> UITheme.ACCENT_RED;
                    case "LECTURER"          -> UITheme.ACCENT;
                    case "STUDENT"           -> UITheme.ACCENT_GREEN;
                    case "TECHNICAL_OFFICER" -> UITheme.ACCENT_YELLOW;
                    default -> UITheme.TEXT_SECONDARY;
                });
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        // Type column colour
        tbl.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.BG_CARD);
                String type = v != null ? v.toString() : "";
                setForeground(switch (type) {
                    case "Repeat"    -> UITheme.ACCENT_RED;
                    case "Suspended" -> UITheme.ACCENT_YELLOW;
                    case "Proper"    -> UITheme.ACCENT_GREEN;
                    default -> UITheme.TEXT_SECONDARY;
                });
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        JPanel editPanel = buildEditPanel(tbl, model, roleFilter);
        editPanel.setVisible(false);
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (tbl.getSelectedRow() >= 0) editPanel.setVisible(true);
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, UITheme.scrollPane(tbl), editPanel);
        split.setOpaque(false); split.setBackground(UITheme.BG_DARK);
        split.setDividerLocation(340); split.setDividerSize(5); split.setBorder(null);
        p.add(split, BorderLayout.CENTER);

        Runnable load = () -> loadTable(model, roleFilter, null);
        load.run();
        searchBtn.addActionListener(e -> loadTable(model, roleFilter, searchField.getText().trim()));
        refreshBtn.addActionListener(e -> { searchField.setText(""); load.run(); });
        return p;
    }

    private void loadTable(DefaultTableModel model, String roleFilter, String search) {
        model.setRowCount(0);
        List<Map<String, Object>> data = roleFilter == null ? svc.getAll() : svc.getAllByRole(roleFilter);
        for (Map<String, Object> u : data) {
            String fullname = str(u, "fullname");
            if (search != null && !search.isEmpty()) {
                String q = search.toLowerCase();
                if (!fullname.toLowerCase().contains(q) &&
                    !str(u, "profile_id").toLowerCase().contains(q) &&
                    !str(u, "user_id").toLowerCase().contains(q)) continue;
            }
            model.addRow(new Object[]{
                str(u, "user_id"),
                str(u, "profile_id"),
                str(u, "role"),
                fullname,
                orDash(str(u, "email")),
                orDash(str(u, "dep_id")),
                orDash(str(u, "type"))
            });
        }
    }

    // ── EDIT / DELETE PANEL ───────────────────────────────────────────────
    private JPanel buildEditPanel(JTable tbl, DefaultTableModel model, String roleFilter) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(12, 0));

        JLabel titleLbl = UITheme.label("Edit Selected User", UITheme.ACCENT, UITheme.FONT_SUBTITLE);
        card.add(titleLbl, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 6, 10, 8));
        fields.setOpaque(false);

        JTextField fFullname = UITheme.styledField("Full Name");
        JTextField fEmail    = UITheme.styledField("Email");
        JTextField fPassword = UITheme.styledField("Password");
        JTextField fDepId    = UITheme.styledField("Dept/Dep ID");
        JTextField fGender   = UITheme.styledField("Gender (M/F)");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Proper","Repeat","Suspended"});
        styleCombo(typeCombo);

        fields.add(label("Full Name")); fields.add(fFullname);
        fields.add(label("Email"));     fields.add(fEmail);
        fields.add(label("Password"));  fields.add(fPassword);
        fields.add(label("Dept ID"));   fields.add(fDepId);
        fields.add(label("Gender"));    fields.add(fGender);
        fields.add(label("Type"));      fields.add(typeCombo);

        card.add(fields, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton saveBtn   = UITheme.primaryButton("💾  Save Changes");
        JButton deleteBtn = UITheme.dangerButton("🗑️  Delete User");
        JLabel  statusLbl = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
        btnRow.add(statusLbl); btnRow.add(saveBtn); btnRow.add(deleteBtn);
        card.add(btnRow, BorderLayout.SOUTH);
        wrapper.add(card);

        tbl.getSelectionModel().addListSelectionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            statusLbl.setText("");
            String userId = model.getValueAt(row, 0).toString();
            String roleVal = model.getValueAt(row, 2).toString();
            svc.getAll().stream().filter(u -> userId.equals(str(u, "user_id"))).findFirst().ifPresent(u -> {
                fFullname.setText(str(u, "fullname"));
                fEmail.setText(orEmpty(str(u, "email")));
                fPassword.setText(str(u, "password"));
                fDepId.setText(orEmpty(str(u, "dep_id")));
                fGender.setText(orEmpty(str(u, "gender")));
                String t = str(u, "type");
                typeCombo.setSelectedItem(t.isEmpty() ? "Proper" : t);
                boolean isStudent  = "STUDENT".equals(roleVal);
                boolean isLecturer = "LECTURER".equals(roleVal);
                typeCombo.setEnabled(isStudent);
                fDepId.setEnabled(isStudent || isLecturer);
                fGender.setEnabled(isLecturer);
            });
        });

        saveBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            String userId  = model.getValueAt(row, 0).toString();
            String roleVal = model.getValueAt(row, 2).toString();
            String err = svc.updateUser(userId,
                fFullname.getText().trim(), fEmail.getText().trim(), fPassword.getText().trim(),
                fDepId.getText().trim(), (String) typeCombo.getSelectedItem(), fGender.getText().trim());
            if (err == null) {
                setStatus(statusLbl, "✅ User updated successfully.", false);
                loadTable(model, roleFilter, null);
            } else {
                setStatus(statusLbl, "❌ " + err, true);
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            String userId = model.getValueAt(row, 0).toString();
            String name   = model.getValueAt(row, 3).toString();
            int confirm = JOptionPane.showConfirmDialog(contentPanel,
                "Delete user \"" + name + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            String err = svc.deleteUser(userId);
            if (err == null) {
                setStatus(statusLbl, "✅ User deleted.", false);
                loadTable(model, roleFilter, null);
                wrapper.setVisible(false);
            } else {
                setStatus(statusLbl, "❌ " + err, true);
            }
        });

        return wrapper;
    }

    // ── CREATE USER TAB ───────────────────────────────────────────────────
    private JPanel buildCreatePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JComboBox<String> roleCombo = new JComboBox<>(new String[]{
            "STUDENT", "LECTURER", "TECHNICAL_OFFICER", "ADMIN"});
        styleCombo(roleCombo);

        JTextField fProfileId = UITheme.styledField("Profile ID (e.g. ICT2022025 / LC006 / TO005 / AD002)");
        JTextField fFullname  = UITheme.styledField("Full Name");
        JTextField fEmail     = UITheme.styledField("Email");
        JTextField fPassword  = UITheme.styledField("Password");
        JTextField fDepId     = UITheme.styledField("Dept ID (optional)");
        JTextField fGender    = UITheme.styledField("Gender: M or F (lecturer only)");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Proper","Repeat","Suspended"});
        styleCombo(typeCombo);

        JPanel studentSection = new JPanel(new GridLayout(1, 4, 10, 8));
        studentSection.setOpaque(false);
        studentSection.add(label("Type")); studentSection.add(typeCombo);
        studentSection.add(new JLabel()); studentSection.add(new JLabel());

        roleCombo.addActionListener(e -> {
            boolean isStudent  = "STUDENT".equals(roleCombo.getSelectedItem());
            boolean isLecturer = "LECTURER".equals(roleCombo.getSelectedItem());
            studentSection.setVisible(isStudent);
            fDepId.setEnabled(isStudent || isLecturer);
            fGender.setEnabled(isLecturer);
        });
        studentSection.setVisible(true);

        int row = 0;
        addRow(card, gbc, row++, "Role",       roleCombo);
        addRow(card, gbc, row++, "Profile ID", fProfileId);
        addRow(card, gbc, row++, "Full Name",  fFullname);
        addRow(card, gbc, row++, "Email",      fEmail);
        addRow(card, gbc, row++, "Password",   fPassword);
        addRow(card, gbc, row++, "Dept ID",    fDepId);
        addRow(card, gbc, row++, "Gender",     fGender);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 4;
        card.add(studentSection, gbc);
        gbc.gridwidth = 1;

        JButton createBtn = UITheme.primaryButton("✅  Create User");
        JButton clearBtn  = UITheme.secondaryButton("🔄  Clear");
        JLabel  statusLbl = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.add(createBtn); btnRow.add(clearBtn); btnRow.add(statusLbl);

        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 4;
        card.add(btnRow, gbc);

        createBtn.addActionListener(e -> {
            String role      = (String) roleCombo.getSelectedItem();
            String profileId = fProfileId.getText().trim();
            String fullname  = fFullname.getText().trim();
            String email     = fEmail.getText().trim();
            String password  = fPassword.getText().trim();

            if (profileId.isEmpty() || fullname.isEmpty() || password.isEmpty()) {
                setStatus(statusLbl, "❌ Profile ID, Full Name and Password are required.", true);
                return;
            }

            String type   = (String) typeCombo.getSelectedItem();
            String depId  = fDepId.getText().trim();
            String gender = fGender.getText().trim();

            String err = svc.createUser(role, profileId, fullname, email, password, depId, type, gender);
            if (err == null) {
                setStatus(statusLbl, "✅ User created successfully!", false);
                fProfileId.setText(""); fFullname.setText(""); fEmail.setText("");
                fPassword.setText(""); fDepId.setText(""); fGender.setText("");
                typeCombo.setSelectedIndex(0);
            } else {
                setStatus(statusLbl, "❌ " + err, true);
            }
        });

        clearBtn.addActionListener(e -> {
            fProfileId.setText(""); fFullname.setText(""); fEmail.setText("");
            fPassword.setText(""); fDepId.setText(""); fGender.setText("");
            typeCombo.setSelectedIndex(0); statusLbl.setText("");
        });

        p.add(card, BorderLayout.NORTH);
        return p;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────
    private void addRow(JPanel card, GridBagConstraints gbc, int row, String lbl, Component field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; card.add(label(lbl), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; card.add(field, gbc);
        gbc.gridwidth = 1;
    }

    private JLabel label(String text) {
        return UITheme.label(text, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
    }

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

    private void setStatus(JLabel lbl, String msg, boolean isError) {
        lbl.setText(msg);
        lbl.setForeground(isError ? UITheme.ACCENT_RED : UITheme.ACCENT_GREEN);
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key); return v != null ? v.toString() : "";
    }

    private String orDash(String s)  { return (s == null || s.isEmpty() || "null".equals(s)) ? "—" : s; }
    private String orEmpty(String s) { return (s == null || s.equals("null") || s.equals("—")) ? "" : s; }
}
