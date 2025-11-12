package BookPk;

import GenrePk.Genre;

abstract class Book {
  private Integer bookId;
  private String title;
  private String author;
  private Genre genre;
  private Boolean isBorrowed;
  private String addedBy; // Track who added the book
  private String borrowedBy; // Track who borrowed the book

  // Constructor for new books (when adding to library)
  public Book(String title, String author, Genre genre, String addedBy) {
    this.title = title;
    this.author = author;
    this.genre = genre;
    this.isBorrowed = false;
    this.addedBy = addedBy;
    this.borrowedBy = null;
  }

  // Constructor for database-loaded books
  public Book(Integer bookId, String title, String author, Genre genre, Boolean isBorrowed, String addedBy, String borrowedBy) {
    this.bookId = bookId;
    this.title = title;
    this.author = author;
    this.genre = genre;
    this.isBorrowed = isBorrowed;
    this.addedBy = addedBy;
    this.borrowedBy = borrowedBy;
  }

  // Getters
  public Integer getBookId() { return bookId; }
  public String getTitle() { return title; }
  public String getAuthor() { return author; }
  public Genre getGenre() { return genre; }
  public Boolean getIsBorrowed() { return isBorrowed; }
  public String getAddedBy() { return addedBy; }
  public String getBorrowedBy() { return borrowedBy; }

  // Setters
  public void setBookId(Integer bookId) { this.bookId = bookId; }
  public void setIsBorrowed(Boolean isBorrowed) { this.isBorrowed = isBorrowed; }
  public void setBorrowedBy(String borrowedBy) { this.borrowedBy = borrowedBy; }

  public void displayBook() {
    StringBuffer sb = new StringBuffer();
    sb.append("Book ID: ").append(bookId)
        .append(", Title: ").append(title)
        .append(", Author: ").append(author)
        .append(", Genre: ").append(genre)
        .append(" - ").append(genre.getDescription())
        .append(", Status: ").append(isBorrowed ? "Borrowed" : "Available")
        .append(", Added By: ").append(addedBy);

    if (isBorrowed) {
      sb.append(", Borrowed By: ").append(borrowedBy);
    }

    System.out.println(sb);
  }
}
