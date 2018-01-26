# EasyAdapterForRecyclerView

##### 介绍
EasyAdapter是用于RecyclerView的适配器，在原有的适配器基础上可支持监听相应的事件并设置点击模式、单选和多选模式。在多选模式下，可设置最大可选数量，以及提供了全选、反选等接口。
##### 使用
Gradle
```
compile 'com.hz.androids.easyadapter:library:1.1'
```
or Maven
```
<dependency>
  <groupId>com.hz.androids.easyadapter</groupId>
  <artifactId>library</artifactId>
  <version>1.1</version>
  <type>pom</type>
</dependency>
```
1.自定义Adapter继承EasyAdapter

```java
 private class MyAdapter extends EasyAdapter<MyViewHolder> {
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ...
    }

    /*
       whenBindViewHolder方法:相当于原生Adapter.onBindViewHolder
    */
    @Override
    public void whenBindViewHolder(MyViewHolder holder, int position) {
        ...
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
```
2.RecycleView设置自定义的适配器

```java
MyAdapter myAdapter = new MyAdapter();
recyclerView.setAdapter(myAdapter);
```
3.可切换点击、单选、多选模式

```java
//点击模式
myAdapter.setSelectMode(EasyAdapter.SelectMode.CLICK);
//单选模式
myAdapter.setSelectMode(EasyAdapter.SelectMode.SINGLE_SELECT);
//多选模式
myAdapter.setSelectMode(EasyAdapter.SelectMode.MULTI_SELECT);
```

4.在自定义适配器中设置相应模式的监听器

```java
// 监听点击事件
myAdapter.setOnItemClickListener(new EasyAdapter.OnItemClickListener() {
    @Override
    public void onSelected(int clickPosition) {
        ...
    }
});
//监听单选事件
myAdapter.setOnItemSelectListener(new EasyAdapter.OnItemSelectListener() {
    @Override
    public void onSelected(int selectedPosition) {
        ...
    }
});
//监听多选事件
myAdapter.setOnItemMultiSelectListener(new EasyAdapter.OnItemMultiSelectListener() {
    @Override
    public void onMultiSelected(int multiSelectedPosition) {
        ...
});
```

