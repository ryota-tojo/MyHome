package com.example.home_api.data_source.master.user_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SetItem;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.master.user_setting_master.UserSettingMasterUpdateRepository;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterRemarks;
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
public class UserSettingMasterUpdateRepositoryImpl implements UserSettingMasterUpdateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー設定マスタ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserSettingMasterUpdateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer update(UserSettingMasterKey key, UserSettingMasterInitValue value, UserSettingMasterRemarks remarks) {

        SqlAndParams sap = createSql(key, value, remarks);

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlAndParams createSql(UserSettingMasterKey key, UserSettingMasterInitValue value, UserSettingMasterRemarks remarks) {

        // 不正値防御（念のため）
        if (key == null || key.getValue() == null || key.getValue().isEmpty()) {
            log.error("createSql {} は必須項目です","key");
            throw new IllegalArgumentException("");
        }

        // SET句の条件リストを作成(オブジェクトNULL：除外、値NULL：許容)
        List<SetItem> setItems = new ArrayList<>();
        if (value != null) {
            setItems.add(SetItem.from("init_value", (value.getValue() == null) ? "NULL" : value.getValue()));
        }
        if (remarks != null) {
            setItems.add(SetItem.from("remarks", (remarks.getValue() == null) ? "NULL" : remarks.getValue()));
        }
        setItems.add(SetItem.from("update_date", "CURRENT_TIMESTAMP"));

        // WHERE句のAND条件リストを作成
        List<WhereItem> whereItemsForAnd = new ArrayList<>();
        whereItemsForAnd.add(WhereItem.from("key", key.getValue()));

        // WHERE句のOR条件リストを作成
        List<WhereItem> whereItemsForOr = null;

        // SQL生成
        return SqlAndParams.toSqlAndParamsForUpdate(
                "user_setting_master",
                setItems,
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
