package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.config.RabbitmqProductConfig;
import com.vehiclecontacting.mapper.*;
import com.vehiclecontacting.msg.*;
import com.vehiclecontacting.pojo.*;
import com.vehiclecontacting.utils.CommentUtils;
import com.vehiclecontacting.utils.KeywordUtils;
import com.vehiclecontacting.utils.OssUtils;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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

    @Autowired
    private HistoryDiscussMapper historyDiscussMapper;

    @Autowired
    private RabbitmqProductConfig rabbitmqProductConfig;

    public static List<String> hotDiscussKeyWord = new LinkedList<>();

    //模板
    public static List<String> hotDiscussKeywordTemplate = new LinkedList<>();

    static{
        hotDiscussKeyWord.add("小青龙");
        hotDiscussKeyWord.add("美工小姐姐");
        hotDiscussKeyWord.add("跑路了");
        hotDiscussKeyWord.add("真的该死");
        hotDiscussKeyWord.add("我恨陆茜蒙");
        hotDiscussKeyWord.add("美工产品不干活");
        hotDiscussKeyWord.add("都让前后端小哥来干");
        hotDiscussKeyWord.add("真有你的");
        hotDiscussKeyWord.add("建议改为");
        hotDiscussKeyWord.add("吉祥物");
        hotDiscussKeywordTemplate.add("小青龙");
        hotDiscussKeywordTemplate.add("美工小姐姐");
        hotDiscussKeywordTemplate.add("跑路了");
        hotDiscussKeywordTemplate.add("真的该死");
        hotDiscussKeywordTemplate.add("我恨陆茜蒙");
        hotDiscussKeywordTemplate.add("美工产品不干活");
        hotDiscussKeywordTemplate.add("都让前后端小哥来干");
        hotDiscussKeywordTemplate.add("真有你的");
        hotDiscussKeywordTemplate.add("建议改为");
        hotDiscussKeywordTemplate.add("吉祥物");
    }

    public static Long hotDiscussNumber1 = 0L;

    public static Long hotDiscussNumber2 = 0L;

    public static Long hotDiscussNumber3 = 0L;

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
        //先判断是否被禁言
        User user = userMapper.selectById(id);
        if(user.getNoSpeakDate() != null && user.getNoSpeakDate().after(new Date())){
            //已经被禁言
            log.info("评论失败，用户已被禁言");
            return "noSpeakWrong";
        }else if(user.getNoSpeakDate() != null && user.getNoSpeakDate().before(new Date())){
            //重置dirtyCounts
            user.setDirtyCounts(0);
            userMapper.updateById(user);
            log.info("用户禁言已被解除，脏话次数已重置");
        }
        //再判断是否为垃圾话
        boolean isDirtyTalk = CommentUtils.judgeComment(comments);
        if(isDirtyTalk){
            //垃圾话，加一，或者禁言
            user.setDirtyCounts(user.getDirtyCounts() + 1);
            if(user.getDirtyCounts() > 5){
                //垃圾话超过5次，禁言2小时
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR,2);
                Date date = calendar.getTime();
                user.setNoSpeakDate(date);
                log.info("用户垃圾话过多，已被禁言2小时");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id",id);
                jsonObject.put("message","您在社区的发言不符合规范，已被自动禁言2小时！");
                jsonObject.put("title","禁言通知");
                rabbitmqProductConfig.sendBoxMessage(jsonObject);
            }
            userMapper.updateById(user);
            log.info("评论失败，用户说垃圾话");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",id);
            jsonObject.put("message","您在社区的发言不符合规范，该发言已被自动删除，多次犯规会遭受禁言处罚！");
            jsonObject.put("title","评论过滤通知");
            rabbitmqProductConfig.sendBoxMessage(jsonObject);
            return "dirtyWrong";
        }
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
                //回复别人
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id",replyComment.getId());
                jsonObject.put("title",user.getUsername() + "回复了你的评论");
                jsonObject.put("message",comments);
                rabbitmqProductConfig.sendBoxMessage(jsonObject);
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
            comment.setCommentCounts(comment.getCommentCounts() + 1);
            commentMapper.updateById(comment);
            if(!comment.getId().equals(user.getId())){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id",comment.getId());
                jsonObject.put("title",user.getUsername() + "评论了你的留言");
                jsonObject.put("message",comments);
                rabbitmqProductConfig.sendBoxMessage(jsonObject);
            }
        }else{
            //一级评论
            Comment comment = new Comment();
            comment.setComments(comments);
            comment.setId(id);
            comment.setDiscussNumber(number);
            //添加评论
            commentMapper.insert(comment);
            User user1 = userMapper.selectById(discuss.getFromId());
            if(!user1.getId().equals(discuss.getFromId())){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id",discuss.getFromId());
                jsonObject.put("title",user1.getUsername() + "评论了你的帖子");
                jsonObject.put("message",comments);
                rabbitmqProductConfig.sendBoxMessage(jsonObject);
            }
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
                String realKeyword = KeywordUtils.deleteSpace(keyword);
                wrapper.like("title",realKeyword);
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
        if(keyword != null && !keyword.equals("")){
            //存热搜词
            String realKeyword = KeywordUtils.deleteSpace(keyword);
            redisUtils.addKeyByTime("hotKeyword_" + realKeyword,24);
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
        redisUtils.addKeyByTime("scan_" + number,24);
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
        //帖子收藏缓存
        redisUtils.addKeyByTime("cntFavor_" + number,24);
        //通知待完成
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",discuss.getFromId());
        jsonObject.put("title","用户收藏通知");
        jsonObject.put("message","有一个新用户收藏了您的帖子");
        rabbitmqProductConfig.sendBoxMessage(jsonObject);
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
        //移除收藏缓存
        redisUtils.subKeyByTime("cntFavor_" + number,24);
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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",comment.getId());
        jsonObject.put("message","有一个新用户点赞了您的评论");
        jsonObject.put("title","用户点赞通知");
        rabbitmqProductConfig.sendBoxMessage(jsonObject);
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
        //点赞缓存
        redisUtils.addKeyByTime("cntLike_" + number,24);
        //推送待完成
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",discuss.getFromId());
        jsonObject.put("title","帖子点赞通知");
        jsonObject.put("message","有一个新用户点赞了您的帖子");
        rabbitmqProductConfig.sendBoxMessage(jsonObject);
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
        //取消点赞缓存
        redisUtils.subKeyByTime("cntLike_" + number,24);
        //推送待完成
        log.info("取消点赞帖子成功");
        return "success";
    }


    @Override
    public JSONObject getHotDiscuss() {
        JSONObject jsonObject = new JSONObject();
        log.info("热帖1：" + hotDiscussNumber1);
        log.info("热帖2：" + hotDiscussNumber2);
        log.info("热帖3：" + hotDiscussNumber3);
        QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("scan_counts");
        List<Discuss> discussList = discussMapper.selectList(wrapper);
        List<HotDiscussMsg> hotDiscussMsgList = new LinkedList<>();
        if(hotDiscussNumber1 == 0L){
            hotDiscussMsgList.add(new HotDiscussMsg(discussList.get(0).getNumber(),discussList.get(0).getPhoto1(),discussList.get(0).getTitle()));
            hotDiscussMsgList.add(new HotDiscussMsg(discussList.get(1).getNumber(),discussList.get(1).getPhoto1(),discussList.get(1).getTitle()));
            hotDiscussMsgList.add(new HotDiscussMsg(discussList.get(2).getNumber(),discussList.get(2).getPhoto1(),discussList.get(2).getTitle()));
        }else if(hotDiscussNumber2 == 0L){
            Discuss discuss = discussMapper.selectById(hotDiscussNumber1);
            hotDiscussMsgList.add(new HotDiscussMsg(discuss.getNumber(),discuss.getPhoto1(),discuss.getTitle()));
            hotDiscussMsgList.add(new HotDiscussMsg(discussList.get(0).getNumber(),discussList.get(0).getPhoto1(),discussList.get(0).getTitle()));
            hotDiscussMsgList.add(new HotDiscussMsg(discussList.get(1).getNumber(),discussList.get(1).getPhoto1(),discussList.get(1).getTitle()));
        }else if(hotDiscussNumber3 == 0L){
            Discuss discuss = discussMapper.selectById(hotDiscussNumber1);
            Discuss discuss1 = discussMapper.selectById(hotDiscussNumber2);
            hotDiscussMsgList.add(new HotDiscussMsg(discuss.getNumber(),discuss.getPhoto1(),discuss.getTitle()));
            hotDiscussMsgList.add(new HotDiscussMsg(discuss1.getNumber(),discuss1.getPhoto1(),discuss1.getTitle()));
            hotDiscussMsgList.add(new HotDiscussMsg(discussList.get(0).getNumber(),discussList.get(0).getPhoto1(),discussList.get(0).getTitle()));
        }else{
            Discuss discuss = discussMapper.selectById(hotDiscussNumber1);
            Discuss discuss1 = discussMapper.selectById(hotDiscussNumber2);
            Discuss discuss2 = discussMapper.selectById(hotDiscussNumber3);
            hotDiscussMsgList.add(new HotDiscussMsg(discuss.getNumber(),discuss.getPhoto1(),discuss.getTitle()));
            hotDiscussMsgList.add(new HotDiscussMsg(discuss1.getNumber(),discuss1.getPhoto1(),discuss1.getTitle()));
            hotDiscussMsgList.add(new HotDiscussMsg(discuss2.getNumber(),discuss2.getPhoto1(),discuss2.getTitle()));
        }
        /*
        if(status == 0){
            //都有图片
            log.info("都有图片呢~");
            Discuss discuss = discussMapper.selectById(hotDiscussNumber1);
            Discuss discuss1 = discussMapper.selectById(hotDiscussNumber2);
            Discuss discuss2 = discussMapper.selectById(hotDiscussNumber3);
            HotDiscussMsg hotDiscussMsg = new HotDiscussMsg(discuss.getNumber(),discuss.getPhoto1(),discuss.getTitle(),discuss1.getNumber(),
                    discuss1.getPhoto1(),discuss1.getTitle(),discuss2.getNumber(),discuss2.getPhoto1(),discuss2.getTitle());
            jsonObject.put("hotDiscuss",hotDiscussMsg);
        }else if(status == 1){
            //第三个没图
            Discuss discuss = discussMapper.selectById(hotDiscussNumber1);
            Discuss discuss1 = discussMapper.selectById(hotDiscussNumber2);
            QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("scan_counts");
            log.info("正在找第三个的图片");
            List<Discuss> discussList = discussMapper.selectList(wrapper);
            Discuss discuss2 = discussList.get(0);
            HotDiscussMsg hotDiscussMsg = new HotDiscussMsg(discuss.getNumber(),discuss.getPhoto1(),discuss.getTitle(),discuss1.getNumber(),
                    discuss1.getPhoto1(),discuss1.getTitle(),discuss2.getNumber(),discuss2.getPhoto1(),discuss2.getTitle());
            jsonObject.put("hotDiscuss",hotDiscussMsg);
        }else if(status == 2){
            //第二和第三没图
            Discuss discuss = discussMapper.selectById(hotDiscussNumber1);
            QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("scan_counts");
            log.info("正在找第二和第三的图片");
            List<Discuss> discussList = discussMapper.selectList(wrapper);
            Discuss discuss1 = discussList.get(0);
            Discuss discuss2 = discussList.get(1);
            HotDiscussMsg hotDiscussMsg = new HotDiscussMsg(discuss.getNumber(),discuss.getPhoto1(),discuss.getTitle(),discuss1.getNumber(),
                    discuss1.getPhoto1(),discuss1.getTitle(),discuss2.getNumber(),discuss2.getPhoto1(),discuss2.getTitle());
            jsonObject.put("hotDiscuss",hotDiscussMsg);
        }else{
            //都没图
            QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("scan_counts");
            log.info("正在找三张图");
            List<Discuss> discussList = discussMapper.selectList(wrapper);
            Discuss discuss = discussList.get(0);
            Discuss discuss1 = discussList.get(1);
            Discuss discuss2 = discussList.get(2);
            HotDiscussMsg hotDiscussMsg = new HotDiscussMsg(discuss.getNumber(),discuss.getPhoto1(),discuss.getTitle(),discuss1.getNumber(),
                    discuss1.getPhoto1(),discuss1.getTitle(),discuss2.getNumber(),discuss2.getPhoto1(),discuss2.getTitle());
            jsonObject.put("hotDiscuss",hotDiscussMsg);
        }

         */
        jsonObject.put("hotDiscussList",hotDiscussMsgList);
        log.info("获取主页面图片信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getFirstDiscuss(Long number, Integer cnt,Long id) {
        JSONObject jsonObject = new JSONObject();
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.error("获取第一页面失败，帖子不存在");
            return null;
        }
        User user = userMapper.selectById(discuss.getFromId());
        OwnerCommentMsg ownerCommentMsg = new OwnerCommentMsg(discuss.getNumber(),user.getId(),user.getUsername(),user.getPhoto(),user.getSex(),discuss.getTitle(),
                discuss.getDescription(),discuss.getPhoto1(),discuss.getPhoto2(),discuss.getPhoto3(),discuss.getLikeCounts(),discuss.getCommentCounts(),discuss.getFavorCounts(),discuss.getScanCounts(),discuss.getCreateTime(),discuss.getDeleted());
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("discuss_number",number)
                .orderByDesc("like_counts");
        List<Comment> commentList = commentMapper.selectList(wrapper);
        List<FirstCommentMsg> firstCommentMsgList = new LinkedList<>();
        int ck = 0;
        for(Comment x:commentList){
            ck ++;
            User user1 = userMapper.selectById(x.getId());
            FirstCommentMsg firstCommentMsg = new FirstCommentMsg(x.getId(),user1.getUsername(),user1.getPhoto(),user1.getVip(),x.getNumber(),x.getComments(),x.getLikeCounts(),x.getCreateTime(),x.getDeleted());
            firstCommentMsgList.add(firstCommentMsg);
            if(ck == cnt){
                break;
            }
        }
        //保存历史记录
        if(id != null){
            QueryWrapper<HistoryDiscuss> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("id",id)
                    .eq("number",number);
            HistoryDiscuss historyDiscuss = historyDiscussMapper.selectOne(wrapper1);
            if(historyDiscuss != null){
                //有历史记录
                historyDiscuss.setFavorCounts(discuss.getFavorCounts());
                historyDiscuss.setCommentCounts(discuss.getCommentCounts());
                //这个bug
                historyDiscussMapper.update(historyDiscuss,wrapper1);
                log.info("更新历史记录成功");
            }else{
                //没历史记录，创建一个
                HistoryDiscuss historyDiscuss1 = new HistoryDiscuss(number,id,discuss.getCommentCounts(),discuss.getFavorCounts(),null,null);
                historyDiscussMapper.insert(historyDiscuss1);
                log.info("添加历史记录成功");
            }
        }
        jsonObject.put("ownerComment",ownerCommentMsg);
        jsonObject.put("firstCommentList",firstCommentMsgList);
        log.info("获取首页帖子成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getSecondDiscuss(Long number, Long cnt, Long page, Integer isOrderByHot) {
        JSONObject jsonObject = new JSONObject();
        Discuss discuss = discussMapper.selectById(number);
        if(discuss == null){
            log.info("获取二级评论失败，帖子不存在");
            return null;
        }
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("discuss_number",number)
                .eq("father_number",0);
        if(isOrderByHot == 1){
            wrapper.orderByDesc("like_counts");
        }else{
            wrapper.orderByDesc("create_time");
        }
        Page<Comment> page1 = new Page<>(page,cnt);
        commentMapper.selectPage(page1,wrapper);
        List<Comment> commentList = page1.getRecords();
        List<SecondCommentMsg> commentMsgList = new LinkedList<>();
        for(Comment x:commentList){
            //对每个评论找到下面的评论
            QueryWrapper<Comment> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("father_number",x.getNumber())
                    .orderByDesc("like_counts");
            User user = userMapper.selectById(x.getId());
            SecondCommentMsg secondCommentMsg = new SecondCommentMsg(x.getNumber(),x.getId(),user.getUsername(),user.getPhoto(),user.getVip(),x.getComments(),x.getLikeCounts(),x.getCommentCounts(),
                    x.getCreateTime(),null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);
            List<Comment> commentList1 = commentMapper.selectList(wrapper1);
            int ck = 0;
            for(Comment x1:commentList1){
                ck ++;
                if(ck == 1){
                    User user1 = userMapper.selectById(x1.getId());
                    secondCommentMsg.setReplyNumber1(x1.getNumber());
                    secondCommentMsg.setReplyId1(user1.getId());
                    secondCommentMsg.setReplyPhoto1(user1.getPhoto());
                    secondCommentMsg.setReplyUsername1(user1.getUsername());
                    secondCommentMsg.setReplyVip1(user1.getVip());
                    secondCommentMsg.setReplyDescription1(x1.getComments());
                    secondCommentMsg.setReplyCreateTime1(x1.getCreateTime());
                    secondCommentMsg.setReplyLikeCounts1(x1.getLikeCounts());
                    if(x1.getReplyNumber() != null && x1.getReplyNumber() != 0L){
                        //有回复别人
                        Comment comment = commentMapper.selectById(x1.getReplyNumber());
                        User user2 = userMapper.selectById(comment.getId());
                        secondCommentMsg.setSecondReplyUsername1(user2.getUsername());
                    }
                }else if(ck == 2){
                    User user1 = userMapper.selectById(x1.getId());
                    secondCommentMsg.setReplyNumber2(x1.getNumber());
                    secondCommentMsg.setReplyId2(user1.getId());
                    secondCommentMsg.setReplyPhoto2(user1.getPhoto());
                    secondCommentMsg.setReplyUsername2(user1.getUsername());
                    secondCommentMsg.setReplyVip2(user1.getVip());
                    secondCommentMsg.setReplyDescription2(x1.getComments());
                    secondCommentMsg.setReplyCreateTime2(x1.getCreateTime());
                    secondCommentMsg.setReplyLikeCounts2(x1.getLikeCounts());
                    if(x1.getReplyNumber() != null && x1.getReplyNumber() != 0L){
                        //有回复别人
                        Comment comment = commentMapper.selectById(x1.getReplyNumber());
                        User user2 = userMapper.selectById(comment.getId());
                        secondCommentMsg.setSecondReplyUsername2(user2.getUsername());
                    }
                }else{
                    break;
                }
            }
            commentMsgList.add(secondCommentMsg);
        }
        jsonObject.put("secondCommentList",commentMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取第二页面评论成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getThirdDiscuss(Long number, Long cnt, Long page) {
        JSONObject jsonObject = new JSONObject();
        Comment comment = commentMapper.selectById(number);
        if(comment == null){
            log.error("获取三级评论失败，父级评论不存在");
            return null;
        }
        User user = userMapper.selectById(comment.getId());
        OwnerCommentMsg1 ownerCommentMsg1 = new OwnerCommentMsg1(comment.getNumber(),user.getId(),user.getUsername(),user.getPhoto(),user.getVip(),comment.getComments(),
                comment.getCommentCounts(),comment.getLikeCounts(),comment.getCreateTime());
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("father_number",number)
                .orderByAsc("create_time");
        Page<Comment> page1 = new Page<>(page,cnt);
        commentMapper.selectPage(page1,wrapper);
        List<Comment> commentList = page1.getRecords();
        List<ThirdCommentMsg> commentMsgList = new LinkedList<>();
        for(Comment x:commentList){
            User user1 = userMapper.selectById(x.getId());
            ThirdCommentMsg thirdCommentMsg = new ThirdCommentMsg(x.getNumber(),user1.getId(),user1.getUsername(),user1.getPhoto(),user1.getVip(),x.getComments(),x.getLikeCounts(),x.getReplyNumber(),
                    null,null,x.getCreateTime());
            //获取回复信息
            if(x.getReplyNumber() != null && x.getReplyNumber() != 0L){
                Comment comment1 = commentMapper.selectById(x.getReplyNumber());
                User user2 = userMapper.selectById(comment1.getId());
                thirdCommentMsg.setReplyId(comment1.getId());
                thirdCommentMsg.setReplyUsername(user2.getUsername());
            }
            commentMsgList.add(thirdCommentMsg);
        }
        jsonObject.put("thirdCommentList",commentMsgList);
        jsonObject.put("ownerComment",ownerCommentMsg1);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        return jsonObject;
    }


    @Override
    public JSONObject judgeCommentLike(Long id, Long number) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("number",number);
        Comment comment = commentMapper.selectOne(wrapper);
        if(comment == null){
            log.error("获取评论点赞状态失败，评论不存在");
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<CommentLikes> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("number",number)
                .eq("id",id);
        CommentLikes commentLikes = commentLikesMapper.selectOne(wrapper1);
        if(commentLikes == null){
            log.info("获取评论点赞状态成功，评论未被点赞");
            jsonObject.put("isLike",0);
        }else{
            log.info("获取评论点赞状态成功，评论已被点赞");
            jsonObject.put("isLike",1);
        }
        return jsonObject;
    }


    @Override
    public JSONObject getHotKeyword() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hotKeywordList",DiscussServiceImpl.hotDiscussKeyWord);
        log.info("获取热词列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getReopenDate(Long id) {
        JSONObject jsonObject = new JSONObject();
        User user = userMapper.selectById(id);
        jsonObject.put("reSpeakDate",user.getNoSpeakDate());
        return jsonObject;
    }


    @Override
    public JSONObject getOnePersonDiscuss(Long id,Long cnt,Long page) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",id)
                .orderByDesc("create_time");
        Page<Discuss> page1 = new Page<>(page,cnt);
        discussMapper.selectPage(page1,wrapper);
        List<Discuss> discussList = page1.getRecords();
        List<DiscussMsg> discussMsgList = new LinkedList<>();
        for(Discuss x:discussList){
            User user = userMapper.selectById(x.getFromId());
            discussMsgList.add(new DiscussMsg(x.getNumber(),x.getPhoto1(),user.getUsername(),user.getPhoto(),x.getTitle(),
                    x.getDescription(),x.getLikeCounts(),x.getCommentCounts(),x.getFavorCounts(),x.getScanCounts(),x.getUpdateTime()));
        }
        jsonObject.put("discussList",discussMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取个人帖子列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getFavorDiscuss(Long id, Long cnt, Long page) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<FavorDiscuss> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id)
                .orderByDesc("create_time");
        Page<FavorDiscuss> page1 = new Page<>(page,cnt);
        favorDiscussMapper.selectPage(page1,wrapper);
        List<FavorDiscuss> favorDiscussList = page1.getRecords();
        List<DiscussMsg> discussMsgList = new LinkedList<>();
        for(FavorDiscuss x:favorDiscussList){
            User user = userMapper.selectById(x.getId());
            Discuss discuss = discussMapper.selectDiscussWhenDeleted(x.getNumber());
            discussMsgList.add(new DiscussMsg(discuss.getNumber(),discuss.getPhoto1(),user.getUsername(),user.getPhoto(),discuss.getTitle(),
                    discuss.getDescription(),discuss.getLikeCounts(),discuss.getCommentCounts(),discuss.getFavorCounts(),discuss.getScanCounts(),discuss.getUpdateTime()));
        }
        jsonObject.put("discussList",discussMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取收藏帖子列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getFirstPageDiscuss(Long cnt) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        List<Discuss> discussList = discussMapper.selectList(wrapper);
        //取随机数
        if(cnt > discussList.size()){
            log.error("需求大于帖子数");
            return null;
        }
        Random random = new Random();
        List<DiscussMsg> discussMsgList = new LinkedList<>();
        HashSet<Integer> hashSet = new HashSet<>();
        while(cnt != 0){
            //获取一个随机数
            int ck = random.nextInt(discussList.size() - 1);
            if(hashSet.contains(ck)){
                //和以前重复
                log.info("发现重复的ck：" + ck);
            }else{
                //加入hashset
                hashSet.add(ck);
                Discuss discuss = discussList.get(ck);
                User user = userMapper.selectById(discuss.getFromId());
                discussMsgList.add(new DiscussMsg(discuss.getNumber(),discuss.getPhoto1(),user.getUsername(),user.getPhoto(),discuss.getTitle(),discuss.getDescription(),
                        discuss.getLikeCounts(),discuss.getCommentCounts(),discuss.getFavorCounts(),discuss.getScanCounts(),discuss.getUpdateTime()));
                cnt --;
            }
        }
        jsonObject.put("discussList",discussMsgList);
        log.info("获取主页面随机帖子信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }

}