package UserPk;

public class User {
  private String userId;
  private String username;

  public User(String userId, String username) {
    if (userId.length() != 8 || !userId.matches("\\d+")) {
      throw new IllegalArgumentException("User ID must be 8 digits");
    }
    this.userId = userId;
    this.username = username;
  }

  public String getUserId() { return userId; }
  public String getUsername() { return username; }

  public void displayUser() {
    System.out.println("User ID: " + userId + ", Username: " + username);
  }
}
