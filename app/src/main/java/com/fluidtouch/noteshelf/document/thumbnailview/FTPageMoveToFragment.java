package com.fluidtouch.noteshelf.document.thumbnailview;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.document.enums.FTCoverOverlayStyle;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.FTDiskItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.adapters.FTCategoryMoveToAdapter;
import com.fluidtouch.noteshelf.shelf.adapters.FTShelfMoveToAdapter;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf.shelf.enums.RKShelfItemType;
import com.fluidtouch.noteshelf.shelf.fragments.FTRenameDialog;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Vineet on 5/06/2019
 */

public class FTPageMoveToFragment extends Fragment implements FTCategoryMoveToAdapter.FTCategoryMoveToAdapterCallback, FTShelfMoveToAdapter.FTShelfMoveToAdapterCallback {
    //region View Bindings
    @BindView(R.id.page_move_recycler_view)
    RecyclerView mShelfItemRecyclerView;
    @BindView(R.id.page_move_title_back)
    TextView mPanelTitleTextView;
    @BindView(R.id.page_move_new_notebook_edit_text)
    EditText mNewNotebookEditText;
    @BindView(R.id.page_move_new_notebook_divider)
    View mNewNotebookDivider;
    //endregion

    //region Member variables
    private ArrayList<FTNoteshelfPage> pages;
    private FTDiskItem diskItem;
    private PageMoveCallback listener;
    private FTShelfMoveToAdapter shelfMoveToAdapter;
    private final FTSmartDialog smartDialog = new FTSmartDialog();
    //endregion

    //region Instance
    public static FTPageMoveToFragment newInstance(ArrayList<FTNoteshelfPage> pages, FTDiskItem itemType) {
        FTPageMoveToFragment pageMoveToFragment = new FTPageMoveToFragment();
        pageMoveToFragment.pages = pages;
        pageMoveToFragment.diskItem = itemType;
        return pageMoveToFragment;
    }
    //endregion

