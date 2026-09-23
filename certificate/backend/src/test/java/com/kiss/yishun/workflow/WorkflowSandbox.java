package com.kiss.yishun.workflow;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.controller.admin.GradingWorkflowController;
import com.kiss.yishun.controller.admin.PreciousController;
import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.dao.GradingJobDao;
import com.kiss.yishun.dao.RateDao;
import org.springframework.beans.factory.annotation.Autowired;
import com.kiss.yishun.entity.*;
import com.kiss.yishun.entity.Role;
import com.kiss.yishun.service.*;
import com.kiss.yishun.common.*;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.nio.file.*;
import java.util.*;
import static org.mockito.Mockito.*;

/** Test-source-only, loopback-only sandbox. Never included in the production jar. */
@Configuration
@EnableAutoConfiguration(excludeName="com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure")
@EntityScan(basePackageClasses=GradingJob.class)
@EnableJpaRepositories(basePackageClasses=GradingJobDao.class)
@Import({GradingWorkflowService.class,GradingBatchPublishService.class,com.kiss.yishun.service.GradingPhotoStore.class,com.kiss.yishun.service.GradingTemplateService.class,com.kiss.yishun.service.GradingTemporaryService.class,com.kiss.yishun.service.GradingTemporaryImportService.class,com.kiss.yishun.service.RateImportService.class,GradingWorkflowController.class,WorkflowSandbox.DemoEndpoints.class})
public class WorkflowSandbox implements WebMvcConfigurer {
    private static Path assets;
    private static Path photos;
    private static final Map<String,String> SESSIONS=new java.util.concurrent.ConcurrentHashMap<>();
    public static void main(String[] args) throws Exception {
        if(args.length!=1) throw new IllegalArgumentException("Provide the local frontend dist directory only");
        assets=Paths.get(args[0]).toRealPath();
        if(!Files.isRegularFile(assets.resolve("index.html"))) throw new IllegalArgumentException("Build the frontend first");
        photos=Files.createTempDirectory("yishun-sandbox-photos-");
        Path privatePhotos=Files.createTempDirectory("yishun-sandbox-private-");
        // Optional local-only checkpoint: preserve test records and login sessions on a code restart.
        String resume=System.getProperty("workflow.sandbox.resume");
        if(resume!=null) {
            Path root=Paths.get(resume).toRealPath();Properties saved=new Properties();
            try(java.io.InputStream in=Files.newInputStream(root.resolve("snapshot.properties"))){saved.load(in);}
            photos=Paths.get(saved.getProperty("photos")).toRealPath();
            privatePhotos=Paths.get(saved.getProperty("privatePhotos")).toRealPath();
            Properties sessions=new Properties();
            try(java.io.InputStream in=Files.newInputStream(root.resolve("sessions.properties"))){sessions.load(in);}
            for(String key:sessions.stringPropertyNames())SESSIONS.put(key,sessions.getProperty(key));
            try(java.sql.Connection c=java.sql.DriverManager.getConnection("jdbc:h2:mem:workflow_sandbox;DB_CLOSE_DELAY=-1;MODE=MySQL","sa","");java.sql.Statement s=c.createStatement()) {
                s.execute("RUNSCRIPT FROM '"+root.resolve("database.sql").toString().replace("'","''")+"'");
            }
        }
        // Do not load application.properties, application-prod.properties, or deployment credentials.
        System.setProperty("spring.config.name","workflow-sandbox-no-production-config");
        Map<String,Object> p=new HashMap<>();
        p.put("grading.private-photo-dir",privatePhotos.toString());
        p.put("server.address","127.0.0.1");p.put("server.port","4188");
        p.put("spring.datasource.url","jdbc:h2:mem:workflow_sandbox;DB_CLOSE_DELAY=-1;MODE=MySQL");
        p.put("spring.datasource.driver-class-name","org.h2.Driver");p.put("spring.datasource.username","sa");p.put("spring.datasource.password","");
        p.put("spring.jpa.hibernate.ddl-auto",resume==null?"create-drop":"update");p.put("spring.jpa.database-platform","org.hibernate.dialect.H2Dialect");
        p.put("spring.servlet.multipart.max-file-size","12MB");p.put("spring.servlet.multipart.max-request-size","13MB");
        p.put("logging.level.root","WARN");p.put("spring.jmx.enabled","false");
        for(String key:Arrays.asList("preciousdir","tmpdir","zipdir")) {
            p.put("upload.disk."+key,photos.toString());p.put("upload.return."+key,"/upload/precious/");
        }
        p.put("upload.port","4188");p.put("upload.server.name","127.0.0.1");
        SpringApplication app=new SpringApplication(WorkflowSandbox.class);app.setDefaultProperties(p);app.run();
        System.out.println("LOCAL SANDBOX ONLY http://127.0.0.1:4188/rateManage  account: demo  password: demo123");
        System.out.println("In-memory test records disappear on shutdown. Test photos: "+photos);
    }
    @Bean public UploadConfig uploadConfig() {return new UploadConfig();}
    @Bean public com.kiss.yishun.service.RateService rateService() {return mock(com.kiss.yishun.service.RateService.class);}
    @Bean public MenuService menuService() {return mock(MenuService.class);}
    @Bean public UserService userService() {
        UserService mock=mock(UserService.class);User user=new User();user.setUsername("demo");user.setDisabled(0);
        Role role=new Role();List<Permission> permissions=new ArrayList<>();
        for(long id=1;id<=5;id++) {Menu menu=new Menu();menu.setPath("rateManage");Operation op=new Operation();op.setId(id);
            Permission perm=new Permission();perm.setMenu(menu);perm.setOperation(op);permissions.add(perm);}
        role.setCode("admin");role.setPermissionList(permissions);user.setRole(role);when(mock.findByUsername("demo")).thenReturn(user);
        Role staffRole=new Role();staffRole.setCode("staff");staffRole.setPermissionList(permissions);
        User other=new User();other.setUsername("peer");other.setDisabled(0);other.setRole(staffRole);when(mock.findByUsername("peer")).thenReturn(other);return mock;
    }
    @Bean public FilterRegistrationBean<Filter> localAuthentication() {
        Filter filter=new Filter() {
            public void init(FilterConfig config) {}
            public void destroy() {}
            public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws java.io.IOException,ServletException {
                HttpServletRequest req=(HttpServletRequest)request;
                if(req.getRequestURI().startsWith("/api/usr/")) {
                    String token=req.getHeader("Authorization");
                    if(token==null||!SESSIONS.containsKey(token)) {((HttpServletResponse)response).sendError(401);return;}
                    Subject subject=mock(Subject.class);when(subject.getPrincipal()).thenReturn(token);ThreadContext.bind(subject);
                }
                try {chain.doFilter(request,response);} finally {ThreadContext.unbindSubject();}
            }
        };
        return new FilterRegistrationBean<>(filter);
    }
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/precious/**").addResourceLocations(photos.toUri().toString()+"/");
        registry.addResourceHandler("/**").addResourceLocations(assets.toUri().toString()+"/");
    }
    @Override public void addViewControllers(ViewControllerRegistry registry) {
        for(String path:Arrays.asList("/grading","/login","/manage","/rateManage","/rateIntake","/rateWorkflow","/rateTemporary"))
            registry.addViewController(path).setViewName("forward:/index.html");
    }
    @RestController public static class DemoEndpoints {
        @Autowired private RateDao rates;
        @PostMapping("/api/base/login") public Result login(@RequestBody Map<String,String> body) {
            if(!Arrays.asList("demo","peer").contains(body.get("username"))||!"REPLACE_WITH_LOCAL_SECRET".equals(body.get("password")))
                return ResultGenerator.genFailureResult("本地演示账号 demo，密码 demo123");
            String token=JWT.create().withClaim(SecurityConstant.ACCOUNT,UUID.randomUUID()+"_"+body.get("username")).sign(Algorithm.HMAC256(UUID.randomUUID().toString()));
            SESSIONS.put(token,body.get("username"));
            Map<String,String> data=new HashMap<>();data.put("token",token);data.put("userName",body.get("username"));data.put("role","demo");
            return ResultGenerator.genSuccessResult(data);
        }
        @GetMapping("/api/usr/logout") public Result logout(HttpServletRequest request) {SESSIONS.remove(request.getHeader("Authorization"));return ResultGenerator.genSuccessResult();}
        @GetMapping("/api/usr/queryRoleMenuList") public Result menus(@RequestParam int operationId) {
            Map<String,Object> rate=new HashMap<>();rate.put("id",2);rate.put("resKey","rateManage");rate.put("resName","评级");rate.put("resIcon","");
            Map<String,Object> parent=new HashMap<>();parent.put("id",1);parent.put("resKey","platformManage");parent.put("resName","工作台");parent.put("children",Arrays.asList(rate));
            return ResultGenerator.genSuccessResult(Collections.singletonMap("list",Arrays.asList(parent)));
        }
        // H2 test adapter for the existing list contract; production still uses the original MySQL controller.
        @GetMapping("/api/usr/queryRateList") public Result rateList(@RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int pageSize,@RequestParam(defaultValue="") String keywords,
            @RequestParam(required=false) Long startNumber,@RequestParam(required=false) Long endNumber) {
            List<Rate> rows=new ArrayList<>();
            for(Rate rate:rates.findAll()) {
                if(!rate.getCertNumber().contains(keywords)) continue;
                if(startNumber!=null||endNumber!=null) {
                    if(!rate.getCertNumber().matches("[0-9]{1,18}")) continue;
                    long number=Long.parseLong(rate.getCertNumber());
                    if(startNumber!=null&&number<startNumber||endNumber!=null&&number>endNumber) continue;
                }
                rows.add(rate);
            }
            rows.sort(Comparator.comparingLong(Rate::getUpdatedate).reversed());
            int size=Math.max(1,Math.min(100,pageSize)),start=Math.min(rows.size(),Math.max(0,page-1)*size);
            Map<String,Object> data=new HashMap<>();data.put("totalCount",rows.size());data.put("page",page);
            data.put("totalPage",(rows.size()+size-1)/size);data.put("list",rows.subList(start,Math.min(rows.size(),start+size)));
            return ResultGenerator.genSuccessResult(data);
        }
        @GetMapping("/api/usr/downloadQrcode") public void qr(@RequestParam String certNo,HttpServletResponse response) {
            new PreciousController().downloadQrcode(certNo,response);
        }
    }
}
