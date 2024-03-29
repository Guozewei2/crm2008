package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.commons.contants.Contants;
import com.bjpowernode.crm.commons.domain.ReturnObject;
import com.bjpowernode.crm.commons.utils.DateUtils;
import com.bjpowernode.crm.commons.utils.UUIDUtils;
import com.bjpowernode.crm.settings.domain.DicValue;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.DicValueService;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.Clue;
import com.bjpowernode.crm.workbench.domain.ClueActivityRelation;
import com.bjpowernode.crm.workbench.domain.ClueRemark;
import com.bjpowernode.crm.workbench.service.ActivityService;
import com.bjpowernode.crm.workbench.service.ClueActivityRelationService;
import com.bjpowernode.crm.workbench.service.ClueRemarkService;
import com.bjpowernode.crm.workbench.service.ClueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ClueController {
    @Autowired
    private UserService userService;
    @Autowired
    private DicValueService dicValueService;
    @Autowired
    private ClueService clueService;
    @Autowired
    private ClueRemarkService clueRemarkService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ClueActivityRelationService clueActivityRelationService;

    @RequestMapping("/workbench/clue/index.do")
    public String index(HttpServletRequest request) {
        // 调用service来查询数据
        List<User> userList = userService.queryAllUsers();
        List<DicValue> appellationList = dicValueService.queryDicValueByTypeCode("appellation");
        List<DicValue> clueStateList = dicValueService.queryDicValueByTypeCode("clueState");
        List<DicValue> sourceList = dicValueService.queryDicValueByTypeCode("source");
        // 把数据保存到作用域中
        request.setAttribute("userList", userList);
        request.setAttribute("appellationList", appellationList);
        request.setAttribute("clueStateList", clueStateList);
        request.setAttribute("sourceList", sourceList);
        // 请求转发
        return "workbench/clue/index";
    }

    @RequestMapping("/workbench/clue/saveCreateClue.do")
    public @ResponseBody Object saveCreateClue(Clue clue, HttpSession session) {
        ReturnObject returnObject = new ReturnObject();
        User user = (User) session.getAttribute(Contants.SESSION_USER);
        // 封装参数
        clue.setId(UUIDUtils.getUUID());
        clue.setCreateTime(DateUtils.formateDateTime(new Date()));
        clue.setCreateBy(user.getId());
        try {
            // 调用service层，保存创建的线索
            int ret = clueService.saveCreateClue(clue);
            if (ret > 0) {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            } else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试");
        }
        return returnObject;
    }

    @RequestMapping("/workbench/clue/detailClue.do")
    public String detailClue(String id, HttpServletRequest request) {
        // 调用service方法查询数据
        Clue clue = clueService.queryClueForDetailById(id);
        List<ClueRemark> remarkList = clueRemarkService.queryClueRemarkForDetailByClueId(clue.getId());
        List<Activity> activityList = activityService.queryActivityForDetailByClueId(clue.getId());
        // 将数据保存到request作用域中
        request.setAttribute("clue", clue);
        request.setAttribute("remarkList", remarkList);
        request.setAttribute("activityList", activityList);
        return "workbench/clue/detail";
    }

    @RequestMapping("/workbench/clue/queryActivityDetailByNameClue.do")
    public @ResponseBody Object queryActivityDetailByNameClue(String activityName, String clueId) {
        Map<String, Object> map = new HashMap<>();
        // 封装参数
        map.put("activityName", activityName);
        map.put("clueId", clueId);
        // 调用service层方，查询市场活动
        List<Activity> activityList = activityService.queryActivityForDetailByNameClueId(map);
        // 根据查询结果返回响应信息
        return activityList;
    }

    @RequestMapping("/workbench/clue/saveBund.do")
    public @ResponseBody Object saveBund(String[] activityId, String clueId) {
        ReturnObject returnObject = new ReturnObject();
        ClueActivityRelation clueActivityRelation = null;
        ArrayList<ClueActivityRelation> relationList = new ArrayList<>();
        // 封装参数
        for (String ai : activityId) {
            clueActivityRelation = new ClueActivityRelation();
            clueActivityRelation.setActivityId(ai);
            clueActivityRelation.setClueId(clueId);
            clueActivityRelation.setId(UUIDUtils.getUUID());
            relationList.add(clueActivityRelation);
        }
        try {
            // 调用service层方法，批量保存线索和市场活动的关联关系
            int ret = clueActivityRelationService.saveCreateClueActivityRelationByList(relationList);
            if (ret > 0) {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
                List<Activity> activityList = activityService.queryActivityForDetailByIds(activityId);
                returnObject.setRetData(activityList);
            } else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后....");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后....");
        }
        return returnObject;
    }

    @RequestMapping("/workbench/clue/saveUnbund.do")
    public @ResponseBody Object saveUnbund(ClueActivityRelation relation) {
        ReturnObject returnObject = new ReturnObject();
        try {
            // 调用service层方法，删除线索和市场活动关联关系
            int ret = clueActivityRelationService.deleteClueActivityRelationByClueIdActivityId(relation);
            if (ret > 0) {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            } else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后....");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后....");
        }
        return returnObject;
    }

    @RequestMapping("/workbench/clue/toConvert.do")
    public String toConvert(String id, HttpServletRequest request) {
        // 调用service层方法查询线索的关键信息
        Clue clue = clueService.queryClueForDetailById(id);
        List<DicValue> stageList = dicValueService.queryDicValueByTypeCode("stage");
        // 把数据保存到作用域中
        request.setAttribute("clue", clue);
        request.setAttribute("stageList", stageList);
        // 根据查询结果生成响应信息
        return "workbench/clue/convert";
    }

    @RequestMapping("/workbench/clue/queryActivityForConvertByNameClueId.do")
    public @ResponseBody Object queryActivityForConvertByNameClueId(String activityName, String clueId) {
        // 封装参数
        Map<String, Object> map = new HashMap<>();
        map.put("activityName", activityName);
        map.put("clueId", clueId);
        // 调用service层方法，查询市场活动
        List<Activity> activityList = activityService.queryActivityForConvertByNameClueId(map);
        // 根据查询结果，返回响应信息
        return activityList;
    }

    @RequestMapping("/workbench/clue/convertClue.do")
    public @ResponseBody Object convertClue(String clueId, String money, String name, String expectedDate, String stage, String activityId, String isCreateTran, HttpSession session) {
        User user = (User) session.getAttribute(Contants.SESSION_USER);
        ReturnObject returnObject = new ReturnObject();
        // 封装参数
        Map<String, Object> map = new HashMap<>();
        map.put("clueId", clueId);
        map.put("money", money);
        map.put("name", name);
        map.put("expectedDate", expectedDate);
        map.put("stage", stage);
        map.put("activityId", activityId);
        map.put("isCreateTran", isCreateTran);
        map.put(Contants.SESSION_USER, user);
        try {
            // 调用service层方法,保存线索转换
            clueService.saveConvertClue(map);
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试....");
        }
        return returnObject;
    }
}
