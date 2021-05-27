package com.vehiclecontacting.controller;


import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.pojo.Result;
import com.vehiclecontacting.service.DiscussService;
import com.vehiclecontacting.utils.RedisUtils;
import com.vehiclecontacting.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "帖子聊天管理类",protocols = "https")
@Slf4j
@RestController
public class DiscussController {


    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private DiscussService discussService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "title",value = "帖子主题",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "description",value = "主要内容",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "photo1",value = "描述图片1（url）",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "photo2",value = "描述图片2（url）",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "photo3",value = "描述图片3（url）",dataType = "string",paramType = "query")
    })
    @ApiOperation(value = "用户创建新帖子",notes = "repeatWrong：该用户短期创建新帖子太多（2小时5次以上） success：成功")
    @PostMapping("/discuss")
    public Result<JSONObject> generateDiscuss(@RequestParam("id") Long id,@RequestParam("title") String title,
                                            @RequestParam("description") String description,
                                            @RequestParam(value = "photo1",required = false) String photo1,
                                            @RequestParam(value = "photo2",required = false) String photo2,
                                            @RequestParam(value = "photo3",required = false) String photo3){
        log.info("用户正在创建帖子，id：" + id + " title：" + title + " description：" + description);
        String sendCnt = redisUtils.getValue("discuss_" + id);
        int cnt = 0;
        if(sendCnt != null){
            cnt = Integer.parseInt(sendCnt);
        }
        //记得改回去
        if(cnt >= 10){
            //短期发帖太多
            log.warn("创建新帖子失败，用户短时间内发帖太多");
            return ResultUtils.getResult(new JSONObject(),"repeatWrong");
        }
        return ResultUtils.getResult(new JSONObject(), discussService.generateDiscuss(id,title,description,photo1,photo2,photo3));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "帖子主人删帖",notes = "existWrong：帖子不存在（可能是重复删除） userWrong：用户不是帖子的主人 success：成功")
    @DeleteMapping("/discuss")
    public Result<JSONObject> deleteDiscuss(@RequestParam("id") Long id,@RequestParam("number") Long number){
        log.info("用户正在删帖，id：" + id + " number：" + number);
        return ResultUtils.getResult(new JSONObject(),discussService.deleteDiscuss(id,number));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "comments",value = "评论内容",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "fatherNumber",value = "父级评论编号（没有填0）",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "replyNumber",value = "被回复的评论编号（二级评论，没有填0）",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "评论帖子",notes = "existWrong：帖子或评论不存在（可能是主贴，父级评论，被回复评论） success：成功")
    @PostMapping("/comment")
    public Result<JSONObject> addComment(@RequestParam("id") Long id,@RequestParam("comments") String comments,@RequestParam("number") Long number,
                                         @RequestParam("fatherNumber") Long fatherNumber,@RequestParam("replyNumber") Long replyNumber){
        log.info("正在评论帖子，id：" + id + " comments：" + comments + " fatherNumber：" + fatherNumber + " replyNumber：" + replyNumber);
        return ResultUtils.getResult(new JSONObject(),discussService.addComment(id,number,comments,fatherNumber,replyNumber));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id（为了游客能浏览，只有在关注页面要带上）",dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "isOrderByTime",value = "是否按时间排序（0按时间，1按热度）（浏览关注的时候不能按热度！！请填0）",required = true,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "keyword",value = "搜索关键词（获取关注时不支持搜索哦~）",dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "isFollow",value = "是否是关注栏（0是推荐页面 1是关注页面）",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "获取主页面帖子浏览",notes = "success：成功（返回json discussList（主页帖子信息列表） pages（页面总数） counts（帖子总数））")
    @GetMapping("/discuss")
    public Result<JSONObject> getDiscuss(@RequestParam("isOrderByTime") Integer isOrderByTime,@RequestParam(value = "keyword",required = false) String keyword,
                                         @RequestParam("cnt") Long cnt,@RequestParam("page") Long page,
                                         @RequestParam("isFollow") Integer isFollow,
                                         @RequestParam(value = "id",required = false) Long id){
        log.info("正在获取主页面帖子浏览，isOrderByTime：" + isOrderByTime + " keyword：" + keyword + " cnt：" + cnt + " page：" + page + " isFollow：" + isFollow + " id：" + id);
        return ResultUtils.getResult(discussService.getDiscuss(isFollow,isOrderByTime,keyword,cnt,page,id),"success");
    }

/*

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "评论用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "comments",value = "评论内容",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "discussNumber",value = "帖子编号",required = true,dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "fatherNumber",value = "父级评论编号（不是留言下面的留言填0）",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "replyNumber",value = "被回复评论编号（不是留言下面的留言并且还在回复别人填0）",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "用户添加评论",notes = "existWrong：帖子或父级评论或被回复评论不存在 success：成功")
    @PostMapping("/comment")
    public Result<JSONObject> addComment(@RequestParam("id") Long id,@RequestParam("comments") String comments,
                                         @RequestParam("discussNumber") String discussNumber,@RequestParam("fatherNumber") Long fatherNumber,
                                         @RequestParam("replyNumber") Long replyNumber){
        log.info("正在添加用户评论，id：" + id + " comments：" + comments + " discussNumber：" + discussNumber + " fatherNumber：" + fatherNumber + " replyNumber：" + replyNumber);

    }


 */




    @ApiImplicitParams({
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "isOrderByTime",value = "是否按时间顺序（0按时间倒序，1按热度，2按时间正序）",required = true,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取评论列表（一级页面）（不用啦~）",notes = "existWrong：帖子不存在（可能被删了，提示一下） success：成功（返回json ownerComment（楼主的评论和帖子的相关信息） pages（页面总数） counts（帖子总数） commentList（评论列表））")
    @GetMapping("/comment")
    public Result<JSONObject> getComment(@RequestParam("number") Long number,@RequestParam("isOrderByTime") Integer isOrderByTime,
                                         @RequestParam("cnt") Long cnt,@RequestParam("page") Long page){
        log.info("正在获取评论列表（一级页面） number：" + number + " isOrderByTime：" + isOrderByTime + " cnt：" + cnt + " page：" + page);
        JSONObject jsonObject = discussService.getComment(number,cnt,page,isOrderByTime);
        if(jsonObject == null){
            return ResultUtils.getResult(new JSONObject(),"existWrong");
        }
        return ResultUtils.getResult(jsonObject,"success");
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "number",value = "父级评论编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取评论列表（二级页面，和B站一样只能按时间排序）（不用啦~）",notes = "existWrong：评论不存在 success：成功（返回json fatherComment（父级评论） commentList（二级评论） pages（页面总数） counts（评论总数））")
    @GetMapping("/comment1")
    public Result<JSONObject> getComment1(@RequestParam("number") Long number,@RequestParam("cnt") Long cnt,
                                          @RequestParam("page") Long page){
        log.info("正在获取评论列表（二级页面） number：" + number + " cnt：" + cnt + " page：" + page);
        JSONObject jsonObject = discussService.getComment1(number,cnt,page);
        if(jsonObject == null){
            return ResultUtils.getResult(new JSONObject(),"existWrong");
        }
        return ResultUtils.getResult(jsonObject,"success");
    }



    @ApiImplicitParams({
            @ApiImplicitParam(name = "photo",required = true,dataType = "file",paramType = "query")
    })
    @ApiOperation(value = "用户帖子图片上传",notes = "fileWrong：文件为空 typeWrong：文件类型错误 success：成功（返回json带url）")
    @PostMapping("/discussPhoto")
    public Result<JSONObject> photoUpload(@RequestParam("photo") MultipartFile file){
        log.info("正在上传帖子图片");
        String url = discussService.photoUpload(file);
        if(url.length() < 12){
            return ResultUtils.getResult(new JSONObject(),url);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url",url);
        return ResultUtils.getResult(jsonObject,"success");
    }



    @ApiImplicitParams({
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "id",value = "收藏用户id",required = true,dataType = "Long",paramType = "query"),
    })
    @ApiOperation(value = "收藏帖子",notes = "repeatWrong：帖子已被收藏（可能是重复收藏） existWrong：帖子不存在 success：成功")
    @PostMapping("/favorDiscuss")
    public Result<JSONObject> favorDiscuss(@RequestParam("id") Long id,@RequestParam("number") Long number){
        log.info("正在收藏帖子，number：" + number + " id：" + id);
        return ResultUtils.getResult(new JSONObject(),discussService.addFavorDiscuss(number,id));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "id",value = "收藏用户id",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "移除帖子收藏",notes = "repeatWrong：帖子未被收藏（可能是重复请求） existWrong：帖子不存在 success：成功")
    @DeleteMapping("/favorDiscuss")
    public Result<JSONObject> removeDiscuss(@RequestParam("number") Long number,@RequestParam("id") Long id){
        log.info("正在移除帖子收藏，number：" + number + " id：" + id);
        return ResultUtils.getResult(new JSONObject(),discussService.deleteFavorDiscuss(number,id));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "评论编号",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "对评论点赞",notes = "repeatWrong：评论已被点赞（可能是重复请求） existWrong：评论不存在 success：成功")
    @PostMapping("/like")
    public Result<JSONObject> likeComment(@RequestParam("id") Long id,@RequestParam("number") Long number){
        log.info("正在对评论点赞，id：" + id + " number：" + number);
        return ResultUtils.getResult(new JSONObject(), discussService.likeComment(number,id));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "评论编号",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "取消评论点赞",notes = "repeatWrong：评论未被点赞（可能是重复请求） existWrong：评论不存在 success：成功")
    @DeleteMapping("/like")
    public Result<JSONObject> deleteLikeComment(@RequestParam("id") Long id,@RequestParam("number") Long number){
        log.info("正在对评论取消点赞，id：" + id + " number：" + number);
        return ResultUtils.getResult(new JSONObject(),discussService.deleteLikeComment(number,id));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "判断用户是否点赞和收藏帖子",notes = "success：成功 （返回json isLike（是否点赞） isFavor（是否收藏））")
    @PostMapping("/likeAndFavor")
    public Result<JSONObject> judgeLikeAndFavor(@RequestParam("id") Long id,@RequestParam("number") Long number){
        log.info("正在判断用户是否点赞和收藏帖子，id：" + id + " number：" + number);
        return ResultUtils.getResult(discussService.judgeLikeAndFavor(number,id),"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "点赞帖子",notes = "existWrong：帖子不存在 success：成功")
    @PostMapping("/likeDiscuss")
    public Result<JSONObject> likeDiscuss(@RequestParam("id") Long id,@RequestParam("number") Long number){
        log.info("正在点赞帖子，id：" + id + " number：" + number);
        return ResultUtils.getResult(new JSONObject(),discussService.likeDiscuss(number,id));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "取消点赞帖子",notes = "existWrong：帖子不存在 success：成功")
    @DeleteMapping("/likeDiscuss")
    public Result<JSONObject> dislikeDiscuss(@RequestParam("id") Long id,@RequestParam("number") Long number){
        log.info("正在取消点赞帖子，id：" + id + " number：" + number);
        return ResultUtils.getResult(new JSONObject(),discussService.dislikeDiscuss(number,id));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "第一页面下面的评论要几个",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "获取第一个页面的帖子数据",notes = "existWrong：帖子不存在  success：成功 （返回json ownerComment：帖子主人写的内容 firstCommentList：下面的评论列表（2-3个就好））")
    @GetMapping("/firstDiscuss")
    public Result<JSONObject> getFirstDiscuss(@RequestParam("number") Long number,@RequestParam("cnt") Integer cnt){
        log.info("正在获取第一个页面帖子数据，number：" + number + " cnt：" + cnt);
        JSONObject jsonObject = discussService.getFirstDiscuss(number,cnt);
        if(jsonObject == null){
            return ResultUtils.getResult(new JSONObject(),"existWrong");
        }
        return ResultUtils.getResult(jsonObject,"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "number",value = "帖子编号",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "isOrderByHot",value = "是否按评论热度排序 1是 0不是",required = true,dataType = "int",paramType = "query")
    })
    @ApiOperation(value = "获取第二个页面帖子数据（就是评论页面）",notes = "existWrong：帖子不存在 success：成功 （返回json secondCommentList：二级评论列表 pages：页面数 counts：数据总数）")
    @GetMapping("/secondDiscuss")
    public Result<JSONObject> getSecondDiscuss(@RequestParam("number") Long number,@RequestParam("cnt") Long cnt,@RequestParam("page") Long page,
                                               @RequestParam("isOrderByHot") Integer isOrderByHot){
        log.info("正在获取第二个页面帖子数据，number：" + number + " cnt：" + cnt + " page：" + page + " isOrderByHot：" + isOrderByHot);
        JSONObject jsonObject = discussService.getSecondDiscuss(number,cnt,page,isOrderByHot);
        if(jsonObject == null){
            return ResultUtils.getResult(new JSONObject(),"existWrong");
        }
        return ResultUtils.getResult(jsonObject,"success");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "number",value = "评论编号（父级评论编号不是帖子编号，就是用户点进去查看详情的那个评论编号！）",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "cnt",value = "页面数据量",required = true,dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "page",value = "当前页面",required = true,dataType = "Long",paramType = "query")
    })
    @ApiOperation(value = "获取第三页面帖子数据（就是评论的评论页面）",notes = "existWrong：评论不存在 success：成功 （返回json thirdCommentList：三级评论列表 OwnerComment：父级评论（显示在" +
            "最上面） pages：页面数 counts：页面数据量）")
    @GetMapping("/thirdDiscuss")
    public Result<JSONObject> getThirdDiscuss(@RequestParam("number") Long number,@RequestParam("cnt") Long cnt,
                                              @RequestParam("page") Long page){
        log.info("正在获取第三页面帖子数据，number：" + number + " cnt：" + cnt + " page：" + page);
        JSONObject jsonObject = discussService.getThirdDiscuss(number,cnt,page);
        if(jsonObject == null){
            return ResultUtils.getResult(new JSONObject(),"existWrong");
        }
        return ResultUtils.getResult(jsonObject,"success");
    }






    @ApiOperation(value = "获取当前热点帖子",notes = "按照8小时内权重综合排序 浏览量：1 点赞：5 收藏：10 取消收藏/点赞等等会降低权重" +
            "如果找不到合适的，比如刚开服等情况，就给当前浏览量最多的几个 success：返回json hotDiscussList")
    @GetMapping("/hotDiscuss")
    public Result<JSONObject> getHotDiscuss(){
        log.info("正在获取热点帖子");
        return ResultUtils.getResult(discussService.getHotDiscuss(),"success");
    }


}