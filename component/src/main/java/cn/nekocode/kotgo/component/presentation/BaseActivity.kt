package cn.nekocode.kotgo.component.presentation

import android.R
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import cn.nekocode.kotgo.component.util.RxLifecycle
import cn.nekocode.kotgo.component.util.LifecycleContainer

import java.lang.ref.WeakReference

abstract class BaseActivity: AppCompatActivity(), LifecycleContainer {
    companion object {
        private val handlers = arrayListOf<MyHandler>()

        fun addHandler(handler: MyHandler) {
            handlers.add(handler)
        }

        fun deleteHandler(handler: MyHandler) {
            handlers.remove(handler)
        }

        fun removeAll() {
            handlers.clear()
        }

        fun broadcast(message: Message) {
            for (handler in handlers) {
                val msg = Message()
                msg.copyFrom(message)
                handler.sendMessage(msg)
            }
        }

        class MyHandler : Handler {
            private val mOuter: WeakReference<BaseActivity>

            constructor(activity: BaseActivity) {
                mOuter = WeakReference(activity)
            }

            override fun handleMessage(msg: Message) {
                if(mOuter.get() == null) {
                    deleteHandler(this)
                    return
                } else {

                    if (msg.what == -101 && msg.arg1 == -102 && msg.arg2 == -103) {
                        val runnable = (msg.obj as WeakReference<() -> Unit>).get()
                        runnable?.invoke()
                        return
                    }

                    mOuter.get().handler(msg)
                }
            }
        }
    }

    override val lifecycle = RxLifecycle()
    protected val handler: MyHandler by lazy {
        MyHandler(this)
    }

    fun sendMsg(message: Message) {
        val msg = Message()
        msg.copyFrom(message)
        handler.sendMessage(msg)
    }

    fun sendMsgDelayed(message: Message, delayMillis: Int) {
        val msg = Message()
        msg.copyFrom(message)
        handler.sendMessageDelayed(msg, delayMillis.toLong())
    }

    fun runDelayed(runnable: ()->Unit, delayMillis: Int) {
        val msg = Message()
        msg.what = -101
        msg.arg1 = -102
        msg.arg2 = -103
        msg.obj = WeakReference<() -> Unit>(runnable)
        handler.sendMessageDelayed(msg, delayMillis.toLong())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addHandler(handler)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> this.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        deleteHandler(handler)
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.onDestory()
    }

    open fun handler(msg: Message) {

    }
}
