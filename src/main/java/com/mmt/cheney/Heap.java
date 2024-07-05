package com.mmt.cheney;

import java.util.List;

public class Heap {

    private HeapSpace toSpace;
    private HeapSpace fromSpace;

    private Object[] roots = new Object[10];//根节点对象

    public Heap(int bottom, int end) {
        //中间值
        int mid = bottom + (end-bottom)/2;
        fromSpace = new HeapSpace(bottom, mid);
        toSpace = new HeapSpace(mid, end);
    }

    /**
     * 垃圾收集
     */
    public void collect(){
        //当前对象都在from区， to区是空的
        //算法核心：
        //依据三色标记法，未扫描的是白色，默认所有对象都是白色，不用记录
        //灰色开始扫描，还未扫描结束，需要一个队列来记录灰色对象，同时灰色对象也必定是可达对象，要被移动到to区
        // 所以将灰色对象移动到to区，既实现了对象移动，又利用了to区充当已扫描对象的临时存储队列。
        //黑色对象是所有子对象都扫描结束的，通过增加一个scanned指针，用于区分to区的黑色和灰色对象即可
        int scanned = toSpace.getBottom();

        //开始从根节点进行扫描
        for (int i = 0; i < roots.length; i++) {
            Object obj = roots[i];

            if(obj!=null && fromSpace.contains(obj.getAddr())){
                //对象还存活者
                //需要移动对象，并更新引用
                Object newObj=null;
                if(obj.isForward()){
                    //已经移动过了， 直接从标记中取
                    newObj = obj.getForward();
                }else{
                    //还没移动过
                    newObj = evacuate(obj);
                }
                //需要更新指针（java 更新引用）
                roots[i] = newObj;
            }
        }

        //以广度优先算法扫描引用有向图
        //从to区取灰色对象扫描，扫描完成后移动scanned指针
        while (scanned < toSpace.getTop()){
            Object parentObj = toSpace.get(scanned);
            for (int i = 0; i < parentObj.getFields().length; i++) {
                int fieldAddr = parentObj.getFields()[i];
                if(fromSpace.contains(fieldAddr)){
                    Object filedObj = fromSpace.get(fieldAddr);
                    //对象还存活者
                    //需要移动对象，并更新引用
                    Object newObj=null;
                    if(filedObj.isForward()){
                        //已经移动过了， 直接从标记中取
                        newObj = filedObj.getForward();
                    }else{
                        //还没移动过
                        newObj = evacuate(filedObj);
                    }
                    //需要更新指针（java 更新引用）
                    parentObj.getFields()[i] = newObj.getAddr();
                }
            }
            //扫描下一个
            scanned ++;
        }
    }

    /**
     * from区和to区调换
     * 同时将to区空间重置
     */
    public void swapSpaces(){
        HeapSpace temp = fromSpace;
        fromSpace = toSpace;
        toSpace = temp;

        toSpace.reset();
    }

    /**
     * 移动存活对象
     * @param obj
     */
    public Object evacuate(Object obj){
        //在to区分配空间
        //将from区对象拷贝到to区
        Object newObj = toSpace.allocateAndCopy(obj);
        //将from区的老对象添加跳转标记
        obj.forwardTo(newObj);
        return newObj;
    }

}
