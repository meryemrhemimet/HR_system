package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * DepartmentScreen — CRUD for departments with headcount display.
 */
public class DepartmentScreen extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final MainScreen parent;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Map<String, String>> departments;

    public DepartmentScreen(MainScreen parent) {
        this.parent = parent;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setOpaque(false);

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(UIHelper.sectionHeader("Departments", "Manage organizational units"), BorderLayout.WEST);

        JButton addBtn = UIHelper.primaryButton("+ Add Department");
        addBtn.addActionListener(e -> showDialog(null));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(addBtn);
        headerRow.add(right, BorderLayout.EAST);

        wrapper.add(headerRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Location", "Budget (MAD)", "Headcount", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setMaxWidth(100);

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIHelper.styledScrollPane(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setOpaque(false);

        JButton editBtn = UIHelper.outlineButton("Edit");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) showDialog(departments.get(row));
            else JOptionPane.showMessageDialog(this, "Select a department to edit.");
        });

        JButton deleteBtn = UIHelper.dangerButton("Delete");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a department to delete."); return; }
            Map<String, String> dept = departments.get(row);
            if (UIHelper.confirmDialog(this, "Delete Department", "Delete " + dept.get("name") + "?")) {
                if (dao.deleteDepartment(Integer.parseInt(dept.get("id")))) {
                    UIHelper.showToast(parent.getFrame(), "Department deleted", true);
                    loadData();
                    parent.reloadDashboard(); // Refresh dashboard
                } else {
                    UIHelper.showToast(parent.getFrame(), "Cannot delete — employees may be assigned", false);
                }
            }
        });

        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        wrapper.add(tableCard, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() {
                return dao.getAllDepartments();
            }
            @Override protected void done() {
                try {
                    departments = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> d : departments) {
                        String budget = d.get("budget") != null ? String.format("%,.0f", Double.parseDouble(d.get("budget"))) : "0";
                        tableModel.addRow(new Object[]{
                            d.get("id"), d.get("name"), nullSafe(d.get("location")),
                            budget, nullSafe(d.get("headcount")), nullSafe(d.get("description"))
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void showDialog(Map<String, String> existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(parent.getFrame(), isEdit ? "Edit Department" : "Add Department", true);
        dialog.setSize(440, 420);
        dialog.setLocationRelativeTo(parent.getFrame());

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JLabel title = new JLabel(isEdit ? "Edit Department" : "New Department");
        title.setFont(UIHelper.FONT_SUBTITLE);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(24));

        JTextField nameF = UIHelper.styledField("Department name");
        JTextField locF = UIHelper.styledField("Location");
        JTextField budgetF = UIHelper.styledField("Budget (MAD)");
        JTextField descF = UIHelper.styledField("Description");

        if (isEdit) {
            nameF.setText(existing.get("name")); locF.setText(existing.get("location"));
            budgetF.setText(existing.get("budget")); descF.setText(existing.get("description"));
        }

        addFormField(form, "Name *", nameF);
        addFormField(form, "Location", locF);
        addFormField(form, "Budget (MAD)", budgetF);
        addFormField(form, "Description", descF);

        form.add(Box.createVerticalStrut(20));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIHelper.primaryButton(isEdit ? "Update" : "Add");
        saveBtn.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dialog, "Name is required."); return; }
            double budget;
            try { budget = budgetF.getText().trim().isEmpty() ? 0 : Double.parseDouble(budgetF.getText().trim()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dialog, "Invalid budget."); return; }

            boolean ok = isEdit
                ? dao.updateDepartment(Integer.parseInt(existing.get("id")), nameF.getText().trim(), descF.getText().trim(), locF.getText().trim(), budget)
                : dao.addDepartment(nameF.getText().trim(), descF.getText().trim(), locF.getText().trim(), budget);

            if (ok) {
                UIHelper.showToast(parent.getFrame(), isEdit ? "Department updated" : "Department added", true);
                dialog.dispose();
                loadData();
                parent.reloadDashboard(); // Refresh dashboard
            } else {
                UIHelper.showToast(parent.getFrame(), "Operation failed", false);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        form.add(btnRow);
        dialog.add(form);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel form, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIHelper.FONT_SMALL);
        lbl.setForeground(UIHelper.TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 2, 4, 0));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIHelper.INPUT_HEIGHT));
        form.add(lbl);
        form.add(field);
        form.add(Box.createVerticalStrut(14));
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}