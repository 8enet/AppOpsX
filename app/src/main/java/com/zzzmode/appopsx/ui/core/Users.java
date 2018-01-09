package com.zzzmode.appopsx.ui.core;

import android.content.pm.UserInfo;
import java.util.ArrayList;
import java.util.List;

public class Users {

  private static Users sUsers;

  public static Users getInstance(){
    if(sUsers == null){
      synchronized (Users.class){
        if(sUsers == null){
          sUsers = new Users();
        }
      }
    }
    return sUsers;
  }


  private List<UserInfo> userInfos;

  private UserInfo currentUser;

  public List<UserInfo> getUsers(){
    return userInfos;
  }

  public boolean isLoaded(){
    return userInfos != null;
  }

  public void updateUsers(List<UserInfo> users){
    userInfos = new ArrayList<>(users);
  }


  public void setCurrentLoadUser(UserInfo user){
    this.currentUser = user;
  }


  public int getCurrentUid(){
    return currentUser != null?currentUser.id:0;
  }

  public UserInfo getCurrentUser(){
    return currentUser;
  }



}
