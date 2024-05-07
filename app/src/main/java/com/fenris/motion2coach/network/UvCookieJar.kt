package com.fenris.motion2coach.network

import android.content.Context
import com.fenris.motion2coach.util.HelperKeys
import com.fenris.motion2coach.util.SessionManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl


class UvCookieJar(context: Context) : CookieJar {

    private val cookies = mutableListOf<Cookie>()
    private val context1 = context

    override fun saveFromResponse(url: HttpUrl, cookieList: List<Cookie>) {
        cookies.clear()
        cookies.addAll(cookieList)
        if (cookieList.isNotEmpty()) {
            SessionManager.saveArrayList(cookieList, HelperKeys.COOKIE_LIST,context1)
        }

    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {


        if (cookies.isEmpty()&&!SessionManager.getStringPref(HelperKeys.USER_ID,context1).equals("")) {
            cookies.addAll(SessionManager.getArrayList(HelperKeys.COOKIE_LIST,context1))
        }

        return cookies
    }



}