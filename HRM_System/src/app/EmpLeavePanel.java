package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

public class EmpLeavePanel extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final EmpMainScreen parent;
    private final int empId;
    private DefaultTableModel model;
    private JPanel statsRow;
    private JPanel root;

    public EmpLeavePanel(EmpMainScreen parent, int empId) {
        this.parent = parent;
        this.empId = empId;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());

        root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(28, 36, 36, 36));

        // Header with action button
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        hdr.add(UIHelper.sectionHeader("My Leaves", "Manage your time off and view your balance"), BorderLayout.WEST);
        JButton add = UIHelper.primaryButton("+ Request Leave");
        add.addActionListener(e -> showRequestDialog());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT)); right.setOpaque(false); right.add(add);
        hdr.add(right, BorderLayout.EAST);
        root.add(hdr);
        root.add(Box.createVerticalStrut(20));

        // Leave balance row
        statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        rebuildStats();
        root.add(statsRow);
        root.add(Box.createVerticalStrut(20));

        // Table
        String[] cols = {"ID", "Type", "Start Date", "End Date", "Days", "Reason", "Status"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = UIHelper.styledTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setMaxWidth(60);
        table.getColumnModel().getColumn(6).setMaxWidth(110);
        table.getColumnModel().getColumn(6).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel b = UIHelper.badge(val == null ? "" : val.toString(), UIHelper.statusColor(val == null ? "" : val.toString()));
            if (sel) b.setOpaque(true); return b;
        });

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());
        JPanel th = new JPanel(new BorderLayout()); th.setOpaque(false); th.setBorder(new EmptyBorder(16, 20, 8, 20));
        JLabel tt = new JLabel("Leave History"); tt.setFont(UIHelper.FONT_HEADING); tt.setForeground(UIHelper.TEXT_PRIMARY);
        th.add(tt, BorderLayout.WEST);
        tableCard.add(th, BorderLayout.NORTH);
        tableCard.add(UIHelper.styledScrollPane(table), BorderLayout.CENTER);
        tableCard.setAlignmentX(LEFT_ALIGNMENT);
        root.add(tableCard);

        JScrollPane sp = UIHelper.styledScrollPane(root);
        sp.getViewport().setBackground(UIHelper.BG);
        add(sp, BorderLayout.CENTER);
        loadData();
    }

    private void rebuildStats() {
        statsRow.removeAll();
        List<Map<String, String>> bal = dao.getMyLeaveBalance(empId);
        Color[] cols = {UIHelper.PRIMARY, UIHelper.ROSE, UIHelper.PURPLE, UIHelper.AMBER};
        String[] icons = {"A", "S", "M", "U"};
        for (int i = 0; i < Math.min(4, bal.size()); i++) {
            Map<String, String> lt = bal.get(i);
            int max = parseInt(lt.get("max_days"), 0);
            int taken = parseInt(lt.get("days_taken"), 0);
            int remaining = Math.max(0, max - taken);
            statsRow.add(UIHelper.statCard(lt.get("name"), remaining + " days", taken + " of " + max + " used", cols[i % 4], icons[i % 4]));
        }
        statsRow.revalidate();
        statsRow.repaint();
    }

    private void loadData() {
        model.setRowCount(0);
        for (Map<String, String> l : dao.getMyLeaves(empId)) {
            model.addRow(new Object[]{l.get("id"), ns(l.get("leave_type")), l.get("start_date"), l.get("end_date"),
                l.get("total_days"), ns(l.get("reason")), l.get("status")});
        }
    }

    private void showRequestDialog() {
        JDialog d = new JDialog(parent.getFrame(), "Request Leave", true);
        d.setSize(460, 480);
        d.setLocationRelativeTo(parent.getFrame());

        JPanel f = new JPanel();
        f.setBackground(Color.WHITE);
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setBorder(new EmptyBorder(26, 30, 22, 30));

        JLabel t = new JLabel("New Leave Request"); t.setFont(UIHelper.FONT_SUBTITLE); t.setForeground(UIHelper.TEXT_PRIMARY); t.setAlignmentX(LEFT_ALIGNMENT);
        JLabel s = new JLabel("Submit your time-off request for HR approval"); s.setFont(UIHelper.FONT_SMALL); s.setForeground(UIHelper.TEXT_SECONDARY); s.setAlignmentX(LEFT_ALIGNMENT);
        s.setBorder(new EmptyBorder(4, 0, 18, 0));
        f.add(t); f.add(s);

        List<Map<String, String>> types = dao.getLeaveTypes();
        String[] tn = new String[types.size() + 1]; tn[0] = "-- Select Type --";
        for (int i = 0; i < types.size(); i++) tn[i + 1] = types.get(i).get("name");
        JComboBox<String> typeCB = UIHelper.styledComboBox(tn);
        JTextField startF = UIHelper.styledField("YYYY-MM-DD");
        JTextField endF = UIHelper.styledField("YYYY-MM-DD");
        JTextField daysF = UIHelper.styledField("Auto-calculated");
        daysF.setEditable(false);
        JTextField reasonF = UIHelper.styledField("Brief reason");

        // Auto-calc days when both dates filled
        Runnable recalc = () -> {
            try {
                LocalDate a = LocalDate.parse(startF.getText().trim());
                LocalDate b = LocalDate.parse(endF.getText().trim());
                long days = ChronoUnit.DAYS.between(a, b) + 1;
                if (days > 0) daysF.setText(String.valueOf(days)); else daysF.setText("—");
            } catch (Exception ex) { daysF.setText("—"); }
        };
        startF.addActionListener(e -> recalc.run());
        endF.addActionListener(e -> recalc.run());
        startF.addFocusListener(new java.awt.event.FocusAdapter() { public void focusLost(java.awt.event.FocusEvent e) { recalc.run(); } });
        endF.addFocusListener(new java.awt.event.FocusAdapter() { public void focusLost(java.awt.event.FocusEvent e) { recalc.run(); } });

        addF(f, "Leave Type *", typeCB);
        JPanel datesRow = new JPanel(new GridLayout(1, 3, 12, 0));
        datesRow.setOpaque(false); datesRow.setAlignmentX(LEFT_ALIGNMENT);
        datesRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        datesRow.add(fieldGroup("Start Date *", startF));
        datesRow.add(fieldGroup("End Date *", endF));
        datesRow.add(fieldGroup("Days", daysF));
        f.add(datesRow); f.add(Box.createVerticalStrut(12));
        addF(f, "Reason", reasonF);

        f.add(Box.createVerticalStrut(16));
        JPanel br = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); br.setOpaque(false); br.setAlignmentX(LEFT_ALIGNMENT);
        JButton cancel = UIHelper.outlineButton("Cancel"); cancel.addActionListener(e -> d.dispose());
        JButton save = UIHelper.primaryButton("Submit Request");
        save.addActionListener(e -> {
            if (typeCB.getSelectedIndex() <= 0) { JOptionPane.showMessageDialog(d, "Please select a leave type."); return; }
            if (startF.getText().trim().isEmpty() || endF.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(d, "Start and end dates are required."); return; }
            int days; try { days = Integer.parseInt(daysF.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(d, "Invalid date range."); return; }
            if (days <= 0) { JOptionPane.showMessageDialog(d, "End date must be after start date."); return; }
            int typeId = Integer.parseInt(types.get(typeCB.getSelectedIndex() - 1).get("id"));
            if (dao.addLeaveRequest(empId, typeId, startF.getText().trim(), endF.getText().trim(), days, reasonF.getText().trim())) {
                UIHelper.showToast(parent.getFrame(), "Leave request submitted", true);
                d.dispose(); loadData(); rebuildStats();
            } else UIHelper.showToast(parent.getFrame(), "Failed to submit request", false);
        });
        br.add(cancel); br.add(save); f.add(br);

        d.add(new JScrollPane(f));
        d.setVisible(true);
    }

    private void addF(JPanel f, String label, JComponent field) {
        JPanel g = fieldGroup(label, field);
        g.setAlignmentX(LEFT_ALIGNMENT);
        f.add(g); f.add(Box.createVerticalStrut(12));
    }

    private JPanel fieldGroup(String label, JComponent field) {
        JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label); l.setFont(UIHelper.FONT_SMALL); l.setForeground(UIHelper.TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(0, 2, 4, 0)); l.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        if (field instanceof JTextField) field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIHelper.INPUT_HEIGHT));
        p.add(l); p.add(field); return p;
    }

    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private static String ns(String s) { return s == null ? "" : s; }
}
