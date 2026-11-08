package com.example.home_api.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/group/patterns")
public class PurchasePatternController {
    private final JdbcTemplate jdbc;
    public PurchasePatternController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    private record Def(String code,String title,String description,boolean search){}
    private Def def(String type){return switch(type){
        case "input-patterns"->new Def("INPUT","購入データ入力パターン管理","購入データ入力時に設定値を自動入力するパターンです。",false);
        case "search-patterns"->new Def("SEARCH","購入データ検索パターン管理","購入データ管理画面へ検索条件を自動入力するパターンです。",true);
        case "templates"->new Def("TEMPLATE","購入データ入力テンプレート管理","定期的な購入データを登録するためのテンプレートです。",false);
        default->throw new IllegalArgumentException("不正なパターン種別です");
    };}

    @GetMapping("/{type}")
    String list(@PathVariable String type,HttpSession session,Model model){
        String group=activeGroup(session);Def d=def(type);disableInvalid(group,d.code());
        model.addAttribute("type",type);model.addAttribute("patternCode",d.code());model.addAttribute("title",d.title());model.addAttribute("description",d.description());model.addAttribute("searchPattern",d.search());
        model.addAttribute("items",jdbc.queryForList("SELECT p.*,(SELECT string_agg(c.category_name,'、' ORDER BY c.category_no) FROM category c WHERE c.id=ANY(string_to_array(COALESCE(p.category_ids,''),','))) AS category_names,(SELECT string_agg(pm.payment_name,'、' ORDER BY pm.payment_no) FROM payment_method pm WHERE pm.id=ANY(string_to_array(COALESCE(p.payment_method_ids,''),','))) AS payment_names,(SELECT string_agg(sl.label_name,'、' ORDER BY sl.label_no) FROM shopping_label sl WHERE sl.id=ANY(string_to_array(COALESCE(p.label_ids,''),','))) AS label_names,(SELECT string_agg(u.user_name,'、' ORDER BY gm.member_no,u.user_name) FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id WHERE gm.group_id=p.group_id AND u.user_id=ANY(string_to_array(COALESCE(p.member_ids,''),','))) AS member_names FROM purchase_pattern p WHERE p.group_id=? AND p.pattern_type=? ORDER BY p.display_no,p.create_date",group,d.code()));
        model.addAttribute("categories",jdbc.queryForList("SELECT id,category_name AS name FROM category WHERE group_id=? AND disabled_flag=0 ORDER BY category_no",group));
        model.addAttribute("payments",jdbc.queryForList("SELECT id,payment_name AS name FROM payment_method WHERE group_id=? AND disabled_flag=0 ORDER BY payment_no",group));
        model.addAttribute("labels",jdbc.queryForList("SELECT id,label_name AS name FROM shopping_label WHERE group_id=? AND disabled_flag=0 ORDER BY label_no",group));
        model.addAttribute("members",jdbc.queryForList("SELECT u.user_id AS id,u.user_name AS name FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id WHERE gm.group_id=? AND gm.approve_flag=1 AND u.delete_flag=0 ORDER BY gm.member_no,u.user_name,u.user_id",group));
        return "group/pattern-list";
    }

