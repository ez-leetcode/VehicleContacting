package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.mapper.*;
import com.vehiclecontacting.msg.*;
import com.vehiclecontacting.pojo.*;
import com.vehiclecontacting.utils.JwtUtils;
import com.vehiclecontacting.utils.OssUtils;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@Service
@Slf4j
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private HistoryDiscussMapper historyDiscussMapper;

    @Autowired
    private ComplainUserMapper complainUserMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private DiscussMapper discussMapper;

    @Autowired
    private BlackUserMapper blackUserMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private PostFriendMapper postFriendMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Override
    public String register(String phone, String code,String password) {
        String redisCode = redisUtils.getValue("1_" + phone);
        if(redisCode == null){
            log.error("注册失败，验证码不存在或已过期");
            return "existWrong";
        }
        if(!redisCode.equals(code)){
            log.error("注册失败，验证码不正确");
            return "codeWrong";
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        if(user != null){
            log.error("注册失败，该手机号已被人绑定");
            return "repeatWrong";
        }
        //成功注册
        User user1 = new User();
        user1.setPhone(phone);
        user1.setUsername(phone);
        //密码加密后存入
        user1.setPassword(new BCryptPasswordEncoder().encode(password));
        //创建新用户
        userMapper.insert(user1);
        //获取自动生成的id
        User user2 = userMapper.selectOne(wrapper);
        //给用户一个用户角色
        userRoleMapper.insert(new UserRole(null,user2.getId(),1,null,null));
        log.info("用户注册成功");
        log.info(user2.toString());
        //让验证码失效
        redisUtils.delete("1_" + phone);
        return "success";
    }

    @Override
    public String judgeCode(String phone, Integer type) {
        String sendCount = redisUtils.getValue("sendCode_" + phone);
        int cnt = 0;
        if(sendCount != null){
            cnt = Integer.parseInt(sendCount);
        }
        if(cnt >= 5){
            //短时间内发送短信验证码次数过多
            log.warn("用户2小时内发送短信验证码次数过多，账号已被锁定：" + phone);
            return "repeatWrong";
        }
        return "success";
    }


    @Override
    public String patchUser(Long id, String username, String sex,String introduction) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("修改用户信息失败，用户不存在：" + id);
            return "existWrong";
        }
        User user1 = new User();
        user1.setUsername(username);
        user1.setSex(sex);
        user1.setId(id);
        user1.setIntroduction(introduction);
        int result = userMapper.updateById(user1);
        if(result == 0){
            log.warn("修改用户信息出现问题，可能是重复请求：" + id);
            return "repeatWrong";
        }
        log.info("修改用户信息成功");
        return "success";
    }

    @Override
    public User getUser(Long id,String phone) {
        log.info("正在获取用户信息：" + id);
        User user;
        if(phone == null){
            user = userMapper.selectById(id);
        }else{
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("phone",phone);
            user = userMapper.selectOne(wrapper);
        }
        if(user == null){
            log.warn("获取用户信息失败，用户不存在");
            return null;
        }
        log.info("获取用户信息成功，用户信息：" + user.toString());
        return user;
    }

    @Override
    public String loginByCode(String phone, String code) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.warn("登录失败，用户不存在");
            String redisCode = redisUtils.getValue("4_" + phone);
            if(redisCode == null){
                log.error("登录失败，验证码不存在");
                return "codeExistWrong";
            }
            if(!redisCode.equals(code.toLowerCase())){
                log.error("登录失败，验证码错误");
                return "codeWrong";
            }
            //账号不存在，当场创建一个账户
            User user1 = new User();
            user1.setUsername(phone);
            user1.setPhone(phone);
            user1.setSex("女");
            user1.setPassword(new BCryptPasswordEncoder().encode(phone));
            userMapper.insert(user1);
            log.info("创建新用户成功");
            log.info("短信登录验证成功（还帮人家注册了）");
            redisUtils.delete("4_" + phone);
            return "success";
        }
        if(user.getFrozenDate() != null && user.getFrozenDate().after(new Date())){
            log.error("登录失败用户已被封禁");
            return user.getFrozenDate().toString();
        }
        String redisCode = redisUtils.getValue("4_" + phone);
        log.info("正在校验验证码");
        log.info("code：" + code);
        log.info("redisCode" + redisCode);
        if(redisCode == null){
            log.error("登录失败，验证码不存在或已失效");
            return "codeExistWrong";
        }
        if(!redisCode.equals(code.toLowerCase())){
            log.error("登录失败，验证码错误");
            return "codeWrong";
        }
        log.info("短信登录验证成功");
        redisUtils.delete("4_" + phone);
        return "success";
    }


    @Override
    public JSONObject generateToken(String phone) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        String token = JwtUtils.createToken(user.getId().toString(),user.getPassword());
        log.info("已生成token：" + token);
        log.info("用户：" + user.getUsername());
        //token生成后存入redis
        redisUtils.saveByHoursTime(user.getId().toString(),token,999);
        jsonObject.put("token",token);
        return jsonObject;
    }


    @Override
    public String changePassword(String phone, String code, String oldPassword, String newPassword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.error("修改密码失败，用户不存在：" + phone);
            return "existWrong";
        }
        boolean judge = new BCryptPasswordEncoder().matches(oldPassword,user.getPassword());
        if(!judge){
            //旧密码错误
            log.error("修改密码失败，用户旧密码错误");
            return "oldPasswordWrong";
        }
        //从数据库取密码
        String redisCode = redisUtils.getValue("2_" + phone);
        log.info("code：" + code);
        log.info("redisCode：" + redisCode);
        if(redisCode == null){
            log.error("修改密码失败，验证码不存在");
            return "codeExistWrong";
        }
        if(!redisCode.equals(code.toLowerCase())){
            log.error("修改密码失败，验证码错误");
            return "codeWrong";
        }
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userMapper.updateById(user);
        log.info("修改密码成功");
        redisUtils.delete("2_" + phone);
        return "success";
    }

    @Override
    public String findPassword(String phone, String code, String newPassword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.error("找回密码失败，用户不存在：" + phone);
            return "existWrong";
        }
        String redisCode = redisUtils.getValue("3_" + phone);
        log.info("code：" + code);
        log.info("redisCode：" + redisCode);
        if(redisCode == null){
            log.error("找回密码失败，验证码不存在");
            return "codeExistWrong";
        }
        if(!redisCode.equals(code.toLowerCase())){
            log.error("找回密码失败，验证码错误");
            return "codeWrong";
        }
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userMapper.updateById(user);
        log.info("找回密码成功");
        //删除验证码
        redisUtils.delete("3_" + phone);
        return "success";
    }

    @Override
    public String uploadPhoto(MultipartFile file, String id) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.warn("上传头像失败，用户不存在：" + id);
            return "existWrong";
        }
        //上传阿里云oss
        String url = OssUtils.uploadPhoto(file,"userPhoto");
        if(url.length() < 12){
            //少于12说明上传报错了
            return url;
        }
        //上传成功后先删除源文件
        if(user.getPhoto() != null){
            //删除原头像文件
            log.info("正在删除原头像文件：" + user.getPhoto());
            String lastObjectName = user.getPhoto().substring(user.getPhoto().lastIndexOf("/") + 1);
            log.info("原文件名：" + lastObjectName);
            OssUtils.deletePhoto(lastObjectName,"userPhoto");
        }
        user.setPhoto(url);
        userMapper.updateById(user);
        log.info("更新头像路径成功：" + user.getPhoto());
        return user.getPhoto();
    }


    @Override
    public String addFans(Long fromId, Long toId) {
        User user = userMapper.selectById(fromId);
        User user1 = userMapper.selectById(toId);
        if(user == null || user1 == null){
            log.error("添加关注失败，用户不存在");
            return "existWrong";
        }
        QueryWrapper<Fans> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("from_id",fromId)
                .eq("to_id",toId);
        Fans fans = fansMapper.selectOne(wrapper1);
        if(fans != null){
            //已添加关注
            log.error("添加关注失败，用户已添加关注");
            return "repeatWrong";
        }
        //添加关注
        Fans fans1 = new Fans(fromId,toId,null);
        fansMapper.insert(fans1);
        log.info("添加关注列表成功");
        //更新关注数和粉丝数，可能可以推到消息队列
        user.setFollowCounts(user.getFollowCounts() + 1);
        user1.setFansCounts(user1.getFansCounts() + 1);
        userMapper.updateById(user);
        userMapper.updateById(user1);
        log.info("添加用户关注成功");
        return "success";
    }

    @Override
    public String removeFans(Long fromId, Long toId) {
        User user = userMapper.selectById(fromId);
        User user1 = userMapper.selectById(toId);
        if(user == null || user1 == null){
            log.error("移除关注失败，用户不存在");
            return "existWrong";
        }
        QueryWrapper<Fans> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",fromId)
                .eq("to_id",toId);
        Fans fans = fansMapper.selectOne(wrapper);
        if(fans == null){
            //未添加关注
            log.error("移除关注失败，用户并未关注");
            return "repeatWrong";
        }
        //移除关注
        fansMapper.delete(wrapper);
        user.setFollowCounts(user.getFollowCounts() - 1);
        user1.setFansCounts(user1.getFansCounts() - 1);
        userMapper.updateById(user);
        userMapper.updateById(user1);
        log.info("移除用户关注成功");
        return "success";
    }


    @Override
    public JSONObject getFans(Long id, Long cnt, Long page, String keyword) {
        JSONObject jsonObject = new JSONObject();
        //自己写一个牛一点的
        Page<Fans> page1 = new Page<>(page,cnt);
        List<Fans> fansList;
        if(keyword == null || keyword.equals("")){
            //没关键词
            fansList = fansMapper.getFans(page1,id);
        }else{
            //有关键词
            fansList = fansMapper.getFansByKeyword(page1,keyword,id);
        }
        List<FansMsg> fansMsgList = new LinkedList<>();
        for(Fans x:fansList){
            User user = userMapper.selectById(x.getFromId());
            fansMsgList.add(new FansMsg(user.getId(),user.getUsername(),user.getSex(),user.getVip(),user.getPhoto(),user.getIntroduction(),x.getCreateTime()));
        }
        jsonObject.put("fansList",fansMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取粉丝列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject getFollow(Long id, Long cnt, Long page, String keyword) {
        JSONObject jsonObject = new JSONObject();
        Page<Fans> page1 = new Page<>(page,cnt);
        List<Fans> fansList;
        if(keyword == null || keyword.equals("")){
            //没关键词
            fansList = fansMapper.getFollow(page1,id);
        }else{
            fansList = fansMapper.getFansByKeyword(page1,keyword,id);
        }
        List<FansMsg> fansMsgList = new LinkedList<>();
        for(Fans x:fansList){
            User user = userMapper.selectById(x.getToId());
            fansMsgList.add(new FansMsg(user.getId(),user.getUsername(),user.getSex(),user.getVip(),user.getPhoto(),user.getIntroduction(),x.getCreateTime()));
        }
        jsonObject.put("followList",fansMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取关注列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }

    @Override
    public String changeEmail(Long id, String code,String newEmail) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email",newEmail);
        User user1 = userMapper.selectOne(wrapper);
        if(user1 != null){
            log.error("修改邮箱失败，邮箱已被使用");
            return "repeatWrong";
        }
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("修改邮箱失败，用户不存在");
            return "existWrong";
        }
        String redisCode = redisUtils.getValue("email1_" + newEmail);
        if(redisCode == null){
            log.error("修改邮箱失败，验证码不存在或已失效");
            return "codeExistWrong";
        }
        if(!code.toLowerCase().equals(redisCode)){
            log.error("修改邮箱失败，验证码错误");
            return "codeWrong";
        }
        //验证码正确
        user.setEmail(newEmail);
        userMapper.updateById(user);
        //一个验证码只能用一次
        redisUtils.delete("email1_" + newEmail);
        log.info("修改邮箱成功");
        return "success";
    }

    @Override
    public Integer judgeFavor(Long fromId, Long toId) {
        QueryWrapper<Fans> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",fromId)
                .eq("to_id",toId);
        Fans fans = fansMapper.selectOne(wrapper);
        if(fans == null){
            log.info("用户未关注");
            return 1;
        }
        //用户已关注情况下
        QueryWrapper<Fans> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("from_id",toId)
                .eq("to_id",fromId);
        Fans fans1 = fansMapper.selectOne(wrapper);
        if(fans1 != null){
            log.info("用户已关注");
            return 2;
        }else{
            log.info("用户已相互关注");
            return 3;
        }
    }

    @Override
    public String clearHistory(Long id) {
        QueryWrapper<HistoryDiscuss> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id);
        int result = historyDiscussMapper.delete(wrapper);
        if(result == 0){
            log.warn("清空历史记录失败，可能是重复请求");
            return "repeatWrong";
        }
        log.info("清空历史记录成功");
        return "success";
    }


    @Override
    public String complainUser(Long fromId, Long toId, String title, String description, String complainPhoto1, String complainPhoto2, String complainPhoto3) {
        User user = userMapper.selectById(toId);
        if(user == null){
            //被举报用户不存在
            log.error("举报失败，被举报用户不存在");
            return "existWrong";
        }
        String complainCounts = redisUtils.getValue("complain_" + fromId);
        if(complainCounts != null){
            int cnt = Integer.parseInt(complainCounts);
            //最多24小时内举报5次
            if(cnt > 5){
                log.error("举报用户失败，用户24小时内举报次数过多");
                return "repeatWrong";
            }
        }
        //添加举报内容
        ComplainUser complainUser = new ComplainUser(fromId,toId,title,description,complainPhoto1,complainPhoto2,complainPhoto3,null);
        complainUserMapper.insert(complainUser);
        //加被举报人被举报次数
        user.setComplainCounts(user.getComplainCounts() + 1);
        userMapper.updateById(user);
        //存入redis举报次数
        redisUtils.addKeyByTime("complain_" + fromId ,24);
        return "success";
    }



    @Override
    public String uploadComplain(MultipartFile file, Long id) {
        String url = OssUtils.uploadPhoto(file,"complainPhoto");
        //打印下日志
        if(url.length() < 12){
            log.error("上传投诉图片失败");
        }else{
            log.info("上传投诉图片成功，url：" + url);
        }
        return url;
    }


    @Override
    public JSONObject getHistory(Long id, Long page, Long cnt) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<HistoryDiscuss> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id)
                .orderByDesc("update_time");
        Page<HistoryDiscuss> page1 = new Page<>(page,cnt);
        historyDiscussMapper.selectPage(page1,wrapper);
        List<HistoryDiscuss> historyDiscussList = page1.getRecords();
        List<HistoryDiscussMsg> historyDiscussMsgList = new LinkedList<>();
        for(HistoryDiscuss x:historyDiscussList){
            //给数据
            User user = userMapper.selectById(x.getId());
            Discuss discuss = discussMapper.selectDiscussWhenDeleted(x.getNumber());
            historyDiscussMsgList.add(new HistoryDiscussMsg(x.getNumber(),x.getId(),user.getUsername(),user.getPhoto(),discuss.getTitle(),discuss.getDescription(),
                    discuss.getPhoto1(),discuss.getCommentCounts() - x.getCommentCounts(),discuss.getFavorCounts() - x.getFavorCounts(),discuss.getFavorCounts(),discuss.getCommentCounts(),x.getUpdateTime()));
        }
        jsonObject.put("historyList",historyDiscussMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取历史记录成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public String addFeedback(Long id, String title, String description) {
        String feedbackCounts = redisUtils.getValue("feedback_" + id);
        if(feedbackCounts != null){
            log.error("用户反馈失败，用户24小时内反馈次数过多");
            return "repeatWrong";
        }
        //添加反馈
        feedbackMapper.insert(new Feedback(id,title,description,0,null));
        //存入redis反馈次数
        redisUtils.addKeyByTime("feedback_" + id,24);
        log.info("添加反馈成功");
        return "success";
    }


    @Override
    public String addBlack(Long fromId, Long toId) {
        QueryWrapper<BlackUser> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",fromId)
                .eq("to_id",toId);
        BlackUser blackUser = blackUserMapper.selectOne(wrapper);
        if(blackUser != null){
            log.error("添加黑名单失败，用户已被拉黑");
            return "repeatWrong";
        }
        //没被拉黑.拉黑
        blackUserMapper.insert(new BlackUser(fromId,toId,null));
        //添加黑名单数
        User user = userMapper.selectById(fromId);
        user.setBlackCounts(user.getBlackCounts() + 1);
        userMapper.updateById(user);
        log.info("添加黑名单成功");
        return "success";
    }

    @Override
    public String removeBlack(Long fromId, Long toId) {
        QueryWrapper<BlackUser> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",fromId)
                .eq("to_id",toId);
        BlackUser blackUser = blackUserMapper.selectOne(wrapper);
        if(blackUser == null){
            log.error("移除黑名单失败，用户已被移除黑名单");
            return "repeatWrong";
        }
        //没被移除黑名单，移除
        blackUserMapper.delete(wrapper);
        //减少黑名单数
        User user = userMapper.selectById(fromId);
        user.setBlackCounts(user.getBlackCounts() - 1);
        userMapper.updateById(user);
        log.info("移除黑名单成功");
        return "success";
    }


    @Override
    public JSONObject getBlackList(Long id, Long page, Long cnt) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<BlackUser> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",id)
                .orderByDesc("create_time");
        Page<BlackUser> page1 = new Page<>(page,cnt);
        blackUserMapper.selectPage(page1,wrapper);
        List<BlackUser> blackUserList = page1.getRecords();
        List<BlackUserMsg> blackUserMsgList = new LinkedList<>();
        for(BlackUser x:blackUserList){
            User user = userMapper.selectById(x.getToId());
            blackUserMsgList.add(new BlackUserMsg(user.getId(),user.getUsername(),user.getSex(),user.getVip(),user.getPhoto(),user.getIntroduction(),x.getCreateTime()));
        }
        jsonObject.put("blackList",blackUserMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取黑名单列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public String addFriend(Long fromId, Long toId, String reason) {
        //先看是不是在黑名单里
        QueryWrapper<BlackUser> wrapper3 = new QueryWrapper<>();
        wrapper3.eq("from_id",toId)
                .eq("to_id",fromId);
        BlackUser blackUser = blackUserMapper.selectOne(wrapper3);
        if(blackUser != null){
            log.info("加好友失败，你在对方的黑名单内");
            return "blackWrong";
        }
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.eq("id1",fromId)
                .eq("id2",toId)
                .or()
                .eq("id1",toId)
                .eq("id2",fromId);
        Friend friend = friendMapper.selectOne(wrapper);
        if(friend != null){
            log.error("申请加好友失败，你们已经加为好友");
            return "repeatWrong";
        }
        QueryWrapper<PostFriend> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("from_id",toId)
                .eq("to_id",fromId);
        PostFriend postFriend = postFriendMapper.selectOne(wrapper1);
        //看看之前有没有自己的请求
        QueryWrapper<PostFriend> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("from_id",fromId)
                .eq("to_id",toId);
        PostFriend postFriend2 = postFriendMapper.selectOne(wrapper2);
        if(postFriend != null && postFriend.getIsPass() == 0){
            log.info("对方已经申请加好友了，直接成为朋友");
            Friend friend1 = new Friend(fromId,toId,null);
            friendMapper.insert(friend1);
            //成功加好友通知待定
            //通过好友申请，直接pass
            postFriend.setIsPass(1);
            postFriendMapper.updateById(postFriend);
            log.info("通过好友申请成功");
            //生成请求直接通过，之前肯定没请求
            PostFriend postFriend1 = new PostFriend(fromId,toId,reason,1,null,null);
            postFriendMapper.insert(postFriend1);
            log.info("生成好友请求并直接通过成功");
            User user = userMapper.selectById(fromId);
            User user1 = userMapper.selectById(toId);
            user.setFriendCounts(user.getFriendCounts() + 1);
            user1.setFriendCounts(user1.getFriendCounts() + 1);
            userMapper.updateById(user);
            userMapper.updateById(user1);
            log.info("好友数更新成功");
            return "success";
        }
        //看看有没有之前的请求，有就翻新
        if(postFriend2 != null){
            //要考虑之前被拒绝的情况
            postFriend2.setIsPass(0);
            postFriend2.setReason(reason);
            postFriendMapper.update(postFriend2,wrapper2);
            log.info("翻新申请好友请求成功");
        }else{
            //没有就发送请求
            PostFriend postFriend1 = new PostFriend(fromId,toId,reason,0,null,null);
            postFriendMapper.insert(postFriend1);
            log.info("发送请求成功");
        }
        log.info("申请加好友成功");
        return "success";
    }

    @Override
    public String verifyFriend(Long fromId, Long toId, Integer isPass) {
        QueryWrapper<PostFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",fromId)
                .eq("to_id",toId)
                .eq("is_pass",0);
        PostFriend postFriend = postFriendMapper.selectOne(wrapper);
        if(postFriend == null){
            log.error("审核好友申请失败，申请不存在");
            return "existWrong";
        }
        //已审核过
        if(postFriend.getIsPass() != 0){
            log.error("审核好友申请失败，申请已被处理");
            return "repeatWrong";
        }
        //审核好友申请
        log.info("正在审核好友申请");
        log.info(postFriend.toString());
        postFriend.setIsPass(isPass);
        postFriendMapper.update(postFriend,wrapper);
        //如果通过
        if(isPass == 1){
            //更新好友数
            User user = userMapper.selectById(fromId);
            User user1 = userMapper.selectById(toId);
            user.setFriendCounts(user.getFriendCounts() + 1);
            user1.setFriendCounts(user1.getFriendCounts() + 1);
            userMapper.updateById(user);
            userMapper.updateById(user1);
            //插入friend
            Friend friend = new Friend(fromId,toId,null);
            friendMapper.insert(friend);
            log.info("添加好友列表成功");
        }
        //通知待定
        log.info("审核好友申请成功");
        return "success";
    }

    @Override
    public JSONObject getPostFriend(Long id, Long cnt, Long page) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<PostFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id",id)
                .orderByAsc("is_pass")
                .orderByDesc("update_time");
        Page<PostFriend> page1 = new Page<>(page,cnt);
        postFriendMapper.selectPage(page1,wrapper);
        List<PostFriend> postFriendList = page1.getRecords();
        List<PostFriendMsg> postFriendMsgList = new LinkedList<>();
        for(PostFriend x:postFriendList){
            User user = userMapper.selectById(x.getFromId());
            postFriendMsgList.add(new PostFriendMsg(user.getId(),user.getUsername(),user.getSex(),user.getVip(),user.getPhoto(),x.getReason(),x.getIsPass(),x.getUpdateTime()));
        }
        jsonObject.put("postFriendList",postFriendMsgList);
        jsonObject.put("pages",page1.getPages());
        jsonObject.put("counts",page1.getTotal());
        log.info("获取申请好友列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }

    @Override
    public String deleteHistory(Long id, List<Long> numbers) {
        for(Long x:numbers){
            QueryWrapper<HistoryDiscuss> wrapper = new QueryWrapper<>();
            wrapper.eq("number",x)
                    .eq("id",id);
            historyDiscussMapper.delete(wrapper);
        }
        log.info("批量删除帖子成功");
        return "success";
    }


    @Override
    public String deleteFriend(Long fromId, Long toId) {
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.eq("id1",fromId)
                .eq("id2",toId)
                .or()
                .eq("id1",toId)
                .eq("id2",fromId);
        Friend friend = friendMapper.selectOne(wrapper);
        if(friend == null){
            log.error("移除好友失败，并未加好友");
            return "existWrong";
        }
        //加好友了，移除friend
        friendMapper.delete(wrapper);
        //改好友数量
        User user = userMapper.selectById(fromId);
        User user1 = userMapper.selectById(toId);
        user.setFriendCounts(user.getFriendCounts() - 1);
        user1.setFriendCounts(user1.getFriendCounts() - 1);
        userMapper.updateById(user);
        userMapper.updateById(user1);
        log.info("移除好友列表成功");
        //通知待定
        return "success";
    }

    @Override
    public JSONObject getFriendList(Long id, Integer type, Long cnt, Long page) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        //这里可能有问题
        if(type == 1){
            wrapper.eq("id1",id)
                    .orderByDesc("create_time")
                    .or()
                    .eq("id2",id)
                    .orderByDesc("create_time");
        }else{
            wrapper.eq("id1",id)
                    .orderByAsc("id2")
                    .or()
                    .eq("id2",id)
                    .orderByAsc("id1");
        }
        Page<Friend> page1 = new Page<>(page,cnt);
        friendMapper.selectPage(page1,wrapper);
        List<Friend> friendList = page1.getRecords();
        List<FriendMsg> friendMsgList = new LinkedList<>();
        for(Friend x:friendList){
            User user;
            if(x.getId1().equals(id)){
                user = userMapper.selectById(x.getId2());
            }else {
                user = userMapper.selectById(x.getId1());
            }
            friendMsgList.add(new FriendMsg(user.getId(),user.getUsername(),user.getVip(),user.getPhoto(),user.getIntroduction(),x.getCreateTime()));
        }
        jsonObject.put("friendList",friendMsgList);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        log.info("获取好友列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


    @Override
    public JSONObject judgeFriend(Long fromId, Long toId) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.eq("id1",fromId)
                .eq("id2",toId)
                .or()
                .eq("id1",toId)
                .eq("id2",fromId);
        Friend friend = friendMapper.selectOne(wrapper);
        if(friend == null){
            log.info("判断是否为好友成功，他们不是好友");
            jsonObject.put("status",0);
            return jsonObject;
        }
        log.info("判断是否为好友成功，他们是好友");
        jsonObject.put("status",1);
        return jsonObject;
    }

    @Override
    public JSONObject searchUser(String username, Long page, Long cnt) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("username",username)
                .orderByAsc("vip");
        Page<User> page1 = new Page<>(page,cnt);
        userMapper.selectPage(page1,wrapper);
        List<User> userList = page1.getRecords();
        List<UserMsg> userMsgList = new LinkedList<>();
        for(User x:userList){
            userMsgList.add(new UserMsg(x.getId(),x.getUsername(),x.getPhoto(),x.getSex(),x.getVip(),x.getIntroduction()));
        }
        jsonObject.put("userList",userMsgList);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        log.info("搜索用户信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }


}
