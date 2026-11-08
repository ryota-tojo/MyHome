package com.example.home_api.data_source.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SetItem;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingUpdateRepository;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
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
public class MasterSettingUpdateRepositoryImpl implements MasterSettingUpdateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.マスタ設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public MasterSettingUpdateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer update(MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks) {

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
                log.error("{} の更新に失敗しました：影響を受けた行なし",itemName);
                throw new RuntimeException();
            }
            return affected;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlAndParams createSql(MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks) {

        // 不正値防御（念のため）
        if (key == null || key.getValue() == null || key.getValue().isEmpty()) {
            log.error("createSql {} は必須項目です","key");
            throw new IllegalArgumentException("");
        }

        // SET句の条件リストを作成(オブジェクトNULL：除外、値NULL：許容)
        List<SetItem> setItems = new ArrayList<>();
        if (value != null) {
            setItems.add(SetItem.from("value", (value.getValue() == null) ? "NULL" : value.getValue()));
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
                "master_setting",
                setItems,
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