    @PostMapping("/{type}") @Transactional
    String create(@PathVariable String type,@RequestParam MultiValueMap<String,String> p,HttpSession session,RedirectAttributes redirect){
        String group=activeGroup(session);Def d=def(type);String name=value(p,"displayName").strip();
        if(!validName(name)||invalidAmountRange(p))return formError(type,"input",p,null,redirect);if(duplicate(group,d.code(),name,null))return formError(type,"duplicate",p,null,redirect);
        Integer no=jdbc.queryForObject("SELECT COALESCE(MAX(display_no),0)+1 FROM purchase_pattern WHERE group_id=? AND pattern_type=?",Integer.class,group,d.code());
        jdbc.update("INSERT INTO purchase_pattern(id,group_id,pattern_type,display_no,display_name,category_ids,payment_method_ids,member_ids,label_ids,transaction_type,amount,settlement_status,date_from,date_to,note,amount_from,amount_to) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            "pat-"+UUID.randomUUID().toString().substring(0,12),group,d.code(),no,name,csv(p,"categoryIds"),csv(p,"paymentIds"),csv(p,"memberIds"),csv(p,"labelIds"),emptyNull(value(p,"transactionType")),longValue(value(p,"amount")),emptyNull(value(p,"settlementStatus")),date(value(p,"dateFrom")),date(value(p,"dateTo")),emptyNull(value(p,"note")),longValue(value(p,"amountFrom")),longValue(value(p,"amountTo")));
        return "redirect:/group/patterns/"+type+"?created";
    }

    @PostMapping("/{type}/{id}/update") @Transactional
    String update(@PathVariable String type,@PathVariable String id,@RequestParam MultiValueMap<String,String> p,HttpSession session,RedirectAttributes redirect){
        String group=activeGroup(session);Def d=def(type);String name=value(p,"displayName").strip();
        if(!validName(name)||invalidAmountRange(p))return formError(type,"input",p,id,redirect);if(duplicate(group,d.code(),name,id))return formError(type,"duplicate",p,id,redirect);
        jdbc.update("UPDATE purchase_pattern SET display_name=?,category_ids=?,payment_method_ids=?,member_ids=?,label_ids=?,transaction_type=?,amount=?,settlement_status=?,date_from=?,date_to=?,note=?,amount_from=?,amount_to=?,auto_disabled=0,update_date=CURRENT_TIMESTAMP WHERE id=? AND group_id=? AND pattern_type=?",
            name,csv(p,"categoryIds"),csv(p,"paymentIds"),csv(p,"memberIds"),csv(p,"labelIds"),emptyNull(value(p,"transactionType")),longValue(value(p,"amount")),emptyNull(value(p,"settlementStatus")),date(value(p,"dateFrom")),date(value(p,"dateTo")),emptyNull(value(p,"note")),longValue(value(p,"amountFrom")),longValue(value(p,"amountTo")),id,group,d.code());
        disableInvalid(group,d.code());return "redirect:/group/patterns/"+type+"?updated";
    }

    @PostMapping("/{type}/{id}/toggle") @Transactional
    String toggle(@PathVariable String type,@PathVariable String id,HttpSession session){
        String group=activeGroup(session);Def d=def(type);disableInvalid(group,d.code());
        var rows=jdbc.queryForList("SELECT disabled_flag,auto_disabled FROM purchase_pattern WHERE id=? AND group_id=? AND pattern_type=?",id,group,d.code());
        if(rows.isEmpty())return "redirect:/group/patterns/"+type;
        int disabled=((Number)rows.get(0).get("disabled_flag")).intValue(),auto=((Number)rows.get(0).get("auto_disabled")).intValue();
        if(disabled==1&&auto==1)return redirect(type,"invalidReference");
        jdbc.update("UPDATE purchase_pattern SET disabled_flag=?,update_date=CURRENT_TIMESTAMP WHERE id=? AND group_id=?",disabled==0?1:0,id,group);
        return "redirect:/group/patterns/"+type;
    }

    @PostMapping("/{type}/{id}/delete") @Transactional
    String delete(@PathVariable String type,@PathVariable String id,HttpSession session){String group=activeGroup(session);Def d=def(type);Integer disabled=jdbc.query("SELECT disabled_flag FROM purchase_pattern WHERE id=? AND group_id=? AND pattern_type=?",rs->rs.next()?rs.getInt(1):null,id,group,d.code());if(disabled==null)return "redirect:/group/patterns/"+type;if(disabled!=1)return redirect(type,"disableFirst");jdbc.update("DELETE FROM purchase_pattern WHERE id=? AND group_id=? AND pattern_type=?",id,group,d.code());return "redirect:/group/patterns/"+type+"?deleted";}

    @PostMapping("/{type}/order") @ResponseBody @Transactional
    Map<String,Object> order(@PathVariable String type,@RequestParam List<String> ids,HttpSession session){
        String group=activeGroup(session);Def d=def(type);int no=1;for(String id:ids)jdbc.update("UPDATE purchase_pattern SET display_no=?,update_date=CURRENT_TIMESTAMP WHERE id=? AND group_id=? AND pattern_type=?",no++,id,group,d.code());return Map.of("ok",true);
    }

    private void disableInvalid(String group,String code){
        Set<String> cats=set("SELECT id FROM category WHERE group_id=? AND disabled_flag=0",group),pays=set("SELECT id FROM payment_method WHERE group_id=? AND disabled_flag=0",group),labels=set("SELECT id FROM shopping_label WHERE group_id=? AND disabled_flag=0",group),members=set("SELECT user_id FROM group_member WHERE group_id=? AND approve_flag=1",group);
        for(Map<String,Object> row:jdbc.queryForList("SELECT id,category_ids,payment_method_ids,label_ids,member_ids FROM purchase_pattern WHERE group_id=? AND pattern_type=?",group,code)){
            boolean invalid=!contained(row.get("category_ids"),cats)||!contained(row.get("payment_method_ids"),pays)||!contained(row.get("label_ids"),labels)||!contained(row.get("member_ids"),members);
            if(invalid)jdbc.update("UPDATE purchase_pattern SET disabled_flag=1,auto_disabled=1,update_date=CURRENT_TIMESTAMP WHERE id=?",row.get("id"));
        }
    }
    private Set<String> set(String sql,String group){return new HashSet<>(jdbc.queryForList(sql,String.class,group));}
    private boolean contained(Object csv,Set<String> active){if(csv==null||String.valueOf(csv).isBlank())return true;return Arrays.stream(String.valueOf(csv).split(",")).allMatch(active::contains);}
    private boolean duplicate(String group,String code,String name,String id){Integer n=id==null?jdbc.queryForObject("SELECT count(*) FROM purchase_pattern WHERE group_id=? AND pattern_type=? AND lower(display_name)=lower(?)",Integer.class,group,code,name):jdbc.queryForObject("SELECT count(*) FROM purchase_pattern WHERE group_id=? AND pattern_type=? AND lower(display_name)=lower(?) AND id<>?",Integer.class,group,code,name,id);return n!=null&&n>0;}
    private boolean validName(String s){return !s.isBlank()&&s.length()<=100;}
    private String value(MultiValueMap<String,String> p,String k){return Optional.ofNullable(p.getFirst(k)).orElse("");}
    private String csv(MultiValueMap<String,String> p,String k){List<String> v=p.get(k);return v==null?"":String.join(",",v.stream().filter(x->x!=null&&!x.isBlank()).distinct().toList());}
    private Object emptyNull(String s){return s==null||s.isBlank()?null:s;}
    private Long longValue(String s){try{return s==null||s.isBlank()?null:Long.parseLong(s);}catch(Exception e){return null;}}
    private boolean invalidAmountRange(MultiValueMap<String,String> p){Long from=longValue(value(p,"amountFrom")),to=longValue(value(p,"amountTo"));return (from!=null&&from<0)||(to!=null&&to<0)||(from!=null&&to!=null&&from>to);}
    private LocalDate date(String s){try{return s==null||s.isBlank()?null:LocalDate.parse(s);}catch(Exception e){return null;}}
    private String redirect(String type,String error){return "redirect:/group/patterns/"+type+"?error="+error;}
    private String formError(String type,String error,MultiValueMap<String,String> values,String id,RedirectAttributes redirect){redirect.addAttribute("error",error);redirect.addFlashAttribute("patternForm",new LinkedHashMap<>(values));redirect.addFlashAttribute("patternFormId",id);return "redirect:/group/patterns/"+type;}
    private String activeGroup(HttpSession session){Object group=session.getAttribute("activeGroupId");if(group==null)throw new IllegalStateException("所属グループを選択してください");return group.toString();}
}
