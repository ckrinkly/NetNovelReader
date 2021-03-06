package com.netnovelreader.repo.proxy

import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class Proxy(val obj: Any) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        Log.d("db------start", method?.name)
        val result = method?.invoke(obj,args)
        Log.d("db------end", method?.name)
        return result
    }

}