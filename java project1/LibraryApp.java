import java.sql.*;
import java.util.*;

// Utility class for DB connection
class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/librarydb";
    private static final String USER = "root";
    private static final String PASSWORD = "ANUJshukla@12345"; // Replace with your DB password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

// User Authentication class
class UserAuth {
    public static boolean register(String username, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration failed. Username may already exist.");
            return false;
        }
    }

    public static boolean login(String username, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

// Base class for Book
class Book {
    protected String title;
    protected String author;
    protected String isbn;
    protected boolean isBorrowed;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isBorrowed = false;
    }

    public boolean borrow() {
        if (!isBorrowed) {
            isBorrowed = true;
            return true;
        }
        return false;
    }

    public boolean returnBook() {
        if (isBorrowed) {
            isBorrowed = false;
            return true;
        }
        return false;
    }

    public String toString() {
        String status = isBorrowed ? "Borrowed" : "Available";
        return title + " by " + author + " (ISBN: " + isbn + ") - " + status;
    }
}

// Textbook subclass
class Textbook extends Book {
    public String subject;

    public Textbook(String title, String author, String isbn, String subject) {
        super(title, author, isbn);
        this.subject = subject;
    }

    public String toString() {
        return super.toString() + " [Textbook: " + subject + "]";
    }
}

// Novel subclass
class Novel extends Book {
    public String genre;

    public Novel(String title, String author, String isbn, String genre) {
        super(title, author, isbn);
        this.genre = genre;
    }

    public String toString() {
        return super.toString() + " [Novel: " + genre + "]";
    }
}

// Library class
class Library {
    private List<Book> books;

    public Library() {
        books = new ArrayList<>();
    }

    public void addBook(Book book) {
        books.add(book);

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO books (isbn, title, author, is_borrowed, type, subject_or_genre) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.isbn);
            stmt.setString(2, book.title);
            stmt.setString(3, book.author);
            stmt.setBoolean(4, book.isBorrowed);

            if (book instanceof Textbook) {
                stmt.setString(5, "Textbook");
                stmt.setString(6, ((Textbook) book).subject);
            } else if (book instanceof Novel) {
                stmt.setString(5, "Novel");
                stmt.setString(6, ((Novel) book).genre);
            } else {
                stmt.setString(5, "Unknown");
                stmt.setString(6, "");
            }

            stmt.executeUpdate();
            System.out.println("Book added to database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.title.toLowerCase().contains(keyword.toLowerCase()) ||
                book.author.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    public boolean borrowBook(String isbn) {
        for (Book book : books) {
            if (book.isbn.equals(isbn)) {
                if (book.borrow()) {
                    updateBorrowStatusInDB(isbn, true);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean returnBook(String isbn) {
        for (Book book : books) {
            if (book.isbn.equals(isbn)) {
                if (book.returnBook()) {
                    updateBorrowStatusInDB(isbn, false);
                    return true;
                }
            }
        }
        return false;
    }

    private void updateBorrowStatusInDB(String isbn, boolean status) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE books SET is_borrowed = ? WHERE isbn = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, status);
            stmt.setString(2, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayBooks() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM books";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                String isbn = rs.getString("isbn");
                boolean isBorrowed = rs.getBoolean("is_borrowed");
                String type = rs.getString("type");
                String detail = rs.getString("subject_or_genre");

                Book book;
                if ("Textbook".equalsIgnoreCase(type)) {
                    book = new Textbook(title, author, isbn, detail);
                } else {
                    book = new Novel(title, author, isbn, detail);
                }
                if (isBorrowed) book.borrow();
                System.out.println(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// Main class
public class LibraryApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Library library = new Library();

        System.out.println("Welcome to the Library Management System!");

        boolean authenticated = false;
        while (!authenticated) {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (option == 1) {
                if (UserAuth.login(username, password)) {
                    System.out.println("Login successful!");
                    authenticated = true;
                } else {
                    System.out.println("Invalid credentials. Try again.");
                }
            } else if (option == 2) {
                if (UserAuth.register(username, password)) {
                    System.out.println("Registration successful. Please login.");
                } else {
                    System.out.println("Registration failed.");
                }
            } else {
                System.out.println("Invalid choice.");
            }
        }

        while (true) {
            System.out.println("=====================================");
            System.out.println("    Library Management System");
            System.out.println("=====================================");
            System.out.println("\n------------------------------------------");
            System.out.println("\nMain Menu:");
            System.out.println("\n------------------------------------------");
            System.out.println("1. Add Textbook");
            System.out.println("2. Add Novel");
            System.out.println("3. Search Books");
            System.out.println("4. Borrow Book");
            System.out.println("5. Return Book");
            System.out.println("6. Display All Books");
            System.out.println("7. Exit");
            System.out.print("Choose an option (1-7): ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter title: ");
                    String tTitle = scanner.nextLine();
                    System.out.print("Enter author: ");
                    String tAuthor = scanner.nextLine();
                    System.out.print("Enter ISBN: ");
                    String tIsbn = scanner.nextLine();
                    System.out.print("Enter subject: ");
                    String tSubject = scanner.nextLine();
                    library.addBook(new Textbook(tTitle, tAuthor, tIsbn, tSubject));
                    break;
                case 2:
                    System.out.print("Enter title: ");
                    String nTitle = scanner.nextLine();
                    System.out.print("Enter author: ");
                    String nAuthor = scanner.nextLine();
                    System.out.print("Enter ISBN: ");
                    String nIsbn = scanner.nextLine();
                    System.out.print("Enter genre: ");
                    String nGenre = scanner.nextLine();
                    library.addBook(new Novel(nTitle, nAuthor, nIsbn, nGenre));
                    break;
                case 3:
                    System.out.print("Enter keyword to search: ");
                    String keyword = scanner.nextLine();
                    List<Book> results = library.searchBooks(keyword);
                    if (results.isEmpty()) {
                        System.out.println("No books found.");
                    } else {
                        for (Book b : results) {
                            System.out.println(b);
                        }
                    }
                    break;
                case 4:
                    System.out.print("Enter ISBN to borrow: ");
                    String borrowIsbn = scanner.nextLine();
                    System.out.println(library.borrowBook(borrowIsbn) ? "Book borrowed." : "Could not borrow book.");
                    break;
                case 5:
                    System.out.print("Enter ISBN to return: ");
                    String returnIsbn = scanner.nextLine();
                    System.out.println(library.returnBook(returnIsbn) ? "Book returned." : "Could not return book.");
                    break;
                case 6:
                    System.out.println("All Books:");
                    library.displayBooks();
                    break;
                case 7:
                    System.out.println("Exiting... Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
