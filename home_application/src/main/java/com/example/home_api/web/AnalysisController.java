package com.example.home_api.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/group/analysis")
public class AnalysisController {
    private final JdbcTemplate jdbc;
    public AnalysisController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping("/budget-actual")
    String budgetActual(@RequestParam(required=false) Integer year,@RequestParam(required=false) List<Integer> months,HttpSession session,Model model){
        String group=group(session);int y=year==null?LocalDate.now().getYear():year;List<Integer> selected=normalizeMonths(months);
        String marks=String.join(",",Collections.nCopies(selected.size(),"?"));List<Object> budgetArgs=new ArrayList<>(List.of(y,group));budgetArgs.addAll(selected);List<Object> shoppingArgs=new ArrayList<>(List.of(y,group));shoppingArgs.addAll(selected);List<Object> args=new ArrayList<>();args.addAll(budgetArgs);args.addAll(shoppingArgs);args.add(group);
        String sql="SELECT c.id,c.category_name,COALESCE(b.budget,0) budget,COALESCE(s.expense,0) expense,COALESCE(s.income,0) income FROM category c LEFT JOIN (SELECT category_id,SUM(amount) budget FROM budgets WHERE year=? AND group_id=? AND month IN ("+marks+") GROUP BY category_id)b ON b.category_id=c.id LEFT JOIN (SELECT category_id,SUM(CASE WHEN type=1 THEN amount ELSE 0 END) expense,SUM(CASE WHEN type=2 THEN amount ELSE 0 END) income FROM shopping WHERE EXTRACT(YEAR FROM shopping_date)=? AND group_id=? AND EXTRACT(MONTH FROM shopping_date) IN ("+marks+") GROUP BY category_id)s ON s.category_id=c.id WHERE c.group_id=? AND c.disabled_flag=0 ORDER BY c.category_no";
        List<Map<String,Object>> rows=jdbc.queryForList(sql,args.toArray());long max=1;for(Map<String,Object>r:rows){long budget=n(r.get("budget")),expense=n(r.get("expense")),income=n(r.get("income")),available=budget+income,remain=available-expense;r.put("available",available);r.put("remaining",remain);r.put("average",expense/selected.size());r.put("rate",available==0?0:Math.round(expense*1000.0/available)/10.0);max=Math.max(max,Math.max(available,expense));}for(Map<String,Object>r:rows){r.put("budget_width",pct(n(r.get("available")),max));r.put("expense_width",pct(n(r.get("expense")),max));}
        List<Map<String,Object>> cumulative=new ArrayList<>();long cb=0,ce=0,cmax=1;for(int month:selected){long b=scalar("SELECT COALESCE(SUM(amount),0) FROM budgets WHERE group_id=? AND year=? AND month=?",group,y,month),e=scalar("SELECT COALESCE(SUM(amount),0) FROM shopping WHERE group_id=? AND EXTRACT(YEAR FROM shopping_date)=? AND EXTRACT(MONTH FROM shopping_date)=? AND type=1",group,y,month);cb+=b;ce+=e;Map<String,Object>x=new LinkedHashMap<>();x.put("month",month);x.put("budget",b);x.put("expense",e);x.put("cum_budget",cb);x.put("cum_expense",ce);cumulative.add(x);cmax=Math.max(cmax,Math.max(cb,ce));}for(Map<String,Object>x:cumulative){x.put("budget_height",pct(n(x.get("cum_budget")),cmax));x.put("expense_height",pct(n(x.get("cum_expense")),cmax));}
        model.addAttribute("year",y);model.addAttribute("selectedMonths",selected);model.addAttribute("rows",rows);model.addAttribute("cumulative",cumulative);model.addAttribute("annualCards",annualBudgetCards(group,y));return "group/analysis-budget-actual";
    }

