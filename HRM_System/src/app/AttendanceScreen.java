package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class AttendanceScreen extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final MainScreen parent;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField dateField;

    public AttendanceScreen(MainScreen parent) {
        this.parent = parent;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setOpaque(false);

        // Header + date picker + record button
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(UIHelper.sectionHeader("Attendance", "Track daily employee attendance"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        dateField = UIHelper.styledField("YYYY-MM-DD");
        dateField.setText(LocalDate.now().toString());
        dateField.setPreferredSize(new Dimension(160, 40));
        dateField.addActionListener(e -> loadData());

        JButton loadBtn = UIHelper.outlineButton("Load");
        loadBtn.addActionListener(e -> loadData());

        JButton recordBtn = UIHelper.primaryButton("+ Record Attendance");
        recordBtn.addActionListener(e -> showRecordDialog());

        actions.add(dateField);
        actions.add(loadBtn);
        actions.add(recordBtn);
        headerRow.add(actions, BorderLayout.EAST);
        wrapper.add(headerRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Employee", "Department", "Check In", "Check Out", "Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(110);

        table.getColumnModel().getColumn(5).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel badge = UIHelper.badge(val == null ? "" : val.toString(), UIHelper.statusColor(val == null ? "" : val.toString()));
            if (sel) badge.setOpaque(true);
            return badge;
        });

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIHelper.styledScrollPane(table), BorderLayout.CENTER);
        wrapper.add(tableCard, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        String date = dateField.getText().trim();
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() {
                return dao.getAttendanceByDate(date);
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> data = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> a : data) {
                        tableModel.addRow(new Object[]{
                            a.get("id"), a.get("emp_name"), nullSafe(a.get("dept_name")),
                            nullSafe(a.get("check_in")), nullSafe(a.get("check_out")),
                            nullSafe(a.get("status")), nullSafe(a.get("notes"))
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void showRecordDialog() {
        JDialog dialog = new JDialog(parent.getFrame(), "Record Attendance", true);
        dialog.setSize(440, 440);
        dialog.setLocationRelativeTo(parent.getFrame());

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JLabel title = new JLabel("Record Attendance");
        title.setFont(UIHelper.FONT_SUBTITLE);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(24));

        // Employee dropdown
        List<Map<String, String>> emps = dao.getAllEmployees();
        String[] empNames = new String[emps.size() + 1];
        empNames[0] = "-- Select Employee --";
        for (int i = 0; i < emps.size(); i++) empNames[i + 1] = emps.get(i).get("first_name") + " " + emps.get(i).get("last_name");
        JComboBox<String> empCB = UIHelper.styledComboBox(empNames);

        JTextField dateF = UIHelper.styledField("YYYY-MM-DD");
        dateF.setText(dateField.getText());
        JTextField checkInF = UIHelper.styledField("HH:MM (e.g. 09:00)");
        JTextField checkOutF = UIHelper.styledField("HH:MM (e.g. 18:00)");
        JComboBox<String> statusCB = UIHelper.styledComboBox(new String[]{"Present", "Absent", "Late", "Half Day", "On Leave"});
        JTextField notesF = UIHelper.styledField("Optional notes");

        addFormField(form, "Employee *", empCB);
        addFormField(form, "Date", dateF);
        addFormField(form, "Check In", checkInF);
        addFormField(form, "Check Out", checkOutF);
        addFormField(form, "Status", statusCB);
        addFormField(form, "Notes", notesF);

        form.add(Box.createVerticalStrut(16));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIHelper.primaryButton("Save");
        saveBtn.addActionListener(e -> {
            if (empCB.getSelectedIndex() <= 0) { JOptionPane.showMessageDialog(dialog, "Select an employee."); return; }
            int empId = Integer.parseInt(emps.get(empCB.getSelectedIndex() - 1).get("id"));
            if (dao.recordAttendance(empId, dateF.getText().trim(), checkInF.getText().trim(),
                    checkOutF.getText().trim(), (String) statusCB.getSelectedItem(), notesF.getText().trim())) {
                UIHelper.showToast(parent.getFrame(), "Attendance recorded", true);
                dialog.dispose();
                loadData();
            } else {
                UIHelper.showToast(parent.getFrame(), "Failed to record attendance", false);
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