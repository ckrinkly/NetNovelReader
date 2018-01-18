package com.netnovelreader.reader

import android.util.Log
import com.netnovelreader.data.database.ChapterSQLManager
import com.netnovelreader.data.database.ShelfSQLManager
import com.netnovelreader.utils.getSavePath
import java.io.FileReader
import java.io.IOException

/**
 * Created by yangbo on 18-1-13.
 */
class ReaderViewModel(val bookName: String) : IReaderContract.IReaderViewModel {
    var readerBean: ReaderBean? = null

    @Throws(IOException::class)
    override fun getChapterText(chapterNum: Int, dirName: String): String {
        Log.d("===============", "view model getChapter start==========")
        val sb = StringBuilder()
        val chapterPath = "${getSavePath()}/$dirName/${ChapterSQLManager().getChapterName(dirName, chapterNum)}"
        val fr = FileReader(chapterPath)
        fr.forEachLine { sb.append(it) }
        fr.close()
        Log.d("===============", "view model getChapter finish==========")
        return sb.toString()
    }

    fun setRecord(bookname: String, record: String){
        ShelfSQLManager().setRecord(bookname, record)
    }

    /**
     * 获取章节总数
     */
    fun getChapterCount(): Int = ChapterSQLManager().getChapterCount("BOOK${ShelfSQLManager().getRecord(bookName)[0]}")

    fun splitChapterTxt(chapter: String, width: Int, height: Int, txtFontSize: Float): ArrayList<ArrayList<String>>{
        val tmpArray = chapter.split("\\n")
        val tmplist = ArrayList<String>()
        tmpArray.forEach{
            val totalCount = width / txtFontSize.toInt() - 1
            if(it.length > totalCount){
                val count = it.length / totalCount
                for(i in 0..count - 1){
                    tmplist.add(it.substring(i * totalCount, (i + 1) * totalCount))
                }
                if(it.length % totalCount != 0){
                    tmplist.add(it.substring(count * totalCount))
                }
            }else {
                tmplist.add(it)
            }
        }
        val arrayList = ArrayList<ArrayList<String>>()
        val totalCount = height / txtFontSize.toInt() - 1
        if( tmplist.size > totalCount){
            val count = tmplist.size / totalCount
            for(i in 0..count -1){
                val a = ArrayList<String>()
                tmplist.subList(i * totalCount, (i + 1) * totalCount).forEach{ a.add(it)}
                arrayList.add(a)
            }
            if(tmplist.size % totalCount != 0){
                val b = ArrayList<String>()
                tmplist.subList(count * totalCount, tmplist.size - 1).forEach{ b.add(it)}
                arrayList.add(b)
            }
        }
        return arrayList
    }

    override fun getModel(): ReaderBean? {
        readerBean ?: synchronized(this){
            readerBean ?: run{ readerBean = ReaderBean() }
        }
        return readerBean
    }
}