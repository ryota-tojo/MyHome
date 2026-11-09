package com.example.home_api.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/group/kanban")
public class KanbanController {
    private static final Set<String> COLORS=Set.of("YELLOW","BLUE","GREEN","PINK","WHITE");
    private final JdbcTemplate jdbc;
    public KanbanController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    String board(HttpSession session,Model model){
        String group=activeGroup(session);
        ensureDefaultLanes(group);
        var lanes=jdbc.queryForList("SELECT id,lane_name AS name,lane_no,lane_color AS color FROM kanban_lane WHERE group_id=? AND disabled_flag=0 ORDER BY lane_no,create_date",group);
        var cards=jdbc.queryForList("SELECT c.* FROM kanban_card c JOIN kanban_lane l ON l.id=c.status AND l.group_id=c.group_id WHERE c.group_id=? ORDER BY l.lane_no,c.display_no,c.create_date",group);
        model.addAttribute("lanes",lanes);
        model.addAttribute("cards",cards);
        Map<String,Long> counts=new HashMap<>();for(var lane:lanes){String id=String.valueOf(lane.get("id"));counts.put(id,cards.stream().filter(c->id.equals(c.get("status"))).count());}model.addAttribute("cardCounts",counts);
        return "group/kanban";
    }

    @PostMapping @Transactional
    String create(@RequestParam String title,@RequestParam(required=false,defaultValue="") String content,@RequestParam String status,@RequestParam(defaultValue="YELLOW") String color,HttpSession session,Authentication auth){
        String group=activeGroup(session);title=title.strip();content=content.strip();
        if(!valid(group,title,content,status,color))return "redirect:/group/kanban?error=input";
        Integer no=jdbc.queryForObject("SELECT COALESCE(MAX(display_no),0)+1 FROM kanban_card WHERE group_id=? AND status=?",Integer.class,group,status);
        jdbc.update("INSERT INTO kanban_card(id,group_id,title,content,status,display_no,color,created_by) VALUES (?,?,?,?,?,?,?,?)","kbn-"+UUID.randomUUID().toString().substring(0,12),group,title,content,status,no,color,auth.getName());
        return "redirect:/group/kanban?created";
    }

    @PostMapping("/{id}/update") @Transactional
    String update(@PathVariable String id,@RequestParam String title,@RequestParam(required=false,defaultValue="") String content,@RequestParam String status,@RequestParam String color,HttpSession session){
        String group=activeGroup(session);title=title.strip();content=content.strip();
        if(!valid(group,title,content,status,color))return "redirect:/group/kanban?error=input";
        jdbc.update("UPDATE kanban_card SET title=?,content=?,status=?,color=?,update_date=CURRENT_TIMESTAMP WHERE id=? AND group_id=?",title,content,status,color,id,group);
        normalize(group);
        return "redirect:/group/kanban?updated";
    }

    @PostMapping("/{id}/delete") @Transactional
    String delete(@PathVariable String id,HttpSession session){
        String group=activeGroup(session);jdbc.update("DELETE FROM kanban_card WHERE id=? AND group_id=?",id,group);normalize(group);
        return "redirect:/group/kanban?deleted";
    }

    @PostMapping("/order") @ResponseBody @Transactional
    Map<String,Object> order(@RequestParam String status,@RequestParam(required=false) List<String> ids,HttpSession session){
        String group=activeGroup(session);if(!laneExists(group,status,false))throw new IllegalArgumentException("不正なレーンです");
        int no=1;for(String id:ids==null?List.<String>of():ids)jdbc.update("UPDATE kanban_card SET status=?,display_no=?,update_date=CURRENT_TIMESTAMP WHERE id=? AND group_id=?",status,no++,id,group);
        normalize(group);return Map.of("ok",true);
    }

    private boolean valid(String group,String title,String content,String status,String color){return !title.isEmpty()&&title.length()<=100&&content.length()<=2000&&laneExists(group,status,false)&&COLORS.contains(color);}
    private boolean laneExists(String group,String lane,boolean includeDisabled){Integer count=jdbc.queryForObject("SELECT count(*) FROM kanban_lane WHERE group_id=? AND id=?"+(includeDisabled?"":" AND disabled_flag=0"),Integer.class,group,lane);return count!=null&&count>0;}
    private void normalize(String group){for(Map<String,Object> lane:jdbc.queryForList("SELECT id FROM kanban_lane WHERE group_id=?",group)){String status=String.valueOf(lane.get("id"));int[] no={1};jdbc.queryForList("SELECT id FROM kanban_card WHERE group_id=? AND status=? ORDER BY display_no,update_date",group,status).forEach(r->jdbc.update("UPDATE kanban_card SET display_no=? WHERE id=?",no[0]++,r.get("id")));}}
    private void ensureDefaultLanes(String group){Integer count=jdbc.queryForObject("SELECT count(*) FROM kanban_lane WHERE group_id=?",Integer.class,group);if(count!=null&&count>0)return;int no=1;for(String name:List.of("未着手","進行中","完了"))jdbc.update("INSERT INTO kanban_lane(id,group_id,lane_no,lane_name) VALUES (?,?,?,?)","lane-"+UUID.randomUUID().toString().substring(0,12),group,no++,name);}
    private String activeGroup(HttpSession session){Object group=session.getAttribute("activeGroupId");if(group==null)throw new IllegalStateException("所属グループを選択してください");return group.toString();}
}
