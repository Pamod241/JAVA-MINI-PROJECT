package gui;

import model.User;
import util.UITheme;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private final User user;

    public DashboardFrame(User user) {
        this.user = user;
        UITheme.apply();
        setTitle("Dashboard — " + user.getRole());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 580));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BG_DARK); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        root.setOpaque(false);

        // ── SIDEBAR ───────────────────────────────────────────────────────
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BG_CARD); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(UITheme.BORDER_COLOR); g2.drawLine(getWidth()-1,0,getWidth()-1,getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(220,0)); sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        // Sidebar top — user info
        JPanel sideTop = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(14,165,233,180),0,getHeight(),new Color(99,102,241,180));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        sideTop.setPreferredSize(new Dimension(220,140)); sideTop.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints(); gc.gridx=0; gc.fill=GridBagConstraints.HORIZONTAL;

        // Avatar circle
        gc.gridy=0; gc.insets=new Insets(20,0,10,0);
        JLabel avatar = new JLabel(getInitials(user), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,40)); g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(Color.WHITE); g2.drawOval(1,1,getWidth()-2,getHeight()-2);
                g2.dispose(); super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI",Font.BOLD,18)); avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(52,52)); avatar.setOpaque(false);
        sideTop.add(avatar, gc);

        gc.gridy=1; gc.insets=new Insets(0,10,2,10);
        JLabel nameLabel = UITheme.label(user.getFullName(), Color.WHITE, new Font("Segoe UI",Font.BOLD,13));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER); sideTop.add(nameLabel, gc);

        gc.gridy=2; gc.insets=new Insets(0,10,16,10);
        JLabel roleLabel = UITheme.label(getRoleDisplay(user.getRole()), new Color(255,255,255,160), new Font("Segoe UI",Font.PLAIN,11));
        roleLabel.setHorizontalAlignment(SwingConstants.CENTER); sideTop.add(roleLabel, gc);

        sidebar.add(sideTop, BorderLayout.NORTH);

        // Sidebar nav buttons
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));

        JPanel contentArea = new JPanel(new CardLayout());
        contentArea.setOpaque(false);

        // Create a welcome panel
        JPanel welcomePanel = createWelcomePanel();
        contentArea.add(welcomePanel, "WELCOME");

        // Add nav items based on role
        switch(user.getRole()) {
            case "ADMIN" -> {
                addNavItem(navPanel, contentArea, "🏠", "Dashboard",      "WELCOME",    () -> welcomePanel);
                addNavItem(navPanel, contentArea, "👥", "User Profiles",  "USERS",      () -> new UserManagementFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "📚", "Courses",        "COURSES",    () -> new CourseManagementFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🎓", "All Students",   "STUDENTS",   () -> new StudentListFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "✅", "Attendance",     "ATTENDANCE", () -> new AttendanceFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "📊", "Marks & GPA",    "MARKS",      () -> new MarksFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🏥", "Medicals",       "MEDICALS",   () -> new MedicalFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "📋", "Notices",        "NOTICES",    () -> new NoticeFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🗓️", "Timetable",      "TIMETABLE",  () -> new TimetableFrame(user).getContentPanel());
            }
            case "LECTURER" -> {
                addNavItem(navPanel, contentArea, "🏠", "Dashboard",        "WELCOME",    () -> welcomePanel);
                addNavItem(navPanel, contentArea, "👤", "My Profile",       "PROFILE",    () -> new ProfileFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "📂", "Course Materials", "MATERIALS",  () -> new CourseMaterialFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🎓", "Undergraduates",   "STUDENTS",   () -> new LecturerStudentView(user).getContentPanel());
                addNavItem(navPanel, contentArea, "📋", "Notices",          "NOTICES",    () -> new NoticeFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🗓️", "Timetable",        "TIMETABLE",  () -> new TimetableFrame(user).getContentPanel());
            }
            case "STUDENT" -> {
                addNavItem(navPanel, contentArea, "🏠", "Dashboard",   "WELCOME",   () -> welcomePanel);
                addNavItem(navPanel, contentArea, "✅", "Attendance",  "ATTENDANCE",() -> new AttendanceFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "📊", "My Marks",    "MARKS",     () -> new MarksFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🏥", "Medicals",    "MEDICALS",  () -> new MedicalFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "📋", "Notices",     "NOTICES",   () -> new NoticeFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🗓️", "Timetable",   "TIMETABLE", () -> new TimetableFrame(user).getContentPanel());
            }
            case "TECHNICAL_OFFICER" -> {
                addNavItem(navPanel, contentArea, "🏠", "Dashboard",   "WELCOME",    () -> welcomePanel);
                addNavItem(navPanel, contentArea, "👤", "My Profile",  "PROFILE",    () -> new ProfileFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "✅", "Attendance",  "ATTENDANCE", () -> new TechOfficerAttendanceFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🏥", "Medicals",    "MEDICALS",   () -> new MedicalFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "📋", "Notices",     "NOTICES",    () -> new NoticeFrame(user).getContentPanel());
                addNavItem(navPanel, contentArea, "🗓️", "Timetable",   "TIMETABLE",() -> new TimetableFrame(user).getContentPanel());
            }
        }

        navPanel.add(Box.createVerticalGlue());

        // Logout button
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutPanel.setOpaque(false); logoutPanel.setBorder(BorderFactory.createEmptyBorder(0,12,16,12));
        JButton logoutBtn = UITheme.dangerButton("⏻  Logout");
        logoutBtn.setMaximumSize(new Dimension(196,36));
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame(); });
        logoutPanel.add(logoutBtn);

        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);
        root.add(sidebar, BorderLayout.WEST);

        // ── MAIN CONTENT ──────────────────────────────────────────────────
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setOpaque(false); mainWrapper.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        mainWrapper.add(contentArea, BorderLayout.CENTER);
        root.add(mainWrapper, BorderLayout.CENTER);

        setContentPane(root); pack(); setSize(1000,620); setLocationRelativeTo(null); setVisible(true);
    }

    private void addNavItem(JPanel nav, JPanel content, String icon, String label, String key, java.util.function.Supplier<JPanel> panelSupplier) {
        JButton btn = new JButton(icon + "  " + label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if(getModel().isRollover()) { g2.setColor(UITheme.BG_HOVER); g2.fillRect(0,0,getWidth(),getHeight()); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BODY); btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12,20,12,20));
        btn.setMaximumSize(new Dimension(220,46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> {
            // Reset all nav button colors
            for(Component c : nav.getComponents()) if(c instanceof JButton b) {
                b.setForeground(UITheme.TEXT_SECONDARY);
                b.setFont(UITheme.FONT_BODY);
            }
            btn.setForeground(UITheme.ACCENT); btn.setFont(new Font("Segoe UI",Font.BOLD,13));
            CardLayout cl = (CardLayout) content.getLayout();
            if(content.getComponentCount() == 0 || content.getComponent(0) == null || key.equals("WELCOME")) {
                cl.show(content, key);
            } else {
                // Lazy load
                boolean exists = false;
                for(Component c : content.getComponents()) if(c.getName()!=null && c.getName().equals(key)) { exists=true; break; }
                if(!exists) { JPanel p = panelSupplier.get(); p.setName(key); content.add(p, key); }
                cl.show(content, key);
            }
        });
        nav.add(btn);
    }

    private JPanel createWelcomePanel() {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx=0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(8,0,8,0);

        gbc.gridy=0;
        String firstName = user.getFullName().split(" ")[0];
        JLabel greet = UITheme.label("Good day, " + firstName + " 👋", UITheme.TEXT_PRIMARY, new Font("Segoe UI",Font.BOLD,26));
        p.add(greet, gbc);

        gbc.gridy=1;
        p.add(UITheme.label("Here's your overview for today", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY), gbc);

        gbc.gridy=2; gbc.insets=new Insets(24,0,8,0);
        // Stat cards row
        JPanel cards = new JPanel(new GridLayout(1,3,16,0)); cards.setOpaque(false);
        cards.add(statCard("Role", getRoleDisplay(user.getRole()), UITheme.ACCENT));
        cards.add(statCard("Email", user.getEmail() != null ? user.getEmail() : "N/A", UITheme.ACCENT_GREEN));
        cards.add(statCard("Profile ID", user.getProfileId() != null ? user.getProfileId() : "N/A", UITheme.ACCENT_PURPLE));
        p.add(cards, gbc);

        // Extra row for students — show student type
        if (user.getRole().equals("STUDENT") && user instanceof model.Student s) {
            gbc.gridy=3; gbc.insets=new Insets(12,0,8,0);
            JPanel studentCards = new JPanel(new GridLayout(1,3,16,0)); studentCards.setOpaque(false);
            Color typeColor = s.isRepeat() ? UITheme.ACCENT_RED :
                                       s.isBatchMissed() ? UITheme.ACCENT_YELLOW : UITheme.ACCENT_GREEN;
            studentCards.add(statCard("Student Type", s.getStudentType(),   typeColor));
            studentCards.add(statCard("Reg No",       s.getStudentId(),     UITheme.ACCENT));
            studentCards.add(statCard("Department",   s.getDepId() != null ? s.getDepId() : "N/A", UITheme.ACCENT_PURPLE));
            p.add(studentCards, gbc);
            gbc.gridy=4;
        } else {
            gbc.gridy=3;
        }

        gbc.insets=new Insets(16,0,8,0);
        p.add(UITheme.label("Use the sidebar to navigate between modules.", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), gbc);
        return p;
    }

    private JPanel statCard(String title, String value, Color accent) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0,8));
        JLabel t = UITheme.label(title, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
        JLabel v = UITheme.label(value, accent, new Font("Segoe UI",Font.BOLD,14));
        v.setForeground(accent);
        card.add(t, BorderLayout.NORTH); card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JPanel createComingSoon(String name) {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false);
        p.add(UITheme.label("🚧  " + name + " — Coming Soon", UITheme.TEXT_SECONDARY, UITheme.FONT_TITLE));
        return p;
    }

    private String getInitials(User u) {
        String[] parts = u.getFullName().trim().split("\\s+");
        if (parts.length >= 2)
            return String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length-1].charAt(0));
        return parts[0].isEmpty() ? "?" : String.valueOf(parts[0].charAt(0));
    }

    private String getRoleDisplay(String role) {
        return switch(role) {
            case "ADMIN" -> "Administrator";
            case "LECTURER" -> "Lecturer";
            case "STUDENT" -> "Undergraduate";
            case "TECHNICAL_OFFICER" -> "Technical Officer";
            default -> role;
        };
    }
}