    @GetMapping("/year-comparison")
    String yearComparison(@RequestParam(required=false) Integer year,HttpSession session,Model model){
        String group=group(session);int y=year==null?LocalDate.now().getYear():year,prev=y-1;model.addAttribute("year",y);model.addAttribute("previousYear",prev);model.addAttribute("annualCards",annualComparisonCards(group,y,prev));return "group/analysis-year-comparison";
    }

    private List<Map<String,Object>> annualBudgetCards(String group,int year){
        List<Map<String,Object>> categories=jdbc.queryForList("SELECT id,category_name FROM category WHERE group_id=? AND disabled_flag=0 ORDER BY category_no",group);
        List<Map<String,Object>> budgets=jdbc.queryForList("SELECT category_id,month,SUM(amount) value FROM budgets WHERE group_id=? AND year=? GROUP BY category_id,month",group,year);
        List<Map<String,Object>> shopping=jdbc.queryForList("SELECT category_id,EXTRACT(MONTH FROM shopping_date)::int AS shopping_month,SUM(CASE WHEN type=1 THEN amount ELSE 0 END) expense,SUM(CASE WHEN type=2 THEN amount ELSE 0 END) income FROM shopping WHERE group_id=? AND EXTRACT(YEAR FROM shopping_date)=? GROUP BY category_id,EXTRACT(MONTH FROM shopping_date)",group,year);
        Map<String,Long> budgetByMonth=new HashMap<>(),expenseByMonth=new HashMap<>(),incomeByMonth=new HashMap<>();
        for(Map<String,Object> row:budgets)budgetByMonth.put(monthKey(row.get("category_id"),row.get("month")),n(row.get("value")));
        for(Map<String,Object> row:shopping){String key=monthKey(row.get("category_id"),row.get("shopping_month"));expenseByMonth.put(key,n(row.get("expense")));incomeByMonth.put(key,n(row.get("income")));}
        List<Map<String,Object>> cards=new ArrayList<>();
        for(Map<String,Object> category:categories){Map<String,Object> card=new LinkedHashMap<>();card.put("name",category.get("category_name"));card.put("totalCard",false);List<Map<String,Object>> months=new ArrayList<>();for(int month=1;month<=12;month++){String key=monthKey(category.get("id"),month);long available=budgetByMonth.getOrDefault(key,0L)+incomeByMonth.getOrDefault(key,0L),expense=expenseByMonth.getOrDefault(key,0L);Map<String,Object>x=new LinkedHashMap<>();x.put("month",month);x.put("available",available);x.put("expense",expense);x.put("remaining",available-expense);months.add(x);}card.put("months",months);cards.add(card);}
        cards.forEach(this::finishBudgetCard);Map<String,Object> total=new LinkedHashMap<>();total.put("name","合計");total.put("totalCard",true);List<Map<String,Object>> totalMonths=new ArrayList<>();for(int month=1;month<=12;month++){long available=0,expense=0;for(Map<String,Object> card:cards){Map<String,Object>x=((List<Map<String,Object>>)card.get("months")).get(month-1);available+=n(x.get("available"));expense+=n(x.get("expense"));}Map<String,Object>x=new LinkedHashMap<>();x.put("month",month);x.put("available",available);x.put("expense",expense);x.put("remaining",available-expense);totalMonths.add(x);}total.put("months",totalMonths);finishBudgetCard(total);cards.add(total);return cards;
    }

    private void finishBudgetCard(Map<String,Object> card){List<Map<String,Object>> months=(List<Map<String,Object>>)card.get("months");long available=0,expense=0,max=1;for(Map<String,Object>x:months){available+=n(x.get("available"));expense+=n(x.get("expense"));max=Math.max(max,Math.max(n(x.get("available")),n(x.get("expense"))));}for(Map<String,Object>x:months){x.put("available_width",pct(n(x.get("available")),max));x.put("expense_width",pct(n(x.get("expense")),max));}card.put("available",available);card.put("expense",expense);card.put("remaining",available-expense);card.put("rate",available==0?0:Math.round(expense*1000.0/available)/10.0);}

