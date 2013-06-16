package blog.hashmade.elasticsearch.beans;

import java.io.Serializable;

public class City implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private String name;
  private String location;
  
  protected City(String name, String location) {
    super();
    this.name = name;
    this.location = location;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }


}
