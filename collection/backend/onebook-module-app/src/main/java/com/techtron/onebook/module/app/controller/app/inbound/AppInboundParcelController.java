package com.techtron.onebook.module.app.controller.app.inbound;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.module.app.service.inbound.InboundParcelService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
@RestController
@RequestMapping("/app/inbound-parcel")
public class AppInboundParcelController {
 @Resource private InboundParcelService service;
 @GetMapping("/config") public CommonResult<Map<String,Object>> config(){return success(service.config());}
 @GetMapping("/list") public CommonResult<List<Map<String,Object>>> list(@RequestParam(defaultValue="1") int page){return success(service.list(getLoginUserId(),null,page));}
 @GetMapping("/get") public CommonResult<Map<String,Object>> get(@RequestParam Long id){return success(service.detail(id,getLoginUserId()));}
 @GetMapping("/available") public CommonResult<List<Map<String,Object>>> available(@RequestParam(required=false) Long parcelId){return success(service.available(getLoginUserId(),parcelId));}
 @GetMapping("/collection-states") public CommonResult<List<Map<String,Object>>> states(){return success(service.collectionStates(getLoginUserId()));}
 @PostMapping("/save") public CommonResult<Long> save(@Valid @RequestBody InboundParcelService.SaveRequest req){return success(service.save(getLoginUserId(),req));}
 @PostMapping("/cancel") public CommonResult<Boolean> cancel(@RequestParam Long id){service.cancel(getLoginUserId(),id);return success(true);}
}
