package com.techtron.onebook.module.app.controller.admin.inbound;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.module.app.service.inbound.InboundParcelService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
@RestController
@RequestMapping("/app/inbound-parcel")
public class InboundParcelController {
 @Resource private InboundParcelService service;
 @GetMapping("/config") @PreAuthorize("@ss.hasPermission('app:collection:query')")
 public CommonResult<Map<String,Object>> config(){return success(service.config());}
 @GetMapping("/list") @PreAuthorize("@ss.hasPermission('app:collection:query')")
 public CommonResult<List<Map<String,Object>>> list(@RequestParam(required=false) String tracking,@RequestParam(defaultValue="1") int page,@RequestParam(required=false) Integer status){return success(service.list(null,tracking,page,status));}
 @GetMapping("/get") @PreAuthorize("@ss.hasPermission('app:collection:query')")
 public CommonResult<Map<String,Object>> get(@RequestParam Long id){return success(service.detail(id,null));}
 @PostMapping("/receive") @PreAuthorize("@ss.hasPermission('app:collection:update')")
 public CommonResult<Boolean> receive(@RequestParam Long id){service.receive(getLoginUserId(),id);return success(true);}
 @PostMapping("/inspect") @PreAuthorize("@ss.hasPermission('app:collection:update')")
 public CommonResult<Boolean> inspect(@Valid @RequestBody InboundParcelService.InspectRequest req){service.inspect(getLoginUserId(),req);return success(true);}
 @PostMapping("/correct") @PreAuthorize("@ss.hasPermission('app:collection:update')")
 public CommonResult<Boolean> correct(@Valid @RequestBody InboundParcelService.CorrectionRequest req){service.correct(getLoginUserId(),req);return success(true);}
 @PostMapping("/reopen") @PreAuthorize("@ss.hasPermission('app:collection:update')")
 public CommonResult<Boolean> reopen(@RequestParam Long parcelId,@RequestParam Long collectionId){service.reopen(getLoginUserId(),parcelId,collectionId);return success(true);}
}
