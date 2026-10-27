package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object — all database queries in one place.
 * Returns raw Maps / Lists so UI can consume directly without model classes.
 */
public class HRMdao {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // ── Authentication ──────────────────────────────────────────
    public Map<String, String> authenticate(String username, String password) {
        String sql = "SELECT id, full_name, email, role, employee_id FROM users WHERE username=? AND password=? AND is_active=1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("id", rs.getString("id"));
                user.put("full_name", rs.getString("full_name"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                user.put("employee_id", rs.getString("employee_id"));
                try (PreparedStatement up = conn().prepareStatement("UPDATE users SET last_login=NOW() WHERE id=?")) {
                    up.setInt(1, rs.getInt("id"));
                    up.executeUpdate();
                }
                return user;
            } else {
                // Debug: Check if user exists (even with wrong password)
                String debugSql = "SELECT id, username FROM users WHERE username=?";
                try (PreparedStatement debugPs = conn().prepareStatement(debugSql)) {
                    debugPs.setString(1, username);
                    ResultSet debugRs = debugPs.executeQuery();
                    if (debugRs.next()) {
                        System.out.println("[AUTH DEBUG] User '" + username + "' exists but password doesn't match.");
                    } else {
                        System.out.println("[AUTH DEBUG] User '" + username + "' not found in database.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[AUTH ERROR] Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ── Registration ────────────────────────────────────────────
    public boolean isUsernameTaken(String username) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM users WHERE username=?")) {
            ps.setString(1, username); ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); } return true;
    }

    public boolean isEmailTaken(String email) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM users WHERE email=?")) {
            ps.setString(1, email); ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); } return true;
    }

