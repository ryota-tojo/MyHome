package com.example.home_api.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/group/masters")
public class GroupMasterController {
    private final JdbcTemplate jdbc;
    public GroupMasterController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private record MasterDef(String title,String table,String orderCol,String nameCol,boolean required) {}
    private MasterDef def(String type) {
        return switch (type) {
            case "categories" -> new MasterDef("カテゴリー管理","category","category_no","category_name",true);
            case "payments" -> new MasterDef("支払い方法管理","payment_method","payment_no","payment_name",true);
            case "labels" -> new MasterDef("ラベル管理","shopping_label","label_no","label_name",false);
            case "kanban-lanes" -> new MasterDef("レーン管理","kanban_lane","lane_no","lane_name",true);
            default -> throw new IllegalArgumentException("不正なマスター種別です");
        };
    }

    @GetMapping("/{type}")
    String list(@PathVariable String type,HttpSession session,Model model) {
        String group=groupForType(session,type); MasterDef d=def(type);
        if("kanban-lanes".equals(type))ensureDefaultLanes(group);
        model.addAttribute("type",type); model.addAttribute("title",d.title()); model.addAttribute("requiredMaster",d.required());
        model.addAttribute("items",jdbc.queryForList("SELECT id,"+d.nameCol()+" AS name,"+d.orderCol()+" AS sort_no,disabled_flag FROM "+d.table()+" WHERE group_id=? ORDER BY "+d.orderCol()+",create_date",group));
        return "group/master-list";
    }

    @PostMapping("/{type}") @Transactional
    String create(@PathVariable String type,@RequestParam String name,HttpSession session,RedirectAttributes redirect) {
        String group=groupForType(session,type); MasterDef d=def(type); String value=name.strip();
        if(value.isEmpty()||value.length()>100) return formError(type,"input",value,null,redirect);
        Integer duplicate=jdbc.queryForObject("SELECT count(*) FROM "+d.table()+" WHERE group_id=? AND lower("+d.nameCol()+")=lower(?)",Integer.class,group,value);
        if(duplicate!=null&&duplicate>0) return formError(type,"duplicate",value,null,redirect);
        Integer next=jdbc.queryForObject("SELECT COALESCE(MAX("+d.orderCol()+"::integer),0)+1 FROM "+d.table()+" WHERE group_id=?",Integer.class,group);
        String id="kanban-lanes".equals(type)?"lane-"+UUID.randomUUID().toString().substring(0,12):UUID.randomUUID().toString();
        try { jdbc.update("INSERT INTO "+d.table()+"(id,group_id,"+d.orderCol()+","+d.nameCol()+",disabled_flag,create_date,update_date) VALUES (?,?,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",id,group,next,value); }
        catch(Exception ex){ return formError(type,"duplicate",value,null,redirect); }
        return "redirect:/group/masters/"+type+"?created";
    }

    @PostMapping("/{type}/{id}/toggle") @Transactional
    String toggle(@PathVariable String type,@PathVariable String id,HttpSession session) {
        String group=groupForType(session,type); MasterDef d=def(type);
        Integer disabled=jdbc.queryForObject("SELECT disabled_flag FROM "+d.table()+" WHERE group_id=? AND id=?",Integer.class,group,id);
        if(disabled==null) return "redirect:/group/masters/"+type;
        if(disabled==0&&d.required()) {
            Integer active=jdbc.queryForObject("SELECT count(*) FROM "+d.table()+" WHERE group_id=? AND disabled_flag=0",Integer.class,group);
            if(active!=null&&active<=1) return "redirect:/group/masters/"+type+"?error=last";
        }
        if("kanban-lanes".equals(type)&&disabled==0){
            Integer used=jdbc.queryForObject("SELECT count(*) FROM kanban_card WHERE group_id=? AND status=?",Integer.class,group,id);
            if(used!=null&&used>0)return "redirect:/group/masters/"+type+"?error=inUse";
        }
        jdbc.update("UPDATE "+d.table()+" SET disabled_flag=?,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND id=?",disabled==0?1:0,group,id);
        if(disabled==0&&!"kanban-lanes".equals(type)){String column=type.equals("categories")?"category_ids":type.equals("payments")?"payment_method_ids":"label_ids";jdbc.update("UPDATE purchase_pattern SET disabled_flag=1,auto_disabled=1,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND ?=ANY(string_to_array("+column+",','))",group,id);}
        return "redirect:/group/masters/"+type;
    }

