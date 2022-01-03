package com.fluidtouch.noteshelf.document

import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.fluidtouch.noteshelf2.R
import kotlinx.android.synthetic.main.fragment_refresh.*

class FTRefreshFragment : Fragment() {
    @BindView(R.id.refresh_label_text_view)
    lateinit var labelTextView: AppCompatTextView
    @BindView(R.id.refresh_image_view)
    lateinit var imageView: ImageView
    @BindView(R.id.refresh_more_options_template_text_view)
    lateinit var templateTextView: AppCompatTextView
    @BindView(R.id.refresh_more_options_photo_text_view)
    lateinit var photoTextView: AppCompatTextView
    @BindView(R.id.refresh_more_options_import_text_view)
    lateinit var importTextView: AppCompatTextView
    @BindView(R.id.refresh_more_options_scan_text_view)
    lateinit var scanTextView: AppCompatTextView
    @BindView(R.id.refresh_layout)
    lateinit var refreshLayout: LinearLayout

    private var position = 0
    var listener: RefreshFragmentListener? = null

    companion object {
        val POSITION = "position"

        @JvmStatic
        fun newInstance(position: Int): FTRefreshFragment {
            val fragment = FTRefreshFragment()
            val bundle = Bundle()
            bundle.putInt(POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.position = arguments?.get(POSITION) as Int
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_refresh, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)

        this.listener = activity as RefreshFragmentListener
        layParent.addOnLayoutChangeListener { view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int ->
            layParent.setPadding((view.width * 0.2).toInt(), resources.getDimensionPixelOffset(R.dimen.refresh_parent_padding_top), (view.width * 0.2).toInt(), resources.getDimensionPixelOffset(R.dimen.refresh_parent_padding_top))
        }
        labelTextView = view.findViewById(R.id.refresh_label_text_view)
        imageView = view.findViewById(R.id.refresh_image_view)
        refreshLayout = view.findViewById(R.id.refresh_layout)

        templateTextView = view.findViewById(R.id.refresh_more_options_template_text_view)
        photoTextView = view.findViewById(R.id.refresh_more_options_photo_text_view)
        importTextView = view.findViewById(R.id.refresh_more_options_import_text_view)
        scanTextView = view.findViewById(R.id.refresh_more_options_scan_text_view)

        refreshLayout.setOnClickListener {
            listener!!.addNewPage()
        }

        templateTextView.setOnClickListener {
            listener!!.addNewPageFromTemplate()
        }

        importTextView.setOnClickListener {
            listener!!.importPdfDocument()
        }

        scanTextView.setOnClickListener {
            listener!!.scanDocument()
        }

        photoTextView.setOnClickListener {
            listener!!.addPageFromPhoto()
        }
    }

    interface RefreshFragmentListener {
        fun getPageRect(positon: Int): RectF
        fun addNewPage()
        fun addNewPageFromTemplate()
        fun importPdfDocument()
        fun scanDocument()
        fun addPageFromPhoto()
    }
}