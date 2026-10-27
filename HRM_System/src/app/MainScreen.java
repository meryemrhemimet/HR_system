package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Map;

public class MainScreen extends JFrame {

    private static final int SIDEBAR_W = 256;

    private final Map<String, String> currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private String activeNav = "Dashboard";
    private JPanel navPanel;

    // icon-letter, label, cardKey, accent color
    private static final Object[][] NAV_ITEMS = {
        {"D", "Dashboard",    "dashboard",    UIHelper.PRIMARY},
        {"E", "Employees",    "employees",    UIHelper.TEAL},
        {"P", "Departments",  "departments",  UIHelper.PURPLE},
        {"A", "Attendance",   "attendance",   UIHelper.SUCCESS},
        {"L", "Leave",        "leave",        UIHelper.AMBER},
        {"S", "Payroll",      "payroll",      UIHelper.GREEN},
        {"R", "Performance",  "performance",  UIHelper.PURPLE},
        {"N", "Announcements","announcements",UIHelper.ROSE},
    };

    public MainScreen(Map<String, String> user) {
        this.currentUser = user;
        setTitle("HRM System — " + user.get("full_name"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BG);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setColor(UIHelper.SIDEBAR_TOP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 6));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));

        sidebar.add(buildLogoArea(), BorderLayout.NORTH);
        sidebar.add(buildNavArea(), BorderLayout.CENTER);
        sidebar.add(buildBottomArea(), BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildLogoArea() {
        JPanel p = new JPanel(null);
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(SIDEBAR_W, 86));

        JPanel mark = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setColor(UIHelper.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, 9, 9, 3, 3);
                g2.setColor(UIHelper.INDIGO);
                g2.fillRoundRect(12, 0, 9, 9, 3, 3);
                g2.setColor(new Color(255, 255, 255, 110));
                g2.fillRoundRect(6, 12, 9, 9, 3, 3);
                g2.dispose();
            }
        };
        mark.setOpaque(false);
        mark.setBounds(24, 26, 24, 24);
        p.add(mark);

        JLabel brand = new JLabel("HRM");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);
        brand.setBounds(56, 24, 80, 26);
        p.add(brand);

        JLabel tag = new JLabel("HR Manager Portal");
        tag.setFont(UIHelper.FONT_TINY);
        tag.setForeground(UIHelper.TEXT_MUTED);
        tag.setBounds(24, 56, 180, 14);
        p.add(tag);

        return p;
    }

    private JPanel buildNavArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(14, 12, 14, 12));
        buildNavItems();

        wrapper.add(navPanel, BorderLayout.NORTH);
        return wrapper;
    }

    private void buildNavItems() {
        navPanel.removeAll();

       
       
      

        for (Object[] item : NAV_ITEMS) {
            navPanel.add(createNavItem((String) item[0], (String) item[1], (String) item[2], (Color) item[3]));
            navPanel.add(Box.createVerticalStrut(2));
        }
    }

    private JPanel createNavItem(String iconLetter, String label, String cardKey, Color accent) {
        float[] anim = {0f};

        JPanel item = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                boolean active = label.equals(activeNav);
                if (active) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(accent);
                    g2.fill(new RoundRectangle2D.Float(0, 7, 3, getHeight() - 14, 3, 3));
                } else if (anim[0] > 0) {
                    g2.setColor(new Color(255, 255, 255, (int) (10 * anim[0])));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(6, 10, 6, 10));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        boolean active = label.equals(activeNav);

        // Icon: letter in rounded square
        JPanel iconBadge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                boolean act = label.equals(activeNav);
                if (act) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                    g2.fill(new RoundRectangle2D.Float(0, 0, 30, 30, 8, 8));
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, 29, 29, 8, 8));
                } else {
                    g2.setColor(new Color(255, 255, 255, 7));
                    g2.fill(new RoundRectangle2D.Float(0, 0, 30, 30, 8, 8));
                }
                g2.setColor(act ? accent : UIHelper.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(iconLetter, (30 - fm.stringWidth(iconLetter)) / 2,
                        (30 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(30, 30); }
        };
        iconBadge.setOpaque(false);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        textLabel.setForeground(active ? Color.WHITE : new Color(160, 175, 200));
        textLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        item.add(iconBadge, BorderLayout.WEST);
        item.add(textLabel, BorderLayout.CENTER);

        Timer[] timerRef = {null};
        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { timerRef[0] = animHover(true, item, anim, timerRef[0]); }
            public void mouseExited(MouseEvent e) { timerRef[0] = animHover(false, item, anim, timerRef[0]); }
            public void mouseClicked(MouseEvent e) {
                activeNav = label;
                cardLayout.show(contentPanel, cardKey);
                refreshSidebar();
            }
        });

        return item;
    }

    private Timer animHover(boolean in, JPanel target, float[] anim, Timer old) {
        if (old != null) old.stop();
        Timer t = new Timer(14, null);
        t.addActionListener(ev -> {
            anim[0] = in ? Math.min(1f, anim[0] + 0.15f) : Math.max(0f, anim[0] - 0.15f);
            target.repaint();
            if ((in && anim[0] >= 1f) || (!in && anim[0] <= 0f)) ((Timer) ev.getSource()).stop();
        });
        t.start();
        return t;
    }

    private JPanel buildBottomArea() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(0, 12, 20, 12));

        // divider
        JComponent div = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 0),
                        getWidth() / 2f, 0, new Color(255, 255, 255, 20)));
                g2.fillRect(0, 0, getWidth() / 2, 1);
                g2.setPaint(new GradientPaint(getWidth() / 2f, 0, new Color(255, 255, 255, 20),
                        getWidth(), 0, new Color(255, 255, 255, 0)));
                g2.fillRect(getWidth() / 2, 0, getWidth() / 2, 1);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 1); }
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 1); }
        };

        p.add(div);
        p.add(Box.createVerticalStrut(14));
        p.add(buildUserCard());
        p.add(Box.createVerticalStrut(10));
        p.add(buildLogoutButton());
        return p;
    }

    private JPanel buildUserCard() {
        String initials = getInitials(currentUser.get("full_name"));

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setColor(new Color(255, 255, 255, 6));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                g2.setColor(new Color(UIHelper.PRIMARY.getRed(), UIHelper.PRIMARY.getGreen(), UIHelper.PRIMARY.getBlue(), 40));
                g2.fillOval(0, 0, 38, 38);
                g2.setColor(UIHelper.PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, 36, 36);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(UIHelper.PRIMARY_LIGHT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (38 - fm.stringWidth(initials)) / 2,
                        (38 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(38, 38); }
        };
        avatar.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBorder(new EmptyBorder(3, 10, 0, 0));

        JLabel name = new JLabel(currentUser.get("full_name"));
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(UIHelper.TEXT_WHITE);

        JLabel role = new JLabel("HR Manager");
        role.setFont(UIHelper.FONT_TINY);
        role.setForeground(UIHelper.TEXT_MUTED);

        text.add(name);
        text.add(role);

        card.add(avatar, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildLogoutButton() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton btn = new JButton("Sign Out") {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIHelper.aa(g);
                Color c = UIHelper.DANGER;
                if (hover) {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                    g2.fill(new RoundRectangle2D.Float(-2, -2, getWidth() + 4, getHeight() + 4, 14, 14));
                    g2.setColor(c.darker());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 15));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                    g2.setColor(c);
                }
                g2.setFont(UIHelper.FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (UIHelper.confirmDialog(this, "Sign Out", "Are you sure you want to sign out?")) {
                dispose();
                new LoginScreen();
            }
        });
        wrapper.add(btn, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIHelper.BG);

        contentPanel.add(new DashboardScreen(this), "dashboard");
        contentPanel.add(new EmployeeScreen(this), "employees");
        contentPanel.add(new DepartmentScreen(this), "departments");
        contentPanel.add(new AttendanceScreen(this), "attendance");
        contentPanel.add(new LeaveScreen(this), "leave");
        contentPanel.add(new PayrollScreen(this), "payroll");
        contentPanel.add(new PerformanceScreen(this), "performance");
        contentPanel.add(new AnnouncementScreen(this), "announcements");

        cardLayout.show(contentPanel, "dashboard");
        return contentPanel;
    }

    private void refreshSidebar() {
        buildNavItems();
        navPanel.revalidate();
        navPanel.repaint();
    }

    public void reloadDashboard() {
        contentPanel.remove(contentPanel.getComponent(0)); // Remove old dashboard
        contentPanel.add(new DashboardScreen(this), "dashboard");
        cardLayout.show(contentPanel, "dashboard");
    }

    public JFrame getFrame() { return this; }
    public Map<String, String> getUser() { return currentUser; }

    private static String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        return parts.length >= 2
                ? ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase()
                : ("" + parts[0].charAt(0)).toUpperCase();
    }
}
