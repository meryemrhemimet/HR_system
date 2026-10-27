package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class EmpDashboard extends JPanel {

    private final HRMdao dao = new HRMdao();
    private final EmpMainScreen parent;
    private final int empId;

    public EmpDashboard(EmpMainScreen parent, int empId) {
        this.parent = parent;
        this.empId = empId;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(28, 36, 36, 36));

        // Header (greeting + date)
        wrap.add(buildGreeting());
        wrap.add(Box.createVerticalStrut(20));

        // Stat cards
        wrap.add(buildStats());
        wrap.add(Box.createVerticalStrut(20));

        // Two columns: Profile (left) + Attendance summary (right)
        JPanel row1 = new JPanel(new GridLayout(1, 2, 18, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 290));
        row1.add(buildProfileCard());
        row1.add(buildAttendanceCard());
        wrap.add(row1);
        wrap.add(Box.createVerticalStrut(20));

        // Two columns: Leave balance + Recent activity
        JPanel row2 = new JPanel(new GridLayout(1, 2, 18, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));
        row2.add(buildLeaveBalanceCard());
        row2.add(buildRecentActivityCard());
        wrap.add(row2);
        wrap.add(Box.createVerticalStrut(20));

        // Announcements
        wrap.add(buildAnnouncementsCard());

        JScrollPane sp = UIHelper.styledScrollPane(wrap);
        sp.getViewport().setBackground(UIHelper.BG);
        add(sp, BorderLayout.CENTER);
    }

    // ── Greeting Header ─────────────────────────────────────────
    private JPanel buildGreeting() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        String fname = parent.getUser().get("full_name").split(" ")[0];
        int hour = LocalDateTime.now().getHour();
        String greeting = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";

        JLabel hi = new JLabel(greeting + ", " + fname + " 👋");
        hi.setFont(UIHelper.FONT_TITLE);
        hi.setForeground(UIHelper.TEXT_PRIMARY);

        JLabel sub = new JLabel("Here's what's happening with your account today");
        sub.setFont(UIHelper.FONT_BODY);
        sub.setForeground(UIHelper.TEXT_SECONDARY);
        sub.setBorder(new EmptyBorder(4, 0, 0, 0));

        left.add(hi);
        left.add(sub);

        // Date pill
        JLabel datePill = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")));
        datePill.setFont(UIHelper.FONT_SMALL);
        datePill.setForeground(UIHelper.PRIMARY);
        datePill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241, 60), 1, true),
                new EmptyBorder(7, 14, 7, 14)));
        datePill.setBackground(new Color(238, 242, 255));
        datePill.setOpaque(true);

        p.add(left, BorderLayout.WEST);
        p.add(datePill, BorderLayout.EAST);
        return p;
    }

    // ── Stat Cards ──────────────────────────────────────────────
    private JPanel buildStats() {
        Map<String, String> profile = dao.getEmployeeProfile(empId);
        int pending = dao.countMyPendingLeaves(empId);
        int present = dao.countMyAttendanceThisMonth(empId);
        List<Map<String, String>> payslips = dao.getMyPayslips(empId);
        String lastNet = "—";
        if (!payslips.isEmpty() && payslips.get(0).get("net_salary") != null)
            lastNet = String.format("%,.0f MAD", Double.parseDouble(payslips.get(0).get("net_salary")));
        double yearTotal = dao.getMyTotalEarningsThisYear(empId);

        JPanel p = new JPanel(new GridLayout(1, 4, 16, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        p.add(UIHelper.statCard("Days Present", String.valueOf(present), "This month", UIHelper.SUCCESS, "P"));
        p.add(UIHelper.statCard("Pending Leaves", String.valueOf(pending), "Awaiting approval", UIHelper.AMBER, "L"));
        p.add(UIHelper.statCard("Last Salary", lastNet, "Net pay received", UIHelper.TEAL, "S"));
        p.add(UIHelper.statCard("Year Earnings", formatCompact(yearTotal), "Total " + LocalDate.now().getYear(), UIHelper.PRIMARY, "Y"));
        return p;
    }

    // ── Profile Card ────────────────────────────────────────────
    private JPanel buildProfileCard() {
        Map<String, String> profile = dao.getEmployeeProfile(empId);
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel title = new JLabel("My Profile");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));

        if (profile == null) {
            card.add(title, BorderLayout.NORTH);
            card.add(UIHelper.emptyState("", "No profile data", "Contact HR"), BorderLayout.CENTER);
            return card;
        }

        // Top: avatar + name + role
        JPanel top = new JPanel(new BorderLayout(14, 0));
        top.setOpaque(false);

        String name = profile.get("first_name") + " " + profile.get("last_name");
        String inits = ("" + profile.get("first_name").charAt(0) + profile.get("last_name").charAt(0)).toUpperCase();

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setPaint(new GradientPaint(0, 0, UIHelper.PRIMARY, getWidth(), getHeight(), UIHelper.PURPLE));
                g2.fillOval(0, 0, 56, 56);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(inits, (56 - fm.stringWidth(inits)) / 2, (56 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(56, 56); }
        };
        avatar.setOpaque(false);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(4, 0, 0, 0));
        JLabel nameLbl = new JLabel(name); nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 16)); nameLbl.setForeground(UIHelper.TEXT_PRIMARY);
        JLabel roleLbl = new JLabel(nullSafe(profile.get("position_title")) + " · " + nullSafe(profile.get("dept_name")));
        roleLbl.setFont(UIHelper.FONT_SMALL); roleLbl.setForeground(UIHelper.TEXT_SECONDARY);
        info.add(nameLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(roleLbl);

        top.add(avatar, BorderLayout.WEST);
        top.add(info, BorderLayout.CENTER);

        // Grid: 2 columns of key/value
        JPanel grid = new JPanel(new GridLayout(4, 2, 18, 12));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(20, 0, 0, 0));
        addInfo(grid, "Email", nullSafe(profile.get("email")));
        addInfo(grid, "Phone", nullSafe(profile.get("phone")));
        addInfo(grid, "Hire Date", nullSafe(profile.get("hire_date")));
        addInfo(grid, "Contract", nullSafe(profile.get("contract_type")));
        addInfo(grid, "City", nullSafe(profile.get("city")));
        addInfo(grid, "Status", nullSafe(profile.get("status")));
        addInfo(grid, "Base Salary", profile.get("base_salary") != null ? String.format("%,.0f MAD", Double.parseDouble(profile.get("base_salary"))) : "—");
        addInfo(grid, "ID", "#" + nullSafe(profile.get("id")));

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.add(top, BorderLayout.NORTH);
        inner.add(grid, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ── Attendance Card (donut chart) ───────────────────────────
    private JPanel buildAttendanceCard() {
        Map<String, Integer> data = dao.getMyAttendanceSummary(empId);
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel title = new JLabel("Last 30 Days Attendance");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        String[] keys = {"Present", "Late", "Absent", "On Leave"};
        Color[] colors = {UIHelper.SUCCESS, UIHelper.AMBER, UIHelper.ROSE, UIHelper.PRIMARY};

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = UIHelper.aa(g);
                int total = data.values().stream().mapToInt(v -> v).sum();
                if (total == 0) {
                    g2.setColor(UIHelper.TEXT_MUTED); g2.setFont(UIHelper.FONT_SMALL);
                    g2.drawString("No attendance recorded yet", 20, 40);
                    g2.dispose(); return;
                }

                int size = Math.min(getWidth() - 200, getHeight() - 30);
                size = Math.min(size, 170);
                int cx = size / 2 + 16, cy = getHeight() / 2;
                int r = size / 2, ir = (int)(r * 0.62);

                int start = 90;
                for (int i = 0; i < keys.length; i++) {
                    int v = data.getOrDefault(keys[i], 0);
                    if (v == 0) continue;
                    int sweep = (int) Math.round(360.0 * v / total);
                    if (i == keys.length - 1) sweep = 360 - (start - 90);
                    g2.setColor(colors[i]);
                    g2.fillArc(cx - r, cy - r, 2 * r, 2 * r, start, -sweep);
                    start -= sweep;
                }
                g2.setColor(UIHelper.CARD);
                g2.fillOval(cx - ir, cy - ir, 2 * ir, 2 * ir);

                int present = data.getOrDefault("Present", 0);
                int rate = total > 0 ? (int)(100.0 * present / total) : 0;
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                g2.setColor(UIHelper.TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                String s = rate + "%";
                g2.drawString(s, cx - fm.stringWidth(s) / 2, cy + 4);
                g2.setFont(UIHelper.FONT_TINY); g2.setColor(UIHelper.TEXT_MUTED);
                fm = g2.getFontMetrics();
                g2.drawString("attendance", cx - fm.stringWidth("attendance") / 2, cy + 18);

                // Legend on right
                int lx = cx + r + 22, ly = cy - (keys.length * 22) / 2;
                for (int i = 0; i < keys.length; i++) {
                    int v = data.getOrDefault(keys[i], 0);
                    g2.setColor(colors[i]);
                    g2.fillOval(lx, ly + i * 22 + 4, 8, 8);
                    g2.setColor(UIHelper.TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString(keys[i], lx + 14, ly + i * 22 + 11);
                    g2.setFont(UIHelper.FONT_TINY); g2.setColor(UIHelper.TEXT_MUTED);
                    g2.drawString(v + " day" + (v == 1 ? "" : "s"), lx + 14 + 60, ly + i * 22 + 11);
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);

        card.add(title, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // ── Leave Balance Card ──────────────────────────────────────
    private JPanel buildLeaveBalanceCard() {
        List<Map<String, String>> bal = dao.getMyLeaveBalance(empId);
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(22, 26, 22, 26));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        JLabel title = new JLabel("Leave Balance"); title.setFont(UIHelper.FONT_HEADING); title.setForeground(UIHelper.TEXT_PRIMARY);
        JLabel sub = new JLabel(LocalDate.now().getYear() + " quota"); sub.setFont(UIHelper.FONT_TINY); sub.setForeground(UIHelper.TEXT_MUTED);
        header.add(title, BorderLayout.WEST); header.add(sub, BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        // Show top 4 leave types only
        for (int i = 0; i < Math.min(4, bal.size()); i++) {
            Map<String, String> lt = bal.get(i);
            int max = parseInt(lt.get("max_days"), 0);
            int taken = parseInt(lt.get("days_taken"), 0);
            int remaining = Math.max(0, max - taken);
            float pct = max > 0 ? (float) taken / max : 0;
            Color c = parseColor(lt.get("color"), UIHelper.PRIMARY);
            list.add(buildBalanceRow(lt.get("name"), taken, max, remaining, pct, c));
            list.add(Box.createVerticalStrut(10));
        }

        card.add(header, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildBalanceRow(String name, int taken, int max, int remaining, float pct, Color color) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel topR = new JPanel(new BorderLayout());
        topR.setOpaque(false);
        JLabel nm = new JLabel(name); nm.setFont(new Font("Segoe UI", Font.BOLD, 12)); nm.setForeground(UIHelper.TEXT_PRIMARY);
        JLabel num = new JLabel(remaining + " / " + max + " days"); num.setFont(UIHelper.FONT_TINY); num.setForeground(UIHelper.TEXT_SECONDARY);
        topR.add(nm, BorderLayout.WEST); topR.add(num, BorderLayout.EAST);

        // Progress bar
        JComponent bar = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setColor(new Color(226, 232, 240));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 6, 6, 6));
                int w = (int)(getWidth() * pct);
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, 6, 6, 6));
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 6); }
        };
        bar.setPreferredSize(new Dimension(0, 6));

        row.add(topR, BorderLayout.NORTH);
        row.add(bar, BorderLayout.CENTER);
        return row;
    }

    // ── Recent Activity Card ────────────────────────────────────
    private JPanel buildRecentActivityCard() {
        List<Map<String, String>> leaves = dao.getMyRecentLeaves(empId, 4);
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel title = new JLabel("My Recent Leave Requests");
        title.setFont(UIHelper.FONT_HEADING);
        title.setForeground(UIHelper.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (leaves.isEmpty()) {
            JLabel n = new JLabel("No leave requests yet");
            n.setFont(UIHelper.FONT_SMALL); n.setForeground(UIHelper.TEXT_MUTED);
            list.add(n);
        } else {
            for (Map<String, String> l : leaves) {
                JPanel r = new JPanel(new BorderLayout());
                r.setOpaque(false);
                r.setBorder(new EmptyBorder(8, 0, 8, 0));
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                JPanel mid = new JPanel();
                mid.setOpaque(false);
                mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
                JLabel t = new JLabel(nullSafe(l.get("leave_type")) + " · " + l.get("total_days") + " days");
                t.setFont(new Font("Segoe UI", Font.BOLD, 12)); t.setForeground(UIHelper.TEXT_PRIMARY);
                JLabel d = new JLabel(nullSafe(l.get("start_date")) + " → " + nullSafe(l.get("end_date")));
                d.setFont(UIHelper.FONT_TINY); d.setForeground(UIHelper.TEXT_MUTED);
                mid.add(t); mid.add(Box.createVerticalStrut(2)); mid.add(d);

                String status = nullSafe(l.get("status"));
                JLabel badge = UIHelper.badge(status, UIHelper.statusColor(status));

                r.add(mid, BorderLayout.CENTER);
                r.add(badge, BorderLayout.EAST);
                list.add(r);
            }
        }

        card.add(title, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    // ── Announcements Card ──────────────────────────────────────
    private JPanel buildAnnouncementsCard() {
        List<Map<String, String>> anns = dao.activeAnnouncements();
        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(22, 26, 22, 26));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel title = new JLabel("Latest Announcements"); title.setFont(UIHelper.FONT_HEADING); title.setForeground(UIHelper.TEXT_PRIMARY);
        JLabel sub = new JLabel(anns.size() + " active"); sub.setFont(UIHelper.FONT_TINY); sub.setForeground(UIHelper.TEXT_MUTED);
        header.add(title, BorderLayout.WEST); header.add(sub, BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (anns.isEmpty()) {
            JLabel n = new JLabel("No announcements"); n.setFont(UIHelper.FONT_SMALL); n.setForeground(UIHelper.TEXT_MUTED);
            list.add(n);
        } else {
            for (int i = 0; i < Math.min(3, anns.size()); i++) {
                Map<String, String> a = anns.get(i);
                String pri = nullSafe(a.get("priority"));
                Color c = "Urgent".equals(pri) ? UIHelper.DANGER : "Important".equals(pri) ? UIHelper.AMBER : UIHelper.PRIMARY;

                JPanel r = new JPanel(new BorderLayout(10, 0));
                r.setOpaque(false);
                r.setBorder(new EmptyBorder(8, 0, 8, 0));
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

                JPanel acc = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = UIHelper.aa(g);
                        g2.setColor(c); g2.fill(new RoundRectangle2D.Float(0, 4, 4, getHeight() - 8, 4, 4));
                        g2.dispose();
                    }
                    @Override public Dimension getPreferredSize() { return new Dimension(8, 36); }
                };
                acc.setOpaque(false);

                JPanel mid = new JPanel(); mid.setOpaque(false);
                mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
                JLabel t = new JLabel(nullSafe(a.get("title"))); t.setFont(new Font("Segoe UI", Font.BOLD, 12)); t.setForeground(UIHelper.TEXT_PRIMARY);
                String content = nullSafe(a.get("content")); if (content.length() > 80) content = content.substring(0, 80) + "...";
                JLabel d = new JLabel(content); d.setFont(UIHelper.FONT_TINY); d.setForeground(UIHelper.TEXT_MUTED);
                mid.add(t); mid.add(Box.createVerticalStrut(2)); mid.add(d);

                r.add(acc, BorderLayout.WEST);
                r.add(mid, BorderLayout.CENTER);
                r.add(UIHelper.badge(pri, c), BorderLayout.EAST);
                list.add(r);
            }
        }

        card.add(header, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    // ── Helpers ──────────────────────────────────────────────────
    private void addInfo(JPanel grid, String label, String value) {
        JPanel p = new JPanel(); p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label); l.setFont(UIHelper.FONT_TINY); l.setForeground(UIHelper.TEXT_MUTED);
        JLabel v = new JLabel(value.isEmpty() ? "—" : value); v.setFont(new Font("Segoe UI", Font.BOLD, 12)); v.setForeground(UIHelper.TEXT_PRIMARY);
        p.add(l); p.add(Box.createVerticalStrut(2)); p.add(v);
        grid.add(p);
    }

    private static String formatCompact(double v) {
        if (v >= 1_000_000) return String.format("%.1fM MAD", v / 1_000_000);
        if (v >= 1_000) return String.format("%.0fK MAD", v / 1_000);
        return String.format("%,.0f MAD", v);
    }

    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private static Color parseColor(String hex, Color def) { try { return Color.decode(hex); } catch (Exception e) { return def; } }
    private static String nullSafe(String s) { return s == null ? "" : s; }
}
