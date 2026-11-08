package com.example.home_api.data_source.master.choice_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.entities.master.ChoiceMasterEntity;
import com.example.home_api.domain.repositories.master.choice_master.ChoiceSettingReferRepository;
import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterId;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterNo;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterSettingKey;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterSettingValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ChoiceSettingReferRepositoryImpl implements ChoiceSettingReferRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.選択肢マスタ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ChoiceSettingReferRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ChoiceMasterEntity> find(ChoiceMasterSettingKey key) {

        SqlAndParams sap = createSql(key);
        List<ChoiceMasterEntity> result = new ArrayList<>();

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(
                            ChoiceMasterEntity.of(
                                    ChoiceMasterId.of(rs.getInt("id")),
                                    ChoiceMasterSettingKey.of(rs.getString("key")),
                                    ChoiceMasterNo.of(rs.getInt("no")),
                                    ChoiceMasterSettingValue.of(rs.getString("value")),
                                    CreateDatetime.of(
                                            rs.getTimestamp("create_date").toLocalDateTime()
                                    ),
                                    UpdateDatetime.of(
                                            rs.getTimestamp("update_date").toLocalDateTime()
                                    )
                            )
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private SqlAndParams createSql(ChoiceMasterSettingKey key) {

        // WHERE句の条件リストを作成
        List<WhereItem> whereItems = new ArrayList<>();
        if (key != null && key.getValue() != null) {
            whereItems.add(WhereItem.from("key", key.getValue()));
        }

        // SQL生成
        return SqlAndParams.toSqlAndParamsForSelect(
                "choice_master",
                List.of("*"),
                whereItems,
                null,
                null,
                null
        );
    }
}
