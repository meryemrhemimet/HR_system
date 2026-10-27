package app;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * UIHelper — Unified design system v2.
 *
 * Palette: "Indigo Mist"
 *   Background : #F0F4FF  Cards : #FFFFFF  Borders : #E8EEFF
 *   Primary    : #4F6EF7  Purple: #8B5CF6  Teal    : #0EA5B8
 *   Success    : #22C55E  Warning: #F59E0B  Danger  : #F43F5E
 *   Text       : #1A1D3A / #6B7280 / #9CA3AF
 *   Sidebar    : #0F172A → #1E1B4B gradient
 */
public class UIHelper {

    // ── Color Palette ────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(0x4F6EF7);
    public static final Color PRIMARY_LIGHT  = new Color(0x818CF8);
    public static final Color PRIMARY_DARK   = new Color(0x3730A3);
    public static final Color ACCENT         = new Color(0xF59E0B);
    public static final Color SUCCESS        = new Color(0x22C55E);
    public static final Color DANGER         = new Color(0xF43F5E);
    public static final Color WARNING        = new Color(0xF59E0B);
    public static final Color INFO           = new Color(0x4F6EF7);
    public static final Color PURPLE         = new Color(0x8B5CF6);
    public static final Color TEAL           = new Color(0x0EA5B8);
    public static final Color ROSE           = new Color(0xF43F5E);
    public static final Color INDIGO         = new Color(0x6366F1);
    public static final Color CYAN           = new Color(0x06B6D4);
    public static final Color GREEN          = new Color(0x22C55E);
    public static final Color AMBER          = new Color(0xF59E0B);

    // ── Backgrounds & Surfaces ───────────────────────────────────
    public static final Color BG             = new Color(0xF0F4FF);
    public static final Color CARD           = Color.WHITE;
    public static final Color CARD_BORDER    = new Color(0xE8EEFF);
    public static final Color INPUT_BG       = new Color(0xF8FAFC);

    // ── Sidebar ──────────────────────────────────────────────────
    public static final Color SIDEBAR_TOP    = new Color(0x0F172A);
    public static final Color SIDEBAR_BOT    = new Color(0x1E1B4B);
    public static final Color SIDEBAR_HOVER  = new Color(255, 255, 255, 12);
    public static final Color SIDEBAR_ACTIVE = new Color(0x4F6EF7, true);    // transparent overlay

    // ── Text ─────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(0x1A1D3A);
    public static final Color TEXT_SECONDARY = new Color(0x6B7280);
    public static final Color TEXT_MUTED     = new Color(0x9CA3AF);
    public static final Color TEXT_WHITE     = new Color(0xF8FAFC);
    public static final Color BORDER         = new Color(0xE2E8F0);
    public static final Color BORDER_FOCUS   = PRIMARY_LIGHT;
    public static final Color TABLE_STRIPE   = new Color(0xF8FAFC);
    public static final Color TABLE_HOVER    = new Color(0xEEF2FF);

