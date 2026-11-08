package com.example.home_api.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/group/csv")
public class GroupCsvController {
    private static final List<String> MASTER_HEADER=List.of("ID","表示順","表示名","状態");
    private static final List<String> PATTERN_HEADER=List.of("ID","表示順","表示名","カテゴリーID","支払い方法ID","メンバーID","ラベルID","種別","金額","金額From","金額To","精算","日付From","日付To","備考","状態");
    private final JdbcTemplate jdbc;
    public GroupCsvController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    private record Def(String title,String table,String order,String name,String patternType){}
    private Def def(String type){return switch(type){
        case "categories"->new Def("カテゴリー","category","category_no","category_name",null);
        case "payments"->new Def("支払い方法","payment_method","payment_no","payment_name",null);
        case "labels"->new Def("ラベル","shopping_label","label_no","label_name",null);
        case "input-patterns"->new Def("購入データ入力パターン","purchase_pattern","display_no","display_name","INPUT");
        case "search-patterns"->new Def("購入データ検索パターン","purchase_pattern","display_no","display_name","SEARCH");
        case "templates"->new Def("購入データ入力テンプレート","purchase_pattern","display_no","display_name","TEMPLATE");
        default->throw new IllegalArgumentException("不正なCSV種別です");};}

    @GetMapping("/{type}") String page(@PathVariable String type,HttpSession session,Model model){String group=leaderGroup(session);Def d=def(type);Integer count=d.patternType()==null?jdbc.queryForObject("SELECT count(*) FROM "+d.table()+" WHERE group_id=?",Integer.class,group):jdbc.queryForObject("SELECT count(*) FROM purchase_pattern WHERE group_id=? AND pattern_type=?",Integer.class,group,d.patternType());model.addAttribute("type",type);model.addAttribute("title",d.title());model.addAttribute("count",count==null?0:count);model.addAttribute("pattern",d.patternType()!=null);model.addAttribute("importedCount",session.getAttribute("groupCsvImportedCount"));model.addAttribute("importError",session.getAttribute("groupCsvImportError"));session.removeAttribute("groupCsvImportedCount");session.removeAttribute("groupCsvImportError");return "group/group-csv";}

    @GetMapping("/{type}/export") void exportCsv(@PathVariable String type,HttpSession session,HttpServletResponse response)throws IOException{String group=leaderGroup(session);Def d=def(type);response.setCharacterEncoding("UTF-8");response.setContentType("text/csv;charset=UTF-8");response.setHeader("Content-Disposition","attachment; filename="+type+"-"+LocalDate.now()+".csv");try(Writer w=new OutputStreamWriter(response.getOutputStream(),StandardCharsets.UTF_8)){w.write('\uFEFF');if(d.patternType()==null){write(w,MASTER_HEADER);for(Map<String,Object>r:jdbc.queryForList("SELECT id,"+d.order()+" sort_no,"+d.name()+" name,disabled_flag FROM "+d.table()+" WHERE group_id=? ORDER BY "+d.order()+",create_date",group))write(w,List.of(text(r.get("id")),text(r.get("sort_no")),text(r.get("name")),flag(r.get("disabled_flag"))));}else{write(w,PATTERN_HEADER);for(Map<String,Object>r:jdbc.queryForList("SELECT * FROM purchase_pattern WHERE group_id=? AND pattern_type=? ORDER BY display_no,create_date",group,d.patternType()))write(w,List.of(text(r.get("id")),text(r.get("display_no")),text(r.get("display_name")),text(r.get("category_ids")),text(r.get("payment_method_ids")),text(r.get("member_ids")),text(r.get("label_ids")),text(r.get("transaction_type")),text(r.get("amount")),text(r.get("amount_from")),text(r.get("amount_to")),text(r.get("settlement_status")),text(r.get("date_from")),text(r.get("date_to")),text(r.get("note")),flag(r.get("disabled_flag"))));}}}

    @PostMapping("/{type}/import") @Transactional String importCsv(@PathVariable String type,@RequestParam("file")MultipartFile file,HttpSession session){String group=leaderGroup(session);Def d=def(type);try{List<List<String>> rows=read(file,d.patternType()==null?MASTER_HEADER:PATTERN_HEADER);int imported=d.patternType()==null?importMasters(rows,group,d):importPatterns(rows,group,d);session.setAttribute("groupCsvImportedCount",imported);}catch(Exception ex){session.setAttribute("groupCsvImportError",ex.getMessage());}return "redirect:/group/csv/"+type;}

