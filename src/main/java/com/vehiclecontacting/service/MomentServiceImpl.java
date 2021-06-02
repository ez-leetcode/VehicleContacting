package com.vehiclecontacting.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vehiclecontacting.mapper.MomentMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.Moment;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.utils.OssUtils;
import com.vehiclecontacting.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class MomentServiceImpl implements MomentService{

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MomentMapper momentMapper;

    @Override
    public String momentPhotoUpload(MultipartFile file, Long id) {
        String url = OssUtils.uploadPhoto(file,"momentPhoto");
        //打印下日志
        if(url.length() < 12){
            log.error("上传动态图片失败");
            return url;
        }
        log.info("上传动态图片成功");
        return url;
    }

    @Override
    public String generateMoment(Long id, String description, String photo1, String photo2, String photo3) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("生成动态失败，用户不存在");
            return "existWrong";
        }
        String momentCounts = redisUtils.getValue("moment_" + id);
        if(momentCounts != null){
            int cnt = Integer.parseInt(momentCounts);
            if(cnt > 10){
                log.warn("发送动态失败，用户24小时内发送动态次数过多");
                return "repeatWrong";
            }
        }
        //生成动态
        momentMapper.insert(new Moment(null,id,description,0,0,photo1,photo2,photo3,null));
        //更新作者动态数
        user.setMomentCounts(user.getMomentCounts() + 1);
        userMapper.updateById(user);
        //存入redis数量
        redisUtils.addKeyByTime("moment_" + id,24);
        log.info("生成动态成功");
        return "success";
    }


    @Override
    public String deleteMoment(Long id, Long number) {
        User user = userMapper.selectById(id);
        if(user == null){
            log.error("删除动态失败，用户不存在");
            return "userWrong";
        }
        Moment moment = momentMapper.selectById(number);
        if(moment == null){
            log.error("删除动态失败，动态不存在");
            return "existWrong";
        }
        if(!moment.getId().equals(id)){
            log.error("删除动态失败，用户不匹配");
            return "userWrong";
        }
        //删除动态
        momentMapper.deleteById(number);
        //更新动态次数
        user.setMomentCounts(user.getMomentCounts() - 1);
        userMapper.updateById(user);
        log.info("删除动态成功");
        return "success";
    }

    @Override
    public String momentLike(Long id, Long number) {
        QueryWrapper<Moment> wrapper = new QueryWrapper<>();
        return null;
    }





}
