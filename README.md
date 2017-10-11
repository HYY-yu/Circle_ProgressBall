# Circle_ProgressBall

[![](https://jitpack.io/v/HYY-yu/Circle_ProgressBall.svg)](https://jitpack.io/#HYY-yu/Circle_ProgressBall)

ProgressBall 是一个可提示用户当前程序正忙的控件，效果图如下：


Indeterminate 模式：
--- 
![github](https://github.com/HYY-yu/Circle_ProgressBall/blob/master/circle_indeterminate.gif "github")

progress 模式：
---
![github](https://github.com/HYY-yu/Circle_ProgressBall/blob/master/circle_progress.gif "github")

自定义
---
可以定制一下属性：
        
名称|格式|介绍
----|----|----
circle_radius| format="color"| 整个进度球的大小
circle_progress| format="integer" |设置进度 默认为0 范围 0 - 100
circle_colors" |format="reference"  |颜色列表(string-array)， 当进度球为progress模式，会跟随progress的数值进行颜色渐变，当进度球为Indeterminate模式，会根据这个列表动态渐变
circle_max_smallball_count" |format="integer"  |最多出现几个小球
circle_number_font" |format="reference"  |支持Android O 出的xml字体，可把字体设置到progress模式下的数字中
circle_disable_cancel_buttonh" |format="dimension" | 不显示取消按钮
circle_indeterminate" |format="dimension"  |indeterminate 模式
    
使用
--- 
 在工程的build.gradle下添加：
 ``` 
 allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 ```
 
 在项目的build.gradle下:
 ```compile 'com.github.HYY-yu:Circle_ProgressBall:1.0.0'```
 
 在xml文件中定制好属性，必须在代码中 circleprogressball.begin()开启动画。
 - 若使用 indeterminate 模式 ，请注意可以用circleprogressball.indeterminateFinish()方法告诉控件进度完成。
 - 若使用 progress 模式，通过setProgress设置进度，注意进度只能为 0 - 100
 - 任何模式都可以调用 error() 方法，通知用户进度出错。
 
 示例：
 ```java
    progressBall.begin();
    startTask()
       /**
      * Indeterminatre模式任务
      */
     public class Task extends AsyncTask<Object, Integer, Integer> {
         int temp = 0;
 
         @Override
         protected Integer doInBackground(Object[] params) {
             while (true) {
                 try {
                     Thread.sleep(300);
                     temp++;
                     if (temp == 100) {
                         //finish
                         return null;
                     }
 
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                     //error
                     progressBall.error();
                 }
             }
         }
 
         @Override
         protected void onPostExecute(Integer integer) {
             super.onPostExecute(integer);
             progressBall.indeterminatreModeFinish();
         }
     }
 ```
