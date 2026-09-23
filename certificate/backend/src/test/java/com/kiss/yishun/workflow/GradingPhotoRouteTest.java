package com.kiss.yishun.workflow;
import com.kiss.yishun.config.ShiroConfig;
import org.apache.shiro.util.AntPathMatcher;
import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class GradingPhotoRouteTest {
    @Test public void nestedPhotosUseJwtBeforeAnonymousFallback(){
        Map<String,String> routes=new ShiroConfig().shiroFilter(mock(org.apache.shiro.mgt.SecurityManager.class)).getFilterChainDefinitionMap();
        AntPathMatcher matcher=new AntPathMatcher();
        for(String path:new String[]{"/api/usr/gradingPhotoFile/example.jpg","/api/usr/gradingNextInBatch","/api/usr/gradingJobs"}){
            String selected=null;for(Map.Entry<String,String> route:routes.entrySet())if(matcher.matches(route.getKey(),path)){selected=route.getValue();break;}
            assertEquals(path,"jwt",selected);
        }
    }
}
