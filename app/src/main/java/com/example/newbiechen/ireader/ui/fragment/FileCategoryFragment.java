package com.example.newbiechen.ireader.ui.fragment;

import android.os.Bundle;
import android.os.Environment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.example.newbiechen.ireader.R;
import com.example.newbiechen.ireader.model.local.BookRepository;
import com.example.newbiechen.ireader.ui.adapter.FileSystemAdapter;
import com.example.newbiechen.ireader.utils.FileStack;
import com.example.newbiechen.ireader.utils.FileUtils;
import com.example.newbiechen.ireader.widget.itemdecoration.DividerItemDecoration;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;

/**
 * Created by newbiechen on 17-5-27.
 */

public class FileCategoryFragment extends BaseFileFragment {
    private static final String TAG = "FileCategoryFragment";
    @BindView(R.id.file_category_tv_path)
    TextView mTvPath;
    @BindView(R.id.file_category_tv_back_last)
    TextView mTvBackLast;
    @BindView(R.id.file_category_rv_content)
    RecyclerView mRvContent;

    private FileStack mFileStack;
    @Override
    protected int getContentId() {
        return R.layout.fragment_file_category;
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        mFileStack = new FileStack();
        setUpAdapter();
    }

    private void setUpAdapter(){
        mAdapter = new FileSystemAdapter();
        mRvContent.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvContent.addItemDecoration(new DividerItemDecoration(getContext()));
        mRvContent.setAdapter(mAdapter);
    }

    @Override
    protected void initClick() {
        super.initClick();
        mAdapter.setOnItemClickListener(
                (view, pos) -> {
                    File file = mAdapter.getItem(pos);
                    if (file.isDirectory()){
                        //?????????????????????
                        FileStack.FileSnapshot snapshot = new FileStack.FileSnapshot();
                        snapshot.filePath = mTvPath.getText().toString();
                        snapshot.files = new ArrayList<File>(mAdapter.getItems());
                        snapshot.scrollOffset = mRvContent.computeVerticalScrollOffset();
                        mFileStack.push(snapshot);
                        //?????????????????????
                        toggleFileTree(file);
                    }
                    else {

                        //??????????????????????????????????????????????????????
                        String id = mAdapter.getItem(pos).getAbsolutePath();
                        if (BookRepository.getInstance().getCollBook(id) != null){
                            return;
                        }
                        //????????????
                        mAdapter.setCheckedItem(pos);
                        //??????
                        if (mListener != null){
                            mListener.onItemCheckedChange(mAdapter.getItemIsChecked(pos));
                        }
                    }
                }
        );

        mTvBackLast.setOnClickListener(
                (v) -> {
                    FileStack.FileSnapshot snapshot = mFileStack.pop();
                    int oldScrollOffset = mRvContent.computeHorizontalScrollOffset();
                    if (snapshot == null) return;
                    mTvPath.setText(snapshot.filePath);
                    mAdapter.refreshItems(snapshot.files);
                    mRvContent.scrollBy(0,snapshot.scrollOffset - oldScrollOffset);
                    //??????
                    if (mListener != null){
                        mListener.onCategoryChanged();
                    }
                }
        );

    }

    @Override
    protected void processLogic() {
        super.processLogic();
        File root = Environment.getExternalStorageDirectory();
        toggleFileTree(root);
    }

    private void toggleFileTree(File file){
        //?????????
        mTvPath.setText(getString(R.string.nb_file_path,file.getPath()));
        //????????????
        File[] files = file.listFiles(new SimpleFileFilter());
        //?????????List
        List<File> rootFiles = Arrays.asList(files);
        //??????
        Collections.sort(rootFiles,new FileComparator());
        //??????
        mAdapter.refreshItems(rootFiles);
        //??????
        if (mListener != null){
            mListener.onCategoryChanged();
        }
    }

    @Override
    public int getFileCount(){
        int count = 0;
        Set<Map.Entry<File, Boolean>> entrys = mAdapter.getCheckMap().entrySet();
        for (Map.Entry<File, Boolean> entry:entrys){
            if (!entry.getKey().isDirectory()){
                ++count;
            }
        }
        return count;
    }

    public class FileComparator implements Comparator<File>{
        @Override
        public int compare(File o1, File o2){
            if (o1.isDirectory() && o2.isFile()) {
                return -1;
            }
            if (o2.isDirectory() && o1.isFile()) {
                return 1;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public class SimpleFileFilter implements FileFilter{
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().startsWith(".")){
                return false;
            }
            //????????????????????????0
            if (pathname.isDirectory() && pathname.list().length == 0){
                return false;
            }

            /**
             * ???????????????TXT???????????????
             */
            //??????????????????,????????????txt?????????
            if (!pathname.isDirectory() &&
                    (pathname.length() == 0 || !pathname.getName().endsWith(FileUtils.SUFFIX_TXT))){
                return false;
            }
            return true;
        }
    }
}
