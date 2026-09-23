package com.kiss.yishun.workflow;

import com.kiss.yishun.dao.PreciousDao;
import com.kiss.yishun.entity.Precious;
import com.kiss.yishun.service.impl.PreciousServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes=PreciousSearchTest.Config.class)
@TestPropertySource(properties={"spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.database-platform=org.hibernate.dialect.H2Dialect","spring.jpa.show-sql=false"})
public class PreciousSearchTest {
    @Configuration @EntityScan(basePackageClasses=Precious.class)
    @EnableJpaRepositories(basePackageClasses=PreciousDao.class)
    @Import(PreciousServiceImpl.class)
    static class Config {}
    @Autowired PreciousDao dao;
    @Autowired PreciousServiceImpl service;
    private void add(String number, String signer, int status) {
        Precious p=new Precious();p.setCertNumber(number);p.setSigner(signer);
        p.setStatus(status);p.setItemType("卡牌");p.setImgUrl("");p.setUpdatedate(1);
        dao.saveAndFlush(p);
    }
    private Page<Precious> search(String keyword,Integer status,int page,int size) {
        return service.findPreciousPageByKeywords(PageRequest.of(page,size),keyword,status);
    }
    @Test public void namesNumbersAndStatus() {
        add("YSAB1","松冈由贵",1);add("YSAB2","松冈由贵",0);add("YSAB3","Alice",1);
        assertEquals(2,search(" 松冈 ",null,0,10).getTotalElements());
        assertEquals(1,search("松冈",1,0,10).getTotalElements());
        assertEquals(1,search("松冈",0,0,10).getTotalElements());
        assertEquals(3,search("ysab",-1,0,10).getTotalElements());
        assertEquals(1,search("ALICE",-1,0,10).getTotalElements());
        assertEquals(0,search("不存在",-1,0,10).getTotalElements());
    }
    @Test public void literalWildcardsAndBlankSearch() {
        add("A1","100%_!",1);add("A2","普通",0);
        for(String term:new String[]{"%","_","!","100%_!"}) assertEquals(1,search(term,-1,0,10).getTotalElements());
        assertEquals(2,search(null,null,0,10).getTotalElements());
        assertEquals(2,search("   ",-1,0,10).getTotalElements());
    }
    @Test public void paginationHasStableOrderAndAccurateTotal() {
        for(int i=1;i<=25;i++)add("N"+i,"同名",1);
        Page<Precious> first=search("同名",1,0,10),last=search("同名",1,2,10);
        assertEquals(25,first.getTotalElements());assertEquals(3,first.getTotalPages());
        assertEquals("N25",first.getContent().get(0).getCertNumber());
        assertEquals(5,last.getNumberOfElements());assertEquals("N5",last.getContent().get(0).getCertNumber());
        assertEquals(20,search("同名",1,0,20).getNumberOfElements());
    }
}
