package com.techtron.onebook.module.member.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.member.controller.admin.user.vo.*;
import com.techtron.onebook.module.member.convert.user.MemberUserConvert;
import com.techtron.onebook.module.member.dal.dataobject.group.MemberGroupDO;
import com.techtron.onebook.module.member.dal.dataobject.level.MemberLevelDO;
import com.techtron.onebook.module.member.dal.dataobject.tag.MemberTagDO;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import com.techtron.onebook.module.member.dal.mysql.user.MemberUserMapper;
import com.techtron.onebook.module.member.enums.point.MemberPointBizTypeEnum;
import com.techtron.onebook.module.member.service.group.MemberGroupService;
import com.techtron.onebook.module.member.service.level.MemberLevelService;
import com.techtron.onebook.module.member.service.point.MemberPointRecordService;
import com.techtron.onebook.module.member.service.tag.MemberTagService;
import com.techtron.onebook.module.member.service.user.MemberUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.common.util.collection.CollectionUtils.convertSet;
import static com.techtron.onebook.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.techtron.onebook.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.techtron.onebook.framework.web.core.util.WebFrameworkUtils.getTerminal;

@Tag(name = "管理后台 - 会员用户")
@RestController
@RequestMapping("/member/user")
@Validated
public class MemberUserController {

    @Resource
    private MemberUserService memberUserService;
    @Resource
    private MemberUserMapper memberUserMapper;
    @Resource
    private MemberTagService memberTagService;
    @Resource
    private MemberLevelService memberLevelService;
    @Resource
    private MemberGroupService memberGroupService;
    @Resource
    private MemberPointRecordService memberPointRecordService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('member:user:update')")
    public CommonResult<Boolean> createUser(@Valid @RequestBody MemberUserCreateReqVO createReqVO) {
        memberUserService.createUserByAdmin(createReqVO.getMobile(),getClientIP(),getTerminal(),createReqVO.getPassword());
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "更新会员用户")
    @PreAuthorize("@ss.hasPermission('member:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody MemberUserUpdateReqVO updateReqVO) {
        memberUserService.updateUser(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-level")
    @Operation(summary = "更新会员用户等级")
    @PreAuthorize("@ss.hasPermission('member:user:update-level')")
    public CommonResult<Boolean> updateUserLevel(@Valid @RequestBody MemberUserUpdateLevelReqVO updateReqVO) {
        memberLevelService.updateUserLevel(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-point")
    @Operation(summary = "更新会员用户积分")
    @PreAuthorize("@ss.hasPermission('member:user:update-point')")
    public CommonResult<Boolean> updateUserPoint(@Valid @RequestBody MemberUserUpdatePointReqVO updateReqVO) {
        memberPointRecordService.createPointRecord(updateReqVO.getId(), updateReqVO.getPoint(),
                MemberPointBizTypeEnum.ADMIN, String.valueOf(getLoginUserId()));
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<MemberUserRespVO> getUser(@RequestParam("id") Long id) {
        MemberUserDO user = memberUserService.getUser(id);
        return success(MemberUserConvert.INSTANCE.convert03(user));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员用户分页")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<PageResult<MemberUserRespVO>> getUserPage(@Valid MemberUserPageReqVO pageVO) {
        PageResult<MemberUserDO> pageResult = memberUserService.getUserPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 处理用户标签返显
        Set<Long> tagIds = pageResult.getList().stream()
                .map(MemberUserDO::getTagIds)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        List<MemberTagDO> tags = memberTagService.getTagList(tagIds);
        // 处理用户级别返显
        List<MemberLevelDO> levels = memberLevelService.getLevelList(
                convertSet(pageResult.getList(), MemberUserDO::getLevelId));
        // 处理用户分组返显
        List<MemberGroupDO> groups = memberGroupService.getGroupList(
                convertSet(pageResult.getList(), MemberUserDO::getGroupId));
        PageResult<MemberUserRespVO> result = MemberUserConvert.INSTANCE.convertPage(pageResult, tags, levels, groups);

        // 处理能量石数量返显
        Set<Long> userIds = convertSet(result.getList(), MemberUserRespVO::getId);
        List<Map<String, Object>> energyStoneList = memberUserMapper.selectEnergyStoneAmountByUserIds(userIds);
        Map<Long, Integer> energyStoneMap = new HashMap<>();
        for (Map<String, Object> row : energyStoneList) {
            Long userId = ((Number) row.get("user_id")).longValue();
            Integer amount = row.get("amount") != null ? ((Number) row.get("amount")).intValue() : 0;
            energyStoneMap.put(userId, amount);
        }
        result.getList().forEach(vo -> vo.setEnergyStoneAmount(energyStoneMap.getOrDefault(vo.getId(), 0)));

        return success(result);
    }

}
