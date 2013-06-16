package blog.hashmade.elasticsearch.geodistance;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.util.Collection;
import java.util.Map;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.geodistance.GeoDistanceFacet;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import blog.hashmade.elasticsearch.StartNode;
import blog.hashmade.elasticsearch.beans.City;
import blog.hashmade.elasticsearch.beans.CityHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Geospatial search.
 * <br>When starting tests, we initialize Elasticsearch cluster with cities contained in resources/cities.txt
 * <br>After tests, we remove all cities.
 * <br>see http://www.elasticsearch.org/guide/reference/java-api/search.html
 * <br>see http://www.elasticsearch.org/guide/reference/query-dsl/geo-distance-filter/
 */
public class GeoDistanceSearchTest extends StartNode {
  
  private static final int NB_MAX_RESULTS = 200;
  private static final double ORIGIN_CITY_LON = 2.34;  // Paris longitude(2.34)
  private static final double ORIGIN_CITY_LAT = 48.86; // Paris lattitude(48.86)
  private static final int DISTANCE_FROM_ORIGIN = 1000;
  private static final DistanceUnit DISTANCE_UNIT = DistanceUnit.KILOMETERS;
  
  protected static final ESLogger logger = ESLoggerFactory.getLogger(GeoDistanceSearchTest.class.getName());

  /**
   * When we start a test, we index cities with s-coordinates
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    node.client().admin().indices().prepareCreate("location").execute().actionGet();
    XContentBuilder xbMapping = buildMapping();
    logger.info("Mapping is : {}", xbMapping.string());
    PutMappingResponse response = node.client()
      .admin()
      .indices()
      .preparePutMapping("location")
      .setType("city")
      .setSource(xbMapping)
      .execute()
      .actionGet();
    if (!response.isAcknowledged()) {
      throw new Exception("Could not define mapping.");
    }
    node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
    
    ObjectMapper mapper = new ObjectMapper();

    BulkRequestBuilder brb = node.client().prepareBulk();
    Collection<City> cities = CityHelper.getCities();
    int i = 0;
    for (City city : cities) {
      IndexRequest irq = new IndexRequest("location", "city", "city"+i);
      String jsonString = mapper.writeValueAsString(city);
      irq.source(jsonString);
      brb.add(irq);
      i++;
    }
    logger.info("------------------------------------------------------------------------------");
    logger.info("Starting GeoDistanceSearch test...");
    logger.info("------------------------------------------------------------------------------");
    logger.info(cities.size() +" cities indexed");
    BulkResponse br = brb.execute().actionGet();
    if(!br.hasFailures()){
      node.client().admin().indices().prepareRefresh().execute().actionGet();
    }
    
  }

  @Test
  public void performGeoDistanceSearch() throws Exception {
    logger.info("------------------------------------------------------------------------------");
    logger.info("Searching cities...");
    logger.info("------------------------------------------------------------------------------");
    logger.info("Origin lattitude: "+ORIGIN_CITY_LAT);
    logger.info("Origin longitude: "+ORIGIN_CITY_LON);
    logger.info("Distance from origin: "+DISTANCE_FROM_ORIGIN+ " " + DISTANCE_UNIT);
    logger.info("Max results limit: "+NB_MAX_RESULTS);
    GeoDistanceFilterBuilder geoDistanceFilterBuilder = FilterBuilders.geoDistanceFilter("coordinates")
      .point(ORIGIN_CITY_LAT, ORIGIN_CITY_LON)
      .distance(DISTANCE_FROM_ORIGIN, DISTANCE_UNIT)
      .optimizeBbox("memory")        // Can be also "indexed" or "none"
      .geoDistance(GeoDistance.ARC); // Or GeoDistance.PLANE
    
    SearchRequestBuilder searchRequestBuilder = node.client().prepareSearch("location").setTypes("city")
    .setQuery(QueryBuilders.matchAllQuery())
    .setFilter(geoDistanceFilterBuilder)
    .setFrom(0)
    .setSize(NB_MAX_RESULTS)
    .addFields("name")
    .addSort(SortBuilders.geoDistanceSort("coordinates")
        .order(SortOrder.DESC)
        .point(ORIGIN_CITY_LAT, ORIGIN_CITY_LON)
        .unit(DISTANCE_UNIT))
    .addFacet(FacetBuilders.geoDistanceFacet("GeoDistanceFacet")
        .field("coordinates")
        .point(ORIGIN_CITY_LAT, ORIGIN_CITY_LON)
        //.addUnboundedFrom(10)
        .addRange(0, DISTANCE_FROM_ORIGIN)
        //.addRange(20, 100)
        //.addUnboundedTo(100)
        .unit(DISTANCE_UNIT));
    
    SearchResponse resp = searchRequestBuilder.execute().actionGet();
    GeoDistanceFacet facet = (GeoDistanceFacet) resp.getFacets().facetsAsMap().get("GeoDistanceFacet");
    logger.info("------------------------------------------------------------------------------");
    logger.info("Search Results (reversed order):");
    logger.info("------------------------------------------------------------------------------");
    for (SearchHit hit : resp.getHits()) {
      Map<String,SearchHitField> fields = hit.getFields();
      for(Map.Entry<String,SearchHitField> entry : fields.entrySet()){
        logger.info(entry.getValue().getValues().get(0) +" ("+hit.getId()+") is "+hit.getSortValues()[0].toString()+" km far from origin");
      }
    }
    logger.info("------------------------------------------------------------------------------");
    logger.info("GeoDistanceFacet results:");
    logger.info("------------------------------------------------------------------------------");
    for (GeoDistanceFacet.Entry entry : facet) {
      logger.info("Distance from origin: "+entry.getFrom());            // Distance from requested
      logger.info("Distance to requested: "+entry.getTo());              // Distance to requested
      logger.info("Number of results: "+entry.getCount());           // Doc count
      logger.info("Minimum distance: " +entry.getMin());             // Min value
      logger.info("Maximum distance: "+entry.getMax());             // Max value
      logger.info("Sum of distances: "+entry.getTotal());           // Sum of values
      logger.info("Mean distance: "+entry.getMean());            // Mean
  }
  }

  private static XContentBuilder buildMapping() throws Exception {
    return jsonBuilder().prettyPrint()
      .startObject()
      .startObject("city")
      .startObject("properties")
      .startObject("coordinates")
      .field("type", "geo_point")
      .endObject()
      .startObject("name")
      .field("type", "string")
      .field("index", "no")//http://www.elasticsearch.org/guide/reference/mapping/core-types/
      .field("include_in_all", false)
      .endObject()
      .endObject()
      .endObject()
      .endObject();
  }

  /**
   * Remove all data
   */
  @After
  public void tearDown() {
    BulkRequestBuilder brb = node.client().prepareBulk();
    Collection<City> cities = CityHelper.getCities();
    for (int i = 0; i < cities.size(); i++) {
      DeleteRequest dr = new DeleteRequest("location", "city", "city_" + i);
      brb.add(dr);
    }

    BulkResponse br = brb.execute().actionGet();
    Assert.assertFalse(br.hasFailures());
    logger.info("------------------------------------------------------------------------------");
    logger.info("GeoDistanceSearch test finished!");
    logger.info("------------------------------------------------------------------------------");
  }
}
