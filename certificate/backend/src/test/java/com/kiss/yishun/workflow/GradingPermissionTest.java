package com.kiss.yishun.workflow;
import com.kiss.yishun.controller.admin.GradingWorkflowController;
import com.kiss.yishun.service.*;
import com.kiss.yishun.entity.*;
import com.kiss.yishun.auth.JwtUtil;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class GradingPermissionTest {
    private GradingWorkflowController controller;
    private GradingWorkflowService workflow;
    private UserService users;
    private MenuService menus;
    @Before public void setup() {
        controller=new GradingWorkflowController();workflow=mock(GradingWorkflowService.class);users=mock(UserService.class);
        ReflectionTestUtils.setField(controller,"workflow",workflow);ReflectionTestUtils.setField(controller,"users",users);
        menus=mock(MenuService.class);ReflectionTestUtils.setField(controller,"menus",menus);
        Subject subject=mock(Subject.class);when(subject.getPrincipal()).thenReturn(JwtUtil.sign("tester",System.currentTimeMillis(),"local"));ThreadContext.bind(subject);
    }
    @After public void cleanup(){ThreadContext.unbindSubject();}
    private User user(long operation) {
        User u=new User();Role r=new Role();Permission p=new Permission();Menu m=new Menu();m.setPath("rateManage");
        Operation op=new Operation();op.setId(operation);p.setMenu(m);p.setOperation(op);r.setPermissionList(Arrays.asList(p));u.setRole(r);u.setDisabled(0);
        when(users.findByUsername("tester")).thenReturn(u);return u;
    }
    @Test public void viewerCanReadButCannotCreate() {
        user(1);assertEquals(200,controller.list("","",1).getCode());
        assertNotEquals(200,controller.create(new GradingJob()).getCode());verify(workflow,never()).create(any(),anyString());
    }
    private void inherited(long operation) {
        Menu parent=user(operation).getRole().getPermissionList().get(0).getMenu();parent.setId(11);parent.setPath("/work");
        Menu rating=new Menu();rating.setId(22);rating.setPath("rateManage");
        when(menus.findMenuChildList(11)).thenReturn(Arrays.asList(rating));
    }
    @Test public void inheritedViewerCanReadButNotWrite(){inherited(1);assertEquals(200,controller.list("","",1).getCode());assertNotEquals(200,controller.create(new GradingJob()).getCode());verify(workflow,never()).create(any(),anyString());}
    @Test public void inheritedEditorCanCreateButNotDelete(){inherited(3);GradingJob j=new GradingJob();assertEquals(200,controller.create(j).getCode());verify(workflow).create(j,"tester");GradingWorkflowService.ManageRequest r=new GradingWorkflowService.ManageRequest();r.setAction("DELETE");assertNotEquals(200,controller.managePreview(r).getCode());}
    @Test public void siblingAndCycleDoNotGrantRating(){Menu m=user(1).getRole().getPermissionList().get(0).getMenu();m.setId(12);m.setPath("preciousManage");when(menus.findMenuChildList(12)).thenReturn(Arrays.asList(m));assertNotEquals(200,controller.list("","",1).getCode());verifyZeroInteractions(workflow);}
    @Test public void disabledAccountCannotReadOrWrite() {
        user(2).setDisabled(1);assertNotEquals(200,controller.list("","",1).getCode());assertNotEquals(200,controller.create(new GradingJob()).getCode());verifyZeroInteractions(workflow);
    }
    @Test public void nextCardRequiresEditPermission(){user(1);assertNotEquals(200,controller.nextInBatch(1L).getCode());verifyZeroInteractions(workflow);user(3);assertEquals(200,controller.nextInBatch(1L).getCode());verify(workflow).nextInBatch(1L,"tester");}
    @Test public void temporaryPurgeRequiresDeletePermission(){GradingTemporaryService temp=mock(GradingTemporaryService.class);ReflectionTestUtils.setField(controller,"temporary",temp);GradingTemporaryService.Range r=new GradingTemporaryService.Range();user(3);assertNotEquals(200,controller.temporaryPurge(r).getCode());verifyZeroInteractions(temp);user(4);assertEquals(200,controller.temporaryPurge(r).getCode());verify(temp).purge(r,"tester");}
    @Test public void editorCanCreate() {
        user(2);GradingJob j=new GradingJob();controller.create(j);verify(workflow).create(j,"tester");
    }
    @Test public void temporaryImportRequiresEnabledEditorAndUsesSessionOwner() throws Exception {
        GradingTemporaryImportService service=mock(GradingTemporaryImportService.class);ReflectionTestUtils.setField(controller,"temporaryImport",service);
        org.springframework.mock.web.MockMultipartFile f=new org.springframework.mock.web.MockMultipartFile("file","cards.xls","application/octet-stream",new byte[]{1});
        user(1);assertNotEquals(200,controller.temporaryImport(f).getCode());verifyZeroInteractions(service);
        user(2).setDisabled(1);assertNotEquals(200,controller.temporaryImport(f).getCode());verifyZeroInteractions(service);
        user(2);assertEquals(200,controller.temporaryImport(f).getCode());verify(service).importFile(f,"tester");
    }
    @Test public void summaryRequiresEditPermission(){user(1);GradingWorkflowController.SummaryEdit r=new GradingWorkflowController.SummaryEdit();assertNotEquals(200,controller.saveSummary(r).getCode());verifyZeroInteractions(workflow);user(3);assertEquals(200,controller.saveSummary(r).getCode());verify(workflow).saveSummary(null,null,null,null,"tester");}
    @Test public void batchNumberRequiresEditPermission(){user(1);GradingWorkflowController.BatchNumberEdit r=new GradingWorkflowController.BatchNumberEdit();assertNotEquals(200,controller.saveBatchNumber(r).getCode());verifyZeroInteractions(workflow);user(3);assertEquals(200,controller.saveBatchNumber(r).getCode());verify(workflow).saveBatchNumber(null,null,null,"tester");}
    @Test public void editorCannotDeleteWithoutDeletePermission(){user(2);GradingWorkflowService.ManageRequest r=new GradingWorkflowService.ManageRequest();r.setAction("DELETE");assertNotEquals(200,controller.managePreview(r).getCode());assertNotEquals(200,controller.manageApply(r).getCode());verifyZeroInteractions(workflow);}
    @Test public void deletePermissionAllowsWorkflowDelete(){user(4);GradingWorkflowService.ManageRequest r=new GradingWorkflowService.ManageRequest();r.setAction("DELETE");assertEquals(200,controller.managePreview(r).getCode());verify(workflow).managePreview(r,"tester");}
    @Test public void unrelatedPermissionCannotWrite() {
        User u=user(3);u.getRole().getPermissionList().get(0).getMenu().setPath("preciousManage");
        assertNotEquals(200,controller.create(new GradingJob()).getCode());verifyZeroInteractions(workflow);
    }
    @Test public void normalEmployeeCanMaintainTemplates() {
        user(1).getRole().setCode("staff");
        GradingTemplateService templates=mock(GradingTemplateService.class);ReflectionTestUtils.setField(controller,"templates",templates);
        GradingLabelTemplate template=new GradingLabelTemplate();
        assertEquals(200,controller.saveTemplate(template).getCode());verify(templates).save(template,"tester");
        assertEquals(200,controller.templates().getCode());
    }
    @Test public void disabledEmployeeCannotMaintainTemplates() {
        user(1).setDisabled(1);GradingTemplateService templates=mock(GradingTemplateService.class);ReflectionTestUtils.setField(controller,"templates",templates);
        assertNotEquals(200,controller.saveTemplate(new GradingLabelTemplate()).getCode());verifyZeroInteractions(templates);
    }
    @Test public void unrelatedUserCannotMaintainTemplates() {
        user(1).getRole().getPermissionList().get(0).getMenu().setPath("preciousManage");
        GradingTemplateService templates=mock(GradingTemplateService.class);ReflectionTestUtils.setField(controller,"templates",templates);
        assertNotEquals(200,controller.saveTemplate(new GradingLabelTemplate()).getCode());verifyZeroInteractions(templates);
    }
}
