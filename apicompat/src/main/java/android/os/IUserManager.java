package android.os;

import android.content.pm.UserInfo;
import java.util.List;

public interface IUserManager {
  UserInfo getPrimaryUser();

  List<UserInfo> getUsers(boolean excludeDying);

  int getManagedProfileBadge(int userId);

  abstract class Stub{
    public static IUserManager asInterface(android.os.IBinder obj){
      return null;
    }
  }

}