    private int importMasters(List<List<String>> rows,String group,Def d){Set<String> ids=new HashSet<>(),names=new HashSet<>();List<List<String>> valid=new ArrayList<>();for(int i=1;i<rows.size();i++){List<String>r=rows.get(i);if(blank(r))continue;int line=i+1;if(r.size()!=4)throw error(line,"列数が正しくありません。");String id=r.get(0).strip(),name=r.get(2).strip();int no=positive(r.get(1),line,"表示順"),disabled=status(r.get(3),line);if(id.isEmpty()||name.isEmpty()||name.length()>100)throw error(line,"IDまたは表示名が正しくありません。");if(!ids.add(id)||!names.add(name.toLowerCase()))throw error(line,"IDまたは表示名がCSV内で重複しています。");Integer other=jdbc.queryForObject("SELECT count(*) FROM "+d.table()+" WHERE id=? AND group_id<>?",Integer.class,id,group);if(other!=null&&other>0)throw error(line,"別の所属グループで使用中のIDです。");valid.add(List.of(id,String.valueOf(no),name,String.valueOf(disabled)));}if(valid.isEmpty())throw new IllegalArgumentException("インポート対象がありません。");for(List<String>r:valid)jdbc.update("INSERT INTO "+d.table()+"(id,group_id,"+d.order()+","+d.name()+",disabled_flag,create_date,update_date) VALUES (?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT(id) DO UPDATE SET "+d.order()+"=EXCLUDED."+d.order()+","+d.name()+"=EXCLUDED."+d.name()+",disabled_flag=EXCLUDED.disabled_flag,update_date=CURRENT_TIMESTAMP",r.get(0),group,Integer.parseInt(r.get(1)),r.get(2),Integer.parseInt(r.get(3)));return valid.size();}

    private int importPatterns(List<List<String>> rows,String group,Def d){Set<String> categories=set("SELECT id FROM category WHERE group_id=?",group),payments=set("SELECT id FROM payment_method WHERE group_id=?",group),members=set("SELECT user_id FROM group_member WHERE group_id=?",group),labels=set("SELECT id FROM shopping_label WHERE group_id=?",group),ids=new HashSet<>(),names=new HashSet<>();List<List<String>> valid=new ArrayList<>();for(int i=1;i<rows.size();i++){List<String>r=rows.get(i);if(blank(r))continue;int line=i+1;if(r.size()!=PATTERN_HEADER.size())throw error(line,"列数が正しくありません。");String id=r.get(0).strip(),name=r.get(2).strip();positive(r.get(1),line,"表示順");status(r.get(15),line);if(id.isEmpty()||name.isEmpty()||name.length()>100||!ids.add(id)||!names.add(name.toLowerCase()))throw error(line,"IDまたは表示名が空欄・重複しています。");validateIds(r.get(3),categories,line,"カテゴリー");validateIds(r.get(4),payments,line,"支払い方法");validateIds(r.get(5),members,line,"メンバー");validateIds(r.get(6),labels,line,"ラベル");Integer other=jdbc.queryForObject("SELECT count(*) FROM purchase_pattern WHERE id=? AND group_id<>?",Integer.class,id,group);if(other!=null&&other>0)throw error(line,"別の所属グループで使用中のIDです。");valid.add(r);}if(valid.isEmpty())throw new IllegalArgumentException("インポート対象がありません。");for(List<String>r:valid)jdbc.update("INSERT INTO purchase_pattern(id,group_id,pattern_type,display_no,display_name,category_ids,payment_method_ids,member_ids,label_ids,transaction_type,amount,amount_from,amount_to,settlement_status,date_from,date_to,note,disabled_flag,auto_disabled,create_date,update_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT(id) DO UPDATE SET display_no=EXCLUDED.display_no,display_name=EXCLUDED.display_name,category_ids=EXCLUDED.category_ids,payment_method_ids=EXCLUDED.payment_method_ids,member_ids=EXCLUDED.member_ids,label_ids=EXCLUDED.label_ids,transaction_type=EXCLUDED.transaction_type,amount=EXCLUDED.amount,amount_from=EXCLUDED.amount_from,amount_to=EXCLUDED.amount_to,settlement_status=EXCLUDED.settlement_status,date_from=EXCLUDED.date_from,date_to=EXCLUDED.date_to,note=EXCLUDED.note,disabled_flag=EXCLUDED.disabled_flag,auto_disabled=0,update_date=CURRENT_TIMESTAMP",r.get(0).strip(),group,d.patternType(),Integer.parseInt(r.get(1).strip()),r.get(2).strip(),clean(r.get(3)),clean(r.get(4)),clean(r.get(5)),clean(r.get(6)),nullIfBlank(r.get(7)),longOrNull(r.get(8)),longOrNull(r.get(9)),longOrNull(r.get(10)),nullIfBlank(r.get(11)),dateOrNull(r.get(12)),dateOrNull(r.get(13)),nullIfBlank(r.get(14)),status(r.get(15),0));return valid.size();}

