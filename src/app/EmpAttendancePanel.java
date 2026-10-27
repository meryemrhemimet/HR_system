package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EmpAttendancePanel extends JPanel {

    public EmpAttendancePanel(EmpMainScreen parent, int empId) {
        HRMdao dao = new HRMdao();
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(28, 36, 36, 36));

        // Header
        JPanel hdr = UIHelper.sectionHeader("My Attendance", "Track your daily attendance and punctuality");
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        wrap.add(hdr);
        wrap.add(Box.createVerticalStrut(20));

        // Summary stats (last 30 days)
        Map<String, Integer> sum = dao.getMyAttendanceSummary(empId);
        int present = sum.getOrDefault("Present", 0);
        int late = sum.getOrDefault("Late", 0);
        int absent = sum.getOrDefault("Absent", 0);
        int onLeave = sum.getOrDefault("On Leave", 0);
        int total = present + late + absent + onLeave;
        int rate = total > 0 ? (100 * (present + late)) / total : 0;

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        stats.add(UIHelper.statCard("Attendance Rate", rate + "%", "Last 30 days", UIHelper.SUCCESS, "%"));
        stats.add(UIHelper.statCard("Days Present", String.valueOf(present), "Last 30 days", UIHelper.PRIMARY, "P"));
        stats.add(UIHelper.statCard("Times Late", String.valueOf(late), "Last 30 days", UIHelper.AMBER, "L"));
        stats.add(UIHelper.statCard("Absences", String.valueOf(absent), "Last 30 days", UIHelper.ROSE, "A"));
        wrap.add(stats);
        wrap.add(Box.createVerticalStrut(20));

        // Table
        String[] cols = {"Date", "Check In", "Check Out", "Hours", "Status", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = UIHelper.styledTable(model);
        table.getColumnModel().getColumn(3).setMaxWidth(80);
        table.getColumnModel().getColumn(4).setMaxWidth(110);

        table.getColumnModel().getColumn(4).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel b = UIHelper.badge(val == null ? "" : val.toString(), UIHelper.statusColor(val == null ? "" : val.toString()));
            if (sel) b.setOpaque(true);
            return b;
        });

        List<Map<String, String>> data = dao.getMyAttendance(empId);
        for (Map<String, String> a : data) {
            String hours = computeHours(a.get("check_in"), a.get("check_out"));
            model.addRow(new Object[]{
                a.get("date"), ns(a.get("check_in")), ns(a.get("check_out")), hours, ns(a.get("status")), ns(a.get("notes"))
            });
        }

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());

        JPanel tableHdr = new JPanel(new BorderLayout());
        tableHdr.setOpaque(false);
        tableHdr.setBorder(new EmptyBorder(16, 20, 8, 20));
        JLabel th = new JLabel("Last 30 Records"); th.setFont(UIHelper.FONT_HEADING); th.setForeground(UIHelper.TEXT_PRIMARY);
        JLabel ts = new JLabel(data.size() + " entries"); ts.setFont(UIHelper.FONT_TINY); ts.setForeground(UIHelper.TEXT_MUTED);
        tableHdr.add(th, BorderLayout.WEST); tableHdr.add(ts, BorderLayout.EAST);

        tableCard.add(tableHdr, BorderLayout.NORTH);
        tableCard.add(UIHelper.styledScrollPane(table), BorderLayout.CENTER);
        tableCard.setAlignmentX(LEFT_ALIGNMENT);
        wrap.add(tableCard);

        JScrollPane sp = UIHelper.styledScrollPane(wrap);
        sp.getViewport().setBackground(UIHelper.BG);
        add(sp, BorderLayout.CENTER);
    }

    private static String computeHours(String in, String out) {
        if (in == null || out == null) return "—";
        try {
            String[] i = in.split(":"), o = out.split(":");
            int min = (Integer.parseInt(o[0]) * 60 + Integer.parseInt(o[1])) - (Integer.parseInt(i[0]) * 60 + Integer.parseInt(i[1]));
            return String.format("%dh %02dm", min / 60, min % 60);
        } catch (Exception e) { return "—"; }
    }

    private static String ns(String s) { return s == null ? "" : s; }
}
