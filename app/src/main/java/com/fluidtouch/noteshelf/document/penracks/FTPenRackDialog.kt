package com.fluidtouch.noteshelf.document.penracks

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.fluidtouch.noteshelf.FTApp
import com.fluidtouch.noteshelf.FTRuntimeException
import com.fluidtouch.noteshelf.commons.FTLog
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog
import com.fluidtouch.noteshelf.commons.utils.ColorUtil
import com.fluidtouch.noteshelf.commons.utils.DrawableUtil
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools
import com.fluidtouch.noteshelf.document.enums.PenOrHighlighterInfo
import com.fluidtouch.noteshelf.document.penracks.FTEditColorsFragment.FTEditColorsContainerCallback
import com.fluidtouch.noteshelf.document.penracks.favorites.FTFavoritesAdapter
import com.fluidtouch.noteshelf.document.penracks.favorites.FTFavoritesAdapter.FavoritesAdapterCallback
import com.fluidtouch.noteshelf.document.penracks.favorites.FTFavoritesProvider
import com.fluidtouch.noteshelf.document.penracks.favorites.Favorite
import com.fluidtouch.noteshelf.preferences.PenRackPref
import com.fluidtouch.noteshelf.preferences.SystemPref
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics
import com.fluidtouch.noteshelf2.R
import com.fluidtouch.renderingengine.annotation.FTPenType
import com.google.android.material.textview.MaterialTextView
import com.thesurix.gesturerecycler.GestureAdapter
import com.thesurix.gesturerecycler.GestureManager
import kotlinx.android.synthetic.main.color_selection_layout.*
import kotlinx.android.synthetic.main.content_penrack_dialog.*
import java.util.*

class FTPenRackDialog : FTBaseDialog.Popup(), FavoritesAdapterCallback, CompoundButton.OnCheckedChangeListener {
    private var tempColor = "#c5c5b2"
    private var dialogType = FTToolBarTools.PEN
    private var expandMargin = 72
    private var defaultMargin = 100
    private val sizesDrawables = arrayOf(R.mipmap.sizehighlighter_1, R.mipmap.sizehighlighter_2, R.mipmap.sizehighlighter_3, R.mipmap.sizehighlighter_4, R.mipmap.sizehighlighter_5, R.mipmap.sizehighlighter_6)
    private var tempPen: View? = null
    private var tempPoint: View? = null
    private var tempSizeView: View? = null
    private lateinit var tempLay: View
    private var tempView: View? = null
    private var tempShadow: View? = null
    private var tempColorView: View? = null
    private lateinit var mPenPref: PenRackPref
    private val penDefaultColor = "#939393"

    //private PenRackDialogListener listener;
    private lateinit var favoritesAdapter: FTFavoritesAdapter
    private lateinit var favoritesProvider: FTFavoritesProvider
    var mPenRackDialogListener: PenRackDialogListener? = null
    private lateinit var penRackModel: FTPenRackModel

