package com.techtron.onebook.module.app.controller.app.collectiontransfer;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.module.app.controller.app.collectiontransfer.vo.CollectionTransferSaveReqVO;
import com.techtron.onebook.module.app.controller.app.collectiontransfer.vo.TrasnferUserRespVO;
import com.techtron.onebook.module.app.service.collectiontransfer.CollectionTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.OFF_LINE;

@Tag(name = "管理后台 - 转移记录")
@RestController
@RequestMapping("/app/transfer")
@Validated
public class CollectionTransferController {

    @Resource
    private CollectionTransferService transferService;

    @PostMapping("/create")
    @Operation(summary = "创建转移记录")
    public CommonResult<Long> createTransfer(@Valid @RequestBody CollectionTransferSaveReqVO createReqVO) {
        throw exception(OFF_LINE);
    }

    @GetMapping("/search-user")
    public CommonResult<TrasnferUserRespVO> searchUser(@Valid @Param("uid") String uid) {
       return success(transferService.searchUser(uid));
    }

}