package com.app.feng.circle_progressball;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.circleprogressball.CircleProgressBall;

public class MainActivity extends AppCompatActivity {

    private CircleProgressBall progressBall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_progressball);

        progressBall = findViewById(R.id.progressBall);

    }

    @Override
    protected void onResume() {
        super.onResume();

        new TaskSomeThing2().execute(new Object());
        progressBall.begin();

        progressBall.setOnCircleEventListener(new CircleProgressBall.OnCircleEventListener() {
            @Override
            public void onCancel(int mProgress) {
                //用户点击cancel按钮
            }

            @Override
            public void onFinish() {
                //完成
            }
        });
    }

    public class TaskSomeThing extends AsyncTask<Object, Integer, Integer> {
        int temp = 0;

        @Override
        protected Integer doInBackground(Object[] params) {
            while (true) {
                try {
                    Thread.sleep(300);
                    temp++;
                    publishProgress(temp);
                    if (temp == 100) {
                        return null;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //用户点击cancel按钮 不再发送progress
            if (!progressBall.getCancelFlag()) {
                progressBall.setProgress(temp);
            }
        }
    }

    /**
     * Indeterminatre模式任务
     */
    public class TaskSomeThing2 extends AsyncTask<Object, Integer, Integer> {
        int temp = 0;

        @Override
        protected Integer doInBackground(Object[] params) {
            while (true) {
                try {
                    Thread.sleep(600);
                    temp++;
                    if (temp == 100) {
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
}
