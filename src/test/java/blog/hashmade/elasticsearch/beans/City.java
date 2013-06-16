package blog.hashmade.elasticsearch.beans;

import java.io.Serializable;

public class City implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private String name;
  private String coordinates;
  
  protected City(String name, String coordinates) {
    super();
    this.name = name;
    this.coordinates = coordinates;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(String coordinates) {
    this.coordinates = coordinates;
  }

}
