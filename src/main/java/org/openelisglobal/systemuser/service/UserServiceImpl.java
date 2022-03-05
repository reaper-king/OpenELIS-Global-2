package org.openelisglobal.systemuser.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openelisglobal.login.service.LoginUserService;
import org.openelisglobal.login.valueholder.LoginUser;
import org.openelisglobal.systemuser.valueholder.SystemUser;
import org.openelisglobal.userrole.service.UserRoleService;
import org.openelisglobal.userrole.valueholder.UserLabUnitRoles;
import org.openelisglobal.userrole.valueholder.LabUnitRoleMap;
import org.openelisglobal.userrole.valueholder.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private LoginUserService loginService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private SystemUserService systemUserService;

    @Override
    @Transactional
    public void updateLoginUser(LoginUser loginUser, boolean loginUserNew, SystemUser systemUser, boolean systemUserNew,
            List<String> selectedRoles, String loggedOnUserId) {
        if (loginUserNew) {
            loginService.insert(loginUser);
        } else {
            loginService.update(loginUser);
        }
        
        if (systemUserNew) {
            systemUserService.insert(systemUser);
        } else {
            systemUserService.update(systemUser);
        }
        
        updateUserRoles(selectedRoles, systemUser, loggedOnUserId, false);
    }

    @Override
    @Transactional
    public void saveUserLabUnitRoles(SystemUser systemUser, Map<String, Set<String>> selectedLabUnitRolesMap,
            String loggedOnUserId) {
        UserLabUnitRoles userLabUnitRoles = userRoleService.getUserLabUnitRoles(systemUser.getId());
        Set<LabUnitRoleMap> labUnitRoleMaps;
        if (userLabUnitRoles == null) {
            userLabUnitRoles = new UserLabUnitRoles();
            userLabUnitRoles.setId(Integer.valueOf(systemUser.getId()));
            labUnitRoleMaps = new HashSet<>();
        } else {
            labUnitRoleMaps = userLabUnitRoles.getLabUnitRoleMap();
            for (LabUnitRoleMap roleMap : labUnitRoleMaps) {
                userRoleService.deleteLabUnitRoleMap(roleMap);
            }
            labUnitRoleMaps.clear();
        }
        Set<String> labUnitRoles = new HashSet<>();
        for (String labUnit : selectedLabUnitRolesMap.keySet()) {
            if (StringUtils.isNotEmpty(labUnit)) {
                LabUnitRoleMap labUnitRoleMap = new LabUnitRoleMap();
                labUnitRoleMap.setLabUnit(labUnit);
                labUnitRoleMap.setRoles(selectedLabUnitRolesMap.get(labUnit));
                labUnitRoleMaps.add(labUnitRoleMap);
                for (String role : selectedLabUnitRolesMap.get(labUnit)) {
                    labUnitRoles.add(role);
                }
            }
            
        }
        userLabUnitRoles.setLabUnitRoleMap(labUnitRoleMaps);
        userRoleService.saveOrUpdateUserLabUnitRoles(userLabUnitRoles);
        updateUserRoles(labUnitRoles.stream().collect(Collectors.toList()), systemUser, loggedOnUserId, true);
    }
    
    @Override
    @Transactional
    public UserLabUnitRoles getUserLabUnitRoles(String systemUserId) {
        return userRoleService.getUserLabUnitRoles(systemUserId);
    }

    @Override
    @Transactional
    public List<UserLabUnitRoles> getAllUserLabUnitRoles() {
        return userRoleService.getAllUserLabUnitRoles();
    }

    private void updateUserRoles(List<String> selectedRoles, SystemUser systemUser, String loggedOnUserId,
            Boolean isLabRole) {
        List<String> currentUserRoles = userRoleService.getRoleIdsForUser(systemUser.getId());
        List<UserRole> deletedUserRoles = new ArrayList<>();
        if (isLabRole) {
            for (String role : currentUserRoles) {
                selectedRoles.add(role);
            }
        }
        
        for (int i = 0; i < selectedRoles.size(); i++) {
            if (!currentUserRoles.contains(selectedRoles.get(i))) {
                UserRole userRole = new UserRole();
                userRole.setSystemUserId(systemUser.getId());
                userRole.setRoleId(selectedRoles.get(i));
                userRole.setSysUserId(loggedOnUserId);
                userRoleService.insert(userRole);
            } else {
                currentUserRoles.remove(selectedRoles.get(i));
            }
        }
        
        for (String roleId : currentUserRoles) {
            UserRole userRole = new UserRole();
            userRole.setSystemUserId(systemUser.getId());
            userRole.setRoleId(roleId);
            userRole.setSysUserId(loggedOnUserId);
            deletedUserRoles.add(userRole);
        }
        
        if (deletedUserRoles.size() > 0) {
            userRoleService.deleteAll(deletedUserRoles);
        }
        
    }
}
