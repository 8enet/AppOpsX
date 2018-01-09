package android.content.pm;

import android.os.UserHandle;

public class UserInfo {
  public int id;
  public int serialNumber;
  public String name;
  public String iconPath;
  public int flags;


  public boolean isPrimary() {
    return false;
  }

  public boolean isAdmin() {
    return false;
  }

  public boolean isManagedProfile() {
    return false;
  }


  public UserHandle getUserHandle() {
    return null;
  }
}
