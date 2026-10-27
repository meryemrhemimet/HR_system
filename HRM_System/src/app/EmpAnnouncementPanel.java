package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class EmpAnnouncementPanel extends JPanel {

    public EmpAnnouncementPanel(EmpMainScreen parent) {
        HRMdao dao = new HRMdao();
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(28, 36, 36, 36));

        // Header with count
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        hdr.add(UIHelper.sectionHeader("Announcements", "Latest updates from HR & management"), BorderLayout.WEST);

        List<Map<String, String>> anns = dao.getAllAnnouncements();
        int active = 0, urgent = 0, important = 0;
        for (Map<String, String> a : anns) {
            if ("1".equals(a.get("is_active"))) active++;
            if ("Urgent".equals(a.get("priority"))) urgent++;
            if ("Important".equals(a.get("priority"))) important++;
        }

        JLabel counter = new JLabel(active + " active");
        counter.setFont(UIHelper.FONT_SMALL);
        counter.setForeground(UIHelper.PRIMARY);
        counter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241, 60), 1, true),
                new EmptyBorder(7, 14, 7, 14)));
        counter.setBackground(new Color(238, 242, 255));
        counter.setOpaque(true);
        hdr.add(counter, BorderLayout.EAST);
        root.add(hdr);
        root.add(Box.createVerticalStrut(20));

        // Stats
        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        stats.add(UIHelper.statCard("Total Posts", String.valueOf(anns.size()), "All announcements", UIHelper.PRIMARY, "T"));
        stats.add(UIHelper.statCard("Important", String.valueOf(important), "High priority", UIHelper.AMBER, "I"));
        stats.add(UIHelper.statCard("Urgent", String.valueOf(urgent), "Immediate attention", UIHelper.DANGER, "U"));
        root.add(stats);
        root.add(Box.createVerticalStrut(20));

        // Cards
        if (anns.isEmpty()) {
            root.add(UIHelper.emptyState("", "No announcements", "Check back later"));
        } else {
            for (Map<String, String> a : anns) {
                root.add(buildAnnouncementCard(a));
                root.add(Box.createVerticalStrut(12));
            }
        }

        JScrollPane sp = UIHelper.styledScrollPane(root);
        sp.getViewport().setBackground(UIHelper.BG);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildAnnouncementCard(Map<String, String> a) {
        String priority = ns(a.get("priority"));
        Color pc = "Urgent".equals(priority) ? UIHelper.DANGER
                : "Important".equals(priority) ? UIHelper.AMBER
                : "Low".equals(priority) ? UIHelper.TEXT_MUTED
                : UIHelper.PRIMARY;

        JPanel card = UIHelper.card();
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Left accent stripe
        JPanel stripe = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setPaint(new GradientPaint(0, 0, pc, 0, getHeight(), pc.darker()));
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(4, 0); }
        };
        stripe.setOpaque(false);

        // Content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 22, 20, 22));

        // Top row: title + badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setAlignmentX(LEFT_ALIGNMENT);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel title = new JLabel(ns(a.get("title")));
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UIHelper.TEXT_PRIMARY);

        JLabel badge = UIHelper.badge(priority, pc);
        topRow.add(title, BorderLayout.WEST);
        topRow.add(badge, BorderLayout.EAST);

        // Body
        String body = ns(a.get("content"));
        JLabel bodyLbl = new JLabel("<html><body style='width:600px'>" + body + "</body></html>");
        bodyLbl.setFont(UIHelper.FONT_BODY);
        bodyLbl.setForeground(UIHelper.TEXT_SECONDARY);
        bodyLbl.setAlignmentX(LEFT_ALIGNMENT);
        bodyLbl.setBorder(new EmptyBorder(8, 0, 8, 0));

        // Meta
        String created = ns(a.get("created_at"));
        if (created.length() > 10) created = created.substring(0, 10);
        String expires = ns(a.get("expires_at"));
        JLabel meta = new JLabel("Posted " + created + (expires.isEmpty() ? "" : "  ·  Expires " + expires));
        meta.setFont(UIHelper.FONT_TINY);
        meta.setForeground(UIHelper.TEXT_MUTED);
        meta.setAlignmentX(LEFT_ALIGNMENT);

        content.add(topRow);
        content.add(bodyLbl);
        content.add(meta);

        card.setLayout(new BorderLayout());
        card.add(stripe, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private static String ns(String s) { return s == null ? "" : s; }
}
