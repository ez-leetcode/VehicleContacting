package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.mapper.CommentMapper;
import com.vehiclecontacting.mapper.DiscussMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.Comment;
import com.vehiclecontacting.pojo.Discuss;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DiscussServiceImpl implements DiscussService{


    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private DiscussMapper discussMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;


    @Override
    public String generateDiscuss(Long id, String title, String description, String photo1, String photo2, String photo3) {
        Discuss discuss = new Discuss();
        discuss.setFromId(id);
        discuss.setTitle(title);
        discuss.setDescription(description);
        if(!(photo1 == null || photo1.equals(""))){
            discuss.setPhoto1(photo1);
        }
        if(!(photo2 == null || photo2.equals(""))){
            discuss.setPhoto2(photo2);
        }
        if(!(photo3 == null || photo3.equals(""))){
            discuss.setPhoto3(photo3);
        }
        discussMapper.insert(discuss);
        log.info("创建帖子成功");
        //添加发帖次数
        User user = userMapper.selectById(id);
        user.setDiscussCounts(user.getDiscussCounts() + 1);
        userMapper.updateById(user);
        //redis限制发帖数
        redisUtils.addKeyByTime("discuss_" + id,2);
        return "success";
    }


    @Override
    public String deleteDiscuss(Long id, Long number) {
        User user = userMapper.selectById(id);
        Discuss discuss = discussMapper.selectById(number);
        if (discuss == null) {
            log.error("删除帖子失败，帖子可能已被删除");
            return "existWrong";
        }
        if (!discuss.getFromId().equals(id)) {
            log.error("删除帖子失败，帖子不是该用户所有");
            return "userWrong";
        }
        //删除帖子
        log.info("删除帖子成功");
        discussMapper.deleteById(number);
        //用户帖子数减一
        user.setDiscussCounts(user.getDiscussCounts() - 1);
        userMapper.updateById(user);
        return "success";
    }


    //老冗余怪了
    //通知再说
    @Override
    public String addComment(Long id, Long number, String comments, Long fatherNumber, Long replyNumber) {
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.error("评论失败，帖子不存在");
            return "existWrong";
        }
        if(fatherNumber != 0){
            Comment comment = commentMapper.selectById(fatherNumber);
            if(comment == null){
                log.error("评论失败，父级回复不存在");
                return "existWrong";
            }
            //暂时一样操作，后面再变动
            if(replyNumber != 0){
                Comment replyComment = commentMapper.selectById(replyNumber);
                if(replyComment == null){
                    log.error("评论失败，回复帖子不存在");
                    return "existWrong";
                }
                //二级回复评论
                Comment comment1 = new Comment();
                comment1.setId(id);
                comment1.setFatherNumber(fatherNumber);
                comment1.setReplyNumber(replyNumber);
                comment1.setComments(comments);
                comment1.setDiscussNumber(number);
                //添加评论
                commentMapper.insert(comment1);
            }else {
                //二级一般评论
                Comment comment1 = new Comment();
                comment1.setId(id);
                comment1.setFatherNumber(fatherNumber);
                comment1.setComments(comments);
                comment1.setDiscussNumber(number);
                //添加评论
                commentMapper.insert(comment1);
            }
        }else{
            //一级评论
            Comment comment = new Comment();
            comment.setComments(comments);
            comment.setId(id);
            comment.setDiscussNumber(number);
            //添加评论
            commentMapper.insert(comment);
        }
        //更新评论数
        discuss.setCommentCounts(discuss.getCommentCounts() + 1);
        discussMapper.updateById(discuss);
        //回复啥的推送后面再说
        log.info("添加评论成功");
        return "success";
    }


    @Override
    public JSONObject getDiscuss(Integer isOrderByTime, String keyword, Long cnt, Long page) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
        Page<Discuss> page1 = new Page<>(page,cnt);
        if(keyword != null && !keyword.equals("")){
            //有关键词
            wrapper.like("title",keyword);
        }
        if(isOrderByTime == 1){
            wrapper.orderByDesc("update_time");
        }else{
            wrapper.orderByDesc("like_counts");
        }
        discussMapper.selectPage(page1,wrapper);
        List<Discuss> discussList = page1.getRecords();
        jsonObject.put("discussList",discussList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("查询帖子列表成功");
        return jsonObject;
    }


}