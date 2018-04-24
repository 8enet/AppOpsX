package com.zzzmode.appopsx.ui.core

import android.content.pm.UserInfo
import java.util.ArrayList

class Users {


    var users: List<UserInfo>? = null
        private set

    var currentUser: UserInfo? = null
        private set

    private val isLoaded: Boolean
        get() = users != null

    val hasMultiUser:Boolean
    get() = isLoaded && !users!!.isEmpty()

    val currentUid: Int
        get() = if (currentUser != null) currentUser!!.id else 0

    fun updateUsers(users: List<UserInfo>) {
        this.users = ArrayList(users)
    }


    fun setCurrentLoadUser(user: UserInfo) {
        this.currentUser = user
    }

    companion object {

        private  val sUsers: Users by lazy {
            Users()
        }

        val instance: Users = sUsers
    }


}
