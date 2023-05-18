package com.example.iot

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class MyPreference {
    private var accountUtil: MyPreference? = null
    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    fun getInstance(context: Context): MyPreference? {
        if (accountUtil == null) accountUtil = MyPreference()
        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(context)
            accountUtil?.pref = pref
        }
        return accountUtil
    }
    fun saveTimeGarden(
        key:String,value: Long
    )
    {
         editor=pref?.edit()
        editor?.putLong(key,value)
        editor?.apply()
    }
    fun saveTempGarden(
        key:String,value: String
    )
    {
        editor=pref?.edit()
        editor?.putString(key,value)
        editor?.apply()
    }
    fun saveHumiGarden(
        key:String,value: String
    )
    {
        editor=pref?.edit()
        editor?.putString(key,value)
        editor?.apply()
    }
    fun getTimeGarden(key:String):Long
    {
       return pref?.getLong(key,0)?:0
    }

    fun setTextTime(key:String, value :String)
    {
        editor=pref?.edit()
        editor?.putString(key,value)
        editor?.apply()
    }
    fun getTextTime(key:String):String
    {
        return pref?.getString(key,"").toString()

    }
    fun getTextHumi(key:String):String
    {
        return pref?.getString(key,"").toString()

    }
    fun getTextTemp(key:String):String
    {
        return pref?.getString(key,"").toString()

    }

    fun saveHumpAndTemp1(data:String)
    {
        editor=pref?.edit()
        editor?.putString("1",data)
        editor?.apply()
    }
    fun saveHumpAndTemp2(data:String)
    {
        editor=pref?.edit()
        editor?.putString("2",data)
        editor?.apply()
    }
    fun getHumpAndTemp1(): String {
        return pref?.getString("1","").toString()
    }
    fun getHumpAndTemp2(): String {
        return pref?.getString("2","").toString()
    }
}