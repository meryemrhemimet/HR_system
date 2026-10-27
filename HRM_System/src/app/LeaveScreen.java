package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class LeaveScreen extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final MainScreen parent;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Map<String, String>> leaves;

    public LeaveScreen(MainScreen parent) {
        this.parent = parent;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(UIHelper.sectionHeader("Leave Requests", "Manage employee leave applications"), BorderLayout.WEST);

        JButton addBtn = UIHelper.primaryButton("+ New Leave Request");
        addBtn.addActionListener(e -> showAddDialog());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(addBtn);
        headerRow.add(right, BorderLayout.EAST);
        wrapper.add(headerRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Employee", "Leave Type", "Start", "End", "Days", "Reason", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(7).setMaxWidth(110);

        table.getColumnModel().getColumn(7).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel badge = UIHelper.badge(val == null ? "" : val.toString(), UIHelper.statusColor(val == null ? "" : val.toString()));
            if (sel) badge.setOpaque(true);
            return badge;
        });

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIHelper.styledScrollPane(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setOpaque(false);

        JButton approveBtn = UIHelper.successButton("Approve");
        approveBtn.addActionListener(e -> updateStatus("Approved"));

        JButton rejectBtn = UIHelper.dangerButton("Reject");
        rejectBtn.addActionListener(e -> updateStatus("Rejected"));

        bottomBar.add(approveBtn);
        bottomBar.add(rejectBtn);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        wrapper.add(tableCard, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() {
                return dao.getAllLeaveRequests();
            }
            @Override protected void done() {
                try {
                    leaves = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> l : leaves) {
                        tableModel.addRow(new Object[]{
                            l.get("id"), l.get("emp_name"), l.get("leave_type"),
                            l.get("start_date"), l.get("end_date"), l.get("total_days"),
                            nullSafe(l.get("reason")), l.get("status")
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void updateStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a leave request."); return; }
        Map<String, String> leave = leaves.get(row);
        if (!"Pending".equals(leave.get("status"))) {
            JOptionPane.showMessageDialog(this, "Only pending requests can be " + status.toLowerCase() + "."); return;
        }
        String notes = JOptionPane.showInputDialog(this, "Add a note (optional):");
        int userId = Integer.parseInt(parent.getUser().get("id"));
        if (dao.updateLeaveStatus(Integer.parseInt(leave.get("id")), status, userId, notes == null ? "" : notes)) {
            UIHelper.showToast(parent.getFrame(), "Leave " + status.toLowerCase(), true);
            loadData();
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(parent.getFrame(), "New Leave Request", true);
        dialog.setSize(460, 460);
        dialog.setLocationRelativeTo(parent.getFrame());

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JLabel title = new JLabel("New Leave Request");
        title.setFont(UIHelper.FONT_SUBTITLE);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(24));

        List<Map<String, String>> emps = dao.getAllEmployees();
        String[] empNames = new String[emps.size() + 1];
        empNames[0] = "-- Select Employee --";
        for (int i = 0; i < emps.size(); i++) empNames[i + 1] = emps.get(i).get("first_name") + " " + emps.get(i).get("last_name");
        JComboBox<String> empCB = UIHelper.styledComboBox(empNames);

        List<Map<String, String>> types = dao.getLeaveTypes();
        String[] typeNames = new String[types.size() + 1];
        typeNames[0] = "-- Select Type --";
        for (int i = 0; i < types.size(); i++) typeNames[i + 1] = types.get(i).get("name");
        JComboBox<String> typeCB = UIHelper.styledComboBox(typeNames);

        JTextField startF = UIHelper.styledField("YYYY-MM-DD");
        JTextField endF = UIHelper.styledField("YYYY-MM-DD");
        JTextField daysF = UIHelper.styledField("Number of days");
        JTextField reasonF = UIHelper.styledField("Reason for leave");

        addFormField(form, "Employee *", empCB);
        addFormField(form, "Leave Type *", typeCB);
        addFormField(form, "Start Date *", startF);
        addFormField(form, "End Date *", endF);
        addFormField(form, "Total Days *", daysF);
        addFormField(form, "Reason", reasonF);

        form.add(Box.createVerticalStrut(16));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIHelper.primaryButton("Submit");
        saveBtn.addActionListener(e -> {
            if (empCB.getSelectedIndex() <= 0 || typeCB.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(dialog, "Select employee and leave type."); return; }
            int empId = Integer.parseInt(emps.get(empCB.getSelectedIndex() - 1).get("id"));
            int typeId = Integer.parseInt(types.get(typeCB.getSelectedIndex() - 1).get("id"));
            int days;
            try { days = Integer.parseInt(daysF.getText().trim()); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid days value."); return; }
            if (dao.addLeaveRequest(empId, typeId, startF.getText().trim(), endF.getText().trim(), days, reasonF.getText().trim())) {
                UIHelper.showToast(parent.getFrame(), "Leave request submitted", true);
                dialog.dispose();
                loadData();
            } else {
                UIHelper.showToast(parent.getFrame(), "Failed to submit", false);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        form.add(btnRow);
        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
    }

    private void addFormField(JPanel form, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIHelper.FONT_SMALL);
        lbl.setForeground(UIHelper.TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 2, 4, 0));
        field.setAlignmentX(LEFT_ALIGNMENT);
        if (field instanceof JTextField) field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIHelper.INPUT_HEIGHT));
        form.add(lbl);
        form.add(field);
        form.add(Box.createVerticalStrut(12));
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}