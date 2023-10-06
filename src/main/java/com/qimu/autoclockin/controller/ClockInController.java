package com.qimu.autoclockin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qimu.autoclockin.common.BaseResponse;
import com.qimu.autoclockin.common.ErrorCode;
import com.qimu.autoclockin.common.IdRequest;
import com.qimu.autoclockin.common.ResultUtils;
import com.qimu.autoclockin.exception.BusinessException;
import com.qimu.autoclockin.model.entity.ClockInInfo;
import com.qimu.autoclockin.model.entity.User;
import com.qimu.autoclockin.model.enums.ClockInStatusEnum;
import com.qimu.autoclockin.model.vo.ClockInInfoVo;
import com.qimu.autoclockin.service.ClockInInfoService;
import com.qimu.autoclockin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static com.qimu.autoclockin.constant.ClockInConstant.SIGN_USER_GROUP;

/**
 * @Author: QiMu
 * @Date: 2023/08/14 11:24:51
 * @Version: 1.0
 * @Description: 时钟在控制器
 */
@RestController
@RequestMapping("/ClockIn")
@Slf4j
public class ClockInController {
    @Resource
    private ClockInInfoService clockInInfoService;
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 打卡
     *
     * @param request 要求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/toClockIn")
    public BaseResponse<Boolean> toClockIn(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        LambdaQueryWrapper<ClockInInfo> clockInInfoQueryWrapper = new LambdaQueryWrapper<>();
        clockInInfoQueryWrapper.eq(ClockInInfo::getUserId, user.getId());
        ClockInInfo clockInInfo = clockInInfoService.getOne(clockInInfoQueryWrapper);
        long secondsUntilUserTime = getObtainClockInTime(clockInInfo);
        if (secondsUntilUserTime > 0) {
            redisTemplate.opsForValue().set(SIGN_USER_GROUP + user.getId(), String.valueOf(user.getId()), secondsUntilUserTime, TimeUnit.SECONDS);
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取打卡时间
     *
     * @param clockInInfoServiceOne 打卡信息服务
     * @return long
     */
    private long getObtainClockInTime(ClockInInfo clockInInfoServiceOne) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalTime userTime = LocalTime.parse(clockInInfoServiceOne.getClockInTime());
        LocalDateTime userDateTime = currentDateTime.with(userTime);
        Duration duration = Duration.between(currentDateTime, userDateTime);
        return duration.getSeconds();
    }

    @PostMapping("/isNotWrite")
    public BaseResponse<Boolean> isNotWrite(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        LambdaQueryWrapper<ClockInInfo> clockInInfoQueryWrapper = new LambdaQueryWrapper<>();
        clockInInfoQueryWrapper.eq(ClockInInfo::getUserId, user.getId());
        ClockInInfo clockInInfoServiceOne = clockInInfoService.getOne(clockInInfoQueryWrapper);
        if (userService.isAdmin(request)) {
            return ResultUtils.success(true);
        }
        if (ObjectUtils.isEmpty(clockInInfoServiceOne)) {
            return ResultUtils.success(true);
        }
        String address = clockInInfoServiceOne.getAddress();
        String deviceType = clockInInfoServiceOne.getDeviceType();
        String deviceId = clockInInfoServiceOne.getDeviceId();
        String longitude = clockInInfoServiceOne.getLongitude();
        String latitude = clockInInfoServiceOne.getLatitude();
        if (StringUtils.isAnyBlank(address, deviceType, deviceId, latitude, longitude)) {
            return ResultUtils.success(true);
        }
        return ResultUtils.success(false);
    }

    /**
     * 开始打卡
     *
     * @param idRequest id请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/starting")
    public BaseResponse<Boolean> startingClockIn(@RequestBody IdRequest idRequest) {
        if (ObjectUtils.anyNull(idRequest, idRequest.getId()) || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        ClockInInfo clockInInfo = clockInInfoService.getById(id);
        if (clockInInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        clockInInfo.setStatus(ClockInStatusEnum.STARTING.getValue());
        return ResultUtils.success(clockInInfoService.updateById(clockInInfo));
    }

    /**
     * 停止打卡
     *
     * @param idRequest id请求
     * @param request   请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/stop")
    public BaseResponse<Boolean> stopClockIn(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (ObjectUtils.anyNull(idRequest, idRequest.getId()) || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        ClockInInfo clockInInfo = clockInInfoService.getById(id);
        if (clockInInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        clockInInfo.setStatus(ClockInStatusEnum.PAUSED.getValue());
        return ResultUtils.success(clockInInfoService.updateById(clockInInfo));
    }
}