    private fun valueAnimator(view: View, start: Int, end: Int) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = 200
        animator.addUpdateListener { animation ->
            val lp = view.layoutParams as RelativeLayout.LayoutParams
            lp.setMargins(0, (animation.animatedValue as Int), 0, 0)
            view.layoutParams = lp
        }
        animator.start()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mPenRackDialogListener = if (context is PenRackDialogListener) {
            context
        } else {
            throw FTRuntimeException("$context must implement mPenRackDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (super.isMobile()) {
            val dialog = Dialog(requireContext())
            dialog.setCanceledOnTouchOutside(true)
            val window = dialog.window
            window?.apply {
                this.setDimAmount(0.0f)
                this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            dialog
        } else {
            val dialog = super.onCreateDialog(savedInstanceState)
            val window = dialog.window
            window?.setGravity(Gravity.TOP or Gravity.CENTER)
            dialog
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_penrack, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        expandMargin = resources.getDimension(R.dimen.pen_collapse_margin).toInt()
        defaultMargin = resources.getDimension(R.dimen.pen_expand_margin).toInt()
        mPenPref = PenRackPref().init(PenRackPref.PREF_NAME)

        //GetData from Plist
        penRackModel = FTPenRackModel.getDefaultPenRack(dialogType, requireContext())
        when (dialogType) {
            FTToolBarTools.PEN -> {
                setSizes(dialogType)
            }
            FTToolBarTools.HIGHLIGHTER -> {
                setSizes(FTToolBarTools.PEN)
            }
            else -> {
                setSizes(FTToolBarTools.PEN)
            }
        }
        checkbox.isChecked = mPenPref.get(PenRackPref.CHECK_BOX_KEY, false)
        checkbox.setOnClickListener { v: View? ->
            FTFirebaseAnalytics.logEvent("ShowFavToolbar")
            FTLog.crashlyticsLog("Penrack: ShowFavToolbar")
            saveCheckBoxStatus(checkbox.isChecked)
            mPenRackDialogListener!!.enableFavWidgetToolbar(checkbox.isChecked)
        }
        favToolBarLyt.setOnClickListener { v: View? -> checkbox.performClick() }
        if (dialogType != FTToolBarTools.ERASER) {
            //Get Pen Colors and Set To Ui
            initFavoritesLayout()
            addColorViews(penRackModel.defaultRack, penRackModel.rackName)
            eraserOptions.visibility = View.GONE
            eraserOptionsView.visibility = View.GONE
            layEraserOptions.visibility = View.GONE
            eraserMoreOptionsDivider!!.visibility = View.GONE

            //Pen Selection Events
            laySelectPen!!.setOnClickListener(View.OnClickListener {
                if (tempPen === imgPenRackPen) {
                    return@OnClickListener
                }
                if (dialogType == FTToolBarTools.HIGHLIGHTER) {
                    setSizes(FTToolBarTools.PEN)
                }
                FTFirebaseAnalytics.logEvent("Pen_Ballpoint")
                FTLog.crashlyticsLog("Penrack: Pen_Ballpoint")

                //String color = "#" + Integer.toHexString(mPenPref.get(penRackModel.prefColorKey, Color.parseColor(tempColor))).toUpperCase(Locale.ENGLISH);
                val color = mPenPref.get(penRackModel.prefColorKey, Color.parseColor(tempColor))
                penorHighlighterInfoChanged(dialogType, color,
                        mPenPref.get(penRackModel.prefSizeKey, penRackModel.defaultSize), if (dialogType == FTToolBarTools.PEN) FTPenType.pen.name else FTPenType.highlighter.name)
                if (tempPen != null) {
                    valueAnimator(tempLay, expandMargin, defaultMargin)
                    DrawableUtil.setGradientDrawableColor(tempView, "#ecece4", 0)
                    var resource = R.mipmap.calligraphycolor
                    if (tempPen === imgPenRackFine) resource = R.mipmap.finecolor
                    DrawableUtil.tintImage(context, tempPen, penDefaultColor, if (dialogType == FTToolBarTools.HIGHLIGHTER) R.mipmap.highflatcolor else resource)
                    tempShadow!!.visibility = View.GONE
                }
                if (tempPen != null) valueAnimator(layPens, defaultMargin, expandMargin) else {
                    val lp = layPens!!.layoutParams as RelativeLayout.LayoutParams
                    lp.setMargins(0, expandMargin, 0, 0)
                    layPens!!.layoutParams = lp
                }
                imgPenRackPenShadow!!.visibility = View.VISIBLE
                tempPen = imgPenRackPen
                tempPoint = imgPenPoint
                tempLay = layPens
                tempView = viewPen
                tempShadow = imgPenRackPenShadow
                DrawableUtil.setGradientDrawableColor(tempView, "#c5c5b2", 0)
                DrawableUtil.tintImage(context, tempPen, mPenPref.get(penRackModel.prefColorKey, tempColor), if (dialogType == FTToolBarTools.HIGHLIGHTER) R.mipmap.highroundcolor else R.mipmap.pencolor)
                if (!isFavorite) {
                    updateFavoritesInfo(false)
                }
                setFavoriteIcon(isFavorite)
            })
            laySelectCalligraphy!!.setOnClickListener(View.OnClickListener {
                if (tempPen === imgPenRackCalligraphy) {
                    return@OnClickListener
                }
                if (dialogType == FTToolBarTools.HIGHLIGHTER) {
                    setSizes(FTToolBarTools.HIGHLIGHTER)
                }
                FTFirebaseAnalytics.logEvent("Pen_FountainPen")
                FTLog.crashlyticsLog("Penrack: Pen_FountainPen")
                val color = mPenPref.get(penRackModel.prefColorKey, Color.parseColor(tempColor))
                penorHighlighterInfoChanged(dialogType, color,
                        mPenPref.get(penRackModel.prefSizeKey, penRackModel.defaultSize), if (dialogType == FTToolBarTools.PEN) FTPenType.caligraphy.toString() else FTPenType.flatHighlighter.name)
                if (tempPen != null) {
                    valueAnimator(tempLay, expandMargin, defaultMargin)
                    DrawableUtil.setGradientDrawableColor(tempView, "#ecece4", 0)
                    var resource = R.mipmap.pencolor
                    if (tempPen === imgPenRackFine) resource = R.mipmap.finecolor
                    DrawableUtil.tintImage(context, tempPen, penDefaultColor, if (dialogType == FTToolBarTools.HIGHLIGHTER) R.mipmap.highroundcolor else resource)
                    tempShadow!!.visibility = View.GONE
                }
                if (tempPen != null) valueAnimator(layCalligraphy, defaultMargin, expandMargin) else {
                    val lp = layCalligraphy!!.layoutParams as RelativeLayout.LayoutParams
                    lp.setMargins(0, expandMargin, 0, 0)
                    layCalligraphy!!.layoutParams = lp
                }
                imgPenRackCalligraphyShadow!!.visibility = View.VISIBLE
                tempPen = imgPenRackCalligraphy
                tempPoint = imgPenRackCalligraphyPoint
                tempLay = layCalligraphy
                tempView = viewCalligraphy
                tempShadow = imgPenRackCalligraphyShadow
                DrawableUtil.setGradientDrawableColor(tempView, "#c5c5b2", 0)
                DrawableUtil.tintImage(context, tempPen, mPenPref.get(penRackModel.prefColorKey, tempColor), if (dialogType == FTToolBarTools.HIGHLIGHTER) R.mipmap.highflatcolor else R.mipmap.calligraphycolor)
                setFavoriteIcon(isFavorite)
            })
            laySelectFine!!.setOnClickListener(View.OnClickListener {
                if (tempPen === imgPenRackFine) {
                    return@OnClickListener
                }
                FTFirebaseAnalytics.logEvent("Pen_Sharpie")
                FTLog.crashlyticsLog("Penrack: Pen_Sharpie")
                val color = mPenPref.get(penRackModel.prefColorKey, Color.parseColor(tempColor))
                penorHighlighterInfoChanged(dialogType, color,
                        mPenPref.get(penRackModel.prefSizeKey, penRackModel.defaultSize), FTPenType.pilotPen.name)
                if (tempPen != null) {
                    valueAnimator(tempLay, expandMargin, defaultMargin)
                    DrawableUtil.setGradientDrawableColor(tempView, "#ecece4", 0)
                    var resource = R.mipmap.pencolor
                    if (tempPen === imgPenRackCalligraphy) resource = R.mipmap.calligraphycolor
                    DrawableUtil.tintImage(context, tempPen, penDefaultColor, resource)
                    tempShadow!!.visibility = View.GONE
                }
                if (tempPen != null) valueAnimator(layFine, defaultMargin, expandMargin) else {
                    val lp = layFine!!.layoutParams as RelativeLayout.LayoutParams
                    lp.setMargins(0, expandMargin, 0, 0)
                    layFine!!.layoutParams = lp
                }
                imgPenRackFineShadow!!.visibility = View.VISIBLE
                tempPen = imgPenRackFine
                tempPoint = imgPenRackFinePoint
                tempLay = layFine
                tempView = viewFine
                tempShadow = imgPenRackFineShadow
                DrawableUtil.setGradientDrawableColor(tempView, "#c5c5b2", 0)
                //DrawableUtil.setGradientDrawableColor(tempPen, mPenPref.get(penRackModel.prefColorKey, tempColor), 0);
                DrawableUtil.tintImage(context, tempPen, mPenPref.get(penRackModel.prefColorKey, tempColor), R.mipmap.finecolor)
                if (!isFavorite) {
                    updateFavoritesInfo(false)
                }
                setFavoriteIcon(isFavorite)
            })

            //Init and Apply Selected data
            DrawableUtil.setGradientDrawableColor(viewPen, "#ecece4", 0)
            DrawableUtil.setGradientDrawableColor(viewCalligraphy, "#ecece4", 0)
            DrawableUtil.setGradientDrawableColor(viewFine, "#ecece4", 0)
            if (dialogType == FTToolBarTools.PEN) {
                DrawableUtil.tintImage(context, imgPenRackPen, penDefaultColor, R.mipmap.pencolor)
                DrawableUtil.tintImage(context, imgPenRackCalligraphy, penDefaultColor, R.mipmap.calligraphycolor)
                DrawableUtil.tintImage(context, imgPenRackFine, penDefaultColor, R.mipmap.finecolor)
                if (FTPenType.valueOf(mPenPref.get(penRackModel.prefPenKey, FTPenType.pen.toString())) == FTPenType.caligraphy) {
                    laySelectCalligraphy!!.performClick()
                } else if (FTPenType.valueOf(mPenPref.get(penRackModel.prefPenKey, FTPenType.pen.toString())) == FTPenType.pilotPen) {
                    laySelectFine!!.performClick()
                } else {
                    laySelectPen!!.performClick()
                }
            } else {
                imgPenRackPen!!.setImageResource(R.mipmap.highroundcolor)
                imgPenPoint!!.setImageResource(R.mipmap.highroundmask)
                imgPenRackPenShadow!!.setImageResource(R.mipmap.highroundshadow)
                imgPenRackCalligraphy!!.setImageResource(R.mipmap.highflatcolor)
                imgPenRackCalligraphyPoint!!.setImageResource(R.mipmap.highflatmask)
                imgPenRackCalligraphyShadow!!.setImageResource(R.mipmap.highflatshadow)
                DrawableUtil.tintImage(context, imgPenRackPen, penDefaultColor, R.mipmap.highroundcolor)
                DrawableUtil.tintImage(context, imgPenRackCalligraphy, penDefaultColor, R.mipmap.highflatcolor)
                laySelectFine!!.visibility = View.GONE
                if (FTPenType.valueOf(mPenPref.get(penRackModel.prefPenKey, FTPenType.pen.toString())) == FTPenType.flatHighlighter) {
                    laySelectCalligraphy!!.performClick()
                } else {
                    laySelectPen!!.performClick()
                }
            }
            setFavoriteIcon(isFavorite)
        } else {
            FTFirebaseAnalytics.logEvent("TapEraser")
            FTLog.crashlyticsLog("Penrack: Eraser")
            favouriteIcon.visibility = View.GONE
            favoritesChooseLayout.visibility = View.GONE
            favoritesRecyclerView.visibility = View.GONE
            favToolBarLyt.visibility = View.GONE
            nonFavoritelayout.visibility = View.VISIBLE
            autoSelectPrevToolSwitch!!.setOnCheckedChangeListener(this)
            eraseEntireStrokeSwitch!!.setOnCheckedChangeListener(this)
            eraseHighlighterStrokeSwitch!!.setOnCheckedChangeListener(this)
            if (mPenPref.getEraserOptions().equals("off", ignoreCase = true)) {
                eraserOptions("off", "off", R.mipmap.bubble_more, View.GONE)
            } else {
                eraserOptions("on", "on", R.mipmap.bubble_hide, View.VISIBLE)
            }

            /*
             * by default make erase entire stroke option true when app installed firstime
             * */eraseEntireStrokeSwitch!!.isChecked = mPenPref.get(PenRackPref.ERASE_ENTIRE_STROKE_SWITCH, true)
            if (mPenPref.get(PenRackPref.AUTO_SELECTION_PREVIOUS_TOOL_SWITCH, false) == true) {
                autoSelectPrevToolSwitch!!.isChecked = true
            }
            if (mPenPref.get(PenRackPref.ERASE_ENTIRE_STROKE_SWITCH, false) == true) {
                eraseEntireStrokeSwitch!!.isChecked = true
            }
            if (mPenPref.get(PenRackPref.ERASE_HIGHLIGHTER_STROKE_SWITCH, false) == true) {
                eraseHighlighterStrokeSwitch!!.isChecked = true
            }
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.topMargin = 0
            laySizes!!.layoutParams = layoutParams
            layPenRackPens!!.removeAllViews()
            layEraserOptions.visibility = View.VISIBLE
            eraserMoreOptionsDivider!!.visibility = View.VISIBLE
            eraserOptions.visibility = View.VISIBLE
            layPenRackPens!!.visibility = View.GONE
            layColors.removeAllViews()
            layColors.visibility = View.GONE
            txtClearPage!!.visibility = View.VISIBLE
            txtClearPage!!.setOnClickListener { v: View? ->
                FTFirebaseAnalytics.logEvent("Eraser_ClearPage")
                FTLog.crashlyticsLog("Penrack: Eraser_ClearPage")
                dismiss()
                mPenRackDialogListener!!.onClearPageSelected()
            }
            eraserOptions.setOnClickListener {
                if (eraserOptions.tag.toString().trim { it <= ' ' } == "on") {
                    eraserOptions("off", "off", R.mipmap.bubble_more, View.GONE)
                } else if (eraserOptions.tag.toString().trim { it <= ' ' } == "off") {
                    eraserOptions("on", "on", R.mipmap.bubble_hide, View.VISIBLE)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isMobile) {
            val window = dialog!!.window
            window!!.setGravity(Gravity.TOP or Gravity.CENTER)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val layoutParams = window.attributes
            val tv = TypedValue()
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                layoutParams.y = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                window.attributes = layoutParams
            }
        }
    }

    private fun eraserOptions(sharedPref: String, setTagOption: String, bubble_hide: Int, visibilityState: Int) {
        mPenPref.saveEraserOptions(sharedPref)
        eraserOptions.tag = setTagOption
        if (setTagOption.equals("off", ignoreCase = true)) {
            eraserOptions.setText(R.string.more_with_arrow)
            eraserOptions.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icondown, 0)
        } else {
            eraserOptions.setText(R.string.hide_with_arrow)
            eraserOptions.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.iconup, 0)
        }
        eraserOptionsView.visibility = visibilityState
    }

    fun saveCheckBoxStatus(checked: Boolean) {
        mPenPref.save(PenRackPref.CHECK_BOX_KEY, checked)
    }

    private fun penorHighlighterInfoChanged(penRackType: FTToolBarTools, penColor: Int, penSize: Int, penType: String) {
        mPenPref.save(PenRackPref.CURRENT_SELECTION, mPenPref.get(penRackModel.prefPenKey, penType))
        mPenRackDialogListener!!.penOrHighlighterInfoChanged(PenOrHighlighterInfo.getInfo(penRackType.toInt() + 1, penColor, penSize, penType))
    }

    private fun addColorViews(defaultPenRack: HashMap<String, Any>, rackName: String) {
        val colors = defaultPenRack["currentColors"] as Array<Any>?
        val colorStrings: MutableList<String> = ArrayList()
        for (i in colors!!.indices) {
            colorStrings.add(colors[i] as String)
        }
        if (!colorStrings.isEmpty()) {
            for (i in colorStrings.indices) {
                addColorView(i, colorStrings)
            }
            val scale = resources.displayMetrics.density
            val textView = TextView(context)
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textView.text = getString(R.string.edit)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getInteger(R.integer.fifteen).toFloat())
            textView.setTextColor(Color.parseColor("#4aa1ff"))
            textView.setPadding((resources.getInteger(R.integer.forty_six) * scale).toInt(), (resources.getInteger(R.integer.eight) * scale).toInt(), (resources.getInteger(R.integer.forty_six) * scale).toInt(), (resources.getInteger(R.integer.eight) * scale).toInt())
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clear_page_bg)
            textView.setOnClickListener { v: View? ->
                FTEditColorsFragment.newInstance(rackName, object : FTEditColorsContainerCallback {
                    @Synchronized
                    override fun addColor(color: String, position: Int) {
                        var position = position
                        FTFirebaseAnalytics.logEvent("Pen_SelectColor")
                        FTLog.crashlyticsLog("Penrack: Pen_SelectColor")
                        if (position != colorStrings.size && colorSelectionColorsLayout!!.childCount >= position && colorSelectionColorsLayout!!.getChildAt(position) != null) {
                            colorSelectionColorsLayout!!.removeViewAt(position)
                        }
                        if (position > colorStrings.size) {
                            position = colorStrings.size
                        }
                        colorStrings.add(position, if (color.contains("#")) color.split("#".toRegex()).toTypedArray()[1] else color)
                        //For selecting instant added color from the color rack
                        val addedColorView = addColorView(position, colorStrings)
                        selectColor(addedColorView, colorStrings)
                    }

                    @Synchronized
                    override fun fetchColors() {
                        colorSelectionColorsLayout!!.removeAllViews()
                        addColorViews(penRackModel.updatedRack, rackName)
                    }

                    @Synchronized
                    override fun removeColor(removedColor: String) {
                        val position = colorStrings.indexOf(removedColor)
                        if (position >= 0) {
                            colorStrings.removeAt(position)
                            colorSelectionColorsLayout!!.removeViewAt(position)
                        }
                    }

                    @Synchronized
                    override fun reorder(fromPos: Int, toPos: Int) {
                        val view = colorSelectionColorsLayout!!.getChildAt(fromPos)
                        colorSelectionColorsLayout!!.removeViewAt(fromPos)
                        colorSelectionColorsLayout!!.addView(view, toPos)
                    }
                }).show(childFragmentManager)
            }
            colorSelectionColorsLayout!!.addView(textView, colorSelectionColorsLayout!!.childCount)
        }
    }

    private fun addColorView(position: Int, colors: List<String>): View {
        val view1 = View(context)
        val color = colors[position]
        val mSize = convertDpToPx(resources.getInteger(R.integer.thirty_six))
        val mMargin = convertDpToPx(resources.getInteger(R.integer.fourteen))
        val layoutParams = LinearLayout.LayoutParams(mSize, mSize)
        layoutParams.rightMargin = mMargin
        view1.layoutParams = layoutParams

        //Set Selected Color
        if (mPenPref.get(penRackModel.prefColorKey, Color.parseColor("#" + colors[0])) == Color.parseColor("#$color")) {
            layColorScroll!!.postDelayed({ //hsv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                if (isAdded)
                    layColorScroll!!.scrollTo(view1.x.toInt() / 2, 0)
            }, 10)
            tempColor = "#$color"
            tempColorView = view1
            view1.background = ContextCompat.getDrawable(requireContext(), R.drawable.selected_color_bg)
            DrawableUtil.setGradientDrawableColor(view1, tempColor, 0)
        } else {
            view1.background = ContextCompat.getDrawable(requireContext(), R.drawable.circular_grey_bg)
            DrawableUtil.setGradientDrawableColor(view1, "#$color", 0)
        }
        if (ColorUtil.isLightColor(Color.parseColor("#$color"))) {
            if (view1.background is GradientDrawable) {
                (view1.background as GradientDrawable).setStroke(1, Color.parseColor("#cccccc"))
            } else if (view1.background is LayerDrawable) {
                ((view1.background as LayerDrawable).getDrawable(0) as GradientDrawable).setStroke(1, Color.parseColor("#cccccc"))
            }
        }
        view1.id = 100 + position
        view1.setOnClickListener { v: View ->
            FTFirebaseAnalytics.logEvent("Pen_EditColor")
            FTLog.crashlyticsLog("Penrack: Pen_EditColor")
            selectColor(v, colors)
        }
        colorSelectionColorsLayout!!.addView(view1, position)
        return view1
    }

    private fun selectColor(v: View, colors: List<String>) {
        val mPos = v.id
        if (mPos - 100 >= colors.size) {
            return
        }
        if (tempColorView != null) {
            tempColorView!!.background = requireContext().getDrawable(R.drawable.circular_grey_bg)
            DrawableUtil.setGradientDrawableColor(tempColorView, tempColor, 0)
        }
        tempColor = "#" + colors[mPos - 100]
        if (null != tempPen) {
            var resource = R.mipmap.pencolor
            if (dialogType == FTToolBarTools.PEN) {
                if (tempPen === imgPenRackPen) resource = R.mipmap.pencolor else if (tempPen === imgPenRackCalligraphy) resource = R.mipmap.calligraphycolor else if (tempPen === imgPenRackFine) resource = R.mipmap.finecolor
            } else {
                if (tempPen === imgPenRackPen) resource = R.mipmap.highroundcolor else if (tempPen === imgPenRackCalligraphy) resource = R.mipmap.highflatcolor
            }
            DrawableUtil.tintImage(context, tempPen, tempColor, resource)
        }
        v.background = requireContext().getDrawable(R.drawable.selected_color_bg)
        DrawableUtil.setGradientDrawableColor(v, tempColor, 0)
        tempColorView = v
        if (favoritesAdapter != null && !isFavorite) {
            updateFavoritesInfo(false)
        }
        penorHighlighterInfoChanged(dialogType, Color.parseColor(tempColor),
                mPenPref.get(penRackModel.prefSizeKey, penRackModel.defaultSize), mPenPref.get(penRackModel.prefPenKey, FTPenType.pen.name))
        setFavoriteIcon(isFavorite)
    }

    //Sets up the sizes for pens/highlighters/eraser
    private fun setSizes(tool: FTToolBarTools) {
        if (penRackModel.fontSizes != null) {
            laySizes!!.removeAllViews()
            for (i in penRackModel.fontSizes.indices) {
                val viewMain = layoutInflater.inflate(R.layout.penrack_size_view, null)
                val sizeView = viewMain.findViewById<View>(R.id.sizeView) as ImageView
                val size = if (tool == FTToolBarTools.PEN) convertDpToPx(penRackModel.sizes[i]) else LinearLayout.LayoutParams.WRAP_CONTENT
                val layoutParams = LinearLayout.LayoutParams(size, size)
                if (dialogType == FTToolBarTools.ERASER) {
                    if (i == 0) layoutParams.leftMargin = convertDpToPx(resources.getInteger(R.integer.thirteen)) else if (i == penRackModel.fontSizes.size - 1) {
                        layoutParams.rightMargin = convertDpToPx(resources.getInteger(R.integer.thirteen))
                    }
                } else {
                    if (i == 0) layoutParams.leftMargin = convertDpToPx(resources.getInteger(R.integer.ten)) else if (i == penRackModel.fontSizes.size - 1) layoutParams.rightMargin = convertDpToPx(resources.getInteger(R.integer.ten))
                }
                sizeView.layoutParams = layoutParams
                sizeView.background = ContextCompat.getDrawable(requireContext(), if (tool == FTToolBarTools.PEN) R.drawable.pen_size_bg else sizesDrawables[i])
                //Set Selected Size
                if (dialogType == FTToolBarTools.ERASER && i == penRackModel.fontSizes.size - 1) {
                    if (mPenPref.get(penRackModel.prefSizeKey, penRackModel.defaultSize) - 1 == i) {
                        sizeView.background = ContextCompat.getDrawable(requireContext(), R.mipmap.sizeauto_sel)
                        tempSizeView = sizeView
                    } else sizeView.background = ContextCompat.getDrawable(requireContext(), R.mipmap.sizeauto)
                } else {
                    if (mPenPref.get(penRackModel.prefSizeKey, penRackModel.defaultSize) - 1 == i) {
                        DrawableUtil.setGradientDrawableColor(sizeView, "#000000", 0)
                        tempSizeView = sizeView
                    } else {
                        DrawableUtil.setGradientDrawableColor(sizeView, "#c5c5b2", 0)
                    }
                }
                sizeView.id = i
                viewMain.id = i + penRackModel.fontSizes.size
                viewMain.setOnClickListener(sizeClickListener)
                viewMain.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                laySizes!!.addView(viewMain)
            }
        }
    }

    private val sizeClickListener: View.OnClickListener = View.OnClickListener {
        val log: String = if (dialogType == FTToolBarTools.ERASER) {
            if (it.id == 3) "Eraser_Auto" else "Eraser_size" + (it.id + 1)
        } else "Pen_Size_" + (it.id + 1)
        FTFirebaseAnalytics.logEvent(log)
        FTLog.crashlyticsLog("Penrack: $log")

        val sizeImageView = it.findViewById(it.id - penRackModel.fontSizes.size) as ImageView
        if (null != tempSizeView) {
            if (dialogType == FTToolBarTools.ERASER && mPenPref.get(penRackModel.prefSizeKey, 0) == 4)
                tempSizeView!!.background = ContextCompat.getDrawable(requireContext(), R.mipmap.sizeauto)
            else DrawableUtil.setGradientDrawableColor(tempSizeView, "#c5c5b2", 0)
        }

        val color = mPenPref.get(penRackModel.prefColorKey, Color.parseColor(tempColor))
        penorHighlighterInfoChanged(dialogType, color,
                sizeImageView.id + 1, mPenPref.get(penRackModel.prefPenKey, FTPenType.pen.name))
        if (dialogType == FTToolBarTools.ERASER && sizeImageView.id == penRackModel.fontSizes.size - 1) {
            sizeImageView.background = ContextCompat.getDrawable(requireContext(), R.mipmap.sizeauto_sel)
        } else {
            DrawableUtil.setGradientDrawableColor(sizeImageView, "#000000", 0)
        }
        tempSizeView = sizeImageView
        if (dialogType != FTToolBarTools.ERASER) {
            if (favoritesAdapter != null && !isFavorite) {
                updateFavoritesInfo(false)
            }
            setFavoriteIcon(isFavorite)
        }
    }

    private fun initFavoritesLayout() {
        //Initialize provider and adapter and set data
        favoritesProvider = FTFavoritesProvider(activity)
        favoritesAdapter = FTFavoritesAdapter(context, this, false)
        favoritesAdapter.setSelectedFavorite(selectedFavoritePen)
        val favorites = favoritesProvider.favorites
        if (favorites != null && !favorites.isEmpty()) {
            favoritesAdapter.data = favorites
        }
        favoritesRecyclerView.adapter = favoritesAdapter
        GestureManager.Builder(favoritesRecyclerView)
                .setSwipeEnabled(false)
                .setLongPressDragEnabled(true)
                .build()
        favoritesAdapter.setDataChangeListener(object : GestureAdapter.OnDataChangeListener<Favorite> {
            override fun onItemRemoved(favorite: Favorite, position: Int) {
                //Not working with swiping now.
            }

            override fun onItemReorder(favorite: Favorite, fromPosition: Int, toPosition: Int) {
                favoritesProvider.saveAllFavorites(favoritesAdapter.data)
            }
        })
        //Check dialog type and hide favorites choose layout based on it
        if (dialogType == FTToolBarTools.PEN) {
            favoritesChooseLayout.visibility = View.VISIBLE
            favoritesChoosenPens.text = getString(R.string.pens)
            favouriteIcon.visibility = View.VISIBLE
            //Get last clicked favorite layout option
            if (FTApp.getPref().get(SystemPref.FAVORITE_PENS_CLICKED, false) && favoritesAdapter.itemCount > 0) {
                favoritesChoosenFavorites.performClick()
            } else {
                favoritesChoosenPens.performClick()
            }
        } else if (dialogType == FTToolBarTools.HIGHLIGHTER) {
            favouriteIcon.visibility = View.VISIBLE
            favoritesChooseLayout.visibility = View.VISIBLE
            favoritesChoosenPens.text = getString(R.string.highlighters)
            //Get last clicked favorite layout option
            if (FTApp.getPref().get(SystemPref.FAVORITE_HIGHLIGHTER_CLICKED, false) && favoritesAdapter.itemCount > 0) {
                favoritesChoosenFavorites.performClick()
            } else {
                favoritesChoosenPens.performClick()
            }
        }
    }

    @OnClick(R.id.favoritesChoosenPens)
    fun onPensOptionClicked() {
        FTFirebaseAnalytics.logEvent("ViewPens")
        FTLog.crashlyticsLog("Penrack: ViewPens")
        if (dialogType == FTToolBarTools.PEN) {
            FTApp.getPref().save(SystemPref.FAVORITE_PENS_CLICKED, false)
        } else if (dialogType == FTToolBarTools.HIGHLIGHTER) {
            FTApp.getPref().save(SystemPref.FAVORITE_HIGHLIGHTER_CLICKED, false)
        }
        //Show pens related layout and views
        nonFavoritelayout.visibility = View.VISIBLE
        favouriteIcon.visibility = View.VISIBLE
        favoritesRecyclerView.visibility = View.GONE
        favToolBarLyt.visibility = View.GONE
        noFavMessageLayout.visibility = View.GONE
        //Change favorites and pens option color
        favoritesChoosenPens.setBackgroundResource(R.drawable.penorfavracktitle_sltd_rounded_bg)
        favoritesChoosenFavorites.setBackgroundColor(Color.parseColor("#00000000"))
        setFavoriteIcon(isFavorite)
    }

    @OnClick(R.id.favoritesChoosenFavorites)
    fun onFavoritesOptionClicked(favouritesTitleView: MaterialTextView) {
        FTFirebaseAnalytics.logEvent("ViewPensFavorites")
        FTLog.crashlyticsLog("Penrack: ViewPensFavorites")

        if (dialogType == FTToolBarTools.PEN) {
            FTApp.getPref().save(SystemPref.FAVORITE_PENS_CLICKED, true)
        } else if (dialogType == FTToolBarTools.HIGHLIGHTER) {
            FTApp.getPref().save(SystemPref.FAVORITE_HIGHLIGHTER_CLICKED, true)
        }
        favoritesAdapter.updateEditMode(false)
        favoritesChooseLayout.visibility = View.VISIBLE
        nonFavoritelayout.visibility = View.INVISIBLE
        favouriteIcon.visibility = View.INVISIBLE
        //Change favorites and pens option color
        favoritesChoosenPens.setBackgroundColor(Color.parseColor("#00000000"))
        favouritesTitleView.setBackgroundResource(R.drawable.penorfavracktitle_sltd_rounded_bg)
        if (favoritesAdapter.data.isNotEmpty()) {
            //Show favorites layout
            noFavMessageLayout.visibility = View.GONE
            favoritesRecyclerView.visibility = View.VISIBLE
            favToolBarLyt.visibility = View.VISIBLE
            favoritesAdapter.setSelectedFavorite(selectedFavoritePen)
        } else {
            //Show no favorites available message
            noFavMessageLayout.visibility = View.VISIBLE
            favoritesRecyclerView.visibility = View.GONE
            favToolBarLyt.visibility = View.GONE
        }
    }

    @OnClick(R.id.favouriteIcon)
    fun onFavoriteIconClicked() {
        val favorite = createFavourite()
        if (isFavorite) {
            updateFavoritesInfo(true);
        } else if (shouldAddFavourite()) {
            //Create new Favorite
            favoritesAdapter.data.add(favorite)
            favoritesProvider.saveFavorite(favorite)
            mPenPref.save(PenRackPref.CURRENT_SELECTION, mPenPref.get(penRackModel.prefPenKey, FTPenType.pen.toString()))
            mPenRackDialogListener?.favAdded(favorite)
            setFavoriteIcon(true)
            if (favoritesAdapter.data.size == 15) {
                favoritesChoosenFavorites.text = getString(R.string.favorites_full)
            }
        }
    }

    override fun onPenSelected(favorite: Favorite?) {
        favorite?.let {
            val penType = penRackModel.getPenRackGroupType(it.penType)
            penorHighlighterInfoChanged(penType, Color.parseColor(it.penColor), it.penSize, it.penType.name)
            Handler(Looper.getMainLooper()).postDelayed({
                activity?.runOnUiThread { dismiss() }
            }, 500)
        }
    }

    override fun removeFromFavorites(position: Int) {
        favoritesChooseLayout.visibility = View.VISIBLE
        //If no favorites show message
        if (favoritesAdapter.data.isNotEmpty() && position >= 0) {
            if (favoritesAdapter.data.size == 15) {
                favoritesChoosenFavorites.text = getString(R.string.favorites)
            }
            val favorite = favoritesAdapter.getItem(position)
            favoritesAdapter.remove(position)
            favoritesProvider.removeFavorite(favorite)
            mPenRackDialogListener!!.favRemoved(favorite)
        }

        if (favoritesAdapter.data.isEmpty()) {
            favToolBarLyt.visibility = View.GONE
            noFavMessageLayout.visibility = View.VISIBLE
            favoritesRecyclerView.visibility = View.GONE
            saveCheckBoxStatus(false)
            mPenRackDialogListener!!.enableFavWidgetToolbar(false)
        } else {
            noFavMessageLayout.visibility = View.GONE
            favoritesRecyclerView.visibility = View.VISIBLE
            favToolBarLyt.visibility = View.VISIBLE
        }
    }

    private val selectedFavoritePen: Favorite?
        get() {
            if (penRackModel.prefPenKey.isEmpty()) return null
            val currentColor = "#" + Integer.toHexString(mPenPref.get(penRackModel.prefColorKey, Color.parseColor("#151515"))).toUpperCase(Locale.ENGLISH)
            val currentPenType = FTPenType.valueOf(mPenPref.get(penRackModel.prefPenKey, FTPenType.pen.toString()))
            val currentPenSize = mPenPref.get(penRackModel.prefSizeKey, 1)
            val favorites = favoritesProvider.favorites
            var selectedFavoritePen: Favorite? = null
            if (favorites != null && favorites.isNotEmpty()) {
                for (favorite in favorites) {
                    if (favorite.penColor == currentColor && favorite.penType == currentPenType && favorite.penSize == currentPenSize) {
                        selectedFavoritePen = favorite
                        break
                    }
                }
            }
            return selectedFavoritePen
        }

    private fun shouldAddFavourite(): Boolean {
        if (favoritesAdapter.itemCount >= 15) {
            favoritesChoosenFavorites.text = getString(R.string.favorites_full)
            favoritesChoosenFavorites.setTextColor(Color.parseColor("#ff0000"))
            favoritesChoosenFavorites.postDelayed({ updateFavoritesInfo(false) }, 2000)
            return false
        }
        return true
    }

    private fun createFavourite(): Favorite {
        val currentPenType = FTPenType.valueOf(mPenPref.get(penRackModel.prefPenKey, FTPenType.pen.toString()))
        val color = "#" + Integer.toHexString(mPenPref.get(penRackModel.prefColorKey, Color.parseColor("#151515"))).toUpperCase(Locale.ENGLISH)
        val currentColor = color
        val currentPenSize = mPenPref.get(penRackModel.prefSizeKey, 1)
        return Favorite(currentColor, currentPenType, currentPenSize)
    }

    private val isFavorite: Boolean
        get() = favoritesProvider.isFavorite(selectedFavoritePen)

    private fun setFavoriteIcon(isFavorite: Boolean) {
        favouriteIcon.setImageResource(if (isFavorite) R.drawable.iconfavoriteon else R.drawable.iconfavorite)
    }

    private fun updateFavoritesInfo(isAdded: Boolean) {
        try {
            if (isAdded) {
                val prevText = favoritesChoosenFavorites.text.toString();
                favoritesChoosenFavorites.text = getString(R.string.added)
                favoritesChoosenFavorites.setTextColor(Color.parseColor("#ff0000"))
                favoritesChoosenFavorites.postDelayed({
                    if(favoritesChoosenFavorites != null) {
                        if (this.isAdded) {
                            favoritesChoosenFavorites.text = prevText
                            favoritesChoosenFavorites.setTextColor(ContextCompat.getColor(favoritesChoosenFavorites.context, android.R.color.black))
                        }
                    }
                }, 1000)
            } else {
                favoritesChoosenFavorites.setTextColor(ContextCompat.getColor(favoritesChoosenFavorites.context, android.R.color.black))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertDpToPx(dpValue: Int): Int {
        return ScreenUtil.convertDpToPx(context, dpValue)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.autoSelectPrevToolSwitch -> {
                FTFirebaseAnalytics.logEvent("Eraser_AutoSelectPrevTool")
                FTLog.crashlyticsLog("Penrack: Eraser_AutoSelectPrevTool")
                switchLogic(isChecked, R.id.autoSelectPrevToolSwitch)
            }
            R.id.eraseEntireStrokeSwitch -> {
                FTFirebaseAnalytics.logEvent("Eraser_EraseStroke")
                FTLog.crashlyticsLog("Penrack: Eraser_EraseStroke")
                switchLogic(isChecked, R.id.eraseEntireStrokeSwitch)
            }
            R.id.eraseHighlighterStrokeSwitch -> {
                FTFirebaseAnalytics.logEvent("Eraser_EraseHighlighter")
                FTLog.crashlyticsLog("Penrack: Eraser_EraseHighlighter")
                switchLogic(isChecked, R.id.eraseHighlighterStrokeSwitch)
            }
        }
    }

    private fun switchLogic(isChecked: Boolean, viewID: Int) {
        when (viewID) {
            R.id.autoSelectPrevToolSwitch -> {
                mPenPref.save(PenRackPref.AUTO_SELECTION_PREVIOUS_TOOL_SWITCH, isChecked)
            }
            R.id.eraseEntireStrokeSwitch -> {
                mPenPref.save(PenRackPref.ERASE_ENTIRE_STROKE_SWITCH, isChecked)
            }
            R.id.eraseHighlighterStrokeSwitch -> {
                mPenPref.save(PenRackPref.ERASE_HIGHLIGHTER_STROKE_SWITCH, isChecked)
            }
        }
    }

    interface PenRackDialogListener {
        fun onClearPageSelected()
        fun penOrHighlighterInfoChanged(mPenOrHighlighterInfo: PenOrHighlighterInfo?)
        fun favAdded(favorite: Favorite)
        fun favRemoved(favorite: Favorite)
        fun enableFavWidgetToolbar(checked: Boolean)
    }

    companion object {
        @JvmStatic
        fun newInstance(tool: FTToolBarTools): FTPenRackDialog {
            val penRackDialog = FTPenRackDialog()
            penRackDialog.dialogType = tool
            return penRackDialog
        }
    }
}