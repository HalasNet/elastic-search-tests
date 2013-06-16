package blog.hashmade.elasticsearch.beans;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.LinkedList;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;

public final class CityHelper {
  
  protected static final ESLogger logger = ESLoggerFactory.getLogger(CityHelper.class.getName());
  
  private CityHelper(){
  }
  
  public static Collection<City> getCities(){
    Collection<City> cities = new LinkedList<City>();
    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(new BufferedReader(new FileReader("src/test/resources/cities.txt")));
      String line = null;
      while( (line = reader.readLine()) !=null){
        String[] cityParams = line.split(";");
        cities.add(new City(cityParams[0], cityParams[1]+","+cityParams[2]));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e, "");
    } finally{
      if(reader!=null){
        try {
          reader.close();
        } catch (IOException e) {
          logger.error(e.getMessage(), e, "");
        }
      }
    }
    return cities;
  }

}
