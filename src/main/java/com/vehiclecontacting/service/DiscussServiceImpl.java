package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.mapper.*;
import com.vehiclecontacting.msg.CommentMsg;
import com.vehiclecontacting.msg.CommentMsg1;
import com.vehiclecontacting.msg.DiscussMsg;
import com.vehiclecontacting.msg.OwnerCommentMsg;
import com.vehiclecontacting.pojo.*;
import com.vehiclecontacting.utils.OssUtils;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.LinkedList;
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

    @Autowired
    private FavorDiscussMapper favorDiscussMapper;

    @Autowired
    private CommentLikesMapper commentLikesMapper;

    @Autowired
    private LikeDiscussMapper likeDiscussMapper;


    @Override
    public String generateDiscuss(Long id, String title, String description, String photo1, String photo2, String photo3) {
        Discuss discuss = new Discuss();
        discuss.setFromId(id);
        discuss.setTitle(title);
        discuss.setDescription(description);
        //设置一下时间
        discuss.setUpdateTime(new Date());
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
    public JSONObject getDiscuss(Integer isFollow, Integer isOrderByTime, String keyword, Long cnt, Long page, Long id) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
        Page<Discuss> page1 = new Page<>(page,cnt);
        List<Discuss> discussList;
        if(isFollow == 1){
            //找关注的列表
            discussList = discussMapper.getFollowDiscuss(id,page1);
        }else{
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
            discussList = page1.getRecords();
        }
        //待处理
        List<DiscussMsg> discussMsgList = new LinkedList<>();
        for(Discuss x:discussList){
            //获取用户实例
            User user = userMapper.selectById(x.getFromId());
            DiscussMsg discussMsg = new DiscussMsg(x.getNumber(),x.getPhoto1(),user.getUsername(),user.getPhoto(),x.getTitle(),x.getDescription(),x.getLikeCounts(),
                    x.getCommentCounts(),x.getFavorCounts(),x.getScanCounts(),x.getUpdateTime());
            discussMsgList.add(discussMsg);
        }
        jsonObject.put("discussList",discussMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("查询帖子列表成功");
        return jsonObject;
    }


    @Override
    public String photoUpload(MultipartFile file) {
        String url = OssUtils.uploadPhoto(file,"discussPhoto");
        if(url.length() < 12){
            log.error("上传帖子图片文件失败");
            return url;
        }
        //成功上传图片
        log.info("上传帖子图片文件成功，url：" + url);
        return url;
    }


    @Override
    public JSONObject getComment(Long number, Long cnt, Long page, Integer isOrderByTime) {
        JSONObject jsonObject = new JSONObject();
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.warn("获取一级评论失败，帖子不存在");
            return null;
        }
        //获取帖子主人实例
        User user = userMapper.selectById(discuss.getFromId());
        OwnerCommentMsg ownerCommentMsg = new OwnerCommentMsg(number,user.getId(),user.getUsername(),user.getPhoto(),user.getSex(),discuss.getTitle(),discuss.getDescription(),
                discuss.getPhoto1(),discuss.getPhoto2(),discuss.getPhoto3(),discuss.getLikeCounts(),discuss.getCommentCounts(),discuss.getFavorCounts(),discuss.getScanCounts(),discuss.getCreateTime(),discuss.getDeleted());
        jsonObject.put("OwnerComment",ownerCommentMsg);
        //获得评论列表
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        Page<Comment> page1 = new Page<>(page,cnt);
        if(isOrderByTime == 0){
            wrapper.orderByDesc("create_time");
        }else if(isOrderByTime == 1){
            wrapper.orderByDesc("like_counts");
        }else{
            wrapper.orderByAsc("create_time");
        }
        //没有父级
        wrapper.eq("father_number",0);
        wrapper.eq("discuss_number",number);
        commentMapper.selectPage(page1,wrapper);
        List<Comment> commentList = page1.getRecords();
        List<CommentMsg> commentMsgList = new LinkedList<>();
        for(Comment x:commentList){
            //获取用户实例
            User user1 = userMapper.selectById(x.getId());
            //评论信息实例
            CommentMsg commentMsg = new CommentMsg(x.getNumber(),x.getId(),user1.getUsername(),user1.getPhoto(),user1.getSex(),x.getComments(),x.getLikeCounts(),x.getCommentCounts(),null,
                    null,null,null,null,null,null,null,null,null,null,null,null,null,null,x.getCreateTime());
            QueryWrapper<Comment> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("father_number",x.getNumber())
                    .eq("discuss_number",number);
            //获取下属评论
            List<Comment> commentList1 = commentMapper.selectList(wrapper1);
            int k = 1;
            for(Comment x1:commentList1){
                if(k == 1){
                    User user2 = userMapper.selectById(x1.getId());
                    commentMsg.setReplyDescription1(x1.getComments());
                    commentMsg.setReplyId1(x1.getId());
                    commentMsg.setReplyUsername1(user2.getUsername());
                    if(x1.getReplyNumber() != 0){
                        //有回复过别人
                        Comment comment = commentMapper.selectById(x1.getReplyNumber());
                        User user3 = userMapper.selectById(comment.getId());
                        commentMsg.setSecondReplyUsername1(user3.getUsername());
                    }
                }else if(k == 2){
                    User user2 = userMapper.selectById(x1.getId());
                    commentMsg.setReplyDescription2(x1.getComments());
                    commentMsg.setReplyId2(x1.getId());
                    commentMsg.setReplyUsername2(user2.getUsername());
                    if(x1.getReplyNumber() != 0){
                        //有回复过别人
                        Comment comment = commentMapper.selectById(x1.getReplyNumber());
                        User user3 = userMapper.selectById(comment.getId());
                        commentMsg.setSecondReplyUsername2(user3.getUsername());
                    }
                }else if(k == 3){
                    User user2 = userMapper.selectById(x1.getId());
                    commentMsg.setReplyDescription3(x1.getComments());
                    commentMsg.setReplyId3(x1.getId());
                    commentMsg.setReplyUsername3(user2.getUsername());
                    if(x1.getReplyNumber() != 0){
                        //有回复过别人
                        Comment comment = commentMapper.selectById(x1.getReplyNumber());
                        User user3 = userMapper.selectById(comment.getId());
                        commentMsg.setSecondReplyUsername3(user3.getUsername());
                    }
                    break;
                }
                k ++;
            }
            commentMsgList.add(commentMsg);
        }
        jsonObject.put("commentList",commentMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取帖子评论列表成功");
        log.info(jsonObject.toString());
        //加浏览量待完成
        return jsonObject;
    }


    @Override
    public JSONObject getComment1(Long number, Long cnt, Long page) {
        JSONObject jsonObject = new JSONObject();
        Comment comment = commentMapper.selectById(number);
        if(comment == null){
            log.error("获取评论列表失败，评论不存在");
            return null;
        }
        //评论存在，先获取父评论信息
        User user = userMapper.selectById(comment.getId());
        CommentMsg1 commentMsg1 = new CommentMsg1(comment.getNumber(),comment.getId(),comment.getComments(),user.getUsername(),user.getPhoto(),
                comment.getLikeCounts(),user.getSex(),comment.getCommentCounts(),0L,null,null,null,comment.getCreateTime());
        jsonObject.put("fatherComment",commentMsg1);
        //获取子评论信息
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("father_number",comment.getNumber())
                .orderByDesc("create_time");
        Page<Comment> page1 = new Page<>(page,cnt);
        commentMapper.selectPage(page1,wrapper);
        List<Comment> commentList = page1.getRecords();
        List<CommentMsg1> commentMsg1List = new LinkedList<>();
        for(Comment x:commentList){
            //评论者实例
            User user1 = userMapper.selectById(x.getId());
            if(x.getReplyNumber() != 0){
                //有回复人，获取被回复人的信息
                Comment comment1 = commentMapper.selectById(x.getNumber());
                User user2 = userMapper.selectById(comment1.getId());
                CommentMsg1 commentMsg11 = new CommentMsg1(x.getNumber(),x.getId(),x.getComments(),user1.getUsername(),user1.getPhoto(),x.getLikeCounts(),user1.getSex(),
                        x.getCommentCounts(),x.getReplyNumber(),comment1.getId(),user2.getUsername(),comment1.getComments(),x.getCreateTime());
                commentMsg1List.add(commentMsg11);
            }else{
                //没有回复人
                CommentMsg1 commentMsg11 = new CommentMsg1(x.getNumber(),x.getId(),x.getComments(),user1.getUsername(),user1.getPhoto(),x.getLikeCounts(),user1.getSex(),
                        x.getCommentCounts(),0L,null,null,null,x.getCreateTime());
                commentMsg1List.add(commentMsg11);
            }
        }
        jsonObject.put("commentList",commentMsg1List);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取二级评论信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public String addFavorDiscuss(Long number, Long id) {
        //获取帖子
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.error("收藏帖子失败，帖子不存在或已被冻结");
            return "existWrong";
        }
        QueryWrapper<FavorDiscuss> wrapper = new QueryWrapper<>();
        wrapper.eq("number",number)
                .eq("id",id);
        FavorDiscuss favorDiscuss = favorDiscussMapper.selectOne(wrapper);
        if(favorDiscuss != null){
            log.error("收藏帖子失败，帖子已被收藏");
            return "repeatWrong";
        }
        //收藏帖子
        FavorDiscuss favorDiscuss1 = new FavorDiscuss(number,id,null);
        favorDiscussMapper.insert(favorDiscuss1);
        //更新帖子收藏数
        discuss.setFavorCounts(discuss.getFavorCounts() + 1);
        discussMapper.updateById(discuss);
        //通知待完成
        log.info("收藏帖子成功");
        return "success";
    }

    @Override
    public String deleteFavorDiscuss(Long number, Long id) {
        //获取帖子
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.error("移除收藏帖子失败，帖子不存在或已被冻结");
            return "existWrong";
        }
        QueryWrapper<FavorDiscuss> wrapper = new QueryWrapper<>();
        wrapper.eq("number",number)
                .eq("id",id);
        FavorDiscuss favorDiscuss = favorDiscussMapper.selectOne(wrapper);
        if(favorDiscuss == null){
            //没有添加收藏
            log.error("移除收藏帖子失败，帖子未被收藏");
            return "repeatWrong";
        }
        //移除收藏
        favorDiscussMapper.delete(wrapper);
        //修改收藏数
        discuss.setFavorCounts(discuss.getFavorCounts() - 1);
        discussMapper.updateById(discuss);
        log.info("移除帖子收藏成功");
        return "success";
    }


    @Override
    public String likeComment(Long number, Long id) {
        Comment comment = commentMapper.selectById(number);
        if(comment == null){
            log.error("点赞评论失败，评论不存在");
            return "existWrong";
        }
        QueryWrapper<CommentLikes> wrapper = new QueryWrapper<>();
        wrapper.eq("number",number)
                .eq("id",id);
        CommentLikes commentLikes = commentLikesMapper.selectOne(wrapper);
        if(commentLikes != null){
            log.error("点赞评论失败，评论已被点赞");
            return "repeatWrong";
        }
        //点赞评论
        commentLikesMapper.insert(new CommentLikes(number,id,null));
        //点赞数加一
        comment.setLikeCounts(comment.getLikeCounts() + 1);
        commentMapper.updateById(comment);
        //通知待完成
        log.info("点赞评论成功");
        return "success";
    }


    @Override
    public String deleteLikeComment(Long number, Long id) {
        Comment comment = commentMapper.selectById(number);
        if(comment == null){
            log.error("取消点赞失败，评论不存在");
            return "existWrong";
        }
        QueryWrapper<CommentLikes> wrapper = new QueryWrapper<>();
        wrapper.eq("number",number)
                .eq("id",id);
        CommentLikes commentLikes = commentLikesMapper.selectOne(wrapper);
        if(commentLikes == null){
            log.error("取消点赞失败，评论未被点赞");
            return "repeatWrong";
        }
        //取消点赞评论
        commentLikesMapper.delete(wrapper);
        //点赞数减一
        comment.setLikeCounts(comment.getLikeCounts() - 1);
        commentMapper.updateById(comment);
        //通知待完成
        log.info("取消点赞评论成功");
        return "success";
    }

    @Override
    public JSONObject judgeLikeAndFavor(Long number, Long id) {
        JSONObject jsonObject = new JSONObject();
        //先获取收藏相关信息
        QueryWrapper<FavorDiscuss> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id)
                .eq("number",number);
        FavorDiscuss favorDiscuss = favorDiscussMapper.selectOne(wrapper);
        if(favorDiscuss == null){
            //没有收藏
            jsonObject.put("isFavor",0);
        }else{
            //已收藏
            jsonObject.put("isFavor",1);
        }
        //获取点赞相关信息
        //待完成
        QueryWrapper<LikeDiscuss> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("id",id)
                .eq("number",number);
        LikeDiscuss likeDiscuss = likeDiscussMapper.selectOne(wrapper1);
        if(likeDiscuss == null){
            //没有点赞
            jsonObject.put("isLike",0);
        }else{
            //已点赞
            jsonObject.put("isLike",1);
        }
        log.info("获取点赞和收藏相关信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public String likeDiscuss(Long number, Long id) {
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.error("点赞帖子失败，帖子不存在");
            return "existWrong";
        }
        //点赞
        likeDiscussMapper.insert(new LikeDiscuss(number,id,null));
        //加点赞数
        discuss.setLikeCounts(discuss.getLikeCounts() + 1);
        discussMapper.updateById(discuss);
        //推送待完成
        log.info("点赞帖子成功");
        return "success";
    }

    @Override
    public String dislikeDiscuss(Long number, Long id) {
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.error("取消点赞失败，帖子不存在");
            return "existWrong";
        }
        //取消点赞
        QueryWrapper<LikeDiscuss> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id)
                .eq("number",number);
        likeDiscussMapper.delete(wrapper);
        //减少点赞数
        discuss.setLikeCounts(discuss.getLikeCounts() - 1);
        discussMapper.updateById(discuss);
        //推送待完成
        log.info("取消点赞帖子成功");
        return "success";
    }

}