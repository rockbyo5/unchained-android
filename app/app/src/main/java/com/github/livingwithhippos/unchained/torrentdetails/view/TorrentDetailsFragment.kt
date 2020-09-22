package com.github.livingwithhippos.unchained.torrentdetails.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.livingwithhippos.unchained.R
import com.github.livingwithhippos.unchained.databinding.FragmentTorrentDetailsBinding
import com.github.livingwithhippos.unchained.torrentdetails.viewmodel.TorrentDetailsViewmodel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TorrentDetailsFragment : Fragment(), TorrentDetailsListener {

    private val viewModel: TorrentDetailsViewmodel by viewModels()

    val args: TorrentDetailsFragmentArgs by navArgs()

    // possible status are magnet_error, magnet_conversion, waiting_files_selection,
    // queued, downloading, downloaded, error, virus, compressing, uploading, dead
    val loadingStatusList = listOf<String>(
        "magnet_conversion",
        "waiting_files_selection",
        "queued",
        "compressing",
        "uploading"
    )
    val endedStatusList = listOf<String>("magnet_error", "downloaded", "error", "virus", "dead")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val torrentBinding = FragmentTorrentDetailsBinding.inflate(inflater, container, false)


        val statusTranslation = mapOf<String, String>(
            "magnet_error" to resources.getString(R.string.magnet_error),
            "magnet_conversion" to resources.getString(R.string.magnet_conversion),
            "waiting_files_selection" to resources.getString(R.string.waiting_files_selection),
            "queued" to resources.getString(R.string.queued),
            "downloading" to resources.getString(R.string.downloading),
            "downloaded" to resources.getString(R.string.downloaded),
            "error" to resources.getString(R.string.error),
            "virus" to resources.getString(R.string.virus),
            "compressing" to resources.getString(R.string.compressing),
            "uploading" to resources.getString(R.string.uploading),
            "dead" to resources.getString(R.string.dead)
        )

        torrentBinding.loadingStatusList = loadingStatusList
        torrentBinding.statusTranslation = statusTranslation
        torrentBinding.listener = this

        viewModel.torrentLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                //todo: manage what happens when the torrent is deleted while in this screen
                /*
                we get an error from the rest call, add management for those first
                 {
                    "error": "unknown_ressource",
                    "error_code": 7
                }
                 */
                torrentBinding.torrent = it
                if (loadingStatusList.contains(it.status) || it.status == "downloading")
                    fetchTorrent()
            }
        })

        viewModel.fetchTorrentDetails(args.torrentID)

        return torrentBinding.root
    }

    // fetch the torrent info every 2 seconds
    private fun fetchTorrent(delay: Long = 2000) {
        lifecycleScope.launch {
            delay(delay)
            viewModel.fetchTorrentDetails(args.torrentID)
        }
    }

    override fun onDownloadClick(links: List<String>) {
        val action =
            TorrentDetailsFragmentDirections.actionTorrentDetailsFragmentToNewDownloadDest(links.toTypedArray())
        findNavController().navigate(action)
    }
}

interface TorrentDetailsListener {
    fun onDownloadClick(links: List<String>)
}