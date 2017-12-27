package com.example.yujieshui.myapplication

/**
 * Created by yujieshui on 2017/12/26.
 */

interface Action
data class DelayAction(val action: String, val time: Int) : Action
data class TapAction(val action: String, val x: Int, val y: Int) : Action
