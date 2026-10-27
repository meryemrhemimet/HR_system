package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class PerformanceScreen extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final MainScreen parent;
    private JTable table;
    private DefaultTableModel tableModel;

    public PerformanceScreen(MainScreen parent) {
        this.parent = parent;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(UIHelper.sectionHeader("Performance Reviews", "Evaluate and track employee performance"), BorderLayout.WEST);

        JButton addBtn = UIHelper.primaryButton("+ New Review");
        addBtn.addActionListener(e -> showAddDialog());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(addBtn);
        headerRow.add(right, BorderLayout.EAST);
        wrapper.add(headerRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Employee", "Department", "Period", "Date", "Productivity", "Quality", "Communication", "Teamwork", "Initiative", "Overall"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        // Color the Overall column
        table.getColumnModel().getColumn(10).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            double rating = 0;
            try { rating = Double.parseDouble(val.toString()); } catch (Exception ignored) {}
            Color c = rating >= 4.0 ? UIHelper.SUCCESS : rating >= 3.0 ? UIHelper.WARNING : UIHelper.DANGER;
            JLabel badge = UIHelper.badge(String.format("%.1f ★", rating), c);
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
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() {
                return dao.getAllReviews();
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> reviews = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> r : reviews) {
                        tableModel.addRow(new Object[]{
                            r.get("id"), r.get("emp_name"), nullSafe(r.get("dept_name")),
                            nullSafe(r.get("period")), r.get("review_date"),
                            starStr(r.get("productivity")), starStr(r.get("quality")),
                            starStr(r.get("communication")), starStr(r.get("teamwork")),
                            starStr(r.get("initiative")), r.get("overall_rating")
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(parent.getFrame(), "New Performance Review", true);
        dialog.setSize(520, 620);
        dialog.setLocationRelativeTo(parent.getFrame());

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JLabel title = new JLabel("New Performance Review");
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

        JTextField periodF = UIHelper.styledField("e.g. Q1 2026");
        JTextField dateF = UIHelper.styledField("YYYY-MM-DD");
        dateF.setText(LocalDate.now().toString());

        String[] ratings = {"1", "2", "3", "4", "5"};
        JComboBox<String> prodCB = UIHelper.styledComboBox(ratings); prodCB.setSelectedIndex(3);
        JComboBox<String> qualCB = UIHelper.styledComboBox(ratings); qualCB.setSelectedIndex(3);
        JComboBox<String> commCB = UIHelper.styledComboBox(ratings); commCB.setSelectedIndex(3);
        JComboBox<String> teamCB = UIHelper.styledComboBox(ratings); teamCB.setSelectedIndex(3);
        JComboBox<String> initCB = UIHelper.styledComboBox(ratings); initCB.setSelectedIndex(3);

        JTextField strengthsF = UIHelper.styledField("Key strengths");
        JTextField improvementsF = UIHelper.styledField("Areas for improvement");
        JTextField goalsF = UIHelper.styledField("Goals for next period");

        addFormField(form, "Employee *", empCB);
        addFormField(form, "Review Period", periodF);
        addFormField(form, "Review Date", dateF);

        // Ratings in grid
        JPanel ratingsGrid = new JPanel(new GridLayout(1, 5, 8, 0));
        ratingsGrid.setOpaque(false);
        ratingsGrid.setAlignmentX(LEFT_ALIGNMENT);
        ratingsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        ratingsGrid.add(ratingGroup("Productivity", prodCB));
        ratingsGrid.add(ratingGroup("Quality", qualCB));
        ratingsGrid.add(ratingGroup("Communication", commCB));
        ratingsGrid.add(ratingGroup("Teamwork", teamCB));
        ratingsGrid.add(ratingGroup("Initiative", initCB));

        JLabel ratLabel = new JLabel("Ratings (1-5)");
        ratLabel.setFont(UIHelper.FONT_SMALL);
        ratLabel.setForeground(UIHelper.TEXT_SECONDARY);
        ratLabel.setAlignmentX(LEFT_ALIGNMENT);
        ratLabel.setBorder(new EmptyBorder(0, 2, 4, 0));
        form.add(ratLabel);
        form.add(ratingsGrid);
        form.add(Box.createVerticalStrut(14));

        addFormField(form, "Strengths", strengthsF);
        addFormField(form, "Areas for Improvement", improvementsF);
        addFormField(form, "Goals", goalsF);

        form.add(Box.createVerticalStrut(12));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIHelper.primaryButton("Submit Review");
        saveBtn.addActionListener(e -> {
            if (empCB.getSelectedIndex() <= 0) { JOptionPane.showMessageDialog(dialog, "Select an employee."); return; }
            int empId = Integer.parseInt(emps.get(empCB.getSelectedIndex() - 1).get("id"));
            int p = Integer.parseInt((String) prodCB.getSelectedItem());
            int q = Integer.parseInt((String) qualCB.getSelectedItem());
            int c = Integer.parseInt((String) commCB.getSelectedItem());
            int t = Integer.parseInt((String) teamCB.getSelectedItem());
            int in = Integer.parseInt((String) initCB.getSelectedItem());
            double overall = (p + q + c + t + in) / 5.0;

            if (dao.addReview(empId, dateF.getText().trim(), periodF.getText().trim(),
                    p, q, c, t, in, overall,
                    strengthsF.getText().trim(), improvementsF.getText().trim(), goalsF.getText().trim())) {
                UIHelper.showToast(parent.getFrame(), String.format("Review submitted — Overall: %.1f", overall), true);
                dialog.dispose();
                loadData();
            } else {
                UIHelper.showToast(parent.getFrame(), "Failed to submit review", false);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        form.add(btnRow);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(sp);
        dialog.setVisible(true);
    }

    private JPanel ratingGroup(String label, JComboBox<String> cb) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIHelper.FONT_TINY);
        lbl.setForeground(UIHelper.TEXT_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        cb.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(cb);
        return p;
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

    private static String starStr(String val) {
        try { int v = Integer.parseInt(val); return "★".repeat(v) + "☆".repeat(5 - v); }
        catch (Exception e) { return val; }
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}