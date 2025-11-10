# LibraryManagementSystem
A Library Management System built in Java, featuring book management, and borrowing/return tracking using maven

🛠️ Setup Instructions

Clone the repository


Create a .env file in the project root
Use the provided .env.template as a guide:

Add your own database credentials inside .env preferable MySQL:
If you’re using a different database, you might need to reconfigure the DatabaseConnection class since it’s currently set up for MySQL.

DB_URL=jdbc:mysql://localhost:3306/library_management
DB_USERNAME=root
DB_PASSWORD=yourpassword


Run the project
Make sure your MySQL server is running before starting the app.
