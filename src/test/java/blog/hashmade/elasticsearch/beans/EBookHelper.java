package blog.hashmade.elasticsearch.beans;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.LinkedList;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;

public final class EBookHelper {

  protected static final ESLogger logger = ESLoggerFactory.getLogger(EBookHelper.class.getName());

  private EBookHelper() {
  }

  //"Biological"
  public static Collection<EBook> getEbooks() {
    Collection<EBook> ebooks = new LinkedList<EBook>();
    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(new BufferedReader(new FileReader("src/test/resources/ebook2013.csv")));
      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] ebookParams = line.split(",");
        String source = removeQuotesIfPresent(ebookParams[0]);
        String packageName = removeQuotesIfPresent(ebookParams[1]);
        String packageID = removeQuotesIfPresent(ebookParams[2]);
        String isbn = removeQuotesIfPresent(ebookParams[3]);
        String bookTitle = removeQuotesIfPresent(ebookParams[4]);
        int year = Integer.parseInt(removeQuotesIfPresent(ebookParams[5]));
        String issn = removeQuotesIfPresent(ebookParams[6]);
        String seriesTitle = removeQuotesIfPresent(ebookParams[7]);
        String volume = removeQuotesIfPresent(ebookParams[8]);
        String imprint = removeQuotesIfPresent(ebookParams[9]);
        String editorAuthor = removeQuotesIfPresent(ebookParams[10]);
        String availableOnline = removeQuotesIfPresent(ebookParams[11]);
        String shortcutURL = removeQuotesIfPresent(ebookParams[12]);
        ebooks.add(new EBook(
          source,
          packageName,
          packageID,
          isbn,
          bookTitle,
          year,
          issn,
          seriesTitle,
          volume,
          imprint,
          editorAuthor,
          availableOnline,
          shortcutURL));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e, "");
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          logger.error(e.getMessage(), e, "");
        }
      }
    }
    return ebooks;
  }
  
  private static String removeQuotesIfPresent(String inputString){
    int startIndex = inputString.startsWith("\"") ? 1:0;
    int endIndex = inputString.endsWith("\"") ? inputString.length()-1:inputString.length(); 
    return inputString.substring(startIndex, endIndex);
  }
}
