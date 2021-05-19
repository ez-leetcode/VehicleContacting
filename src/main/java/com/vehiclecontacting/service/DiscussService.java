package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;

public interface DiscussService {

    String generateDiscuss(Long id,String title,String description,String photo1,String photo2,String photo3);

    String deleteDiscuss(Long id,Long number);

    String addComment(Long id,Long number,String comments,Long fatherNumber,Long replyNumber);

    JSONObject getDiscuss(Integer isOrderByTime,String keyword,Long cnt,Long page);

}
