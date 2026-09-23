package com.techtron.onebook.module.app.service.category;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DirectPurchaseCategoryTest {
    @Test void standardCategoryIsPreserved() {
        var jdbc = database();
        jdbc.execute("INSERT INTO app_category(id,name,user_id,deleted) VALUES (1,'签名卡砖',0,0),(2,'自定义',0,0)");
        assertEquals(1L, service(jdbc).getPurchaseCategory(1L).getId());
        assertEquals("签名卡砖", service(jdbc).getPurchaseCategory(1L).getName());
    }
    @Test void privateDeletedMissingAndLegacyCategoriesUseCustom() {
        var jdbc = database();
        jdbc.execute("INSERT INTO app_category(id,name,user_id,deleted) VALUES (1,'私有',305,0),(2,'自定义',0,0),(3,'已删除',0,1),(4,'直购物品',0,0),(5,'直购18',305,1)");
        for (Long id : new Long[]{1L,3L,4L,5L,999L,null}) {
            assertEquals(2L, service(jdbc).getPurchaseCategory(id).getId());
            assertEquals("自定义", service(jdbc).getPurchaseCategory(id).getName());
        }
        assertEquals(5, jdbc.queryForObject("SELECT COUNT(*) FROM app_category", Integer.class));
    }
    @Test void copiedStandardCategoryKeepsItsPublicType() {
        var jdbc = database();
        jdbc.execute("INSERT INTO app_category VALUES (1,'评级卡砖',0,0,NULL),(2,'评级卡砖3',305,0,1)");
        assertEquals(1L, service(jdbc).getPurchaseCategory(2L).getId());
    }
    @Test void customMustBeActivePublicCategory() {
        var jdbc = database();
        jdbc.execute("INSERT INTO app_category(id,name,user_id,deleted) VALUES (1,'自定义',305,0),(2,'自定义',0,1)");
        assertThrows(IllegalStateException.class, () -> service(jdbc).getPurchaseCategory(null));
    }
    private JdbcTemplate database() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE app_category(id bigint PRIMARY KEY,name varchar(255),user_id bigint,deleted int,copy_id bigint)");
        return jdbc;
    }
    private CollectionCategoryServiceImpl service(JdbcTemplate jdbc) {
        var service = new CollectionCategoryServiceImpl();
        ReflectionTestUtils.setField(service, "jdbc", jdbc);
        return service;
    }
}
