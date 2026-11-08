package com.example.home_api.data_source.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingDeleteRepository;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
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
public class MasterSettingDeleteRepositoryImpl implements MasterSettingDeleteRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.マスタ設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public MasterSettingDeleteRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer delete(MasterSettingKey key) {

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

    private SqlAndParams createSql(MasterSettingKey key) {

        // WHERE句のAND条件リストを作成
        List<WhereItem> whereItemsForAnd = new ArrayList<>();
        if (key != null && key.getValue() != null) {
            whereItemsForAnd.add(WhereItem.from("key", key.getValue()));
        }

        // WHERE句のOR条件リストを作成
        List<WhereItem> whereItemsForOr = null;

        // SQL生成
        return SqlAndParams.toSqlAndParamsForDelete(
                "master_setting",
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