    private List<Map<String,Object>> annualComparisonCards(String group,int year,int previousYear){
        List<Map<String,Object>> categories=jdbc.queryForList("SELECT id,category_name FROM category WHERE group_id=? AND disabled_flag=0 ORDER BY category_no",group);
        List<Map<String,Object>> shopping=jdbc.queryForList("SELECT category_id,EXTRACT(YEAR FROM shopping_date)::int AS target_year,EXTRACT(MONTH FROM shopping_date)::int AS shopping_month,SUM(amount) value FROM shopping WHERE group_id=? AND type=1 AND EXTRACT(YEAR FROM shopping_date) IN (?,?) GROUP BY category_id,EXTRACT(YEAR FROM shopping_date),EXTRACT(MONTH FROM shopping_date)",group,year,previousYear);
        Map<String,Long> currentByMonth=new HashMap<>(),previousByMonth=new HashMap<>();
        for(Map<String,Object> row:shopping){Map<String,Long> destination=n(row.get("target_year"))==year?currentByMonth:previousByMonth;destination.put(monthKey(row.get("category_id"),row.get("shopping_month")),n(row.get("value")));}
        List<Map<String,Object>> cards=new ArrayList<>();
        for(Map<String,Object> category:categories){Map<String,Object> card=new LinkedHashMap<>();card.put("name",category.get("category_name"));card.put("totalCard",false);List<Map<String,Object>> months=new ArrayList<>();for(int month=1;month<=12;month++){String key=monthKey(category.get("id"),month);Map<String,Object>x=new LinkedHashMap<>();x.put("month",month);x.put("current",currentByMonth.getOrDefault(key,0L));x.put("previous",previousByMonth.getOrDefault(key,0L));months.add(x);}card.put("months",months);cards.add(card);}cards.forEach(this::finishComparisonCard);Map<String,Object>total=new LinkedHashMap<>();total.put("name","合計");total.put("totalCard",true);List<Map<String,Object>>totalMonths=new ArrayList<>();for(int month=1;month<=12;month++){long current=0,previous=0;for(Map<String,Object>card:cards){Map<String,Object>x=((List<Map<String,Object>>)card.get("months")).get(month-1);current+=n(x.get("current"));previous+=n(x.get("previous"));}Map<String,Object>x=new LinkedHashMap<>();x.put("month",month);x.put("current",current);x.put("previous",previous);totalMonths.add(x);}total.put("months",totalMonths);finishComparisonCard(total);cards.add(total);return cards;
    }

    private String monthKey(Object categoryId,Object month){return String.valueOf(categoryId)+":"+n(month);}

    private void finishComparisonCard(Map<String,Object> card){List<Map<String,Object>>months=(List<Map<String,Object>>)card.get("months");long current=0,previous=0,max=1;for(Map<String,Object>x:months){current+=n(x.get("current"));previous+=n(x.get("previous"));max=Math.max(max,Math.max(n(x.get("current")),n(x.get("previous"))));}for(Map<String,Object>x:months){x.put("current_width",pct(n(x.get("current")),max));x.put("previous_width",pct(n(x.get("previous")),max));x.put("difference",n(x.get("current"))-n(x.get("previous")));}card.put("current",current);card.put("previous",previous);card.put("difference",current-previous);}

    private List<Integer> normalizeMonths(List<Integer> months){if(months==null||months.isEmpty())return List.of(LocalDate.now().getMonthValue());List<Integer> valid=months.stream().filter(m->m>=1&&m<=12).distinct().sorted().toList();return valid.isEmpty()?List.of(LocalDate.now().getMonthValue()):valid;}
    private long scalar(String sql,Object...args){Long v=jdbc.queryForObject(sql,Long.class,args);return v==null?0:v;}private long n(Object o){return o instanceof Number x?x.longValue():0;}private double pct(long v,long max){return Math.round(v*1000.0/max)/10.0;}private String group(HttpSession s){Object g=s.getAttribute("activeGroupId");if(g==null)throw new IllegalStateException("所属グループを選択してください");return g.toString();}
}
