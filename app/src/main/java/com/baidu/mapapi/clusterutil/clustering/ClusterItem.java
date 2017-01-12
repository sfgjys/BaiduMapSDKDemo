/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */

package com.baidu.mapapi.clusterutil.clustering;


import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.model.LatLng;

/**
 * 该接口代表的是聚合功能中,聚合的单个条目,这个条目对象需要可以获得本条目的坐标对象LatLng和自定义的图标BitmapDescriptor对象
 */
public interface ClusterItem {

    LatLng getPosition();

    BitmapDescriptor getBitmapDescriptor();
}