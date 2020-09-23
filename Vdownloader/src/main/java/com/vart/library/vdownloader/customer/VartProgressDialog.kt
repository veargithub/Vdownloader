package com.vart.library.vdownloader.customer

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.vart.library.vdownloader.R


class VartProgressDialog(
    context: Context,
    layoutRes: Int = 0,
    var id: Int = 0,
    var subId: Int = 0,
    var title: String? = null,
    var tips: String? = null,
    var onInteractionListener: OnInteractionListener? = null,
    var canCancel: Boolean? = true,
    var btnConfirmEnabled: Boolean? = null,
    var btnCancelEnabled: Boolean? = null,
    var btnConfirmText: String = "",
    var btnCancelText: String = ""

): Dialog(context), View.OnClickListener {

    var tvLabel: TextView? = null
    var tvTips: TextView? = null
    var tvConfirm: TextView? = null
    var tvCancel: TextView? = null
    var progressBar: ProgressBar? = null

    init {
        initLayout(layoutRes)
        initView()
    }

    private fun initLayout(layoutRes: Int) {
        if (layoutRes == 0) {
            setContentView(R.layout.dialog_vart_progressbar)
        } else {
            setContentView(layoutRes)
        }
    }

    private fun initView() {
        tvLabel = findViewById(R.id.tvLabel)
        tvLabel?.text = title
        tvTips = findViewById(R.id.tvTips)
        if (TextUtils.isEmpty(tips)) {
            tvTips?.visibility = View.GONE
        } else {
            tvTips?.visibility = View.VISIBLE
            tvTips?.text = tips
        }

        tvConfirm = findViewById(R.id.tvConfirm)
        tvConfirm?.isEnabled = btnConfirmEnabled ?: true
        tvConfirm?.text = if (btnConfirmText.isBlank()) "确定" else btnConfirmText
        tvConfirm!!.setOnClickListener(this)

        tvCancel = findViewById(R.id.tvCancel)
        tvCancel?.isEnabled = btnCancelEnabled ?: true
        tvCancel?.text = if (btnCancelText.isBlank()) "取消" else btnCancelText

        tvCancel!!.setOnClickListener(this)

        progressBar = findViewById(R.id.progressBar)
        progressBar?.progress = 0

        this.setCancelable(canCancel ?: true)

    }

    fun setProgress(progress: Int) {
        progressBar?.progress = progress
    }

    override fun onClick(v: View) {
        val vId = v.id
        if (vId == R.id.tvConfirm) {
            onInteractionListener?.onConfirm(id, subId)
        } else if (vId == R.id.tvCancel) {
            onInteractionListener?.onCancel(id, subId)
        }
    }

    data class Builder(
        var context: Context? = null,
        var layoutRes: Int = 0,
        var id: Int = 0,
        var subId: Int = 0,
        var title: String? = null,
        var tips: String? = null,
        var onInteractionListener: OnInteractionListener? = null,
        var canCancel: Boolean? = null,
        var btnConfirmEnabled: Boolean? = null,
        var btnCancelEnabled: Boolean? = null,
        var btnConfirmText: String = "",
        var btnCancelText: String = ""
    ) {

        fun context(context: Context) = apply { this.context = context }
        fun id(id: Int) = apply { this.id = id }
        fun subId(subId: Int) = apply { this.subId = subId }
        fun layoutRes(layoutRes: Int) = apply { this.layoutRes = layoutRes }
        fun title(title: String?) = apply { this.title = title }
        fun tips(tips: String?) = apply { this.tips = tips }
        fun onInteractionListener(onInteractionListener: OnInteractionListener?) = apply { this.onInteractionListener = onInteractionListener }
        fun canCancel(cancelable: Boolean) = apply { this.canCancel = cancelable }
        fun btnConfirmEnabled(enabled: Boolean) = apply { this.btnConfirmEnabled = enabled }
        fun btnCancelEnabled(enabled: Boolean) = apply { this.btnCancelEnabled = enabled }
        fun btnConfirmText(text: String) = apply { this.btnConfirmText = text }
        fun btnCancelText(text: String) = apply { this.btnCancelText = text }

        fun build(): VartProgressDialog {
            return VartProgressDialog(context!!, layoutRes, id, subId, title, tips, onInteractionListener,
                canCancel, btnConfirmEnabled, btnCancelEnabled, btnConfirmText, btnCancelText)
        }
    }

    interface OnInteractionListener {
        fun onConfirm(id: Int, subId: Int)
        fun onCancel(id: Int, subId: Int)
    }

}