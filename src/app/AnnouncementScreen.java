package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AnnouncementScreen extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final MainScreen parent;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Map<String, String>> announcements;

    public AnnouncementScreen(MainScreen parent) {
        this.parent = parent;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(UIHelper.sectionHeader("Announcements", "Publish important updates for the team"), BorderLayout.WEST);

        JButton addBtn = UIHelper.primaryButton("+ New Announcement");
        addBtn.addActionListener(e -> showAddDialog());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(addBtn);
        headerRow.add(right, BorderLayout.EAST);
        wrapper.add(headerRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Priority", "Content", "Expires", "Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(110);
        table.getColumnModel().getColumn(4).setMaxWidth(110);
        table.getColumnModel().getColumn(5).setMaxWidth(110);

        table.getColumnModel().getColumn(2).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            Color c = priorityColor(val == null ? "" : val.toString());
            JLabel badge = UIHelper.badge(val == null ? "" : val.toString(), c);
            if (sel) badge.setOpaque(true);
            return badge;
        });

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIHelper.styledScrollPane(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setOpaque(false);

        JButton deleteBtn = UIHelper.dangerButton("Delete");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an announcement."); return; }
            Map<String, String> ann = announcements.get(row);
            if (UIHelper.confirmDialog(this, "Delete", "Delete \"" + ann.get("title") + "\"?")) {
                if (dao.deleteAnnouncement(Integer.parseInt(ann.get("id")))) {
                    UIHelper.showToast(parent.getFrame(), "Announcement deleted", true);
                    loadData();
                }
            }
        });

        JButton refreshBtn = UIHelper.outlineButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());

        bottomBar.add(deleteBtn);
        bottomBar.add(refreshBtn);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        wrapper.add(tableCard, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() {
                return dao.getAllAnnouncements();
            }
            @Override protected void done() {
                try {
                    announcements = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> a : announcements) {
                        String content = nullSafe(a.get("content"));
                        if (content.length() > 80) content = content.substring(0, 80) + "...";
                        tableModel.addRow(new Object[]{
                            a.get("id"), a.get("title"), a.get("priority"),
                            content, nullSafe(a.get("expires_at")),
                            nullSafe(a.get("created_at")).substring(0, Math.min(10, nullSafe(a.get("created_at")).length()))
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(parent.getFrame(), "New Announcement", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(parent.getFrame());

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JLabel title = new JLabel("Publish Announcement");
        title.setFont(UIHelper.FONT_SUBTITLE);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(24));

        JTextField titleF = UIHelper.styledField("Announcement title");
        JTextArea contentArea = new JTextArea(4, 30);
        contentArea.setFont(UIHelper.FONT_BODY);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane contentSP = new JScrollPane(contentArea);
        contentSP.setAlignmentX(LEFT_ALIGNMENT);
        contentSP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        contentSP.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));

        JComboBox<String> priorityCB = UIHelper.styledComboBox(new String[]{"Low", "Normal", "Important", "Urgent"});
        priorityCB.setSelectedIndex(1);

        JTextField expiresF = UIHelper.styledField("YYYY-MM-DD (optional)");

        addFormField(form, "Title *", titleF);

        JLabel contentLabel = new JLabel("Content *");
        contentLabel.setFont(UIHelper.FONT_SMALL);
        contentLabel.setForeground(UIHelper.TEXT_SECONDARY);
        contentLabel.setAlignmentX(LEFT_ALIGNMENT);
        contentLabel.setBorder(new EmptyBorder(0, 2, 4, 0));
        form.add(contentLabel);
        form.add(contentSP);
        form.add(Box.createVerticalStrut(14));

        addFormField(form, "Priority", priorityCB);
        addFormField(form, "Expires At", expiresF);

        form.add(Box.createVerticalStrut(12));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIHelper.primaryButton("Publish");
        saveBtn.addActionListener(e -> {
            if (titleF.getText().trim().isEmpty() || contentArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title and content are required."); return;
            }
            if (dao.addAnnouncement(titleF.getText().trim(), contentArea.getText().trim(),
                    (String) priorityCB.getSelectedItem(), expiresF.getText().trim())) {
                UIHelper.showToast(parent.getFrame(), "Announcement published", true);
                dialog.dispose();
                loadData();
            } else {
                UIHelper.showToast(parent.getFrame(), "Failed to publish", false);
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
        form.add(Box.createVerticalStrut(14));
    }

    private static Color priorityColor(String p) {
        switch (p) {
            case "Urgent": return UIHelper.DANGER;
            case "Important": return UIHelper.ACCENT;
            case "Low": return UIHelper.TEXT_MUTED;
            default: return UIHelper.INFO;
        }
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}