# 📚 Library Management System

A simple GUI-based Java application to manage a library's book inventory, including functionalities like login, search, add, borrow, and return books. The project uses *Java Swing* for the UI, *JDBC* for database connectivity, and *MySQL* for data storage.

---

## ✅ Features

- 🔐 *User Authentication*
  - Login and registration system

- 📖 *Book Management*
  - Add new books (Novels or Textbooks)
  - View and search books by ID or title
  - Borrow and return books

- 🛠 *Admin Operations*
  - Admin-specific controls for book updates and deletions

- 🧠 *OOP Concepts Used*
  - Inheritance (Book, Novel, Textbook)
  - Interfaces (BookOperations)
  - Abstraction and Encapsulation

---

## 🧰 Tech Stack

| Tech        | Usage                  |
|-------------|------------------------|
| Java        | Core language          |
| Java Swing  | GUI                    |
| JDBC        | MySQL Connectivity     |
| MySQL       | Backend database       |
| IntelliJ / Eclipse | IDE             |

---

## 🗃 Project Structure

```bash
Library-Management-System/
├── src/
│   ├── models/          # Book, Novel, Textbook classes
│   ├── dao/             # DAO classes for DB operations
│   ├── ui/              # GUI classes (Login, Register, MainMenu)
│   ├── db/              # DBUtil.java for JDBC connection
│   └── Main.java        # Entry point
├── schema.sql           # MySQL table creation script
├── README.md
└── presentation.pptx
