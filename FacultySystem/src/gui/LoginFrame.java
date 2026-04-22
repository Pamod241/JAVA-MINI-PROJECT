package gui;

import auth.AuthService;
import model.User;
import util.UITheme;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         statusLabel;
    private JButton        loginButton;

    public LoginFrame() {
        UITheme.apply();
        setTitle("Faculty Management System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, UITheme.BG_DARK, getWidth(), getHeight(), new Color(20,30,48));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(56,189,248,12)); g2.fillOval(-80,-80,260,260);
                g2.setColor(new Color(168,85,247,10)); g2.fillOval(getWidth()-180,getHeight()-180,320,320);
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // LEFT PANEL
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, new Color(14,165,233,200), getWidth(),getHeight(), new Color(99,102,241,200));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,15));
                for(int x=0;x<getWidth();x+=30) for(int y=0;y<getHeight();y+=30) g2.fillOval(x,y,4,4);
                g2.dispose();
            }
        };
        leftPanel.setPreferredSize(new Dimension(260,0)); leftPanel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints(); gc.gridx=0; gc.fill=GridBagConstraints.HORIZONTAL;

        gc.gridy=0; gc.insets=new Insets(0,0,16,0);
        JLabel icon = new JLabel("🎓",SwingConstants.CENTER); icon.setFont(new Font("Segoe UI Emoji",Font.PLAIN,56));
        leftPanel.add(icon, gc);

        gc.gridy=1; gc.insets=new Insets(0,20,6,20);
        JLabel uni = UITheme.label("University of Ruhuna", Color.WHITE, new Font("Segoe UI",Font.BOLD,15));
        uni.setHorizontalAlignment(SwingConstants.CENTER); leftPanel.add(uni, gc);

        gc.gridy=2;
        JLabel dept = UITheme.label("Faculty of Technology", new Color(255,255,255,180), new Font("Segoe UI",Font.PLAIN,12));
        dept.setHorizontalAlignment(SwingConstants.CENTER); leftPanel.add(dept, gc);

        gc.gridy=3; gc.insets=new Insets(20,20,0,20);
        JLabel sys = UITheme.label("Management System v1.0", new Color(255,255,255,120), new Font("Segoe UI",Font.PLAIN,10));
        sys.setHorizontalAlignment(SwingConstants.CENTER); leftPanel.add(sys, gc);

        root.add(leftPanel, BorderLayout.WEST);

        // RIGHT PANEL
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false); right.setBorder(BorderFactory.createEmptyBorder(50,45,50,45));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.weightx=1.0;

        gbc.gridy=0; gbc.insets=new Insets(0,0,4,0);
        right.add(UITheme.label("Welcome back", UITheme.TEXT_PRIMARY, new Font("Segoe UI",Font.BOLD,24)), gbc);

        gbc.gridy=1; gbc.insets=new Insets(0,0,32,0);
        right.add(UITheme.label("Sign in to your account", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY), gbc);

        gbc.gridy=2; gbc.insets=new Insets(0,0,4,0);
        right.add(UITheme.label("Email", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), gbc);

        gbc.gridy=3; gbc.insets=new Insets(0,0,16,0);
        usernameField = UITheme.styledField("Enter your email");
        usernameField.setPreferredSize(new Dimension(300,42)); right.add(usernameField, gbc);

        gbc.gridy=4; gbc.insets=new Insets(0,0,4,0);
        right.add(UITheme.label("Password", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL), gbc);

        gbc.gridy=5; gbc.insets=new Insets(0,0,20,0);
        passwordField = UITheme.styledPassword("Enter your password");
        passwordField.setPreferredSize(new Dimension(300,42)); right.add(passwordField, gbc);

        gbc.gridy=6; gbc.insets=new Insets(0,0,10,0);
        statusLabel = new JLabel(" "); statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.ACCENT_RED); statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        right.add(statusLabel, gbc);

        gbc.gridy=7; gbc.insets=new Insets(0,0,16,0);
        loginButton = UITheme.primaryButton("Sign In");
        loginButton.setPreferredSize(new Dimension(300,44)); right.add(loginButton, gbc);

        gbc.gridy=8;
        JLabel hint = UITheme.label("Demo: use registered email / password", UITheme.TEXT_SECONDARY, new Font("Segoe UI",Font.ITALIC,11));
        hint.setHorizontalAlignment(SwingConstants.CENTER); right.add(hint, gbc);

        root.add(right, BorderLayout.CENTER);
        setContentPane(root); pack(); setSize(640,480); setLocationRelativeTo(null);

        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
        setVisible(true);
    }

    private void handleLogin() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword()).trim();
        if(u.isEmpty()||p.isEmpty()){ statusLabel.setText("Please enter email and password"); return; }
        loginButton.setText("Signing in..."); loginButton.setEnabled(false);
        SwingWorker<User,Void> w = new SwingWorker<>() {
            @Override protected User doInBackground() { return new AuthService().login(u,p); }
            @Override protected void done() {
                try {
                    User user = get();
                    if(user!=null){ dispose(); user.openDashboard(); }
                    else { statusLabel.setText("Invalid email or password"); loginButton.setText("Sign In"); loginButton.setEnabled(true); passwordField.setText(""); }
                } catch(Exception ex){ statusLabel.setText("Connection error. Check database."); loginButton.setText("Sign In"); loginButton.setEnabled(true); }
            }
        };
        w.execute();
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(LoginFrame::new); }
}
