package blog.hashmade.elasticsearch.scan;

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
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.highlight.HighlightField;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import blog.hashmade.elasticsearch.StartNode;
import blog.hashmade.elasticsearch.beans.EBook;
import blog.hashmade.elasticsearch.beans.EBookHelper;
import blog.hashmade.elasticsearch.geodistance.GeoDistanceSearchTest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ScanSearchTest extends StartNode {

  private static final String TEXT_TO_SEARCH = "Biological";

  private static final int SCROLL_SIZE = 5;

  protected static final ESLogger logger = ESLoggerFactory.getLogger(GeoDistanceSearchTest.class.getName());

  /**
   * When we start a test, we index cities with s-coordinates
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    node.client().admin().indices().prepareCreate("culture").execute().actionGet();
    XContentBuilder xbMapping = buildMapping();
    logger.info("Mapping is : {}", xbMapping.string());
    PutMappingResponse response = node.client()
      .admin()
      .indices()
      .preparePutMapping("culture")
      .setType("ebook")
      .setSource(xbMapping)
      .execute()
      .actionGet();
    if (!response.isAcknowledged()) {
      throw new Exception("Could not define mapping.");
    }
    node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

    ObjectMapper mapper = new ObjectMapper();

    BulkRequestBuilder brb = node.client().prepareBulk();
    Collection<EBook> ebooks = EBookHelper.getEbooks();
    int i = 0;
    for (EBook ebook : ebooks) {
      IndexRequest irq = new IndexRequest("culture", "ebook", "ebook" + i);
      String jsonString = mapper.writeValueAsString(ebook);
      irq.source(jsonString);
      brb.add(irq);
      i++;
    }
    logger.info("------------------------------------------------------------------------------");
    logger.info("Starting Ebooks Search test...");
    logger.info("------------------------------------------------------------------------------");
    logger.info(ebooks.size() + " ebooks indexed");
    BulkResponse br = brb.execute().actionGet();
    if (!br.hasFailures()) {
      node.client().admin().indices().prepareRefresh().execute().actionGet();
    }

  }

  @Test
  public void performEbookSearch() throws Exception {
    logger.info("------------------------------------------------------------------------------");
    logger.info("Searching ebooks... (scan type)");
    logger.info("------------------------------------------------------------------------------");
    logger.info("Scroll size: " + SCROLL_SIZE);
    logger.info("Text to search: " + TEXT_TO_SEARCH);

    SearchRequestBuilder searchRequestBuilder = node.client()
      .prepareSearch("culture")
      .setTypes("ebook")
      .setQuery(
        QueryBuilders.boolQuery()
          .should(QueryBuilders.fieldQuery("packageName", TEXT_TO_SEARCH))
          .should(QueryBuilders.fieldQuery("bookTitle", TEXT_TO_SEARCH))
          .minimumNumberShouldMatch(1))
      .addHighlightedField("packageName")
      .addHighlightedField("bookTitle")
      .setHighlighterPreTags("<b>")
      .setHighlighterPostTags("</b>")
      .setSearchType(SearchType.SCAN)
      .setScroll(new Scroll(TimeValue.timeValueSeconds(5)))
      .setSize(SCROLL_SIZE)
      .addFields("isbn", "year")
    //.addSort(SortBuilders.fieldSort("isbn") // sort ignored for scan requests! : Note, scan search type does not support sorting (either on score or a field) or faceting.
    //    .order(SortOrder.DESC)
    // )
    ;

    SearchResponse resp = searchRequestBuilder.execute().actionGet();
    long totalHits = resp.getHits().getTotalHits();
    logger.info("------------------------------------------------------------------------------");
    logger.info("Start scrolling through search results (" + totalHits + ") - scrollId: "+resp.getScrollId());
    logger.info("------------------------------------------------------------------------------");
    while (true) {
      resp = node.client()
        .prepareSearchScroll(resp.getScrollId())
        .setScroll(TimeValue.timeValueSeconds(5))
        .execute()
        .actionGet();
      boolean hitsRead = false;
      int newHitsSize = resp.getHits().hits().length;
      if (newHitsSize > 0) {
        logger.info("Scrolling : " + newHitsSize + " results");
      }

      for (SearchHit hit : resp.getHits()) {
        hitsRead = true;
        StringBuilder builder = new StringBuilder();
        Map<String, SearchHitField> fields = hit.getFields();
        builder.append("(").append(hit.getId()).append(") : ");
        builder.append(fields.get("isbn").getValues().get(0)).append(",");
        builder.append(fields.get("year").getValues().get(0));
        builder.append(" - highlighted: ");
        for (Map.Entry<String, HighlightField> entry : hit.getHighlightFields().entrySet()) {
          builder.append(entry.getKey()).append("=[");
          for (Text fragment : entry.getValue().getFragments()) {
            builder.append(fragment);
          }
          builder.append(entry.getKey()).append("] ");

        }
        logger.info(builder.toString());
      }
      //Break condition: No hits are returned
      if (!hitsRead) {
        break;
      }
    }
  }

  private static XContentBuilder buildMapping() throws Exception {
    return jsonBuilder().prettyPrint()
      .startObject()
      .startObject("ebook")
      .startObject("properties")
      .startObject("packageName")
      .field("type", "string")
      .endObject()
      .startObject("year")
      .field("type", "integer")
      .endObject()
      .startObject("bookTitle")
      .field("type", "string")
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
    Collection<EBook> ebooks = EBookHelper.getEbooks();
    for (int i = 0; i < ebooks.size(); i++) {
      DeleteRequest dr = new DeleteRequest("culture", "ebook", "ebook_" + i);
      brb.add(dr);
    }

    BulkResponse br = brb.execute().actionGet();
    Assert.assertFalse(br.hasFailures());
    logger.info("------------------------------------------------------------------------------");
    logger.info("Ebook search test finished!");
    logger.info("------------------------------------------------------------------------------");
  }
}
