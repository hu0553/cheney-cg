package com.mmt.cheney;

import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * |[ 已分配并且已扫描完的对象 ]|[ 已分配但未扫描完的对象 ]|[ 未分配空间 ]|
 * ^                        ^                      ^             ^
 * bottom                   scanned                top           end
 */
public class HeapSpace {

    @Getter
    private int top;
    @Getter
    private int bottom;
    @Getter
    private int end;
    //以数组代替内存
    private Object[] mem;

    /**
     * 初始化一个空间，通过bottom end定义空间的收尾
     * top空间已分配的标志位
     * @param bottom
     * @param end
     */
    public HeapSpace(int bottom, int end) {
        this.top = bottom;
        this.bottom = bottom;
        this.end = end;
        mem = new Object[end-bottom];
    }

    /**
     * 空间是否包含地址
     * @param addr
     * @return
     */
    public boolean contains(int addr){
        return addr>=bottom && addr<top;
    }

    /**
     * 空间内分配一块内存并拷贝对象
     * @param obj
     * @return
     */
    public Object allocateAndCopy(Object obj){
        if(top+1<end){
            int addr = top;
            //top移动一位
            top +=1;
            //拷贝对象
            mem[addr] = obj.clone();
            return mem[addr];
        }
        throw new NoSpaceException();
    }

    public Object get(int addr){
        if(addr<top) {
            return mem[addr];
        }else{
            return null;
        }
    }

    /**
     * 重置空间，只需要移动top的位置，不需要重置内存数据。
     */
    public void reset(){
        top = bottom;
    }

}
