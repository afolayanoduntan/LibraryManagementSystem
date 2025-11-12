package GenrePk;

import ExceptionPk.InvalidGenreException;

public enum Genre {
  FICTION("Fictional Story"),
  NON_FICTION("Non-Fictional Story"),
  SCIENCE("Scientific Content"),
  HISTORY("Historical Events"),
  TECHNOLOGY("Technology Related");

  private final String description;

  Genre(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public static Genre validateGenre(String genre) throws InvalidGenreException {
    try {
      return Genre.valueOf(genre.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new InvalidGenreException("Invalid Genre: " + genre + ". Valid Genres: FICTION, NON_FICTION, SCIENCE, HISTORY, TECHNOLOGY");

    }
  }
}
