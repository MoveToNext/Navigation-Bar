package com.example.navigationbar.suspension;

/**
 * <pre>
 * PackageName:  com.example.navigationbar.suspension
 * Description:  分类悬停的接口
 * Created by :   Liu
 * date:         2017/10/10 上午11:16
 * </pre>
 */
public interface ISuspensionInterface {
    //是否需要显示悬停title
    boolean isShowSuspension();

    //悬停的title
    String getSuspensionTag();
}
