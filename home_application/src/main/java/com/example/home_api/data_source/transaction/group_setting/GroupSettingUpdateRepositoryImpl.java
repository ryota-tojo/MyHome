package com.example.home_api.data_source.transaction.group_setting;

import com.example.home_api.data_source.transaction.group.GroupReferRepositoryImpl;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SetItem;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingUpdateRepository;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GroupSettingUpdateRepositoryImpl implements GroupSettingUpdateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupSettingUpdateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer update(GroupId groupId, GroupSettingKey key, GroupSettingValue value) {

        SqlAndParams sap = createSql(groupId, key, value);

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                log.error("{} の更新に失敗しました：影響を受けた行なし",itemName);
                throw new RuntimeException();
            }

            return affected;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlAndParams createSql(GroupId groupId, GroupSettingKey key, GroupSettingValue value) {

        // 不正値防御（念のため）
        if (groupId == null || groupId.getValue() == null) {
            log.error("createSql {} は必須項目です","groupId");
            throw new IllegalArgumentException("");
        }
        if (key == null || key.getValue() == null) {
            log.error("createSql {} は必須項目です","key");
            throw new IllegalArgumentException("");
        }

        // SET句の条件リストを作成(オブジェクトNULL：除外、値NULL：許容)
        List<SetItem> setItems = new ArrayList<>();
        if (value != null) {
            setItems.add(SetItem.from("value", (value.getValue() == null) ? "NULL" : value.getValue()));
        }
        setItems.add(SetItem.from("update_date", "CURRENT_TIMESTAMP"));

        // WHERE句の条件リストを作成
        List<WhereItem> whereItemsForAnd = new ArrayList<>();
        whereItemsForAnd.add(WhereItem.from("group_id", groupId.getValue()));
        whereItemsForAnd.add(WhereItem.from("key", key.getValue()));

        // WHERE句のOR条件リストを作成
        List<WhereItem> whereItemsForOr = null;

        // SQL生成
        return SqlAndParams.toSqlAndParamsForUpdate(
                "group_setting",
                setItems,
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
