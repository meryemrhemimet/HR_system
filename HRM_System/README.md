# HRM System — Final

**HCI Final Project | Spring 2025-2026 | Pr. M_AJANA**

## Test Accounts

| Username | Password | Role | Dashboard |
|----------|----------|------|-----------|
| `admin`  | `admin123` | HR Manager | Full management portal |
| `youssef` | `emp123` | Employee | Personal employee portal (rich data) |
| `amina`  | `emp123` | Employee | Personal employee portal (rich data) |

## What's New

The employee side has been fully polished with rich sample data:

- **Smart greeting** ("Good morning/afternoon/evening, Youssef 👋")
- **4 stat cards**: Days Present, Pending Leaves, Last Salary, Year Earnings
- **Profile card** with avatar, role pill, and 8 key fields
- **Attendance donut chart** showing last 30 days breakdown with percentage
- **Leave balance bars** with progress indicators per leave type
- **Recent activity feed** showing your latest leave requests
- **Announcements** with priority accent stripes

### Employee Panels
- **My Attendance** — Stats cards + 30-day history with hours worked computed
- **My Leaves** — 4 leave-balance summary cards + history + Request dialog with auto-day calculation
- **My Payslips** — Summary stats + Click "View →" to open full payslip breakdown dialog
- **Announcements** — Card view with priority stripe

## Database

The SQL uses **`CURDATE()` + `INTERVAL`** so all dates stay relative to the day you run it. Includes:
- 25+ days of attendance for Youssef and Amina
- 3 months of payroll history each
- 7 leave requests with mixed statuses (Pending/Approved/Rejected)
- 7 active announcements (Urgent/Important/Normal/Low)

## Setup

1. Run `sql/hrm_database.sql` in MySQL
2. Place `mysql-connector-java.jar` in `lib/`
3. Right-click `Main.java` → Run As → Java Application

```sql
mysql -u root -p < sql/hrm_database.sql
```

## Project Structure

```
src/
├── Main.java
├── app/
│   ├── UIHelper.java          (unified design system)
│   ├── LoginScreen.java       (sliding login + employee register)
│   ├── MainScreen.java        ┐
│   ├── DashboardScreen.java   │
│   ├── EmployeeScreen.java    │
│   ├── DepartmentScreen.java  │ HR Manager Portal
│   ├── AttendanceScreen.java  │ (8 modules)
│   ├── LeaveScreen.java       │
│   ├── PayrollScreen.java     │
│   ├── PerformanceScreen.java │
│   ├── AnnouncementScreen.java┘
│   ├── EmpMainScreen.java     ┐
│   ├── EmpDashboard.java      │ Employee Portal
│   ├── EmpAttendancePanel.java│ (5 modules)
│   ├── EmpLeavePanel.java     │
│   ├── EmpPayslipPanel.java   │
│   └── EmpAnnouncementPanel.java┘
└── db/
    ├── DatabaseConnection.java
    └── HRMdao.java
```
