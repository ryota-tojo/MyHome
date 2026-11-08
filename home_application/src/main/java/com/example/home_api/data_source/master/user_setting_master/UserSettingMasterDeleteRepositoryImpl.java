package com.example.home_api.data_source.master.user_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.master.user_setting_master.UserSettingMasterDeleteRepository;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
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
public class UserSettingMasterDeleteRepositoryImpl implements UserSettingMasterDeleteRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー設定マスタ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserSettingMasterDeleteRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer delete(UserSettingMasterKey key) {

        SqlAndParams sap = createSql(key);

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

    private SqlAndParams createSql(UserSettingMasterKey key) {

        // WHERE句のAND条件リストを作成
        List<WhereItem> whereItemsForAnd = new ArrayList<>();
        if (key != null && key.getValue() != null) {
            whereItemsForAnd.add(WhereItem.from("key", key.getValue()));
        }

        // WHERE句のOR条件リストを作成
        List<WhereItem> whereItemsForOr = null;

        // SQL生成
        return SqlAndParams.toSqlAndParamsForDelete(
                "user_setting_master",
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
