package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.netnovelreader.repo.SiteSelectorRepo
import com.netnovelreader.repo.db.SiteSelectorEntity
import com.netnovelreader.utils.IO_EXECUTOR
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class SiteSelectorViewModel(val repo: SiteSelectorRepo, app: Application) : AndroidViewModel(app) {
    val allSiteSelector = LivePagedListBuilder(
        repo.allSiteSelectors,
        PagedList.Config.Builder()
            .setPageSize(30)
            .setEnablePlaceholders(false)
            .build()
    ).build()
    val editPreferenceCommand = MutableLiveData<SiteSelectorEntity>()

    fun updatePreference(perferNet: Boolean) {
        Observable.zip(
            repo.getSelectorsFromNet(),
            repo.getSelectorsFromLocal().toObservable(),
            BiFunction<List<SiteSelectorEntity>, List<SiteSelectorEntity>,
                    Pair<List<SiteSelectorEntity>, List<SiteSelectorEntity>>> { t1, t2 ->
                Pair(t1, t2)
            }
        ).subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe {
                if (perferNet) {
                    it.first.forEach { item ->
                        it.second.firstOrNull { item.hostname == it.hostname }
                            ?.let { item._id = it._id }
                    }
                    repo.saveAll(it.first)
                } else {
                    val list = it.first.filter { item ->
                        it.second.none { item.hostname == it.hostname }
                    }
                    repo.saveAll(list)
                }
            }
    }

    fun editPreference(entity: SiteSelectorEntity) {
        editPreferenceCommand.value = entity
    }

    fun savePreference(entity: SiteSelectorEntity) {
        if (entity.hostname.isEmpty()) {
            return
        }
        repo.saveSelector(entity)
    }
}