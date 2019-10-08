package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;

    @Value("${xuecheng.course.type}")
    private String type;


    @Value("${xuecheng.course.source_field}")
    private String source_field;

    @Value("${xuecheng.media.index}")
    private String media_index;

    @Value("${xuecheng.media.type}")
    private String media_type;


    @Value("${xuecheng.media.source_field}")
    private String media_source_field;


    @Autowired
    RestHighLevelClient restHighLevelClient;


    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
       //防止下面的空指针的出现
        if (courseSearchParam == null){
           courseSearchParam = new CourseSearchParam();
        }

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);

        //设置搜索源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        String[] source_field_arry = source_field.split(",");
        //设置不需要的字段 new String []{}
        //设置过滤源字段
        searchSourceBuilder.fetchSource(source_field_arry,new String[]{});

        //设置boolQuery进行条件的查询进行组合查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();


        //设置搜索条件

        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "teachplan", "description")
                    .minimumShouldMatch("70%")
                    .field("name",10);
            boolQueryBuilder.must(multiMatchQueryBuilder);

        }

        if (StringUtils.isNotEmpty(courseSearchParam.getMt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }
        if (page<0){
            page=1;
        }
        if (size<0){
            size=2;
        }
        int from = (page-1)*size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);


        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);



        QueryResult<CoursePub> queryResult = new QueryResult();
        List<CoursePub> list = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            long totalHits = hits.getTotalHits();
            queryResult.setTotal(totalHits);
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits){
                CoursePub coursePub = new CoursePub();
                //课程名称
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String id =(String) sourceAsMap.get("id");
                coursePub.setId(id);
                String name =(String) sourceAsMap.get("name");
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields != null){
                    HighlightField highlightField = highlightFields.get("name");
                    if (highlightField != null){
                        Text[] fragments = highlightField.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text text : fragments){
                            stringBuffer.append(text);
                        }
                        name = stringBuffer.toString();
                    }



                }
                coursePub.setName(name);
                //课程图片
                String pic =(String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //新的价格
                Double price = null;
                try {
                    if(sourceAsMap.get("price")!=null ){
                        price = (Double)sourceAsMap.get("price");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                Double price_old = null;
                try {
                    if(sourceAsMap.get("price_old")!=null ){
                        price_old = (Double)sourceAsMap.get("price_old");
                        //System.out.println("1111111");发生异常不会被执行,会被捕捉，不影响异常捕获代码后面的代码的执行
                    }
                } catch (Exception e) {
                    //System.out.println("2111111");
                    e.printStackTrace();
                    //System.out.println("444444");
                }

                //System.out.println("31111111");
                coursePub.setPrice_old(price_old);
                list.add(coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        queryResult.setList(list);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    public Map<String,CoursePub> getall(String id) {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));


        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] hits = searchHits.getHits();
            Map<String,CoursePub> map = new HashMap<>();
            for (SearchHit searchHit : hits){
                CoursePub coursePub = new CoursePub();
                //得到源数据的内容
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                String  courseId =(String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setTeachplan(teachplan);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setCharge(charge);
                coursePub.setDescription(description);
                map.put(courseId,coursePub);
                return map;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public QueryResponseResult getmedia(String[] teachplanIds) {
        SearchRequest searchRequest = new SearchRequest(media_index);
        searchRequest.types(media_type);
        //设置searchBuileders
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置查询条件，如果是数组的话，用TermsQuery
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        //设置过滤的条件
        String[] split = media_source_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});
        //将查询对象封装到searchRequest
        searchRequest.source(searchSourceBuilder);

        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long total = 0;
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            total = hits.getTotalHits();
            SearchHit[] hitsHits = hits.getHits();
            for (SearchHit  searchHit : hitsHits){
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);
                teachplanMediaPubList.add(teachplanMediaPub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        QueryResult queryResult = new QueryResult();
        queryResult.setTotal(total);
        queryResult.setList(teachplanMediaPubList);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;

    }
}
