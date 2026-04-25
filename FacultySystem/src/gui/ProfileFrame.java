package gui;

import db.DBConnection;
import model.Lecturer;
import model.Student;
import model.User;
import util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileFrame extends JFrame {

    private final User user;
    private JPanel contentPanel;

    public ProfileFrame(User user) {
        this.user = user;
        buildPanel();
    }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 24));
        contentPanel.setOpaque(false);

        contentPanel.add(UITheme.label("My Profile", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 20)), BorderLayout.NORTH);

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2;
        card.add(UITheme.label("Account Information  (read-only)",
            UITheme.TEXT_SECONDARY, new Font("Segoe UI", Font.BOLD, 12)), gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1; gbc.gridx = 0; card.add(lbl("Profile ID"), gbc);
        gbc.gridx = 1;
        card.add(UITheme.label(user.getProfileId(), UITheme.ACCENT,
            new Font("Segoe UI", Font.BOLD, 13)), gbc);

        gbc.gridy = 2; gbc.gridx = 0; card.add(lbl("Role"), gbc);
        gbc.gridx = 1;
        card.add(UITheme.label(roleDisplay(user.getRole()), UITheme.ACCENT_PURPLE,
            new Font("Segoe UI", Font.BOLD, 13)), gbc);

        if (user instanceof Lecturer lec) {
            gbc.gridy = 3; gbc.gridx = 0; card.add(lbl("Department"), gbc);
            gbc.gridx = 1;
            card.add(UITheme.label(lec.getDepartment() != null ? lec.getDepartment() : "—",
                UITheme.TEXT_PRIMARY, UITheme.FONT_BODY), gbc);
        } else if (user instanceof Student stu) {
            gbc.gridy = 3; gbc.gridx = 0; card.add(lbl("Department"), gbc);
            gbc.gridx = 1;
            card.add(UITheme.label(stu.getDepId() != null ? stu.getDepId() : "—",
                UITheme.TEXT_PRIMARY, UITheme.FONT_BODY), gbc);
        }

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(16, 10, 4, 10);
        card.add(UITheme.separator(), gbc);
        gbc.insets = new Insets(8, 10, 8, 10);

        gbc.gridy = 5; gbc.gridwidth = 2;
        card.add(UITheme.label("Edit Your Details  (changes are saved to the database)",
            UITheme.TEXT_SECONDARY, new Font("Segoe UI", Font.BOLD, 12)), gbc);
        gbc.gridwidth = 1;

        JTextField fFullname = UITheme.styledField("Full Name");
        JTextField fEmail    = UITheme.styledField("Email");

        fFullname.setText(user.getFullName() != null ? user.getFullName() : "");
        fEmail.setText(user.getEmail()    != null ? user.getEmail()    : "");

        fFullname.setPreferredSize(new Dimension(300, 38));
        fEmail.setPreferredSize(new Dimension(300, 38));

        gbc.gridy = 6; gbc.gridx = 0; card.add(lbl("Full Name"), gbc);
        gbc.gridx = 1; card.add(fFullname, gbc);
        gbc.gridy = 7; gbc.gridx = 0; card.add(lbl("Email"),     gbc);
        gbc.gridx = 1; card.add(fEmail,    gbc);

        JButton saveBtn   = UITheme.primaryButton("💾  Save Changes");
        JLabel  statusLbl = UITheme.label("", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(16, 10, 4, 10);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn); btnRow.add(statusLbl);
        card.add(btnRow, gbc);

        saveBtn.addActionListener(e -> {
            String fn = fFullname.getText().trim();
            String em = fEmail.getText().trim();

            if (fn.isEmpty()) {
                statusLbl.setText("❌ Full Name is required.");
                statusLbl.setForeground(UITheme.ACCENT_RED);
                return;
            }

            String err = saveProfile(user.getUserId(), user.getProfileId(), user.getRole(), fn, em);
            if (err == null) {
                statusLbl.setText("✅ Profile updated successfully.");
                statusLbl.setForeground(UITheme.ACCENT_GREEN);
                user.setFullname(fn);
                user.setEmail(em.isEmpty() ? null : em);
            } else {
                statusLbl.setText("❌ " + err);
                statusLbl.setForeground(UITheme.ACCENT_RED);
            }
        });

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(card);
        contentPanel.add(wrapper, BorderLayout.CENTER);
    }

    private String saveProfile(String userId, String profileId, String role, String fullname, String email) {
        try {
            PreparedStatement u = DBConnection.getConnection().prepareStatement(
                "UPDATE User SET Email=? WHERE User_id=?");
            u.setString(1, email.isEmpty() ? null : email); u.setString(2, userId);
            u.executeUpdate();

            String tableUpdate = switch (role) {
                case "LECTURER"          -> "UPDATE Lecturer SET Fullname=?, Email=? WHERE Lec_id=?";
                case "TECHNICAL_OFFICER" -> "UPDATE Technical_Officer SET Fullname=?, Email=? WHERE TO_id=?";
                case "ADMIN"             -> "UPDATE Admin SET Fullname=?, Email=? WHERE Admin_id=?";
                default                  -> "UPDATE Student SET Fullname=?, Email=? WHERE Reg_no=?";
            };
            PreparedStatement s = DBConnection.getConnection().prepareStatement(tableUpdate);
            s.setString(1, fullname); s.setString(2, email.isEmpty() ? null : email);
            s.setString(3, profileId);
            s.executeUpdate();
            return null;
        } catch (SQLException e) {
            System.err.println("ProfileFrame.saveProfile: " + e.getMessage());
            return e.getMessage();
        }
    }

    private JLabel lbl(String text) {
        return UITheme.label(text, UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL);
    }

    private String roleDisplay(String role) {
        return switch (role) {
            case "LECTURER"          -> "Lecturer";
            case "TECHNICAL_OFFICER" -> "Technical Officer";
            case "ADMIN"             -> "Administrator";
            default -> role;
        };
    }
}
