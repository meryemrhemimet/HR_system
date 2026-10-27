package app;

import db.HRMdao;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Map;

public class LoginScreen extends JFrame {

    private static final Color BRAND_DARK = new Color(30, 27, 75);
    private static final Color BRAND_MID  = new Color(55, 48, 163);
    private static final Color BRAND_GLOW = new Color(99, 102, 241);
    private static final Color ACCENT     = new Color(129, 140, 248);
    private static final Color ACCENT_S   = new Color(165, 180, 252);
    private static final Color PAGE_BG    = new Color(246, 248, 253);
    private static final Color FORM_BG    = Color.WHITE;
    private static final Color IN_BG      = new Color(249, 250, 253);
    private static final Color IN_BR      = new Color(220, 226, 240);
    private static final Color IN_BR_F    = new Color(99, 102, 241);
    private static final Color IN_GLOW    = new Color(99, 102, 241, 18);
    private static final Color BTN_B      = new Color(79, 70, 229);
    private static final Color BTN_H      = new Color(67, 56, 202);
    private static final Color BTN_P      = new Color(55, 48, 163);
    private static final Color T_TITLE    = new Color(10, 18, 42);
    private static final Color T_BODY     = new Color(65, 80, 110);
    private static final Color T_LABEL    = new Color(85, 100, 130);
    private static final Color T_MUTED    = new Color(160, 172, 200);
    private static final Color T_HINT     = new Color(190, 200, 220);
    private static final Color LINK_C     = new Color(79, 70, 229);
    private static final Color ERR_BG     = new Color(254, 242, 242);
    private static final Color ERR_BR     = new Color(252, 165, 165);
    private static final Color ERR_TXT    = new Color(185, 28, 28);
    private static final Color SUC_BG     = new Color(240, 253, 244);
    private static final Color SUC_BR     = new Color(167, 243, 208);
    private static final Color SUC_TXT    = new Color(21, 128, 61);

    private static final int FW = 960, FH = 600, BW = 440, FORM_PW = FW - BW, ANIM_MS = 380;

    private boolean animating = false;
    private JLayeredPane layered;
    private JPanel brandPanel, loginPanel, registerPanel;

    // Login fields
    private InputField loginUser;
    private PassField loginPass;
    private JCheckBox showPassCB;
    private JPanel loginErr;
    private JLabel loginErrMsg;
    private ActionBtn loginBtn;

    // Register fields
    private InputField regName, regUser, regEmail;
    private PassField regPass, regConf;
    private JPanel regMsgBox;
    private JLabel regMsgLabel;

    private final HRMdao dao = new HRMdao();

    public LoginScreen() {
        setTitle("HRM System — Sign In");
        setSize(FW, FH); setMinimumSize(new Dimension(FW, FH));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); setResizable(false);

        layered = new JLayeredPane(); layered.setLayout(null);
        brandPanel    = buildBrand();
        loginPanel    = buildLoginForm();
        registerPanel = buildRegisterForm();

        brandPanel.setBounds(0, 0, BW, FH);
        loginPanel.setBounds(BW, 0, FORM_PW, FH);
        registerPanel.setBounds(-FORM_PW, 0, FORM_PW, FH);

        layered.add(brandPanel, JLayeredPane.DEFAULT_LAYER);
        layered.add(loginPanel, JLayeredPane.DEFAULT_LAYER);
        layered.add(registerPanel, JLayeredPane.DEFAULT_LAYER);

