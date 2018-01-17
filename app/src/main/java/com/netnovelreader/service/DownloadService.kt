package com.netnovelreader.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.netnovelreader.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by yangbo on 18-1-15.
 */
class DownloadService : Service() {
    var mNotificationManager: NotificationManager? = null
    var builder: NotificationCompat.Builder?  = null
    var queue: LinkedBlockingQueue<DownloadTask>? = null
    var tmpQueue: LinkedList<DownloadTask>? = null
    val NOTIFYID = 1599407175
    var threadPool: ExecutorService? = null
    @Volatile
    var max = -1
    @Volatile
    var progress = 0
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        openNotification()
        queue = LinkedBlockingQueue<DownloadTask>()
        tmpQueue = LinkedList<DownloadTask>()
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
        downloadThreadStart()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        synchronized(this){
            if(queue != null || intent != null){
                val t = DownloadTask(intent!!.getStringExtra("localpath"),intent.getStringExtra("catalogurl"))
                if(max == -1){
                    queue!!.offer(t)
                    max = 0 //从网上解析目录需要时间，max不会马上赋值，所有在这里改变
                }else{
                    tmpQueue!!.add(t)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager?.cancel(NOTIFYID)
        threadPool?.shutdown()
    }

    //TODO 优化
    fun downloadThreadStart(){
        Thread{
            Thread.sleep(100)
            while (true){
                var taskList: ArrayList<DownloadTask.ChapterRunnable>? = null
                try {
                    taskList = queue!!.take().getRunnables()
                }catch (e: Exception){
                    stopSelf()
                    break
                }
                max += taskList.size
                Observable.fromIterable(taskList)
                        .flatMap { it ->
                            Observable.create<Int>( { e -> threadPool?.execute(it.setFun { e.onNext(1) }) } )
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            updateNotification(max, ++progress)
                        }
            }
        }.start()
    }

    fun openNotification(){
        builder = NotificationCompat.Builder(this, "reader")
                .setTicker(getString(R.string.app_name))
                .setContentTitle(getString(R.string.prepare_download))
                .setSmallIcon(R.drawable.ic_launcher_background)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    fun updateNotification(max: Int, progress: Int){
        var str: String?
        if(tmpQueue!!.isEmpty()){
            str = ""
        }else{
            str = ",${getString(R.string.wait2download)}".replace("n", tmpQueue!!.size.toString())
        }
        builder?.setProgress(max, progress, false)?.setContentTitle("${getString(R.string.downloading)}:$progress/$max$str")
        mNotificationManager?.notify(NOTIFYID, builder?.build())
        if(progress == max && max != 0){
            if(tmpQueue!!.size == 0){
                stopSelf()
            }else {
                queue!!.offer(tmpQueue!!.removeFirst())
            }
        }
    }

}