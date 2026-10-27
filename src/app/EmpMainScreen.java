package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Map;

public class EmpMainScreen extends JFrame {

    private static final int SIDEBAR_W = 250;
    private final Map<String, String> currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private String activeNav = "Dashboard";
    private JPanel navPanel;

    private static final Object[][] NAV = {
        {"D", "Dashboard",    "dashboard",   UIHelper.PRIMARY},
        {"A", "My Attendance","attendance",   UIHelper.SUCCESS},
        {"L", "My Leaves",   "leaves",       UIHelper.AMBER},
        {"S", "My Payslips", "payslips",     UIHelper.TEAL},
        {"N", "Announcements","announcements",UIHelper.PURPLE},
    };

    public EmpMainScreen(Map<String, String> user) {
        this.currentUser = user;
        setTitle("HRM — " + user.get("full_name"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setColor(UIHelper.SIDEBAR_TOP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sb.setPreferredSize(new Dimension(SIDEBAR_W, 0));

        // Logo
        JPanel logo = new JPanel(null);
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(SIDEBAR_W, 82));
        JPanel mk = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setColor(UIHelper.PRIMARY_LIGHT); g2.fillRoundRect(0, 0, 9, 9, 3, 3);
                g2.setColor(UIHelper.INDIGO); g2.fillRoundRect(12, 0, 9, 9, 3, 3);
                g2.setColor(new Color(255, 255, 255, 110)); g2.fillRoundRect(6, 12, 9, 9, 3, 3);
                g2.dispose();
            }
        };
        mk.setOpaque(false); mk.setBounds(22, 24, 24, 24); logo.add(mk);
        JLabel br = new JLabel("HRM"); br.setFont(new Font("Segoe UI", Font.BOLD, 19)); br.setForeground(Color.WHITE); br.setBounds(52, 22, 80, 26); logo.add(br);
        JLabel tg = new JLabel("Employee Portal"); tg.setFont(UIHelper.FONT_TINY); tg.setForeground(UIHelper.TEXT_MUTED); tg.setBounds(22, 52, 180, 14); logo.add(tg);

        // Nav
        navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(12, 10, 12, 10));
        buildNavItems();
        JPanel navWrap = new JPanel(new BorderLayout());
        navWrap.setOpaque(false);
        navWrap.add(navPanel, BorderLayout.NORTH);