        setContentPane(layered);
        bindLogin();
        setVisible(true);
    }

    // ── Brand Panel ─────────────────────────────────────────────
    private JPanel buildBrand() {
        JPanel p = new JPanel(null) {
            float a = 0; final Timer t = new Timer(40, e -> { a += 0.025f; repaint(); }); { t.start(); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g); int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, BRAND_DARK, w, h, BRAND_MID)); g2.fillRect(0, 0, w, h);
                float ga = 50 + (float)(Math.sin(a) * 10);
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(w * .4f, h * .38f), 220,
                    new float[]{0, 1}, new Color[]{new Color(99, 102, 241, (int) ga), new Color(0, 0, 0, 0)}));
                g2.fillOval(w / 2 - 220, h / 2 - 240, 440, 440);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .06f));
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(w * .12, 90 + Math.sin(a) * 22, 100, 100));
                g2.fill(new RoundRectangle2D.Double(w * .6, h * .55 + Math.cos(a * .8) * 18, 80, 80, 18, 18));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
                g2.setColor(new Color(255, 255, 255, 20));
                for (int c = 0; c < 8; c++) for (int r = 0; r < 5; r++) g2.fillOval(36 + c * 18, h - 130 + r * 18, 3, 3);
                g2.dispose();
            }
        };
        p.setOpaque(true);
        JPanel mk = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(ACCENT); g2.fillRoundRect(0, 0, 10, 10, 3, 3);
                g2.setColor(ACCENT_S); g2.fillRoundRect(13, 0, 10, 10, 3, 3);
                g2.setColor(new Color(255, 255, 255, 140)); g2.fillRoundRect(6, 13, 10, 10, 3, 3);
                g2.dispose();
            }
        };
        mk.setOpaque(false); mk.setBounds(36, 48, 26, 26); p.add(mk);
        p.add(lbl("HRM System", new Font("Segoe UI", Font.BOLD, 17), Color.WHITE, 68, 50, 200, 22));
        p.add(lbl("Manage", new Font("Segoe UI", Font.BOLD, 42), Color.WHITE, 36, 175, 340, 50));
        p.add(lbl("your people.", new Font("Segoe UI", Font.BOLD, 42), ACCENT, 36, 222, 380, 50));
        p.add(lbl("Drive results.", new Font("Segoe UI", Font.BOLD, 42), Color.WHITE, 36, 269, 380, 50));
        p.add(lbl("The complete HR platform for", new Font("Segoe UI", Font.PLAIN, 14), new Color(160, 185, 230), 36, 340, 320, 20));
        p.add(lbl("workforce management & payroll.", new Font("Segoe UI", Font.PLAIN, 14), new Color(160, 185, 230), 36, 360, 320, 20));
        
        return p;
    }

    // ── Login Form ──────────────────────────────────────────────
    private JPanel buildLoginForm() {
        JPanel p = bgPanel(); JPanel c = card(360, 490); p.add(c);
        int lx = 28, fw = 304;
        c.add(lbl("Welcome back", new Font("Segoe UI", Font.BOLD, 28), T_TITLE, lx, 36, fw, 36));
        c.add(lbl("Sign in to your portal", new Font("Segoe UI", Font.PLAIN, 14), T_BODY, lx, 76, fw, 20));
        c.add(hr(lx, 112, fw));
        c.add(fl("Username", lx, 130));
        loginUser = new InputField("Enter username"); loginUser.setBounds(lx, 152, fw, 46); c.add(loginUser);
        c.add(fl("Password", lx, 214));
        loginPass = new PassField(); loginPass.setBounds(lx, 236, fw, 46); c.add(loginPass);
        showPassCB = new JCheckBox("Show password"); showPassCB.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPassCB.setForeground(T_LABEL); showPassCB.setOpaque(false); showPassCB.setBounds(lx, 290, fw, 22); c.add(showPassCB);
        loginErr = errBox(); loginErr.setBounds(lx, 320, fw, 36); loginErr.setVisible(false);
        JLabel ei = new JLabel("\u26A0"); ei.setFont(new Font("Segoe UI", Font.PLAIN, 13)); ei.setForeground(ERR_TXT); ei.setBounds(12, 9, 16, 18); loginErr.add(ei);
        loginErrMsg = new JLabel(" "); loginErrMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12)); loginErrMsg.setForeground(ERR_TXT); loginErrMsg.setBounds(32, 9, fw - 44, 18); loginErr.add(loginErrMsg);
        c.add(loginErr);
        loginBtn = new ActionBtn("Sign In  \u2192", BTN_B, BTN_H, BTN_P); loginBtn.setBounds(lx, 328, fw, 50); c.add(loginBtn);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0)); row.setOpaque(false); row.setBounds(lx, 395, fw, 24);
        row.add(new JLabel("Don't have an account?") {{ setFont(new Font("Segoe UI", Font.PLAIN, 13)); setForeground(T_MUTED); }});
        JLabel link = new JLabel("Create an account"); link.setFont(new Font("Segoe UI", Font.BOLD, 13)); link.setForeground(LINK_C);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { link.setText("<html><u>Create an account</u></html>"); }
            public void mouseExited(MouseEvent e) { link.setText("Create an account"); }
            public void mouseClicked(MouseEvent e) { if (!animating) slideToRegister(); }
        });
        row.add(link); c.add(row);
        
        return p;
    }

    // ── Register Form ───────────────────────────────────────────
    private JPanel buildRegisterForm() {
        JPanel p = bgPanel(); JPanel c = card(360, 490); p.add(c);
        int lx = 28, fw = 304, half = (fw - 12) / 2, FH2 = 38, LH = 16, GAP = 5;

        c.add(lbl("Create an account", new Font("Segoe UI", Font.BOLD, 26), T_TITLE, lx, 26, fw, 34));
        c.add(lbl("Employee registration", new Font("Segoe UI", Font.PLAIN, 13), T_BODY, lx, 62, fw, 18));
        c.add(hr(lx, 90, fw));

        int y = 102;
        c.add(fl("Full Name *", lx, y));
        regName = new InputField("e.g. Youssef Benali"); regName.setBounds(lx, y + LH + GAP, fw, FH2); c.add(regName);

        y += LH + GAP + FH2 + 8;
        c.add(fl("Username *", lx, y));
        regUser = new InputField("e.g. youssef"); regUser.setBounds(lx, y + LH + GAP, fw, FH2); c.add(regUser);

        y += LH + GAP + FH2 + 8;
        c.add(fl("Email ", lx, y));
        regEmail = new InputField("your.email@company.ma"); regEmail.setBounds(lx, y + LH + GAP + 2, fw, FH2); c.add(regEmail);

        y += LH + GAP + FH2 + 12;
        c.add(fl("Password *", lx, y)); c.add(fl("Confirm *", lx + half + 12, y));
        regPass = new PassField(); regPass.setBounds(lx, y + LH + GAP, half, FH2); c.add(regPass);
        regConf = new PassField(); regConf.setBounds(lx + half + 12, y + LH + GAP, half, FH2); c.add(regConf);

        y += LH + GAP + FH2 + 10;
        regMsgBox = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor((Color) getClientProperty("bg")); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor((Color) getClientProperty("br")); g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); g2.dispose();
            }
        };
        regMsgBox.putClientProperty("bg", ERR_BG); regMsgBox.putClientProperty("br", ERR_BR);
        regMsgBox.setOpaque(false); regMsgBox.setBounds(lx, y, fw, 34); regMsgBox.setVisible(false);
        regMsgLabel = new JLabel(" "); regMsgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        regMsgLabel.setForeground(ERR_TXT); regMsgLabel.setBounds(10, 9, fw - 20, 16); regMsgBox.add(regMsgLabel);
        c.add(regMsgBox);

        y += 8;
        ActionBtn createBtn = new ActionBtn("Create Account  \u2192", BTN_B, BTN_H, BTN_P);
        createBtn.setBounds(lx, y, fw, 48); c.add(createBtn);
        createBtn.addActionListener(e -> doRegister());

        y += 56;
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0)); row.setOpaque(false); row.setBounds(lx, y, fw, 24);
        row.add(new JLabel("Already have an account?") {{ setFont(new Font("Segoe UI", Font.PLAIN, 13)); setForeground(T_MUTED); }});
        JLabel sl = new JLabel("Sign in"); sl.setFont(new Font("Segoe UI", Font.BOLD, 13)); sl.setForeground(LINK_C);
        sl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sl.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { sl.setText("<html><u>Sign in</u></html>"); }
            public void mouseExited(MouseEvent e) { sl.setText("Sign in"); }
            public void mouseClicked(MouseEvent e) { if (!animating) slideToLogin(); }
        });
        row.add(sl); c.add(row);
        return p;
    }

    // ── Slide Animation ─────────────────────────────────────────
    private void slideToRegister() {
        animating = true;
        int bs = brandPanel.getX(), bt = FW - BW;
        int rs = registerPanel.getX(), rt = 0;
        int ls = loginPanel.getX(), lt = FW;
        long start = System.currentTimeMillis();
        new Timer(8, e -> {
            double t = Math.min(1, (double)(System.currentTimeMillis() - start) / ANIM_MS);
            double ease = t < .5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
            brandPanel.setLocation((int)(bs + (bt - bs) * ease), 0);
            registerPanel.setLocation((int)(rs + (rt - rs) * ease), 0);
            loginPanel.setLocation((int)(ls + (lt - ls) * ease), 0);
            layered.repaint();
            if (t >= 1) { ((Timer) e.getSource()).stop(); animating = false; setTitle("HRM — Create Account"); }
        }).start();
    }

    private void slideToLogin() {
        animating = true;
        int bs = brandPanel.getX(), bt = 0;
        int ls = loginPanel.getX(), lt = BW;
        int rs = registerPanel.getX(), rt = -FORM_PW;
        long start = System.currentTimeMillis();
        new Timer(8, e -> {
            double t = Math.min(1, (double)(System.currentTimeMillis() - start) / ANIM_MS);
            double ease = t < .5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
            brandPanel.setLocation((int)(bs + (bt - bs) * ease), 0);
            loginPanel.setLocation((int)(ls + (lt - ls) * ease), 0);
            registerPanel.setLocation((int)(rs + (rt - rs) * ease), 0);
            layered.repaint();
            if (t >= 1) { ((Timer) e.getSource()).stop(); animating = false; setTitle("HRM — Sign In"); clearReg(); }
        }).start();
    }

    private void clearReg() { regName.setText(""); regUser.setText(""); regEmail.setText(""); regPass.setText(""); regConf.setText(""); regMsgBox.setVisible(false); }

    // ── Login Logic ─────────────────────────────────────────────
    private void bindLogin() {
        loginBtn.addActionListener(e -> doLogin());
        loginUser.addKeyListener(new KeyAdapter() { public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) loginPass.requestFocus(); } });
        loginPass.addKeyListener(new KeyAdapter() { public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin(); } });
        showPassCB.addActionListener(e -> loginPass.setEchoChar(showPassCB.isSelected() ? 0 : '\u25CF'));
    }

    private void doLogin() {
        String u = loginUser.getText().trim(), p = new String(loginPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) { showLoginErr("Please fill in all fields."); return; }
        loginBtn.setEnabled(false); loginBtn.setText("Signing in\u2026");
        new SwingWorker<Map<String, String>, Void>() {
            @Override protected Map<String, String> doInBackground() { return dao.authenticate(u, p); }
            @Override protected void done() {
                loginBtn.setEnabled(true); loginBtn.setText("Sign In  \u2192");
                try {
                    Map<String, String> r = get();
                    if (r != null) {
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            if ("EMPLOYEE".equals(r.get("role"))) new EmpMainScreen(r);
                            else new MainScreen(r);
                        });
                    } else { showLoginErr("Invalid username or password."); loginPass.setText(""); }
                } catch (Exception ex) { showLoginErr("Database error."); ex.printStackTrace(); }
            }
        }.execute();
    }

    private void showLoginErr(String m) { loginErrMsg.setText(m); loginErr.setVisible(true); loginBtn.setLocation(loginBtn.getX(), 366); shake(); }

    // ── Register Logic ──────────────────────────────────────────
    private void doRegister() {
        String name = regName.getText().trim(), user = regUser.getText().trim();
        String email = regEmail.getText().trim(), pass = new String(regPass.getPassword()), conf = new String(regConf.getPassword());
        String role = "EMPLOYEE";

        if (name.isEmpty() || user.isEmpty() || email.isEmpty() || pass.isEmpty()) { showRegMsg("Fill all required fields.", false); return; }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) { showRegMsg("Invalid email.", false); return; }
        if (pass.length() < 4) { showRegMsg("Password must be 4+ characters.", false); return; }
        if (!pass.equals(conf)) { showRegMsg("Passwords don't match.", false); return; }

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                if (dao.isUsernameTaken(user)) return "Username already taken.";
                if (dao.isEmailTaken(email)) return "Email already registered.";
                int empId = -1;
                if ("EMPLOYEE".equals(role)) {
                    empId = dao.findEmployeeByEmail(email);
                    if (empId < 0) return "No employee found with that email. Ask HR to add you first.";
                }
                return dao.registerUser(user, pass, name, email, role, empId) ? "OK" : "Registration failed.";
            }
            @Override protected void done() {
                try {
                    String res = get();
                    if ("OK".equals(res)) {
                        showRegMsg("\u2713 Account created! Redirecting\u2026", true);
                        Timer t = new Timer(2000, ev -> { if (!animating) slideToLogin(); }); t.setRepeats(false); t.start();
                    } else showRegMsg(res, false);
                } catch (Exception ex) { showRegMsg("Database error.", false); }
            }
        }.execute();
    }

    private void showRegMsg(String m, boolean ok) {
        regMsgBox.putClientProperty("bg", ok ? SUC_BG : ERR_BG); regMsgBox.putClientProperty("br", ok ? SUC_BR : ERR_BR);
        regMsgLabel.setForeground(ok ? SUC_TXT : ERR_TXT); regMsgLabel.setText(m);
        regMsgBox.setVisible(true); regMsgBox.repaint(); if (!ok) shake();
    }

    private void shake() {
        int[] t = {0}, o = {0}; int[] p = {8, -8, 6, -6, 4, -4, 2, -2, 0};
        new Timer(14, e -> { if (t[0] < p.length) { setLocation(getX() - o[0] + p[t[0]], getY()); o[0] = p[t[0]++]; } else { ((Timer) e.getSource()).stop(); setLocation(getX() - o[0], getY()); } }).start();
    }

    // ── UI Helpers ──────────────────────────────────────────────

    // ✅ FIX: use the Graphics2D returned by aa(g), not the original g
    private JPanel bgPanel() {
        return new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = aa(g);
                g2.setColor(PAGE_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    private JPanel card(int w, int h) {
        JPanel c = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);                         // ✅ always call super first
                Graphics2D g2 = aa(g);
                // soft drop-shadow layers
                for (int i = 10; i >= 1; i--) {
                    g2.setColor(new Color(90, 105, 180, 4 + (10 - i) * 2));
                    g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, 18, 18);
                }
                // ✅ white card face
                g2.setColor(FORM_BG);
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 16, 16);
                // subtle border
                g2.setColor(new Color(220, 226, 242));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 11, getHeight() - 11, 16, 16);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBounds((FORM_PW - w) / 2, (FH - h) / 2, w, h);
        return c;
    }

    private JPanel errBox() {
        return new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(ERR_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(ERR_BR); g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
    }

    private static JLabel lbl(String t, Font f, Color c, int x, int y, int w, int h) { JLabel l = new JLabel(t); l.setFont(f); l.setForeground(c); l.setBounds(x, y, w, h); return l; }
    private static JLabel fl(String t, int x, int y) { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l.setForeground(T_LABEL); l.setBounds(x, y, 400, 14); return l; }
    private static JComponent hr(int x, int y, int w) { return new JComponent() { { setBounds(x, y, w, 1); } @Override protected void paintComponent(Graphics g) { g.setColor(new Color(226, 230, 242)); g.fillRect(0, 0, getWidth(), 1); } }; }
    private static Graphics2D aa(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB); return g2; }

    // ── Custom Components ───────────────────────────────────────
    static class InputField extends JTextField {
        String hint; boolean f;
        InputField(String h) { hint = h; setOpaque(false); setFont(new Font("Segoe UI", Font.PLAIN, 13)); setForeground(T_TITLE); setCaretColor(IN_BR_F); setBorder(new EmptyBorder(0, 14, 0, 14));
            addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { f = true; repaint(); } public void focusLost(FocusEvent e) { f = false; repaint(); } }); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (f) { g2.setColor(IN_GLOW); g2.fillRoundRect(-4, -4, getWidth() + 8, getHeight() + 8, 14, 14); }
            g2.setColor(f ? Color.WHITE : IN_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(f ? IN_BR_F : IN_BR); g2.setStroke(new BasicStroke(f ? 1.8f : 1)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); g2.dispose();
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) { Graphics2D g3 = (Graphics2D) g.create(); g3.setColor(T_HINT); g3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g3.getFontMetrics(); g3.drawString(hint, 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2); g3.dispose(); }
        }
        @Override protected void paintBorder(Graphics g) {}
    }

    static class PassField extends JPasswordField {
        boolean f;
        PassField() { setOpaque(false); setFont(new Font("Segoe UI", Font.PLAIN, 13)); setForeground(T_TITLE); setCaretColor(IN_BR_F); setEchoChar('\u25CF'); setBorder(new EmptyBorder(0, 14, 0, 14));
            addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { f = true; repaint(); } public void focusLost(FocusEvent e) { f = false; repaint(); } }); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (f) { g2.setColor(IN_GLOW); g2.fillRoundRect(-4, -4, getWidth() + 8, getHeight() + 8, 14, 14); }
            g2.setColor(f ? Color.WHITE : IN_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(f ? IN_BR_F : IN_BR); g2.setStroke(new BasicStroke(f ? 1.8f : 1)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); g2.dispose();
            super.paintComponent(g);
        }
        @Override protected void paintBorder(Graphics g) {}
    }

    static class ActionBtn extends JButton {
        Color base, hov, prs, cur;
        ActionBtn(String l, Color b, Color h, Color p) { super(l); base = b; hov = h; prs = p; cur = b;
            setOpaque(false); setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14)); setForeground(Color.WHITE); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { cur = hov; repaint(); } public void mouseExited(MouseEvent e) { cur = base; repaint(); }
                public void mousePressed(MouseEvent e) { cur = prs; repaint(); } public void mouseReleased(MouseEvent e) { cur = hov; repaint(); } }); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (cur != base) { g2.setColor(new Color(cur.getRed(), cur.getGreen(), cur.getBlue(), 30)); g2.fillRoundRect(-3, -3, getWidth() + 6, getHeight() + 6, 14, 14); }
            g2.setPaint(new GradientPaint(0, 0, cur.brighter(), 0, getHeight(), cur.darker())); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(new Color(255, 255, 255, 22)); g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() / 2, 12, 12); g2.dispose(); super.paintComponent(g); }
    }
}