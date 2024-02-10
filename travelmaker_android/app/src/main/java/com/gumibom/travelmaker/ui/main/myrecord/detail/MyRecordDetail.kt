package com.gumibom.travelmaker.ui.main.myrecord.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.gumibom.travelmaker.R
import com.gumibom.travelmaker.constant.NO_RECORD
import com.gumibom.travelmaker.data.dto.request.DeleteRecordRequestDTO
import com.gumibom.travelmaker.databinding.FragmentMyRecordBinding
import com.gumibom.travelmaker.databinding.FragmentMyRecordDetailBinding
import com.gumibom.travelmaker.model.pamphlet.Record
import com.gumibom.travelmaker.ui.dialog.ClickEventDialog
import com.gumibom.travelmaker.ui.main.MainActivity
import com.gumibom.travelmaker.ui.main.findmate.meeting_post.MeetingPostActivity
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MyRecordDetail_싸피"
@AndroidEntryPoint
class MyRecordDetail : Fragment() {

    private var _binding: FragmentMyRecordDetailBinding? = null
    private val binding get() = _binding!!
    private val myRecordDetailViewModel : MyRecordDetailViewModel by viewModels()
    private lateinit var activity: MainActivity
    private lateinit var adapter : MyRecordDetailAdapter
    private var pamphletId : Long = 0
    private var recordId : Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pamphletId = arguments?.getLong("pamphletId") ?: 0
        Log.d(TAG, "onCreate: $pamphletId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyRecordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInit()
        setAdapter()
        observeRecord()
        createMyRecord()
        deleteRecord()
    }

    /**
     * 아이콘 클릭 시 다이얼로그 생성
     * 사진+텍스트 또는 영상을 업로드 할 수 있게하는 페이지 이동
     */
    private fun createMyRecord() {
        binding.ivMyRecordDetailEdit.setOnClickListener {
            Log.d(TAG, "createMyRecord: 호출되니")

            val alertDialog = ClickEventDialog(requireContext())
            val bundle = bundleOf("pamphletId" to pamphletId)

            alertDialog.setIgnoreTitleContent()
            alertDialog.setPositiveBtnTitle("사진 + 텍스트를 만들어보세요")
            alertDialog.setNegativeBtnTitle("영상을 만들어보세요.")

            alertDialog.setPositiveButtonListener {
                // 사진 + 텍스트 화면으로 이동
                Navigation.findNavController(it).navigate(R.id.action_myRecordDetail_to_makeMyRecordPictureFragment, bundle)
            }

            alertDialog.setNegativeButtonListener {
                // 영상 화면으로 이동
                Navigation.findNavController(it).navigate(R.id.action_myRecordDetail_to_makeMyRecordVideoFragment, bundle)
            }
            alertDialog.clickDialogShow()
        }
    }

    private fun setAdapter() {
        adapter = MyRecordDetailAdapter(requireContext(), myRecordDetailViewModel)
        binding.rvMyRecordDetail.adapter = adapter

        myRecordDetailViewModel.myAllRecord.observe(viewLifecycleOwner) { recordList ->
            if (recordList.isEmpty()) {
                binding.tvMyRecordDetailText.text = NO_RECORD
            } else {
                setInitFragmentBody(recordList[0])
                adapter.submitList(recordList.toMutableList())
            }
        }
    }

    /**
     * 초기 세팅
     */
    private fun setInit() {
        myRecordDetailViewModel.getMyAllRecord(pamphletId)
    }

    /**
     * 초기 리싸이클러뷰 밑에 화면 렌더링
     */
    private fun setInitFragmentBody(record : Record) {
        // TODO 영상 기능 구현 시 if로 분기 처리

        Glide.with(this)
            .load(record.imgUrl)
            .into(binding.ivMyRecordDetail)

        Glide.with(this)
            .load(emojiDrawableId[record.emoji])
            .into(binding.ivMyRecordDetailEmoji)

        binding.tvMyRecordDetailText.text = record.text
    }

    /**
     * 리싸이클러뷰 아이템 클릭 시 밑에 화면 렌더링
     */
    private fun observeRecord() {
        myRecordDetailViewModel.record.observe(viewLifecycleOwner) { record ->
            recordId = record.recordId
            setInitFragmentBody(record)
        }

        myRecordDetailViewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "기록 삭제 성공", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "기록 삭제 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Record를 삭제하는 api 호출하는 함수
     */
    private fun deleteRecord() {
        binding.btnMyRecordDetailDelete.setOnClickListener {
            val deleteRecordRequestDTO = DeleteRecordRequestDTO(
                pamphletId,
                recordId
            )

            myRecordDetailViewModel.deleteRecord(deleteRecordRequestDTO)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val emojiDrawableId = mapOf<String, Int>("HAPPY" to R.drawable.happy, "SMILE" to R.drawable.smile, "SOSO" to R.drawable.soso,
                                                "SAD" to R.drawable.sad, "ANGRY" to R.drawable.angry)
    }
}