package com.mmt.cheney;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

public class Object implements Serializable,Cloneable {

    @Getter
    private Object forward = null;
    @Getter
    private int[] fields;
    private int size;
    @Getter
    private int addr;

    public Object(int[] fields, int size, int addr) {
        this.fields = fields;
        this.size = size;
        this.addr = addr;
    }

    /**
     * 跳转标记， 表示对象已经被移到了新的地址
     * @param newObj
     */
    public void forwardTo(Object newObj){
        this.forward = newObj;
    }

    public boolean isForward(){
        return forward!=null;
    }

    /**
     * 对象的大小
     * 本身size + 字段引用（按4算）
     * @return
     */
    public int size(){
        return 1;
    }

    @Override
    public Object clone() {
        try {
            return (Object) super.clone();
        } catch (CloneNotSupportedException e) {
            return this;
        }
    }
}
