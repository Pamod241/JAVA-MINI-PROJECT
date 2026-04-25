package gui;

import model.Medical;
import model.Student;
import model.User;
import service.MedicalService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MedicalFrame extends JFrame {

    private final MedicalService svc = new MedicalService();
    private final User user;
    private JPanel contentPanel;

    public MedicalFrame(User user) { this.user = user; buildPanel(); }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0,16)); contentPanel.setOpaque(false);
        contentPanel.add(UITheme.label("Medical Records", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI",Font.BOLD,20)), BorderLayout.NORTH);

        String[] cols = {"ID","Student ID","From Date","To Date","Description"};
        DefaultTableModel model = new DefaultTableModel(cols,0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table = new JTable(model);
        table.setBackground(UITheme.BG_CARD); table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY); table.setRowHeight(36); table.setGridColor(UITheme.BORDER_COLOR);
        table.setSelectionBackground(new Color(56,189,248,40)); table.setSelectionForeground(UITheme.ACCENT);
        table.getTableHeader().setBackground(UITheme.BG_DARK); table.getTableHeader().setForeground(UITheme.ACCENT);
        table.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        contentPanel.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        JPanel bottom = UITheme.card(); bottom.setLayout(new FlowLayout(FlowLayout.LEFT,12,8));

        if(user.getRole().equals("TECHNICAL_OFFICER") || user.getRole().equals("ADMIN")) {
            JTextField sidField   = UITheme.styledField("Student ID");   sidField.setPreferredSize(new Dimension(130,36));
            JTextField sDateField = UITheme.styledField("From (YYYY-MM-DD)"); sDateField.setPreferredSize(new Dimension(140,36));
            JTextField eDateField = UITheme.styledField("To (YYYY-MM-DD)");   eDateField.setPreferredSize(new Dimension(140,36));
            JTextField descField  = UITheme.styledField("Description");  descField.setPreferredSize(new Dimension(180,36));
            JButton addBtn    = UITheme.primaryButton("➕ Add Medical");
            JButton deleteBtn = UITheme.dangerButton("🗑️ Delete Selected");

            bottom.add(UITheme.label("Student ID:", UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(sidField);
            bottom.add(UITheme.label("From:",       UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(sDateField);
            bottom.add(UITheme.label("To:",         UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(eDateField);
            bottom.add(UITheme.label("Description:",UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(descField);
            bottom.add(addBtn); bottom.add(deleteBtn);

            addBtn.addActionListener(e -> {
                boolean ok = svc.add(sidField.getText().trim(), sDateField.getText().trim(),
                    eDateField.getText().trim(), descField.getText().trim());
                if (ok) { sidField.setText(""); sDateField.setText(""); eDateField.setText(""); descField.setText(""); }
                loadTable(model);
            });
            deleteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) { svc.delete((String)model.getValueAt(row,0)); loadTable(model); }
            });
        } else {
            JButton refresh = UITheme.secondaryButton("🔄 Refresh");
            refresh.addActionListener(e -> loadTable(model));
            bottom.add(refresh);
        }

        contentPanel.add(bottom, BorderLayout.SOUTH);
        loadTable(model);
    }

    private void loadTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<Medical> list = user.getRole().equals("STUDENT") && user instanceof Student s
            ? svc.getByStudent(s.getStudentId()) : svc.getAll();
        for (Medical m : list)
            model.addRow(new Object[]{
                m.getMedicalId(), m.getStudentId(),
                m.getSDate(), m.getEDate(), m.getDescription()
            });
    }
}
