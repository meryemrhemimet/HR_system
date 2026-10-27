package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * EmployeeScreen — Full CRUD with search bar, styled table, add/edit dialogs.
 */
public class EmployeeScreen extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final MainScreen parent;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Map<String, String>> employees;

    public EmployeeScreen(MainScreen parent) {
        this.parent = parent;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setOpaque(false);

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel left = UIHelper.sectionHeader("Employees", "Manage your workforce");
        headerRow.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        searchField = UIHelper.searchField("Search employees...");
        searchField.addActionListener(e -> loadData());
        // Live search on key typed
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { loadData(); }
        });

        JButton addBtn = UIHelper.primaryButton("+ Add Employee");
        addBtn.addActionListener(e -> showEmployeeDialog(null));

        actions.add(searchField);
        actions.add(addBtn);
        headerRow.add(actions, BorderLayout.EAST);

        wrapper.add(headerRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Email", "Phone", "Department", "Position", "Contract", "Salary", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(6).setMaxWidth(90);
        table.getColumnModel().getColumn(8).setMaxWidth(100);

        // Custom renderer for status column
        table.getColumnModel().getColumn(8).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel badge = UIHelper.badge(val == null ? "" : val.toString(), UIHelper.statusColor(val == null ? "" : val.toString()));
            if (sel) badge.setBackground(new Color(79, 70, 229, 20));
            badge.setOpaque(sel);
            return badge;
        });

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());
        JScrollPane scrollPane = UIHelper.styledScrollPane(table);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setOpaque(false);

        JButton editBtn = UIHelper.outlineButton("Edit");
        editBtn.addActionListener(e -> editSelected());

        JButton deleteBtn = UIHelper.dangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteSelected());

        JButton refreshBtn = UIHelper.outlineButton("Refresh");
        refreshBtn.addActionListener(e -> { searchField.setText(""); loadData(); });

        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);
        bottomBar.add(refreshBtn);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        wrapper.add(tableCard, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        String keyword = searchField.getText().trim();
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() {
                return keyword.isEmpty() ? dao.getAllEmployees() : dao.searchEmployees(keyword);
            }
            @Override protected void done() {
                try {
                    employees = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> e : employees) {
                        String salary = e.get("base_salary") != null ? String.format("%,.0f MAD", Double.parseDouble(e.get("base_salary"))) : "";
                        tableModel.addRow(new Object[]{
                            e.get("id"),
                            e.get("first_name") + " " + e.get("last_name"),
                            e.get("email"),
                            e.get("phone"),
                            nullSafe(e.get("dept_name")),
                            nullSafe(e.get("position_title")),
                            nullSafe(e.get("contract_type")),
                            salary,
                            nullSafe(e.get("status"))
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select an employee to edit."); return; }
        showEmployeeDialog(employees.get(row));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select an employee to delete."); return; }
        Map<String, String> emp = employees.get(row);
        if (UIHelper.confirmDialog(this, "Delete Employee",
                "Delete " + emp.get("first_name") + " " + emp.get("last_name") + "? This cannot be undone.")) {
            if (dao.deleteEmployee(Integer.parseInt(emp.get("id")))) {
                UIHelper.showToast(parent.getFrame(), "Employee deleted successfully", true);
                loadData();
                parent.reloadDashboard(); // Refresh dashboard after delete
            } else {
                UIHelper.showToast(parent.getFrame(), "Failed to delete employee", false);
            }
        }
    }

    private void showEmployeeDialog(Map<String, String> existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(parent.getFrame(), isEdit ? "Edit Employee" : "Add Employee", true);
        dialog.setSize(560, 680);
        dialog.setLocationRelativeTo(parent.getFrame());
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JLabel titleLabel = new JLabel(isEdit ? "Edit Employee" : "New Employee");
        titleLabel.setFont(UIHelper.FONT_SUBTITLE);
        titleLabel.setForeground(UIHelper.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(titleLabel);
        form.add(Box.createVerticalStrut(20));

        // Two columns for name
        JPanel nameRow = new JPanel(new GridLayout(1, 2, 12, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(LEFT_ALIGNMENT);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JTextField firstNameF = UIHelper.styledField("First name");
        JTextField lastNameF = UIHelper.styledField("Last name");
        if (isEdit) { firstNameF.setText(existing.get("first_name")); lastNameF.setText(existing.get("last_name")); }

        JPanel fn = fieldGroup("First Name", firstNameF);
        JPanel ln = fieldGroup("Last Name", lastNameF);
        nameRow.add(fn); nameRow.add(ln);
        form.add(nameRow);
        form.add(Box.createVerticalStrut(12));

        JTextField emailF = UIHelper.styledField("Email address");
        JTextField phoneF = UIHelper.styledField("Phone number");
        JTextField dobF = UIHelper.styledField("YYYY-MM-DD");
        JTextField hireDateF = UIHelper.styledField("YYYY-MM-DD");
        JTextField nationalIdF = UIHelper.styledField("National ID");
        JTextField addressF = UIHelper.styledField("Address");
        JTextField cityF = UIHelper.styledField("City");
        JTextField salaryF = UIHelper.styledField("Base salary (MAD)");

        JComboBox<String> genderCB = UIHelper.styledComboBox(new String[]{"Male", "Female"});
        JComboBox<String> contractCB = UIHelper.styledComboBox(new String[]{"CDI", "CDD", "Freelance", "Intern"});

        // Department dropdown
        List<Map<String, String>> depts = dao.getAllDepartments();
        String[] deptNames = new String[depts.size() + 1];
        deptNames[0] = "-- Select Department --";
        for (int i = 0; i < depts.size(); i++) deptNames[i + 1] = depts.get(i).get("name");
        JComboBox<String> deptCB = UIHelper.styledComboBox(deptNames);

        // Position dropdown
        List<Map<String, String>> positions = dao.getAllPositions();
        String[] posNames = new String[positions.size() + 1];
        posNames[0] = "-- Select Position --";
        for (int i = 0; i < positions.size(); i++) posNames[i + 1] = positions.get(i).get("title");
        JComboBox<String> posCB = UIHelper.styledComboBox(posNames);

        JComboBox<String> statusCB = UIHelper.styledComboBox(new String[]{"Active", "On Leave", "Suspended", "Terminated"});

        if (isEdit) {
            emailF.setText(existing.get("email")); phoneF.setText(existing.get("phone"));
            dobF.setText(nullSafe(existing.get("date_of_birth"))); hireDateF.setText(existing.get("hire_date"));
            nationalIdF.setText(existing.get("national_id")); addressF.setText(existing.get("address"));
            cityF.setText(existing.get("city")); salaryF.setText(existing.get("base_salary"));
            genderCB.setSelectedItem(existing.get("gender"));
            contractCB.setSelectedItem(existing.get("contract_type"));
            statusCB.setSelectedItem(existing.get("status"));
            // Select dept
            for (int i = 0; i < depts.size(); i++) {
                if (depts.get(i).get("id").equals(existing.get("department_id"))) { deptCB.setSelectedIndex(i + 1); break; }
            }
            for (int i = 0; i < positions.size(); i++) {
                if (positions.get(i).get("id").equals(existing.get("position_id"))) { posCB.setSelectedIndex(i + 1); break; }
            }
        } else {
            hireDateF.setText(java.time.LocalDate.now().toString());
        }

        // Layout
        addField(form, "Email", emailF);
        addField(form, "Phone", phoneF);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false); row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row2.add(fieldGroup("Date of Birth", dobF));
        row2.add(fieldGroup("Gender", genderCB));
        form.add(row2);
        form.add(Box.createVerticalStrut(12));

        addField(form, "National ID", nationalIdF);

        JPanel row3 = new JPanel(new GridLayout(1, 2, 12, 0));
        row3.setOpaque(false); row3.setAlignmentX(LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row3.add(fieldGroup("Address", addressF));
        row3.add(fieldGroup("City", cityF));
        form.add(row3);
        form.add(Box.createVerticalStrut(12));

        JPanel row4 = new JPanel(new GridLayout(1, 2, 12, 0));
        row4.setOpaque(false); row4.setAlignmentX(LEFT_ALIGNMENT);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row4.add(fieldGroup("Department", deptCB));
        row4.add(fieldGroup("Position", posCB));
        form.add(row4);
        form.add(Box.createVerticalStrut(12));

        JPanel row5 = new JPanel(new GridLayout(1, 3, 12, 0));
        row5.setOpaque(false); row5.setAlignmentX(LEFT_ALIGNMENT);
        row5.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row5.add(fieldGroup("Hire Date", hireDateF));
        row5.add(fieldGroup("Contract", contractCB));
        row5.add(fieldGroup("Salary (MAD)", salaryF));
        form.add(row5);

        if (isEdit) {
            form.add(Box.createVerticalStrut(12));
            addField(form, "Status", statusCB);
        }

        // Buttons
        form.add(Box.createVerticalStrut(24));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIHelper.primaryButton(isEdit ? "Update" : "Add Employee");
        saveBtn.addActionListener(e -> {
            // Validate
            if (firstNameF.getText().trim().isEmpty() || lastNameF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "First and last name are required."); return;
            }
            if (emailF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Email is required."); return;
            }
            if (deptCB.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a department."); return;
            }
            if (posCB.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a position."); return;
            }

            int deptId = Integer.parseInt(depts.get(deptCB.getSelectedIndex() - 1).get("id"));
            int posId = Integer.parseInt(positions.get(posCB.getSelectedIndex() - 1).get("id"));
            double salary;
            try { salary = Double.parseDouble(salaryF.getText().trim()); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid salary value."); return;
            }

            boolean ok;
            if (isEdit) {
                ok = dao.updateEmployee(Integer.parseInt(existing.get("id")),
                    firstNameF.getText().trim(), lastNameF.getText().trim(), emailF.getText().trim(),
                    phoneF.getText().trim(), dobF.getText().trim(), (String) genderCB.getSelectedItem(),
                    nationalIdF.getText().trim(), addressF.getText().trim(), cityF.getText().trim(),
                    deptId, posId, (String) contractCB.getSelectedItem(), salary, (String) statusCB.getSelectedItem());
            } else {
                ok = dao.addEmployee(firstNameF.getText().trim(), lastNameF.getText().trim(), emailF.getText().trim(),
                    phoneF.getText().trim(), dobF.getText().trim(), (String) genderCB.getSelectedItem(),
                    nationalIdF.getText().trim(), addressF.getText().trim(), cityF.getText().trim(),
                    deptId, posId, hireDateF.getText().trim(), (String) contractCB.getSelectedItem(), salary);
            }

            if (ok) {
                UIHelper.showToast(parent.getFrame(), isEdit ? "Employee updated" : "Employee added", true);
                dialog.dispose();
                loadData();
            } else {
                UIHelper.showToast(parent.getFrame(), "Operation failed. Check data.", false);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        form.add(btnRow);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(sp, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void addField(JPanel form, String label, JComponent field) {
        JPanel group = fieldGroup(label, field);
        form.add(group);
        form.add(Box.createVerticalStrut(12));
    }

    private JPanel fieldGroup(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIHelper.FONT_SMALL);
        lbl.setForeground(UIHelper.TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 2, 4, 0));
        field.setAlignmentX(LEFT_ALIGNMENT);
        if (field instanceof JTextField) {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIHelper.INPUT_HEIGHT));
        }
        p.add(lbl);
        p.add(field);
        return p;
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}