    // ── Typography ───────────────────────────────────────────────
    private static final String FF = "Segoe UI";
    public static final Font FONT_TITLE      = new Font(FF, Font.BOLD,  24);
    public static final Font FONT_SUBTITLE   = new Font(FF, Font.BOLD,  17);
    public static final Font FONT_HEADING    = new Font(FF, Font.BOLD,  14);
    public static final Font FONT_BODY       = new Font(FF, Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font(FF, Font.PLAIN, 12);
    public static final Font FONT_TINY       = new Font(FF, Font.PLAIN, 10);
    public static final Font FONT_BUTTON     = new Font(FF, Font.BOLD,  13);
    public static final Font FONT_SIDEBAR    = new Font(FF, Font.PLAIN, 13);
    public static final Font FONT_STAT       = new Font(FF, Font.BOLD,  30);
    public static final Font FONT_NAV_HEADER = new Font(FF, Font.BOLD,  10);
    public static final Font FONT_TABLE      = new Font(FF, Font.PLAIN, 13);
    public static final Font FONT_TABLE_H    = new Font(FF, Font.BOLD,  12);

    // ── Dimensions ───────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH = 256;
    public static final int CARD_RADIUS   = 16;
    public static final int BTN_RADIUS    = 10;
    public static final int INPUT_RADIUS  = 10;
    public static final int INPUT_HEIGHT  = 42;
    public static final int BTN_HEIGHT    = 40;

    // ── Card ─────────────────────────────────────────────────────
    public static JPanel card() { return card(CARD_RADIUS); }

    public static JPanel card(int radius) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                // layered shadow
                for (int i = 6; i > 0; i--) {
                    g2.setColor(new Color(79, 110, 247, i * 3));
                    g2.fill(new RoundRectangle2D.Float(i, i + 1, getWidth() - 2*i, getHeight() - 2*i, radius+i, radius+i));
                }
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-2, radius, radius));
                // subtle border
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-2, getHeight()-3, radius, radius));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        return p;
    }

    // ── Stat Card (Dashboard) ─────────────────────────────────────
    public static JPanel statCard(String title, String value, String subtitle, Color accent, String icon) {
        Color bgTint = tint(accent, 0.06f);

        JPanel outer = new JPanel() {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                // shadow
                for (int i = 6; i > 0; i--) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), i*2));
                    g2.fill(new RoundRectangle2D.Float(i, i+2, getWidth()-2*i, getHeight()-2*i, 18+i, 18+i));
                }
                g2.setColor(hover ? new Color(
                    Math.min(255, bgTint.getRed()+6),
                    Math.min(255, bgTint.getGreen()+6),
                    Math.min(255, bgTint.getBlue()+6)) : bgTint);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-3, 18, 18));
                // accent border
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hover ? 120 : 70));
                g2.setStroke(new BasicStroke(hover ? 1.8f : 1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-2, getHeight()-4, 18, 18));
                // top accent bar
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, 4, 18, 18));
                g2.fillRect(0, 2, getWidth()-1, 2);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setLayout(new BorderLayout());
        outer.setBorder(new EmptyBorder(20, 22, 16, 22));
        outer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Icon bubble
        JPanel iconBubble = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22));
                g2.fillOval(0, 0, 44, 44);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, 42, 42);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, (44-fm.stringWidth(icon))/2, (44+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(44, 44); }
        };
        iconBubble.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font(FF, Font.PLAIN, 11));
        lTitle.setForeground(TEXT_SECONDARY);

        JLabel lValue = new JLabel(value);
        lValue.setFont(new Font(FF, Font.BOLD, 26));
        lValue.setForeground(TEXT_PRIMARY);

        JLabel lSub = new JLabel(subtitle);
        lSub.setFont(FONT_TINY);
        lSub.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));

        textPanel.add(lTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lValue);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lSub);

        gbc.gridx=0; gbc.gridy=0; gbc.weightx=0; gbc.insets=new Insets(0,0,0,14);
        content.add(iconBubble, gbc);
        gbc.gridx=1; gbc.weightx=1; gbc.insets=new Insets(0,0,0,0);
        content.add(textPanel, gbc);

        outer.add(content, BorderLayout.CENTER);
        return outer;
    }

    // ── Buttons ──────────────────────────────────────────────────
    public static JButton primaryButton(String text) { return styledButton(text, PRIMARY, Color.WHITE); }
    public static JButton successButton(String text) { return styledButton(text, SUCCESS, Color.WHITE); }
    public static JButton dangerButton(String  text) { return styledButton(text, DANGER,  Color.WHITE); }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text) {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                if (hover) {
                    g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 12));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BTN_RADIUS, BTN_RADIUS));
                }
                g2.setColor(hover ? PRIMARY : BORDER);
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(0.7f, 0.7f, getWidth()-2, getHeight()-2, BTN_RADIUS, BTN_RADIUS));
                g2.setColor(hover ? PRIMARY : TEXT_SECONDARY);
                g2.setFont(FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                              (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width+32, BTN_HEIGHT));
        styleBtn(btn);
        return btn;
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            boolean hover=false, pressed=false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e)  { hover=true;   repaint(); }
                public void mouseExited(MouseEvent e)   { hover=false;  pressed=false; repaint(); }
                public void mousePressed(MouseEvent e)  { pressed=true;  repaint(); }
                public void mouseReleased(MouseEvent e) { pressed=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                Color c = pressed ? bg.darker() : hover ? brighten(bg) : bg;
                if (hover && !pressed) {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 28));
                    g2.fill(new RoundRectangle2D.Float(-2, 2, getWidth()+4, getHeight()+2, BTN_RADIUS+4, BTN_RADIUS+4));
                }
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BTN_RADIUS, BTN_RADIUS));
                g2.setColor(fg);
                g2.setFont(FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                              (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width+40, BTN_HEIGHT));
        styleBtn(btn);
        return btn;
    }

    private static void styleBtn(JButton btn) {
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ── Text Field ───────────────────────────────────────────────
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INPUT_RADIUS, INPUT_RADIUS));
                Color bc = isFocusOwner() ? PRIMARY : CARD_BORDER;
                g2.setColor(bc);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, INPUT_RADIUS, INPUT_RADIUS));
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g3 = aa(g);
                    g3.setColor(TEXT_MUTED);
                    g3.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g3.drawString(placeholder, ins.left, getHeight()/2+g3.getFontMetrics().getAscent()/2-2);
                    g3.dispose();
                }
            }
        };
        f.setOpaque(false);
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setBorder(new EmptyBorder(0, 14, 0, 14));
        f.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.repaint(); }
            public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    // ── Password Field ───────────────────────────────────────────
    public static JPasswordField styledPasswordField(String placeholder) {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INPUT_RADIUS, INPUT_RADIUS));
                g2.setColor(isFocusOwner() ? PRIMARY : CARD_BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, INPUT_RADIUS, INPUT_RADIUS));
                g2.dispose();
                super.paintComponent(g);
                if (getPassword().length==0 && !isFocusOwner()) {
                    Graphics2D g3 = aa(g);
                    g3.setColor(TEXT_MUTED);
                    g3.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g3.drawString(placeholder, ins.left, getHeight()/2+g3.getFontMetrics().getAscent()/2-2);
                    g3.dispose();
                }
            }
        };
        f.setOpaque(false);
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setBorder(new EmptyBorder(0, 14, 0, 14));
        f.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.repaint(); }
            public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    // ── ComboBox ─────────────────────────────────────────────────
    public static JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT_PRIMARY);
        cb.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            new EmptyBorder(4, 10, 4, 10)));
        return cb;
    }

    // ── Table ─────────────────────────────────────────────────────
    public static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            int hoveredRow = -1;
            { addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    int r = rowAtPoint(e.getPoint());
                    if (r != hoveredRow) { hoveredRow=r; repaint(); }
                }
            });
              addMouseListener(new MouseAdapter() {
                public void mouseExited(MouseEvent e) { hoveredRow=-1; repaint(); }
            }); }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(0x4F6EF7, true) {{ }});
                    c.setBackground(TABLE_HOVER);
                    c.setForeground(TEXT_PRIMARY);
                } else if (row == hoveredRow) {
                    c.setBackground(new Color(0xF5F3FF));
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    c.setBackground(row%2==0 ? CARD : TABLE_STRIPE);
                    c.setForeground(TEXT_PRIMARY);
                }
                if (c instanceof JLabel) ((JLabel)c).setBorder(new EmptyBorder(0,14,0,14));
                return c;
            }
        };
        table.setFont(FONT_TABLE);
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(CARD);
        table.setSelectionBackground(TABLE_HOVER);
        table.setSelectionForeground(TEXT_PRIMARY);
        JTableHeader th = table.getTableHeader();
        th.setFont(FONT_TABLE_H);
        th.setBackground(new Color(0xF5F7FF));
        th.setForeground(TEXT_SECONDARY);
        th.setPreferredSize(new Dimension(0, 44));
        th.setBorder(BorderFactory.createMatteBorder(0,0,1,0, CARD_BORDER));
        th.setReorderingAllowed(false);
        return table;
    }

    // ── Scroll Pane ───────────────────────────────────────────────
    public static JScrollPane styledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8,0));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        if (sp.getHorizontalScrollBar()!=null) {
            sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
            sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0,8));
        }
        return sp;
    }

    // ── Badge / Pill ──────────────────────────────────────────────
    public static JLabel badge(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font(FF, Font.BOLD, 10));
        l.setForeground(color.darker());
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }

    // ── Section Header ────────────────────────────────────────────
    public static JPanel sectionHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(FONT_TITLE);
        lTitle.setForeground(TEXT_PRIMARY);
        p.add(lTitle, BorderLayout.NORTH);
        if (subtitle != null) {
            JLabel lSub = new JLabel(subtitle);
            lSub.setFont(FONT_BODY);
            lSub.setForeground(TEXT_SECONDARY);
            lSub.setBorder(new EmptyBorder(4, 0, 0, 0));
            p.add(lSub, BorderLayout.CENTER);
        }
        return p;
    }

    // ── Form Label ────────────────────────────────────────────────
    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_PRIMARY);
        l.setBorder(new EmptyBorder(0, 2, 6, 0));
        return l;
    }

    // ── Toast ─────────────────────────────────────────────────────
    public static void showToast(JFrame parent, String message, boolean success) {
        Color bg = success ? SUCCESS : DANGER;
        JWindow toast = new JWindow(parent);
        JLabel label = new JLabel(message, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                // glow
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 40));
                g2.fill(new RoundRectangle2D.Float(-4, 2, getWidth()+8, getHeight()+4, 22, 22));
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(FONT_BUTTON);
        label.setForeground(Color.WHITE);
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(12, 32, 12, 32));
        toast.getContentPane().add(label);
        toast.setBackground(new Color(0,0,0,0));
        toast.pack();
        if (parent!=null) {
            toast.setLocation(parent.getX()+(parent.getWidth()-toast.getWidth())/2, parent.getY()+72);
        }
        toast.setVisible(true);
        new Timer(2500, e -> {
            Timer ft = new Timer(25, null);
            ft.addActionListener(new ActionListener() {
                float op = 1f;
                public void actionPerformed(ActionEvent ev) {
                    op -= 0.09f;
                    if (op<=0) { ft.stop(); toast.dispose(); }
                    else toast.setOpacity(Math.max(0f,op));
                }
            });
            ft.start();
        }) {{ setRepeats(false); }}.start();
    }

    // ── Confirm Dialog ────────────────────────────────────────────
    public static boolean confirmDialog(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title,
               JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ── Search Field ──────────────────────────────────────────────
    public static JTextField searchField(String placeholder) {
        JTextField f = styledField(placeholder);
        f.setPreferredSize(new Dimension(300, 40));
        return f;
    }

    // ── Empty State ───────────────────────────────────────────────
    public static JPanel emptyState(String emoji, String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(60, 0, 60, 0));
        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_BODY);
        subLabel.setForeground(TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(iconLabel);
        p.add(Box.createVerticalStrut(14));
        p.add(titleLabel);
        p.add(Box.createVerticalStrut(6));
        p.add(subLabel);
        return p;
    }

    // ── Status Color ──────────────────────────────────────────────
    public static Color statusColor(String status) {
        if (status==null) return TEXT_MUTED;
        switch (status) {
            case "Active": case "Present": case "Approved": case "Paid": case "Completed": return SUCCESS;
            case "Late":   case "Pending": case "Half Day":                                 return WARNING;
            case "Absent": case "Rejected": case "Terminated":
            case "Cancelled": case "Suspended":                                             return DANGER;
            case "On Leave":                                                                return INFO;
            default:                                                                        return TEXT_MUTED;
        }
    }

    // ── Modern Scrollbar ──────────────────────────────────────────
    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(160, 170, 200, 140);
            trackColor = new Color(0,0,0,0);
        }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() { JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = aa(g);
            g2.setColor(new Color(100, 120, 200, 90));
            g2.fill(new RoundRectangle2D.Float(r.x+1, r.y+1, r.width-2, r.height-2, 6, 6));
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {}
    }

    // ── Internal Helpers ─────────────────────────────────────────
    static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,   RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    private static Color tint(Color c, float alpha) {
        return new Color(
            (int)(c.getRed()  + (255-c.getRed())  *(1-alpha)),
            (int)(c.getGreen()+ (255-c.getGreen())*(1-alpha)),
            (int)(c.getBlue() + (255-c.getBlue()) *(1-alpha))
        );
    }

    private static Color brighten(Color c) {
        return new Color(
            Math.min(255, c.getRed()+20),
            Math.min(255, c.getGreen()+20),
            Math.min(255, c.getBlue()+20));
    }
}