package LibMgmtSys;

import BookPk.*;
import DatabasePk.*;
import ExceptionPk.*;
import GenrePk.Genre;
import UserPk.User;

import java.util.Scanner;
public class LibraryManagementSystem {
    private static String currentUserId = null;

    public static void main(String[] args) {

        // Test database connection at startup
        try {
            DatabaseConnection.testConnection();
        } catch (Exception e) {
            System.out.println("\n Database connection failed!");
            System.out.println("Please check your .env file configuration.");
            System.out.println("Make sure you have a .env file with:");
            System.out.println("DB_URL=jdbc:mysql://localhost:3306/library_management");
            System.out.println("DB_USERNAME=your_username");
            System.out.println("DB_PASSWORD=your_password");
            System.out.println("\nYou can copy .env.template to .env and edit it.");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        Library library = new Library();

        System.out.println("=== LIBRARY MANAGEMENT SYSTEM ===");

        // User.User login/setup
        while (currentUserId == null) {
            System.out.print("Enter your 8-digit User ID: ");
            String userId = scanner.nextLine();

            if (userId.length() == 8 && userId.matches("\\d+")) {
                if (library.userExists(userId)) {
                    System.out.println("Welcome back, user " + userId + "!");
                    currentUserId = userId;
                } else {
                    System.out.print("New user detected. Enter your username: ");
                    String username = scanner.nextLine();
                    library.addUser(new User(userId, username));
                    currentUserId = userId;
                    System.out.println("User account created successfully!");
                }
            } else {
                System.out.println("Invalid User ID. Must be exactly 8 digits.");
            }
        }

        while (true) {
            System.out.println("\n=== LIBRARY MANAGEMENT SYSTEM ===");
            System.out.println("Current User: " + currentUserId);
            System.out.println("1. Add Physical Book");
            System.out.println("2. Add E-Book");
            System.out.println("3. Display Books");
            System.out.println("4. Borrow a Book");
            System.out.println("5. Return a Book");
            System.out.println("6. Remove a Book");
            System.out.println("7. View Activity Log");
            System.out.println("8. View Overdue Books");
            System.out.println("9. View Borrowing History");
            System.out.println("10. Switch User");
            System.out.println("11. Exit");
            System.out.print("Enter choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        System.out.print("Enter book title: ");
                        String title = scanner.nextLine();
                        System.out.print("Enter author name: ");
                        String author = scanner.nextLine();
                        System.out.print("Enter genre: ");
                        Genre genre = Genre.validateGenre(scanner.nextLine());
                        library.addBook(new PhysicalBook(title, author, genre, currentUserId));
                        break;
                    case 2:
                        System.out.print("Enter book title: ");
                        String eTitle = scanner.nextLine();
                        System.out.print("Enter author name: ");
                        String eAuthor = scanner.nextLine();
                        System.out.print("Enter genre: ");
                        Genre eGenre = Genre.validateGenre(scanner.nextLine());
                        library.addBook(new EBook(eTitle, eAuthor, eGenre, currentUserId));
                        break;
                    case 3:
                        library.displayBooks();
                        break;
                    case 4:
                        System.out.print("Enter Book ID to borrow: ");
                        library.borrowBook(Integer.parseInt(scanner.nextLine()), currentUserId);
                        break;
                    case 5:
                        System.out.print("Enter Book ID to return: ");
                        library.returnBook(Integer.parseInt(scanner.nextLine()), currentUserId);
                        break;
                    case 6:
                        System.out.print("Enter Book ID to remove: ");
                        library.removeBook(Integer.parseInt(scanner.nextLine()), currentUserId);
                        break;
                    case 7:
                        library.viewActivityLog();
                        break;
                    case 8:
                        library.displayOverdueBooks();
                        break;
                    case 9:
                        library.displayBorrowingHistory();
                        break;
                    case 10:
                        currentUserId = null;
                        while (currentUserId == null) {
                            System.out.print("Enter your 8-digit User ID: ");
                            String newUserId = scanner.nextLine();
                            if (newUserId.length() == 8 && newUserId.matches("\\d+") && library.userExists(newUserId)) {
                                currentUserId = newUserId;
                                System.out.println("Switched to user: " + currentUserId);
                            } else {
                                System.out.println("Invalid or non-existent User ID.");
                            }
                        }
                        break;
                    case 11:
                        System.out.println("Exiting Library System.");
                        DatabaseConnection.closeConnection();
                        scanner.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice! Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            } catch (InvalidGenreException | BookNotFoundException | UserNotFoundException | UnauthorizedActionException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
