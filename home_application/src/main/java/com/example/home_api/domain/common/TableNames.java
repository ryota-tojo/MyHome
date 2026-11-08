package com.example.home_api.domain.common;

public enum TableNames {
    マスタ設定("master_setting"),
    選択肢マスタ("choice_master"),
    所属グループ設定マスタ("group_setting_master"),
    ユーザー設定マスタ("user_setting_master"),
    お知らせ("notice"),
    所属グループ("group"),
    ユーザー("user"),
    所属グループメンバー("group_member"),
    所属グループ設定("group_setting"),
    ユーザー設定("user_setting"),
    カテゴリー("category"),
    購入ラベル("shopping_label"),
    購入データ("shopping"),
    登録リスト("shopping_entry_list"),
    入力リスト("shopping_input_list"),
    検索リスト("shopping_search_list"),
    予算("budgets"),
    確定データ("fixed"),
    コメント("comment"),
    お付き合い帳("communication");

    private String itemName;

    TableNames(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }
    }
