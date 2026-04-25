package gui;

import model.Timetable;
import model.User;
import service.TimetableService;
import util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TimetableFrame extends JFrame {

    private final TimetableService svc = new TimetableService();
    private final User user;
    private JPanel contentPanel;

    public TimetableFrame(User user) { this.user = user; buildPanel(); }

    public JPanel getContentPanel() { return contentPanel; }

    private void buildPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.label("Timetable", UITheme.TEXT_PRIMARY,
            new Font("Segoe UI", Font.BOLD, 20)), BorderLayout.WEST);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterRow.setOpaque(false);

        // FIX 1: Default dept changed from "ICT" to "D01" to match DB value
        JTextField deptField = UITheme.styledField("Department ID");
        deptField.setPreferredSize(new Dimension(140, 36));
        deptField.setText("D01");

        JButton loadBtn = UITheme.primaryButton("Load");
        filterRow.add(UITheme.label("Department ID:", UITheme.TEXT_SECONDARY, UITheme.FONT_BODY));
        filterRow.add(deptField);
        filterRow.add(loadBtn);
        header.add(filterRow, BorderLayout.EAST);
        contentPanel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Day", "Course", "Start", "End", "Location", "Type"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(38);
        table.setGridColor(UITheme.BORDER_COLOR);
        table.setSelectionBackground(new Color(56, 189, 248, 40));
        table.setSelectionForeground(UITheme.ACCENT);
        table.getTableHeader().setBackground(UITheme.BG_DARK);
        table.getTableHeader().setForeground(UITheme.ACCENT);
        table.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);


        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.BG_CARD);
                String day = v != null ? v.toString() : "";
                setForeground(switch (day) {
                    case "Monday"    -> UITheme.ACCENT;
                    case "Tuesday"   -> UITheme.ACCENT_GREEN;
                    case "Wednesday" -> UITheme.ACCENT_YELLOW;
                    case "Thursday"  -> UITheme.ACCENT_PURPLE;
                    case "Friday"    -> new Color(251, 113, 133);
                    default          -> UITheme.TEXT_PRIMARY;
                });
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return this;
            }
        });

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.BG_CARD);
                String type = v != null ? v.toString() : "";
                setForeground("Theory".equals(type) ? UITheme.ACCENT : UITheme.ACCENT_GREEN);
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        contentPanel.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        if (user.getRole().equals("ADMIN")) {
            JPanel bottom = UITheme.card();
            bottom.setLayout(new GridLayout(2, 6, 10, 10));

            JTextField dept  = UITheme.styledField("D01");


            JComboBox<String> day = new JComboBox<>(new String[]{
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
            });
            day.setBackground(UITheme.BG_CARD);
            day.setForeground(UITheme.TEXT_PRIMARY);

            JTextField course = UITheme.styledField("Course ID e.g. ICT1212");
            JTextField start  = UITheme.styledField("08:00:00");
            JTextField end    = UITheme.styledField("10:00:00");
            JTextField loc    = UITheme.styledField("e.g. Hall B1");

            JComboBox<String> type = new JComboBox<>(new String[]{"Theory", "Practical"});
            type.setBackground(UITheme.BG_CARD);
            type.setForeground(UITheme.TEXT_PRIMARY);

            JButton addBtn    = UITheme.primaryButton("Add");
            JButton deleteBtn = UITheme.dangerButton("Delete");

            bottom.add(UITheme.label("Dept ID",   UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(dept);
            bottom.add(UITheme.label("Day",       UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(day);
            bottom.add(UITheme.label("Course",    UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(course);
            bottom.add(UITheme.label("Start",     UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(start);
            bottom.add(UITheme.label("End",       UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(end);
            bottom.add(UITheme.label("Location",  UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(loc);
            bottom.add(UITheme.label("Type",      UITheme.TEXT_SECONDARY, UITheme.FONT_SMALL)); bottom.add(type);
            bottom.add(addBtn);
            bottom.add(deleteBtn);

            addBtn.addActionListener(e -> {
                String deptVal   = dept.getText().trim();
                String courseVal = course.getText().trim();
                String startVal  = start.getText().trim();
                String endVal    = end.getText().trim();
                String locVal    = loc.getText().trim();
                String dayVal    = (String) day.getSelectedItem();
                String typeVal   = (String) type.getSelectedItem();

                if (deptVal.isEmpty() || courseVal.isEmpty()) {
                    JOptionPane.showMessageDialog(contentPanel, "Dept ID and Course ID cannot be empty.");
                    return;
                }
                boolean ok = svc.add(deptVal, dayVal, courseVal, startVal, endVal, locVal, typeVal);
                if (ok) {
                    loadTable(model, deptField.getText().trim());
                } else {
                    JOptionPane.showMessageDialog(contentPanel, "Failed to add. Check Course ID and Dept ID exist.");
                }
            });

            deleteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(contentPanel, "Please select a row to delete.");
                    return;
                }
                svc.delete((String) model.getValueAt(row, 0));
                loadTable(model, deptField.getText().trim());
            });

            contentPanel.add(bottom, BorderLayout.SOUTH);
        }

        loadBtn.addActionListener(e -> loadTable(model, deptField.getText().trim()));

        loadTable(model, "D01");
    }

    private void loadTable(DefaultTableModel model, String dept) {
        model.setRowCount(0);
        List<Timetable> list = svc.getByDept(dept);
        if (list.isEmpty()) {
            System.out.println("No timetable entries found for dept: " + dept);
        }
        for (Timetable t : list) {
            model.addRow(new Object[]{
                t.getTimetableId(),
                t.getDayOfWeek(),
                t.getCourseId(),
                t.getStartTime(),
                t.getEndTime(),
                t.getLocation(),
                t.getSessionType()
            });
        }
    }
}
