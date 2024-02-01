package com.gumibom.travelmaker.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.gumibom.travelmaker.ui.dialog.ClickEventDialog
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "PermissionChecker"
class PermissionChecker (private val context : Context){

    lateinit var permitted : PermissionListener
    fun checkPermission(permission: Array<String>) : Boolean{
        //내가 받은 권한들을 리스트로 받고 하나라도 거부된 권한이 있다면 false를 리턴한다.
        Log.d(TAG, "checkPermission:START ${permission.size}")
        for (tokens in permission){
            if (ActivityCompat.checkSelfPermission(context,tokens)!=PackageManager.PERMISSION_GRANTED) {
                return false
            }
            Log.d(TAG, "checkPermission: ${tokens.toString()} ${PackageManager.PERMISSION_GRANTED.toString()}")
        }
        Log.d(TAG, "checkPermission:END ")
        return true;
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermission() {
        val checker = PermissionChecker(context)
        val runtimePermissions = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
        )
        if (checker.checkPermission(runtimePermissions)) {
            checker.requestPermissionLauncher.launch(runtimePermissions)
            checker.permitted = object : PermissionListener {
                override fun onGranted() {
                    //퍼미션 획득 성공일때
                    Toast.makeText(context, "모든권한이 성공임", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "실패쓰~", Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(runtimePermissions)
        }
    }
    //권한 호출 후 결과받아 처리할 런처이다 권한 없는 것을 확인하고 다른 창이나 설정으로 이동시킨다.
    //startPermiossionRequestResult
    val requestPermissionLauncher = (context as AppCompatActivity).registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
        if (it.values.contains(false)){
            Toast.makeText(context,"권한이 부족합니다 설정창으로 넘어갑니다.",Toast.LENGTH_SHORT).show()
            moveToSettings()
        }else{
            Toast.makeText(context,"모든 권한이 허가됐습니다. ",Toast.LENGTH_SHORT).show()
            permitted.onGranted()
        }
    }
    //사용자가 권한을 허용하지 않았을때, 설정창으로 이동
    fun moveToSettings() { // TODO:  내 커스텀 다이얼로그로 수정
        val alertDialog = ClickEventDialog(context)
        alertDialog.setTitle("권한이 필요합니다.")
        alertDialog.setMessage("설정으로 이동합니다.")
        alertDialog.setPositiveBtnTitle("확인")
        alertDialog.setPositiveButtonListener {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + context.packageName))
            context.startActivity(intent)
            alertDialog.clickDialogDissMiss()
        }
        alertDialog.setNegativeButton("취소") { dialogInterface, i -> dialogInterface.cancel() }
        alertDialog.clickDialogShow()
    }

}