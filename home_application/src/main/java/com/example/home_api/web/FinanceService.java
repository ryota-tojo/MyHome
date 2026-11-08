package com.example.home_api.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Service
public class FinanceService {
    private final JdbcTemplate jdbc;
    public FinanceService(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public String groupId(String userId){
        List<String> ids=jdbc.query("SELECT group_id FROM group_member WHERE user_id=? AND approve_flag=1 ORDER BY leader_flag DESC,create_date LIMIT 1",(rs,n)->rs.getString(1),userId);
        if(ids.isEmpty()) throw new IllegalStateException("承認済みの所属グループがありません"); return ids.get(0);
    }
    public List<Map<String,Object>> categories(String group){return jdbc.queryForList("SELECT id,category_name FROM category WHERE group_id=? AND disabled_flag=0 ORDER BY category_no",group);}
    public List<Map<String,Object>> shopping(String group,int year,int month){return jdbc.queryForList("""
        SELECT s.id,s.shopping_date,s.type,s.amount,s.payment,s.settlement,s.remarks,c.category_name,u.user_name
        FROM shopping s LEFT JOIN category c ON c.id=s.category_id LEFT JOIN "user" u ON u.user_id=s.shopping_user_id
        WHERE s.group_id=? AND EXTRACT(YEAR FROM s.shopping_date)=? AND EXTRACT(MONTH FROM s.shopping_date)=?
        ORDER BY s.shopping_date DESC,s.id DESC""",group,year,month);}
    public Map<String,Object> summary(String group,int year,int month){return jdbc.queryForMap("""
        SELECT COALESCE(SUM(CASE WHEN type=2 THEN amount ELSE 0 END),0) income,
        COALESCE(SUM(CASE WHEN type=1 THEN amount ELSE 0 END),0) expense,
        COALESCE(SUM(CASE WHEN type=2 THEN amount ELSE -amount END),0) balance
        FROM shopping WHERE group_id=? AND EXTRACT(YEAR FROM shopping_date)=? AND EXTRACT(MONTH FROM shopping_date)=?""",group,year,month);}
    public List<Map<String,Object>> categorySummary(String group,int year,int month){return jdbc.queryForList("""
        SELECT c.category_name,COALESCE(SUM(s.amount),0) amount FROM category c LEFT JOIN shopping s
        ON s.category_id=c.id AND s.type=1 AND EXTRACT(YEAR FROM s.shopping_date)=? AND EXTRACT(MONTH FROM s.shopping_date)=?
        WHERE c.group_id=? GROUP BY c.id,c.category_name,c.category_no ORDER BY c.category_no""",year,month,group);}
    @Transactional public void addShopping(String group,String user,ShoppingForm f){jdbc.update("""
        INSERT INTO shopping(group_id,shopping_date,shopping_user_id,category_id,type,payment,settlement,amount,remarks,create_user_id,fixed_flag,create_date,update_date)
        VALUES (?,?,?,?,?,?,?,?,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)""",group,Timestamp.valueOf(f.date().atStartOfDay()),user,f.categoryId(),f.type(),f.payment(),f.settlement(),f.amount(),f.remarks(),user);}
    public void deleteShopping(String group,long id){jdbc.update("DELETE FROM shopping s WHERE s.id=? AND s.group_id=? AND COALESCE(s.settlement,1)<>2 AND s.fixed_flag=0 AND NOT EXISTS(SELECT 1 FROM fixed f WHERE f.group_id=s.group_id AND f.year=EXTRACT(YEAR FROM s.shopping_date)::int AND f.month=EXTRACT(MONTH FROM s.shopping_date)::int)",id,group);}
    public List<Map<String,Object>> budgets(String group,int year,int month){return jdbc.queryForList("""
        SELECT c.id,c.category_name,COALESCE(b.amount,0) amount FROM category c LEFT JOIN budgets b
        ON b.category_id=c.id AND b.group_id=c.group_id AND b.year=? AND b.month=?
        WHERE c.group_id=? AND c.disabled_flag=0 ORDER BY c.category_no""",year,month,group);}
    @Transactional public void saveBudget(String group,int year,int month,String category,int amount){jdbc.update("""
        INSERT INTO budgets(group_id,year,month,category_id,amount,fixed_flag,create_date,update_date)
        VALUES (?,?,?,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT(group_id,year,month,category_id)
        DO UPDATE SET amount=EXCLUDED.amount,update_date=CURRENT_TIMESTAMP""",group,year,month,category,amount);}
    public long budgetTotal(String group,int year,int month){Long value=jdbc.queryForObject("SELECT COALESCE(SUM(amount),0) FROM budgets WHERE group_id=? AND year=? AND month=?",Long.class,group,year,month);return value==null?0L:value;}
    @Transactional public void saveBudgets(String group,int year,int month,List<String> categoryIds,List<Integer> amounts,boolean allMonths){
        if(categoryIds.size()!=amounts.size())throw new IllegalArgumentException("予算の入力内容が不正です");
        var valid=new java.util.HashSet<>(jdbc.queryForList("SELECT id FROM category WHERE group_id=? AND disabled_flag=0",String.class,group));
        if(allMonths)jdbc.update("DELETE FROM budgets b WHERE b.group_id=? AND b.year=? AND NOT EXISTS(SELECT 1 FROM category c WHERE c.id=b.category_id AND c.group_id=b.group_id AND c.disabled_flag=0)",group,year);
        else jdbc.update("DELETE FROM budgets b WHERE b.group_id=? AND b.year=? AND b.month=? AND NOT EXISTS(SELECT 1 FROM category c WHERE c.id=b.category_id AND c.group_id=b.group_id AND c.disabled_flag=0)",group,year,month);
        for(int i=0;i<categoryIds.size();i++){String category=categoryIds.get(i);if(!valid.contains(category))continue;int amount=Math.max(0,amounts.get(i)==null?0:amounts.get(i));if(allMonths){for(int m=1;m<=12;m++)saveBudget(group,year,m,category,amount);}else saveBudget(group,year,month,category,amount);}
    }
    @Transactional public void copyPreviousBudgets(String group,YearMonth target){
        YearMonth source=target.minusMonths(1);var categories=jdbc.queryForList("SELECT id FROM category WHERE group_id=? AND disabled_flag=0 ORDER BY category_no",String.class,group);
        for(String category:categories){Integer amount=jdbc.query("SELECT amount FROM budgets WHERE group_id=? AND year=? AND month=? AND category_id=?",rs->rs.next()?rs.getInt(1):0,group,source.getYear(),source.getMonthValue(),category);saveBudget(group,target.getYear(),target.getMonthValue(),category,amount==null?0:amount);}
    }
    public List<Map<String,Object>> yearlyBudgets(String group,int year){return jdbc.queryForList("""
        SELECT c.id,c.category_name,
        COALESCE(MAX(CASE WHEN b.month=1 THEN b.amount END),0) m1,COALESCE(MAX(CASE WHEN b.month=2 THEN b.amount END),0) m2,
        COALESCE(MAX(CASE WHEN b.month=3 THEN b.amount END),0) m3,COALESCE(MAX(CASE WHEN b.month=4 THEN b.amount END),0) m4,
        COALESCE(MAX(CASE WHEN b.month=5 THEN b.amount END),0) m5,COALESCE(MAX(CASE WHEN b.month=6 THEN b.amount END),0) m6,
        COALESCE(MAX(CASE WHEN b.month=7 THEN b.amount END),0) m7,COALESCE(MAX(CASE WHEN b.month=8 THEN b.amount END),0) m8,
        COALESCE(MAX(CASE WHEN b.month=9 THEN b.amount END),0) m9,COALESCE(MAX(CASE WHEN b.month=10 THEN b.amount END),0) m10,
        COALESCE(MAX(CASE WHEN b.month=11 THEN b.amount END),0) m11,COALESCE(MAX(CASE WHEN b.month=12 THEN b.amount END),0) m12,
        COALESCE(SUM(b.amount),0) total FROM category c LEFT JOIN budgets b ON b.category_id=c.id AND b.group_id=c.group_id AND b.year=?
        WHERE c.group_id=? AND c.disabled_flag=0 GROUP BY c.id,c.category_name,c.category_no ORDER BY c.category_no""",year,group);}
    public List<Long> yearlyBudgetMonthTotals(String group,int year){var result=new ArrayList<Long>();for(int month=1;month<=12;month++)result.add(budgetTotal(group,year,month));return result;}
    public boolean monthFixed(String group,int year,int month){Integer n=jdbc.queryForObject("SELECT count(*) FROM fixed WHERE group_id=? AND year=? AND month=?",Integer.class,group,year,month);return n!=null&&n>0;}
    public boolean yearHasFixedMonth(String group,int year){Integer n=jdbc.queryForObject("SELECT count(*) FROM fixed WHERE group_id=? AND year=?",Integer.class,group,year);return n!=null&&n>0;}
    public List<Map<String,Object>> communications(String group,String keyword,Integer returnFlag){
        StringBuilder sql=new StringBuilder("SELECT * FROM communication WHERE group_id=?");List<Object> args=new ArrayList<>();args.add(group);
        if(keyword!=null&&!keyword.isBlank()){sql.append(" AND (CAST(id AS text) ILIKE ? OR COALESCE(receive_from,'') ILIKE ? OR COALESCE(receive_to,'') ILIKE ?)");String like="%"+keyword.trim()+"%";args.add(like);args.add(like);args.add(like);}
        if(returnFlag!=null){sql.append(" AND COALESCE(return_flag,0)=?");args.add(returnFlag);}
        sql.append(" ORDER BY receive_date DESC,id DESC");return jdbc.queryForList(sql.toString(),args.toArray());
    }
    public Map<String,Object> communication(String group,int id){List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM communication WHERE group_id=? AND id=?",group,id);if(rows.isEmpty())throw new IllegalArgumentException("お付き合い帳データが見つかりません");return rows.get(0);}
    @Transactional public void addCommunication(String group,CommunicationForm f){jdbc.update("""
        INSERT INTO communication(group_id,receive_date,receive_from,receive_to,receive_item,receive_amount,receive_remarks,return_flag,create_date,update_date)
        VALUES (?,?,?,?,?,?,?,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)""",group,Timestamp.valueOf(f.receiveDate().atStartOfDay()),f.receiveFrom(),f.receiveTo(),f.receiveItem(),String.valueOf(f.receiveAmount()),f.receiveRemarks());}
    @Transactional public void updateCommunication(String group,CommunicationEditForm f){jdbc.update("""
        UPDATE communication SET receive_date=?,receive_from=?,receive_to=?,receive_item=?,receive_amount=?,receive_remarks=?,
        return_date=?,return_item=?,return_amount=?,return_remarks=?,return_flag=?,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND id=?""",
        Timestamp.valueOf(f.receiveDate().atStartOfDay()),f.receiveFrom(),f.receiveTo(),f.receiveItem(),String.valueOf(f.receiveAmount()),f.receiveRemarks(),
        f.returnDate()==null?null:Timestamp.valueOf(f.returnDate().atStartOfDay()),f.returnItem(),f.returnAmount()==null?null:String.valueOf(f.returnAmount()),f.returnRemarks(),f.returnFlag(),group,f.id());}
    public record ShoppingForm(LocalDate date,String categoryId,int type,int payment,int settlement,int amount,String remarks){}
    public record CommunicationForm(LocalDate receiveDate,String receiveFrom,String receiveTo,String receiveItem,int receiveAmount,String receiveRemarks){}
    public record CommunicationEditForm(int id,LocalDate receiveDate,String receiveFrom,String receiveTo,String receiveItem,int receiveAmount,String receiveRemarks,LocalDate returnDate,String returnItem,Integer returnAmount,String returnRemarks,int returnFlag){}
}
