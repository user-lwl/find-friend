package com.lwl.findfriend.once;

import com.alibaba.excel.EasyExcelFactory;

/**
 * @author user-lwl
 * @date 2022/11/9 15:10
 */
public class ImportExcel {
    public static void main(String[] args) {
        String fileName = "";
        EasyExcelFactory.read(fileName, UserInfo.class, new TableListener()).sheet().doRead();
    }
}
