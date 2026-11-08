package com.example.home_api.api.transaction.user.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.transaction.user.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class UserUpdateApiRequest {
    UserId userId;
    UserPassword password;
    UserName name;
    UserPermissionFlag permissionflag;
    UserApproveFlag approveflag;
    UserDeleteFlag deleteflag;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForUserId = ParamUtils.isValidCheckForRequired(request, "user_id");
        if (!paramCheckResultForUserId.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForUserId.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForPassword = ParamUtils.isValidCheckForOptional(request, "user_password");
        if (!paramCheckResultForPassword.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForPassword.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForUserName = ParamUtils.isValidCheckForOptional(request, "user_name");
        if (!paramCheckResultForUserName.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForUserName.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForPermission = ParamUtils.isValidCheckForOptional(request, "permission_flag");
        if (!paramCheckResultForPermission.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForPermission.getMessages()
            );
        }
        ParamCheckResult validateForPermission = UserPermissionFlag.validate(request);
        if (!validateForPermission.isResult()) {
            return ParamCheckResult.of(
                    false,
                    validateForPermission.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForApproval = ParamUtils.isValidCheckForOptional(request, "approve_flag");
        if (!paramCheckResultForApproval.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForApproval.getMessages()
            );
        }
        ParamCheckResult validateForApprove = UserApproveFlag.validate(request);
        if (!validateForApprove.isResult()) {
            return ParamCheckResult.of(
                    false,
                    validateForApprove.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForDelete = ParamUtils.isValidCheckForOptional(request, "delete_flag");
        if (!paramCheckResultForDelete.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForDelete.getMessages()
            );
        }
        ParamCheckResult validateForDelete = UserDeleteFlag.validate(request);
        if (!validateForDelete.isResult()) {
            return ParamCheckResult.of(
                    false,
                    validateForDelete.getMessages()
            );
        }
        return ParamCheckResult.of(
                true,
                null
        );
    }

    public static UserUpdateApiRequest from(JsonNode request) {

        return new UserUpdateApiRequest(
                getParamForUserId(request),
                getParamForForPassword(request),
                getParamForForName(request),
                getParamForForPermissionFlag(request),
                getParamForForApproveFlag(request),
                getParamForForDeleteFlag(request)
        );
    }

    private static UserId getParamForUserId(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "user_id")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserId.of(request.get("user_id").asText());
        }
    }

    private static UserPassword getParamForForPassword(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "user_password")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserPassword.of(request.get("user_password").asText());
        }
    }

    private static UserName getParamForForName(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "user_name")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserName.of(request.get("user_name").asText());
        }
    }

    private static UserPermissionFlag getParamForForPermissionFlag(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "permission_flag")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserPermissionFlag.of(request.get("permission_flag").asInt());
        }
    }

    private static UserApproveFlag getParamForForApproveFlag(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "approve_flag")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserApproveFlag.of(request.get("approve_flag").asInt());
        }
    }

    private static UserDeleteFlag getParamForForDeleteFlag(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "delete_flag")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserDeleteFlag.of(request.get("delete_flag").asInt());
        }
    }
}
