package com.example.home_api.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
@RequestMapping("/group/purchases/csv")
public class PurchaseCsvController {
    private static final List<String> HEADER=List.of("購入日","購入者ID","カテゴリーID","種別","支払い方法ID","金額","精算","ラベルID","備考");
    private static final List<String> BUDGET_HEADER=List.of("年","月","カテゴリーID","予算額");
    private static final List<String> COMMUNICATION_HEADER=List.of("いただき日","from","to","いただきもの","金額","説明","おかえし日","おかえし品物","おかえし金額","おかえし説明","返却状況");
    private final JdbcTemplate jdbc;
    public PurchaseCsvController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    String page(@RequestParam(defaultValue="purchases")String type,HttpSession session,Model model){
        String group=group(session);
        if(!Set.of("purchases","budgets","communications").contains(type))type="purchases";
        model.addAttribute("csvType",type);
        model.addAttribute("csvTitle",switch(type){case "budgets"->"予算";case "communications"->"お付き合い帳";default->"購入データ";});
        Integer count=jdbc.queryForObject("SELECT count(*) FROM shopping WHERE group_id=?",Integer.class,group);
        model.addAttribute("purchaseCount",count==null?0:count);
        model.addAttribute("budgetCount",jdbc.queryForObject("SELECT count(*) FROM budgets WHERE group_id=?",Integer.class,group));
        model.addAttribute("communicationCount",jdbc.queryForObject("SELECT count(*) FROM communication WHERE group_id=?",Integer.class,group));
        model.addAttribute("importedCount",session.getAttribute("csvImportedCount"));
        model.addAttribute("importedType",session.getAttribute("csvImportedType"));
        model.addAttribute("importError",session.getAttribute("csvImportError"));
        session.removeAttribute("csvImportedCount");session.removeAttribute("csvImportedType");session.removeAttribute("csvImportError");
        return "group/purchase-csv";
    }

