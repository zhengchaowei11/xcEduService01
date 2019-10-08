package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.AbstractHighlighterBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.fetch.subphase.highlight.Highlighter;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {


    @Autowired
    RestHighLevelClient client;


    @Autowired
    RestClient restClient;

    @Test
    public void testSearchAll() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //source源的过滤
        searchSourceBuilder.fetchSource(new String []{"name","studymodel","price","timestamp","description"},new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits){
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String)sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }





    }

    //分页查询的设置
    @Test
    public void testSearchPage() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        int page = 1;
        int size = 1;
        int from = (page-1)*size ;
        searchSourceBuilder.size(size);
        searchSourceBuilder.from(from);
        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }

    //Term

    @Test
    public void testTermQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        int page = 1;
        int size = 1;
        int from = (page-1)*size ;
        searchSourceBuilder.size(size);
        searchSourceBuilder.from(from);
        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }


    @Test
    public void testTermQueryById() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] arrys = new String[]{"1","2","3"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",arrys));


        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }


    //MatchQuery
    @Test
    public void testMatchQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("description","spring开发框架").minimumShouldMatch("80%"));

        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }


    @Test
    public void testMultiQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring css","name","description").minimumShouldMatch("50%").field("name",10));

        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }
    //Boolean查询
    @Test
    public void testBoolQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%").
                        field("name", 10);
        TermQueryBuilder studymodel = QueryBuilders.termQuery("studymodel", "201001");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(studymodel);
        boolQueryBuilder.must(multiMatchQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);

        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }



    @Test
    //过滤器查询
    public void testFilterQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%").
                        field("name", 10);
        TermQueryBuilder studymodel = QueryBuilders.termQuery("studymodel", "201001");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(studymodel);
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);

        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }



    @Test
    //过滤器查询
    public void testSortQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        TermQueryBuilder studymodel = QueryBuilders.termQuery("studymodel", "201001");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(studymodel);
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price",SortOrder.ASC);
        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }


    @Test
    //高亮的查询
    public void testHighLightQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        //创造source源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");//设置前缀
        highlightBuilder.postTags("</tag>");//设置后缀
// 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%").
                        field("name", 10);
        TermQueryBuilder studymodel = QueryBuilders.termQuery("studymodel", "201001");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(studymodel);
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);

        //source源的过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //client
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //这是一个数组
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String name = null;
        for (SearchHit hit : searchHits) {
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            name = (String) sourceAsMap.get("name");
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null){
                HighlightField nameField = highlightFields.get("name");
                if (nameField != null){
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text f : fragments){
                        stringBuffer.append(f);
                    }
                    name = stringBuffer.toString();
                }
            }
            String description = (String) sourceAsMap.get("description");
            Date date = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(date);
            System.out.println(name);
            System.out.println(description);
        }

    }







}
