package gui;

import model.Notice;
import model.User;
import service.NoticeService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class NoticeFrame extends JFrame {

    private final NoticeService svc = new NoticeService();
    private final User user;
    private JPanel contentPanel;

    public NoticeFrame(User user) { this.user = user; buildPanel(); }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0,16)); contentPanel.setOpaque(false);
        contentPanel.add(UITheme.label("Notice Board", UITheme.TEXT_PRIMARY, new Font("Segoe UI",Font.BOLD,20)), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(12,0)); main.setOpaque(false);

        // Table
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Title","Posted At"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table = new JTable(model);
        table.setBackground(UITheme.BG_CARD); table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY); table.setRowHeight(36); table.setGridColor(UITheme.BORDER_COLOR);
        table.setSelectionBackground(new Color(56,189,248,40)); table.setSelectionForeground(UITheme.ACCENT);
        table.getTableHeader().setBackground(UITheme.BG_DARK); table.getTableHeader().setForeground(UITheme.ACCENT);
        table.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        // Content viewer
        JPanel rightPanel = UITheme.card(); rightPanel.setLayout(new BorderLayout(0,12));
        rightPanel.setPreferredSize(new Dimension(300,0));
        JLabel noticeTitle = UITheme.label("Select a notice", UITheme.ACCENT, UITheme.FONT_SUBTITLE);
        JTextArea noticeContent = new JTextArea();
        noticeContent.setLineWrap(true); noticeContent.setWrapStyleWord(true);
        noticeContent.setEditable(false); noticeContent.setBackground(UITheme.BG_CARD);
        noticeContent.setForeground(UITheme.TEXT_PRIMARY); noticeContent.setFont(UITheme.FONT_BODY);
        noticeContent.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));
        rightPanel.add(noticeTitle, BorderLayout.NORTH);
        rightPanel.add(UITheme.scrollPane(noticeContent), BorderLayout.CENTER);

        // Click to view
        List<Notice>[] noticesRef = new List[]{svc.getAll()};
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row>=0) {
                String id = (String) model.getValueAt(row,0);
                noticesRef[0].stream().filter(n->n.getNoticeId().equals(id)).findFirst().ifPresent(n->{
                    noticeTitle.setText(n.getTitle());
                    noticeContent.setText(n.getContent());
                });
            }
        });

        main.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        main.add(rightPanel, BorderLayout.EAST);
        contentPanel.add(main, BorderLayout.CENTER);

        // Admin controls
        if(user.getRole().equals("ADMIN")) {
            JPanel bottom = UITheme.card(); bottom.setLayout(new GridLayout(0,2,12,12));
            JTextField titleField = UITheme.styledField("Notice Title");
            JTextArea editArea = new JTextArea(3,20);
            editArea.setBackground(UITheme.INPUT_BG); editArea.setForeground(UITheme.TEXT_PRIMARY);
            editArea.setFont(UITheme.FONT_BODY); editArea.setCaretColor(UITheme.ACCENT);
            editArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR,1),BorderFactory.createEmptyBorder(8,12,8,12)));
            JButton addBtn    = UITheme.primaryButton("📢  Post Notice");
            JButton deleteBtn = UITheme.dangerButton("🗑️  Delete Selected");

            bottom.add(UITheme.label("Title",   UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(titleField);
            bottom.add(UITheme.label("Content", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(new JScrollPane(editArea));
            bottom.add(addBtn); bottom.add(deleteBtn);

            addBtn.addActionListener(e -> {
                String t = titleField.getText().trim(); String c = editArea.getText().trim();
                if(!t.isEmpty()&&!c.isEmpty()){
                    svc.add(t,c,user.getProfileId()); noticesRef[0]=svc.getAll();
                    loadTable(model,noticesRef[0]); titleField.setText(""); editArea.setText("");
                }
            });
            deleteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if(row>=0){ svc.delete((String)model.getValueAt(row,0)); noticesRef[0]=svc.getAll(); loadTable(model,noticesRef[0]); }
            });
            contentPanel.add(bottom, BorderLayout.SOUTH);
        }

        loadTable(model, noticesRef[0]);
    }

    private void loadTable(DefaultTableModel model, List<Notice> list) {
        model.setRowCount(0);
        for(Notice n : list) model.addRow(new Object[]{n.getNoticeId(), n.getTitle(), n.getCreatedAt().toLocalDate()});
    }
}
