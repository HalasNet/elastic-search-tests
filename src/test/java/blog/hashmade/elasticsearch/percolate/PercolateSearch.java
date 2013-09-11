package blog.hashmade.elasticsearch.percolate;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.percolate.PercolateRequest;
import org.elasticsearch.action.percolate.PercolateRequestBuilder;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import blog.hashmade.elasticsearch.StartNode;

public class PercolateSearch extends StartNode {

  protected static final ESLogger logger = ESLoggerFactory.getLogger(PercolateSearch.class.getName());

  @Before
  public void setUp() throws Exception {
    node.client().admin().indices().prepareCreate("vehicle").execute().actionGet();
    XContentBuilder xbMapping = buildMapping();
    logger.info("Mapping is : {}", xbMapping.string());
    PutMappingResponse response = node.client()
      .admin()
      .indices()
      .preparePutMapping("vehicle")
      .setType("car")
      .setSource(xbMapping)
      .execute()
      .actionGet();
    if (!response.isAcknowledged()) {
      throw new Exception("Could not define mapping.");
    }
    node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

    QueryBuilder qbMercedes = QueryBuilders.termQuery("brand", "mercedes");
    xbMapping = jsonBuilder();
    xbMapping.startObject().field("query", qbMercedes) // Register the query
      .endObject();

    QueryBuilder qbBMW = QueryBuilders.termQuery("brand", "bmw");
    xbMapping = jsonBuilder();
    xbMapping.startObject().field("query", qbBMW) // Register the query
      .endObject();

    node.client()
      .prepareIndex("_percolator", "vehicle", "mercedes cars")
      .setSource(jsonBuilder().startObject().field("query", qbMercedes) // Register the query
        .endObject())
      .setRefresh(true)
      // Needed when the query shall be available immediately
      .execute()
      .actionGet();

    node.client()
      .prepareIndex("_percolator", "vehicle", "bmw cars")
      .setSource(jsonBuilder().startObject().field("query", qbBMW) // Register the query
        .endObject())
      .setRefresh(true)
      .execute()
      .actionGet();

    node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

  }

  @Test
  public void testPercolate() throws Exception {
    XContentBuilder docBuilder = XContentFactory.jsonBuilder().startObject();
    docBuilder.field("doc").startObject(); //This is needed to designate the document
    docBuilder.field("brand", "mercedes");
    docBuilder.endObject(); //End of the doc field
    docBuilder.endObject(); //End of the JSON root object
    /*PercolateResponse response = node.client()
      .preparePercolate("vehicle", "car")
      .setSource(docBuilder)
      .execute()
      .actionGet();
    //Iterate over the results
    for (String result : response) {
      logger.info("result=" + result, null);
    }*/

    PercolateRequest percolateRequest = new PercolateRequestBuilder(node.client()).setIndex("vehicle")
      .setType("car")
      .setSource(docBuilder)
      .request();
    ActionFuture<PercolateResponse> aReponse = node.client().percolate(percolateRequest);
    PercolateResponse response = aReponse.actionGet();
    for (String result : response) {
      logger.info("result=" + result, null);
    }

  }

  /**
   * Remove all data
   */
  @After
  public void tearDown() {
    node.client().close();
    node.close();
  }

  private static XContentBuilder buildMapping() throws Exception {
    return jsonBuilder().prettyPrint()
      .startObject()
      .startObject("car")
      .startObject("properties")
      .startObject("color")
      .field("type", "string")
      .endObject()
      .startObject("brand")
      .field("type", "string")
      .endObject()
      .endObject()
      .endObject()
      .endObject();
  }
}
