package com.example.home_api.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.Collator;
import java.text.Normalizer;
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
        String colorColumn="kanban-lanes".equals(type)?",lane_color AS color":",NULL::varchar AS color";
        model.addAttribute("items",jdbc.queryForList("SELECT id,"+d.nameCol()+" AS name,"+d.orderCol()+" AS sort_no,disabled_flag"+colorColumn+" FROM "+d.table()+" WHERE group_id=? ORDER BY "+d.orderCol()+",create_date",group));
        return "group/master-list";
    }

    @PostMapping("/{type}") @Transactional
    String create(@PathVariable String type,@RequestParam String name,@RequestParam(defaultValue="#f8f6f1") String color,HttpSession session,RedirectAttributes redirect) {
        String group=groupForType(session,type); MasterDef d=def(type); String value=name.strip();
        if(value.isEmpty()||value.length()>100||("kanban-lanes".equals(type)&&!validColor(color))) return formError(type,"input",value,null,color,redirect);
        Integer duplicate=jdbc.queryForObject("SELECT count(*) FROM "+d.table()+" WHERE group_id=? AND lower("+d.nameCol()+")=lower(?)",Integer.class,group,value);
        if(duplicate!=null&&duplicate>0) return formError(type,"duplicate",value,null,color,redirect);
        Integer next=jdbc.queryForObject("SELECT COALESCE(MAX("+d.orderCol()+"::integer),0)+1 FROM "+d.table()+" WHERE group_id=?",Integer.class,group);
        String id="kanban-lanes".equals(type)?"lane-"+UUID.randomUUID().toString().substring(0,12):UUID.randomUUID().toString();
        try {
            if("kanban-lanes".equals(type))jdbc.update("INSERT INTO kanban_lane(id,group_id,lane_no,lane_name,lane_color,disabled_flag,create_date,update_date) VALUES (?,?,?,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",id,group,next,value,color.toLowerCase(Locale.ROOT));
            else jdbc.update("INSERT INTO "+d.table()+"(id,group_id,"+d.orderCol()+","+d.nameCol()+",disabled_flag,create_date,update_date) VALUES (?,?,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",id,group,next,value);
        }
        catch(Exception ex){ return formError(type,"duplicate",value,null,color,redirect); }
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
    String update(@PathVariable String type,@PathVariable String id,@RequestParam String name,@RequestParam(defaultValue="#f8f6f1") String color,HttpSession session,RedirectAttributes redirect) {
        String group=groupForType(session,type); MasterDef d=def(type); String value=name.strip();
        if(value.isEmpty()||value.length()>100||("kanban-lanes".equals(type)&&!validColor(color))) return formError(type,"input",value,id,color,redirect);
        Integer duplicate=jdbc.queryForObject("SELECT count(*) FROM "+d.table()+" WHERE group_id=? AND lower("+d.nameCol()+")=lower(?) AND id<>?",Integer.class,group,value,id);
        if(duplicate!=null&&duplicate>0) return formError(type,"duplicate",value,id,color,redirect);
        if("kanban-lanes".equals(type))jdbc.update("UPDATE kanban_lane SET lane_name=?,lane_color=?,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND id=?",value,color.toLowerCase(Locale.ROOT),group,id);
        else jdbc.update("UPDATE "+d.table()+" SET "+d.nameCol()+"=?,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND id=?",value,group,id);
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

    @PostMapping("/{type}/auto-order") @Transactional
    String autoOrder(@PathVariable String type,@RequestParam String sortBy,@RequestParam String direction,HttpSession session) {
        String group=groupForType(session,type);MasterDef d=def(type);
        String order="desc".equalsIgnoreCase(direction)?"DESC":"ASC";
        List<String>ids;
        if("name".equals(sortBy)){
            List<Map<String,Object>>rows=jdbc.queryForList("SELECT id,"+d.nameCol()+" AS name FROM "+d.table()+" WHERE group_id=?",group);
            Comparator<Map<String,Object>>comparator=Comparator.comparingInt((Map<String,Object> row)->nameCategory(Objects.toString(row.get("name"),""))).thenComparing(row->normalizedName(Objects.toString(row.get("name"),"")),japaneseCollator()).thenComparing(row->Objects.toString(row.get("id"),""));
            if("DESC".equals(order))comparator=comparator.reversed();
            rows.sort(comparator);ids=rows.stream().map(row->Objects.toString(row.get("id"),"")).toList();
        }else{
            String column=switch(sortBy){case "created"->"create_date";case "updated"->"update_date";case "status"->"disabled_flag";default->throw new IllegalArgumentException("不正な並び替え条件です");};
            ids=jdbc.queryForList("SELECT id FROM "+d.table()+" WHERE group_id=? ORDER BY "+column+" "+order+" NULLS LAST,id ASC",String.class,group);
        }
        int no=1;for(String id:ids)jdbc.update("UPDATE "+d.table()+" SET "+d.orderCol()+"=? WHERE group_id=? AND id=?",no++,group,id);
        return "redirect:/group/masters/"+type+"?sorted";
    }

    private Collator japaneseCollator(){Collator collator=Collator.getInstance(Locale.JAPANESE);collator.setStrength(Collator.TERTIARY);return collator;}
    private String normalizedName(String value){return Normalizer.normalize(value==null?"":value,Normalizer.Form.NFKC);}
    private int nameCategory(String value){String normalized=normalizedName(value);if(normalized.isBlank())return 4;int code=normalized.codePointAt(0);if(Character.isDigit(code))return 0;if((code>='A'&&code<='Z')||(code>='a'&&code<='z'))return 1;if(code>=0x3040&&code<=0x30ff)return 2;if(Character.UnicodeScript.of(code)==Character.UnicodeScript.HAN)return 3;return 4;}

    private String groupForType(HttpSession session,String type){Object group=session.getAttribute("activeGroupId");if(group==null)throw new IllegalStateException("所属グループを選択してください");if(!"labels".equals(type)&&!Boolean.TRUE.equals(session.getAttribute("activeGroupLeader")))throw new IllegalStateException("リーダー権限が必要です");return group.toString();}
    private void ensureDefaultLanes(String group){Integer count=jdbc.queryForObject("SELECT count(*) FROM kanban_lane WHERE group_id=?",Integer.class,group);if(count!=null&&count>0)return;int no=1;for(String name:List.of("未着手","進行中","完了"))jdbc.update("INSERT INTO kanban_lane(id,group_id,lane_no,lane_name) VALUES (?,?,?,?)","lane-"+UUID.randomUUID().toString().substring(0,12),group,no++,name);}
    private boolean validColor(String color){return color!=null&&color.matches("#[0-9A-Fa-f]{6}");}
    private String formError(String type,String error,String name,String id,String color,RedirectAttributes redirect){redirect.addAttribute("error",error);redirect.addFlashAttribute("formName",name);redirect.addFlashAttribute("formColor",color);redirect.addFlashAttribute("formMode",id==null?"create":"edit");if(id!=null)redirect.addFlashAttribute("formId",id);return "redirect:/group/masters/"+type;}
}
