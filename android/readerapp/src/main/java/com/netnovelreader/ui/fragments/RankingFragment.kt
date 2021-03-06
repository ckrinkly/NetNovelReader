package com.netnovelreader.ui.fragments

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.FragmentRankingBinding
import com.netnovelreader.ui.adapters.RankingPageListAdapter
import com.netnovelreader.vm.RankingViewModel


class RankingFragment : androidx.fragment.app.Fragment() {
    lateinit var binding: FragmentRankingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_ranking, container, false
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(RankingViewModel::class.java)

        val adapter = RankingPageListAdapter(viewModel)
        binding.rankingRecycler.adapter = adapter
        viewModel?.ranking?.observe(this, Observer(adapter::submitList))
        viewModel?.networkState?.observe(this, Observer(adapter::setNetworkState))
        viewModel?.searchCommand?.observe(this, Observer { sb ->
            sb.takeIf { (it?.length ?: 0) > 0  } ?: return@Observer
            val bookname = sb!!.toString()
            sb.delete(0, sb.length)
            val bundle = Bundle().apply {
                putString("bookname", bookname)
                putInt("type", SearchFragment.TYPE_RANKING)
            }
            NavHostFragment.findNavController(this@RankingFragment)
                .navigate(R.id.action_homeFragment_to_searchFragment, bundle)
        })
    }
}
