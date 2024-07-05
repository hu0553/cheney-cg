### 简介

通过阅读R大对GC算法的介绍([HotSpot VM Serial GC的一个问题 - 讨论 - 高级语言虚拟机 - ITeye群组](https://hllvm-group.iteye.com/group/topic/39376#post-257329))，实现了一个java版的Cheney算法&#x20;

### 实现

通过实现三个类描述了大概的算法

Heap类：表示了堆

HeapSpace：堆内空间，如from区、to区

Object：对象

#### HeapSpace

堆内空间通过3个指针描述了空间.

bottom：空间的起始地址

top: 已分配与未分配的分割点

end： 空间结束地址

```javascript
|[ 已分配并且已扫描完的对象 ]|[ 已分配但未扫描完的对象 ]|[ 未分配空间 ]|
^                        ^                      ^             ^
bottom                   scanned                top           end
```

HeapSpace的主要功能就是分配空间，写入对象。

#### Heap

堆对象包含了堆内空间的引用

```java
private HeapSpace toSpace;
private HeapSpace fromSpace;

//初始化堆空间
int mid = bottom + (end-bottom)/2;
fromSpace = new HeapSpace(bottom, mid);
toSpace = new HeapSpace(mid, end);
```

Heap的功能主要是对to、from的交换，垃圾回收

重点算法就是Heap的垃圾回收，使得的是标记-复制法

算法分为三个过程：

1.  通过可达性标记出与根对象集合有关联的对象
2.  复制被标记的对象
3.  更新引用

其中可达性标记，本质是一个有向图搜索，图的遍历有两种广度优先和深度优先，下面采用的是广度优先遍历

```java
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
```

### 问题

Cheney算法对图遍历时，使用了广度优先算法，可以节约一个对遍历对象的存储集合。但同时也存在一个缺陷。

广度优先遍历是按层进行遍历的，这就造成对象复制到to区时，对象是以层进行排列的，而java对象的访问特性是父子对象往往被一同访问，而同层的兄弟对象很少被一起访问。

所以按java特性来说，用深度优先遍历才是最适合的。但使用深度优先遍历就无法利用to区进行存储队列，就不得不新建一个集合。

### JVM的解决方式

...
