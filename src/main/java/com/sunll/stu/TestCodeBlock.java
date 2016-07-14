package com.sunll.stu;

import java.io.*;

/**
 * Created by sunll on 16/7/12.
 */
public class TestCodeBlock {

    static {
        //静态代码块
    }



    public static void main(String[] args) {

        {
            int x = 0;
            //普通代码块
        }

        int x = 1;

    }

}