    //region Fragment methods
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            this.listener = (PageMoveCallback) getParentFragment();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moveto_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        if (this.diskItem == null) {
            showNewNotebookLayout(false);
            mPanelTitleTextView.setText(getString(R.string.move_to));
            FTShelfCollectionProvider.getInstance().shelfs(shelfs -> {
                FTNoteshelfDocument document = pages.get(0).getParentDocument();
                String path = document.getFileURL().getPath().split(".shelf/")[0] + ".shelf";
                FTUrl ftUrl = new FTUrl(path);
                FTShelfItemCollection shelfItemCollection = new FTShelfItemCollection(ftUrl);
                FTCategoryMoveToAdapter ftCategoryMoveToAdapter = new FTCategoryMoveToAdapter(FTPageMoveToFragment.this, shelfItemCollection, false);
                ftCategoryMoveToAdapter.addAll(shelfs);
                mShelfItemRecyclerView.setAdapter(ftCategoryMoveToAdapter);
            });
        } else if (this.diskItem instanceof FTShelfItemCollection) {
            showNewNotebookLayout(true);
            FTShelfItemCollection shelfItemCollection = (FTShelfItemCollection) diskItem;
            mPanelTitleTextView.setText(diskItem.getDisplayTitle(getContext()));
            shelfItemCollection.shelfItems(getContext(), FTShelfSortOrder.BY_NAME, null, "", (notebooks, error) -> {
                if (notebooks != null && error == null && !pages.isEmpty()) {
                    FTNoteshelfDocument document = pages.get(0).getParentDocument();
                    String path = document.getFileURL().getPath();
                    if (path.contains(".group")) {
                        path = path.split(".group/")[0] + ".group";
                    }
                    FTUrl ftUrl = new FTUrl(path);
                    FTShelfItem shelfItem = new FTShelfItem(ftUrl);
                    shelfMoveToAdapter = new FTShelfMoveToAdapter(FTPageMoveToFragment.this, shelfItem, false);
                    shelfMoveToAdapter.addAll(notebooks);
                    mShelfItemRecyclerView.setAdapter(shelfMoveToAdapter);
                }
            });
        } else if (this.diskItem instanceof FTGroupItem) {
            showNewNotebookLayout(true);
            FTGroupItem groupItem = (FTGroupItem) diskItem;
            mPanelTitleTextView.setText(diskItem.getDisplayTitle(getContext()));
            groupItem.getShelfCollection().shelfItems(getContext(), FTShelfSortOrder.BY_NAME, groupItem, "", (notebooks, error) -> {
                if (notebooks != null && error == null && !pages.isEmpty()) {
                    FTShelfItem shelfItem = new FTShelfItem(pages.get(0).getParentDocument().getFileURL());
                    shelfMoveToAdapter = new FTShelfMoveToAdapter(FTPageMoveToFragment.this, shelfItem, false);
                    shelfMoveToAdapter.addAll(notebooks);
                    mShelfItemRecyclerView.setAdapter(shelfMoveToAdapter);
                }
            });
        }
    }

    //region OnClick events
    @OnClick(R.id.page_move_new_notebook_edit_text)
    void onNewNotebookClicked() {
        mNewNotebookEditText.setText(getString(R.string.untitled_notebook));
        mNewNotebookEditText.setShowSoftInputOnFocus(true);
    }

    @OnClick({R.id.page_move_title_back, R.id.moveto_page_back_image_view})
    void onBackClicked() {
        if (getParentFragment() instanceof FTThumbnailFragment) {
            FTThumbnailFragment thumbnailFragment = (FTThumbnailFragment) getParentFragment();
            thumbnailFragment.getChildFragmentManager().beginTransaction()
                    .setCustomAnimations(0, R.anim.slide_end_left_to_right)
                    .remove(FTPageMoveToFragment.this)
                    .commit();
            thumbnailFragment.getChildFragmentManager().popBackStack();
        }
    }
    //endregion

    @Override
    public void showInCategoryPanel(FTShelfItemCollection ftShelfItemCollection) {
        FTPageMoveToFragment pageMoveToFragment = FTPageMoveToFragment.newInstance(pages, ftShelfItemCollection);
        if (getFragmentManager() != null) {
            if (getParentFragment() instanceof FTThumbnailFragment) {
                FTThumbnailFragment thumbnailFragment = (FTThumbnailFragment) getParentFragment();
                thumbnailFragment.getChildFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_end_right_to_left, 0)
                        .add(R.id.moveto_panel_container, pageMoveToFragment)
                        .commit();
            }
        }
    }

    @Override
    public void showInGroupPanel(FTGroupItem ftGroupItem) {
        FTPageMoveToFragment pageMoveToFragment = FTPageMoveToFragment.newInstance(pages, ftGroupItem);
        if (getFragmentManager() != null) {
            if (getParentFragment() instanceof FTThumbnailFragment) {
                FTThumbnailFragment thumbnailFragment = (FTThumbnailFragment) getParentFragment();
                thumbnailFragment.getChildFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_end_right_to_left, 0)
                        .add(R.id.moveto_panel_container, pageMoveToFragment)
                        .commit();
            }
        }
    }

    @Override
    public void onNotebookClicked(FTShelfItem notebook) {
        if (pages.size() > 0) {
            FTNoteshelfDocument sourceDoc = pages.get(0).getParentDocument();
            FTNoteshelfDocument destinationDoc = FTDocumentFactory.documentForItemAtURL(notebook.getFileURL());
            if (destinationDoc.getDocumentUUID().equals(sourceDoc.getDocumentUUID())) {
                Toast.makeText(getContext(), R.string.cannot_move_to_same_notebook, Toast.LENGTH_SHORT).show();
            } else {
                if (listener != null) {
                    if (getFragmentManager() != null) {
                        for (Fragment childFragment : getFragmentManager().getFragments()) {
                            if (childFragment instanceof FTRenameDialog) {
                                ((FTRenameDialog) childFragment).dismiss();
                            } else if (childFragment instanceof FTPageMoveToFragment) {
                                ((FTPageMoveToFragment) childFragment).onBackClicked();
                            }
                        }
                    }
                    if (!smartDialog.isAdded())
                        smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                                .setMessage(getString(R.string.moving))
                                .show(getParentFragmentManager());
                    listener.onMovePages(destinationDoc, smartDialog);
                }
            }
        }
    }

    //region Helper methods
    // Redundant method from FTBaseShelfActivity.java
    private void createNotebookWithDefaultOptions(String name) {
        smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                .setMessage(getString(R.string.moving))
                .show(getParentFragmentManager());
        AsyncTask.execute(() -> {
            FTNCoverTheme coverTheme = null;
            FTNTheme paperTheme = null;
            String coverPackName = FTApp.getPref().get(SystemPref.RECENT_COVER_THEME_NAME, FTConstants.DEFAULT_COVER_THEME_NAME);
            if (FTNTheme.theme(FTNThemeCategory.getUrl(coverPackName)) instanceof FTNCoverTheme && coverPackName.endsWith(".nsc")) {
                coverTheme = (FTNCoverTheme) FTNTheme.theme(FTNThemeCategory.getUrl(coverPackName));
            }

            if (coverTheme == null || coverTheme.themeThumbnail(getContext()) == null) {
                coverTheme = new FTNThemeCategory(getContext(), "Simple", FTNThemeCategory.FTThemeType.COVER).getCoverThemeForPackName(FTConstants.DEFAULT_COVER_THEME_NAME);
                FTApp.getPref().save(SystemPref.RECENT_COVER_THEME_NAME, FTConstants.DEFAULT_COVER_THEME_NAME);
            }

            paperTheme = new FTNThemeCategory(getContext(), "Simple", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);

            createNewShelfItem(name, coverTheme, paperTheme);
        });
    }
    //endregion

    //Redundant method from FTBaseShelfActivity.java
    private void createNewShelfItem(final String name, FTNCoverTheme coverTheme, FTNTheme paperTheme) {
        final FTUrl fileUri = FTDocumentFactory.tempDocumentPath(FTDocumentUtils.getUDID());
        final FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(fileUri);
        Log.d("TemplatePicker==>"," Sample Notebook FTPageMoveToFragment createNewShelfItem");
        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        paperTheme.template(getContext(), (documentInfo, generationError) -> {
            if (coverTheme != null) {
                documentInfo.overlayStyle = FTCoverOverlayStyle.DEFAULT_STYLE;
                documentInfo.setCoverTheme(coverTheme);
            }

            document.createDocument(getContext(), documentInfo, (success, error) -> {
                if (success) {
                    if (diskItem != null) {
                        FTShelfItemCollection shelfItemCollection = null;
                        FTGroupItem groupItem = null;
                        if (diskItem.getType() == RKShelfItemType.GROUP) {
                            groupItem = (FTGroupItem) diskItem;
                            shelfItemCollection = ((FTGroupItem) diskItem).getShelfCollection();
                        } else {
                            shelfItemCollection = (FTShelfItemCollection) diskItem;
                        }
                        if (shelfItemCollection != null) {
                            shelfItemCollection.addShelfItemForDocument(getContext(), name, groupItem, (documentItem, error1) -> {
                                if (isAdded() && getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        if (error1 == null) {
                                            if (shelfMoveToAdapter != null) {
                                                shelfMoveToAdapter.add(documentItem);
                                            }
                                            onNotebookClicked(documentItem);
                                        } else {
                                            Toast.makeText(getContext(), error1.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }, document.getFileURL());
                        }
                    }
                } else {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });
    }

    private void showNewNotebookLayout(boolean show) {
        mNewNotebookDivider.setVisibility(show ? View.VISIBLE : View.GONE);
        mNewNotebookEditText.setVisibility(show ? View.VISIBLE : View.GONE);
        mNewNotebookEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER) &&
                    getActivity() != null && getActivity().getWindow() != null && !TextUtils.isEmpty(mNewNotebookEditText.getText().toString())) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                createNotebookWithDefaultOptions(mNewNotebookEditText.getText().toString());
                return true;
            }
            return false;
        });
    }

    public interface PageMoveCallback {
        void onMovePages(FTNoteshelfDocument destinationDocument, FTSmartDialog smartDialog);
    }
    //endregion
}