package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * DashboardScreen — Uses UIHelper design system exclusively for consistency.
 */
public class DashboardScreen extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final MainScreen parent;

    private static final Color[] BAR_COLORS = {
        UIHelper.PRIMARY, UIHelper.TEAL, UIHelper.PURPLE, UIHelper.SUCCESS,
        UIHelper.AMBER, UIHelper.ROSE, UIHelper.INDIGO, UIHelper.CYAN
    };

    public DashboardScreen(MainScreen parent) {
        this.parent = parent;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(new EmptyBorder(32, 40, 40, 40));

        // ── Header ──
        wrapper.add(buildPageHeader());
        wrapper.add(Box.createVerticalStrut(24));

        // ── Stat Cards ──
        int empCount = dao.countEmployees();
        int deptCount = dao.countDepartments();
        int pendingLeaves = dao.countPendingLeaves();
        double payroll = dao.totalPayrollLastMonth();

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 18, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        statsRow.add(UIHelper.statCard("Total Employees", String.valueOf(empCount), "Active workforce", UIHelper.PRIMARY, "E"));
        statsRow.add(UIHelper.statCard("Departments", String.valueOf(deptCount), "Organizational units", UIHelper.PURPLE, "D"));
        statsRow.add(UIHelper.statCard("Pending Leaves", String.valueOf(pendingLeaves), "Awaiting approval", UIHelper.AMBER, "L"));
        statsRow.add(UIHelper.statCard("Last Payroll", formatMAD(payroll), "Total net disbursed", UIHelper.SUCCESS, "P"));

        wrapper.add(statsRow);
        wrapper.add(Box.createVerticalStrut(24));

        // ── Charts Row ──
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 18, 0));
        chartsRow.setOpaque(false);
        chartsRow.setAlignmentX(LEFT_ALIGNMENT);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));
        chartsRow.add(buildBarChart());
        chartsRow.add(buildDonutChart());

        wrapper.add(chartsRow);
        wrapper.add(Box.createVerticalStrut(24));

        // ── Bottom Row ──
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 18, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 290));
        bottomRow.add(buildRecentHires());
        bottomRow.add(buildAnnouncements());

        wrapper.add(bottomRow);

        JScrollPane scroll = UIHelper.styledScrollPane(wrapper);
        scroll.getViewport().setBackground(UIHelper.BG);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Page Header ─────────────────────────────────────────────
    private JPanel buildPageHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Dashboard");
        title.setFont(UIHelper.FONT_TITLE);
        title.setForeground(UIHelper.TEXT_PRIMARY);

        JLabel sub = new JLabel("Welcome back, " + parent.getUser().get("full_name") + "  —  here's your overview");
        sub.setFont(UIHelper.FONT_BODY);
        sub.setForeground(UIHelper.TEXT_SECONDARY);
        sub.setBorder(new EmptyBorder(4, 0, 0, 0));

        left.add(title);
        left.add(sub);

        // Date badge
        JLabel dateBadge = new JLabel(todayLabel());
        dateBadge.setFont(UIHelper.FONT_SMALL);
        dateBadge.setForeground(UIHelper.PRIMARY);
        dateBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)));
        dateBadge.setBackground(new Color(238, 242, 255));
        dateBadge.setOpaque(true);

        p.add(left, BorderLayout.WEST);
        p.add(dateBadge, BorderLayout.EAST);
        return p;
    }

    // ── Bar Chart ───────────────────────────────────────────────
    private JPanel buildBarChart() {
        List<Object[]> data = dao.departmentDistribution();
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Employees by Department");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.TEXT_PRIMARY);

        JLabel sub = new JLabel("Current headcount");
        sub.setFont(UIHelper.FONT_TINY);
        sub.setForeground(UIHelper.TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(sub, BorderLayout.EAST);

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (data.isEmpty()) return;
                Graphics2D g2 = UIHelper.aa(g);

                int w = getWidth(), h = getHeight();
                int padL = 14, padR = 14, padT = 24, padB = 58;
                int chartW = w - padL - padR, chartH = h - padT - padB;
                int maxVal = data.stream().mapToInt(r -> ((Number) r[1]).intValue()).max().orElse(1);

                // Grid lines
                g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{4f, 4f}, 0f));
                for (int i = 1; i <= 4; i++) {
                    int y = padT + chartH - i * chartH / 4;
                    g2.setColor(UIHelper.BORDER);
                    g2.drawLine(padL, y, w - padR, y);
                    g2.setColor(UIHelper.TEXT_MUTED);
                    g2.setFont(UIHelper.FONT_TINY);
                    g2.drawString(String.valueOf(maxVal * i / 4), 2, y + 4);
                }
                g2.setStroke(new BasicStroke(1f));

                int n = data.size();
                int barGap = Math.max(8, 16 - n);
                int barW = Math.max(18, (chartW - (n + 1) * barGap) / n);

                for (int i = 0; i < n; i++) {
                    int val = ((Number) data.get(i)[1]).intValue();
                    int barH = maxVal > 0 ? (int) ((double) val / maxVal * chartH) : 0;
                    int x = padL + barGap + i * (barW + barGap);
                    int y = padT + chartH - barH;
                    Color c = BAR_COLORS[i % BAR_COLORS.length];

                    // Shadow
                    g2.setColor(new Color(0, 0, 0, 8));
                    g2.fill(new RoundRectangle2D.Float(x + 2, y + 3, barW, barH, 8, 8));

                    // Bar
                    if (barH > 0) {
                        g2.setPaint(new GradientPaint(x, y, c, x, y + barH,
                                new Color(c.getRed(), c.getGreen(), c.getBlue(), 160)));
                        g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 8, 8));
                    }

                    // Value
                    g2.setColor(UIHelper.TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    String vs = String.valueOf(val);
                    g2.drawString(vs, x + (barW - fm.stringWidth(vs)) / 2, Math.max(padT + 12, y - 5));

                    // Rotated label
                    g2.setColor(UIHelper.TEXT_SECONDARY);
                    g2.setFont(UIHelper.FONT_TINY);
                    fm = g2.getFontMetrics();
                    String lbl = truncate((String) data.get(i)[0], 10);
                    Graphics2D g3 = (Graphics2D) g2.create();
                    g3.rotate(-Math.PI / 4, x + barW / 2.0, padT + chartH + 8);
                    g3.drawString(lbl, x + barW / 2 - fm.stringWidth(lbl) / 2, padT + chartH + 8 + fm.getAscent());
                    g3.dispose();
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);

        card.add(header, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // ── Donut Chart ─────────────────────────────────────────────
    private JPanel buildDonutChart() {
        Map<String, Integer> data = dao.todayAttendanceSummary();
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Today's Attendance");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.TEXT_PRIMARY);

        JLabel sub = new JLabel("Live snapshot");
        sub.setFont(UIHelper.FONT_TINY);
        sub.setForeground(UIHelper.TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(sub, BorderLayout.EAST);

        String[] keys = {"Present", "Late", "Absent", "On Leave"};
        Color[] colors = {UIHelper.SUCCESS, UIHelper.AMBER, UIHelper.ROSE, UIHelper.PRIMARY};

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = UIHelper.aa(g);

                int total = data.values().stream().mapToInt(v -> v).sum();
                if (total == 0) {
                    g2.setFont(UIHelper.FONT_SMALL);
                    g2.setColor(UIHelper.TEXT_MUTED);
                    g2.drawString("No attendance data for today", 20, 40);
                    g2.dispose();
                    return;
                }

                int size = Math.min(getWidth() - 160, getHeight() - 40);
                size = Math.min(size, 190);
                int cx = size / 2 + 28, cy = getHeight() / 2;
                int r = size / 2, innerR = (int) (r * 0.58);

                int startAngle = 90;
                for (int i = 0; i < keys.length; i++) {
                    int val = data.getOrDefault(keys[i], 0);
                    if (val == 0) continue;
                    int sweep = (int) Math.round(360.0 * val / total);
                    if (i == keys.length - 1) sweep = 360 - (startAngle - 90);

                    g2.setColor(colors[i]);
                    g2.fillArc(cx - r, cy - r, 2 * r, 2 * r, startAngle, -sweep);
                    startAngle -= sweep;
                }

                // Donut hole
                g2.setColor(UIHelper.CARD);
                g2.fillOval(cx - innerR, cy - innerR, 2 * innerR, 2 * innerR);

                // Center text
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                g2.setColor(UIHelper.TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                String ts = String.valueOf(total);
                g2.drawString(ts, cx - fm.stringWidth(ts) / 2, cy + 5);
                g2.setFont(UIHelper.FONT_TINY);
                fm = g2.getFontMetrics();
                g2.setColor(UIHelper.TEXT_MUTED);
                g2.drawString("employees", cx - fm.stringWidth("employees") / 2, cy + 19);

                // Legend
                int lx = cx + r + 22, ly = cy - (keys.length * 26) / 2;
                for (int i = 0; i < keys.length; i++) {
                    int val = data.getOrDefault(keys[i], 0);
                    int pct = total > 0 ? (int) (100.0 * val / total) : 0;
                    g2.setColor(colors[i]);
                    g2.fillOval(lx + 4, ly + i * 26 + 4, 10, 10);
                    g2.setColor(UIHelper.TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString(keys[i], lx + 20, ly + i * 26 + 13);
                    g2.setFont(UIHelper.FONT_TINY);
                    g2.setColor(UIHelper.TEXT_MUTED);
                    g2.drawString(val + " (" + pct + "%)", lx + 20, ly + i * 26 + 23);
                }

                g2.dispose();
            }
        };
        chart.setOpaque(false);

        card.add(header, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // ── Recent Hires ────────────────────────────────────────────
    private JPanel buildRecentHires() {
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Recent Hires");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        List<Map<String, String>> hires = dao.recentHires();
        Color[] palette = {UIHelper.PRIMARY, UIHelper.TEAL, UIHelper.PURPLE, UIHelper.SUCCESS, UIHelper.ROSE};

        if (hires.isEmpty()) {
            list.add(UIHelper.emptyState("", "No recent hires", ""));
        } else {
            for (Map<String, String> h : hires) {
                String fname = h.get("first_name"), lname = h.get("last_name");
                String name = fname + " " + lname;
                String inits = ("" + fname.charAt(0) + lname.charAt(0)).toUpperCase();
                Color ac = palette[Math.abs(name.hashCode()) % palette.length];

                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(7, 0, 7, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

                // Avatar
                JPanel avatar = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = UIHelper.aa(g);
                        g2.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 25));
                        g2.fillOval(0, 0, 36, 36);
                        g2.setColor(ac);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(inits, (36 - fm.stringWidth(inits)) / 2,
                                (36 + fm.getAscent() - fm.getDescent()) / 2);
                        g2.dispose();
                    }
                    @Override public Dimension getPreferredSize() { return new Dimension(36, 36); }
                };
                avatar.setOpaque(false);

                JPanel info = new JPanel();
                info.setOpaque(false);
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

                JLabel nLabel = new JLabel(name);
                nLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                nLabel.setForeground(UIHelper.TEXT_PRIMARY);

                JLabel dLabel = new JLabel(nullSafe(h.get("dept")) + "  ·  " + nullSafe(h.get("hire_date")));
                dLabel.setFont(UIHelper.FONT_TINY);
                dLabel.setForeground(UIHelper.TEXT_MUTED);

                info.add(nLabel);
                info.add(Box.createVerticalStrut(2));
                info.add(dLabel);

                row.add(avatar, BorderLayout.WEST);
                row.add(info, BorderLayout.CENTER);
                row.add(UIHelper.badge("NEW", UIHelper.SUCCESS), BorderLayout.EAST);
                list.add(row);
            }
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    // ── Announcements ───────────────────────────────────────────
    private JPanel buildAnnouncements() {
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Announcements");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        List<Map<String, String>> anns = dao.activeAnnouncements();
        if (anns.isEmpty()) {
            list.add(UIHelper.emptyState("", "No announcements", ""));
        } else {
            for (Map<String, String> a : anns) {
                String priority = nullSafe(a.get("priority"));
                Color pColor = priorityColor(priority);

                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(6, 0, 6, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                // Accent bar
                JPanel bar = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = UIHelper.aa(g);
                        g2.setColor(pColor);
                        g2.fill(new RoundRectangle2D.Float(0, 2, 4, getHeight() - 4, 4, 4));
                        g2.dispose();
                    }
                    @Override public Dimension getPreferredSize() { return new Dimension(8, 40); }
                };
                bar.setOpaque(false);

                JPanel info = new JPanel();
                info.setOpaque(false);
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

                JLabel aTitle = new JLabel(a.get("title"));
                aTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
                aTitle.setForeground(UIHelper.TEXT_PRIMARY);

                JLabel aDate = new JLabel(nullSafe(a.get("date_str")) + "  ·  " + priority);
                aDate.setFont(UIHelper.FONT_TINY);
                aDate.setForeground(UIHelper.TEXT_MUTED);

                info.add(aTitle);
                info.add(Box.createVerticalStrut(2));
                info.add(aDate);

                row.add(bar, BorderLayout.WEST);
                row.add(info, BorderLayout.CENTER);
                row.add(UIHelper.badge(priority, pColor), BorderLayout.EAST);
                list.add(row);
            }
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    // ── Helpers ──────────────────────────────────────────────────
    private static String formatMAD(double val) {
        if (val >= 1_000_000) return String.format("%.1fM", val / 1_000_000);
        if (val >= 1_000) return String.format("%.0fK", val / 1_000);
        return NumberFormat.getInstance().format((long) val);
    }

    private static Color priorityColor(String p) {
        if (p == null) return UIHelper.TEXT_MUTED;
        switch (p) {
            case "Urgent": return UIHelper.ROSE;
            case "Important": return UIHelper.AMBER;
            default: return UIHelper.PRIMARY;
        }
    }

    private static String truncate(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max - 1) + "\u2026";
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }

    private static String todayLabel() {
        java.time.LocalDate d = java.time.LocalDate.now();
        return d.getDayOfWeek().toString().charAt(0)
                + d.getDayOfWeek().toString().substring(1).toLowerCase()
                + ", " + d.getMonth().toString().charAt(0)
                + d.getMonth().toString().substring(1).toLowerCase()
                + " " + d.getDayOfMonth() + ", " + d.getYear();
    }
}
