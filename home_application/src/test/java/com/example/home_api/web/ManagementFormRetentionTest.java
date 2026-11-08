package com.example.home_api.web;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ManagementFormRetentionTest {
    @Test void masterCreateKeepsInvalidNameAndReopensCreateDialog() {
        var session=new MockHttpSession();session.setAttribute("activeGroupId","group-1");session.setAttribute("activeGroupLeader",true);
        var flash=new RedirectAttributesModelMap();
        String view=new GroupMasterController(mock(JdbcTemplate.class)).create("categories"," ",session,flash);
        assertEquals("redirect:/group/masters/categories",view);
        assertEquals("",flash.getFlashAttributes().get("formName"));
        assertEquals("create",flash.getFlashAttributes().get("formMode"));
        assertEquals("input",flash.getAttribute("error"));
    }

    @Test void patternCreateKeepsAllNonPasswordValuesOnValidationError() {
        var session=new MockHttpSession();session.setAttribute("activeGroupId","group-1");
        var values=new LinkedMultiValueMap<String,String>();values.add("displayName","検索条件");values.add("amountFrom","2000");values.add("amountTo","1000");values.add("labelIds","label-1");
        var flash=new RedirectAttributesModelMap();
        String view=new PurchasePatternController(mock(JdbcTemplate.class)).create("search-patterns",values,session,flash);
        assertEquals("redirect:/group/patterns/search-patterns",view);
        assertEquals(values,flash.getFlashAttributes().get("patternForm"));
        assertEquals("input",flash.getAttribute("error"));
    }
}