    private List<List<String>> read(MultipartFile file,List<String> header)throws IOException{if(file==null||file.isEmpty())throw new IllegalArgumentException("CSVファイルを選択してください。");String s=CsvTextDecoder.decode(file.getBytes());List<List<String>>rows=parse(s);if(rows.isEmpty()||!rows.get(0).equals(header))throw new IllegalArgumentException("CSVヘッダーが正しくありません。対象画面からエクスポートした形式を使用してください。");return rows;}
    private List<List<String>> parse(String s){List<List<String>>out=new ArrayList<>();List<String>row=new ArrayList<>();StringBuilder f=new StringBuilder();boolean q=false;for(int i=0;i<s.length();i++){char c=s.charAt(i);if(q){if(c=='"'){if(i+1<s.length()&&s.charAt(i+1)=='"'){f.append('"');i++;}else q=false;}else f.append(c);}else if(c=='"')q=true;else if(c==','){row.add(f.toString());f.setLength(0);}else if(c=='\n'){row.add(f.toString());f.setLength(0);out.add(row);row=new ArrayList<>();}else if(c!='\r')f.append(c);}if(q)throw new IllegalArgumentException("CSVの引用符が閉じられていません。");if(f.length()>0||!row.isEmpty()){row.add(f.toString());out.add(row);}return out;}
    private void write(Writer w,List<String>v)throws IOException{for(int i=0;i<v.size();i++){if(i>0)w.write(',');w.write('"'+v.get(i).replace("\"","\"\"")+'"');}w.write("\r\n");}
    private Set<String>set(String sql,String group){return new HashSet<>(jdbc.queryForList(sql,String.class,group));}
    private void validateIds(String csv,Set<String>valid,int line,String name){for(String id:csv.split(",")){id=id.strip();if(!id.isEmpty()&&!valid.contains(id))throw error(line,name+"ID「"+id+"」が存在しません。");}}
    private int positive(String s,int line,String name){try{int n=Integer.parseInt(s.strip());if(n<1)throw new Exception();return n;}catch(Exception e){throw error(line,name+"は1以上の整数で入力してください。");}}
    private int status(String s,int line){return switch(s.strip()){case "有効","0"->0;case "無効","1"->1;default->throw error(line,"状態は有効または無効を指定してください。");};}
    private String flag(Object o){return o instanceof Number n&&n.intValue()==1?"無効":"有効";}
    private boolean blank(List<String>r){return r.size()==1&&r.get(0).isBlank();}
    private String clean(String s){return String.join(",",Arrays.stream(s.split(",")).map(String::strip).filter(x->!x.isEmpty()).distinct().toList());}
    private String nullIfBlank(String s){s=s.strip();return s.isEmpty()?null:s;}
    private Long longOrNull(String s){s=s.strip();if(s.isEmpty())return null;try{return Long.parseLong(s);}catch(Exception e){throw new IllegalArgumentException("金額は整数で入力してください。");}}
    private java.sql.Date dateOrNull(String s){s=s.strip();return s.isEmpty()?null:java.sql.Date.valueOf(s);}
    private IllegalArgumentException error(int line,String m){return new IllegalArgumentException(line+"行目："+m);}
    private String text(Object o){return o==null?"":String.valueOf(o);}
    private String leaderGroup(HttpSession s){Object g=s.getAttribute("activeGroupId");if(g==null)throw new IllegalStateException("所属グループを選択してください");if(!Boolean.TRUE.equals(s.getAttribute("activeGroupLeader")))throw new IllegalStateException("リーダー権限が必要です");return g.toString();}
}