    @PostMapping("/{type}/{id}/update") @Transactional
    String update(@PathVariable String type,@PathVariable String id,@RequestParam String name,HttpSession session,RedirectAttributes redirect) {
        String group=groupForType(session,type); MasterDef d=def(type); String value=name.strip();
        if(value.isEmpty()||value.length()>100) return formError(type,"input",value,id,redirect);
        Integer duplicate=jdbc.queryForObject("SELECT count(*) FROM "+d.table()+" WHERE group_id=? AND lower("+d.nameCol()+")=lower(?) AND id<>?",Integer.class,group,value,id);
        if(duplicate!=null&&duplicate>0) return formError(type,"duplicate",value,id,redirect);
        jdbc.update("UPDATE "+d.table()+" SET "+d.nameCol()+"=?,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND id=?",value,group,id);
        return "redirect:/group/masters/"+type+"?updated";
    }

    @PostMapping("/{type}/{id}/delete")
    String delete(@PathVariable String type,@PathVariable String id,HttpSession session) {
        String group=groupForType(session,type); MasterDef d=def(type);
        var rows=jdbc.queryForList("SELECT disabled_flag FROM "+d.table()+" WHERE group_id=? AND id=?",group,id);
        if(rows.isEmpty()) return "redirect:/group/masters/"+type;
        int disabled=((Number)rows.get(0).get("disabled_flag")).intValue();
        if(disabled!=1)return "redirect:/group/masters/"+type+"?error=disableFirst";
        if("payments".equals(type)){Integer used=jdbc.queryForObject("SELECT count(*) FROM shopping WHERE group_id=? AND payment_method_id=?",Integer.class,group,id);if(used!=null&&used>0)return "redirect:/group/masters/"+type+"?error=inUse";}
        if("labels".equals(type)){Integer used=jdbc.queryForObject("SELECT count(*) FROM shopping WHERE group_id=? AND ?=ANY(string_to_array(COALESCE(shopping_label_ids,''),','))",Integer.class,group,id);if(used!=null&&used>0)return "redirect:/group/masters/"+type+"?error=inUse";}
        if(d.required()&&disabled==0){Integer active=jdbc.queryForObject("SELECT count(*) FROM "+d.table()+" WHERE group_id=? AND disabled_flag=0",Integer.class,group);if(active!=null&&active<=1)return "redirect:/group/masters/"+type+"?error=last";}
        try{jdbc.update("DELETE FROM "+d.table()+" WHERE group_id=? AND id=?",group,id);}catch(org.springframework.dao.DataAccessException ex){return "redirect:/group/masters/"+type+"?error=inUse";}
        if(!"kanban-lanes".equals(type)){
            String column=type.equals("categories")?"category_ids":type.equals("payments")?"payment_method_ids":"label_ids";
            jdbc.update("UPDATE purchase_pattern SET disabled_flag=1,auto_disabled=1,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND ?=ANY(string_to_array("+column+",','))",group,id);
        }
        return "redirect:/group/masters/"+type+"?deleted";
    }

    @PostMapping("/{type}/order") @ResponseBody @Transactional
    Map<String,Object> order(@PathVariable String type,@RequestParam List<String> ids,HttpSession session) {
        String group=groupForType(session,type); MasterDef d=def(type); int no=1;
        for(String id:ids) jdbc.update("UPDATE "+d.table()+" SET "+d.orderCol()+"=?,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND id=?",no++,group,id);
        return Map.of("ok",true);
    }

    private String groupForType(HttpSession session,String type){Object group=session.getAttribute("activeGroupId");if(group==null)throw new IllegalStateException("所属グループを選択してください");if(!"labels".equals(type)&&!Boolean.TRUE.equals(session.getAttribute("activeGroupLeader")))throw new IllegalStateException("リーダー権限が必要です");return group.toString();}
    private void ensureDefaultLanes(String group){Integer count=jdbc.queryForObject("SELECT count(*) FROM kanban_lane WHERE group_id=?",Integer.class,group);if(count!=null&&count>0)return;int no=1;for(String name:List.of("未着手","進行中","完了"))jdbc.update("INSERT INTO kanban_lane(id,group_id,lane_no,lane_name) VALUES (?,?,?,?)","lane-"+UUID.randomUUID().toString().substring(0,12),group,no++,name);}
    private String formError(String type,String error,String name,String id,RedirectAttributes redirect){redirect.addAttribute("error",error);redirect.addFlashAttribute("formName",name);redirect.addFlashAttribute("formMode",id==null?"create":"edit");if(id!=null)redirect.addFlashAttribute("formId",id);return "redirect:/group/masters/"+type;}
}
