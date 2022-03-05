package org.openelisglobal.systemuser.service;

import java.util.List;
import java.util.Set;
import java.util.Map;

import org.openelisglobal.login.valueholder.LoginUser;
import org.openelisglobal.systemuser.valueholder.SystemUser;
import org.openelisglobal.userrole.valueholder.UserLabUnitRoles;

public interface UserService {

    void updateLoginUser(LoginUser loginUser, boolean loginUserNew, SystemUser systemUser, boolean systemUserNew,
            List<String> selectedRoles, String loggedOnUserId);

    void saveUserLabUnitRoles(SystemUser systemUser, Map<String , Set<String>> selectedLabUnitRolesMap ,String loggedOnUserId);   
    
    UserLabUnitRoles getUserLabUnitRoles(String systemUserId );

    List<UserLabUnitRoles> getAllUserLabUnitRoles();
}
