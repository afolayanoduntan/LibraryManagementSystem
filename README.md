LibraryManagementSystem

A Java-based Library Management System that establishes the book management, and book lending and borrowing tracking through maven.

Setup Instructions:

Clone the repository

Put together a .env file at the project root base The following is the example of the provided .envTemplate:

Insert your own database credentials within .env favourable MySQL: In the event that you have a different database, you may need to change up the DatabaseConnection class as it is currently configured to use MySQL.

DB_URL= jdbc: mysql://localhost: 3306/library management db DB_USERNAME= root DB_PASSWORD= your password.

Start the app Have your MySQL server up and running before starting the app.
