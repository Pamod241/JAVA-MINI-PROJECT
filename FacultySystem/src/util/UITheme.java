package util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UITheme {

    // ── Colour Palette ────────────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(13,  17,  23);
    public static final Color BG_CARD       = new Color(22,  27,  34);
    public static final Color BG_HOVER      = new Color(33,  41,  54);
    public static final Color ACCENT        = new Color(56, 189, 248);
    public static final Color ACCENT_DARK   = new Color(14, 165, 233);
    public static final Color ACCENT_GREEN  = new Color(34, 197, 94);
    public static final Color ACCENT_RED    = new Color(239, 68, 68);
    public static final Color ACCENT_YELLOW = new Color(251, 191, 36);
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    public static final Color TEXT_PRIMARY  = new Color(230, 237, 243);
    public static final Color TEXT_SECONDARY= new Color(139, 148, 158);
    public static final Color BORDER_COLOR  = new Color(48,  54,  61);
    public static final Color INPUT_BG      = new Color(13,  17,  23);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 12);

    // ── Apply global Look & Feel defaults ─────────────────────────────────────
    public static void apply() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        UIManager.put("Panel.background",           BG_DARK);
        UIManager.put("Frame.background",           BG_DARK);
        UIManager.put("Label.foreground",           TEXT_PRIMARY);
        UIManager.put("Label.font",                 FONT_BODY);
        UIManager.put("Button.background",          BG_CARD);
        UIManager.put("Button.foreground",          TEXT_PRIMARY);
        UIManager.put("Button.font",                FONT_BODY);
        UIManager.put("Button.border",              BorderFactory.createEmptyBorder(8,16,8,16));
        UIManager.put("TextField.background",       INPUT_BG);
        UIManager.put("TextField.foreground",       TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",  ACCENT);
        UIManager.put("TextField.font",             FONT_BODY);
        UIManager.put("TextField.border",           inputBorder());
        UIManager.put("PasswordField.background",   INPUT_BG);
        UIManager.put("PasswordField.foreground",   TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", ACCENT);
        UIManager.put("PasswordField.border",       inputBorder());
        UIManager.put("TextArea.background",        INPUT_BG);
        UIManager.put("TextArea.foreground",        TEXT_PRIMARY);
        UIManager.put("TextArea.font",              FONT_BODY);
        UIManager.put("ComboBox.background",        BG_CARD);
        UIManager.put("ComboBox.foreground",        TEXT_PRIMARY);
        UIManager.put("ComboBox.font",              FONT_BODY);
        UIManager.put("Table.background",           BG_CARD);
        UIManager.put("Table.foreground",           TEXT_PRIMARY);
        UIManager.put("Table.font",                 FONT_BODY);
        UIManager.put("Table.gridColor",            BORDER_COLOR);
        UIManager.put("Table.selectionBackground",  new Color(56,189,248,60));
        UIManager.put("Table.selectionForeground",  ACCENT);
        UIManager.put("TableHeader.background",     BG_DARK);
        UIManager.put("TableHeader.foreground",     ACCENT);
        UIManager.put("TableHeader.font",           FONT_SUBTITLE);
        UIManager.put("ScrollPane.background",      BG_CARD);
        UIManager.put("ScrollBar.background",       BG_DARK);
        UIManager.put("ScrollBar.thumb",            BORDER_COLOR);
        UIManager.put("ScrollBar.track",            BG_DARK);
        UIManager.put("SplitPane.background",       BG_DARK);
        UIManager.put("TabbedPane.background",      BG_DARK);
        UIManager.put("TabbedPane.foreground",      TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",        BG_CARD);
        UIManager.put("TabbedPane.font",            FONT_BODY);
        UIManager.put("OptionPane.background",      BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
    }

    // ── Styled Components ─────────────────────────────────────────────────────

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    /** Primary accent button */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()  ? ACCENT_DARK :
                             getModel().isRollover() ? new Color(125,211,252) : ACCENT;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(BG_DARK);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    /** Secondary outline button */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? BG_HOVER : BG_CARD;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_PRIMARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /** Danger red button */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isRollover()
                    ? new Color(220,38,38) : new Color(239,68,68,200);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /** Success green button */
    public static JButton successButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isRollover()
                    ? new Color(22,163,74) : new Color(34,197,94,200);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /** Styled text field */
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setColor(TEXT_SECONDARY);
                    g3.setFont(getFont());
                    FontMetrics fm = g3.getFontMetrics();
                    g3.drawString(placeholder, 12,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g3.dispose();
                }
            }
        };
        f.setOpaque(false);
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return f;
    }

    /** Styled password field */
    public static JPasswordField styledPassword(String placeholder) {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setColor(TEXT_SECONDARY);
                    g3.setFont(getFont());
                    FontMetrics fm = g3.getFontMetrics();
                    g3.drawString(placeholder, 12,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g3.dispose();
                }
            }
        };
        f.setOpaque(false);
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return f;
    }

    /** Styled label */
    public static JLabel label(String text, Color color, Font font) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(font);
        return l;
    }

    /** Card panel with rounded border */
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return p;
    }

    /** Status badge */
    public static JLabel badge(String text, Color bg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(bg);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setForeground(bg);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    /** Separator line */
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BG_DARK);
        return sep;
    }

    /** Styled scroll pane */
    public static JScrollPane scrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getHorizontalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    /** Show styled message dialog */
    public static void showMessage(Component parent, String message, String title, boolean isError) {
        JOptionPane.showMessageDialog(parent, message, title,
            isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }
}
