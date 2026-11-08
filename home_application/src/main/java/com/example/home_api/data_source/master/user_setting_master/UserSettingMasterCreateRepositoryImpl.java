package com.example.home_api.data_source.master.user_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.repositories.master.user_setting_master.UserSettingMasterCreateRepository;
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
public class UserSettingMasterCreateRepositoryImpl implements UserSettingMasterCreateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー設定マスタ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserSettingMasterCreateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String create(UserSettingMasterKey key, UserSettingMasterInitValue value, UserSettingMasterRemarks remarks) {

        SqlAndParams sap = createSql(key, value, remarks);

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                log.error("{} の作成に失敗しました：影響を受けた行なし",itemName);
                throw new RuntimeException();
            }
            return key.getValue();

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

        // セットする値のリストを作成
        List<Object> valueItems = new ArrayList<>();
        valueItems.add(key.getValue());
        if (value == null || value.getValue() == null) {
            valueItems.add("NULL");
        } else {
            valueItems.add(value.getValue());
        }
        if (remarks == null || remarks.getValue() == null) {
            valueItems.add("NULL");
        } else {
            valueItems.add(remarks.getValue());
        }
        valueItems.add("CURRENT_TIMESTAMP");
        valueItems.add("CURRENT_TIMESTAMP");

        // SQL生成
        return SqlAndParams.toSqlAndParamsForInsert(
                "user_setting_master",
                List.of("key", "init_value", "remarks", "create_date", "update_date"),
                valueItems
        );
    }
}
