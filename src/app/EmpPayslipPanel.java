package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class EmpPayslipPanel extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final EmpMainScreen parent;
    private final int empId;
    private List<Map<String, String>> payslips;

    private static final String[] MONTHS = {"", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    public EmpPayslipPanel(EmpMainScreen parent, int empId) {
        this.parent = parent;
        this.empId = empId;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(28, 36, 36, 36));

        JPanel hdr = UIHelper.sectionHeader("My Payslips", "View your salary history and payment details");
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        root.add(hdr);
        root.add(Box.createVerticalStrut(20));

        payslips = dao.getMyPayslips(empId);

        // Summary stats
        double yearTotal = dao.getMyTotalEarningsThisYear(empId);
        double avgNet = 0; int paidCount = 0;
        double lastNet = 0;
        for (Map<String, String> p : payslips) {
            if ("Paid".equals(p.get("payment_status"))) {
                avgNet += parseDouble(p.get("net_salary"));
                paidCount++;
                if (lastNet == 0) lastNet = parseDouble(p.get("net_salary"));
            }
        }
        if (paidCount > 0) avgNet /= paidCount;

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        stats.add(UIHelper.statCard("Last Payment", String.format("%,.0f MAD", lastNet), "Most recent salary", UIHelper.SUCCESS, "$"));
        stats.add(UIHelper.statCard("Year Total", String.format("%,.0f MAD", yearTotal), LocalDate.now().getYear() + " earnings", UIHelper.PRIMARY, "Y"));
        stats.add(UIHelper.statCard("Avg Monthly", String.format("%,.0f MAD", avgNet), "Average net pay", UIHelper.TEAL, "M"));
        stats.add(UIHelper.statCard("Total Payslips", String.valueOf(payslips.size()), "All time", UIHelper.PURPLE, "#"));
        root.add(stats);
        root.add(Box.createVerticalStrut(20));

        // Table
        String[] cols = {"Period", "Base Salary", "OT Pay", "Bonuses", "Deductions", "Tax", "CNSS", "Net Salary", "Status", ""};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = UIHelper.styledTable(model);
        table.getColumnModel().getColumn(0).setMinWidth(140);
        table.getColumnModel().getColumn(8).setMaxWidth(100);
        table.getColumnModel().getColumn(9).setMaxWidth(80);
        table.getColumnModel().getColumn(8).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel b = UIHelper.badge(val == null ? "" : val.toString(), UIHelper.statusColor(val == null ? "" : val.toString()));
            if (sel) b.setOpaque(true); return b;
        });
        // "View" column renderer - just show "View"
        table.getColumnModel().getColumn(9).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel l = new JLabel("View →", SwingConstants.CENTER);
            l.setForeground(UIHelper.PRIMARY);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            return l;
        });

        for (Map<String, String> p : payslips) {
            int m = parseInt(p.get("period_month"), 0);
            String period = (m > 0 && m <= 12 ? MONTHS[m] : "?") + " " + ns(p.get("period_year"));
            model.addRow(new Object[]{
                period, fmt(p.get("base_salary")), fmt(p.get("overtime_pay")), fmt(p.get("bonuses")),
                fmt(p.get("deductions")), fmt(p.get("tax_amount")), fmt(p.get("cnss")),
                fmt(p.get("net_salary")), ns(p.get("payment_status")), "View"
            });
        }

        // Click "View" column to open detail dialog
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 9 && row >= 0 && row < payslips.size()) {
                    showPayslipDetail(payslips.get(row));
                }
            }
        });

        JPanel tableCard = UIHelper.card();
        tableCard.setLayout(new BorderLayout());
        JPanel tHdr = new JPanel(new BorderLayout()); tHdr.setOpaque(false); tHdr.setBorder(new EmptyBorder(16, 20, 8, 20));
        JLabel tt = new JLabel("Salary History"); tt.setFont(UIHelper.FONT_HEADING); tt.setForeground(UIHelper.TEXT_PRIMARY);
        JLabel ts = new JLabel("Click 'View' for full breakdown"); ts.setFont(UIHelper.FONT_TINY); ts.setForeground(UIHelper.TEXT_MUTED);
        tHdr.add(tt, BorderLayout.WEST); tHdr.add(ts, BorderLayout.EAST);
        tableCard.add(tHdr, BorderLayout.NORTH);
        tableCard.add(UIHelper.styledScrollPane(table), BorderLayout.CENTER);
        tableCard.setAlignmentX(LEFT_ALIGNMENT);
        root.add(tableCard);

        JScrollPane sp = UIHelper.styledScrollPane(root);
        sp.getViewport().setBackground(UIHelper.BG);
        add(sp, BorderLayout.CENTER);
    }

    // ── Payslip Detail Dialog ───────────────────────────────────
    private void showPayslipDetail(Map<String, String> p) {
        JDialog d = new JDialog(parent.getFrame(), "Payslip Detail", true);
        d.setSize(440, 540);
        d.setLocationRelativeTo(parent.getFrame());

        JPanel root = new JPanel();
        root.setBackground(Color.WHITE);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(28, 32, 24, 32));

        // Header
        int m = parseInt(p.get("period_month"), 0);
        String period = (m > 0 && m <= 12 ? MONTHS[m] : "?") + " " + ns(p.get("period_year"));

        JLabel title = new JLabel("Payslip");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel(period + "  ·  " + parent.getUser().get("full_name"));
        sub.setFont(UIHelper.FONT_BODY);
        sub.setForeground(UIHelper.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 18, 0));

        // Status badge
        JLabel badge = UIHelper.badge(ns(p.get("payment_status")), UIHelper.statusColor(ns(p.get("payment_status"))));
        badge.setAlignmentX(LEFT_ALIGNMENT);

        root.add(title);
        root.add(sub);
        root.add(badge);
        root.add(Box.createVerticalStrut(20));

        // Earnings
        root.add(sectionLabel("EARNINGS"));
        root.add(detailRow("Base Salary", fmt(p.get("base_salary")) + " MAD", false));
        root.add(detailRow("Overtime Pay", fmt(p.get("overtime_pay")) + " MAD  (" + ns(p.get("overtime_hours")) + "h)", false));
        root.add(detailRow("Bonuses", fmt(p.get("bonuses")) + " MAD", false));
        double gross = parseDouble(p.get("base_salary")) + parseDouble(p.get("overtime_pay")) + parseDouble(p.get("bonuses"));
        root.add(detailRow("Gross Salary", String.format("%,.0f MAD", gross), true));
        root.add(Box.createVerticalStrut(14));

        // Deductions
        root.add(sectionLabel("DEDUCTIONS"));
        root.add(detailRow("Income Tax (IR)", fmt(p.get("tax_amount")) + " MAD", false));
        root.add(detailRow("CNSS", fmt(p.get("cnss")) + " MAD", false));
        root.add(detailRow("Other Deductions", fmt(p.get("deductions")) + " MAD", false));
        double totalDed = parseDouble(p.get("tax_amount")) + parseDouble(p.get("cnss")) + parseDouble(p.get("deductions"));
        root.add(detailRow("Total Deductions", String.format("%,.0f MAD", totalDed), true));
        root.add(Box.createVerticalStrut(14));

        // Net (large)
        JPanel netRow = new JPanel(new BorderLayout());
        netRow.setBackground(new Color(238, 242, 255));
        netRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241, 80), 1),
                new EmptyBorder(14, 18, 14, 18)));
        netRow.setAlignmentX(LEFT_ALIGNMENT);
        netRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel netL = new JLabel("NET SALARY"); netL.setFont(new Font("Segoe UI", Font.BOLD, 12)); netL.setForeground(UIHelper.PRIMARY);
        JLabel netV = new JLabel(fmt(p.get("net_salary")) + " MAD"); netV.setFont(new Font("Segoe UI", Font.BOLD, 22)); netV.setForeground(UIHelper.PRIMARY_DARK);
        netRow.add(netL, BorderLayout.WEST);
        netRow.add(netV, BorderLayout.EAST);
        root.add(netRow);

        if (p.get("payment_date") != null) {
            JLabel pd = new JLabel("Paid on " + p.get("payment_date"));
            pd.setFont(UIHelper.FONT_TINY); pd.setForeground(UIHelper.TEXT_MUTED);
            pd.setAlignmentX(LEFT_ALIGNMENT); pd.setBorder(new EmptyBorder(8, 0, 0, 0));
            root.add(pd);
        }

        root.add(Box.createVerticalStrut(20));
        JPanel br = new JPanel(new FlowLayout(FlowLayout.RIGHT)); br.setOpaque(false); br.setAlignmentX(LEFT_ALIGNMENT);
        JButton close = UIHelper.primaryButton("Close"); close.addActionListener(e -> d.dispose());
        br.add(close);
        root.add(br);

        d.add(new JScrollPane(root));
        d.setVisible(true);
    }

    private static JLabel sectionLabel(String s) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(UIHelper.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    private static JPanel detailRow(String label, String value, boolean bold) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        p.setBorder(new EmptyBorder(3, 0, 3, 0));
        if (bold) p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER),
                new EmptyBorder(6, 0, 3, 0)));

        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(bold ? UIHelper.TEXT_PRIMARY : UIHelper.TEXT_SECONDARY);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 13)); v.setForeground(UIHelper.TEXT_PRIMARY);
        p.add(l, BorderLayout.WEST); p.add(v, BorderLayout.EAST);
        return p;
    }

    private static int parseInt(String s, int d) { try { return Integer.parseInt(s); } catch (Exception e) { return d; } }
    private static double parseDouble(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0; } }
    private static String fmt(String v) { try { return String.format("%,.0f", Double.parseDouble(v)); } catch (Exception e) { return "0"; } }
    private static String ns(String s) { return s == null ? "" : s; }
}
