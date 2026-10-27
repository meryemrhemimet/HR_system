package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class PayrollScreen extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final MainScreen parent;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Map<String, String>> payrolls;
    private JComboBox<String> monthCB, yearCB;

    public PayrollScreen(MainScreen parent) {
        this.parent = parent;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(UIHelper.sectionHeader("Payroll", "Process and manage employee salaries"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        monthCB = UIHelper.styledComboBox(months);
        monthCB.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthCB.setPreferredSize(new Dimension(90, 40));

        String[] years = new String[5];
        int curYear = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) years[i] = String.valueOf(curYear - 2 + i);
        yearCB = UIHelper.styledComboBox(years);
        yearCB.setSelectedItem(String.valueOf(curYear));
        yearCB.setPreferredSize(new Dimension(90, 40));

        JButton loadBtn = UIHelper.outlineButton("Load");
        loadBtn.addActionListener(e -> loadData());

        JButton genBtn = UIHelper.primaryButton("+ Generate Payroll");
        genBtn.addActionListener(e -> showGenerateDialog());

        actions.add(monthCB);
        actions.add(yearCB);
        actions.add(loadBtn);
        actions.add(genBtn);
        headerRow.add(actions, BorderLayout.EAST);
        wrapper.add(headerRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Employee", "Department", "Base Salary", "OT Pay", "Bonuses", "Deductions", "Tax", "CNSS", "Net Salary", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(10).setMaxWidth(100);

        table.getColumnModel().getColumn(10).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel badge = UIHelper.badge(val == null ? "" : val.toString(), UIHelper.statusColor(val == null ? "" : val.toString()));
            if (sel) badge.setOpaque(true);
            return badge;
        });

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIHelper.styledScrollPane(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setOpaque(false);

        JButton markPaidBtn = UIHelper.successButton("Mark as Paid");
        markPaidBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a payroll record."); return; }
            Map<String, String> p = payrolls.get(row);
            if ("Paid".equals(p.get("payment_status"))) { JOptionPane.showMessageDialog(this, "Already paid."); return; }
            if (dao.markPayrollPaid(Integer.parseInt(p.get("id")))) {
                UIHelper.showToast(parent.getFrame(), "Marked as paid", true);
                loadData();
            }
        });
        bottomBar.add(markPaidBtn);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        wrapper.add(tableCard, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        int month = monthCB.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCB.getSelectedItem());
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() {
                return dao.getPayrollByPeriod(month, year);
            }
            @Override protected void done() {
                try {
                    payrolls = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> p : payrolls) {
                        tableModel.addRow(new Object[]{
                            p.get("id"), p.get("emp_name"), nullSafe(p.get("dept_name")),
                            fmt(p.get("base_salary")), fmt(p.get("overtime_pay")), fmt(p.get("bonuses")),
                            fmt(p.get("deductions")), fmt(p.get("tax_amount")), fmt(p.get("cnss")),
                            fmt(p.get("net_salary")), p.get("payment_status")
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void showGenerateDialog() {
        JDialog dialog = new JDialog(parent.getFrame(), "Generate Payroll", true);
        dialog.setSize(480, 540);
        dialog.setLocationRelativeTo(parent.getFrame());

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JLabel title = new JLabel("Generate Payroll");
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

        JTextField baseSalaryF = UIHelper.styledField("Auto-filled from employee");
        JTextField otHoursF = UIHelper.styledField("0");
        JTextField bonusesF = UIHelper.styledField("0");
        JTextField deductionsF = UIHelper.styledField("0");
        JLabel netLabel = new JLabel("Net: —");
        netLabel.setFont(UIHelper.FONT_HEADING);
        netLabel.setForeground(UIHelper.SUCCESS);
        netLabel.setAlignmentX(LEFT_ALIGNMENT);

        empCB.addActionListener(e -> {
            int idx = empCB.getSelectedIndex();
            if (idx > 0) baseSalaryF.setText(emps.get(idx - 1).get("base_salary"));
        });

        addFormField(form, "Employee *", empCB);
        addFormField(form, "Base Salary (MAD)", baseSalaryF);
        addFormField(form, "Overtime Hours", otHoursF);
        addFormField(form, "Bonuses (MAD)", bonusesF);
        addFormField(form, "Deductions (MAD)", deductionsF);
        form.add(Box.createVerticalStrut(8));
        form.add(netLabel);
        form.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIHelper.primaryButton("Generate");
        saveBtn.addActionListener(e -> {
            if (empCB.getSelectedIndex() <= 0) { JOptionPane.showMessageDialog(dialog, "Select an employee."); return; }
            try {
                double base = Double.parseDouble(baseSalaryF.getText().trim());
                double otH = otHoursF.getText().trim().isEmpty() ? 0 : Double.parseDouble(otHoursF.getText().trim());
                double bonus = bonusesF.getText().trim().isEmpty() ? 0 : Double.parseDouble(bonusesF.getText().trim());
                double ded = deductionsF.getText().trim().isEmpty() ? 0 : Double.parseDouble(deductionsF.getText().trim());

                // Moroccan-style calculation
                double otPay = otH * (base / 191.0) * 1.25;  // 191 = avg monthly hours
                double gross = base + otPay + bonus - ded;
                double tax = gross * 0.20;  // simplified IR
                double cnss = base * 0.0448;  // CNSS employee share
                double net = gross - tax - cnss;

                int empId = Integer.parseInt(emps.get(empCB.getSelectedIndex() - 1).get("id"));
                int month = monthCB.getSelectedIndex() + 1;
                int year = Integer.parseInt((String) yearCB.getSelectedItem());

                if (dao.generatePayroll(empId, month, year, base, otH, otPay, bonus, ded, tax, cnss, net)) {
                    UIHelper.showToast(parent.getFrame(), String.format("Payroll generated — Net: %,.0f MAD", net), true);
                    dialog.dispose();
                    loadData();
                } else {
                    UIHelper.showToast(parent.getFrame(), "Failed to generate", false);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid numeric values.");
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

    private static String fmt(String val) {
        try { return String.format("%,.0f", Double.parseDouble(val)); } catch (Exception e) { return "0"; }
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}