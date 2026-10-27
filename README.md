# Human Resource Management System (HRMS)

A desktop-based **Human Resource Management System** built with Java Swing and MySQL to simplify employee administration, attendance tracking, leave management, payroll processing, performance reviews, and internal communication.

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [User Roles](#-user-roles)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Database Design](#-database-design)
- [Getting Started](#-getting-started)
- [Demo Accounts](#-demo-accounts)
- [Application Workflow](#-application-workflow)
- [Future Improvements](#-future-improvements)
- [Contributing](#-contributing)
- [License](#-license)

## 📖 Overview

The Human Resource Management System (HRMS) is a Java desktop application designed to centralize and simplify essential HR operations within an organization.

The application provides two dedicated portals:

- **HR Manager Portal:** Enables authorized HR managers to manage employees, departments, attendance, leave requests, payroll, performance reviews, and announcements.
- **Employee Portal:** Allows employees to access their personal dashboard, monitor attendance, submit leave requests, view payslips, and read company announcements.

The system uses Java Swing to provide a graphical user interface and MySQL to store and manage application data. It also includes sample data to demonstrate the application's main functionalities.

## ✨ Features

###  1. HR Manager Portal

The HR Manager Portal provides centralized access to the main HR management modules.

| Module | Description |
|---|---|
| Dashboard | Overview of HR activities and key information |
| Employee Management | Manage employee records and employment details |
| Department Management | Organize departments and maintain department information |
| Attendance Management | Track attendance records, check-in/check-out times, and attendance statuses |
| Leave Management | Review and manage employee leave requests |
| Payroll Management | Manage salary records, bonuses, deductions, taxes, and payment statuses |
| Performance Management | Record and review employee performance evaluations |
| Announcements | Publish internal announcements with different priority levels |

###  2. Employee Portal

Employees have access to a dedicated interface for their personal HR information.

**Personal Dashboard**
- Personalized greeting and employee profile.
- Attendance statistics and recent activity.
- Pending leave requests and leave balances.
- Latest salary information and yearly earnings.

**My Attendance**
- View attendance history.
- Review attendance statuses.
- Monitor working hours and attendance statistics.

**My Leaves**
- View leave balances by leave type.
- Review previous leave requests and their statuses.
- Submit new leave requests.
- Calculate the requested leave duration automatically.

**My Payslips**
- View salary history.
- Access detailed payslip breakdowns.
- Review salary components, deductions, and net salary information.

**Announcements**
- Read company announcements.
- Identify important or urgent updates through priority indicators.

###  3. User Interface

- Java Swing graphical interface.
- Nimbus Look and Feel.
- Consistent styling through a shared UI helper.
- Dashboard cards, tables, progress indicators, and attendance charts.
- Dialogs for viewing detailed information and submitting requests.

##  User Roles

The application supports two main user roles.

| Role | Access |
|---|---|
| HR Manager | Administrative HR modules and management operations |
| Employee | Personal dashboard, attendance, leave requests, payslips, and announcements |

Access is determined by the authenticated user's role, providing separate workflows for HR managers and employees.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core application logic |
| Java Swing | Desktop graphical user interface |
| MySQL | Relational database management |
| JDBC | Communication between Java and MySQL |
| MySQL Connector/J | JDBC driver for MySQL |
| Eclipse IDE | Development environment |
| Git & GitHub | Version control and project hosting |

## 📂 Project Structure

```text
HRM_System_Final/
│
├── src/
│   ├── Main.java
│   │
│   ├── app/
│   │   ├── HRMApp.java
│   │   ├── LoginScreen.java
│   │   ├── UIHelper.java
│   │   │
│   │   ├── MainScreen.java
│   │   ├── DashboardScreen.java
│   │   ├── EmployeeScreen.java
│   │   ├── DepartmentScreen.java
│   │   ├── AttendanceScreen.java
│   │   ├── LeaveScreen.java
│   │   ├── PayrollScreen.java
│   │   ├── PerformanceScreen.java
│   │   ├── AnnouncementScreen.java
│   │   │
│   │   ├── EmpMainScreen.java
│   │   ├── EmpDashboard.java
│   │   ├── EmpAttendancePanel.java
│   │   ├── EmpLeavePanel.java
│   │   ├── EmpPayslipPanel.java
│   │   └── EmpAnnouncementPanel.java
│   │
│   └── db/
│       ├── DatabaseConnection.java
│       └── HRMdao.java
│
├── sql/
│   └── hrm_database.sql
│
├── lib/
│   └── mysql-connector-java.jar
│
├── README.md
└── .gitignore
```

## 🗄️ Database Design

The application uses a MySQL database named `hrm_system`.

The database schema includes the following tables:

| Table | Purpose |
|---|---|
| `employees` | Employee profiles and employment information |
| `departments` | Department details and budgets |
| `positions` | Job titles and salary ranges |
| `users` | Application accounts and user roles |
| `attendance` | Daily attendance records |
| `leave_types` | Available leave categories and limits |
| `leave_requests` | Employee leave requests and approval statuses |
| `payroll` | Salary records and payment information |
| `performance_reviews` | Employee evaluations and performance ratings |
| `announcements` | Internal company announcements |

### Database Highlights

- Relational structure with primary and foreign keys.
- Constraints to maintain data consistency.
- Automatic timestamps for relevant records.
- Sample data for demonstrating application functionality.
- Date-relative sample records using MySQL date functions.

**Important:** The provided SQL script starts by dropping and recreating the `hrm_system` database. Back up any existing data before executing it.

## 🚀 Getting Started

Follow these steps to run the application locally.

### Prerequisites

Make sure you have installed:

- Java Development Kit (JDK).
- MySQL Server.
- Eclipse IDE or another Java IDE.
- MySQL Connector/J JDBC driver.

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
```

Navigate to the project directory:

```bash
cd HRM_System_Final
```

Replace the repository URL with your actual GitHub repository URL.

### 2. Create the Database

Start your MySQL server, open a terminal in the project directory, and execute:

```bash
mysql -u root -p < sql/hrm_database.sql
```

Enter your MySQL password when prompted.

Alternatively, open `sql/hrm_database.sql` in MySQL Workbench and execute the script.

The script creates the database, tables, and demonstration data.

### 3. Configure the Database Connection

Open:

```text
src/db/DatabaseConnection.java
```

Update the database connection settings to match your local MySQL configuration.

```java
private static final String URL =
    "jdbc:mysql://localhost:3306/hrm_system?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";

private static final String USER = "root";
private static final String PASS = "YOUR_MYSQL_PASSWORD";
```

Replace `YOUR_MYSQL_PASSWORD` with your actual local MySQL password.

**Security recommendation:** Do not commit real database passwords to GitHub. For a public repository, use environment variables or an external configuration file excluded from version control.

### 4. Add MySQL Connector/J

Download MySQL Connector/J from the official MySQL website:

https://dev.mysql.com/downloads/connector/j/

Place the downloaded JAR file in the project's `lib/` directory.

In Eclipse:

1. Right-click the project.
2. Select **Build Path → Configure Build Path**.
3. Open the **Libraries** tab.
4. Choose **Add JARs** or **Add External JARs**.
5. Select the MySQL Connector/J JAR.
6. Apply the changes.

If your project contains an old absolute path to a JAR file on another computer, remove that path and configure the driver using your local JAR.

### 5. Import the Project into Eclipse

1. Open Eclipse IDE.
2. Select **File → Import**.
3. Choose **General → Existing Projects into Workspace**.
4. Select the cloned project directory.
5. Finish the import.
6. Verify that the JDK and MySQL Connector/J are configured correctly.

### 6. Run the Application

Locate:

```text
src/Main.java
```

Right-click `Main.java` and select:

**Run As → Java Application**

The login screen should open. Sign in using one of the available demonstration accounts below.

## 🔐 Demo Accounts

The following accounts are defined in the project's current README for demonstration purposes.

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | HR Manager |
| `youssef` | `emp123` | Employee |
| `amina` | `emp123` | Employee |

These are demonstration credentials, not production-ready accounts. Their availability depends on the database initialization and authentication implementation.

Do not use these credentials for a real deployment.

## 🔄 Application Workflow

The main application workflow is:

```text
              Start Application
                      |
                      v
                 Login Screen
                      |
                      v
              Authenticate User
                      |
              +-------+-------+
              |               |
              v               v
         HR Manager         Employee
              |               |
              v               v
      HR Manager Portal   Employee Portal
              |               |
      +-------+-------+       |
      |       |       |       |
      v       v       v       v
   Manage   Manage   Payroll  Personal
 Employees Attendance & HR   Dashboard
      |       |       |       |
      +-------+-------+       |
              |               |
              v               v
          MySQL Database <-----+
```

## 🧪 Testing

To verify the application locally:

- [ ] Confirm that the MySQL server is running.
- [ ] Confirm that the `hrm_system` database exists.
- [ ] Verify the database connection configuration.
- [ ] Confirm that MySQL Connector/J is available on the classpath.
- [ ] Test login using the demonstration accounts.
- [ ] Verify that the HR Manager and Employee portals open correctly.
- [ ] Review attendance, leave, payroll, and announcement data.
- [ ] Test submitting a leave request and reviewing its status.

## 🔮 Future Improvements

Potential improvements for future versions include:

- Secure password hashing and improved authentication.
- Role-based authorization checks across all data operations.
- Externalized database configuration and secret management.
- Automated unit and integration tests.
- Enhanced reporting and data visualization.
- PDF payslip generation and export.
- Email notifications for leave approvals and announcements.
- Search, filtering, and pagination for large datasets.
- Maven-based dependency management and simplified setup.
- Deployment-ready configuration and database migration scripts.

## 🤝 Contributing

Contributions and suggestions are welcome.

1. Fork the repository.
2. Create a feature branch:

   ```bash
   git checkout -b feature/your-feature
   ```

3. Commit your changes:

   ```bash
   git commit -m "Add your feature"
   ```

4. Push the branch:

   ```bash
   git push origin feature/your-feature
   ```

5. Open a Pull Request describing your changes.

## 📄 License

No license has been specified yet. If you plan to make this project publicly reusable, consider adding an appropriate license file, such as the MIT License, after deciding on the terms under which others may use your code.

---

**Developed as an academic project for Human-Computer Interaction (HCI), Spring 2025–2026.**

*Built with Java Swing, JDBC, and MySQL.*
