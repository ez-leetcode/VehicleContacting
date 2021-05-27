package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public interface DiscussService {

    String generateDiscuss(Long id,String title,String description,String photo1,String photo2,String photo3);

    String deleteDiscuss(Long id,Long number);

    String addComment(Long id,Long number,String comments,Long fatherNumber,Long replyNumber);

    JSONObject getDiscuss(Integer isFollow,Integer isOrderByTime,String keyword,Long cnt,Long page,Long id);

    String photoUpload(MultipartFile file);

    JSONObject getComment(Long number,Long cnt,Long page,Integer isOrderByTime);

    JSONObject getComment1(Long number,Long cnt,Long page);

    String addFavorDiscuss(Long number,Long id);

    String deleteFavorDiscuss(Long number,Long id);

    String likeComment(Long number,Long id);

    String deleteLikeComment(Long number,Long id);

    JSONObject judgeLikeAndFavor(Long number,Long id);

    String likeDiscuss(Long number,Long id);

    String dislikeDiscuss(Long number,Long id);

    JSONObject getFirstDiscuss(Long number,Integer cnt);

    JSONObject getSecondDiscuss(Long number,Long cnt,Long page,Integer isOrderByHot);

    JSONObject getThirdDiscuss(Long number,Long cnt,Long page);

    JSONObject getHotDiscuss();
}
