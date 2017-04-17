package com.example.yh.myapplication.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yh.myapplication.R;
import com.example.yh.myapplication.adapters.Picture1RecylerViewAdapter;
import com.example.yh.myapplication.api.GetPictureApi;
import com.example.yh.myapplication.base.BasicFragment;
import com.example.yh.myapplication.entities.PictureBean;
import com.example.yh.myapplication.network.HttpUrls;
import com.example.yh.myapplication.result.PictureResult;
import com.example.yh.myapplication.views.IView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fixme
 * Author: YH
 * Date: 2017/04/07 13:38
 * Copyright (c) 2016 d2cmall. All rights reserved.
 */

public class PictureFragment1 extends BasicFragment implements IView<PictureResult>{
    @BindView(R.id.mrecylerview) RecyclerView mRecyclerView;
    private Picture1RecylerViewAdapter mPicture1RecylerViewAdapter;
    private List<PictureBean> mList;
    private int orientation=LinearLayoutManager.HORIZONTAL;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View v= inflater.inflate(R.layout.fatgment_picture1,container,false);
        ButterKnife.bind(this,v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mList=new ArrayList<>();
        mPicture1RecylerViewAdapter=new Picture1RecylerViewAdapter(mList,R.layout.reycelerview_picture1_item);
        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mPicture1RecylerViewAdapter);
        GetPictureApi.getPictureBeans(HttpUrls.PICTURESURL+1,0,this);
    }

    @Override
    public void getBean(PictureResult v) {
        if(v.getResults()!=null &&v.getResults().size()>0) {
            mList.addAll(v.getResults());
            mPicture1RecylerViewAdapter.notifyDataSetChanged();
          //  mPicture1RecylerViewAdapter.notify();
        }
    }
}