        // Bottom
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(0, 10, 18, 10));
        bottom.add(divider());
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(buildUserCard());
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(buildLogoutBtn());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(logo);
        top.add(divider());
        top.add(navWrap);

        sb.add(top, BorderLayout.NORTH);
        sb.add(Box.createGlue(), BorderLayout.CENTER);
        sb.add(bottom, BorderLayout.SOUTH);
        return sb;
    }

    private JPanel createNavItem(String letter, String label, String key, Color accent) {
        float[] anim = {0f};
        JPanel item = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                boolean act = label.equals(activeNav);
                if (act) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(accent);
                    g2.fill(new RoundRectangle2D.Float(0, 7, 3, getHeight() - 14, 3, 3));
                } else if (anim[0] > 0) {
                    g2.setColor(new Color(255, 255, 255, (int)(10 * anim[0])));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(6, 10, 6, 10));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boolean act = label.equals(activeNav);

        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                boolean a = label.equals(activeNav);
                g2.setColor(a ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30) : new Color(255, 255, 255, 7));
                g2.fillRoundRect(0, 0, 28, 28, 8, 8);
                if (a) { g2.setColor(accent); g2.setStroke(new BasicStroke(1)); g2.drawRoundRect(0, 0, 27, 27, 8, 8); }
                g2.setColor(a ? accent : UIHelper.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(letter, (28 - fm.stringWidth(letter)) / 2, (28 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(28, 28); }
        };
        icon.setOpaque(false);
        JLabel txt = new JLabel(label);
        txt.setFont(new Font("Segoe UI", act ? Font.BOLD : Font.PLAIN, 13));
        txt.setForeground(act ? Color.WHITE : new Color(160, 175, 200));
        txt.setBorder(new EmptyBorder(0, 10, 0, 0));
        item.add(icon, BorderLayout.WEST);
        item.add(txt, BorderLayout.CENTER);

        Timer[] tr = {null};
        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (tr[0] != null) tr[0].stop(); tr[0] = new Timer(14, ev -> { anim[0] = Math.min(1, anim[0] + .15f); item.repaint(); if (anim[0] >= 1) ((Timer)ev.getSource()).stop(); }); tr[0].start(); }
            public void mouseExited(MouseEvent e) { if (tr[0] != null) tr[0].stop(); tr[0] = new Timer(14, ev -> { anim[0] = Math.max(0, anim[0] - .15f); item.repaint(); if (anim[0] <= 0) ((Timer)ev.getSource()).stop(); }); tr[0].start(); }
            public void mouseClicked(MouseEvent e) { activeNav = label; cardLayout.show(contentPanel, key); buildNavItems(); navPanel.revalidate(); navPanel.repaint(); }
        });
        return item;
    }

    private void buildNavItems() {
        navPanel.removeAll();
        JLabel s = new JLabel("MY MENU"); s.setFont(new Font("Segoe UI", Font.BOLD, 9)); s.setForeground(UIHelper.TEXT_MUTED);
        s.setBorder(new EmptyBorder(4, 10, 8, 0)); s.setAlignmentX(LEFT_ALIGNMENT); navPanel.add(s);
        for (Object[] n : NAV) { navPanel.add(createNavItem((String)n[0], (String)n[1], (String)n[2], (Color)n[3])); navPanel.add(Box.createVerticalStrut(2)); }
    }

    private JPanel buildUserCard() {
        String ini = getInitials(currentUser.get("full_name"));
        JPanel c = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) { Graphics2D g2 = UIHelper.aa(g); g2.setColor(new Color(255,255,255,6)); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12)); g2.dispose(); }
        };
        c.setOpaque(false); c.setBorder(new EmptyBorder(8, 10, 8, 10)); c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        JPanel av = new JPanel() {
            @Override protected void paintComponent(Graphics g) { Graphics2D g2 = UIHelper.aa(g); g2.setColor(new Color(UIHelper.TEAL.getRed(), UIHelper.TEAL.getGreen(), UIHelper.TEAL.getBlue(), 40)); g2.fillOval(0,0,34,34);
                g2.setColor(UIHelper.TEAL); g2.setStroke(new BasicStroke(1.5f)); g2.drawOval(1,1,32,32); g2.setFont(new Font("Segoe UI", Font.BOLD, 12)); FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, (34-fm.stringWidth(ini))/2, (34+fm.getAscent()-fm.getDescent())/2); g2.dispose(); }
            @Override public Dimension getPreferredSize() { return new Dimension(34, 34); }
        };
        av.setOpaque(false);
        JPanel tx = new JPanel(); tx.setOpaque(false); tx.setLayout(new BoxLayout(tx, BoxLayout.Y_AXIS)); tx.setBorder(new EmptyBorder(2, 8, 0, 0));
        JLabel nm = new JLabel(currentUser.get("full_name")); nm.setFont(new Font("Segoe UI", Font.BOLD, 11)); nm.setForeground(UIHelper.TEXT_WHITE);
        JLabel rl = new JLabel("Employee"); rl.setFont(UIHelper.FONT_TINY); rl.setForeground(UIHelper.TEXT_MUTED);
        tx.add(nm); tx.add(rl);
        c.add(av, BorderLayout.WEST); c.add(tx, BorderLayout.CENTER);
        return c;
    }

    private JPanel buildLogoutBtn() {
        JPanel w = new JPanel(new BorderLayout()); w.setOpaque(false); w.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JButton b = new JButton("Sign Out") {
            boolean h = false;
            { addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { h=true; repaint(); } public void mouseExited(MouseEvent e) { h=false; repaint(); } }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g); Color c = UIHelper.DANGER;
                if (h) { g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),30)); g2.fill(new RoundRectangle2D.Float(-2,-2,getWidth()+4,getHeight()+4,14,14)); g2.setColor(c.darker()); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10)); g2.setColor(Color.WHITE); }
                else { g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),15)); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10)); g2.setColor(c); }
                g2.setFont(UIHelper.FONT_BUTTON); FontMetrics fm = g2.getFontMetrics(); g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0, 36)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> { if (UIHelper.confirmDialog(this, "Sign Out", "Sign out?")) { dispose(); new LoginScreen(); } });
        w.add(b, BorderLayout.CENTER); return w;
    }

    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIHelper.BG);
        int empId = currentUser.get("employee_id") != null ? Integer.parseInt(currentUser.get("employee_id")) : 0;
        contentPanel.add(new EmpDashboard(this, empId), "dashboard");
        contentPanel.add(new EmpAttendancePanel(this, empId), "attendance");
        contentPanel.add(new EmpLeavePanel(this, empId), "leaves");
        contentPanel.add(new EmpPayslipPanel(this, empId), "payslips");
        contentPanel.add(new EmpAnnouncementPanel(this), "announcements");
        cardLayout.show(contentPanel, "dashboard");
        return contentPanel;
    }

    private static JComponent divider() {
        JComponent c = new JComponent() {
            @Override protected void paintComponent(Graphics g) { Graphics2D g2 = UIHelper.aa(g); g2.setPaint(new GradientPaint(12,0,new Color(255,255,255,20),getWidth()-12,0,new Color(255,255,255,0))); g2.drawLine(12,0,getWidth()-12,0); g2.dispose(); }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 1); }
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 1); }
        };
        c.setOpaque(false); return c;
    }

    public JFrame getFrame() { return this; }
    public Map<String, String> getUser() { return currentUser; }
    private static String getInitials(String n) { if (n == null || n.isEmpty()) return "?"; String[] p = n.trim().split("\\s+"); return p.length >= 2 ? (""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase() : (""+p[0].charAt(0)).toUpperCase(); }
}
