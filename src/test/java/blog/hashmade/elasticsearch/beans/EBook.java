package blog.hashmade.elasticsearch.beans;

import java.io.Serializable;
import java.util.Date;

public class EBook implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private final String source;
  private final String packageName;
  private final String packageID;
  private final String isbn;
  private final String bookTitle;
  private final int year;
  private final String issn;
  private final String seriesTitle;
  private final String volume;
  private final String imprint;
  private final String editorAuthor;
  private final String availableOnline;
  private final String shortcutURL;
  
  protected EBook(
    String source,
    String packageName,
    String packageID,
    String isbn,
    String bookTitle,
    int year,
    String issn,
    String seriesTitle,
    String volume,
    String imprint,
    String editorAuthor,
    String availableOnline,
    String shortcutURL) {
    super();
    this.source = source;
    this.packageName = packageName;
    this.packageID = packageID;
    this.isbn = isbn;
    this.bookTitle = bookTitle;
    this.year = year;
    this.issn = issn;
    this.seriesTitle = seriesTitle;
    this.volume = volume;
    this.imprint = imprint;
    this.editorAuthor = editorAuthor;
    this.availableOnline = availableOnline;
    this.shortcutURL = shortcutURL;
  }

  public String getSource() {
    return source;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getPackageID() {
    return packageID;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getBookTitle() {
    return bookTitle;
  }

  public int getYear() {
    return year;
  }

  public String getIssn() {
    return issn;
  }

  public String getSeriesTitle() {
    return seriesTitle;
  }

  public String getVolume() {
    return volume;
  }

  public String getImprint() {
    return imprint;
  }

  public String getEditorAuthor() {
    return editorAuthor;
  }

  public String getAvailableOnline() {
    return availableOnline;
  }

  public String getShortcutURL() {
    return shortcutURL;
  }
  
}