    /** Find employee by email — returns employee id or -1 */
    public int findEmployeeByEmail(String email) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT id FROM employees WHERE email=?")) {
            ps.setString(1, email); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean registerUser(String username, String password, String fullName, String email, String role, int employeeId) {
        String sql = "INSERT INTO users (username, password, full_name, email, role, employee_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username); ps.setString(2, password);
            ps.setString(3, fullName); ps.setString(4, email);
            ps.setString(5, role);
            if (employeeId > 0) ps.setInt(6, employeeId); else ps.setNull(6, java.sql.Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Employee Self-Service Queries ────────────────────────────
    public Map<String, String> getEmployeeProfile(int empId) {
        String sql = "SELECT e.*, d.name as dept_name, p.title as position_title FROM employees e " +
                "LEFT JOIN departments d ON e.department_id=d.id LEFT JOIN positions p ON e.position_id=p.id WHERE e.id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); ResultSet rs = ps.executeQuery();
            List<Map<String, String>> list = resultSetToMaps(rs);
            return list.isEmpty() ? null : list.get(0);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Map<String, String>> getMyAttendance(int empId) {
        String sql = "SELECT * FROM attendance WHERE employee_id=? ORDER BY date DESC LIMIT 30";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public List<Map<String, String>> getMyLeaves(int empId) {
        String sql = "SELECT lr.*, lt.name as leave_type FROM leave_requests lr " +
                "JOIN leave_types lt ON lr.leave_type_id=lt.id WHERE lr.employee_id=? ORDER BY lr.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public List<Map<String, String>> getMyPayslips(int empId) {
        String sql = "SELECT * FROM payroll WHERE employee_id=? ORDER BY period_year DESC, period_month DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public int countMyPendingLeaves(int empId) {
        return countQuery("SELECT COUNT(*) FROM leave_requests WHERE employee_id=" + empId + " AND status='Pending'");
    }

    public int countMyAttendanceThisMonth(int empId) {
        return countQuery("SELECT COUNT(*) FROM attendance WHERE employee_id=" + empId +
                " AND MONTH(date)=MONTH(CURDATE()) AND YEAR(date)=YEAR(CURDATE()) AND status='Present'");
    }

    /** Get attendance summary stats for an employee */
    public Map<String, Integer> getMyAttendanceSummary(int empId) {
        Map<String, Integer> m = new HashMap<>();
        m.put("Present", 0); m.put("Late", 0); m.put("Absent", 0); m.put("On Leave", 0);
        String sql = "SELECT status, COUNT(*) c FROM attendance WHERE employee_id=? AND date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY status";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); ResultSet rs = ps.executeQuery();
            while (rs.next()) m.put(rs.getString("status"), rs.getInt("c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return m;
    }

    /** Get leave balance: type → days taken this year */
    public List<Map<String, String>> getMyLeaveBalance(int empId) {
        String sql = "SELECT lt.id, lt.name, lt.max_days, lt.color, " +
                "COALESCE(SUM(CASE WHEN lr.status='Approved' AND YEAR(lr.start_date)=YEAR(CURDATE()) THEN lr.total_days ELSE 0 END), 0) as days_taken " +
                "FROM leave_types lt LEFT JOIN leave_requests lr ON lr.leave_type_id=lt.id AND lr.employee_id=? " +
                "GROUP BY lt.id, lt.name, lt.max_days, lt.color ORDER BY lt.id";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    /** Get total earnings this year for an employee */
    public double getMyTotalEarningsThisYear(int empId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COALESCE(SUM(net_salary),0) FROM payroll WHERE employee_id=? AND period_year=YEAR(CURDATE()) AND payment_status='Paid'")) {
            ps.setInt(1, empId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Get my latest 3 leave requests for dashboard activity feed */
    public List<Map<String, String>> getMyRecentLeaves(int empId, int limit) {
        String sql = "SELECT lr.*, lt.name as leave_type FROM leave_requests lr " +
                "JOIN leave_types lt ON lr.leave_type_id=lt.id WHERE lr.employee_id=? ORDER BY lr.created_at DESC LIMIT ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); ps.setInt(2, limit);
            return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    // ── Dashboard Stats ─────────────────────────────────────────
    public int countEmployees() { return countQuery("SELECT COUNT(*) FROM employees WHERE status='Active'"); }
    public int countDepartments() { return countQuery("SELECT COUNT(*) FROM departments"); }
    public int countPendingLeaves() { return countQuery("SELECT COUNT(*) FROM leave_requests WHERE status='Pending'"); }
    public double totalPayroll() {
        String sql = "SELECT COALESCE(SUM(net_salary),0) FROM payroll WHERE period_month=MONTH(CURDATE()) AND period_year=YEAR(CURDATE()) AND payment_status='Paid'";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    public double totalPayrollLastMonth() {
        String sql = "SELECT COALESCE(SUM(net_salary),0) FROM payroll WHERE " +
                "((period_month=MONTH(CURDATE())-1 AND period_year=YEAR(CURDATE())) OR " +
                "(MONTH(CURDATE())=1 AND period_month=12 AND period_year=YEAR(CURDATE())-1)) AND payment_status='Paid'";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Department distribution for charts
    public List<Object[]> departmentDistribution() {
        String sql = "SELECT d.name, COUNT(e.id) FROM departments d LEFT JOIN employees e ON e.department_id=d.id AND e.status='Active' GROUP BY d.id, d.name ORDER BY COUNT(e.id) DESC";
        return queryList(sql, new String[]{"name","count"});
    }

    // Attendance summary for today
    public Map<String, Integer> todayAttendanceSummary() {
        Map<String, Integer> m = new HashMap<>();
        m.put("Present", 0); m.put("Late", 0); m.put("Absent", 0); m.put("On Leave", 0);
        String sql = "SELECT status, COUNT(*) c FROM attendance WHERE date=CURDATE() GROUP BY status";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) m.put(rs.getString("status"), rs.getInt("c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return m;
    }

    // Recent hires (last 5)
    public List<Map<String, String>> recentHires() {
        String sql = "SELECT e.first_name, e.last_name, d.name as dept, e.hire_date FROM employees e " +
                "LEFT JOIN departments d ON e.department_id=d.id ORDER BY e.hire_date DESC LIMIT 5";
        return queryMaps(sql);
    }

    // Active announcements
    public List<Map<String, String>> activeAnnouncements() {
        String sql = "SELECT title, content, priority, DATE_FORMAT(created_at,'%b %d') as date_str FROM announcements " +
                "WHERE is_active=1 AND (expires_at IS NULL OR expires_at>=CURDATE()) ORDER BY FIELD(priority,'Urgent','Important','Normal','Low'), created_at DESC LIMIT 5";
        return queryMaps(sql);
    }

    // ── Employees ───────────────────────────────────────────────
    public List<Map<String, String>> getAllEmployees() {
        String sql = "SELECT e.*, d.name as dept_name, p.title as position_title, " +
                "CONCAT(m.first_name,' ',m.last_name) as manager_name " +
                "FROM employees e LEFT JOIN departments d ON e.department_id=d.id " +
                "LEFT JOIN positions p ON e.position_id=p.id " +
                "LEFT JOIN employees m ON e.manager_id=m.id ORDER BY e.first_name";
        return queryMaps(sql);
    }

    public List<Map<String, String>> searchEmployees(String keyword) {
        String sql = "SELECT e.*, d.name as dept_name, p.title as position_title, " +
                "CONCAT(m.first_name,' ',m.last_name) as manager_name " +
                "FROM employees e LEFT JOIN departments d ON e.department_id=d.id " +
                "LEFT JOIN positions p ON e.position_id=p.id " +
                "LEFT JOIN employees m ON e.manager_id=m.id " +
                "WHERE e.first_name LIKE ? OR e.last_name LIKE ? OR e.email LIKE ? OR d.name LIKE ? ORDER BY e.first_name";
        String kw = "%" + keyword + "%";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw); ps.setString(4, kw);
            return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public boolean addEmployee(String firstName, String lastName, String email, String phone,
                               String dob, String gender, String nationalId, String address, String city,
                               int deptId, int posId, String hireDate, String contractType, double salary) {
        String sql = "INSERT INTO employees (first_name,last_name,email,phone,date_of_birth,gender,national_id,address,city,department_id,position_id,hire_date,contract_type,base_salary) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, firstName); ps.setString(2, lastName); ps.setString(3, email);
            ps.setString(4, phone); ps.setString(5, dob); ps.setString(6, gender);
            ps.setString(7, nationalId); ps.setString(8, address); ps.setString(9, city);
            ps.setInt(10, deptId); ps.setInt(11, posId); ps.setString(12, hireDate);
            ps.setString(13, contractType); ps.setDouble(14, salary);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateEmployee(int id, String firstName, String lastName, String email, String phone,
                                  String dob, String gender, String nationalId, String address, String city,
                                  int deptId, int posId, String contractType, double salary, String status) {
        String sql = "UPDATE employees SET first_name=?,last_name=?,email=?,phone=?,date_of_birth=?,gender=?,national_id=?,address=?,city=?,department_id=?,position_id=?,contract_type=?,base_salary=?,status=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, firstName); ps.setString(2, lastName); ps.setString(3, email);
            ps.setString(4, phone); ps.setString(5, dob); ps.setString(6, gender);
            ps.setString(7, nationalId); ps.setString(8, address); ps.setString(9, city);
            ps.setInt(10, deptId); ps.setInt(11, posId); ps.setString(12, contractType);
            ps.setDouble(13, salary); ps.setString(14, status); ps.setInt(15, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteEmployee(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM employees WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Departments ─────────────────────────────────────────────
    public List<Map<String, String>> getAllDepartments() {
        String sql = "SELECT d.*, (SELECT COUNT(*) FROM employees e WHERE e.department_id=d.id AND e.status='Active') as headcount FROM departments d ORDER BY d.name";
        return queryMaps(sql);
    }

    public boolean addDepartment(String name, String desc, String location, double budget) {
        try (PreparedStatement ps = conn().prepareStatement("INSERT INTO departments (name,description,location,budget) VALUES (?,?,?,?)")) {
            ps.setString(1, name); ps.setString(2, desc); ps.setString(3, location); ps.setDouble(4, budget);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateDepartment(int id, String name, String desc, String location, double budget) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE departments SET name=?,description=?,location=?,budget=? WHERE id=?")) {
            ps.setString(1, name); ps.setString(2, desc); ps.setString(3, location); ps.setDouble(4, budget); ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteDepartment(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM departments WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Positions (for dropdowns) ───────────────────────────────
    public List<Map<String, String>> getAllPositions() {
        return queryMaps("SELECT p.*, d.name as dept_name FROM positions p LEFT JOIN departments d ON p.department_id=d.id ORDER BY p.title");
    }

    public List<Map<String, String>> getPositionsByDept(int deptId) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM positions WHERE department_id=? ORDER BY title")) {
            ps.setInt(1, deptId);
            return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    // ── Attendance ──────────────────────────────────────────────
    public List<Map<String, String>> getAttendanceByDate(String date) {
        String sql = "SELECT a.*, CONCAT(e.first_name,' ',e.last_name) as emp_name, d.name as dept_name " +
                "FROM attendance a JOIN employees e ON a.employee_id=e.id LEFT JOIN departments d ON e.department_id=d.id " +
                "WHERE a.date=? ORDER BY e.first_name";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, date);
            return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public boolean recordAttendance(int empId, String date, String checkIn, String checkOut, String status, String notes) {
        String sql = "INSERT INTO attendance (employee_id,date,check_in,check_out,status,notes) VALUES (?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE check_in=VALUES(check_in),check_out=VALUES(check_out),status=VALUES(status),notes=VALUES(notes)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); ps.setString(2, date);
            ps.setString(3, checkIn.isEmpty() ? null : checkIn);
            ps.setString(4, checkOut.isEmpty() ? null : checkOut);
            ps.setString(5, status); ps.setString(6, notes);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Leave Requests ──────────────────────────────────────────
    public List<Map<String, String>> getAllLeaveRequests() {
        String sql = "SELECT lr.*, CONCAT(e.first_name,' ',e.last_name) as emp_name, lt.name as leave_type, lt.color as type_color " +
                "FROM leave_requests lr JOIN employees e ON lr.employee_id=e.id JOIN leave_types lt ON lr.leave_type_id=lt.id ORDER BY lr.created_at DESC";
        return queryMaps(sql);
    }

    public List<Map<String, String>> getLeaveTypes() {
        return queryMaps("SELECT * FROM leave_types ORDER BY name");
    }

    public boolean addLeaveRequest(int empId, int typeId, String startDate, String endDate, int days, String reason) {
        String sql = "INSERT INTO leave_requests (employee_id,leave_type_id,start_date,end_date,total_days,reason) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); ps.setInt(2, typeId); ps.setString(3, startDate);
            ps.setString(4, endDate); ps.setInt(5, days); ps.setString(6, reason);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateLeaveStatus(int id, String status, int reviewerId, String notes) {
        String sql = "UPDATE leave_requests SET status=?, reviewed_by=?, review_date=NOW(), review_notes=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, reviewerId); ps.setString(3, notes); ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Payroll ─────────────────────────────────────────────────
    public List<Map<String, String>> getPayrollByPeriod(int month, int year) {
        String sql = "SELECT p.*, CONCAT(e.first_name,' ',e.last_name) as emp_name, d.name as dept_name " +
                "FROM payroll p JOIN employees e ON p.employee_id=e.id LEFT JOIN departments d ON e.department_id=d.id " +
                "WHERE p.period_month=? AND p.period_year=? ORDER BY e.first_name";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, month); ps.setInt(2, year);
            return resultSetToMaps(ps.executeQuery());
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public boolean generatePayroll(int empId, int month, int year, double baseSalary,
                                   double otHours, double otPay, double bonuses, double deductions,
                                   double tax, double cnss, double netSalary) {
        String sql = "INSERT INTO payroll (employee_id,period_month,period_year,base_salary,overtime_hours,overtime_pay,bonuses,deductions,tax_amount,cnss,net_salary) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE base_salary=VALUES(base_salary),overtime_hours=VALUES(overtime_hours)," +
                "overtime_pay=VALUES(overtime_pay),bonuses=VALUES(bonuses),deductions=VALUES(deductions),tax_amount=VALUES(tax_amount),cnss=VALUES(cnss),net_salary=VALUES(net_salary)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); ps.setInt(2, month); ps.setInt(3, year);
            ps.setDouble(4, baseSalary); ps.setDouble(5, otHours); ps.setDouble(6, otPay);
            ps.setDouble(7, bonuses); ps.setDouble(8, deductions); ps.setDouble(9, tax);
            ps.setDouble(10, cnss); ps.setDouble(11, netSalary);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean markPayrollPaid(int id) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE payroll SET payment_status='Paid', payment_date=CURDATE() WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Performance Reviews ─────────────────────────────────────
    public List<Map<String, String>> getAllReviews() {
        String sql = "SELECT pr.*, CONCAT(e.first_name,' ',e.last_name) as emp_name, d.name as dept_name " +
                "FROM performance_reviews pr JOIN employees e ON pr.employee_id=e.id LEFT JOIN departments d ON e.department_id=d.id ORDER BY pr.review_date DESC";
        return queryMaps(sql);
    }

    public boolean addReview(int empId, String reviewDate, String period, int prod, int qual, int comm, int team, int init, double overall, String strengths, String improvements, String goals) {
        String sql = "INSERT INTO performance_reviews (employee_id,review_date,period,productivity,quality,communication,teamwork,initiative,overall_rating,strengths,improvements,goals) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, empId); ps.setString(2, reviewDate); ps.setString(3, period);
            ps.setInt(4, prod); ps.setInt(5, qual); ps.setInt(6, comm); ps.setInt(7, team);
            ps.setInt(8, init); ps.setDouble(9, overall); ps.setString(10, strengths);
            ps.setString(11, improvements); ps.setString(12, goals);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Announcements ───────────────────────────────────────────
    public List<Map<String, String>> getAllAnnouncements() {
        return queryMaps("SELECT * FROM announcements ORDER BY FIELD(priority,'Urgent','Important','Normal','Low'), created_at DESC");
    }

    public boolean addAnnouncement(String title, String content, String priority, String expiresAt) {
        String sql = "INSERT INTO announcements (title,content,priority,expires_at) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, title); ps.setString(2, content); ps.setString(3, priority);
            ps.setString(4, expiresAt.isEmpty() ? null : expiresAt);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteAnnouncement(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM announcements WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Utility Methods ─────────────────────────────────────────
    private int countQuery(String sql) {
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private List<Map<String, String>> queryMaps(String sql) {
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return resultSetToMaps(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    private List<Map<String, String>> resultSetToMaps(ResultSet rs) throws SQLException {
        List<Map<String, String>> list = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        while (rs.next()) {
            Map<String, String> row = new HashMap<>();
            for (int i = 1; i <= cols; i++) {
                row.put(meta.getColumnLabel(i), rs.getString(i));
            }
            list.add(row);
        }
        return list;
    }

    private List<Object[]> queryList(String sql, String[] labels) {
        List<Object[]> list = new ArrayList<>();
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = new Object[labels.length];
                for (int i = 0; i < labels.length; i++) row[i] = rs.getObject(i + 1);
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
