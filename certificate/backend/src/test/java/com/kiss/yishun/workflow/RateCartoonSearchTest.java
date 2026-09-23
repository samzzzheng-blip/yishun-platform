package com.kiss.yishun.workflow;

import com.kiss.yishun.dao.*;
import com.kiss.yishun.entity.*;
import com.kiss.yishun.service.impl.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes=RateCartoonSearchTest.Config.class)
@TestPropertySource(properties={"spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.database-platform=org.hibernate.dialect.H2Dialect","spring.jpa.show-sql=false"})
public class RateCartoonSearchTest {
 @Configuration @EntityScan(basePackageClasses=Rate.class)
 @EnableJpaRepositories(basePackageClasses=RateDao.class)
 @Import({RateServiceImpl.class,CartoonServiceImpl.class}) static class Config {}
 @Autowired RateDao rates; @Autowired CartoonDao cartoons;
 @Autowired RateServiceImpl rate; @Autowired CartoonServiceImpl cartoon;
 @Autowired JdbcTemplate jdbc;
 @Before public void dialect(){jdbc.execute("CREATE DOMAIN IF NOT EXISTS UNSIGNED AS BIGINT");}
 private void addRate(String number,String name){
  Rate r=new Rate();r.setCertNumber(number);r.setRateName(name);r.setSurface("");r.setCenter("");r.setImgUrl("");r.setEdge("");r.setCorner("");r.setUpdatedate(1);rates.saveAndFlush(r);
 }
 private void addCartoon(String number,String role,String title){
  Cartoon c=new Cartoon();c.setCertNumber(number);c.setRoleName(role);c.setCartoonName(title);c.setItemType("");c.setImgUrl("");c.setAuthor("");c.setCompany("");c.setUpdatedate(1);cartoons.saveAndFlush(c);
 }
 private Page<Rate> r(String term,Long start,Long end,int page,int size){return rate.findRatePageByFilters(PageRequest.of(page,size),term,start,end);}
 private Page<Cartoon> c(String term,int page,int size){return cartoon.findCartoonPageByKeywords(PageRequest.of(page,size),term);}
 @Test public void rateNamesNumbersAndRange(){
  addRate("100","皮卡丘");addRate("200","皮卡丘");addRate("ABC","Alice");
  assertEquals(2,r(" 皮卡 ",null,null,0,10).getTotalElements());
  assertEquals(1,r("ALICE",null,null,0,10).getTotalElements());
  assertEquals(1,r("abc",null,null,0,10).getTotalElements());
  assertEquals(1,r("皮卡",150L,250L,0,10).getTotalElements());
  assertEquals(0,r("Alice",100L,300L,0,10).getTotalElements());
 }
 @Test public void cartoonNamesNumbers(){
  addCartoon("YSL1","黑崎一护","死神");addCartoon("YSL2","Alice","Wonderland");
  assertEquals(1,c(" 黑崎 ",0,10).getTotalElements());
  assertEquals(1,c("死神",0,10).getTotalElements());
  assertEquals(1,c("ALICE",0,10).getTotalElements());
  assertEquals(2,c("ysl",0,10).getTotalElements());
  assertEquals(0,c("不存在",0,10).getTotalElements());
 }
 @Test public void wildcardsBlankAndPaging(){
  addRate("100","100%_!");addCartoon("A","100%_!","Test");
  for(int i=1;i<=24;i++){addRate("N"+i,"普通");addCartoon("C"+i,"普通","动漫");}
  for(String term:new String[]{"%","_","!"}){assertEquals(1,r(term,null,null,0,10).getTotalElements());assertEquals(1,c(term,0,10).getTotalElements());}
  assertEquals(25,r(null,null,null,0,10).getTotalElements());assertEquals(25,c("  ",0,10).getTotalElements());
  assertEquals("N24",r("",null,null,0,10).getContent().get(0).getCertNumber());
  assertEquals("C24",c("",0,10).getContent().get(0).getCertNumber());
  assertEquals(5,r("",null,null,2,10).getNumberOfElements());assertEquals(5,c("",2,10).getNumberOfElements());
  assertEquals(20,r("",null,null,0,20).getNumberOfElements());assertEquals(20,c("",0,20).getNumberOfElements());
 }
}