    @GetMapping("/export")
    void exportCsv(HttpSession session,HttpServletResponse response) throws IOException {
        String group=group(session);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT shopping_date,shopping_user_id,category_id,type,payment_method_id,amount,settlement,shopping_label_ids,remarks FROM shopping WHERE group_id=? ORDER BY shopping_date,id",group);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment; filename=purchase-data-"+LocalDate.now()+".csv");
        OutputStream out=response.getOutputStream();
        out.write(new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF});
        try(Writer writer=new OutputStreamWriter(out,StandardCharsets.UTF_8)){
            writeRow(writer,HEADER);
            for(Map<String,Object> row:rows){
                LocalDate date=((Timestamp)row.get("shopping_date")).toLocalDateTime().toLocalDate();
                writeRow(writer,List.of(date.toString(),text(row.get("shopping_user_id")),text(row.get("category_id")),number(row.get("type"))==2?"収入":"支出",text(row.get("payment_method_id")),text(row.get("amount")),number(row.get("settlement"))==2?"精算済":"未精算",text(row.get("shopping_label_ids")),text(row.get("remarks"))));
            }
        }
    }

    @PostMapping("/import")
    @Transactional
    String importCsv(@RequestParam("file") MultipartFile file,HttpSession session,Authentication authentication){
        String group=group(session);
        List<PurchaseRow> purchases;
        try{purchases=parseAndValidate(file,group);}
        catch(IllegalArgumentException|IOException ex){session.setAttribute("csvImportError",ex.getMessage());return "redirect:/group/purchases/csv";}
        for(PurchaseRow row:purchases){
            jdbc.update("INSERT INTO shopping(group_id,shopping_date,shopping_user_id,category_id,type,payment,settlement,amount,shopping_label_id,remarks,create_user_id,fixed_flag,create_date,update_date,payment_method_id,shopping_label_ids) VALUES (?,?,?,?,?,0,?,?,NULL,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,?)",group,Timestamp.valueOf(row.date().atStartOfDay()),row.memberId(),row.categoryId(),row.type(),row.settlement(),row.amount(),row.remarks(),authentication.getName(),row.paymentId(),row.labelIds());
        }
        imported(session,"購入データ",purchases.size());
        return "redirect:/group/purchases/csv";
    }

    @GetMapping("/budgets/export")
    void exportBudgets(HttpSession session,HttpServletResponse response)throws IOException{
        String group=group(session);prepareCsv(response,"budgets-"+LocalDate.now()+".csv");
        try(Writer writer=new OutputStreamWriter(response.getOutputStream(),StandardCharsets.UTF_8)){writer.write('\uFEFF');writeRow(writer,BUDGET_HEADER);for(Map<String,Object> row:jdbc.queryForList("SELECT year,month,category_id,amount FROM budgets WHERE group_id=? ORDER BY year,month,category_id",group))writeRow(writer,List.of(text(row.get("year")),text(row.get("month")),text(row.get("category_id")),text(row.get("amount"))));}
    }

    @PostMapping("/budgets/import") @Transactional
    String importBudgets(@RequestParam("file")MultipartFile file,HttpSession session){
        String group=group(session);List<BudgetRow> rows;
        try{rows=parseBudgets(file,group);}catch(IllegalArgumentException|IOException ex){failed(session,ex);return "redirect:/group/purchases/csv?type=budgets";}
        for(BudgetRow row:rows)jdbc.update("INSERT INTO budgets(group_id,year,month,category_id,amount,fixed_flag,create_date,update_date) VALUES (?,?,?,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT(group_id,year,month,category_id) DO UPDATE SET amount=EXCLUDED.amount,update_date=CURRENT_TIMESTAMP",group,row.year(),row.month(),row.categoryId(),row.amount());
        imported(session,"予算",rows.size());return "redirect:/group/purchases/csv?type=budgets";
    }

    @GetMapping("/communications/export")
    void exportCommunications(HttpSession session,HttpServletResponse response)throws IOException{
        String group=group(session);prepareCsv(response,"communications-"+LocalDate.now()+".csv");
        try(Writer writer=new OutputStreamWriter(response.getOutputStream(),StandardCharsets.UTF_8)){writer.write('\uFEFF');writeRow(writer,COMMUNICATION_HEADER);for(Map<String,Object> row:jdbc.queryForList("SELECT * FROM communication WHERE group_id=? ORDER BY receive_date,id",group)){writeRow(writer,List.of(dateText(row.get("receive_date")),text(row.get("receive_from")),text(row.get("receive_to")),text(row.get("receive_item")),text(row.get("receive_amount")),text(row.get("receive_remarks")),dateText(row.get("return_date")),text(row.get("return_item")),text(row.get("return_amount")),text(row.get("return_remarks")),returnStatus(numberOrZero(row.get("return_flag")))));}}
    }

    @PostMapping("/communications/import") @Transactional
    String importCommunications(@RequestParam("file")MultipartFile file,HttpSession session){
        String group=group(session);List<CommunicationRow> rows;
        try{rows=parseCommunications(file);}catch(IllegalArgumentException|IOException ex){failed(session,ex);return "redirect:/group/purchases/csv?type=communications";}
        for(CommunicationRow row:rows)jdbc.update("INSERT INTO communication(group_id,receive_date,receive_from,receive_to,receive_item,receive_amount,receive_remarks,return_date,return_item,return_amount,return_remarks,return_flag,create_date,update_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",group,Timestamp.valueOf(row.receiveDate().atStartOfDay()),row.from(),row.to(),row.item(),String.valueOf(row.amount()),row.remarks(),row.returnDate()==null?null:Timestamp.valueOf(row.returnDate().atStartOfDay()),row.returnItem(),row.returnAmount()==null?null:String.valueOf(row.returnAmount()),row.returnRemarks(),row.returnFlag());
        imported(session,"お付き合い帳",rows.size());return "redirect:/group/purchases/csv?type=communications";
    }

    private List<PurchaseRow> parseAndValidate(MultipartFile file,String group) throws IOException {
        if(file==null||file.isEmpty())throw new IllegalArgumentException("CSVファイルを選択してください。");
        String source=CsvTextDecoder.decode(file.getBytes());
        List<List<String>> rows=parseCsv(source);
        if(rows.isEmpty())throw new IllegalArgumentException("CSVファイルが空です。");
        if(!rows.get(0).equals(HEADER))throw new IllegalArgumentException("CSVのヘッダーが正しくありません。エクスポートした形式を使用してください。");
        Set<String> members=new HashSet<>(jdbc.queryForList("SELECT user_id FROM group_member WHERE group_id=? AND approve_flag=1",String.class,group));
        Set<String> categories=new HashSet<>(jdbc.queryForList("SELECT id FROM category WHERE group_id=?",String.class,group));
        Set<String> payments=new HashSet<>(jdbc.queryForList("SELECT id FROM payment_method WHERE group_id=?",String.class,group));
        Set<String> labels=new HashSet<>(jdbc.queryForList("SELECT id FROM shopping_label WHERE group_id=?",String.class,group));
        Set<String> closed=new HashSet<>();for(Map<String,Object>x:jdbc.queryForList("SELECT year,month FROM fixed WHERE group_id=?",group))closed.add(number(x.get("year"))+"-"+number(x.get("month")));
        List<PurchaseRow> result=new ArrayList<>();
        for(int i=1;i<rows.size();i++){
            List<String> row=rows.get(i);int line=i+1;
            if(row.size()==1&&row.get(0).isBlank())continue;
            if(row.size()!=HEADER.size())throw error(line,"列数が正しくありません。");
            LocalDate date;try{date=LocalDate.parse(row.get(0).strip());}catch(DateTimeParseException ex){throw error(line,"購入日はyyyy-MM-dd形式で入力してください。");}
            String member=row.get(1).strip(),category=row.get(2).strip(),payment=row.get(4).strip();
            if(!members.contains(member))throw error(line,"購入者IDが所属グループの承認済みメンバーではありません。");
            if(!categories.contains(category))throw error(line,"カテゴリーIDが存在しません。");
            if(!payments.contains(payment))throw error(line,"支払い方法IDが存在しません。");
            int type=switch(row.get(3).strip()){case "支出","EXPENSE","1"->1;case "収入","INCOME","2"->2;default->throw error(line,"種別は支出または収入を指定してください。");};
            int amount;try{amount=Integer.parseInt(row.get(5).strip());}catch(NumberFormatException ex){throw error(line,"金額は整数で入力してください。");}
            if(amount<1)throw error(line,"金額は1円以上で入力してください。");
            int settlement=switch(row.get(6).strip()){case "未精算","UNSETTLED","1"->1;case "精算済","SETTLED","2"->2;default->throw error(line,"精算は未精算または精算済を指定してください。");};
            String labelCsv=normalizeLabels(row.get(7),labels,line);
            if(closed.contains(date.getYear()+"-"+date.getMonthValue()))throw error(line,"月次確定済みの年月にはインポートできません。");
            result.add(new PurchaseRow(date,member,category,type,payment,amount,settlement,labelCsv,row.get(8)));
        }
        if(result.isEmpty())throw new IllegalArgumentException("インポート対象の購入データがありません。");
        return result;
    }

    private String normalizeLabels(String value,Set<String> labels,int line){
        if(value==null||value.isBlank())return "";
        List<String> result=new ArrayList<>();
        for(String id:value.split(",")){String normalized=id.strip();if(normalized.isEmpty())continue;if(!labels.contains(normalized))throw error(line,"ラベルID「"+normalized+"」が存在しません。");if(!result.contains(normalized))result.add(normalized);}
        return String.join(",",result);
    }

    private List<BudgetRow> parseBudgets(MultipartFile file,String group)throws IOException{
        List<List<String>> rows=readRows(file,BUDGET_HEADER);Set<String> categories=new HashSet<>(jdbc.queryForList("SELECT id FROM category WHERE group_id=?",String.class,group));Set<String> keys=new HashSet<>();List<BudgetRow> result=new ArrayList<>();
        for(int i=1;i<rows.size();i++){List<String> row=rows.get(i);int line=i+1;if(blankRow(row))continue;if(row.size()!=BUDGET_HEADER.size())throw error(line,"列数が正しくありません。");int year=parseInt(row.get(0),line,"年"),month=parseInt(row.get(1),line,"月"),amount=parseInt(row.get(3),line,"予算額");String category=row.get(2).strip();if(year<1900||year>9999)throw error(line,"年が正しくありません。");if(month<1||month>12)throw error(line,"月は1～12で入力してください。");if(!categories.contains(category))throw error(line,"カテゴリーIDが存在しません。");if(amount<0)throw error(line,"予算額は0以上で入力してください。");Integer fixed=jdbc.queryForObject("SELECT count(*) FROM fixed WHERE group_id=? AND year=? AND month=?",Integer.class,group,year,month);if(fixed!=null&&fixed>0)throw error(line,"月次確定済みの年月にはインポートできません。");if(!keys.add(year+":"+month+":"+category))throw error(line,"同じ年月・カテゴリーがCSV内で重複しています。");result.add(new BudgetRow(year,month,category,amount));}
        if(result.isEmpty())throw new IllegalArgumentException("インポート対象の予算データがありません。");return result;
    }

    private List<CommunicationRow> parseCommunications(MultipartFile file)throws IOException{
        List<List<String>> rows=readRows(file,COMMUNICATION_HEADER);List<CommunicationRow> result=new ArrayList<>();
        for(int i=1;i<rows.size();i++){List<String> row=rows.get(i);int line=i+1;if(blankRow(row))continue;if(row.size()!=COMMUNICATION_HEADER.size())throw error(line,"列数が正しくありません。");LocalDate received=parseDate(row.get(0),line,"いただき日",true),returned=parseDate(row.get(6),line,"おかえし日",false);String from=row.get(1).strip(),to=row.get(2).strip(),item=row.get(3).strip();if(from.isEmpty()||to.isEmpty()||item.isEmpty())throw error(line,"from、to、いただきものは必須です。");if(from.length()>200||to.length()>200||item.length()>200||row.get(5).length()>500||row.get(7).length()>200||row.get(9).length()>500)throw error(line,"文字数が上限を超えています。");int amount=parseInt(row.get(4),line,"金額");if(amount<0)throw error(line,"金額は0以上で入力してください。");Integer returnAmount=row.get(8).isBlank()?null:parseInt(row.get(8),line,"おかえし金額");if(returnAmount!=null&&returnAmount<0)throw error(line,"おかえし金額は0以上で入力してください。");int flag=parseReturnStatus(row.get(10),line);result.add(new CommunicationRow(received,from,to,item,amount,row.get(5),returned,row.get(7),returnAmount,row.get(9),flag));}
        if(result.isEmpty())throw new IllegalArgumentException("インポート対象のお付き合い帳データがありません。");return result;
    }

    private List<List<String>> readRows(MultipartFile file,List<String> header)throws IOException{if(file==null||file.isEmpty())throw new IllegalArgumentException("CSVファイルを選択してください。");String source=CsvTextDecoder.decode(file.getBytes());List<List<String>> rows=parseCsv(source);if(rows.isEmpty())throw new IllegalArgumentException("CSVファイルが空です。");if(!rows.get(0).equals(header))throw new IllegalArgumentException("CSVのヘッダーが正しくありません。対象データをエクスポートした形式を使用してください。");return rows;}
    private boolean blankRow(List<String> row){return row.size()==1&&row.get(0).isBlank();}
    private int parseInt(String value,int line,String name){try{return Integer.parseInt(value.strip());}catch(NumberFormatException ex){throw error(line,name+"は整数で入力してください。");}}
    private LocalDate parseDate(String value,int line,String name,boolean required){if(value==null||value.isBlank()){if(required)throw error(line,name+"は必須です。");return null;}try{return LocalDate.parse(value.strip());}catch(DateTimeParseException ex){throw error(line,name+"はyyyy-MM-dd形式で入力してください。");}}
    private int parseReturnStatus(String value,int line){return switch(value.strip()){case "未返却","0"->0;case "返却済","1"->1;case "返却不要","2"->2;default->throw error(line,"返却状況は未返却、返却済、返却不要のいずれかを指定してください。");};}
    private String returnStatus(int flag){return flag==1?"返却済":flag==2?"返却不要":"未返却";}
    private String dateText(Object value){if(value==null)return "";if(value instanceof Timestamp t)return t.toLocalDateTime().toLocalDate().toString();if(value instanceof java.sql.Date d)return d.toLocalDate().toString();return String.valueOf(value);}
    private int numberOrZero(Object value){return value==null?0:number(value);}
    private void prepareCsv(HttpServletResponse response,String filename){response.setCharacterEncoding(StandardCharsets.UTF_8.name());response.setContentType("text/csv;charset=UTF-8");response.setHeader("Content-Disposition","attachment; filename="+filename);}
    private void imported(HttpSession session,String type,int count){session.setAttribute("csvImportedType",type);session.setAttribute("csvImportedCount",count);}
    private void failed(HttpSession session,Exception ex){session.setAttribute("csvImportError",ex.getMessage());}

    private List<List<String>> parseCsv(String source){
        List<List<String>> rows=new ArrayList<>();List<String> row=new ArrayList<>();StringBuilder field=new StringBuilder();boolean quoted=false;
        for(int i=0;i<source.length();i++){char c=source.charAt(i);if(quoted){if(c=='"'){if(i+1<source.length()&&source.charAt(i+1)=='"'){field.append('"');i++;}else quoted=false;}else field.append(c);}else if(c=='"')quoted=true;else if(c==','){row.add(field.toString());field.setLength(0);}else if(c=='\n'){row.add(field.toString());field.setLength(0);rows.add(row);row=new ArrayList<>();}else if(c!='\r')field.append(c);}
        if(quoted)throw new IllegalArgumentException("CSVの引用符が閉じられていません。");
        if(field.length()>0||!row.isEmpty()){row.add(field.toString());rows.add(row);}
        return rows;
    }

    private static void writeRow(Writer writer,List<String> fields) throws IOException {for(int i=0;i<fields.size();i++){if(i>0)writer.write(',');writer.write(csv(fields.get(i)));}writer.write("\r\n");}
    private static String csv(String value){String s=value==null?"":value;return "\""+s.replace("\"","\"\"")+"\"";}
    private IllegalArgumentException error(int line,String message){return new IllegalArgumentException(line+"行目："+message);}
    private String group(HttpSession session){Object value=session.getAttribute("activeGroupId");if(value==null)throw new IllegalStateException("所属グループを選択してください");if(!Boolean.TRUE.equals(session.getAttribute("activeGroupLeader")))throw new IllegalStateException("リーダー権限が必要です");return value.toString();}
    private static String text(Object value){return value==null?"":String.valueOf(value);}
    private static int number(Object value){return value instanceof Number n?n.intValue():Integer.parseInt(String.valueOf(value));}
    private record PurchaseRow(LocalDate date,String memberId,String categoryId,int type,String paymentId,int amount,int settlement,String labelIds,String remarks){}
    private record BudgetRow(int year,int month,String categoryId,int amount){}
    private record CommunicationRow(LocalDate receiveDate,String from,String to,String item,int amount,String remarks,LocalDate returnDate,String returnItem,Integer returnAmount,String returnRemarks,int returnFlag){}
}
